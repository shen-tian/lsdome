public static class LayoutUtil {

  static float SQRT_3 = (float)Math.sqrt(3.);

  // Evenly fill a triangle with a grid of points of size n. The triangle filled is an equilateral triangle
  // with points at (0, 0), (1, 0), and (.5, sqrt(3)/2). Points are placed such that spacing between two
  // adjacent points will match the spacing between an edge point and the opposing point of a neighboring
  // triangle. Returns a list of points traversed in a boustrophedon manner, starting near the origin,
  // proceeding left/right, then upward. The point near (0, 0) will thus be known as the 'entry' point, and
  // the top-most point as the 'exit' point.
  static ArrayList<PVector> fillTriangle(int n) {
    float spacing = 1. / (n - 1 + SQRT_3);
    PVector offset = new PVector(spacing * .5 * SQRT_3, spacing * .5);

    ArrayList<PVector> points = new ArrayList<PVector>();
    for (int row = 0; row < n; row++) {
      boolean reversed = (row % 2 == 1);
      int width = n - row;
      for (int col = 0; col < width; col++) {
        int c = (reversed ? width - 1 - col : col);
        PVector p = PVector.add(axialToXy(PVector.mult(new PVector(row, c), spacing)), offset);
        points.add(p);
      }
    }
    return points;
  }

  // Compute a basis transformation for vector p, where u is the transformation result of basis vector U (1, 0),
  // and v is the transformation of basis V (0, 1)
  static PVector basisTransform(PVector p, PVector u, PVector v) {
    return new PVector(u.x * p.x + v.x * p.y, u.y * p.x + v.y * p.y);
  }

  // Convert a 2-vector of (U, V) coordinates from the axial coordinate scheme into (x, y) cartesian coordinates
  static PVector axialToXy(PVector p) {
    PVector U = new PVector(.5, .5 * SQRT_3);
    PVector V = new PVector(1., 0.);
    return basisTransform(p, U, V);
  }

  // Convert (x, y) coordinate p to screen pixel coordinates where top-left is pixel (0, 0) and bottom-right is
  // pixel (width, height). 'span' is the size of the viewport in world coordinates, where size means width if horizSpan is
  // true and height if horizSpan is false. World origin is in the center of the viewport.
  static PVector xyToScreen(PVector p, int width, int height, float span, boolean horizSpan) {
    float scale = span / (horizSpan ? width : height);
    PVector U = new PVector(1. / scale, 0);
    PVector V = new PVector(0, -1. / scale);
    PVector offset = new PVector(.5 * width, .5 * height);
    return PVector.add(basisTransform(p, U, V), offset);
  }

  // For sampling from a rendered screen. Convert led positions from world coordinates to screen pixels and register with
  // the fadecandy(ies).
  // TODO: properly handle when pixel coordinates exceed the viewport
  static void registerScreenSamples(OPC opc, ArrayList<PVector> points, int width, int height, float span, boolean horizSpan) {
    for (int ix = 0; ix < points.size(); ix++) {
      PVector px = xyToScreen(points.get(ix), width, height, span, horizSpan);
      opc.led(ix, (int)px.x, (int)px.y);
    }
  }

}