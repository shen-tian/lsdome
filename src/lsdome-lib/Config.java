// Global configuration variables.

public interface Config {

    // Debug mode.
    static final boolean DEBUG = false;

    // Fadecandy server.
    static final String FADECANDY_HOST = "127.0.0.1";
    //static final String FADECANDY_HOST = "192.168.1.135";

    // Size of single panel's pixel grid.
    static final int PANEL_SIZE = 15;

    // Panel configuration.
    //static final PanelLayout PANEL_LAYOUT = PanelLayout._2;
    static final PanelLayout PANEL_LAYOUT = PanelLayout._13;
    //static final PanelLayout PANEL_LAYOUT = PanelLayout._24;

    // If true, size the panels as if they were part of a larger layout.
    static final boolean PARTIAL_LAYOUT = false;
    // The larger layout. Ignored if PARTIAL_LAYOUT is false.
    static final PanelLayout FULL_PANEL_LAYOUT = PanelLayout._24;
}
