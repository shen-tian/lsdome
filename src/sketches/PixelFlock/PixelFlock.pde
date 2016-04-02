FadecandySketch driver = new FadecandySketch(this, 250, 250);
Flock flock;

void setup() {
 colorMode(HSB, 100);
 driver.init();
 
  flock = new Flock();
  // Add an initial set of boids into the system
  for (int i = 0; i < 150; i++) {
    flock.addBoid(new Boid(width/2,height/2));
  }
}

void draw() {
   background(0);
  flock.run();
}
