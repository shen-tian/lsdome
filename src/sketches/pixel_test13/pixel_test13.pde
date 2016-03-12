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

  int[] arms = new int[] {4, 4, 4, 1};

  background(0);
  loadPixels();
  for (int i = 0; i < points.size(); i++) {
    int panel = i / 120;
    int xx = 0;
    int arm0 = 0;
    int arm;
    for (arm = 0; arm < arms.length; arm++) {
      xx += arms[arm];
      if (panel < xx) {
        break;
      }
      arm0 = xx;
    }
    panel -= arm0;
    int px = (i - arm0*120);
    float k = (px - creep_speed * t) / ramp_length;
    setLED(i, color(100 * arm / 4., 50 + 15*panel, 100 * LayoutUtil.fmod(k, 1.)));
  }
  updatePixels();
}

