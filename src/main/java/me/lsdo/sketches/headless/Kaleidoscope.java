package me.lsdo.sketches.headless;

import me.lsdo.processing.*;

public class Kaleidoscope extends DomeAnimation {

    private TriCoord basePanel;

    public Kaleidoscope(Dome dome, OPC opc) {
        super(dome, opc);
        basePanel = new TriCoord(TriCoord.CoordType.PANEL, 0, 0, -1);
    }

    // colors for the base panel. What algorithm is here?
    int getBasePixel(DomeCoord c, double t) {
        PVector2 p = dome.getLocation(c);
        p = LayoutUtil.Vrot(p, t * (.5 + 3*.5*(Math.cos(.1213*t)+1)));
        p = LayoutUtil.Vmult(p, 1/(1 + 5*.5*(Math.cos(.3025*t)+1)));
        p = LayoutUtil.Vadd(p, LayoutUtil.V(2*Math.cos(.2*t), 0));

        return OpcColor.getHsbColor(
                MathUtil.fmod(p.x + .4081*t, 1.),
                .6,
                .5 * (Math.cos(40*p.x)+1));
    }

    // Compute the base panel upfront for efficiency. Since its content is copied to every
    // other panel, the base panel pixels can be computed only once per frame and re-used.
    @Override
    public void preFrame(double t, double deltaT) {
	for (DomeCoord c : dome.coords) {
            if (c.panel.equals(basePanel)) {
                dome.setColor(c, getBasePixel(c, t));
            }
        }
    }
    
    // This is the kaleidoscope effect. Depending on which panel, flip/rotate.
    // and copy the base panel's colors.
    @Override
    public int drawPixel(DomeCoord c, double t) {
        int pos = MathUtil.mod(c.panel.u - c.panel.v, 3);
        int rot = MathUtil.mod(c.panel.getOrientation() == TriCoord.PanelOrientation.A ? 2*pos : 1-2*pos, 6);
        boolean flip = (MathUtil.mod(rot, 2) == 1);
        TriCoord basePx = c.pixel.rotate(rot);
        if (flip) {
            basePx = basePx.flip(TriCoord.Axis.U);
        }

        return dome.getColor(new DomeCoord(basePanel, basePx));
    }
}

