import java.util.*;
import processing.core.*;

public class KaleidoscopeSketch extends PixelGridSketch<Object> {

    TriCoord basePanel;

    public KaleidoscopeSketch(PApplet app, int size_px) {
        super(app, size_px);

        basePanel = new TriCoord(CoordType.PANEL, 0, 0, -1);
    }

    void beforeFrame(double t) {
        for (DomeCoord c : coords) {
            if (c.panel.equals(basePanel)) {
                pixelColors.put(c, getBasePixel(c, t));
            }
        }
    }
 
    int getBasePixel(DomeCoord c, double t) {
        PVector p = points.get(c);
        p = LayoutUtil.Vrot(p, t * (.5 + 3*.5*(Math.cos(.1213*t)+1)));
        p = LayoutUtil.Vmult(p, 1/(1 + 5*.5*(Math.cos(.3025*t)+1)));
        p = LayoutUtil.Vadd(p, LayoutUtil.V(2*Math.cos(.2*t), 0));
        return color(MathUtil.fmod(p.x + .4081*t, 1.), .6, .5*(Math.cos(40*p.x)+1));
    }
    
    int drawPixel(DomeCoord c, double t) {
        int pos = MathUtil.mod(c.panel.u - c.panel.v, 3);
        int rot = MathUtil.mod(c.panel.getOrientation() == PanelOrientation.A ? 2*pos : 1-2*pos, 6);
        boolean flip = (MathUtil.mod(rot, 2) == 1);
        TriCoord basePx = c.pixel.rotate(rot);
        if (flip) {
            basePx = basePx.flip(Axis.U);
        }
        return pixelColors.get(new DomeCoord(basePanel, basePx));
    }

}
