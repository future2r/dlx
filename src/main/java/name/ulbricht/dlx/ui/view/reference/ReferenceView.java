package name.ulbricht.dlx.ui.view.reference;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for browsing the DLX assembler language reference (instructions,
/// directives, and register conventions).
public final class ReferenceView implements View<Parent, ReferenceViewModel> {

    /// {@return a cell factory for the reference tree view}
    public static Callback<TreeView<ReferenceItem>, TreeCell<ReferenceItem>> referenceCellFactory() {
        return _ -> new ReferenceTreeCell();
    }

    /// {@return the configured reference view with the loaded content}
    public static ReferenceView load() {
        return new ReferenceView(Views.loadController(ReferenceView.class));
    }

    private final ReferenceController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(
            Messages.getString("reference.title"));

    private ReferenceView(final ReferenceController controller) {
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
    }
}
