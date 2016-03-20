import java.util.*;
import processing.core.*;

public class PixelGridSketch<S> extends FadecandySketch<S> {

    HashMap<DomeCoord, Integer> pixelColors;

    PixelGridSketch(PApplet app, int size_px) {
        super(app, size_px);
    }

    void init() {
        super.init();

        pixelColors = new HashMap<DomeCoord, Integer>();
        for (DomeCoord c : coords) {
            pixelColors.put(c, 0x0);
        }
    }

    void draw(double t) {
        app.background(0);
        app.loadPixels();
        for (int i = 0; i < coords.size(); i++) {
            setLED(i, drawPixel(coords.get(i), t));
        }
        app.updatePixels();
    }

    // You can set the pixel colors in pixelColors in beforeFrame() and they will be automatically
    // rendered here. Or, you can override drawPixel() directly.
    int drawPixel(DomeCoord c, double t) {
        return pixelColors.get(c);
    }

}
