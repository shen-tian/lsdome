public interface Config {

    // Debug mode.
    static final boolean DEBUG = true;

    // Fadecandy server.
    static final String FADECANDY_HOST = "127.0.0.1";
    //static final String FADECANDY_HOST = "192.168.1.135";

    // Size of single panel's pixel grid.
    static final int PANEL_SIZE = 15;

    // Panel configuration.
    static final PanelLayout PANEL_LAYOUT = PanelLayout._2;
    //static final PanelLayout PANEL_LAYOUT = PanelLayout._13;
    //static final PanelLayout PANEL_LAYOUT = PanelLayout._24;

}
