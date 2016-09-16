import me.lsdo.processing.*;
import java.util.*;
import processing.core.*;

public class PixelTestAnimation extends DomeAnimation {

    HashMap<DomeCoord, Integer> coordOrder;
    int[] arms;
    int px_per_panel;

    public PixelTestAnimation(Dome dome, OPC opc) {
        super(dome, opc);
        coordOrder = new HashMap<DomeCoord, Integer>();
        for (int i = 0; i < dome.coords.size(); i++) {
            coordOrder.put(dome.coords.get(i), i);
        }

        arms = new int[] {4, 4, 4, 1};
        px_per_panel = LayoutUtil.pixelsPerPanel(15);
    }

    protected int drawPixel(DomeCoord c, double t) {
        float creep_speed = 20;
        float ramp_length = 100;

        int i = coordOrder.get(c);
        int panel = i / px_per_panel;
        int arm;
        int panel0 = 0; // panel that starts the current arm
        for (arm = 0; arm < arms.length; arm++) {
            if (panel < arms[arm]) {
                break;
            }
            panel -= arms[arm];
            panel0 += arms[arm];
        }
        int px = i - panel0 * px_per_panel; // pixel number within the current arm

        double k_px = (px - creep_speed * t) / ramp_length;
        double k_panel = panel / (double)Math.max(arms[arm] - 1, 1);

        double min_sat = .5;
        double max_sat = 1.;
        return getHsbColor(
        (int)(255 * arm / (double)arms.length), 
        (int)(255 * (min_sat*(1-k_panel) + max_sat*k_panel)), 
        (int)(255* MathUtil.fmod(k_px, 1.)));
    }

}
