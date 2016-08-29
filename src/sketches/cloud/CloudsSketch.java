/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */

import processing.core.*;
import me.lsdo.processing.*;

public class CloudsSketch extends XYAnimation {

    int mode;
    
    double dx;
    double dy;
    double dz;
    
    double last_t;

    double noiseScale=0.02;
    
    PApplet app;

    public CloudsSketch(Dome dome, OPC opc) {
        super(dome, opc);
        mode = 0;
        
        dx = dy = dz = 0;
        
        app = new PApplet();
    }

    protected void preFrame(double t) {
        double delta_t = t - last_t;
        last_t = t;
        
        double speed = 0.06;
        double zspeed = 3.;
        double angle = Math.sin(t);

        dx += Math.cos(angle) * speed * delta_t;
        dy += Math.sin(angle) * speed * delta_t;
        dz += (noise(t * 0.014) - 0.5) * zspeed * delta_t;
    }

    protected int samplePoint(PVector p, double t, double t_jitter) {
        // Noise patterns are symmetrical around the origin and it looks weird. Move origin to corner.
        p = LayoutUtil.Vadd(p, LayoutUtil.V(1, 1));

        double z = .08 * t;
        double hue = .1 * t;
        double scale = .75;
        
        double n = fractalNoise(dx + p.x*scale, dy + p.y*scale, z) - 0.75;
        double m = fractalNoise(dx + p.x*scale, dy + p.y*scale, z + 10.0) - 0.75;
                    
        return getHsbColor(
                     (int)(255 * MathUtil.fmod(hue + .8 * m, 1.)), 
                     (int)(255 * (1. - constrain(Math.pow(3.0 * n, 3.5), 0, 0.9))), 
                     (int)(255 * constrain(Math.pow(3.0 * n, 1.5), 0, 0.9))
                     );
    }

    private double fractalNoise(double x, double y, double z) {
        double r = 0;
        double amp = 1.0;
        for (int octave = 0; octave < 4; octave++) {
            r += app.noise((float)x, (float)y, (float)z) * amp;
            amp /= 2;
            x *= 2;
            y *= 2;
            z *= 2;
        }
        return r;
    }

    private double noise(double x) {
        return app.noise((float)x);
    }

    private double constrain(double x, double min, double max) {
        return app.constrain((float)x, (float)min, (float)max);
    }

}


