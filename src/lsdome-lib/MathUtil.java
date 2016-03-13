public class MathUtil {

    static double LN2 = Math.log(2.);

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

    static double log2(double x) {
        return Math.log(x) / LN2;
    }

}
