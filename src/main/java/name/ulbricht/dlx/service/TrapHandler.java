package name.ulbricht.dlx.service;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.simulator.ReadOnlyMemory;
import name.ulbricht.dlx.simulator.TrapListener;

/// Bridges non-halt trap instructions to the [Console] service.
///
/// Supported trap numbers:
///
/// - **1** - Print integer: writes the signed decimal value of R1.
/// - **2** - Print character: writes the character whose code is in R1.
/// - **3** - Print string: writes a null-terminated string starting at the
///   memory address in R1.
///
/// Unknown trap numbers are silently ignored.
public final class TrapHandler implements TrapListener {

    private static final int TRAP_PRINT_INT = 1;
    private static final int TRAP_PRINT_CHAR = 2;
    private static final int TRAP_PRINT_STRING = 3;

    private final Console console;

    /// Creates a handler that writes trap output to the given console.
    ///
    /// @param console the console to write to; must not be `null`
    public TrapHandler(final Console console) {
        this.console = requireNonNull(console, "console must not be null");
    }

    @Override
    public void trapRetired(final TrapEvent event) {
        final var r1 = event.registers().read(1);
        switch (event.trapNumber()) {
            case TRAP_PRINT_INT -> this.console.append(Integer.toString(r1));
            case TRAP_PRINT_CHAR -> this.console.append(String.valueOf((char) (r1 & 0xFFFF)));
            case TRAP_PRINT_STRING -> this.console.append(readString(event.memory(), r1));
            default -> { /* unknown trap — ignored */ }
        }
    }

    private static String readString(final ReadOnlyMemory memory, final int startAddr) {
        final var sb = new StringBuilder();
        var addr = startAddr;
        while (true) {
            final var b = memory.loadByteU(addr);
            if (b == 0)
                break;
            sb.append((char) b);
            addr++;
        }
        return sb.toString();
    }
}
