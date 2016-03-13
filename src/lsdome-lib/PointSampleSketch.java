import java.util.*;
import processing.core.*;

public abstract class PointSampleSketch<E> extends FadecandySketch {

    final int DEFAULT_BASE_SUBSAMPLING = 1;
    final int MAX_SUBSAMPLING = 64;

    ArrayList<ArrayList<E>> points_ir;
    int base_subsampling;
    boolean temporal_jitter;

    PointSampleSketch(PApplet app) {
        super(app);
    }
    
    void init(int width, int height) {
        init(width, height, DEFAULT_BASE_SUBSAMPLING, false);
    }

    void init(int width, int height, int base_subsampling, boolean temporal_jitter) {
        super.init(width, height);

        this.base_subsampling = base_subsampling;
        this.temporal_jitter = temporal_jitter;

        points_ir = new ArrayList<ArrayList<E>>();
        for (PVector p : points) {
            ArrayList<E> samples = new ArrayList<E>();
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
    }

    PVector normalizePoint(PVector p) {
        return LayoutUtil.Vmult(p, 1. / radius);
    }

    double subsamplingBoost(PVector p) {
        return 1.;
    }

    E toIntermediateRepresentation(PVector p) {
        // Default implementation assumes E is PVector
        return (E)p;
    }

    void draw(double t) {
        beforeFrame(t);

        app.background(0);
        app.loadPixels();
        for (int i = 0; i < points.size(); i++) {
            app.pixels[opc.pixelLocations[i]] = sampleAntialiased(points_ir.get(i), t);
        }
        app.updatePixels();

        afterFrame(t);
    }

    void beforeFrame(double t) { }

    void afterFrame(double t) { }

    int sampleAntialiased(ArrayList<E> sub, double t) {
        int[] samples = new int[sub.size()];
        for (int i = 0; i < sub.size(); i++) {
            double t_jitter = (temporal_jitter ? (Math.random() - .5) / app.frameRate : 0.);
            samples[i] = samplePoint(sub.get(i), t + t_jitter);
        }
        return blendSamples(samples);
    }

    abstract int samplePoint(E ir, double t);

    int blendSamples(int[] samples) {
        int blended = samples[0];
        for (int i = 1; i < samples.length; i++) {
            blended = app.lerpColor(blended, samples[i], (float)(1. / (1. + i)));
        }
        return blended;
    }

}
