/*
 * Renderings of zooming down a straight tube, whose surface is an arbitrary UV-mapped texture.
 * This is a prototype for direct pixel-based rendering. The screen itself is not rendered-- the pixel coordinates
 * are evaluated directly (with antialiasing support) and written to the screen solely for visualization.
 */
import me.lsdo.processing.*;

PixelGridSketch sketch;

void setup() {
    size(300, 300);
    Dome dome = new Dome(6);
    OPC opc = new OPC();
    DomeAnimation animation = new TubeSketch(dome, opc);
    sketch = new PixelGridSketch(this, animation);
}

void draw() {
    sketch.draw();
}


