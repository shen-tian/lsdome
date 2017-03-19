package me.lsdo.sketches.headless;

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
import java.io.*;

public class Screencast extends XYAnimation {

    static final int SUBSAMPLING = 8;
    
    int width;
    int height;
    int xo = 200;
    int yo = 200;
    boolean alignHorizontal;
    double xscale;
    double yscale;
    ScreenGrabber grabber;
    PVector2 viewport0;
    PVector2 viewportDim;
    
    public Screencast(Dome dome, OPC opc) {
	this(dome, opc, 512);
    }

    
    // screencap a square of size x size pixels without any stretching
    public Screencast(Dome dome, OPC opc, int size) {
        super(dome, opc, SUBSAMPLING);
	this.width = size;
	this.height = size;
	this.alignHorizontal = true;
	this.xscale = 1.;
	this.yscale = 1.;

	initGrabber();

	viewport0 = LayoutUtil.V(-1, -1);
	viewportDim = LayoutUtil.V(2, 2);
    }

    
    public Screencast(Dome dome, OPC opc, int width, int height, boolean alignHorizontal) {
	this(dome, opc, width, height, alignHorizontal, 1., 1.);
    }

    // screencap a width x height area, stretching to perfecting fit the dome viewport
    public Screencast(Dome dome, OPC opc, int width, int height, boolean alignHorizontal, double xscale, double yscale) {
        super(dome, opc, SUBSAMPLING);
	this.width = width;
	this.height = height;
	this.alignHorizontal = alignHorizontal;
	this.xscale = xscale;
	this.yscale = yscale;

	initGrabber();
	initViewport();
    }


    public Screencast(Dome dome, OPC opc, String pidOrTitle) {
	this(dome, opc, pidOrTitle, true, 1., 1.);
    }

    // screencap while matching the dimensions to a specific window
    public Screencast(Dome dome, OPC opc, String pidOrTitle, boolean alignHorizontal, double xscale, double yscale) {
        super(dome, opc, SUBSAMPLING);
	PVector2 windowPlacement[] = getWindowPlacement(pidOrTitle);
	this.width = (int)windowPlacement[1].x;
	this.height = (int)windowPlacement[1].y;
	this.xo = (int)windowPlacement[0].x;
	this.yo = (int)windowPlacement[0].y;
	System.out.println(String.format("%dx%d+%d,%d", width, height, xo, yo));
	this.alignHorizontal = alignHorizontal;
	this.xscale = xscale;
	this.yscale = yscale;

	initGrabber();
	initViewport();
    }
    
    public abstract class ScreenGrabber {
	int width;
	int height;
	int x0;
	int y0;

	public ScreenGrabber(int width, int height, int xo, int yo) {
	    this.width = width;
	    this.height = height;
	    this.x0 = xo;
	    this.y0 = yo;
	}
	
	public abstract void captureFrame();
	public abstract int getPixel(int x, int y);
    }

    // simple and portable, but slow as balls
    public class RobotGrabber extends ScreenGrabber {
	Robot robot;
	BufferedImage frame;

	public RobotGrabber(int width, int height, int xo, int yo) {
	    super(width, height, xo, yo);

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

    // decent framerate, but still has a slight delay
    public class OpenCvGrabber extends ScreenGrabber {
	FFmpegFrameGrabber grabber;
	OpenCVFrameConverter.ToIplImage converter;
	IplImage img;
	ByteBuffer bytes;

	public OpenCvGrabber(int width, int height, int xo, int yo) {
	    super(width, height, xo, yo);

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

    private void initGrabber() {
	//grabber = new RobotGrabber(width, height, xo, yo);
	grabber = new OpenCvGrabber(width, height, xo, yo);
    }

    private void initViewport() {
	PVector2 viewport[] = dome.getViewport(rotAngle());
	viewport0 = normalizePoint(viewport[0]);
	viewportDim = normalizePoint(viewport[1]);
    }

    // depends on x11
    private PVector2[] getWindowPlacement(String pidOrTitle) {
	while(true) {
	    try {
		Process p = Runtime.getRuntime().exec("wmctrl -l -G -p");
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while ((line = reader.readLine()) != null) {
		    String[] parts = line.split(" +");
		    if (parts.length < 9) {
			continue;
		    }

		    String pid = parts[2];
		    int xo = Integer.parseInt(parts[3]);
		    int yo = Integer.parseInt(parts[4]);
		    int width = Integer.parseInt(parts[5]);
		    int height = Integer.parseInt(parts[6]);
		    StringBuilder sb = new StringBuilder();
		    for (int i = 8; i < parts.length; i++) {
			sb.append(parts[i]);
			if (i < parts.length - 1) {
			    sb.append(" ");
			}
		    }
		    String title = sb.toString();
		    
		    if (pid.equals(pidOrTitle) || title.startsWith(pidOrTitle)) {
			System.out.println("found window");
			return new PVector2[] {LayoutUtil.V(xo, yo), LayoutUtil.V(width, height)};
		    }
		}

		System.out.println("window not found");
		Thread.sleep(1000);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }
    
    @Override
    protected void preFrame(double t, double deltaT) {
	long start = System.currentTimeMillis();
	grabber.captureFrame();
	long end = System.currentTimeMillis();
	//System.out.println(String.format("capture: %d ms   framerate: %.1f", end - start, frameRate));
    }

    private double rotAngle() {
	return alignHorizontal ? 0. : Math.PI / 6.;
    }
    
    @Override
    protected PVector2 toIntermediateRepresentation(PVector2 p) {
	p = LayoutUtil.Vrot(p, rotAngle());
	p = LayoutUtil.V(p.x / xscale, p.y / yscale);
	return LayoutUtil.xyToScreenAsym(p, width, height, viewportDim.x, viewportDim.y);
    }

    @Override
    protected int samplePoint(PVector2 p, double t) {
	int x = (int)Math.floor(p.x);
	int y = (int)Math.floor(p.y);
	if (x < 0 || x >= width || y < 0 || y >= height) {
	    return 0;
	} else {
	    return grabber.getPixel((int)Math.floor(p.x), (int)Math.floor(p.y));
	}
    }
    
}

