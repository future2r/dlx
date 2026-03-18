package name.ulbricht.dlx.simulator;

import static name.ulbricht.dlx.simulator.InstructionFactory.add;
import static name.ulbricht.dlx.simulator.InstructionFactory.addi;
import static name.ulbricht.dlx.simulator.InstructionFactory.beqz;
import static name.ulbricht.dlx.simulator.InstructionFactory.bnez;
import static name.ulbricht.dlx.simulator.InstructionFactory.halt;
import static name.ulbricht.dlx.simulator.InstructionFactory.j;
import static name.ulbricht.dlx.simulator.InstructionFactory.jal;
import static name.ulbricht.dlx.simulator.InstructionFactory.jalr;
import static name.ulbricht.dlx.simulator.InstructionFactory.jr;
import static name.ulbricht.dlx.simulator.InstructionFactory.lhi;
import static name.ulbricht.dlx.simulator.InstructionFactory.lw;
import static name.ulbricht.dlx.simulator.InstructionFactory.nop;
import static name.ulbricht.dlx.simulator.InstructionFactory.sll;
import static name.ulbricht.dlx.simulator.InstructionFactory.sub;
import static name.ulbricht.dlx.simulator.InstructionFactory.sw;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class InstructionFactoryTest {

    // ── R-format ─────────────────────────────────────────────────────────────

    @Test
    void nopEncodesAsAllZeros() {
        final var decoded = InstructionDecoder.decode(nop());
        final var r = assertInstanceOf(RegisterInstruction.class, decoded);
        assertEquals(OperationCode.SPECIAL, r.opcode());
        assertEquals(FunctionCode.NOP, r.func());
        assertEquals(0, r.rs1());
        assertEquals(0, r.rs2());
        assertEquals(0, r.rd());
    }

    @Test
    void addRoundTrips() {
        final var decoded = InstructionDecoder.decode(add(3, 1, 2));
        final var r = assertInstanceOf(RegisterInstruction.class, decoded);
        assertEquals(OperationCode.SPECIAL, r.opcode());
        assertEquals(FunctionCode.ADD, r.func());
        assertEquals(1, r.rs1());
        assertEquals(2, r.rs2());
        assertEquals(3, r.rd());
    }

    @Test
    void subRoundTrips() {
        final var decoded = InstructionDecoder.decode(sub(5, 10, 20));
        final var r = assertInstanceOf(RegisterInstruction.class, decoded);
        assertEquals(FunctionCode.SUB, r.func());
        assertEquals(10, r.rs1());
        assertEquals(20, r.rs2());
        assertEquals(5, r.rd());
    }

    @Test
    void sllRoundTrips() {
        final var decoded = InstructionDecoder.decode(sll(4, 2, 3));
        final var r = assertInstanceOf(RegisterInstruction.class, decoded);
        assertEquals(FunctionCode.SLL, r.func());
        assertEquals(2, r.rs1());
        assertEquals(3, r.rs2());
        assertEquals(4, r.rd());
    }

    // ── I-format — arithmetic immediate ──────────────────────────────────────

    @Test
    void addiRoundTrips() {
        final var decoded = InstructionDecoder.decode(addi(1, 0, 42));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.ADDI, i.opcode());
        assertEquals(0, i.rs1());
        assertEquals(1, i.rd());
        assertEquals(42, i.immediate());
    }

    @Test
    void addiNegativeImmediate() {
        final var decoded = InstructionDecoder.decode(addi(5, 3, -100));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.ADDI, i.opcode());
        assertEquals(3, i.rs1());
        assertEquals(5, i.rd());
        assertEquals(-100, i.immediate());
    }

    // ── I-format — load high immediate ───────────────────────────────────────

    @Test
    void lhiRoundTrips() {
        final var decoded = InstructionDecoder.decode(lhi(1, 0x1234));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.LHI, i.opcode());
        assertEquals(0, i.rs1());
        assertEquals(1, i.rd());
        assertEquals(0x1234, i.immediate());
    }

    // ── I-format — branches ──────────────────────────────────────────────────

    @Test
    void beqzRoundTrips() {
        final var decoded = InstructionDecoder.decode(beqz(5, 16));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.BEQZ, i.opcode());
        assertEquals(5, i.rs1());
        assertEquals(0, i.rd());
        assertEquals(16, i.immediate());
    }

    @Test
    void bnezNegativeOffset() {
        final var decoded = InstructionDecoder.decode(bnez(3, -8));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.BNEZ, i.opcode());
        assertEquals(3, i.rs1());
        assertEquals(-8, i.immediate());
    }

    // ── I-format — loads and stores ──────────────────────────────────────────

    @Test
    void lwRoundTrips() {
        final var decoded = InstructionDecoder.decode(lw(7, 2, 100));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.LW, i.opcode());
        assertEquals(2, i.rs1());
        assertEquals(7, i.rd());
        assertEquals(100, i.immediate());
    }

    @Test
    void swRoundTrips() {
        final var decoded = InstructionDecoder.decode(sw(2, 7, 100));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.SW, i.opcode());
        assertEquals(2, i.rs1());
        assertEquals(7, i.rd());
        assertEquals(100, i.immediate());
    }

    // ── I-format — jump register ─────────────────────────────────────────────

    @Test
    void jrRoundTrips() {
        final var decoded = InstructionDecoder.decode(jr(31));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.JR, i.opcode());
        assertEquals(31, i.rs1());
        assertEquals(0, i.rd());
        assertEquals(0, i.immediate());
    }

    @Test
    void jalrRoundTrips() {
        final var decoded = InstructionDecoder.decode(jalr(10));
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.JALR, i.opcode());
        assertEquals(10, i.rs1());
    }

    // ── I-format — halt ──────────────────────────────────────────────────────

    @Test
    void haltRoundTrips() {
        final var decoded = InstructionDecoder.decode(halt());
        final var i = assertInstanceOf(ImmediateInstruction.class, decoded);
        assertEquals(OperationCode.HALT, i.opcode());
    }

    // ── J-format ─────────────────────────────────────────────────────────────

    @Test
    void jRoundTrips() {
        final var decoded = InstructionDecoder.decode(j(100));
        final var jump = assertInstanceOf(JumpInstruction.class, decoded);
        assertEquals(OperationCode.J, jump.opcode());
        assertEquals(100, jump.distance());
    }

    @Test
    void jalRoundTrips() {
        final var decoded = InstructionDecoder.decode(jal(-200));
        final var jump = assertInstanceOf(JumpInstruction.class, decoded);
        assertEquals(OperationCode.JAL, jump.opcode());
        assertEquals(-200, jump.distance());
    }

    @Test
    void jNegativeDistance() {
        final var decoded = InstructionDecoder.decode(j(-16));
        final var jump = assertInstanceOf(JumpInstruction.class, decoded);
        assertEquals(-16, jump.distance());
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void registerOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> add(32, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> add(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> addi(0, 32, 0));
    }

    @Test
    void immediateOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> addi(1, 0, 32_768));
        assertThrows(IllegalArgumentException.class, () -> addi(1, 0, -32_769));
    }

    @Test
    void distanceOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> j(33_554_432));
        assertThrows(IllegalArgumentException.class, () -> j(-33_554_433));
    }
}
