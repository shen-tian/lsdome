// Template for sketches that compute pixel values directly based on their (x,y) position within a
// a scene. This implies you have a sampling function to render a scene pixel-by-pixel (such as
// ray-tracing and fractals). It is not for scenes that must be rendered the whole screen at once
// (i.e., anything using GPU acceleration), nor for 'pixel art'-type scenes where you want to treat
// the pixels as a discrete grid.
//
// Both spatial and temporal anti-aliasing is supported.

import java.util.*;
import processing.core.*;

// IR is the type of the intermediate representation of the individual points to be sampled/rendered.
public abstract class PointSampleSketch<IR, S> extends FadecandySketch<S> {

    static final int DEFAULT_BASE_SUBSAMPLING = 1;
    static final int MAX_SUBSAMPLING = 64;

    // Mapping of display pixels to 1 or more actual samples that will be combined to yield that
    // display pixel's color.
    ArrayList<ArrayList<IR>> points_ir;

    // Amount of subsampling for each display pixel.
    int base_subsampling;

    // If true, perform anti-aliasing in the time domain. Combined with subsampling, this will yield
    // motion blur.
    boolean temporal_jitter;

    PointSampleSketch(PApplet app, int size_px) {
        this(app, size_px, DEFAULT_BASE_SUBSAMPLING, false);
    }

    PointSampleSketch(PApplet app, int size_px, int base_subsampling, boolean temporal_jitter) {
        super(app, size_px);
        this.base_subsampling = base_subsampling;
        this.temporal_jitter = temporal_jitter;        
    }
    
    // Assign each display pixel to N random samples based on the required amount of subsampling.
    // Furthermore, each subsample is converted to its intermediate representation to avoid
    // re-computing it every frame.
    void init() {
        super.init();

        points_ir = new ArrayList<ArrayList<IR>>();
        int total_subsamples = 0;
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

            total_subsamples += num_subsamples;
        }

        System.out.println(String.format("%d subsamples for %d pixels (%.1f samples/pixel)",
                                         total_subsamples, points.size(), (double)total_subsamples / points.size()));
    }

    // Convert an xy coordinate in 'panel length' units such that the perimeter of the display area
    // is the unit circle.
    PVector normalizePoint(PVector p) {
        return LayoutUtil.Vmult(p, 1. / radius);
    }

    // **OVERRIDE** (optional)
    // We may want to perform more subsampling in certain areas. Return the factor (e.g., 2x, 3x) to
    // increase subsampling by at the given point.
    double subsamplingBoost(PVector p) {
        return 1.;
    }

    // **OVERRIDE** (optional)
    // Convert an xy point to be sampled into an intermediate representation, if it would save work
    // that would otherwise be re-computed each frame. E.g., this is a good place to do texture
    // mapping.
    IR toIntermediateRepresentation(PVector p) {
        // Default implementation assumes IR is PVector
        return (IR)p;
    }

    void draw(double t) {
        app.background(0);
        app.loadPixels();
        for (int i = 0; i < points.size(); i++) {
            app.pixels[opc.pixelLocations[i]] = sampleAntialiased(points_ir.get(i), t);
        }
        app.updatePixels();
    }

    // Perform the anti-aliasing for a single display pixel.
    int sampleAntialiased(ArrayList<IR> sub, double t) {
        int[] samples = new int[sub.size()];
        for (int i = 0; i < sub.size(); i++) {
            double t_jitter = (temporal_jitter ? (Math.random() - .5) / app.frameRate : 0.);
            samples[i] = samplePoint(sub.get(i), t + t_jitter, t_jitter);
        }
        return blendSamples(samples);
    }

    // Render an individual sample. 't' is clock time, including temporal jitter. 't_jitter' is the
    // amount of jitter added. Return a color.
    abstract int samplePoint(IR ir, double t, double t_jitter);

    int blendSamples(int[] samples) {
        int blended = samples[0];
        for (int i = 1; i < samples.length; i++) {
            //TODO what colorspace does fadecandy use?
            blended = app.lerpColor(blended, samples[i], (float)(1. / (1. + i)));
        }
        return blended;
    }

}
