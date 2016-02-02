OPC opc;
float dx, dy;

void setup()
{
  size(125, 125);

  // Connect to the local instance of fcserver. You can change this line to connect to another computer's fcserver
  //opc = new OPC(this, "192.168.1.135", 7890);
  opc = new OPC(this, "127.0.0.1", 7890);
   int n = 15;
  
  float h_pitch = width / n;
  float v_pitch = h_pitch * sqrt(3) / 2;
  
  int j = 0;
  boolean flip = false;
  for (int i = n; i > 0; i--){
    opc.ledStrip(j, i, width/2, height/2 + (i - n/2 - .5) * v_pitch, h_pitch, 0, flip);
    j += i;
    flip = !flip;
  }
  // Make the status LED quiet
  opc.setStatusLed(false);
  
  colorMode(HSB, 100);
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

void draw() {
  long now = millis();
  float speed = 0.002;
  float angle = sin(now * 0.001);
  float z = now * 0.00008;
  float hue = now * 0.01;
  float scale = 0.005;

  dx += cos(angle) * speed;
  dy += sin(angle) * speed;

  loadPixels();
  for (int x=0; x < width; x++) {
    for (int y=0; y < height; y++) {
     
      float n = fractalNoise(dx + x*scale, dy + y*scale, z) - 0.75;
      float m = fractalNoise(dx + x*scale, dy + y*scale, z + 10.0) - 0.75;

      color c = color(
         (hue + 80.0 * m) % 100.0,
         100 - 100 * constrain(pow(3.0 * n, 3.5), 0, 0.9),
         100 * constrain(pow(3.0 * n, 1.5), 0, 0.9)
         );
      
      pixels[x + width*y] = c;
    }
  }
  updatePixels();
}

