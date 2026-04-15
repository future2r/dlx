package name.ulbricht.dlx.ui.view.reference;

import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;

/// Custom tree cell for the reference tree view. Displays the label of a
/// [ReferenceItem] and applies the `group` pseudo-class to category and group
/// nodes so they can be styled via CSS.
final class ReferenceTreeCell extends TreeCell<ReferenceItem> {

    private static final PseudoClass GROUP = PseudoClass.getPseudoClass("group");

    @Override
    protected void updateItem(final ReferenceItem item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            pseudoClassStateChanged(GROUP, false);
        } else {
            setText(item.label());
            final var isGroup = switch (item) {
                case final ReferenceItem.TopCategory _,final ReferenceItem.InstructionGroup _,final ReferenceItem.DirectiveGroup _ ->
                    true;
                default -> false;
            };
            pseudoClassStateChanged(GROUP, isGroup);
        }
    }
}
