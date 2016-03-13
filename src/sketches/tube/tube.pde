/*
 * Renderings of zooming down a straight tube, whose surface is an arbitrary UV-mapped texture.
 * This is a prototype for direct pixel-based rendering. The screen itself is not rendered-- the pixel coordinates
 * are evaluated directly (with antialiasing support) and written to the screen solely for visualization.
 */

TubeSketch driver = new TubeSketch(this);

void setup() {
  driver.init(300, 300, 4, true);
}

void draw() {
  driver.draw();
}

