/*
 * Demo the properties of the triangular pixel grid.
 */
import me.lsdo.processing.*;

PixelGridSketch sketch;

void setup() {
    size(450, 450);
    Dome dome = new Dome(6);
    OPC opc = new OPC();
    DomeAnimation animation = new GridTest(dome, opc);
    sketch = new PixelGridSketch(this, animation);
}

void draw() {
    sketch.draw();
}

