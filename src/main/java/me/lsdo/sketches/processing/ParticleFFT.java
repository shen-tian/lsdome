package me.lsdo.sketches.processing;

import processing.core.*;
import ddf.minim.analysis.*;
import ddf.minim.*;
import me.lsdo.processing.*;

/**
 * Created by shen on 2016/09/16.
 */
public class ParticleFFT extends PApplet {
    // Some real-time FFT! This visualizes music in the frequency domain using a
// polar-coordinate particle system. Particle size and radial distance are modulated
// using a filtered FFT. Color is sampled from an image.



    PImage dot;
    PImage colors;
    Minim minim;
    AudioInput in;
    FFT fft;
    float[] fftFilter;

    float spin = 0.001f;
    float radiansPerBucket = radians(2);
    float decay = 0.97f;
    float opacity = 40f;
    float minSize = 0.1f;
    float sizeScale = 0.2f;

    CanvasSketch canvas;

    public void setup()
    {
        size(300, 300, P3D);

        Dome dome = new Dome();
        OPC opc = new OPC();
        canvas = new CanvasSketch(this, dome, opc);

        minim = new Minim(this);

        // Small buffer size!
        in = minim.getLineIn();
        fft = new FFT(in.bufferSize(), in.sampleRate());
        fftFilter = new float[fft.specSize()];

	// TODO figure out where processing thinks the data dir is to avoid hardcoding these paths
        dot = loadImage("/home/drew/dev/lsdome/lsdome/src/sketches/particle_fft/data/dot.png");
        colors = loadImage("/home/drew/dev/lsdome/lsdome/src/sketches/particle_fft/data/colors.png");
    }

    public void draw()
    {
        background(0);

        fft.forward(in.mix);
        for (int i = 0; i < fftFilter.length; i++) {
            fftFilter[i] = max(fftFilter[i] * decay, log(1 + fft.getBand(i)));
        }

        for (int i = 0; i < fftFilter.length; i += 3) {
            int rgb = colors.get((int)(map(i, 0, fftFilter.length-1, 0, colors.width-1)), colors.height/2);
            tint(rgb, fftFilter[i] * opacity);
            blendMode(ADD);

            float size = height * (minSize + sizeScale * fftFilter[i]);
            PVector center = new PVector(width * (fftFilter[i] * 0.2f), 0f);
            center.rotate(millis() * spin + i * radiansPerBucket);
            center.add(new PVector(width * 0.5f, height * 0.5f));

            image(dot, center.x - size/2, center.y - size/2, size, size);
        }

        canvas.draw();
    }


}
