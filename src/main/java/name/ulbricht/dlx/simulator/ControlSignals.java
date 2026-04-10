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
/// ## Pipeline bubble
/// [#NOP] is the canonical "no side effects" instance. It is placed into a
/// latch whenever a bubble must be inserted (stall or flush). Every boolean
/// signal is `false` and the ALU operation is [Operation#ADD] (harmless default).
///
/// @param regWrite       write the result to the destination register in WB
/// @param memRead        read data from memory in the MEM stage (loads)
/// @param memWrite       write data to memory in the MEM stage (stores)
/// @param memToReg       select memory data (`true`) or ALU result (`false`)
///                       as the value written to the register file in WB
/// @param branch         this instruction is a conditional branch
///                       (BEQZ / BNEZ); branch condition is evaluated in EX
/// @param branchNotZero  when [#branch] is `true`: branch is taken when
///                       rs1 **≠ 0** (`true` = BNEZ) or when rs1 **= 0**
///                       (`false` = BEQZ)
/// @param jump           this instruction unconditionally redirects the PC
///                       (J / JAL / JR / JALR); the IF and ID stages are
///                       flushed after EX resolves the target
/// @param jumpReg        jump target address comes from register rs1
///                       (`true` = JR / JALR) rather than from the
///                       instruction's encoded distance (`false` = J / JAL)
/// @param jalLink        save `PC + 4` to R31 as the return address
///                       (`true` = JAL / JALR)
/// @param aluSrc         ALU second operand is the sign-extended immediate
///                       (`true`) rather than the value of rs2 (`false`)
/// @param loadHighImm    the immediate must be shifted left by 16 bits before
///                       being passed to the ALU as operand B (LHI only)
/// @param memWidth       granularity of the memory access (byte, half-word,
///                       or word); only consulted when [#memRead] or
///                       [#memWrite] is `true`; must not be `null`
/// @param memUnsigned    zero-extend memory loads instead of sign-extending
///                       them; only relevant for byte and half-word loads
/// @param aluOp          the operation the ALU must perform in the EX stage;
///                       must not be `null`
/// @param trap           this instruction is a trap; the pipeline flushes
///                       behind it and the CPU dispatches by trap number
///                       once it retires through WB
public record ControlSignals(
        boolean regWrite,
        boolean memRead,
        boolean memWrite,
        boolean memToReg,
        boolean branch,
        boolean branchNotZero,
        boolean jump,
        boolean jumpReg,
        boolean jalLink,
        boolean aluSrc,
        boolean loadHighImm,
        MemWidth memWidth,
        boolean memUnsigned,
        Operation aluOp,
        boolean trap
) {

    /// Validates that `memWidth` and `aluOp` are not `null`.
    ///
    /// @throws NullPointerException if `memWidth` or `aluOp` is `null`
    public ControlSignals {
        requireNonNull(memWidth, "memWidth must not be null");
        requireNonNull(aluOp,    "aluOp must not be null");
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
            false, false, false, false,
            false, false, false, false,
            false, false, false,
            MemWidth.WORD, false,
            Operation.ADD,
            false
    );
}
