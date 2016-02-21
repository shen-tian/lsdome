/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */

OPC opc;
PImage dot;

boolean[] mask;
ArrayList<PVector> points;
ArrayList<ArrayList<PVector>> uv;

void setup()
{
  size(300, 300);
  dot = loadImage("dot.png");
  //setupOpc("127.0.0.1");
  setupOpc("192.168.1.135");
  setupMask(1);
  colorMode(HSB, 100);
}

int SUPERSAMPLE = 64;

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
      sub.add(new PVector((float)Math.atan2(psub.y, psub.x), 4. / psub.mag()));
    }
  }
}

void setupMask(float radius)
{
  mask = new boolean[width * height];
  int window = (int)Math.ceil(radius) - 1;

  for (int i = 0; i < opc.pixelLocations.length; i++) {
    if (opc.pixelLocations[i] == -1) {
      continue;
    }

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

boolean render(int x, int y) {
  return mask[x + y * width];
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

