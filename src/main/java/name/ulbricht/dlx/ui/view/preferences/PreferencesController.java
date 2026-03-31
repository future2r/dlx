package name.ulbricht.dlx.ui.view.preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import name.ulbricht.dlx.config.MemorySize;
import name.ulbricht.dlx.config.ProcessorSpeed;
import name.ulbricht.dlx.ui.scene.Theme;

/// Controller for the application preferences.
public final class PreferencesController {

    @FXML
    private DialogPane preferencesRoot;

    @FXML
    private ButtonType restoreDefaultsButtonType;

    @FXML
    private PreferencesViewModel viewModel;

    @FXML
    private ChoiceBox<ProcessorSpeed> processorSpeedChoiceBox;
    @FXML
    private ChoiceBox<MemorySize> memorySizeChoiceBox;
    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    /// Creates a new instance.
    public PreferencesController() {
    }

    @FXML
    private void initialize() {

        this.preferencesRoot.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, this::handleOk);

        final var restoreDefaultsButton = this.preferencesRoot.lookupButton(this.restoreDefaultsButtonType);
        ButtonBar.setButtonUniformSize(restoreDefaultsButton, false);
        restoreDefaultsButton.addEventFilter(ActionEvent.ACTION, this::handleRestoreDefaults);

        this.processorSpeedChoiceBox.itemsProperty().bind(this.viewModel.processorSpeedsProperty());
        this.processorSpeedChoiceBox.valueProperty().bindBidirectional(this.viewModel.selectedProcessorSpeedProperty());

        this.memorySizeChoiceBox.itemsProperty().bind(this.viewModel.memorySizesProperty());
        this.memorySizeChoiceBox.valueProperty().bindBidirectional(this.viewModel.selectedMemorySizeProperty());

        this.themeChoiceBox.itemsProperty().bind(this.viewModel.themesProperty());
        this.themeChoiceBox.valueProperty().bindBidirectional(this.viewModel.selectedThemeProperty());
    }

    private void handleOk(@SuppressWarnings("unused") final ActionEvent event) {
        this.viewModel.savePreferences();
    }

    private void handleRestoreDefaults(final ActionEvent event) {
        this.viewModel.restoreDefaults();
        event.consume();
    }
}
