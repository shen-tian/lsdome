/*
 * Renderings of zooming down a straight tube, whose surface is an arbitrary UV-mapped texture.
 * This is a prototype for direct pixel-based rendering. The screen itself is not rendered-- the pixel coordinates
 * are evaluated directly (with antialiasing support) and written to the screen solely for visualization.
 */

OPC opc;

ArrayList<PVector> points;
ArrayList<ArrayList<PVector>> uv;

void setup()
{
  size(300, 300);
  //setupOpc("127.0.0.1");
  setupOpc("192.168.1.135");
  colorMode(HSB, 100);
}

int SUPERSAMPLE = 64;
float FOV = 120;

void setupOpc(String hostname)
{
  opc = new OPC(this, hostname, 7890);
  int PANEL_LENGTH = 15;
  points = LayoutUtil.fillLSDome(PANEL_LENGTH);
  LayoutUtil.registerScreenSamples(opc, points, width, height, 4., true);

  uv = new ArrayList<ArrayList<PVector>>();
  for (int i = 0; i < points.size(); i++) {
    ArrayList<PVector> sub = new ArrayList<PVector>();
    uv.add(sub);
    PVector p = points.get(i);

    for (int k = 0; k < SUPERSAMPLE; k++) {
      PVector offset = new PVector(random(1.) * .5 * LayoutUtil.pixelSpacing(PANEL_LENGTH), 0.);
      offset.rotate(random(1.) * 2*PI);
      PVector psub = PVector.add(p, offset);
      sub.add(new PVector((float)Math.atan2(psub.y, psub.x), 2. / (float)Math.tan(Math.toRadians(FOV / 2.)) / psub.mag()));
    }
  }
}

int getTexture(PVector uv, float dist) {
    float u_pct = LayoutUtil.fmod(uv.x / (2*PI), 1.);
    float t = uv.y + dist;
    //boolean chk = ((int)LayoutUtil.fmod((uv.x + t)/(.25*PI), 2.) + (int)LayoutUtil.fmod((uv.x - t)/(.25*PI), 2.)) % 2 == 0;
    boolean chk = ((int)LayoutUtil.fmod((uv.x)/(.25*PI), 2.) + (int)LayoutUtil.fmod((t)/(.25*PI), 2.)) % 2 == 0;
    //boolean chk = (LayoutUtil.fmod((uv.x + t) / PI, 2.) < 1.);
    return color(100*LayoutUtil.fmod(u_pct + t/10., 1.), 50, chk ? 100 : 10);
}

int getAntialiasedTexture(ArrayList<PVector> sub, float dist) {
  int[] samples = new int[SUPERSAMPLE];
  int k = samples.length;
  for (int i = 0; i < k; i++) {
    samples[i] = getTexture(sub.get(i), dist);
  }
  while (k > 1) {
    k = k / 2;
    for (int i = 0; i < k; i++) {
      samples[i] = lerpColor(samples[2*i], samples[2*i+1], .5);
    }
  }
  return samples[0];
}

void draw() {
  float t = millis() / 1000.;
  float speed = 10.;
  background(0);
  loadPixels();
  for (int i = 0; i < points.size(); i++) {
    pixels[opc.pixelLocations[i]] = getAntialiasedTexture(uv.get(i), t*speed);
  }
  updatePixels();
}

