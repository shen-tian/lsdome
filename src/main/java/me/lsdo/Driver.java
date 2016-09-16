package me.lsdo;

import me.lsdo.ab16.*;
import me.lsdo.processing.*;

public class Driver
{
    public static void main(String[] args){
        //ParticleFFT.main(new String[]{"me.lsdo.ab16.ParticleFFT"});

        headless(args);


    }

    public static void headless(String[] args) {

        long start = System.currentTimeMillis();

        Dome dome = new Dome(6);
        OPC opc = new OPC();

        DomeAnimation animation;
        if (args.length > 0) {
            if (args[0].equals("rings")) {
                System.out.println("Starting rings");
                animation = new Rings(dome, opc);
            } else if (args[0].equals("cloud")) {
                System.out.println("Starting cloud");
                animation = new Cloud(dome, opc);
            } else if (args[0].equals("kaleidoscope")) {
                System.out.println("Starting kaleidoscope");
                animation = new Kaleidoscope(dome, opc);
            } else if (args[0].equals("twinkle")) {
                System.out.println("Starting twinkle");
                animation = new Twinkle(dome, opc);
            } else if (args[0].equals("pixeltest")) {
                System.out.println("Starting pixeltest");
                animation = new PixelTest(dome, opc);
            } else
                animation = new Rings(dome, opc);
        } else
            animation = new Rings(dome, opc);

        while (true) {
            double t = (System.currentTimeMillis() - start) / 1000d;
            animation.draw(t);
        }
    }
}
