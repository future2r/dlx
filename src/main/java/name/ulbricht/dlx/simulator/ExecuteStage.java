package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// **EX** (Execute) stage.
///
/// This is the third of the five pipeline stages and the most complex one. It
/// performs four tasks in a single cycle:
///
/// 1. **Forwarding** - resolves RAW data hazards by substituting stale
///    register-file values with fresh results from the EX/MEM or MEM/WB latches
///    (see [Forwarding]).
/// 2. **ALU input selection** - chooses between the (possibly forwarded)
///    register value and the sign-extended immediate as the second ALU operand.
/// 3. **ALU execution** - computes the arithmetic, logical, address, or
///    comparison result (see [ALU]).
/// 4. **Branch / jump resolution** - evaluates branch conditions and computes
///    jump targets; if a redirect is needed the method signals the CPU via
///    [ExecuteResult#pcRedirect()].
///
/// ## Branch target convention
/// All offsets are treated as **signed byte offsets** added to the address of
/// the branch or jump instruction itself (stored in [IdExLatch#pc()]):
///
/// | Instruction | Target |
/// |-------------|--------|
/// | BEQZ / BNEZ | `PC + sign_extend(imm16)` |
/// | J / JAL     | `PC + sign_extend(dist26)` |
/// | JR / JALR   | `rs1` (register value) |
///
/// ## JAL / JALR link address
/// For JAL and JALR the ALU result written to R31 is `PC + 4` - the address of
/// the instruction that follows the jump - not the jump target. This value is
/// computed here and stored in `result` before building the [ExMemLatch] latch.
final class ExecuteStage {

    /// Private constructor - this class is not instantiable.
    private ExecuteStage() {
    }

    /// Executes the instruction in `idEx` and returns the EX stage output.
    ///
    /// The `exMem` and `memWb` parameters are the **current** (pre-commit) latch
    /// values, used as forwarding sources. The CPU computes MEM and WB outputs first
    /// and passes `newMemWb` here so that MEM/WB forwarding is up-to-date within the
    /// same clock cycle.
    ///
    /// @param idEx  the ID/EX latch for the instruction being executed
    /// @param exMem the current EX/MEM latch (EX/MEM forwarding source)
    /// @param memWb the current MEM/WB latch (MEM/WB forwarding source)
    /// @param alu   the ALU instance to invoke
    /// @return the EX stage output including the new EX/MEM latch value and an
    ///         optional PC redirect
    static ExecuteResult execute(
            final IdExLatch idEx,
            final ExMemLatch exMem,
            final MemWbLatch memWb,
            final ALU alu) {

        requireNonNull(idEx, "idEx must not be null");
        requireNonNull(exMem, "exMem must not be null");
        requireNonNull(memWb, "memWb must not be null");
        requireNonNull(alu, "alu must not be null");

        final var ctrl = idEx.ctrl();

        // -----------------------------------------------------------------
        // Step 1: Resolve forwarding for both ALU operands.
        // The Forwarding unit decides whether to use the register-file value
        // (from ID) or a fresher value from a later latch.
        // -----------------------------------------------------------------
        final var fwdA = Forwarding.selectA(idEx.rs1(), exMem, memWb);
        final var fwdB = Forwarding.selectB(idEx.rs2(), exMem, memWb);

        final var aVal = resolve(fwdA, idEx.rs1Val(), exMem, memWb);
        final var bVal = resolve(fwdB, idEx.rs2Val(), exMem, memWb);

        // -----------------------------------------------------------------
        // Step 2: Select ALU operand B.
        // For LHI the immediate must first be shifted left by 16 bits.
        // For immediate-mode instructions, use the (possibly shifted) imm.
        // For register-mode instructions, use the (possibly forwarded) bVal.
        // -----------------------------------------------------------------
        final var immVal = ctrl.loadHighImm() ? (idEx.immediate() << 16) : idEx.immediate();
        final var aluB = ctrl.aluSrc() ? immVal : bVal;

        // -----------------------------------------------------------------
        // Step 3: Execute the ALU operation.
        // -----------------------------------------------------------------
        final var aluResult = alu.execute(ctrl.aluOp(), aVal, aluB);
        // result may be overwritten below for JAL/JALR (link address).
        var result = aluResult.value();

        // -----------------------------------------------------------------
        // Step 4: Evaluate branches and compute jump targets.
        // -----------------------------------------------------------------
        // taken and newPc start as "not taken / zero" and are updated if
        // a branch condition is true or an unconditional jump is present.
        var taken = false;
        var newPc = 0;

        if (ctrl.branch()) {
            // Evaluate the branch condition using the (forwarded) rs1 value.
            // BEQZ: taken when rs1 == 0; BNEZ: taken when rs1 != 0.
            final var condition = ctrl.branchNotZero() ? (aVal != 0) : (aVal == 0);
            if (condition) {
                taken = true;
                // Branch target: PC of the branch instruction + signed byte offset.
                newPc = idEx.pc() + idEx.immediate();
            }
        } else if (ctrl.jump()) {
            taken = true;
            // JR / JALR: target is the value of rs1 (already forwarded in aVal).
            // J / JAL: target is PC + sign_extend(dist26) stored in immediate.
            newPc = ctrl.jumpReg() ? aVal : (idEx.pc() + idEx.immediate());
        }

        // -----------------------------------------------------------------
        // Step 5: For JAL / JALR, override the ALU result with the return
        // address (PC + 4) that will be written to R31 in WB.
        // -----------------------------------------------------------------
        if (ctrl.jalLink()) {
            result = idEx.pc() + 4;
        }

        // Pack the outputs into the EX/MEM latch.
        // bVal (the forwarded rs2 value) is carried forward as the store-data
        // value; the MEM stage will write it to memory if this is a store.
        final var newExMem = new ExMemLatch(ctrl, result, bVal, idEx.rd(), idEx.immediate());
        return new ExecuteResult(newExMem, taken, newPc);
    }

    /// Resolves the actual operand value for one ALU input based on the forwarding
    /// selector produced by the [Forwarding] unit.
    ///
    /// - [Forwarding.Forward#NONE]: use the register-file value read in ID.
    /// - [Forwarding.Forward#FROM_EX_MEM]: use the ALU result from the EX/MEM latch.
    /// - [Forwarding.Forward#FROM_MEM_WB]: use the write-back value from the MEM/WB latch. For
    ///   a load instruction this is `memData`; for all others it is `aluResult`.
    ///
    /// @param sel    the forwarding decision for this operand
    /// @param regVal the stale register-file value from ID (used when no forwarding
    ///               is needed)
    /// @param exMem  the current EX/MEM latch
    /// @param memWb  the current MEM/WB latch
    /// @return the resolved operand value
    private static int resolve(
            final Forwarding.Forward sel,
            final int regVal,
            final ExMemLatch exMem,
            final MemWbLatch memWb) {
        return switch (sel) {
            case NONE -> regVal;
            case FROM_EX_MEM -> exMem.aluResult();
            // For load instructions the forwarded value is the memory data;
            // for all other producers it is the ALU result.
            case FROM_MEM_WB -> memWb.ctrl().memToReg() ? memWb.memData() : memWb.aluResult();
        };
    }
}
