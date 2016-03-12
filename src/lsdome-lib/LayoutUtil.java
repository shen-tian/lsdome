import java.util.*;
import processing.core.*;

public class LayoutUtil {
    
    static double SQRT_3 = Math.sqrt(3.);
    static double PI = Math.PI;
    static double PI2 = 2. * PI;

    static double pixelSpacing(int n) {
        return 1. / (n - 1 + SQRT_3);
    }
    
    static PVector V(double x, double y) {
        return new PVector((float)x, (float)y);
    }

    static PVector V(PVector v) {
        return V(v.x, v.y);
    }

    static PVector Vadd(PVector a, PVector b) {
        return PVector.add(a, b);
    }

    static PVector Vmult(PVector v, double k) {
        return PVector.mult(v, (float)k);
    }

    static PVector Vrot(PVector v, double theta) {
        PVector rot = V(v);
        rot.rotate((float)theta);
        return rot;
    }

    // Evenly fill a triangle with a grid of points of size n. The triangle filled is an equilateral triangle
    // with points at (0, 0), (1, 0), and (.5, sqrt(3)/2). Points are placed such that spacing between two
    // adjacent points will match the spacing between an edge point and the opposing point of a neighboring
    // triangle. Returns a list of points traversed in a boustrophedon manner, starting near the origin,
    // proceeding left/right, then upward. The point near (0, 0) will thus be known as the 'entry' point, and
    // the top-most point as the 'exit' point.
    static ArrayList<PVector> fillTriangle(int n) {
        double spacing = pixelSpacing(n);
        PVector offset = V(spacing * .5 * SQRT_3, spacing * .5);
        
        ArrayList<PVector> points = new ArrayList<PVector>();
        for (int row = 0; row < n; row++) {
            boolean reversed = (row % 2 == 1);
            int width = n - row;
            for (int col = 0; col < width; col++) {
                int c = (reversed ? width - 1 - col : col);
                PVector p = Vadd(axialToXy(Vmult(V(row, c), spacing)), offset);
                points.add(p);
            }
        }
        return points;
    }
    
    // Fill a triangle using the sizing and entry/exit semantics from above, where the triangle's origin is
    // the axial UV coordinate 'entry' and rotated clockwise by angle 60deg * rot
    static ArrayList<PVector> fillTriangle(PVector entry, int rot, int n) {
        ArrayList<PVector> points = new ArrayList<PVector>();
        for (PVector p : fillTriangle(n)) {
            points.add(Vadd(Vrot(p, -rot * PI / 3.), axialToXy(entry)));
        }
        return points;
    }
    
    // Get the exit point for a triangle fill
    static PVector fillExitPoint(PVector entry, int rot) {
        return axialNeighbor(entry, rot - 1);
    }
    
    // Fill a fan of triangles proceeding in a clockwise fashion until a complete hexagon whose perimeter
    // intersects the origin is filled. 'segments' is the number of triangular segments to fill (up to 6).
    // 'pixels' is the fill density within each triangle. 'orientation' is the initial orientation in
    // which the long axis of the hexagon follows the angle specified by 'rot' semantics above.
    static ArrayList<PVector> fillFan(int orientation, int segments, int pixels) {
        ArrayList<PVector> points = new ArrayList<PVector>();
        PVector entry = V(0., 0.);
        int rot = orientation;
        for (int i = 0; i < segments; i++) {
            points.addAll(fillTriangle(entry, rot, pixels));
            entry = fillExitPoint(entry, rot);
            rot += 1;
        }
        return points;
    }
    
    // Fill the 24-panel lsdome configuration
    static ArrayList<PVector> fillLSDome24(int n) {
        ArrayList<PVector> points = new ArrayList<PVector>();
        for (int i = 0; i < 6; i++) {
            points.addAll(fillFan(i, 4, n));
        }
        return points;
    }
    
    // Fill the 13-panel lsdome configuration
    static ArrayList<PVector> fillLSDome13(int n) {
        ArrayList<PVector> entries = new ArrayList<PVector>();
        entries.add(axialToXy(V(1, 0)));
        entries.add(axialToXy(V(0, 1)));
        entries.add(axialToXy(V(0, 0)));
        ArrayList<PVector> points = new ArrayList<PVector>();
        for (int i = 0; i < 3; i++) {
            for (PVector p : fillFan(2*i+1, 4, n)) {
                points.add(PVector.add(p, entries.get(i)));
            }
        }
        points.addAll(fillTriangle(V(0, 0), 0, n));
        
        PVector offset = axialToXy(V(-1/3., -1/3.));
        for (int i = 0; i < points.size(); i++) {
            points.set(i, PVector.add(points.get(i), offset));
        }
        
        System.out.println(points.size());

        return points;
    }
    
    // Compute a basis transformation for vector p, where u is the transformation result of basis vector U (1, 0),
    // and v is the transformation of basis V (0, 1)
    static PVector basisTransform(PVector p, PVector u, PVector v) {
        return V(u.x * p.x + v.x * p.y, u.y * p.x + v.y * p.y);
    }
    
    // Convert a 2-vector of (U, V) coordinates from the axial coordinate scheme into (x, y) cartesian coordinates
    static PVector axialToXy(PVector p) {
        PVector U = V(.5, .5 * SQRT_3);
        PVector V = V(1., 0.);
        return basisTransform(p, U, V);
    }
    
    // Convert (x, y) coordinate p to screen pixel coordinates where top-left is pixel (0, 0) and bottom-right is
    // pixel (width, height). 'span' is the size of the viewport in world coordinates, where size means width if horizSpan is
    // true and height if horizSpan is false. World origin is in the center of the viewport.
    static PVector xyToScreen(PVector p, int width, int height, float span, boolean horizSpan) {
        float scale = span / (horizSpan ? width : height);
        PVector U = V(1. / scale, 0);
        PVector V = V(0, -1. / scale);
        PVector offset = V(.5 * width, .5 * height);
        return PVector.add(basisTransform(p, U, V), offset);
    }
    
    // For sampling from a rendered screen. Convert led positions from world coordinates to screen pixels and register with
    // the fadecandy(ies).
    static void registerScreenSamples(OPC opc, ArrayList<PVector> points, int width, int height, float span, boolean horizSpan) {
        for (int ix = 0; ix < points.size(); ix++) {
            PVector px = xyToScreen(points.get(ix), width, height, span, horizSpan);
            opc.led(ix, (int)px.x, (int)px.y);
        }
    }
    
    // Return the adjacent axial coordinate moving from 'p' in direction 'rot'
    static PVector axialNeighbor(PVector p, int rot) {
        int axis = mod(rot, 3);
        boolean hemi = (mod(rot, 6) < 3);
        int du = (axis == 0 ? 0 : (hemi ? -1 : 1));
        int dv = (axis == 2 ? 0 : (hemi ? 1 : -1));
        return V(p.x + du, p.y + dv);
    }
    
    // Return whether two axial coordinates are adjacent lattice points
    static boolean axialCoordsAdjacent(PVector a, PVector b) {
        int du = (int)a.x - (int)b.x;
        int dv = (int)a.y - (int)b.y;
        return (du >= -1 && du <= 1 && dv >= -1 && dv <= 1 && du != dv);
    }
    
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