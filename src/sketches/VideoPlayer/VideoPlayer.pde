import processing.video.*;
import me.lsdo.processing.*;

CanvasSketch sketch;

String filename = "/Users/Shen/lsdome/small.mp4";

float zoom = 2;

Movie movie;
PGraphics[] pyramid;

static public void main(String args[]) {
  
    for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
    }
     String[] newArgs=new String[1];
    newArgs[0]="ul_vid_player";


  PApplet.main(newArgs);

}

void setup()
{
  size(480, 480, P3D);

  sketch = new CanvasSketch(this, new Dome(13), new OPC());

  movie = new Movie(this, filename);
  movie.loop();

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
    try{
    m.read();
}
    catch (NullPointerException e)
    {
        System.out.println("Oops");
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