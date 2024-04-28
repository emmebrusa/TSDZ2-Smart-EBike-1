#!/bin/bash

# Simple script to build the configurator without an IDE

set -e

BUILD_DIR="build"

rm -rf "$BUILD_DIR"
javac --release 8 -d "$BUILD_DIR" src/TSDZ2_Configurator.java
# For java 8:
# javac -d "$BUILD_DIR" src/TSDZ2_Configurator.java
jar -c -f ../../JavaConfigurator.jar -e TSDZ2_Configurator -C "$BUILD_DIR" .
