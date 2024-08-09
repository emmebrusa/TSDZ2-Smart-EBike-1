import subprocess
import importlib
from load_c_code import load_code

def pytest_addoption(parser):
    parser.addoption("--coverage", action="store_true", help="Enable coverage analysis with gcovr")
    parser.addoption("--force", action="store_true", help="Force recompile")

def pytest_sessionstart(session):
    """
    Called after the Session object has been created and
    before performing collection and entering the run test loop.
    """
    lib, _ = load_code('_tsdz2',
        coverage=session.config.option.coverage,
        force_recompile=session.config.option.force,
        strict=session.config.option.strict)
    if session.config.option.coverage:
        lib.__gcov_reset()


def pytest_configure(config):
    """
    This hook is called for every plugin and initial conftest
    file after command line options have been parsed.
    """

def pytest_sessionfinish(session, exitstatus):
    """
    Called after whole test run finished, right before
    returning the exit status to the system.
    """
    
    if session.config.option.coverage:
        try:
            module = importlib.import_module("sim._tsdz2")
            module.lib.__gcov_dump()
        except Exception as e:
            # __gcov_dump() is stil hidden ion some compilers? Failing on CI Ubuntu 22.04, but worked on 20.04
            print(f"Error dumping gcov: {e}. Try running gcovr manually after pytest process exits.")
        try:
            # Attempt to call gcovr with the specified arguments
            subprocess.call(['gcovr', '-r', 'tests', '--print-summary'])
        except FileNotFoundError:
            # Handle the case where gcovr is not found (i.e., not installed)
            print(" Install Gcovr to generate code coverage report.")
        except Exception as e:
            # E.g. gcov will fail if cffi compiled code with msvc
            print(f"Error running gcovr: {e}")
        # btw, if there is aerror(warning) "bgcov profiling error: ... overwriting an existing profile data with a different timestamp"
        # it has to do with pytest without being called in the background by e.g. VScode without --coverage and recompiling the objects

def pytest_unconfigure(config):
    """
    called before test process is exited.
    """
