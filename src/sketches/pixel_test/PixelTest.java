import me.lsdo.processing.*;
import java.util.*;
import processing.core.*;

public class PixelTest extends PixelGridSketch<Object> {

    HashMap<DomeCoord, Integer> coordOrder;
    int[] arms;
    int px_per_panel;

    public PixelTest(PApplet app, int size_px) {
        super(app, size_px);
    }

    public void init() {
        super.init();

        coordOrder = new HashMap<DomeCoord, Integer>();
        for (int i = 0; i < coords.size(); i++) {
            coordOrder.put(coords.get(i), i);
        }

        arms = LayoutUtil.getPanelConfig(panel_config_mode).arms;
        px_per_panel = LayoutUtil.pixelsPerPanel(panel_size);
    }

    int drawPixel(DomeCoord c, double t) {
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
        return color(arm / (double)arms.length, min_sat*(1-k_panel) + max_sat*k_panel, MathUtil.fmod(k_px, 1.));
    }

}
