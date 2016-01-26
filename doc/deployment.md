# Notes on deploying this stuff

## Steps taken to run sketches on Raspbian

You have to be in X to run processing sketches. So to autoboot into it, there things were done, in no particular order.

* Export the sketch, targeting Linux (don't need to worry about arch)
* Set the pi to go into X (default pi user is fine)
* Install java7, and set it to be default
* Ensure that X is running in 32bit mode
* Add a .desktop file to the startup folder (`/etc/xdg/autostart`)

format is something like this:

```
[Desktop Entry]
Name=Cloud Autostart
Exec=sh /home/pi/sketches/application.linux32/triangle_clouds
Type=Application
Terminal=true
```

