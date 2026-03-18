package name.ulbricht.dlx.simulator;

/// Decodes a raw 32-bit instruction word into a typed [Instruction] value.
///
/// The decoder is a pure stateless function - all behaviour lives in the single
/// static method [#decode(int)]. Instantiation is prevented.
///
/// ## Decoding rules
///
/// 1. Extract bits 31–26 as the 6-bit opcode.
/// 2. Look up the [OperationCode] constant via [OperationCode#fromCode(int)].
/// 3. Depending on the opcode, extract the remaining fields:
///    - [OperationCode#SPECIAL] → R-format (rs1, rs2, rd, func)
///    - [OperationCode#J] / [OperationCode#JAL] → J-format (sign-extended 26-bit
///      distance)
///    - Everything else → I-format (rs1, rd, sign-extended imm16)
///
/// @see Instruction
/// @see OperationCode
/// @see FunctionCode
public final class InstructionDecoder {

    /// Private constructor - this class is not instantiable.
    private InstructionDecoder() {
    }

    /// Decodes a 32-bit instruction word and returns the corresponding
    /// [Instruction] subtype.
    ///
    /// The method is pure: the same input always produces an equal output and no
    /// state is modified.
    ///
    /// @param word the 32-bit instruction word to decode
    /// @return a [RegisterInstruction], [ImmediateInstruction], or [JumpInstruction]
    /// @throws IllegalArgumentException if the opcode or func code value is
    ///                                  not recognised
    public static Instruction decode(final int word) {

        // Extract the upper 6 bits (bits 31–26) as the opcode.
        final var opcodeCode = (word >>> 26) & 0x3F;
        final var opcode = OperationCode.fromCode(opcodeCode);

        return switch (opcode) {

            // -----------------------------------------------------------------
            // R-format: SPECIAL opcode - operation is in the func field
            // -----------------------------------------------------------------
            case SPECIAL -> {
                // Bits 25–21: first source register
                final var rs1 = (word >>> 21) & 0x1F;
                // Bits 20–16: second source register
                final var rs2 = (word >>> 16) & 0x1F;
                // Bits 15–11: destination register
                final var rd = (word >>> 11) & 0x1F;
                // Bits 5–0: function code that selects the ALU operation
                final var func = FunctionCode.fromCode(word & 0x3F);
                yield new RegisterInstruction(opcode, rs1, rs2, rd, func);
            }

            // -----------------------------------------------------------------
            // J-format: J and JAL - 26-bit signed branch distance
            // -----------------------------------------------------------------
            case J, JAL -> {
                // Bits 25–0: raw unsigned 26-bit distance
                final var rawDist = word & 0x03FF_FFFF;
                // Sign-extend bit 25 into the upper 6 bits to make a signed
                // 32-bit byte offset.
                final var dist = (rawDist & 0x0200_0000) != 0
                        ? (rawDist | 0xFC00_0000)
                        : rawDist;
                yield new JumpInstruction(opcode, dist);
            }

            // -----------------------------------------------------------------
            // I-format: everything else - rs1, rd, 16-bit immediate
            // -----------------------------------------------------------------
            default -> {
                // Bits 25–21: source register (base for loads/stores/branches)
                final var rs1 = (word >>> 21) & 0x1F;
                // Bits 20–16: destination or store-data register
                final var rd = (word >>> 16) & 0x1F;
                // Bits 15–0: immediate value; cast to short sign-extends it.
                final var imm = (short) (word & 0xFFFF);
                yield new ImmediateInstruction(opcode, rs1, rd, imm);
            }
        };
    }
}
