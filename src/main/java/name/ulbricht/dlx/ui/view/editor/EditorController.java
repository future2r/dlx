package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import name.ulbricht.dlx.util.TextPosition;

/// Controller for the editor view.
public final class EditorController {

    @FXML
    private Parent editorRoot;

    @FXML
    private EditorViewModel viewModel;

    @FXML
    private CodeArea sourceCodeArea;
    private final CodeTextModel codeModel;

    private boolean updatingFromModel;
    private boolean updatingFromView;

    private final ReadOnlyObjectWrapper<TextPosition> editPosition = new ReadOnlyObjectWrapper<>();

    /// Creates a new editor controller instance.
    public EditorController() {
        this.codeModel = new CodeTextModel();
        this.codeModel.setDecorator(new EditorSyntaxDecorator());
        this.codeModel.addListener(_ -> editedSourceChanged());
    }

    @FXML
    private void initialize() {
        // Apply the syntax model to the code area
        this.sourceCodeArea.setModel(this.codeModel);

        // Bind the current text position to the caret position of the code area
        this.editPosition.bind(
                this.sourceCodeArea.caretPositionProperty().map(pos -> new TextPosition(pos.index(), pos.offset()))
                        .orElse(new TextPosition(0, 0)));

        // React on changes of the view model
        this.viewModel.sourceProperty().subscribe(this::viewModelSourceChanged);
    }

    /// {@return the view model of this controller}
    public EditorViewModel getViewModel() {
        return this.viewModel;
    }

    /// {@return the root node of the editor view}
    Parent getRoot() {
        return this.editorRoot;
    }

    void requestFocus() {
        this.sourceCodeArea.requestFocus();
        this.sourceCodeArea.moveDocumentStart();
    }

    ReadOnlyObjectProperty<TextPosition> editPositionProperty() {
        return this.editPosition.getReadOnlyProperty();
    }

    TextPosition getEditPosition() {
        return editPositionProperty().get();
    }

    void showEditPosition(final TextPosition position) {
        requireNonNull(position);

        this.sourceCodeArea.requestFocus();
        this.sourceCodeArea.moveDocumentStart();

        final var line = position.line();
        for (var i = 0; i < line; i++)
            this.sourceCodeArea.moveParagraphDown();

        final var column = position.column();
        for (var i = 0; i < column; i++)
            this.sourceCodeArea.moveRight();

    }

    /// Updates the view model when the user edits the source code.
    private void editedSourceChanged() {
        if (!this.updatingFromModel) {
            this.updatingFromView = true;
            try {
                final var source = this.sourceCodeArea.getText();
                this.viewModel.setSource(source);
                this.viewModel.markDirty();
            } finally {
                this.updatingFromView = false;
            }
        }
    }

    /// Updates the code area when the view model's source code changes.
    private void viewModelSourceChanged() {
        if (!this.updatingFromView) {
            this.updatingFromModel = true;
            try {
                final var source = this.viewModel.getSource();
                this.sourceCodeArea.setText(source);
            } finally {
                this.updatingFromModel = false;
            }
        }
    }
}
