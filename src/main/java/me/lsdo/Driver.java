package me.lsdo;

import me.lsdo.ab16.*;
import me.lsdo.processing.*;

import java.util.Random;

public class Driver
{
    public static final int FPS_CAP = 60;

    public static void main(String[] args){
        if (args.length > 0 && args[0].equals("pixelflock"))
            PixelFlock.main(new String[]{"me.lsdo.ab16.PixelFlock"});
        else if (args.length > 0 && args[0].equals("particlefft"))
            ParticleFFT.main(new String[]{"me.lsdo.ab16.ParticleFFT"});
        else
            headless(args);
    }

    public static void headless(String[] args) {

        Dome dome = new Dome();
        OPC opc = new OPC();

        if (args.length > 0) {
            for (int i = 0; i < 5; i++)
                RunAnimation(dome, opc, args[0], 0);
        }
        else{
            Random random = new Random(0);
            String[] animations = new String[] {
                    "rings",
                    "cloud",
                    "kaleidoscope",
                    "twinkle",
                    "pixeltest",
                    "noire",
                    "snowflake",
                    "dontknow"};
            while (true)
            {
                int dice = random.nextInt(animations.length);
                System.out.println(dice);
                RunAnimation(dome, opc, animations[dice], 60);
            }
        }
    }

    private static void RunAnimation(Dome dome, OPC opc, String name, int duration)
    {
        DomeAnimation animation;
        if (name.equals("rings")) {
            System.out.println("Starting rings");
            animation = new Rings(dome, opc);
        } else if (name.equals("cloud")) {
            System.out.println("Starting cloud");
            animation = new Cloud(dome, opc);
        } else if (name.equals("kaleidoscope")) {
            System.out.println("Starting kaleidoscope");
            animation = new Kaleidoscope(dome, opc);
        } else if (name.equals("twinkle")) {
            System.out.println("Starting twinkle");
            animation = new Twinkle(dome, opc);
        } else if (name.equals("pixeltest")) {
            System.out.println("Starting pixeltest");
            animation = new PixelTest(dome, opc);
        } else if (name.equals("gridtest")) {
            System.out.println("Starting gridtest");
            animation = new GridTest(dome, opc);
        } else if (name.equals("noire")) {
            System.out.println("Starting noire");
            animation = new Noire(dome, opc);
        } else if (name.equals("snowflake")) {
            System.out.println("Starting snowflake");
            animation = new Snowflake(dome, opc);
        } else if (name.equals("dontknow")) {
            System.out.println("Starting dontknow");
            animation = new DontKnow(dome, opc);
        } else if (name.equals("tube")) {
            System.out.println("Starting tube");
            animation = new Tube(dome, opc);
        } else {
	    throw new RuntimeException("animation [" + name + "] not recognized");
	}

        RunAnimation(animation, duration);
    }

    private static void RunAnimation(DomeAnimation animation, int duration)
    {
        long start = System.currentTimeMillis();
        double t = 0;
        while (t < duration || duration == 0)
        {
            t = (System.currentTimeMillis() - start) / 1000d;
            animation.draw(t);
            double s = (System.currentTimeMillis() - start) / 1000d;
            int ms = (int)((s - t) * 1000);
            try {
                Thread.sleep(Math.max((int)(1000./FPS_CAP) - ms, 0));
            } catch (InterruptedException ie) {
            }
        }
    }
}
