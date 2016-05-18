#!/usr/bin/env python

# Light each LED in sequence, and repeat.

import opc
import time
import colorsys
import sys

numLEDs = 54
client = opc.Client(sys.argv[1])

start_time = time.time()

while True:
    pixels = pixels = [(0, 0, 0)] * numLEDs
    seconds = (time.time() - start_time)
    x = (seconds % 5) / 5
    hue = (seconds % 10) / 10
    for i in range(numLEDs):
        loc = float(i)/numLEDs

        dist = min(abs(loc - x), abs(loc - x + 1), abs(loc - x - 1))
        value = max(1 - 3 * dist, .25)
        r, g, b = colorsys.hsv_to_rgb(hue, .7, value)
        pixels[i] = (int(r * 255), int(g * 255), int(b * 255))
    client.put_pixels(pixels)
    time.sleep(.03)
