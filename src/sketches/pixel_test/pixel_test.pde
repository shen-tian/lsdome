/*
 * A sketch to help debug the proper placement of panels.
 *
 * Each fadecandy 'arm' is a different color; each panel is slightly more saturated than the previous, and the
 * individual pixels progress in a marching ants pattern.
 */

import me.lsdo.processing.*;

PixelGridSketch sketch;

void setup() {
    size(450, 450);
    Dome dome = new Dome(6);
    OPC opc = new OPC();
    DomeAnimation animation = new PixelTestAnimation(dome, opc);
    sketch = new PixelGridSketch(this, animation);
}

void draw() {
    sketch.draw();
}
