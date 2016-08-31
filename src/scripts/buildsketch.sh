#!/bin/bash

# Usage: launchsketch.sh <path of processing install> <name of sketch>
# Launch a sketch in a failsafe manner.
# Example:
# ./src/scripts/launchsketch.sh ~/processing-2.2.1 pixel_test

PROCESSING_DIR=$1  # Directory of the processing install (containing core/, java/, lib/, etc.)

SRC_DIR=$(cd $(dirname $0)/.. && pwd -P)  # src/ directory of lsdome repo

for D in $(ls -1 src/sketches); do

    SKETCH_NAME=$D
    
    echo =================
    echo building $SKETCH_NAME
    
    SKETCH_DIR=$(pwd)/src/sketches/$SKETCH_NAME
    OUTPUT_DIR=$(pwd)/bin/$SKETCH_NAME

    mkdir -p $OUTPUT_DIR
    
    $PROCESSING_DIR/processing-java --sketch=$SKETCH_DIR --export --output=$OUTPUT_DIR --force
    
done
