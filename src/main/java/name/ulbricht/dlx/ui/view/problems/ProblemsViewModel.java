package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.TreeItem;
import javafx.util.Subscription;

/// View model for the problems view. Manages a tree of problem items grouped by
/// source origin.
public final class ProblemsViewModel {

    private final TreeItem<ProblemItem> root = new TreeItem<>();
    private final ReadOnlyIntegerWrapper totalProblemsCount = new ReadOnlyIntegerWrapper();
    private final Map<UUID, SourceBinding> sourceBindings = new LinkedHashMap<>();

    /// Creates a new problems view model instance.
    public ProblemsViewModel() {
    }

    /// {@return the invisible root tree item}
    public TreeItem<ProblemItem> getRoot() {
        return this.root;
    }

    /// {@return a read-only property representing the total number of diagnostics
    /// across all source origins}
    public ReadOnlyIntegerProperty totalProblemsCountProperty() {
        return this.totalProblemsCount.getReadOnlyProperty();
    }

    /// {@return the total number of diagnostics across all source origins}
    public int getTotalProblemsCount() {
        return totalProblemsCountProperty().get();
    }

    /// Adds a source origin to be tracked. Creates an origin node and subscribes to
    /// its diagnostics.
    ///
    /// @param source the source origin to add
    void addSource(final SourceOrigin source) {
        requireNonNull(source);
        final var id = source.id();
        if (this.sourceBindings.containsKey(id))
            return;

        final var originItem = new SourceOriginItem(source);
        final var treeItem = new TreeItem<ProblemItem>(originItem);
        treeItem.setExpanded(true);

        final var binding = new SourceBinding(source, originItem, treeItem);
        this.sourceBindings.put(id, binding);

        // Subscribe to diagnostics property and list changes
        binding.diagnosticsSubscription = source.diagnosticsProperty()
                .subscribe((_, newList) -> {
                    if (binding.listSubscription != null)
                        binding.listSubscription.unsubscribe();

                    rebuildChildren(binding);

                    if (newList != null)
                        binding.listSubscription = newList.subscribe(() -> rebuildChildren(binding));
                });
    }

    /// Removes a source origin from tracking. Unsubscribes from its diagnostics and
    /// removes the origin node.
    ///
    /// @param source the source origin to remove
    void removeSource(final SourceOrigin source) {
        requireNonNull(source);
        final var binding = this.sourceBindings.remove(source.id());
        if (binding != null) {
            disposeBinding(binding);
            this.root.getChildren().remove(binding.treeItem);
            updateTotalCount();
        }
    }

    /// Disposes of the view model and releases all resources.
    void dispose() {
        for (final var binding : this.sourceBindings.values()) {
            disposeBinding(binding);
        }
        this.sourceBindings.clear();
        this.root.getChildren().clear();
        this.totalProblemsCount.set(0);
    }

    private void rebuildChildren(final SourceBinding binding) {
        final var children = binding.treeItem.getChildren();
        children.clear();

        final var list = binding.source.diagnosticsProperty().get();
        if (list != null && !list.isEmpty()) {
            children.addAll(list.stream()
                    .map(diagnostic -> new DiagnosticItem(diagnostic, binding.source))
                    .map(TreeItem<ProblemItem>::new)
                    .toList());
        }

        updateOriginVisibility(binding);
        updateTotalCount();
    }

    private void updateOriginVisibility(final SourceBinding binding) {
        final var hasChildren = !binding.treeItem.getChildren().isEmpty();
        final var isInRoot = this.root.getChildren().contains(binding.treeItem);

        if (hasChildren && !isInRoot) {
            this.root.getChildren().add(binding.treeItem);
        } else if (!hasChildren && isInRoot) {
            this.root.getChildren().remove(binding.treeItem);
        }
    }

    private void updateTotalCount() {
        final var count = this.sourceBindings.values().stream()
                .mapToInt(binding -> binding.treeItem.getChildren().size())
                .sum();
        this.totalProblemsCount.set(count);
    }

    private static void disposeBinding(final SourceBinding binding) {
        if (binding.listSubscription != null)
            binding.listSubscription.unsubscribe();
        if (binding.diagnosticsSubscription != null)
            binding.diagnosticsSubscription.unsubscribe();
        binding.originItem.dispose();
    }

    private static final class SourceBinding {

        final SourceOrigin source;
        final SourceOriginItem originItem;
        final TreeItem<ProblemItem> treeItem;
        Subscription diagnosticsSubscription;
        Subscription listSubscription;

        SourceBinding(final SourceOrigin source, final SourceOriginItem originItem,
                final TreeItem<ProblemItem> treeItem) {
            this.source = requireNonNull(source);
            this.originItem = requireNonNull(originItem);
            this.treeItem = requireNonNull(treeItem);
        }
    }
}
