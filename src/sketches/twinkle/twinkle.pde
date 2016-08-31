// Twinkling stars.

import me.lsdo.processing.*;

PixelGridSketch twinkle;

void setup() {
    size(300, 300);
    Dome dome = new Dome(6);
    OPC opc = new OPC();
    twinkle = new PixelGridSketch(this, new TwinkleSketch(dome, opc));
}

void draw() {
    twinkle.draw();
}

