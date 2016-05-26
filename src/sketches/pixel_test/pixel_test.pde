/*
 * A sketch to help debug the proper placement of panels.
 *
 * Each fadecandy 'arm' is a different color; each panel is slightly more saturated than the previous, and the
 * individual pixels progress in a marching ants pattern.
 */
import me.lsdo.processing.*;

FadecandySketch driver = new PixelTest(this, 300);

void setup() {
  driver.init();
}

void draw() {
  driver.draw();
}
