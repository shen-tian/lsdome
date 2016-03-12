#!/bin/bash

# Recompile shared java code and place the library where processing sketches can access it.

PROCESSING_DIR=$1  # Directory of the processing install (containing core/, java/, lib/, etc.)
SRC_DIR=$2  # src/ directory of lsdome repo 

TEMPDIR=$(mktemp -d)
javac -cp $PROCESSING_DIR/core/library/core.jar -d $TEMPDIR $SRC_DIR/lsdome-lib/*.java
TEMPFILE=$(mktemp)
jar cf $TEMPFILE -C $TEMPDIR .
cp $TEMPFILE $PROCESSING_DIR/lib/lsdome-lib.jar
