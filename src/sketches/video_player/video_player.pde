// Some real-time FFT! This visualizes music in the frequency domain using a
// polar-coordinate particle system. Particle size and radial distance are modulated
// using a filtered FFT. Color is sampled from an image.

import processing.video.*;

FadecandySketch driver = new FadecandySketch(this, 250, 250);

// autosize canvas based on pixel density
// crop viewing area to extents of pixel grid
// option to preserve aspect ratio (with crop or shrink)
// contrast stretch

Movie mov;

void setup() {
  driver.init();
  //mov = new Movie(this, "/home/drew/Downloads/Game.of.Thrones.S05E09.HDTV.x264-ASAP.mp4");
  //mov = new Movie(this, "/mnt/ext/media/electric-sheep-in-hd-mkv/electric-sheep-in-hd.mkv");
  //mov = new Movie(this, "/home/drew/media/video/musicvideos/ponpon.mp4");
  //mov = new Movie(this, "/home/drew/media/video/musicvideos/missy_elliott-work_it.flv");
  mov = new Movie(this, "/home/drew/media/video/musicvideos/the_knife-we_share_our_mothers_health.mp4");
  //mov = new Movie(this, "/home/drew/Downloads/Justice_-_DVNO_(Official_Video).mp4");
  mov.play();
}

void draw() {
  image(mov, 0, 0, width, height);
}

void keyPressed() {
  double skip = 0;
  if (key == '.') {
    skip = 5;
  } else if (key == ',') {
    skip = -5;
  }
  if (skip == 0) {
    return;
  }

  mov.jump(mov.time() + (float)skip);
}

void movieEvent(Movie m) { 
  m.read(); 
} 