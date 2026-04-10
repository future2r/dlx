package name.ulbricht.dlx.ui.view.problems;

import javafx.scene.control.TreeCell;
import name.ulbricht.dlx.ui.i18n.Messages;

/// A tree cell that displays problem items with formatted text. Source group
/// items are displayed as "filename (count)" and diagnostic items are displayed
/// as "\[stage\] message (Ln X, Col Y)".
final class ProblemTreeCell extends TreeCell<ProblemItem> {

    /// Creates a new instance.
    ProblemTreeCell() {
    }

    @Override
    protected void updateItem(final ProblemItem item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            switch (item) {
                case final SourceOriginItem group -> setText(
                        Messages.getString("problems.sourceGroup.pattern").formatted(
                                group.getName(),
                                Integer.valueOf(group.getDiagnosticCount())));
                case final DiagnosticItem diag -> setText(
                        Messages.getString("problems.diagnostic.pattern").formatted(
                                Messages.getString("problems.source." + diag.stage().name().toLowerCase()),
                                diag.message(),
                                Messages.getString("problems.textPosition.pattern").formatted(
                                        Integer.valueOf(diag.textPosition().displayLine()),
                                        Integer.valueOf(diag.textPosition().displayColumn()))));
            }
        }
    }
}
