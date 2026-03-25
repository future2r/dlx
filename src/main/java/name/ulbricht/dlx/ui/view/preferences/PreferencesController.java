package name.ulbricht.dlx.ui.view.preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import name.ulbricht.dlx.ui.scene.Theme;

/// Controller for the application preferences.
public final class PreferencesController {

    @FXML
    private DialogPane preferencesRoot;

    @FXML
    private PreferencesViewModel viewModel;

    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    /// Creates a new instance.
    public PreferencesController() {
    }

    @FXML
    private void initialize() {
        this.preferencesRoot.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, this::onOk);

        this.themeChoiceBox.itemsProperty().bind(this.viewModel.themesProperty());
        this.themeChoiceBox.valueProperty().bindBidirectional(this.viewModel.selectedThemeProperty());
    }

    private void onOk(@SuppressWarnings("unused") final ActionEvent event) {
        this.viewModel.savePreferences();
    }
}
