package name.ulbricht.dlx.ui.view.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.CPU.PipelineSnapshot;
import name.ulbricht.dlx.simulator.ControlSignals;
import name.ulbricht.dlx.simulator.ExMemLatch;
import name.ulbricht.dlx.simulator.IdExLatch;
import name.ulbricht.dlx.simulator.IfIdLatch;
import name.ulbricht.dlx.simulator.InstructionFormatter;
import name.ulbricht.dlx.simulator.MemWbLatch;
import name.ulbricht.dlx.simulator.ProcessingListener;

/// View model for the pipeline view. Exposes per-stage display strings derived
/// from the [PipelineSnapshot] delivered with each [ProcessStep].
public final class PipelineViewModel implements ProcessingListener {

    private final Executor uiExecutor;

    private final ObjectProperty<CPU> processor = new SimpleObjectProperty<>();

    // -- IF stage --
    private final ReadOnlyStringWrapper ifInstruction = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper ifPc = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper ifWord = new ReadOnlyStringWrapper("");

    // -- ID stage --
    private final ReadOnlyStringWrapper idInstruction = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper idRs1 = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper idRs2 = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper idRd = new ReadOnlyStringWrapper("");

    // -- EX stage --
    private final ReadOnlyStringWrapper exInstruction = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper exAluResult = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper exRd = new ReadOnlyStringWrapper("");

    // -- MEM stage --
    private final ReadOnlyStringWrapper memInstruction = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper memAluResult = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper memData = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper memRd = new ReadOnlyStringWrapper("");

    // -- WB stage --
    private final ReadOnlyStringWrapper wbInstruction = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper wbResult = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper wbRd = new ReadOnlyStringWrapper("");

    /// Creates a new pipeline view model.
    ///
    /// @param uiExecutor the executor to use for UI updates, must not be `null`
    public PipelineViewModel(@NamedArg("uiExecutor") final Executor uiExecutor) {
        this.uiExecutor = requireNonNull(uiExecutor);
        this.processor.subscribe(this::processorChanged);
    }

    /// {@return a property representing the current processor}
    public ObjectProperty<CPU> processorProperty() {
        return this.processor;
    }

    // -- IF properties --

    /// {@return a read-only property for the IF stage instruction label}
    public ReadOnlyStringProperty ifInstructionProperty() {
        return this.ifInstruction.getReadOnlyProperty();
    }

    /// {@return the current IF stage instruction text}
    public String getIfInstruction() {
        return this.ifInstruction.get();
    }

    /// {@return a read-only property for the IF stage PC}
    public ReadOnlyStringProperty ifPcProperty() {
        return this.ifPc.getReadOnlyProperty();
    }

    /// {@return the current IF stage PC text}
    public String getIfPc() {
        return this.ifPc.get();
    }

    /// {@return a read-only property for the IF stage instruction word}
    public ReadOnlyStringProperty ifWordProperty() {
        return this.ifWord.getReadOnlyProperty();
    }

    /// {@return the current IF stage instruction word text}
    public String getIfWord() {
        return this.ifWord.get();
    }

    // -- ID properties --

    /// {@return a read-only property for the ID stage instruction}
    public ReadOnlyStringProperty idInstructionProperty() {
        return this.idInstruction.getReadOnlyProperty();
    }

    /// {@return the current ID stage instruction text}
    public String getIdInstruction() {
        return this.idInstruction.get();
    }

    /// {@return a read-only property for the ID stage rs1}
    public ReadOnlyStringProperty idRs1Property() {
        return this.idRs1.getReadOnlyProperty();
    }

    /// {@return the current ID stage rs1 text}
    public String getIdRs1() {
        return this.idRs1.get();
    }

    /// {@return a read-only property for the ID stage rs2}
    public ReadOnlyStringProperty idRs2Property() {
        return this.idRs2.getReadOnlyProperty();
    }

    /// {@return the current ID stage rs2 text}
    public String getIdRs2() {
        return this.idRs2.get();
    }

    /// {@return a read-only property for the ID stage rd}
    public ReadOnlyStringProperty idRdProperty() {
        return this.idRd.getReadOnlyProperty();
    }

    /// {@return the current ID stage rd text}
    public String getIdRd() {
        return this.idRd.get();
    }

    // -- EX properties --

    /// {@return a read-only property for the EX stage instruction}
    public ReadOnlyStringProperty exInstructionProperty() {
        return this.exInstruction.getReadOnlyProperty();
    }

    /// {@return the current EX stage instruction text}
    public String getExInstruction() {
        return this.exInstruction.get();
    }

    /// {@return a read-only property for the EX stage ALU result}
    public ReadOnlyStringProperty exAluResultProperty() {
        return this.exAluResult.getReadOnlyProperty();
    }

    /// {@return the current EX stage ALU result text}
    public String getExAluResult() {
        return this.exAluResult.get();
    }

    /// {@return a read-only property for the EX stage rd}
    public ReadOnlyStringProperty exRdProperty() {
        return this.exRd.getReadOnlyProperty();
    }

    /// {@return the current EX stage rd text}
    public String getExRd() {
        return this.exRd.get();
    }

    // -- MEM properties --

    /// {@return a read-only property for the MEM stage instruction}
    public ReadOnlyStringProperty memInstructionProperty() {
        return this.memInstruction.getReadOnlyProperty();
    }

    /// {@return the current MEM stage instruction text}
    public String getMemInstruction() {
        return this.memInstruction.get();
    }

    /// {@return a read-only property for the MEM stage ALU result}
    public ReadOnlyStringProperty memAluResultProperty() {
        return this.memAluResult.getReadOnlyProperty();
    }

    /// {@return the current MEM stage ALU result text}
    public String getMemAluResult() {
        return this.memAluResult.get();
    }

    /// {@return a read-only property for the MEM stage memory data}
    public ReadOnlyStringProperty memDataProperty() {
        return this.memData.getReadOnlyProperty();
    }

    /// {@return the current MEM stage memory data text}
    public String getMemData() {
        return this.memData.get();
    }

    /// {@return a read-only property for the MEM stage rd}
    public ReadOnlyStringProperty memRdProperty() {
        return this.memRd.getReadOnlyProperty();
    }

    /// {@return the current MEM stage rd text}
    public String getMemRd() {
        return this.memRd.get();
    }

    // -- WB properties --

    /// {@return a read-only property for the WB stage instruction}
    public ReadOnlyStringProperty wbInstructionProperty() {
        return this.wbInstruction.getReadOnlyProperty();
    }

    /// {@return the current WB stage instruction text}
    public String getWbInstruction() {
        return this.wbInstruction.get();
    }

    /// {@return a read-only property for the WB stage result}
    public ReadOnlyStringProperty wbResultProperty() {
        return this.wbResult.getReadOnlyProperty();
    }

    /// {@return the current WB stage result text}
    public String getWbResult() {
        return this.wbResult.get();
    }

    /// {@return a read-only property for the WB stage rd}
    public ReadOnlyStringProperty wbRdProperty() {
        return this.wbRd.getReadOnlyProperty();
    }

    /// {@return the current WB stage rd text}
    public String getWbRd() {
        return this.wbRd.get();
    }

    // -- ProcessingListener --

    @Override
    public void processing(final ProcessStep step) {
        final var pipeline = step.pipeline();
        this.uiExecutor.execute(() -> updateFromSnapshot(pipeline));
    }

    // -- internal --

    private void processorChanged(final CPU oldProcessor, final CPU newProcessor) {
        if (oldProcessor != null) {
            oldProcessor.removeProcessingListener(this);
        }
        clearAll();
        if (newProcessor != null) {
            newProcessor.addProcessingListener(this);
        }
    }

    private void clearAll() {
        this.ifInstruction.set("");
        this.ifPc.set("");
        this.ifWord.set("");

        this.idInstruction.set("");
        this.idRs1.set("");
        this.idRs2.set("");
        this.idRd.set("");

        this.exInstruction.set("");
        this.exAluResult.set("");
        this.exRd.set("");

        this.memInstruction.set("");
        this.memAluResult.set("");
        this.memData.set("");
        this.memRd.set("");

        this.wbInstruction.set("");
        this.wbResult.set("");
        this.wbRd.set("");
    }

    private void updateFromSnapshot(final PipelineSnapshot pipeline) {
        updateIf(pipeline.ifId());
        updateId(pipeline.idEx());
        updateEx(pipeline.exMem());
        updateMemWb(pipeline.memWb());
    }

    private void updateIf(final IfIdLatch ifId) {
        if (ifId == IfIdLatch.BUBBLE) {
            this.ifInstruction.set("bubble");
            this.ifPc.set("");
            this.ifWord.set("");
        } else {
            this.ifInstruction.set(InstructionFormatter.format(ifId.instructionWord()));
            this.ifPc.set("0x%08X".formatted(Integer.valueOf(ifId.pc())));
            this.ifWord.set("0x%08X".formatted(Integer.valueOf(ifId.instructionWord())));
        }
    }

    private void updateId(final IdExLatch idEx) {
        if (idEx == IdExLatch.BUBBLE) {
            this.idInstruction.set("bubble");
            this.idRs1.set("");
            this.idRs2.set("");
            this.idRd.set("");
        } else {
            this.idInstruction.set(describeFromControl(idEx.ctrl()));
            this.idRs1.set("R%d = 0x%08X".formatted(Integer.valueOf(idEx.rs1()), Integer.valueOf(idEx.rs1Val())));
            this.idRs2.set("R%d = 0x%08X".formatted(Integer.valueOf(idEx.rs2()), Integer.valueOf(idEx.rs2Val())));
            this.idRd.set("R%d".formatted(Integer.valueOf(idEx.rd())));
        }
    }

    private void updateEx(final ExMemLatch exMem) {
        if (exMem == ExMemLatch.BUBBLE) {
            this.exInstruction.set("bubble");
            this.exAluResult.set("");
            this.exRd.set("");
        } else {
            this.exInstruction.set(describeFromControl(exMem.ctrl()));
            this.exAluResult.set("0x%08X".formatted(Integer.valueOf(exMem.aluResult())));
            this.exRd.set("R%d".formatted(Integer.valueOf(exMem.rd())));
        }
    }

    private void updateMemWb(final MemWbLatch memWb) {
        // The memWb latch represents the instruction that just completed MEM
        // and is about to enter WB. We show it as the MEM stage content.
        // WB is shown from the previous cycle's memWb — but since we get
        // notified before and after each step, we use a simple approach:
        // shift the current MEM display to WB before updating MEM.

        // Move current MEM → WB
        this.wbInstruction.set(this.memInstruction.get());
        this.wbResult.set(this.memAluResult.get());
        this.wbRd.set(this.memRd.get());

        // Update MEM from the new memWb latch
        if (memWb == MemWbLatch.BUBBLE) {
            this.memInstruction.set("bubble");
            this.memAluResult.set("");
            this.memData.set("");
            this.memRd.set("");
        } else {
            this.memInstruction.set(describeFromControl(memWb.ctrl()));
            this.memAluResult.set("0x%08X".formatted(Integer.valueOf(memWb.aluResult())));
            if (memWb.ctrl().memory().memRead()) {
                this.memData.set("0x%08X".formatted(Integer.valueOf(memWb.memData())));
            } else {
                this.memData.set("");
            }
            this.memRd.set("R%d".formatted(Integer.valueOf(memWb.rd())));
        }
    }

    /// Derives a short instruction description from control signals. This is used
    /// for stages past ID where the original instruction word is no
    /// longer available.
    @SuppressWarnings("checkstyle:NPathComplexity")
    private static String describeFromControl(final ControlSignals ctrl) {
        if (ctrl == ControlSignals.NOP) {
            return "NOP";
        }
        if (ctrl.trap()) {
            return "TRAP";
        }

        final var flow = ctrl.flow();
        if (flow.jump()) {
            if (flow.jalLink() && flow.jumpReg()) {
                return "JALR";
            }
            if (flow.jalLink()) {
                return "JAL";
            }
            if (flow.jumpReg()) {
                return "JR";
            }
            return "J";
        }
        if (flow.branch()) {
            return flow.branchNotZero() ? "BNEZ" : "BEQZ";
        }

        final var memory = ctrl.memory();
        if (memory.memRead()) {
            return "LOAD";
        }
        if (memory.memWrite()) {
            return "STORE";
        }

        final var alu = ctrl.alu();
        if (alu.loadHighImm()) {
            return "LHI";
        }
        return alu.displayName();
    }
}
