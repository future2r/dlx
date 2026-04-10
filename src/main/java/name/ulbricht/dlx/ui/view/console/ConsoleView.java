package name.ulbricht.dlx.ui.view.console;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Parent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for displaying program console output from trap instructions.
public final class ConsoleView implements View<Parent, ConsoleViewModel> {

    /// {@return the loaded console view}
    public static ConsoleView load() {
        return new ConsoleView(Views.loadController(ConsoleView.class));
    }

    private final ConsoleController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("console.title"));

    private ConsoleView(final ConsoleController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public void dispose() {
        this.controller.dispose();
        this.controller.getViewModel().dispose();
    }
}
