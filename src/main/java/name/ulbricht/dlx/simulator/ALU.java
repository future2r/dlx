package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Arithmetic-Logic Unit (ALU).
///
/// Performs a single [Operation] operation on two 32-bit integer operands and
/// returns the result together with an overflow flag.
final class ALU {

    /// Package-private constructor - only the CPU creates ALU instances.
    ALU() {
    }

    /// ALU operation selector.
    ///
    /// Each constant identifies one arithmetic or logical operation that the
    /// enclosing [ALU] can perform on two 32-bit integer operands. The
    /// control unit resolves the correct `Operation` during instruction decode.
    enum Operation {

        /// Signed 32-bit addition (`a + b`).
        ///
        /// Overflow is detected via the standard two's-complement rule and
        /// reported in [Result#overflow()].
        ADD,

        /// Unsigned 32-bit addition (`a + b`).
        ///
        /// Produces the same bit pattern as [#ADD] but never sets the overflow
        /// flag.
        ADDU,

        /// Signed 32-bit subtraction (`a - b`).
        ///
        /// Overflow is detected and reported in [Result#overflow()].
        SUB,

        /// Unsigned 32-bit subtraction (`a - b`).
        ///
        /// Produces the same bit pattern as [#SUB] but never sets the overflow
        /// flag.
        SUBU,

        /// Bitwise AND (`a & b`).
        AND,

        /// Bitwise OR (`a | b`).
        OR,

        /// Bitwise exclusive OR (`a ^ b`).
        XOR,

        /// Logical shift left (`a << (b & 0x1F)`).
        ///
        /// The shift amount is taken from the lower 5 bits of operand `b`,
        /// matching the DLX hardware behaviour. Vacated bits are filled with
        /// zero.
        SLL,

        /// Logical shift right, zero-fill (`a >>> (b & 0x1F)`).
        ///
        /// The shift amount is taken from the lower 5 bits of operand `b`.
        /// Vacated high-order bits are filled with zero regardless of the sign
        /// of `a`.
        SRL,

        /// Arithmetic shift right, sign-fill (`a >> (b & 0x1F)`).
        ///
        /// The shift amount is taken from the lower 5 bits of operand `b`.
        /// Vacated high-order bits are filled with the sign bit of `a`,
        /// preserving the sign of the value.
        SRA,

        /// Set if equal: result is `1` when `a == b`, otherwise `0`.
        SEQ,

        /// Set if not equal: result is `1` when `a != b`, otherwise `0`.
        SNE,

        /// Set if less than (signed): result is `1` when `a < b`, otherwise `0`.
        SLT,

        /// Set if greater than (signed): result is `1` when `a > b`, otherwise `0`.
        SGT,

        /// Set if less than or equal (signed): result is `1` when `a <= b`,
        /// otherwise `0`.
        SLE,

        /// Set if greater than or equal (signed): result is `1` when `a >= b`,
        /// otherwise `0`.
        SGE,

        /// Pass-through: result equals operand `b` unchanged, ignoring `a`.
        ///
        /// Used by the LHI instruction: the EX stage pre-shifts the immediate
        /// left by 16 bits and passes it as operand `b`; this operation then
        /// writes that shifted value into the destination register without any
        /// further arithmetic.
        PASS_B
    }

    /// Immutable result of a single [ALU] operation.
    ///
    /// The `overflow` flag is only meaningful for **signed** addition
    /// ([Operation#ADD]) and **signed** subtraction ([Operation#SUB]); all other
    /// operations always return `false` for that field. The current simulator
    /// does not raise an exception on overflow - the flag is available for
    /// future trap handling or display purposes.
    ///
    /// @param value    the 32-bit result of the ALU operation
    /// @param overflow `true` if a signed arithmetic overflow occurred;
    ///                 always `false` for non-arithmetic or unsigned operations
    record Result(int value, boolean overflow) {}

    /// Executes `op` on operands `a` (first/left) and `b` (second/right)
    /// and returns the result.
    ///
    /// @param op the ALU operation to perform; must not be `null`
    /// @param a  the first (left-hand) operand
    /// @param b  the second (right-hand) operand; for shift operations only
    ///           the lower 5 bits are used
    /// @return a [Result] containing the 32-bit result and the overflow
    ///         flag (always `false` for non-arithmetic or unsigned operations)
    Result execute(final Operation op, final int a, final int b) {
        requireNonNull(op, "op must not be null");
        
        return switch (op) {

            case ADD -> {
                final var r = a + b;
                // Signed overflow: if both inputs have the same sign but the
                // result has the opposite sign, overflow occurred.
                final var ov = ((a ^ r) & (b ^ r)) < 0;
                yield new Result(r, ov);
            }

            // Unsigned addition - same bit pattern, no overflow detection.
            case ADDU -> new Result(a + b, false);

            case SUB -> {
                final var r = a - b;
                // Signed overflow: if the inputs have different signs and the
                // result sign differs from a's sign, overflow occurred.
                final var ov = ((a ^ b) & (a ^ r)) < 0;
                yield new Result(r, ov);
            }

            // Unsigned subtraction - same bit pattern, no overflow detection.
            case SUBU -> new Result(a - b, false);

            // Bitwise operations never overflow.
            case AND -> new Result(a & b, false);
            case OR  -> new Result(a | b, false);
            case XOR -> new Result(a ^ b, false);

            // Shift operations: mask b to 5 bits to prevent undefined behaviour.
            case SLL -> new Result(a << (b & 0x1F), false);
            case SRL -> new Result(a >>> (b & 0x1F), false);  // zero-fill
            case SRA -> new Result(a >> (b & 0x1F), false);   // sign-fill

            // Set-if comparisons: result is 1 (true) or 0 (false).
            case SEQ -> new Result(a == b ? 1 : 0, false);
            case SNE -> new Result(a != b ? 1 : 0, false);
            case SLT -> new Result(a <  b ? 1 : 0, false);  // signed <
            case SGT -> new Result(a >  b ? 1 : 0, false);  // signed >
            case SLE -> new Result(a <= b ? 1 : 0, false);  // signed <=
            case SGE -> new Result(a >= b ? 1 : 0, false);  // signed >=

            // Pass-through: return b unchanged (used by LHI after pre-shifting).
            case PASS_B -> new Result(b, false);
        };
    }
}