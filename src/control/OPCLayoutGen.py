#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Script for generating LED layout files. Immediately, useful for the gl_serer simulator
# that comes with openpixelcontrol, but could also be used client side?

import json
import sys
import math


def rotate(origin, point, angle):
    """
    Rotate a point counterclockwise by a given angle around a given origin.

    The angle should be given in radians.
    """
    ox, oy = origin
    px, py = point

    qx = ox + math.cos(angle) * (px - ox) - math.sin(angle) * (py - oy)
    qy = oy + math.sin(angle) * (px - ox) + math.cos(angle) * (py - oy)
    return qx, qy


def get_line(x, y, n, space, rot):
    leds = []

    for i in range(n):
        x_i = x + (i - (n-1)/2.) * space
        y_i = y
        x_i, y_i = rotate((x, y), (x_i, y_i), rot)
        leds.append({"point": [x_i, y_i, 0]})
    return leds


def main(argv):
    fname = argv[0]

    leds = list()

    leds.extend(get_line(0, -8./38., 19, 1/19., 0))
    leds.extend(get_line(.5, 0, 8, 1/19., math.pi/2))
    leds.extend(get_line(0, 8./38., 19, 1/19., math.pi))
    leds.extend(get_line(-.5, 0, 8, 1/19., 3 * math.pi/2))

    with open(fname, 'w') as outfile:
        json.dump(leds, outfile, sort_keys=True, indent=4)

    exit(0)

if __name__ == '__main__':
    main(sys.argv[1:])