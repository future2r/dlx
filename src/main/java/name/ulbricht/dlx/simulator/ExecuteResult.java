package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Output of the **EX** (Execute) stage, returned by [ExecuteStage#execute].
///
/// The EX stage does two things whose results must be communicated back to the
/// [CPU][name.ulbricht.dlx.simulator.CPU]:
///
/// 1. It produces the [ExMemLatch] value that is latched into the EX/MEM
///    register and consumed by the MEM stage in the next cycle.
/// 2. It may **redirect the program counter** when a branch is taken or an
///    unconditional jump is executed. If `pcRedirect` is `true`, the CPU must:
///    - Replace the current PC with `newPc`.
///    - Flush the IF/ID and ID/EX latches by writing [IfIdLatch#BUBBLE] and
///      [IdExLatch#BUBBLE] (the two instructions younger than the branch/jump
///      are discarded).
///
/// When `pcRedirect` is `false`, `newPc` is `0` and must not be used.
///
/// @param exMem      the latch value produced by EX; passed to MEM unchanged
/// @param pcRedirect `true` if the PC must be redirected to `newPc` this cycle
/// @param newPc      the branch or jump target address; only valid when
///                   `pcRedirect` is `true`
record ExecuteResult(ExMemLatch exMem, boolean pcRedirect, int newPc) {
    ExecuteResult {
        requireNonNull(exMem, "exMem must not be null");
    }
}
