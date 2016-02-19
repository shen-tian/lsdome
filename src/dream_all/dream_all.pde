/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */

OPC opc;
PImage dot;

int mode;

// state variables for cloud
float dx, dy, dz;
boolean hud;



boolean[] mask;

void setup()
{
  size(250, 250, P2D);
  dot = loadImage("dot.png");
  mode = 0;
  //setupOpcMulti("127.0.0.1");
  setupOpcMulti("192.168.1.135");
  setupMask(4);
  colorMode(HSB, 100);
}

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);

  // Code to lay out the pixels
  int n = 25;

  float h_pitch = width / n;
  float v_pitch = h_pitch * sqrt(3) / 2;

  int j = 0;
  boolean flip = false;
  for (int i = n; i > 0; i--) {
    opc.ledStrip(j, i, width/2, height/2 + (i - n/2 - .5) * v_pitch, h_pitch, 0, flip);
    j += i;
    flip = !flip;
  }
  // Make the status LED quiet
  opc.setStatusLed(false);
}

void setupOpcMulti(String hostname)
{
  opc = new OPC(this, hostname, 7890);

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
  //opc.ledTriangle(240, n, centerX, centerY, spacing, theta, false);
}

void setupMask(float radius)
{
  mask = new boolean[width * height];

  for (int x=0; x < width; x++) {
    for (int y=0; y < height; y++) {
      mask[x + width * y] = false;
      for (int i = 0; i < opc.pixelLocations.length; i++)
      {
        int thisX = opc.pixelLocations[i] % width;
        int thisY = (opc.pixelLocations[i] - thisX) / width;
        if (sq(thisX - x) + sq(thisY - y) <= sq(radius))
          mask[x + width * y] = true;
      }
    }
  }
}

float noiseScale=0.02;

float fractalNoise(float x, float y, float z) {
  float r = 0;
  float amp = 1.0;
  for (int octave = 0; octave < 4; octave++) {
    r += noise(x, y, z) * amp;
    amp /= 2;
    x *= 2;
    y *= 2;
    z *= 2;
  }
  return r;
}

boolean render(int x, int y) {
  return mask[x + y * width];
}

void drawCloud()
{
  long now = millis();
  float speed = 0.002;
  float angle = sin(now * 0.001);
  //float angle = now * 0.001;
  float z = now * 0.00008;
  float hue = now * 0.01;
  // Feels like this should be linked to canvas size. The smaller the canvas, the larger it
  // should be.
  float scale = 0.005;

  dx += cos(angle) * speed;
  dy += sin(angle) * speed;

  loadPixels();

  for (int x=0; x < width; x++) {
    for (int y=0; y < height; y++) {
      if (render(x, y)) {
        float n = fractalNoise(dx + x*scale, dy + y*scale, z) - 0.75;
        float m = fractalNoise(dx + x*scale, dy + y*scale, z + 10.0) - 0.75;

        color c = color(
        (hue + 80.0 * m) % 100.0, 
        100 - 100 * constrain(pow(3.0 * n, 3.5), 0, 0.9), 
        100 * constrain(pow(3.0 * n, 1.5), 0, 0.9)
          );

        pixels[x + width*y] = c;
      } else
        pixels[x + width * y] = color(0, 0, 0);
    }
  }
  updatePixels();
}

void drawRings() {
  long now = millis();
  float speed = 0.002;
  float zspeed = 0.1;
  float angle = sin(now * 0.001);
  float z = now * 0.00008;
  float hue = now * 0.01;
  float scale = 0.005;

  float saturation = 100 * constrain(pow(1.15 * noise(now * 0.000122), 2.5), 0, 1);
  float spacing = noise(now * 0.000124) * 0.1;

  dx += cos(angle) * speed;
  dy += sin(angle) * speed;
  dz += (noise(now * 0.000014) - 0.5) * zspeed;

  float centerx = noise(now *  0.000125) * 1.25 * width;
  float centery = noise(now * -0.000125) * 1.25 * height;

  loadPixels();
  for (int x=0; x < width; x++) {
    for (int y=0; y < height; y++) {
      if (render(x, y)) {

        float dist = sqrt(pow(x - centerx, 2) + pow(y - centery, 2));
        float pulse = (sin(dz + dist * spacing) - 0.3) * 0.3;

        float n = fractalNoise(dx + x*scale + pulse, dy + y*scale, z) - 0.75;
        float m = fractalNoise(dx + x*scale, dy + y*scale, z + 10.0) - 0.75;

        color c = color(
        (hue + 40.0 * m) % 100.0, 
        saturation, 
        100 * constrain(pow(3.0 * n, 1.5), 0, 0.9)
          );

        pixels[x + width*y] = c;
      } else
        pixels[x + width * y] = color(0, 0, 0);
    }
  }
  updatePixels();
}

void drawDot() {
  background(0);

  // Change the dot size as a function of time, to make it "throb"
  float dotSize = height * 0.6 * (1.0 + 0.2 * sin(millis() * 0.01));

  // Draw it centered at the mouse location
  image(dot, mouseX - dotSize/2, mouseY - dotSize/2, dotSize, dotSize);
}


void draw() {

  hud = false;
  boolean ring = false;
  if (keyPressed) {
    hud = (key == 'v' || key == 'V');
    if (key == 'c')
      mode = 0;
    if (key == 'r')
      mode = 1;
    if (key == 'd')
      mode = 2;
  }

  //drawCloud();
  if (mode == 1)
    drawRings();
  if (mode == 0)
    drawCloud();
  if (mode ==2)
    drawDot();

  if (hud)
  {
    // Show the FPS
    int txtSize = 16;
    textSize(txtSize);
    fill(0, 100, 100);
    text((int)frameRate + "fps", 2, (txtSize + 2)); 
    text("dx " + dx, 2, (txtSize + 2) * 2);
    text("dy " + dy, 2, (txtSize + 2) * 3);
  }
}

