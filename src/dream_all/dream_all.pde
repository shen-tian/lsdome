/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */

OPC opc;

// state variables for cloud
float dx, dy;
boolean hud;


boolean[] mask;

void setup()
{
  size(250, 250, P2D);
  setupOpc("127.0.0.1");
  setupMask(3);
  colorMode(HSB, 100);
}

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);

  // Code to lay out the pixels
  int n = 15;

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

void draw() {

  hud = false;
  if (keyPressed) {
    hud = (key == 'v' || key == 'V');
  }

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

  if (hud)
  {
    // Show the FPS
    int txtSize = 16;
    textSize(txtSize);
    fill(0, 100, 100);
    text((int)frameRate + "fps", 2, (txtSize + 2)); 
    text("dx " + dx, 2, (txtSize + 2) * 2);
    text("dy " + dy, 2, (txtSize + 2) * 3);
    text("angle " + angle, 2, (txtSize + 2) * 4);
    text("scale " + scale, 2, (txtSize + 2) * 5);
  }
}

