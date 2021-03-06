# Limitless Slip Dome

[![Build Status](https://travis-ci.org/shen-tian/lsdome.svg?branch=master)](https://travis-ci.org/shen-tian/lsdome)

Project for Afrika Burn 2016. A dome style gazebo with around 1500 - 2800
WS2812B LEDs, controlled via [Fadecandies](http://www.misc.name/fadecandy/).
See the [project](http://lsdo.me) page.

This is a library of animations. It uses the associated `lsdome-processing`
[lirary](https://github.com/shen-tian/lsdome-processing).

### From project submission:

The piece will be a net with 1500-2000 LEDs, about 3-4m in diameter suspended
on a frame 2m off of the ground. Since the LEDs are fully addressable
(NeoPixels!), they can be used to run various visualisations at night. It works
really well with sound too, so I'd like to add some speakers (not a dance party
level. More like ambient++). Its footprint should be around 5x5m.

### Build

Work in progress: the project is split between two parts right now: 

* Headless animations that can run without an X environment
* Processing sketches that need a graphics environment to run.

#### Headless java app

The headless code is organised as a Java app. It's built by `gradle`.
Before running `gradle`, set `local_processing_install` in `build.gradle`
to the path of your processing installation.
`gradle installDist` builds and places it in the `\build\install\lsdome` folder.
Running the `./bin/lsdome` script launches it. It accepts these parameters:

    lsdome [cloud|rings|kaleidoscope|twinkle|pixeltest]
    
It's pretty self contained. Since these are headless, you do need an 
OPC server that's connected to see what's going on though. Use a `config.properties`
file to configure where the OPC serer is. Sample file:

    opchostname=192.168.1.123
    opcport=7890

### Dependencies

You must install Processing 2.2.1

On ubuntu:

    add-apt-repository ppa:mc3man/gstffmpeg-keep && apt-get update
    apt-get install gstreamer0.10-plugins-base gstreamer0.10-plugins-good gstreamer0.10-ffmpeg gstreamer0.10-pulseaudio gstreamer0.10-alsa

### Simulator

Grab this: 

    git clone https://github.com/zestyping/openpixelcontrol.git
    
And maybe some dependcies. on ubuntu:

    sudo apt-get install mesa-common-dev freeglut3-dev

Build the thing:
   
    make

Then run:

    bin/gl_server -l $LSDOME_REPO_PATH/src/config/simulator_layouts/opcsimulator_layout_24.json -p 7890
    
Use the layout file for the corresponding number of panels.
