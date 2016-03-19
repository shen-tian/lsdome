import processing.core.*;
import processing.video.*;
import java.util.Arrays;

// crop viewing area to extents of pixel grid
// option to preserve aspect ratio (with crop or shrink)
// contrast stretch

// p: play/plause
// .: ff 5 sec
// ,: rewind 5 sec

enum VideoSizing {
    STRETCH_TO_FIT,
    PRESERVE_ASPECT_GROW,
    PRESERVE_ASPECT_SHRINK
}

public class VideoPlayer extends FadecandySketch implements OPC.FramePostprocessor {

    static final double[] skips = {5};

    Movie mov;
    boolean playing;
    VideoSizing sizeMode;
    boolean contrastStretch;

    public VideoPlayer(PApplet app, int size_px, String filename) {
        this(app, size_px, filename, VideoSizing.STRETCH_TO_FIT, false);
    }

    public VideoPlayer(PApplet app, int size_px, String filename, VideoSizing sizeMode, boolean contrastStretch) {
        super(app, size_px);
        mov = new Movie(app, filename);
        this.sizeMode = sizeMode;
        this.contrastStretch = contrastStretch;
    }

    void init() {
        super.init();
        opc.framePostprocessor = this;

        mov.play(); // or loop() ?
        playing = true;
        System.out.println("duration: " + mov.duration());
        // TODO some event when playback has finished?
    }

    void draw(double t) {
        app.image(mov, 0, 0, width, height);
    }

    void keyPressed() {
        int dir = 0;
        if (app.key == '.') {
            dir = 1;
        } else if (app.key == ',') {
            dir = -1;
        } else if (app.key == 'p') {
            if (playing) {
                mov.pause();
            } else {
                mov.play();
            }
            playing = !playing;
        }

        if (dir != 0) {
            double t = Math.max(0, Math.min(mov.duration(), mov.time() + dir * skips[0]));
            mov.jump((float)t);
            System.out.println(String.format("%.2f / %.2f", t, mov.duration()));
        }
    }

    class ContrastStretch implements OPC.FramePostprocessor {
        final double BENCHMARK_PCTILE = .95;

        // FIXME pixels outside the video projection area should be excluded
        public void postProcessFrame(int[] pixelBuffer) {
            int numPixels = pixelBuffer.length;
            float[] lums = new float[numPixels];
            for (int i = 0; i < numPixels; i++) {
                int pixel = pixelBuffer[i];
                lums[i] = app.brightness(pixel);
            }
            Arrays.sort(lums);

            float lowlum = lums[(int)((1-BENCHMARK_PCTILE) * numPixels)];
            float highlum = lums[(int)(BENCHMARK_PCTILE * numPixels)];

            for (int i = 0; i < numPixels; i++) {
                int pixel = pixelBuffer[i];
                float h = app.hue(pixel);
                float s = app.saturation(pixel);
                float l = app.brightness(pixel);
                l = 100f * (l - lowlum) / (highlum - lowlum);
                pixelBuffer[i] = app.color(h, s, l);
            }
        }
    }
    OPC.FramePostprocessor _contrastStretch = new ContrastStretch();

    public void postProcessFrame(int[] pixelBuffer) {
        OPC.FramePostprocessor proc = null;
        if (contrastStretch) {
            proc = _contrastStretch;
        }
        if (proc != null) {
            proc.postProcessFrame(pixelBuffer);
        }
    }

}
