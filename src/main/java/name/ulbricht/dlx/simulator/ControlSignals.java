package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.simulator.ALU.Operation;

/// Immutable set of control signals produced by the
/// [ControlUnit] during instruction decode (ID stage).
///
/// An instance travels through the pipeline alongside the data it governs -
/// from the ID/EX latch through EX/MEM and MEM/WB - so that each subsequent
/// stage can check only its own relevant signals without needing to re-examine
/// the original instruction.
///
/// The signals are grouped by concern to keep the record cohesive:
///
/// - write-back gating (`regWrite`, `memToReg`)
/// - control-flow decisions ([#flow])
/// - ALU setup ([#alu])
/// - memory access details ([#memory])
/// - lifecycle control (`trap`)
///
/// ## Pipeline bubble
/// [#NOP] is the canonical "no side effects" instance. It is placed into a
/// latch whenever a bubble must be inserted (stall or flush). Every boolean
/// signal is `false` and the ALU operation is [Operation#ADD] (harmless default).
///
/// @param regWrite write the result to the destination register in WB
/// @param memToReg select memory data (`true`) or ALU result (`false`) as the
///                 value written to the register file in WB
/// @param flow     grouped branch and jump signals; must not be `null`
/// @param alu      grouped ALU input and operation signals; must not be `null`
/// @param memory   grouped memory access signals; must not be `null`
/// @param trap     this instruction is a trap; the pipeline flushes behind it and
///                 the CPU dispatches by trap number once it retires through WB
public record ControlSignals(
        boolean regWrite,
        boolean memToReg,
        FlowControl flow,
        AluControl alu,
        MemoryControl memory,
        boolean trap
) {

    /// Validates that the grouped control blocks are not `null`.
    ///
    /// @throws NullPointerException if any grouped control block is `null`
    public ControlSignals {
        requireNonNull(flow, "flow must not be null");
        requireNonNull(alu, "alu must not be null");
        requireNonNull(memory, "memory must not be null");
    }

    /// Group of branch and jump signals consumed by the EX and ID stages.
    ///
    /// @param branch `true` when the instruction is a conditional branch
    /// @param branchNotZero `true` for BNEZ, `false` for BEQZ when branch is active
    /// @param jump `true` when the instruction unconditionally redirects the PC
    /// @param jumpReg `true` when the jump target comes from rs1 instead of the instruction
    /// @param jalLink `true` when the instruction writes `PC + 4` to R31
    public record FlowControl(
            boolean branch,
            boolean branchNotZero,
            boolean jump,
            boolean jumpReg,
            boolean jalLink
    ) {

        /// Shared instance for instructions that do not affect control flow.
        public static final FlowControl NONE = new FlowControl(false, false, false, false, false);

        /// Creates control for a conditional branch instruction.
        ///
        /// @param branchNotZero `true` for BNEZ, `false` for BEQZ
        /// @return the configured flow control block
        public static FlowControl branch(final boolean branchNotZero) {
            return new FlowControl(true, branchNotZero, false, false, false);
        }

        /// Creates control for an unconditional jump instruction.
        ///
        /// @param jumpReg `true` when the target address comes from rs1
        /// @param jalLink `true` when the instruction writes `PC + 4` to R31
        /// @return the configured flow control block
        public static FlowControl jump(final boolean jumpReg, final boolean jalLink) {
            return new FlowControl(false, false, true, jumpReg, jalLink);
        }
    }

    /// Group of ALU selection and execution signals consumed by the EX stage.
    ///
    /// @param aluSrc `true` when operand B comes from the immediate instead of rs2
    /// @param loadHighImm `true` when the immediate must be shifted left by 16 bits
    /// @param aluOp the ALU operation to execute; must not be `null`
    public record AluControl(
            boolean aluSrc,
            boolean loadHighImm,
            Operation aluOp
    ) {

        /// Shared default used when no special ALU configuration is needed.
        public static final AluControl DEFAULT = new AluControl(false, false, Operation.ADD);

        /// Validates that the ALU operation is not `null`.
        ///
        /// @throws NullPointerException if `aluOp` is `null`
        public AluControl {
            requireNonNull(aluOp, "aluOp must not be null");
        }

        /// {@return a UI-safe mnemonic for the configured ALU operation}
        public String displayName() {
            return this.aluOp.name();
        }
    }

    /// Group of memory-access signals consumed by the MEM stage.
    ///
    /// @param memRead `true` when the instruction reads from data memory
    /// @param memWrite `true` when the instruction writes to data memory
    /// @param memWidth the granularity of the access; must not be `null`
    /// @param memUnsigned `true` when loads must be zero-extended instead of sign-extended
    public record MemoryControl(
            boolean memRead,
            boolean memWrite,
            MemWidth memWidth,
            boolean memUnsigned
    ) {

        /// Shared instance for instructions that do not access data memory.
        public static final MemoryControl NONE = new MemoryControl(false, false, MemWidth.WORD, false);

        /// Creates control for a load instruction.
        ///
        /// @param memWidth the width of the load
        /// @param memUnsigned `true` for zero-extension, `false` for sign-extension
        /// @return the configured memory control block
        public static MemoryControl load(final MemWidth memWidth, final boolean memUnsigned) {
            return new MemoryControl(true, false, memWidth, memUnsigned);
        }

        /// Creates control for a store instruction.
        ///
        /// @param memWidth the width of the store
        /// @return the configured memory control block
        public static MemoryControl store(final MemWidth memWidth) {
            return new MemoryControl(false, true, memWidth, false);
        }

        /// Validates that the memory width is not `null`.
        ///
        /// @throws NullPointerException if `memWidth` is `null`
        public MemoryControl {
            requireNonNull(memWidth, "memWidth must not be null");
        }
    }

    /// Width of a memory access performed in the MEM stage.
    public enum MemWidth {

        /// 8-bit access (one byte).
        BYTE,

        /// 16-bit access (two bytes, big-endian).
        HALF,

        /// 32-bit access (four bytes, big-endian).
        WORD
    }

    /// Canonical pipeline bubble - all control signals are inactive, the
    /// ALU operation is [Operation#ADD] (a benign default), and `trap` is
    /// `false`.
    ///
    /// Use this constant whenever a stage must be flushed or a stall bubble
    /// must be injected rather than constructing a new instance.
    public static final ControlSignals NOP = new ControlSignals(
            false,
            false,
            FlowControl.NONE,
            AluControl.DEFAULT,
            MemoryControl.NONE,
            false
    );
}
