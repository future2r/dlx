# DLX Simulator

This project is about simulating that processor using Java and JavaFX.

## Development

### Prerequisites

- **Any JDK** installed and `JAVA_HOME` pointing to it. The Maven wrapper uses this JDK to bootstrap Maven itself.
- **JDK 25** (OpenJDK) registered in a [Maven toolchain](https://maven.apache.org/guides/mini/guide-using-toolchains.html). The build selects the correct JDK via the `maven-toolchains-plugin`, so your `JAVA_HOME` JDK does not need to be version 25. Add the following to your `~/.m2/toolchains.xml`:

```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>25</version>
      <vendor>openjdk</vendor>
    </provides>
    <configuration>
      <jdkHome>C:\path\to\jdk-25</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### Build

The project uses the [Maven Wrapper](https://maven.apache.org/wrapper/), so you do **not** need Maven installed. The wrapper automatically downloads the required Maven version on first use.

```powershell
.\mvnw compile       # compile sources and generate Javadoc
.\mvnw test          # run unit tests
.\mvnw package       # build JAR in target/lib/
.\mvnw clean         # remove build artifacts
```

### Editing

The recommended editor is [Visual Studio Code](https://code.visualstudio.com/) with the Java extensions.

1. Open the workspace file `dlx.code-workspace` in VS Code.
2. Install the recommended extensions when prompted.
3. Use the pre-configured launch configuration to run the application.

### Using

1. Open a DLX source file from `assets/examples`.
2. Choose *Compile and Load*.
3. Choose *Run*.
4. Watch the magic happen! 😉

## DLX Processor

The DLX microprocessor (pronounced: *Deluxe*) is a hypothetical processor architecture. The DLX processor uses a RISC instruction set and has 32 registers.

## Pipeline

The DLX pipeline consists of five stages:

| Stage | Name | Description |
|-------|------|-------------|
| IF | Instruction Fetch | Load the instruction into the instruction buffer; increment the program counter. |
| ID | Instruction Decode | Generate processor-internal control signals; provide operands from registers. |
| EX | Execute | ALU performs the operation; compute the effective address for load/store instructions. |
| MEM | Memory Access | Perform memory access for load/store instructions. Other instructions pass through this stage passively. |
| WB | Write Back | Write the operation result to a register. Instructions without a result pass through this stage passively. |

## Registers

| Register | Purpose |
|----------|---------|
| R0 | Always zero; immutable |
| R1 | Reserved for the assembler |
| R2–R3 | Function return values |
| R4–R7 | Function parameters |
| R8–R15 | General purpose |
| R16–R23 | Register variables |
| R24–R25 | General purpose |
| R26–R27 | Reserved for the operating system |
| R28 | Global pointer |
| R29 | Stack pointer |
| R30 | Register variable |
| R31 | Return address |

## Instruction Formats

Every DLX instruction is exactly **32 bits** wide. The different instruction formats define how those 32 bits are divided into fields. In all three formats, the first 6 bits always represent the **opcode**.

### I-Format (Immediate)

Used for load/store instructions, arithmetic instructions, and conditional/unconditional branches. The instruction contains one source register `rs1` and one destination register `rd`, plus 16 bits for the immediate value, used differently depending on the instruction type.

```
| opcode (6) | rs1 (5) | rd (5) | immediate (16) |
```

### R-Format (Register)

Used for operations on registers. Source registers `rs1` and `rs2` are used by the ALU operation `func`, and the result is written to the destination register `rd`.

```
| opcode (6) | rs1 (5) | rs2 (5) | rd (5) | unused (5) | func (6) |
```

### J-Format (Jump)

Used for jump instructions. The distance (`dist`) is simply added to the program counter.

```
| opcode (6) | distance (26) |
```

## Instruction Set

The following table lists the DLX instruction set, excluding floating-point instructions.

| Instruction | Operands | Meaning |
|-------------|----------|---------|
| `LB` / `LH` / `LW` | R1, val(R2) | Load byte / load half word / load word |
| `LBU` / `LHU` | R1, val(R2) | Load byte unsigned / load half word unsigned |
| `SB` / `SH` / `SW` | val(R2), R1 | Store byte / store half word / store word |
| `LHI` | R1, #val | Load high immediate |
| `ADD` / `SUB` | R1, R2, R3 | Add / subtract |
| `ADDU` / `SUBU` | R1, R2, R3 | Add unsigned / subtract unsigned |
| `ADDI` / `SUBI` | R1, R2, #val | Add immediate / subtract immediate |
| `ADDUI` / `SUBUI` | R1, R2, #val | Add immediate unsigned / subtract immediate unsigned |
| `AND` / `OR` / `XOR` | R1, R2, R3 | Bitwise AND / OR / exclusive OR |
| `ANDI` / `ORI` / `XORI` | R1, R2, #val | Bitwise AND / OR / XOR immediate |
| `SLL` / `SRL` / `SRA` | R1, R2, R3 | Shift left logical / shift right logical / shift right arithmetic |
| `SLLI` / `SRLI` / `SRAI` | R1, R2, #val | Shift left logical / right logical / right arithmetic - immediate |
| `SLT` / `SLE` / `SEQ` | R1, R2, R3 | Set if less than / less or equal / equal |
| `SLTI` / `SLEI` / `SEQI` | R1, R2, #val | Set if less than / less or equal / equal - immediate |
| `SGT` / `SGE` / `SNE` | R1, R2, R3 | Set if greater than / greater or equal / not equal |
| `SGTI` / `SGEI` / `SNEI` | R1, R2, #val | Set if greater than / greater or equal / not equal - immediate |
| `BEQZ` / `BNEZ` | R4, label | Branch if equal to zero / branch if not equal to zero |
| `J` | label | Unconditional jump |
| `JR` | R5 | Jump register |
| `JAL` | label | Jump and link (saves return address in R31) |
| `JALR` | R5 | Jump and link register (saves return address in R31) |
| `HALT` | n/a | Halt the processor |

**Notes:**
- `val`: 16-bit value used as an address offset or immediate value
- `label`: 16-bit or 26-bit address distance
