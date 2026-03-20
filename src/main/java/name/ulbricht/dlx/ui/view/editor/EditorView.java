package name.ulbricht.dlx.ui.view.editor;

import java.nio.charset.StandardCharsets;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import name.ulbricht.dlx.compiler.AsciiDataDeclaration;
import name.ulbricht.dlx.compiler.AsciiZDataDeclaration;
import name.ulbricht.dlx.compiler.ByteDataDeclaration;
import name.ulbricht.dlx.compiler.DataDeclaration;
import name.ulbricht.dlx.compiler.HalfWordDataDeclaration;
import name.ulbricht.dlx.compiler.InstructionCall;
import name.ulbricht.dlx.compiler.SpaceDataDeclaration;
import name.ulbricht.dlx.compiler.WordDataDeclaration;
import name.ulbricht.dlx.simulator.ImmediateInstruction;
import name.ulbricht.dlx.simulator.JumpInstruction;
import name.ulbricht.dlx.simulator.RegisterInstruction;
import name.ulbricht.dlx.ui.control.DecimalTableCell;
import name.ulbricht.dlx.ui.control.HexadecimalTableCell;
import name.ulbricht.dlx.ui.util.FormatUtil;

/// View for the editor. Since the view is created via `<fx:include>` in the
/// main view, it does not need a view class. However, we still need a class to
/// provide some view utilities.
public final class EditorView {

    private static final String NOT_APPLICABLE = "-";

    /// {@return a cell factory for the address column}
    public static Callback<TableColumn<?, Integer>, TableCell<?, Integer>> addressCellFactory() {
        return _ -> new HexadecimalTableCell<>();
    }

    /// {@return a cell factory for the size column}
    public static Callback<TableColumn<?, Integer>, TableCell<?, Integer>> sizeCellFactory() {
        return _ -> new DecimalTableCell<>();
    }

    /// {@return a cell factory for the value column}
    public static Callback<TableColumn<DataItem<?>, DataDeclaration>, TableCell<DataItem<?>, DataDeclaration>> valueCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final DataDeclaration item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item) {
                        case final ByteDataDeclaration b -> FormatUtil.decimal(b.value());
                        case final HalfWordDataDeclaration hw -> FormatUtil.decimal(hw.value());
                        case final WordDataDeclaration w -> FormatUtil.decimal(w.value());
                        case final SpaceDataDeclaration _ -> "";
                        case final AsciiDataDeclaration a ->
                            new String(a.characters(), StandardCharsets.US_ASCII);
                        case final AsciiZDataDeclaration az ->
                            new String(az.characters(), StandardCharsets.US_ASCII);
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the raw value column}
    public static Callback<TableColumn<DataItem<?>, DataDeclaration>, TableCell<DataItem<?>, DataDeclaration>> rawValueCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final DataDeclaration item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(FormatUtil.hexadecimalBytes(item.data()));
                }
            }
        };
    }

    /// {@return a cell factory for the instruction type column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> instructionTypeCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction _ -> "I";
                        case final RegisterInstruction _ -> "R";
                        case final JumpInstruction _ -> "J";
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the operation column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> operationCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction _ -> item.instruction().opcode().name();
                        case final RegisterInstruction r -> r.func().name();
                        case final JumpInstruction _ -> item.instruction().opcode().name();
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the source 1 column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> source1CellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction i -> formatRegister(i.rs1());
                        case final RegisterInstruction r -> formatRegister(r.rs1());
                        case final JumpInstruction _ -> NOT_APPLICABLE;
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the source 2 column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> source2CellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction _ -> NOT_APPLICABLE;
                        case final RegisterInstruction r -> formatRegister(r.rs2());
                        case final JumpInstruction _ -> NOT_APPLICABLE;
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the destination column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> destinationCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction i -> formatRegister(i.rd());
                        case final RegisterInstruction r -> formatRegister(r.rd());
                        case final JumpInstruction _ -> NOT_APPLICABLE;
                    });
                }
            }
        };
    }

    /// {@return a cell factory for the immediate column}
    public static Callback<TableColumn<CodeItem, InstructionCall>, TableCell<CodeItem, InstructionCall>> immediateCellFactory() {
        return _ -> new TableCell<>() {

            @Override
            protected void updateItem(final InstructionCall item, final boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(switch (item.instruction()) {
                        case final ImmediateInstruction i -> formatNumber(i.immediate());
                        case final RegisterInstruction _ -> NOT_APPLICABLE;
                        case final JumpInstruction j -> formatDistance(j.distance());
                    });
                }
            }
        };
    }

    private static final String formatRegister(final int index) {
        return "R" + index;
    }

    private static final String formatNumber(final short number) {
        return "#" + number;
    }

    private static final String formatDistance(final int distance) {
        return "0x" + Integer.toHexString(distance).toUpperCase();
    }

    /// Private constructor to prevent instantiation.
    private EditorView() {
    }
}
