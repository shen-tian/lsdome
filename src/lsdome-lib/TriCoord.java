import java.util.*;
import processing.core.*;

// Representation of a lattice (integer) coordinate within a 3-axis triangular coordinate system.
// Unlike a two-axis x/y coordinate system, a triangular lattice has points along 3 axes. It is
// still 2-dimensional, as moving along one axis has effects on the other two.
//
// We call the 3 axes U, V, and W. The value for U increases in the vertical direction. Values for V
// and W increase in the directions rotated clockwise from U 120 and 240 degrees, respectively.
//
// The values for u, v, and w will always sum to the same amount. What this amount is varies
// depending on the context in which the coordinates are used. Also, things are slightly more
// complicated for us in that the dome uses two differently oriented triangular grids due to the
// tesselation of the panels, so the magic number will be one of two values, and the sum tells us
// the grid orientation.
//
// In theory u, v, and w can be real-valued, but for our purposes we truncate them to ints.
// Specifically, we take the floor of the value. That means there is no pixel (0, 0, 0), because
// positive increase in the W direction is on the opposite side of the origin from u=0 v=0. Thus
// that first pixel is actually (0, 0, -1).
//
// We use these coordinates in 3 different contexts:
//
// UNIVERSAL
// An abstract plane where all pixels comprise a continuous, infinite grid. This still works despite
// our oscillating grid orientation, as the third axis provides enough information to disambiguate.
// (With a pure triangular grid, you could always derive the 3rd axis from the first two). Axes sum
// to -1 for upright orientation, and -2 when flipped.
//
// PANEL
// Essential similar to universal, but referring to actual panels in the grid rather than pixels.
// For panels of size 1, the systems are equivalent.
//
// PIXEL
// For tracking pixels within a single panel. Each axes will be valued [0, panel size). In normal
// orientation, axes align with the panel edges; all axes sum to <panel size> - 1. In flipped
// orientation, axes graze the corners; they will sum to 2*(<panel size> - 1).
//
// Coordinates can be converted to xy cartesian coordinates in LayoutUtil.

enum CoordType {
    UNIVERSAL,
    PANEL,
    PIXEL
}

enum Axis {
    U,
    V,
    W
}

enum PanelOrientation {
    A,  // Horizontal edge on bottom
    B   // Horizontal edge on top
}

public class TriCoord {

    static final Axis[] axes = {Axis.U, Axis.V, Axis.W};
    static final PanelOrientation[] orientations = {PanelOrientation.A, PanelOrientation.B};

    CoordType type;
    int u;
    int v;
    int w;
    int panel_length;

    public TriCoord(CoordType type, int u, int v, int w) {
        this(type, u, v, w, 0);
    }

    public TriCoord(CoordType type, int u, int v, int w, int panel_length) {
        this.type = type;
        this.u = u;
        this.v = v;
        this.w = w;

        assert panel_length >= 0;
        assert (type == CoordType.PIXEL) == (panel_length > 0) : "panel length iff coord type 'pixel'";
        this.panel_length = panel_length;

        if (type == CoordType.PIXEL) {
            assert u >= 0 && u < panel_length;
            assert v >= 0 && v < panel_length;
            assert w >= 0 && w < panel_length;
        }

        int sum = u + v + w;
        assert sum == checksum(PanelOrientation.A) || sum == checksum(PanelOrientation.B) : String.format("%d %d %d", u, v, w);
    }

    // Build a coordinate from just two of the axes and the orientation.
    static TriCoord fromParts(CoordType type, Axis axis1, int val1, Axis axis2, int val2, PanelOrientation o, int panel_length) {
        assert axis1 != axis2;

        int[] vals = new int[3];
        boolean[] have = new boolean[3];

        vals[axis1.ordinal()] = val1;
        have[axis1.ordinal()] = true;
        vals[axis2.ordinal()] = val2;
        have[axis2.ordinal()] = true;

        for (int i = 0; i < 3; i++) {
            if (!have[i]) {
                vals[i] = checksum(o, panel_length) - val1 - val2;
            }
        }
        return new TriCoord(type, vals[0], vals[1], vals[2], panel_length);
    }

    // Expected sum of the values u, v, w.
    static int checksum(PanelOrientation o, int panel_length) {
        if (o == PanelOrientation.A) {
            return panel_length - 1;
        } else if (o == PanelOrientation.B) {
            return 2 * (panel_length - 1);
        } else {
            throw new IllegalArgumentException();
        }
    }

    int checksum(PanelOrientation o) {
        return checksum(o, panel_length);
    }

    // Derive the orientation from the coordinate.
    PanelOrientation getOrientation() {
        int sum = u + v + w;
        if (sum == checksum(PanelOrientation.A)) {
            return PanelOrientation.A;
        } else if (sum == checksum(PanelOrientation.B)) {
            return PanelOrientation.B;
        } else {
            throw new RuntimeException();
        }
    }

    // Rotate the coordinate clockwise by rot * 60 degrees.
    TriCoord rotate(int rot) {
        boolean invert = (MathUtil.mod(rot, 2) == 1);
        if (invert) {
            rot -= 3;
        }
        int axis_shift = MathUtil.mod(rot, 6) / 2;

        Axis newAx1 = axes[(Axis.U.ordinal() + axis_shift) % 3];
        Axis newAx2 = axes[(Axis.V.ordinal() + axis_shift) % 3];
        int newVal1 = (invert ? panel_length - 1 - u : u);
        int newVal2 = (invert ? panel_length - 1 - v : v);
        PanelOrientation newO = orientations[(getOrientation().ordinal() + (invert ? 1 : 0)) % 2];
        return TriCoord.fromParts(type, newAx1, newVal1, newAx2, newVal2, newO, panel_length);
    }

    // Flip about an axis.
    TriCoord flip(Axis axis) {
        switch (axis) {
        case U: return TriCoord.fromParts(type, Axis.U, u, Axis.V, w, getOrientation(), panel_length);
        case V: return TriCoord.fromParts(type, Axis.V, v, Axis.W, u, getOrientation(), panel_length);
        case W: return TriCoord.fromParts(type, Axis.W, w, Axis.U, v, getOrientation(), panel_length);
        default: throw new RuntimeException();
        }
    }

    static TriCoord toUniversal(TriCoord panel, TriCoord pixel) {
        assert panel.type == CoordType.PANEL;
        assert pixel.type == CoordType.PIXEL;
        return new TriCoord(CoordType.UNIVERSAL,
                            pixel.panel_length * panel.u + pixel.u,
                            pixel.panel_length * panel.v + pixel.v,
                            pixel.panel_length * panel.w + pixel.w);
    }

    static TriCoord toPanel(TriCoord uni, int panel_length) {
        assert uni.type == CoordType.UNIVERSAL;
        return new TriCoord(CoordType.PANEL,
                            (int)Math.floor(uni.u / (double)panel_length),
                            (int)Math.floor(uni.v / (double)panel_length),
                            (int)Math.floor(uni.w / (double)panel_length));
    }

    static TriCoord toPixel(TriCoord uni, int panel_length) {
        assert uni.type == CoordType.UNIVERSAL;
        return new TriCoord(CoordType.PIXEL,
                            MathUtil.mod(uni.u, panel_length),
                            MathUtil.mod(uni.v, panel_length),
                            MathUtil.mod(uni.w, panel_length));
    }

    PVector toV() {
        return LayoutUtil.V(u, v);
    }

    int getAxis(Axis ax) {
        switch (ax) {
        case U: return u;
        case V: return v;
        case W: return w;
        default: throw new RuntimeException();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof TriCoord) {
            TriCoord tc = (TriCoord)o;
            return type == tc.type && u == tc.u && v == tc.v && w == tc.w;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(type, u, v, w);
    }

    public String toString() {
        return String.format("<u:%d v:%d w:%d o:%s>", u, v, w, (getOrientation() == PanelOrientation.A ? "A" : "B"));
    }

}