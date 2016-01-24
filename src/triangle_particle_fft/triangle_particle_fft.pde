// Some real-time FFT! This visualizes music in the frequency domain using a
// polar-coordinate particle system. Particle size and radial distance are modulated
// using a filtered FFT. Color is sampled from an image.

import ddf.minim.analysis.*;
import ddf.minim.*;

OPC opc;
PImage dot;
PImage colors;
TriangleGrid triangle;
Minim minim;
AudioPlayer sound;
FFT fft;
float[] fftFilter;

String filename = "083_trippy-ringysnarebeat-3bars.mp3";
float spin = 0.001;
float radiansPerBucket = radians(2);
float decay = 0.97;
float opacity = 40;
float minSize = 0.1;
float sizeScale = 0.25;

void setup()
{
  size(250, 250, P3D);

  minim = new Minim(this); 

  // Small buffer size!
  sound = minim.loadFile(filename, 512);
  sound.loop();
  fft = new FFT(sound.bufferSize(), sound.sampleRate());
  fftFilter = new float[fft.specSize()];

  dot = loadImage("dot.png");
  colors = loadImage("colors.png");

  // Connect to the local instance of fcserver
  //opc = new OPC(this, "192.168.1.135", 7890);
  opc = new OPC(this, "127.0.0.1", 7890);
  
int n = 15;
  
    int index = 0;
  index += opc.ledTriangle(index, n, width/4, height/2, width/2, 0, false);
  float theta = (float)(Math.PI/3);
  index += opc.ledTriangle(index, n,
  (float)(width/2 + width/4*(Math.sin(theta)-Math.cos(theta))),
  (float)(.75f*height - width/4*(Math.sin(theta)+Math.cos(theta))), width/2, theta, false);
  
  // Make the status LED quiet
  opc.setStatusLed(false);
}

void draw()
{
  background(0);

  fft.forward(sound.mix);
  for (int i = 0; i < fftFilter.length; i++) {
    fftFilter[i] = max(fftFilter[i] * decay, log(1 + fft.getBand(i)));
  }
  
  for (int i = 0; i < fftFilter.length; i += 3) {   
    color rgb = colors.get(int(map(i, 0, fftFilter.length-1, 0, colors.width-1)), colors.height/2);
    tint(rgb, fftFilter[i] * opacity);
    blendMode(ADD);
 
    float size = height * (minSize + sizeScale * fftFilter[i]);
    PVector center = new PVector(width * (fftFilter[i] * 0.2), 0);
    center.rotate(millis() * spin + i * radiansPerBucket);
    center.add(new PVector(width * 0.5, height * 0.5));
 
    image(dot, center.x - size/2, center.y - size/2, size, size);
  }
}

