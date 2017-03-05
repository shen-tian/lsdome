package me.lsdo.ab16;

import processing.core.*;
import me.lsdo.processing.*;
import java.awt.*;
import java.awt.image.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import java.nio.*;

// TODO support asymmetric stretching of dome canvas to match capture area dimensions
// TODO support shrinking display area to get more of window visible, at expense of 'letterboxing'
// TODO support aligning by either horizontal or vertical axis
// TODO choose viewport automatically based on named window (os-specific)

public class Screencast extends XYAnimation {

    public abstract class ScreenGrabber {
	int x0 = 200;
	int y0 = 200;
	int width;
	int height;

	public ScreenGrabber(int width, int height) {
	    this.width = width;
	    this.height = height;
	}
	
	public abstract void captureFrame();
	public abstract int getPixel(int x, int y);
    }

    // simple and portable, but slow as balls
    public class RobotGrabber extends ScreenGrabber {
	Robot robot;
	BufferedImage frame;

	public RobotGrabber(int width, int height) {
	    super(width, height);

	    try {
		robot = new Robot();
	    } catch (AWTException e) {
		throw new RuntimeException(e);
	    }
	}
	
	public void captureFrame() {
	    frame = robot.createScreenCapture(new Rectangle(x0, y0, width, height));
	}
	
	public int getPixel(int x, int y) {
	    return frame.getRGB(x, y);
	}
    }
    
    public class OpenCvGrabber extends ScreenGrabber {
	FFmpegFrameGrabber grabber;
	OpenCVFrameConverter.ToIplImage converter;
	IplImage img;
	ByteBuffer bytes;

	public OpenCvGrabber(int width, int height) {
	    super(width, height);

	    // Don't know the appropriate settings/formats for other OSes, but I think they
	    // theoretically are supported with the right incantations.
	    grabber = new FFmpegFrameGrabber(":0.0+" + x0 + "," + y0);
	    grabber.setFormat("x11grab");
	    grabber.setImageWidth(width);
	    grabber.setImageHeight(height);
	    try {
		grabber.start();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	    converter = new OpenCVFrameConverter.ToIplImage();
	}
	
	public void captureFrame() {
	    org.bytedeco.javacv.Frame f;
	    try {
		f = grabber.grab();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	    img = converter.convert(f);
	    bytes = img.getByteBuffer();
	}
	
	public int getPixel(int x, int y) {
	    int ix = img.nChannels() * (width * y + x);
	    int b = bytes.get(ix) & 0xFF; 
	    int g = bytes.get(ix + 1) & 0xFF;
	    int r = bytes.get(ix + 2) & 0xFF;
	    return (r << 16) | (g << 8) | b;
	}
    }

    int width;
    int height;
    ScreenGrabber grabber;
    
    public Screencast(Dome dome, OPC opc) {
	this(dome, opc, 512);
    }
    
    public Screencast(Dome dome, OPC opc, int size_px) {
        super(dome, opc, 8);
	this.width = size_px;
	this.height = size_px;
	//grabber = new RobotGrabber(width, height);
	grabber = new OpenCvGrabber(width, height);
    }

    @Override
    protected void preFrame(double t, double deltaT) {
	long start = System.currentTimeMillis();
	grabber.captureFrame();
	long end = System.currentTimeMillis();
	//System.out.println(String.format("capture: %d ms   framerate: %.1f", end - start, frameRate));
    }

    @Override
    protected PVector2 toIntermediateRepresentation(PVector2 p) {
	return LayoutUtil.normalizedXyToScreen(p, width, width); // won't stretch to fit currently
    }

    @Override
    protected int samplePoint(PVector2 p, double t) {
	return grabber.getPixel((int)Math.floor(p.x), (int)Math.floor(p.y));
    }
    
}

