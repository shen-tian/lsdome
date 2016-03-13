import java.io.*;
import processing.core.*;

public class TubeSketch extends PointSampleSketch<PVector, Double> {

    static final double DEFAULT_FOV = 120.;
    static final int DEFAULT_SUBSAMPLING = 4;

    double fov;
    BufferedReader input;

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
        // This uses a planar projection, although the dome itself will be slightly curved.
        PVector polar = LayoutUtil.xyToPolar(p);
        return LayoutUtil.V(polar.y, 1. / Math.tan(Math.toRadians(.5*fov)) / polar.x);
    }

    Double initialState() {
        return 0.;
    }

    Double updateState(Double pos, double delta_t) {
        return pos + speed * delta_t;
    }

    int samplePoint(PVector uv, double t, double t_jitter) {
        double pos0 = state;
        double pos = pos0 + speed * t_jitter;

        double u_pct = MathUtil.fmod(uv.x / (2*Math.PI), 1.);
        double dist = uv.y + pos;
        //boolean chk = ((int)MathUtil.fmod((uv.x + dist)/(.25*Math.PI), 2.) + (int)MathUtil.fmod((uv.x - dist)/(.25*Math.PI), 2.)) % 2 == 0;
        boolean chk = ((int)MathUtil.fmod((uv.x)/(.25*Math.PI), 2.) + (int)MathUtil.fmod((dist)/(.25*Math.PI), 2.)) % 2 == 0;
        //boolean chk = (MathUtil.fmod((uv.x + dist) / Math.PI, 2.) < 1.);
        return color(MathUtil.fmod(u_pct + dist/10., 1.), .5, chk ? 1 : .05);
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
    }

}
