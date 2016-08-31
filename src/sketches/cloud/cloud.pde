/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */
import me.lsdo.processing.*;

PixelGridSketch sketch;

void setup() {
    size(300, 300);
    Dome dome = new Dome(6);
    OPC opc = new OPC();
    DomeAnimation animation = new CloudsSketch(dome, opc);
    sketch = new PixelGridSketch(this, animation);
}

void draw() {
    sketch.draw();
}

