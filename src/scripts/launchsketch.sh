#!/bin/bash

# Launch a sketch in a failsafe manner

# Example:
# ./src/scripts/launchsketch.sh ~/processing-2.2.1 src/ pixel_test

PROCESSING_DIR=$1  # Directory of the processing install (containing core/, java/, lib/, etc.)
SRC_DIR=$2  # src/ directory of lsdome repo 
SKETCH_NAME=$3  # Name of sketch

TEMPDIR=$(mktemp -d)
$SRC_DIR/scripts/buildlib.sh $PROCESSING_DIR $SRC_DIR
$PROCESSING_DIR/processing-java --sketch=$SRC_DIR/sketches/$SKETCH_NAME --run --output=$TEMPDIR --force