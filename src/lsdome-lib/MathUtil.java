public class MathUtil {

    // Fix java's stupid AF mod operator to always return a positive result
    static int mod(int a, int b) {
        return ((a % b) + b) % b;
    }
    
    static double fmod(double a, double b) {
        double mod = a % b;
        if (mod < 0) {
            mod += b;
        }
        return mod;
    }

}