import java.util.*;
import processing.core.*;

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
        assert sum == checksum(PanelOrientation.A) || sum == checksum(PanelOrientation.B);
    }

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