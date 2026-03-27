package name.ulbricht.dlx.config;

import java.time.Duration;

/// Preset choices for the simulated CPU cycle duration.
public enum ProcessorSpeed {

    /// No delay — the simulation runs at full host speed.
    OFF(Duration.ZERO),

    /// 250 milliseconds per cycle.
    FAST(Duration.ofMillis(250)),

    /// 500 milliseconds per cycle.
    MEDIUM(Duration.ofMillis(500)),

    /// 1 second per cycle — the default preset.
    SLOW(Duration.ofSeconds(1));

    private final Duration duration;

    ProcessorSpeed(final Duration duration) {
        this.duration = duration;
    }

    /// {@return the cycle duration associated with this preset}
    public Duration duration() {
        return this.duration;
    }
}
