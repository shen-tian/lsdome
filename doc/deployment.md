# Notes on deploying this stuff

## Steps taken to run sketches on Raspbian

You have to be in X to run processing sketches. So to autoboot into it, there things were done, in no particular order.

* Export the sketch, targeting Linux (don't need to worry about arch)

### Setup the Pi so that the sketch runs 

We are working with Processing 2.x (most likely 2.2), apparently, Java 7 works best. Install it by

```
sudo apt-get install oracle-java7-jdk
```
and ensure it's the default Java by running:

```
sudo update-alternatives --config java
```

Now try to run the sketch (once you are in X) by typing `sh /path/yoursketch`. If you get something about can't use pixels on this device, it might be this issue: https://github.com/processing/processing/issues/2010.html

The fix is to force 32 bit mode. Adding this to the bottom of `\boot\config.txt` seems to work:

```
framebuffer_depth=32
framebuffer_ignore_alpha=1
```

* Ensure that X is running in 32bit mode

### Automatically launch into X, then the sketch

`sudo raspi-config` launches a config utility. One of the options there allows you to launch X automatically on boot.

Add a .desktop file to the startup folder (`/etc/xdg/autostart`)

format is something like this:

```
[Desktop Entry]
Name=Cloud Autostart
Exec=sh /home/pi/sketches/application.linux32/triangle_clouds
Type=Application
Terminal=true
```

Tried adding stuff to end of `sudo nano /etc/xdg/lxsession/LXDE-pi/autostart` but it didn't work.

