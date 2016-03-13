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
  points = LayoutUtil.fillLSDome(PANEL_LENGTH);
  LayoutUtil.registerScreenSamples(opc, points, width, height, 4., true);
}

void draw() { 
  float creep_speed = 20;
  float ramp_length = 100;

  float t = millis() / 1000.;

  background(0);
  loadPixels();
  for (int i = 0; i < points.size(); i++) {
    int arm = i / (120 * 4);
    int px = i % (120 * 4);
    int panel = px / 120;
    float k = (px - creep_speed * t) / ramp_length;
    pixels[opc.pixelLocations[i]] = color(100 * arm / 6., 50 + 15*panel, 100 * LayoutUtil.fmod(k, 1.));
  }
  updatePixels();
}

