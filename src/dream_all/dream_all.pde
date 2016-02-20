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
  size(300, 300, P2D);
  dot = loadImage("dot.png");
  mode = 0;
  //setupOpc("127.0.0.1");
  setupOpc("192.168.1.135");
  setupMask(1);
  colorMode(HSB, 100);
}

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);
  int PANEL_LENGTH = 15;
  LayoutUtil.registerScreenSamples(opc, LayoutUtil.fillLSDome(PANEL_LENGTH), width, height, 4., true);
}

void setupMask(float radius)
{
  mask = new boolean[width * height];
  int window = (int)Math.ceil(radius) - 1;

  for (int i = 0; i < opc.pixelLocations.length; i++) {
    int thisX = opc.pixelLocations[i] % width;
    int thisY = (opc.pixelLocations[i] - thisX) / width;
    for (int dx = -window; dx <= window; dx++) {
      for (int dy = -window; dy <= window; dy++) {
        int x = thisX + dx;
        int y = thisY + dy;
        if (x >= 0 && x < width && y >= 0 && y < height) {
          mask[x + width * y] = true;
        }
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

