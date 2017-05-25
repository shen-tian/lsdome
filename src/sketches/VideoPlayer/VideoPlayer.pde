import processing.video.*;
import me.lsdo.processing.*;
import java.util.Properties.*;
import java.lang.Thread.*;

CanvasSketch sketch;

String filename = "/Users/Shen/lsdome/small.mp4";

float zoom = 1;

Movie movie;
PGraphics[] pyramid;

void setup()
{
    System.out.println("Launching video");
    size(480, 480, P3D);
    /*
    for (int i = 0; i < args.length; i++) {
        System.out.println(args[i]);
    }
    */
    
        if (this.args == null)
    {
        println("null");
    }
    else
    {
        println(args.length);
    }
    

    sketch = new CanvasSketch(this, new Dome(10), new OPC());

    movie = new Movie(this, filename);
    movie.loop();
    
    delay(1000);
    
    while (!movie.available())
        delay(100);

    pyramid = new PGraphics[4];
    for (int i = 0; i < pyramid.length; i++) {
        pyramid[i] = createGraphics(width / (1 << i), height / (1 << i), P3D);
    }
}

void keyPressed() {
    if (key == ' ') movie.pause();
    if (key == ']') zoom *= 1.1;
    if (key == '[') zoom *= 0.9;
    if (key == 'n') {  
        movie.stop();
        movie = new Movie(this, "/Users/Shen/lsdome/club.mp4");
        movie.loop();
    }
}

void keyReleased() {
    if (key == ' ') movie.play();
}  

void movieEvent(Movie m)
{
    if (m.available()) {
        m.read();
    }
}

void draw()
{
    // Scale to width, center height
    int mWidth = int(pyramid[0].width * zoom);
    int mHeight = mWidth * movie.height / movie.width;

    // Center location
    float x, y;

    if (mousePressed) {
        // Pan horizontally and vertically with the mouse
        x = -mouseX * (mWidth - pyramid[0].width) / width;
        y = -mouseY * (mHeight - pyramid[0].height) / height;
    } else {
        // Centered
        x = -(mWidth - pyramid[0].width) / 2;
        y = -(mHeight - pyramid[0].height) / 2;
    }

    pyramid[0].beginDraw();
    pyramid[0].background(0);
    pyramid[0].image(movie, x, y, mWidth, mHeight);
    pyramid[0].endDraw();

    for (int i = 1; i < pyramid.length; i++) {
        pyramid[i].beginDraw();
        pyramid[i].image(pyramid[i-1], 0, 0, pyramid[i].width, pyramid[i].height);
        pyramid[i].endDraw();
    }

    image(pyramid[pyramid.length - 1], 0, 0, width, height);

    sketch.draw();
}
