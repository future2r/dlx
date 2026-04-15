package name.ulbricht.dlx.ui.view.reference;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import name.ulbricht.dlx.asm.Directive;
import name.ulbricht.dlx.asm.Instruction;
import name.ulbricht.dlx.ui.i18n.Messages;

/// View model for the reference view. Holds the filter text, builds the
/// reference tree, and computes the detail text for the currently selected item.
public final class ReferenceViewModel {

    private final StringProperty filterText = new SimpleStringProperty("");
    private final ObservableList<TreeItem<ReferenceItem>> modifiableTreeItems = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<TreeItem<ReferenceItem>> treeItems = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableTreeItems));
    private final ObjectProperty<TreeItem<ReferenceItem>> selectedItem = new SimpleObjectProperty<>();
    private final ReadOnlyStringWrapper detailText = new ReadOnlyStringWrapper("");

    /// Creates a new reference view model instance.
    public ReferenceViewModel() {
        this.filterText.subscribe(this::rebuildTreeItems);
        this.selectedItem.subscribe(item -> this.detailText.set(formatDetail(item)));
        rebuildTreeItems(this.filterText.get());
    }

    /// {@return the filter text property bound to the search field}
    public StringProperty filterTextProperty() {
        return this.filterText;
    }

    /// {@return the current filter text}
    public String getFilterText() {
        return this.filterText.get();
    }

    /// {@return a read-only property with the tree items for the reference tree}
    public ReadOnlyListProperty<TreeItem<ReferenceItem>> treeItemsProperty() {
        return this.treeItems.getReadOnlyProperty();
    }

    /// {@return the current tree items}
    public ObservableList<TreeItem<ReferenceItem>> getTreeItems() {
        return this.treeItems.getReadOnlyProperty();
    }

    /// {@return the selected item property}
    public ObjectProperty<TreeItem<ReferenceItem>> selectedItemProperty() {
        return this.selectedItem;
    }

    /// {@return a read-only property with the formatted detail text for the
    /// currently selected item}
    public ReadOnlyStringProperty detailTextProperty() {
        return this.detailText.getReadOnlyProperty();
    }

    private void rebuildTreeItems(final String filter) {
        final var normalizedFilter = filter == null ? "" : filter.strip().toLowerCase();

        this.modifiableTreeItems.clear();

        final var instructionsNode = buildInstructionsNode(normalizedFilter);
        if (instructionsNode != null) {
            this.modifiableTreeItems.add(instructionsNode);
        }

        final var directivesNode = buildDirectivesNode(normalizedFilter);
        if (directivesNode != null) {
            this.modifiableTreeItems.add(directivesNode);
        }

        final var registersNode = buildRegistersNode(normalizedFilter);
        if (registersNode != null) {
            this.modifiableTreeItems.add(registersNode);
        }
    }

    private static TreeItem<ReferenceItem> buildInstructionsNode(final String filter) {
        final var topItem = new TreeItem<ReferenceItem>(
                new ReferenceItem.TopCategory("reference.category.instructions"));
        topItem.setExpanded(true);

        for (final var category : InstructionCategory.values()) {
            final var groupItem = new TreeItem<ReferenceItem>(
                    new ReferenceItem.InstructionGroup(category));
            groupItem.setExpanded(true);

            for (final var instruction : category.instructions()) {
                if (matchesFilter(instruction, filter)) {
                    groupItem.getChildren().add(new TreeItem<>(
                            new ReferenceItem.InstructionEntry(instruction)));
                }
            }

            if (!groupItem.getChildren().isEmpty()) {
                topItem.getChildren().add(groupItem);
            }
        }

        return topItem.getChildren().isEmpty() ? null : topItem;
    }

    private static TreeItem<ReferenceItem> buildDirectivesNode(final String filter) {
        final var topItem = new TreeItem<ReferenceItem>(
                new ReferenceItem.TopCategory("reference.category.directives"));
        topItem.setExpanded(true);

        for (final var category : DirectiveCategory.values()) {
            final var groupItem = new TreeItem<ReferenceItem>(
                    new ReferenceItem.DirectiveGroup(category));
            groupItem.setExpanded(true);

            for (final var directive : category.directives()) {
                if (matchesFilter(directive, filter)) {
                    groupItem.getChildren().add(new TreeItem<>(
                            new ReferenceItem.DirectiveEntry(directive)));
                }
            }

            if (!groupItem.getChildren().isEmpty()) {
                topItem.getChildren().add(groupItem);
            }
        }

        return topItem.getChildren().isEmpty() ? null : topItem;
    }

    private static TreeItem<ReferenceItem> buildRegistersNode(final String filter) {
        final var topItem = new TreeItem<ReferenceItem>(
                new ReferenceItem.TopCategory("reference.category.registers"));
        topItem.setExpanded(true);

        for (final var register : RegisterConvention.values()) {
            if (matchesFilter(register, filter)) {
                topItem.getChildren().add(new TreeItem<>(
                        new ReferenceItem.RegisterEntry(register)));
            }
        }

        return topItem.getChildren().isEmpty() ? null : topItem;
    }

    private static boolean matchesFilter(final Instruction instruction, final String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        if (instruction.mnemonic().contains(filter)) {
            return true;
        }
        final var description = Messages.getOptionalString("reference.instruction."
                + instruction.mnemonic() + ".description");
        return description.isPresent() && description.get().toLowerCase().contains(filter);
    }

    private static boolean matchesFilter(final Directive directive, final String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        if (directive.directiveName().contains(filter)) {
            return true;
        }
        final var description = Messages.getOptionalString("reference.directive."
                + directive.directiveName() + ".description");
        return description.isPresent() && description.get().toLowerCase().contains(filter);
    }

    private static boolean matchesFilter(final RegisterConvention register, final String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        if (register.displayName().toLowerCase().contains(filter)) {
            return true;
        }
        final var purpose = Messages.getOptionalString("reference.register."
                + register.name().toLowerCase() + ".purpose");
        return purpose.isPresent() && purpose.get().toLowerCase().contains(filter);
    }

    private static String formatDetail(final TreeItem<ReferenceItem> item) {
        if (item == null) {
            return "";
        }
        return switch (item.getValue()) {
            case final ReferenceItem.InstructionEntry entry -> formatInstructionDetail(entry);
            case final ReferenceItem.DirectiveEntry entry -> formatDirectiveDetail(entry);
            case final ReferenceItem.RegisterEntry entry -> formatRegisterDetail(entry);
            case final ReferenceItem.TopCategory _,final ReferenceItem.InstructionGroup _,final ReferenceItem.DirectiveGroup _ ->
                "";
        };
    }

    private static String formatInstructionDetail(final ReferenceItem.InstructionEntry entry) {
        final var instruction = entry.instruction();
        final var prefix = "reference.instruction." + instruction.mnemonic();
        final var sb = new StringBuilder();

        appendSection(sb, "reference.detail.description", prefix + ".description");
        appendSection(sb, "reference.detail.syntax", prefix + ".syntax");
        appendSection(sb, "reference.detail.operation", prefix + ".operation");

        final var formatKey = "reference.operandFormat." + instruction.format().name();
        final var formatLabel = Messages.getOptionalString("reference.detail.encoding");
        final var formatValue = Messages.getOptionalString(formatKey);
        if (formatLabel.isPresent() && formatValue.isPresent()) {
            sb.append(formatLabel.get()).append('\n').append(formatValue.get()).append("\n\n");
        }

        appendSection(sb, "reference.detail.example", prefix + ".example");

        return sb.toString();
    }

    private static String formatDirectiveDetail(final ReferenceItem.DirectiveEntry entry) {
        final var directive = entry.directive();
        final var prefix = "reference.directive." + directive.directiveName();
        final var sb = new StringBuilder();

        appendSection(sb, "reference.detail.description", prefix + ".description");
        appendSection(sb, "reference.detail.example", prefix + ".example");

        return sb.toString();
    }

    private static String formatRegisterDetail(final ReferenceItem.RegisterEntry entry) {
        final var register = entry.register();
        final var prefix = "reference.register." + register.name().toLowerCase();
        final var sb = new StringBuilder();

        appendSection(sb, "reference.detail.purpose", prefix + ".purpose");
        appendSection(sb, "reference.detail.notes", prefix + ".notes");

        return sb.toString();
    }

    private static void appendSection(final StringBuilder sb, final String labelKey,
            final String valueKey) {
        final var label = Messages.getOptionalString(labelKey);
        final var value = Messages.getOptionalString(valueKey);
        if (label.isPresent() && value.isPresent()) {
            sb.append(label.get()).append('\n').append(value.get()).append("\n\n");
        }
    }
}
