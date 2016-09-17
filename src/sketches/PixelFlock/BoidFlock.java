

/**
 * Created by shen on 2016/09/17.
 */

import java.util.ArrayList;

public class BoidFlock {
    ArrayList<Boid> boids; // An ArrayList for all the boids
    int currentHue = 60;

    BoidFlock() {
        boids = new ArrayList<Boid>(); // Initialize the ArrayList
    }

    void run() {
        for (Boid b : boids) {
            b.run(boids);  // Passing the entire list of boids to each boid individually
        }
    }

    void cycleHue() {
        int newHue = currentHue + 1;
        if (newHue > 100) {
            newHue = newHue - 100;
        }
        currentHue=newHue;
        for (Boid b : boids) {
            b.setHue(currentHue);
        }
    }

    void setBrightness(int brightness) {
        for (Boid b : boids) {
            b.setBrightness(brightness);
        }
    }

    void scatterFlock() {
        for (Boid b : boids) {
            b.sepWeight = 10.0f;
            b.aliWeight= 0.0f;
            b.cohWeight = 0.0f;
            b.maxspeed = 2 * Boid.MAX_SPEED;
        }
    }

    void collectFlock() {
        for (Boid b : boids) {
            b.sepWeight = 1.5f;
            b.aliWeight= 1.0f;
            b.cohWeight = 1.0f;
            b.maxspeed = Boid.MAX_SPEED;
        }
    }

    void addBoid(Boid b) {
        boids.add(b);
    }
}
