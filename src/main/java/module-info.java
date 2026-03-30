/// DLX processor simulator module.
module name.ulbricht.dlx {

    // Required JDK modules
    requires java.prefs;

    // Required modules
    requires javafx.controls;
    requires javafx.fxml;

    // Required incubator modules for syntax highlighting
    requires jfx.incubator.input;
    requires jfx.incubator.richtext;

    // Open UI packages to JavaFX
    opens name.ulbricht.dlx.ui to javafx.graphics;
    opens name.ulbricht.dlx.ui.scene.layout to javafx.fxml;
    opens name.ulbricht.dlx.ui.view to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.editor to javafx.base, javafx.fxml;
    opens name.ulbricht.dlx.ui.view.main to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.memory to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.outline to javafx.base, javafx.fxml;
    opens name.ulbricht.dlx.ui.view.preferences to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.problems to javafx.base, javafx.fxml;
    opens name.ulbricht.dlx.ui.view.registers to javafx.base, javafx.fxml;

    // Open resource packages for resource loading
    opens name.ulbricht.dlx.ui.css;
    opens name.ulbricht.dlx.ui.icon;
    opens name.ulbricht.dlx.ui.image;

    // Public application API (for the entry point)
    exports name.ulbricht.dlx;
}
