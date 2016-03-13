import java.io.*;
import processing.core.*;

public class TubeSketch extends PointSampleSketch<PVector> {

    static final double DEFAULT_FOV = 120.;
    static final int DEFAULT_SUBSAMPLING = 4;

    double fov;
    BufferedReader input;

    double pos = 0;
    double last_t = 0;
    double speed = 1.;

    TubeSketch(PApplet app, double fov, int size_px, int subsampling, boolean temporal_jitter) {
        super(app, size_px, subsampling, temporal_jitter);
        this.fov = fov;
        input = app.createReader("/tmp/pipe");
    }

    TubeSketch(PApplet app, int size_px, double fov) {
        this(app, fov, size_px, DEFAULT_SUBSAMPLING, true);
    }

    TubeSketch(PApplet app, int size_px, int subsampling, boolean temporal_jitter) {
        this(app, DEFAULT_FOV, size_px, subsampling, temporal_jitter);
    }

    TubeSketch(PApplet app, int size_px) {
        this(app, size_px, DEFAULT_FOV);
    }

    double subsamplingBoost(PVector p) {
        return 1. / LayoutUtil.xyToPolar(p).x;
    }

    PVector toIntermediateRepresentation(PVector p) {
        PVector polar = LayoutUtil.xyToPolar(p);
        return LayoutUtil.V(polar.y, 1. / Math.tan(Math.toRadians(.5*fov)) / polar.x);
    }

    int samplePoint(PVector uv, double t) {
        double pos = this.pos + speed*(t - last_t);

        double u_pct = MathUtil.fmod(uv.x / (2*Math.PI), 1.);
        t = uv.y + pos;
        //boolean chk = ((int)MathUtil.fmod((uv.x + t)/(.25*Math.PI), 2.) + (int)MathUtil.fmod((uv.x - t)/(.25*Math.PI), 2.)) % 2 == 0;
        boolean chk = ((int)MathUtil.fmod((uv.x)/(.25*Math.PI), 2.) + (int)MathUtil.fmod((t)/(.25*Math.PI), 2.)) % 2 == 0;
        //boolean chk = (MathUtil.fmod((uv.x + t) / Math.PI, 2.) < 1.);
        return color(MathUtil.fmod(u_pct + t/10., 1.), .5, chk ? 1 : .1);
    }

    void beforeFrame(double t) {
        try {
            while (input.ready()) {
                String line = input.readLine();
                System.out.println(line);
                
                if (line.equals("jog_a inc")) {
                    speed *= 1.01;
                } else if (line.equals("jog_a dec")) {
                    speed /= 1.01;
                }
                System.out.println(""+speed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // TODO on init?

        double delta_t = t - last_t;
        double delta_p = speed * delta_t;
        pos += delta_p;
        last_t = t;
        
    }

}
