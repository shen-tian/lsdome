/*
 * A sketch to help debug the proper placement of panels.
 *
 * Each fadecandy 'arm' is a different color; each panel is slightly more saturated than the previous, and the
 * individual pixels progress in a marching ants pattern.
 */

FadecandySketch driver = new PixelTest(this);

void setup() {
  driver.init(300, 300);
}

void draw() {
  driver.draw();
}
