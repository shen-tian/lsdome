#!/usr/bin/env python

# Light each LED in sequence, and repeat.

import opc
import time
import colorsys
import sys


def rgb2color(r, g, b):
    return int(r * 255), int(g * 255), int(b * 255)


def add(c1, c2):
    return min(c1[0] + c2[0], 255), min(c1[1] + c2[1], 255), min(c1[2] + c2[2], 255)


def get_rainbow_pulse(t):
    colors = [(0, 0, 0)] * numLEDs

    x = (t % 5) / 5
    hue = (t % 10) / 10
    for i in range(numLEDs):
        loc = float(i) / numLEDs

        dist = min(abs(loc - x), abs(loc - x + 1), abs(loc - x - 1))
        value = max(1 - 3 * dist, .25)
        r, g, b = colorsys.hsv_to_rgb(hue, .7, value)
        colors[i] = rgb2color(r, g, b)

    return colors


def get_charge_light(t):
    colors = [(0, 0, 0)] * numLEDs

    if t % 1 > .5:
        for i in range(5, 15):
            dist = abs(i - 9)
            colors[i] = rgb2color(max((1 - dist * .3), 0), 0, 0)

    return colors


numLEDs = 54
client = opc.Client(sys.argv[1])

start_time = time.time()

target_fps = 30

charge = True

while True:
    t = time.time() - start_time
    pixels = get_rainbow_pulse(t)
    pixels2 = get_charge_light(t)

    for j in range(numLEDs):
        pixels[j] = add(pixels[j], pixels2[j])

    client.put_pixels(pixels)
    time.sleep(1./target_fps)
