package name.ulbricht.dlx.simulator;

/// Pipeline latch between the **IF** (Instruction Fetch) and **ID**
/// (Instruction Decode) stages.
///
/// This record models the physical flip-flop register that latches the output of
/// the IF stage at the clock edge and holds it stable for the ID stage to read
/// during the next cycle.
///
/// ## Bubble
/// When the pipeline must be flushed (e.g. after a taken branch) or when the IF
/// stage is stalled, [#BUBBLE] is written into this latch instead. Its
/// instruction word is `0x00000000`, which the decoder recognises as the
/// canonical NOP (`SLL R0, R0, 0`).
///
/// @param pc              the byte address of the fetched instruction (the PC
///                        value *before* it was incremented by 4); used in EX to
///                        compute branch/jump targets and link addresses
/// @param instructionWord the raw 32-bit instruction word read from memory
public record IfIdLatch(int pc, int instructionWord) {

    /// Canonical pipeline bubble for the IF/ID latch.
    ///
    /// Both fields are zero: `pc = 0` and `instructionWord = 0x00000000` (the
    /// NOP encoding).
    public static final IfIdLatch BUBBLE = new IfIdLatch(0, 0);
}
