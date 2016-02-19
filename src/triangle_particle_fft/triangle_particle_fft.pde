// Some real-time FFT! This visualizes music in the frequency domain using a
// polar-coordinate particle system. Particle size and radial distance are modulated
// using a filtered FFT. Color is sampled from an image.

import ddf.minim.analysis.*;
import ddf.minim.*;

OPC opc;
PImage dot;
PImage colors;
PImage colors1;
PImage colors2;
PImage colors3;
Minim minim;
AudioInput in;
FFT fft;
float[] fftFilter;

//String filename = "083_trippy-ringysnarebeat-3bars.mp3";
String filename = "/Users/Shen/kafkaf.mp3";
float spin = 0.001;
float radiansPerBucket = radians(2);
float decay = 0.97;
float opacity = 40;
float minSize = 0.1;
float sizeScale = 0.2;

void setup()
{
  size(250, 250, P2D);

  minim = new Minim(this); 

  // Small buffer size!
  in = minim.getLineIn();

  fft = new FFT(in.bufferSize(), in.sampleRate());
  fftFilter = new float[fft.specSize()];

  dot = loadImage("dot.png");
  colors1 = loadImage("colors.png");
  colors2 = loadImage("colors2.png");
  colors3 = loadImage("colors3.png");
  
  colors = colors1;

  // Connect to the local instance of fcserver
  opc = new OPC(this, "192.168.1.135", 7890);
  //opc = new OPC(this, "127.0.0.1", 7890);

  int n = 15;

  float spacing = width / (2 * n);

  float heightTotal = spacing * (n - 1) * sqrt(3) / 2.0;
  float distToCentroid = spacing * (n - 1) / (2.0 * sqrt(3));

  float theta = (float) Math.PI / 3;
  float centerX = width / 2;
  float centerY = height / 2 - heightTotal / 2 + distToCentroid;

  opc.ledTriangle(120, n, centerX, centerY, spacing, theta, false);

  theta = (float) (0 * Math.PI / 3);
  centerX = width / 2 - width / 4;
  centerY = height / 2 + heightTotal / 2 - distToCentroid;
  opc.ledTriangle(0, n, centerX, centerY, spacing, theta, false);

  centerX = width / 2 + width / 4;
  centerY = height / 2 + heightTotal / 2 - distToCentroid;
  theta += (float) 2 * Math.PI / 3;
  opc.ledTriangle(360, n, centerX, centerY, spacing, theta, false);
  // Make the status LED quiet
  opc.setStatusLed(false);
}

void draw()
{
  background(0);

  if (keyPressed) {
    if (key == '1')
      colors = colors1;
    if (key == '2')
      colors = colors2;
    if (key == '3')
      colors = colors3;
  }

  fft.forward(in.mix);
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

