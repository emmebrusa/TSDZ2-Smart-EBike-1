#!/usr/bin/python3

# Unit-Test C with Python:
# Based on https://cffi.readthedocs.io/en/latest/using.html

import os
import shutil
import re
import subprocess
import cffi
from pycparser import c_ast, parse_file, c_generator, plyparser
from distutils import ccompiler
from typing import List
import importlib
import hashlib

MOD_PATH = "sim"
LIB_DIR = 'tests/' + MOD_PATH

source_dirs = ['src/']  # don't add hardware libraries
include_dirs = ["src/", "src/STM8S_StdPeriph_Lib/inc/"]
define_macros = ["STM8S105", "__CDT_PARSER__", ]
fake_defines = """
#define __interrupt(x)
#define __asm__(x)
#define __trap
"""
compiler_args = ["-std=c99", "-Wall", "-Wextra"]
linker_args = []
custom_parser = ''  # actually don't use sdcpp as the cdef stdint definitions should match x86 platform

# cffi uses the same compiler as distutils
# Adjust compiler arguments based on the detected compiler
if ccompiler.get_default_compiler() == "msvc":
    compiler_args = []  # Windows msvc uses some funky flags
    linker_args = []

class Checksum:
    def __init__(self, hash_file_path, include_dirs, cdef):
        self.hash_file_path = hash_file_path
        self.include_dirs = include_dirs
        self.cdef = cdef

    def __enter__(self):
        self.previous_hash = ""
        if os.path.exists(self.hash_file_path):
            with open(self.hash_file_path, 'r') as f:
                self.previous_hash = f.read().strip()

        sha256_hash = hashlib.sha256()
        sha256_hash.update(self.cdef.encode())
        with open(__file__, "rb") as f:
            sha256_hash.update(f.read())
        for include_dir in self.include_dirs:
            for root, _, files in os.walk(include_dir):
                for file in sorted(files):
                    if file.endswith(('.c', '.h')):
                        with open(os.path.join(root, file), "rb") as f:
                            for byte_block in iter(lambda: f.read(4096), b""):
                                sha256_hash.update(byte_block)  # type: ignore
        self.current_hash = sha256_hash.hexdigest()
        return self.current_hash == self.previous_hash

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is None:
            with open(self.hash_file_path, 'w') as f:
                f.write(self.current_hash)

def preprocess_ast(ast):
    names = set()
    for ext in ast.ext[:]:  # Iterate over a copy of the list
        if isinstance(ext, c_ast.Decl):
            if ext.name in names:
                ast.ext.remove(ext)
                continue
            else:
                names.add(ext.name)
            ext.init = None  # remove initializations
            if (isinstance(ext.type, c_ast.TypeDecl) or isinstance(ext.type, c_ast.PtrDecl) or isinstance(ext.type, c_ast.ArrayDecl)) and ext.storage == []:
                ext.storage = ['extern']

class HeaderGenerator(c_generator.CGenerator):
    def __init__(self, ast):
        super().__init__()
        self.typedef_dups = {}
        self.src_ast = ast

    def visit_Decl(self, n, no_type=False):
        result = super().visit_Decl(n, no_type)
        if isinstance(n.type, c_ast.FuncDecl):
            # Is a function declaration
            if not any(isinstance(ext, c_ast.FuncDef) and ext.decl.name == n.name for ext in self.src_ast.ext):
                # if not defined in source content, add Python+C decoration to allow mocking
                result = 'extern "Python+C" ' + result
        return result

    def visit_FuncDef(self, n):
        return ""  # don't generate function definitions

    def visit_Typedef(self, n, *args):
        result = super().visit_Typedef(n)
        # find duplicates enum typdefs and merge them into a single line, e.g:
        # typedef enum {RESET = 0, SET = 1} FlagStatus, ITStatus, BitStatus;
        self.typedef_dups[n.name] = []
        for ext in self.src_ast.ext:
            if isinstance(ext, c_ast.Typedef):
                if n.type.type == ext.type.type and n.name != ext.name:
                    # duplicate detected, check if it wasn't yet processed:
                    if not any([n.name in self.typedef_dups[type] for type in self.typedef_dups]):
                        self.typedef_dups[n.name].append(ext.name)
                        # inline the duplicate typedefs
                        result = result.replace(f"{'}'} {n.name}", f"{'}'} {n.name}, {ext.name}")
                    else:
                        return ""  # don't generate duplicate typedefs that were already handled above
        return result

def generate_cdef(module_name, src_file):
    # exceptions needed on some platforms (mingw)
    skip_extensions = ["__attribute__(x)=", "__extension__=", "__MINGW_EXTENSION="]
    skip_std_includes = ["_INC_STDIO", "_INC_STDDEF", "__STDDEF_H__", "_MATH_H_", "_INC_CORECRT",]
    undef_macros = []
    std_include = []
    if shutil.which(custom_parser):
        std_include.append(os.path.abspath(os.path.join(os.path.dirname(os.path.dirname(shutil.which(custom_parser))), "include")))  # type: ignore
        cpp_path = custom_parser
    else:
        print("Parsing with 'cpp'")
        cpp_path = 'cpp'
    cpp_args = ["-xc"]

    idirs = [r'-I' + d for d in include_dirs + std_include]
    ddefs = [r'-D' + d for d in define_macros + skip_std_includes + skip_extensions]
    udefs = [r'-U' + d for d in undef_macros]

    args = idirs + ddefs + udefs + cpp_args
    print("Parser args:", *args)
    print("Generating AST...")
    source_ast = parse_file(src_file, use_cpp=True, cpp_path=cpp_path, cpp_args=args)  # type: ignore
    print("Postporcess AST...")
    preprocess_ast(source_ast)
    print("generating cdef headers from AST...")
    header_generator = HeaderGenerator(source_ast)
    cdef = header_generator.visit(source_ast)
    # remove all literal casting expressions like (uint8_t) as they are not supported by cffi.cdef
    cdef = re.sub(r'\((uint8_t|uint16_t)\)\s*(\d+)', r'\2', cdef)
    assert cdef != ""  # cdef should not be empty
    with open(os.path.join(LIB_DIR, f"{module_name}.cdef"), "w", encoding="utf8", newline="\n") as fp:
        fp.write(f"// {module_name}.cdef - autogenerated by load_c_code.py\n")
        fp.write("// do not commit if only system types has changed\n\n")
        fp.write(cdef)
    return cdef

def load_code(module_name, coverage=False, force_recompile=False, strict=False):
    # Load previous combined hash
    hash_file_path = os.path.join(LIB_DIR, f"{module_name}.sha")
    # Recalculate hash if code or arguments have changed
    with Checksum(hash_file_path, source_dirs, module_name+"".join(define_macros)) as skip:
        if not skip or force_recompile or coverage or strict: # also recompile if coverage is enabled because backround test runners are compiling without gcov
            print("Collecting source code..")
            source_content_list: List[str] = []
            source_files = [os.path.abspath(os.path.join(dir, file)) for dir in source_dirs for file in os.listdir(dir) if file.endswith('.c')]
            for file_path in source_files:
                print(file_path)
                with open(file_path, encoding="utf8") as fp:
                    source_content_list.append(fp.read())
            combined_source: str = "\n".join(source_content_list)
            combined_source = fake_defines + combined_source
            combined_source = re.sub(r"#\s*include\s*<.*?>", r"//\g<0>", combined_source) # comment out standard includes
            combined_source_file_path = os.path.join(LIB_DIR, f"{module_name}.i")
            with open(combined_source_file_path, "w", encoding="utf8") as fp:
                fp.write(combined_source)
            try:
                cdef = generate_cdef(module_name, combined_source_file_path)
            except (subprocess.CalledProcessError, RuntimeError, plyparser.ParseError) as e:
                print(f"{e}\n\033[93mFailed to generate cdef using your cpp standard headers!!!\nYou may have to edit it manually. Continuing...\033[0m")
                with open(os.path.join(LIB_DIR, f"{module_name}.cdef"), "r", encoding="utf8") as fp:
                    cdef = fp.read()
            extra_compile_args = compiler_args
            extra_link_args = linker_args
            # Coverage
            if coverage:
                # expose gcov api, (will not work with microsoft compiler)
                cdef += "\n" + "extern void __gcov_reset(void);"
                cdef += "\n" + "extern void __gcov_dump(void);"
                # add inner coverage exclusion markers
                combined_source = "extern void __gcov_reset(void);\n" + combined_source
                combined_source = "extern void __gcov_dump(void);\n" + combined_source
                combined_source =  "// GCOVR_EXCL_STOP\n" + combined_source + "\n// GCOVR_EXCL_START"
                extra_compile_args += ["--coverage"]
                extra_link_args +=    ["--coverage"]
            if strict:
                extra_compile_args += ["-Werror"]
            # Create a CFFI instance
            ffibuilder = cffi.FFI()
            print("Processing cdefs...")
            ffibuilder.cdef(cdef)
            print("Compiler args:", extra_compile_args, *include_dirs)
            ffibuilder.set_source(module_name, combined_source,
                                include_dirs=[os.path.abspath(d) for d in include_dirs], 
                                define_macros=[(macro, None) for macro in define_macros],
                                extra_compile_args=extra_compile_args,
                                extra_link_args=extra_link_args
                                )
            print("Compiling...")
            tmpdir = os.path.abspath(LIB_DIR)
            ffibuilder.compile(tmpdir=tmpdir)
            if coverage: # Add outer coverage exclusion markers after generating ffi api c-code
                with open(os.path.join(LIB_DIR, f"{module_name}.c"), "r+", encoding="utf8") as fp:
                        c = fp.readlines() # add inline comments because it may matter for gcov to keep the number of lines, idk:
                        c[0] = c[0].strip() + " // GCOVR_EXCL_START"
                        c[-1] = c[-1].strip() + " // GCOVR_EXCL_STOP"
                        fp.seek(0); fp.writelines(c); fp.truncate()
        else:
            print("No changes found. Skipping compilation")


    module_path = MOD_PATH + "." + module_name
    print(f"Loading module: {module_path}")
    try:
        module = importlib.import_module(module_path)
    except Exception as e:
        os.remove(hash_file_path)
        print(f"Failed to load module: {module_path}. Hash was cleared, so try running this script again.")
        raise e
    module_ffi : cffi.FFI = module.ffi # adds typing
    return module.lib, module_ffi



if __name__ == '__main__':
    load_code("_tsdz2", force_recompile=True)
