import java.util.*;
import processing.core.*;

public abstract class PointSampleSketch<IR, S> extends FadecandySketch {

    static final int DEFAULT_BASE_SUBSAMPLING = 1;
    static final int MAX_SUBSAMPLING = 64;

    ArrayList<ArrayList<IR>> points_ir;
    S state;
    double last_t;

    int base_subsampling;
    boolean temporal_jitter;

    PointSampleSketch(PApplet app, int size_px) {
        this(app, size_px, DEFAULT_BASE_SUBSAMPLING, false);
    }

    PointSampleSketch(PApplet app, int size_px, int base_subsampling, boolean temporal_jitter) {
        super(app, size_px);
        this.base_subsampling = base_subsampling;
        this.temporal_jitter = temporal_jitter;        
    }
    
    void init() {
        super.init();

        points_ir = new ArrayList<ArrayList<IR>>();
        for (PVector p : points) {
            ArrayList<IR> samples = new ArrayList<IR>();
            points_ir.add(samples);

            p = normalizePoint(p);
            int num_subsamples = Math.min((int)Math.ceil(base_subsampling * subsamplingBoost(p)), MAX_SUBSAMPLING);
            boolean jitter = (num_subsamples > 1);
            for (int i = 0; i < num_subsamples; i++) {
                PVector offset = (jitter ?
                                  normalizePoint(LayoutUtil.polarToXy(LayoutUtil.V(
                                      Math.random() * .5*LayoutUtil.pixelSpacing(panel_size),
                                      Math.random() * 2*Math.PI
                                  ))) :
                                  LayoutUtil.V(0, 0));
                PVector sample = LayoutUtil.Vadd(p, offset);
                samples.add(toIntermediateRepresentation(sample));
            }
        }

        state = initialState();
    }

    PVector normalizePoint(PVector p) {
        return LayoutUtil.Vmult(p, 1. / radius);
    }

    double subsamplingBoost(PVector p) {
        return 1.;
    }

    IR toIntermediateRepresentation(PVector p) {
        // Default implementation assumes IR is PVector
        return (IR)p;
    }

    S initialState() {
        return null;
    }

    S updateState(S state, double t_delta) {
        return state;
    }

    void draw(double t) {
        _updateState(t);
        beforeFrame(t);

        app.background(0);
        app.loadPixels();
        for (int i = 0; i < points.size(); i++) {
            app.pixels[opc.pixelLocations[i]] = sampleAntialiased(points_ir.get(i), t);
        }
        app.updatePixels();

        afterFrame(t);
    }

    void _updateState(double t) {
        if (last_t > 0) {
            state = updateState(state, t - last_t);
        }
        last_t = t;
    }

    void beforeFrame(double t) { }

    void afterFrame(double t) { }

    int sampleAntialiased(ArrayList<IR> sub, double t) {
        int[] samples = new int[sub.size()];
        for (int i = 0; i < sub.size(); i++) {
            double t_jitter = (temporal_jitter ? (Math.random() - .5) / app.frameRate : 0.);
            samples[i] = samplePoint(sub.get(i), t + t_jitter, t_jitter);
        }
        return blendSamples(samples);
    }

    abstract int samplePoint(IR ir, double t, double t_jitter);

    int blendSamples(int[] samples) {
        int blended = samples[0];
        for (int i = 1; i < samples.length; i++) {
            blended = app.lerpColor(blended, samples[i], (float)(1. / (1. + i)));
        }
        return blended;
    }

}
