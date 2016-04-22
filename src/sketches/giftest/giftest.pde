// Little program for displaying one animated GIF
// based on the gifAnimation library by Extrapixel 
// http://extrapixel.github.io/gif-animation/ 


import gifAnimation.*;    // import the gifAnimation library

Gif myAnimation;          // create Gif object called myAnimation
FadecandySketch driver = new FadecandySketch(this, FadecandySketch.widthForPixelDensity(1.));

void setup() {

  colorMode(HSB, 100);
  driver.init();

  myAnimation = new Gif(this, "130309.gif");

  myAnimation.play();                       // play the animated GIF
}


void draw() {
  imageMode(CENTER);
  image(myAnimation, 125,125);
}

