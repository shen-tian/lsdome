import java.util.*;
import processing.core.*;

enum PanelLayout {
    _13,
    _24
}

public class FadecandySketch {

    PApplet app;
    OPC opc;
    ArrayList<PVector> points;
    int panel_size;
    PanelLayout panel_config_mode;
    double radius;

    FadecandySketch(PApplet app) {
        this.app = app;
    }

    void init(int width, int height) {
        app.size(width, height, app.P2D);

        String hostname = "127.0.0.1";  // TODO get from config param
        int port = 7890;
        opc = new OPC(app, hostname, port);

        panel_size = 15; // TODO get from config param
        panel_config_mode = PanelLayout._13; // TODO get from config param
        switch (panel_config_mode) {
        case _13:
            points = LayoutUtil.fillLSDome13(panel_size);
            radius = LayoutUtil.panel13Radius();
            break;
        case _24:
            points = LayoutUtil.fillLSDome24(panel_size);
            radius = LayoutUtil.panel24Radius();
            break;
        default:
            throw new RuntimeException();
        }
        LayoutUtil.registerScreenSamples(opc, points, width, height, 2*radius, true);

        app.colorMode(app.HSB, 100);
    }
    
    // TODO for sketches that only render pixels directly, is there a cost penalty for going
    // through the screen buffer?
    void setLED(int i, int rgb) {
        int px = opc.pixelLocations[i];
        if (px >= 0) {
            app.pixels[px] = rgb;
        }
    }

    void draw() {
        draw(app.millis() / 1000.);
        System.out.println(app.frameRate); // TODO make conditional on some debug mode?
    }

    void draw(double t) {
        System.out.println("nothing to draw");
    }
    
    int color(double r, double g, double b) {
        return app.color((float)(100. * r), (float)(100. * g), (float)(100. * b));
    }

}