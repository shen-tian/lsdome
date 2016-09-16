package me.lsdo;

import me.lsdo.processing.*;

public class Driver
{
    public static void main(String[] args){
        long start = System.currentTimeMillis();

        Dome dome = new Dome();
        OPC opc = new OPC("127.0.0.1", 7890);
        //DomeAnimation animation = new PixelTestAnimation(dome, opc);

        while(true) {
            double t = (System.currentTimeMillis() - start) / 1000d;
            //animation.draw(t);
        }
    }
}
