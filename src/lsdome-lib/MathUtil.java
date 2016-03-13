public class MathUtil {

    // Fix java's stupid AF mod operator to always return a positive result
    static int mod(int a, int b) {
        return ((a % b) + b) % b;
    }
    
    static float fmod(float a, float b) {
        float mod = a % b;
        if (mod < 0) {
            mod += b;
        }
        return mod;
    }

}