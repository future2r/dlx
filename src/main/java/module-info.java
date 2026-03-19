/// DLX processor simulator module.
module name.ulbricht.dlx {

    requires javafx.controls;
    requires javafx.fxml;

    opens name.ulbricht.dlx.ui to javafx.graphics;
    opens name.ulbricht.dlx.ui.view.console to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.editor to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.main to javafx.fxml;
    opens name.ulbricht.dlx.ui.view.internals to javafx.fxml;

    exports name.ulbricht.dlx;

    opens name.ulbricht.dlx.ui.icon;
    opens name.ulbricht.dlx.ui.image;
}
