import processing.net.*; 
Client myClient;


OPC opc;
PImage dot;


float xoff = 0.0;
float xincrement = 0.02; 

void setup() {
  size(250,200);
  background(0);
  noStroke();
  
  myClient = new Client(this, "127.0.0.1", 5204);
  
  // Load a sample image
  dot = loadImage("color-dot.png");

  // Connect to the local instance of fcserver
  opc = new OPC(this, "192.168.1.125", 7890);

  // Map one 64-LED strip to the center of the window
  opc.ledGrid(0,25,2, width/2, height/2, width / 30., width / 30. ,0, true);
 
 level = 255;
 last = 0;
}

int level;
int last;

void draw() {
  // Create an alpha blended background
  
  
  background(0);//fill(0, 10);
  //rect(0,0,width,height);
  
  //float n = random(0,width);  // Try this line instead of noise
  
  // Get a noise value based on xoff and scale it according to the window's width
  float n = (PerlinNoise_1D(xoff)/2 + .5);
  // With each cycle, increment xoff
  xoff += xincrement;
  
  // Draw the ellipse at the value produced by perlin noise
  fill(200);
  //ellipse(n,height/2,100,100);
  
  tint(255, level);
  image(dot, width/2 - 225 + n * 50, height/2 - 20, 400, 40);
  
  myClient.write("UPS State");
  String state = myClient.readString();
  if (state != null){
      try {
          JSONObject json = parseJSONObject(state);
          
          
          float maxPwr = 10;
          if (json.getString("ups.status").indexOf("OB") != -1)
              maxPwr = 4.;
          else
              maxPwr = 10;
          int nowTime = millis();
          if (nowTime - last > 1000)
          {
               last = nowTime;
              float pwr = json.getFloat("output.power");
          
              if (pwr < maxPwr - .1)
                  level = min (level + 5, 255);
               
              if (pwr > maxPwr + .1)
                  level = max (level - 5, 0);
          }
          
          text(String.format("Battery charge: %s", json.getString("batt.charge")), 10, 10);
          text(String.format("UPS Status    : %s", json.getString("ups.status")), 10, 30);
          text(String.format("Power draw: %.2fw", json.getFloat("output.power")), 10, 50);
          text(String.format("Power level:    %d", level), 10, 70);
      }
      catch (Exception e)
      {
      }
  }
  
  delay(30);
}

float Noise(int xx, int i){
    return sin(xx * (1 + i/10.));
    //int x = (int)pow((xx << 1), xx);
    //println(xx + " " + x);
    //return ( 1.0 - ( (x * (x * x * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);    
}


float SmoothedNoise(int x ,int i){
    return Noise(x, i);
    //return Noise(x, i)/2  +  Noise(x-1, i)/4  +  Noise(x+1, i)/4;
}


float InterpolatedNoise(float x, int i){
      //println(x);
      int integer_X    = int(x);
      float fractional_X = x - integer_X;

      float v1 = SmoothedNoise(integer_X, i);
      float v2 = SmoothedNoise(integer_X + 1, i);

      return Interpolate(v1 , v2 , fractional_X);

}

float Interpolate(float a, float b, float x)
{
    float ft = x * 3.1415927;
    float f = (1 - cos(ft)) * .5;

    return  a*(1-f) + b*f;
}

float PerlinNoise_1D(float x)
{
      float total = 0;
      for (int i = 0; i < 3; i++)
      {
          int frequency = (int) pow(2,i);
          float amplitude = pow(.25, i);
          total += InterpolatedNoise(x * frequency, i) * amplitude;
      }
      //println(x + " " + total);
      return total;
}
