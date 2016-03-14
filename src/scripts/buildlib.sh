#!/bin/bash

# Recompile shared java code and place the library where processing sketches can access it.

# Example:
# ./src/scripts/buildlib.sh ~/processing-2.2.1 src/

PROCESSING_DIR=$1  # Directory of the processing install (containing core/, java/, lib/, etc.)
SRC_DIR=$2  # src/ directory of lsdome repo 

TEMPDIR=$(mktemp -d)
javac -target 1.6 -source 1.6 -cp $PROCESSING_DIR/core/library/core.jar -d $TEMPDIR $SRC_DIR/lsdome-lib/*.java
TEMPFILE=$(mktemp)
jar cf $TEMPFILE -C $TEMPDIR .
cp $TEMPFILE $PROCESSING_DIR/lib/lsdomeLib.jar
