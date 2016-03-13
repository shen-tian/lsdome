/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */

CloudsSketch driver = new CloudsSketch(this);

void setup() {
  driver.init(300, 300);
}

void draw() {
  driver.draw();
}


