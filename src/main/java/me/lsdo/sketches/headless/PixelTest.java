package me.lsdo.sketches.headless;

/*
 * A sketch to help debug the proper placement of panels.
 *
 * Each fadecandy 'arm' is a different color; each panel is slightly more saturated than the previous, and the
 * individual pixels progress in a marching ants pattern.
 */

import me.lsdo.processing.*;
import java.util.*;

public class PixelTest extends DomeAnimation {

    private HashMap<DomeCoord, Integer> coordOrder;
    private static final int ARM_LENGTH = 4;
    private int px_per_panel;
    private int total_arms;

    public PixelTest(Dome dome, OPC opc) {
        super(dome, opc);
        coordOrder = new HashMap<DomeCoord, Integer>();
        for (int i = 0; i < dome.coords.size(); i++) {
            coordOrder.put(dome.coords.get(i), i);
        }

        px_per_panel = LayoutUtil.pixelsPerPanel(15);
	total_arms = (int)Math.ceil((double)dome.getNumPoints() / (px_per_panel * ARM_LENGTH));
    }

    @Override
    protected int drawPixel(DomeCoord c, double t) {
        float creep_speed = 20;
        float ramp_length = 100;

        int i = coordOrder.get(c);
        int panel = i / px_per_panel;
        int arm = panel / ARM_LENGTH;
	panel = panel % ARM_LENGTH;
        int px = i - arm * ARM_LENGTH * px_per_panel; // pixel number within the current arm

        double k_px = (px - creep_speed * t) / ramp_length;
        double k_panel = (double)panel / (ARM_LENGTH - 1);

        double min_sat = .5;
        double max_sat = 1.;
        return OpcColor.getHsbColor(
		(double)arm / total_arms,
                (min_sat*(1-k_panel) + max_sat*k_panel),
                MathUtil.fmod(k_px, 1.));
    }

}
