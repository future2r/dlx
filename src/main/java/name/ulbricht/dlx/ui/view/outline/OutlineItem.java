package name.ulbricht.dlx.ui.view.outline;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import name.ulbricht.dlx.asm.parser.ImmediateOperand;
import name.ulbricht.dlx.asm.parser.LabelImmediateOperand;
import name.ulbricht.dlx.asm.parser.LabelMemoryOperand;
import name.ulbricht.dlx.asm.parser.LabelOperand;
import name.ulbricht.dlx.asm.parser.MemoryOperand;
import name.ulbricht.dlx.asm.parser.Operand;
import name.ulbricht.dlx.asm.parser.ParsedDataDeclaration;
import name.ulbricht.dlx.asm.parser.ParsedInstruction;
import name.ulbricht.dlx.asm.parser.RegisterOperand;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.util.TextPosition;

/// Represents an item in the outline view.
public final class OutlineItem {

    static OutlineItem dataSection() {
        return new OutlineItem(null, Messages.getString("outline.data_section"), null, null);
    }

    static OutlineItem codeSection() {
        return new OutlineItem(null, Messages.getString("outline.code_section"), null, null);
    }

    static OutlineItem data(final ParsedDataDeclaration data) {
        requireNonNull(data);

        return new OutlineItem(data.pos(), data.label(), data.directive(), formatValues(data.values()));
    }

    private static String formatValues(final List<Object> values) {
        return values.stream()
                .map(value -> switch (value) {
                    case final Number n -> n.toString();
                    case final String s -> String.format("\"%s\"", s);
                    default -> throw new IllegalStateException("Unexpected value type: " + value.getClass().getName());
                })
                .collect(Collectors.joining(", "));
    }

    static OutlineItem instruction(final ParsedInstruction instruction) {
        requireNonNull(instruction);

        return new OutlineItem(instruction.pos(), instruction.label(), instruction.opcode(),
                formatOperands(instruction.operands()));
    }

    private static String formatOperands(final List<Operand> operands) {
        return operands.stream()
                .map(operand -> switch (operand) {
                    case final RegisterOperand reg -> "R" + reg.number();
                    case final ImmediateOperand imm -> Integer.toString(imm.value());
                    case final LabelImmediateOperand lbl -> lbl.name();
                    case final LabelOperand lbl -> lbl.name();
                    case final MemoryOperand mem ->
                        "%s(R%s)".formatted(Integer.toString(mem.offset()), Integer.toString(mem.baseReg()));
                    case final LabelMemoryOperand lblmem ->
                        "%s(R%s)".formatted(lblmem.offsetLabel(), Integer.toString(lblmem.baseReg()));
                })
                .collect(Collectors.joining(", "));
    }

    private final ReadOnlyObjectWrapper<TextPosition> textPosition = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper label = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper keyword = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper operands = new ReadOnlyStringWrapper();

    private OutlineItem(final TextPosition textPosition, final String label, final String keyword,
            final String operands) {
        this.textPosition.set(textPosition);
        this.label.set(label);
        this.keyword.set(keyword);
        this.operands.set(operands);
    }

    /// {@return a read-only property for the text position}
    public ReadOnlyObjectProperty<TextPosition> textPositionProperty() {
        return this.textPosition.getReadOnlyProperty();
    }

    /// {@return the text position}
    public TextPosition getTextPosition() {
        return textPositionProperty().get();
    }

    /// {@return a read-only property for the label}
    public ReadOnlyStringProperty labelProperty() {
        return this.label.getReadOnlyProperty();
    }

    /// {@return the label}
    public String getLabel() {
        return labelProperty().get();
    }

    /// {@return a read-only property for the keyword}
    public ReadOnlyStringProperty keywordProperty() {
        return this.keyword.getReadOnlyProperty();
    }

    /// {@return the keyword}
    public String getKeyword() {
        return keywordProperty().get();
    }

    /// {@return a read-only property for the operands}
    public ReadOnlyStringProperty operandsProperty() {
        return this.operands.getReadOnlyProperty();
    }

    /// {@return the operands}
    public String getOperands() {
        return operandsProperty().get();
    }
}
