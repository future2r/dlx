package name.ulbricht.dlx.ui.view.registers;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.util.Subscription;
import name.ulbricht.dlx.simulator.Access;

/// A table row representing a register, with visual feedback for read and
/// write accesses.
public final class RegisterTableRow extends TableRow<RegisterItem> {

    private static final PseudoClass READ_PSEUDO_CLASS = PseudoClass.getPseudoClass("read");
    private static final PseudoClass WRITE_PSEUDO_CLASS = PseudoClass.getPseudoClass("write");

    private Subscription accessSubscription = Subscription.EMPTY;

    /// Creates a new register table row instance.
    RegisterTableRow() {
    }

    @Override
    protected void updateItem(final RegisterItem item, final boolean empty) {
        super.updateItem(item, empty);

        // Remove the old subscription from any old row item
        this.accessSubscription.unsubscribe();

        // Subscribe for changes of the "access" property of the row item
        if (!empty && item != null) {
            this.accessSubscription = item.accessProperty().subscribe(this::accessChanged);
        }

        accessChanged();
    }

    private void accessChanged() {
        final var item = getItem();
        final var access = !isEmpty() && item != null ? item.getAccess() : null;

        pseudoClassStateChanged(READ_PSEUDO_CLASS, access == Access.READ);
        pseudoClassStateChanged(WRITE_PSEUDO_CLASS, access == Access.WRITE);
    }
}
