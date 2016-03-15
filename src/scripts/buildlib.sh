#!/bin/bash

# Usage: buildlib.sh <path of processing install>
# Recompile shared java code and build a library. Prints the location of the compiled jar to stdout.
# Example:
# ./src/scripts/buildlib.sh ~/processing-2.2.1

PROCESSING_DIR=$1  # Directory of the processing install (containing core/, java/, lib/, etc.)

SRC_DIR=$(cd $(dirname $0)/.. && pwd -P)  # src/ directory of lsdome repo 

TEMPDIR=$(mktemp -d)
javac -target 1.6 -source 1.6 -cp $PROCESSING_DIR/core/library/core.jar -d $TEMPDIR $SRC_DIR/lsdome-lib/*.java
TEMPFILE=$(mktemp)
jar cf $TEMPFILE -C $TEMPDIR .
echo $TEMPFILE
