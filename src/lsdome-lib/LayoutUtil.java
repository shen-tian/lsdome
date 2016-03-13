import java.util.*;
import processing.core.*;

public class LayoutUtil {
    
    /**
     * Axial coordinates are a coordinate system referring to vertices of a triangular grid. The
     * grid is oriented such that triangle edges are horizontal (coincident with the x-axis) and at
     * +/-60 degrees. For a grid coordinate (u, v), u refers to coordinates increasing from the
     * origin in the line 60 degrees counter-clockwise from the +x axis, while v refers to
     * increasing along the x-axis.
     */

    static double SQRT_3 = Math.sqrt(3.);
    static double PI = Math.PI;

    // Convenience methods to make vector math easier. Input arguments are treated as constants.

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

    // Compute a basis transformation for vector p, where u is the transformation result of basis vector U (1, 0),
    // and v is the transformation of basis V (0, 1)
    static PVector basisTransform(PVector p, PVector U, PVector V) {
        return Vadd(Vmult(U, p.x), Vmult(V, p.y));
    }

    // Spacing between points of a triangular grid of size 'n' where distance between points is twice the
    // distance from an edge point to the edge of the containing triangle.
    static double pixelSpacing(int n) {
        return 1. / (n - 1 + SQRT_3);
    }
    
    static interface Transform {
        public PVector transform(PVector p);
    }

    // Convert a set of points in bulk according to some transformation function.
    static ArrayList<PVector> transform(ArrayList<PVector> points, Transform tx) {
        ArrayList<PVector> transformed = new ArrayList<PVector>();
        for (PVector p : points) {
            transformed.add(tx.transform(p));
        }
        return transformed;
    }

    static Transform translate(final PVector o) {
        return new Transform() {
            public PVector transform(PVector p) {
                return Vadd(p, o);
            }
        };
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
    static ArrayList<PVector> fillTriangle(final PVector entry, final int rot, int n) {
        return transform(fillTriangle(n), new Transform() {
                public PVector transform(PVector p) {
                    return Vadd(Vrot(p, -rot * PI / 3.), axialToXy(entry));
                }
            });
    }
    
    // Get the exit point for a triangle fill
    static PVector exitPointForFill(PVector entry, int rot) {
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
            entry = exitPointForFill(entry, rot);
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
        final PVector[] entries = {V(1, 0), V(0, 1), V(0, 0)};
        ArrayList<PVector> points = new ArrayList<PVector>();
        for (int i = 0; i < 3; i++) {
            points.addAll(transform(fillFan(2*i+1, 4, n), translate(axialToXy(entries[i]))));
        }
        points.addAll(fillTriangle(V(0, 0), 0, n));
        return transform(points, translate(axialToXy(V(-1/3., -1/3.))));
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
    static PVector xyToScreen(PVector p, int width, int height, double span, boolean horizSpan) {
        double scale = span / (horizSpan ? width : height);
        PVector U = V(1. / scale, 0);
        PVector V = V(0, -1. / scale);
        PVector offset = V(.5 * width, .5 * height);
        return Vadd(basisTransform(p, U, V), offset);
    }
    
    // For sampling from a rendered screen. Convert led positions from world coordinates to screen pixels and register with
    // the fadecandy(ies).
    static void registerScreenSamples(OPC opc, ArrayList<PVector> points, int width, int height, double span, boolean horizSpan) {
        for (int ix = 0; ix < points.size(); ix++) {
            PVector px = xyToScreen(points.get(ix), width, height, span, horizSpan);
            opc.led(ix, (int)px.x, (int)px.y);
        }
    }
    
    // Return the adjacent axial coordinate moving from 'p' in direction 'rot'
    static PVector axialNeighbor(PVector p, int rot) {
        int axis = MathUtil.mod(rot, 3);
        boolean hemi = (MathUtil.mod(rot, 6) < 3);
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
    
}