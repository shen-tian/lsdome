// Little program for displaying one animated GIF
// based on the gifAnimation library by Extrapixel 
// http://extrapixel.github.io/gif-animation/ 

// unzip into local processing libs:
// http://www.extrapixel.ch/processing/gifAnimation/gifAnimation.zip
// (don't think it's gradle-installable)

import gifAnimation.*;    // import the gifAnimation library
import me.lsdo.processing.*;

Gif myAnimation;          // create Gif object called myAnimation
CanvasSketch simple;

void setup() {

    size(300, 300);
    simple = new CanvasSketch(this, new Dome(), new OPC());
    colorMode(HSB, 100);
  myAnimation = new Gif(this, "130309.gif");

  myAnimation.play();                       // play the animated GIF
}


void draw() {
  imageMode(CENTER);
  image(myAnimation, 125,125);
  
  simple.draw();
}

