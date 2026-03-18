package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Determines whether an ALU operand in the **EX** stage should be taken from a
/// later pipeline latch instead of from the value that was read from the
/// register file in ID.
///
/// ## Why forwarding is necessary
/// In a pipelined processor, the WB stage writes its result to the register file
/// at the end of a cycle. The ID stage reads the register file at the beginning
/// of a cycle two cycles earlier. Without forwarding, any instruction that
/// produces a value in EX or MEM would cause a two- or three-cycle stall before
/// the next instruction could safely read that value.
///
/// Forwarding eliminates most stalls by routing the result directly from the
/// pipeline latch where it sits to the ALU input it is needed. Only the load-use
/// case - where the value is not yet in any latch when EX needs it - still
/// requires a one-cycle stall (see [HazardDetectionUnit]).
///
/// ## Forwarding paths
/// Two forwarding paths exist:
///
/// | Source latch | Distance | Selector constant |
/// |--------------|----------|-------------------|
/// | EX/MEM       | 1 cycle ahead | [Forward#FROM_EX_MEM] |
/// | MEM/WB       | 2 cycles ahead | [Forward#FROM_MEM_WB] |
///
/// EX/MEM takes priority: if both latches write to the same register (possible
/// when two consecutive instructions both produce the same `rd`) the more recent
/// value wins.
///
/// R0 is never forwarded because it is hardwired to zero and the register file
/// read already returns 0 correctly.
final class Forwarding {

    /// Private constructor - this class is not instantiable.
    private Forwarding() {
    }

    /// Selects the value source for one ALU operand in the EX stage.
    ///
    /// The [Forwarding] unit computes a `Forward` for each of the two ALU
    /// inputs (`rs1` and `rs2`) every cycle. The EX stage then uses these
    /// selectors to pick the correct operand value from one of three possible
    /// sources, resolving RAW (read-after-write) data hazards without stalling.
    ///
    /// ## Priority
    /// When both the EX/MEM and MEM/WB latches would supply a value for the
    /// same register, the EX/MEM source takes priority because it carries a
    /// more recent result.
    ///
    /// @see Forwarding
    /// @see ExecuteStage
    enum Forward {

        /// No forwarding - use the register value that was read in the ID stage
        /// and is stored in [IdExLatch#rs1Val()] or [IdExLatch#rs2Val()].
        ///
        /// This is the default: no producer instruction in EX/MEM or MEM/WB
        /// writes to the needed register.
        NONE,

        /// Forward the ALU result from the **EX/MEM** latch
        /// ([ExMemLatch#aluResult()]).
        ///
        /// Selected when the instruction in the MEM stage (one cycle ahead of
        /// the consumer) writes to the register that the current EX stage reads.
        /// This handles the most common back-to-back dependency:
        ///
        /// ```asm
        /// ADD R1, R2, R3   ; produces R1 - now in MEM
        /// ADD R4, R1, R5   ; consumes R1 - now in EX  → forward from EX/MEM
        /// ```
        FROM_EX_MEM,

        /// Forward the write-back value from the **MEM/WB** latch.
        ///
        /// For load instructions the forwarded value is [MemWbLatch#memData()];
        /// for all others it is [MemWbLatch#aluResult()].
        ///
        /// Selected when the instruction in the WB stage (two cycles ahead of
        /// the consumer) writes to the register that the current EX stage reads,
        /// and the EX/MEM source does not claim it.
        ///
        /// ```asm
        /// ADD R1, R2, R3   ; produces R1 - now in WB
        /// NOP              ; separates producer and consumer
        /// ADD R4, R1, R5   ; consumes R1 - now in EX  → forward from MEM/WB
        /// ```
        FROM_MEM_WB
    }

    /// Returns the forwarding selector for **operand A** (the value of `rs1`).
    ///
    /// @param rs1   the source register index read by the EX-stage instruction
    /// @param exMem the current EX/MEM latch (result from the preceding instruction)
    /// @param memWb the current MEM/WB latch (result from two instructions ago)
    /// @return the forwarding decision for operand A
    static Forward selectA(final int rs1, final ExMemLatch exMem, final MemWbLatch memWb) {
        requireNonNull(exMem, "exMem must not be null");
        requireNonNull(memWb, "memWb must not be null");
        return select(rs1, exMem, memWb);
    }

    /// Returns the forwarding selector for **operand B** (the value of `rs2` or the
    /// store-data register for store instructions).
    ///
    /// @param rs2   the source register index read by the EX-stage instruction
    /// @param exMem the current EX/MEM latch
    /// @param memWb the current MEM/WB latch
    /// @return the forwarding decision for operand B
    static Forward selectB(final int rs2, final ExMemLatch exMem, final MemWbLatch memWb) {
        requireNonNull(exMem, "exMem must not be null");
        requireNonNull(memWb, "memWb must not be null");
        return select(rs2, exMem, memWb);
    }

    /// Common implementation shared by [#selectA] and [#selectB].
    ///
    /// Priority order:
    /// 1. R0 is never forwarded - always [Forward#NONE].
    /// 2. EX/MEM forwarding if the MEM-stage instruction writes `reg`.
    /// 3. MEM/WB forwarding if the WB-stage instruction writes `reg`.
    /// 4. [Forward#NONE] if no producer is found.
    ///
    /// @param reg   the register index to check
    /// @param exMem the EX/MEM latch
    /// @param memWb the MEM/WB latch
    /// @return the appropriate [Forward] constant
    private static Forward select(final int reg, final ExMemLatch exMem, final MemWbLatch memWb) {
        // R0 is hardwired to zero - no forwarding needed or valid.
        if (reg == 0)
            return Forward.NONE;

        // EX/MEM path: the instruction in MEM writes to `reg` and has a
        // result ready in the EX/MEM latch.
        if (exMem.ctrl().regWrite() && exMem.rd() == reg)
            return Forward.FROM_EX_MEM;

        // MEM/WB path: the instruction in WB writes to `reg`. The value is
        // either the ALU result or the memory data (for loads) - the EX stage
        // chooses between them when it applies the forwarded value.
        if (memWb.ctrl().regWrite() && memWb.rd() == reg)
            return Forward.FROM_MEM_WB;

        // No active forwarding path - use the register-file value from ID.
        return Forward.NONE;
    }
}
