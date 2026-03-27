package name.ulbricht.dlx.config;

/// Preset choices for the simulated memory size in bytes.
public enum MemorySize {

    /// 512 bytes of simulated memory.
    TINY(512),

    /// 1024 bytes (1 KB) of simulated memory — the default preset.
    SMALL(1024),

    /// 2048 bytes (2 KB) of simulated memory.
    MEDIUM(2048),

    /// 4096 bytes (4 KB) of simulated memory.
    LARGE(4096);

    private final int sizeInBytes;

    MemorySize(final int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    /// {@return the memory size in bytes associated with this preset}
    public int sizeInBytes() {
        return this.sizeInBytes;
    }
}
