/// DLX processor simulator module.
module name.ulbricht.dlx {

    // Required modules
    requires javafx.controls;
    requires javafx.fxml;

    // Open UI packages to JavaFX
    opens name.ulbricht.dlx.ui to javafx.graphics;
    opens name.ulbricht.dlx.ui.view.console to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.editor to javafx.base, javafx.fxml;
    opens name.ulbricht.dlx.ui.view.main to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.internals to javafx.base, javafx.fxml;

    // Open resource packages for resource loading
    opens name.ulbricht.dlx.ui.css;
    opens name.ulbricht.dlx.ui.icon;
    opens name.ulbricht.dlx.ui.image;

    // Public application API (for the entry point)
    exports name.ulbricht.dlx;
}
