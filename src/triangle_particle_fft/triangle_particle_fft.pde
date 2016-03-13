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

ArrayList<PVector> points;

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

  setupOpc("192.168.1.135");
}

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);
  int PANEL_LENGTH = 15;
  points = LayoutUtil.2(0,2,PANEL_LENGTH);
  ArrayList<PVector> newPoints = new ArrayList<PVector>();
  for (PVector p : points)
  {
    p.add(-.75,-.5,0);
    p.mult(2.5);
    newPoints.add(p);
  }
    
  LayoutUtil.registerScreenSamples(opc, newPoints, width, height, 4., true);
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

