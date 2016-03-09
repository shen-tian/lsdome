/*
 * A sketch to help debug the proper placement of panels.
 *
 * Each fadecandy 'arm' is a different color; each panel is slightly more saturated than the previous, and the
 * individual pixels progress in a marching ants pattern.
 */

OPC opc;

ArrayList<PVector> points;

void setup()
{
  size(300, 300);
  //setupOpc("127.0.0.1");
  setupOpc("192.168.1.135");
  colorMode(HSB, 100);
}

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);
  int PANEL_LENGTH = 15;
  points = LayoutUtil.fillLSDome13(PANEL_LENGTH);
  float radius = (float)Math.sqrt(7/3.);
  LayoutUtil.registerScreenSamples(opc, points, width, height, 2*radius, true);
}

void setLED(int i, int rgb) {
  int px = opc.pixelLocations[i];
  if (px >= 0) {
    pixels[px] = rgb;
  }
}

void draw() { 
  float creep_speed = 20;
  float ramp_length = 100;

  float t = millis() / 1000.;

  background(0);
  loadPixels();
  for (int i = 0; i < points.size(); i++) {
    int panel = i / 120;
    int arm, px;
    if (panel == 0) {
      arm = 0;
      px = i;
    } else {
      // TODO make this logic more robust
      arm = (panel - 1) / 4 + 1;
      px = (i - 120) % (120 * 4);
      panel = (panel - 1) % 4;
    }
    float k = (px - creep_speed * t) / ramp_length;
    setLED(i, color(100 * arm / 4., 50 + 15*panel, 100 * LayoutUtil.fmod(k, 1.)));
  }
  updatePixels();
}

