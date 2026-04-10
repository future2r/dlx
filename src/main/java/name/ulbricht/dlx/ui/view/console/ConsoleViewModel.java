package name.ulbricht.dlx.ui.view.console;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import name.ulbricht.dlx.service.Console;

/// View model for the Console view.
///
/// Subscribes to [Console] changes and updates a [ReadOnlyStringProperty] on the
/// JavaFX application thread via the provided executor.
public final class ConsoleViewModel {

    private final Executor uiExecutor;
    private final Console console;
    private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper("");
    private final Console.ConsoleListener consoleListener = this::consoleChanged;

    /// Creates a new console view model instance.
    ///
    /// @param uiExecutor the executor for UI-thread updates; must not be `null`
    /// @param console    the console service to observe; must not be `null`
    public ConsoleViewModel(
            @NamedArg("uiExecutor") final Executor uiExecutor,
            @NamedArg("console") final Console console) {
        this.uiExecutor = requireNonNull(uiExecutor);
        this.console = requireNonNull(console);

        this.console.addListener(this.consoleListener);
        // Apply current snapshot so the view shows existing content
        final var snapshot = this.console.snapshot();
        if (!snapshot.isEmpty()) {
            this.text.set(snapshot);
        }
    }

    /// {@return a read-only property containing the full console text}
    public ReadOnlyStringProperty textProperty() {
        return this.text.getReadOnlyProperty();
    }

    /// {@return the current console text}
    public String getText() {
        return textProperty().get();
    }

    /// Clears the console output.
    public void clear() {
        this.console.clear();
    }

    /// Disposes of this view model and removes the console listener.
    void dispose() {
        this.console.removeListener(this.consoleListener);
    }

    private void consoleChanged(final Console.Change change) {
        this.uiExecutor.execute(() -> {
            if (change.cleared()) {
                this.text.set("");
            } else {
                this.text.set(this.text.get() + change.appended());
            }
        });
    }
}
