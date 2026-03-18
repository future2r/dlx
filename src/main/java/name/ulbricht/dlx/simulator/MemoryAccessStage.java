package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// **MEM** (Memory Access) stage.
///
/// This is the fourth of the five pipeline stages. It interacts with the data
/// memory for load and store instructions; all other instructions pass through
/// without performing any memory operation.
///
/// ## Load instructions
/// When `ctrl.memRead()` is `true`, the stage reads from the address stored in
/// `exMem.aluResult()` (the effective address computed in EX). The memory width
/// and sign/zero-extension behaviour are controlled by `ctrl.memWidth()` and
/// `ctrl.memUnsigned()` respectively. The loaded value is stored in
/// [MemWbLatch#memData()] so that the WB stage can write it to the register
/// file.
///
/// ## Store instructions
/// When `ctrl.memWrite()` is `true`, the stage writes `exMem.rs2Val()` (the
/// store-data value forwarded from EX) to the effective address. No register is
/// modified; `memData` in the output latch is `0` and `ctrl.regWrite()` is
/// `false`.
///
/// ## Non-memory instructions
/// Both `ctrl.memRead()` and `ctrl.memWrite()` are `false`. The stage simply
/// propagates `ctrl`, `aluResult`, and `rd` into the MEM/WB latch. `memData` is
/// `0` and will not be used by WB because `ctrl.memToReg()` is also `false`.
final class MemoryAccessStage {

    /// Private constructor - this class is not instantiable.
    private MemoryAccessStage() {
    }

    /// Performs the memory access (if any) for the instruction in `exMem` and
    /// returns the new MEM/WB latch value.
    ///
    /// @param exMem  the current EX/MEM latch produced by the EX stage
    /// @param memory the simulated data memory
    /// @return the populated [MemWbLatch] latch for the WB stage
    /// @throws IllegalArgumentException if the effective address is outside the
    ///                                  memory bounds
    static MemWbLatch execute(final ExMemLatch exMem, final Memory memory) {
        requireNonNull(exMem, "exMem must not be null");
        requireNonNull(memory, "memory must not be null");
        final var ctrl = exMem.ctrl();
        // The ALU result carries the effective byte address for loads/stores.
        final var addr = exMem.aluResult();
        // memData defaults to 0; reassigned only for load instructions.
        var memData = 0;

        if (ctrl.memRead()) {
            // Load: read from memory and select sign- or zero-extension.
            memData = switch (ctrl.memWidth()) {
                case BYTE -> ctrl.memUnsigned()
                        ? memory.loadByteU(addr) // LBU: zero-extend
                        : memory.loadByte(addr); // LB: sign-extend
                case HALF -> ctrl.memUnsigned()
                        ? memory.loadHalfWordU(addr) // LHU: zero-extend
                        : memory.loadHalfWord(addr); // LH: sign-extend
                case WORD -> memory.loadWord(addr); // LW: always full 32 bits
            };
        } else if (ctrl.memWrite()) {
            // Store: write the store-data value (carried through from EX) to memory.
            switch (ctrl.memWidth()) {
                case BYTE -> memory.storeByte(addr, exMem.rs2Val());
                case HALF -> memory.storeHalfWord(addr, exMem.rs2Val());
                case WORD -> memory.storeWord(addr, exMem.rs2Val());
            }
            // memData remains 0; regWrite is false for stores so WB ignores it.
        }

        // Propagate the ALU result and control signals into the MEM/WB latch.
        // For loads: memData contains the value to write; for others: unused.
        return new MemWbLatch(ctrl, exMem.aluResult(), memData, exMem.rd());
    }
}
