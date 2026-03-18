/// DLX processor simulator module.
module name.ulbricht.dlx {

    requires javafx.controls;
    requires javafx.fxml;

    opens name.ulbricht.dlx.ui to javafx.graphics;
    opens name.ulbricht.dlx.ui.view.main to javafx.fxml;

    exports name.ulbricht.dlx;
}
