package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener for processing of the CPU.
@FunctionalInterface
public interface ProcessingListener {

    /// Event describing a processing step.
    ///
    /// @param cycles         the number of cycles at the step
    /// @param programCounter the program counter at the step
    /// @param halted         whether the CPU is halted after this step
    /// @param pipeline       immutable snapshot of all four pipeline latches at
    ///                       this step
    public record ProcessStep(long cycles, int programCounter, boolean halted,
            CPU.PipelineSnapshot pipeline) {

        /// Validates that `pipeline` is not `null`.
        ///
        /// @throws NullPointerException if `pipeline` is `null`
        public ProcessStep {
            requireNonNull(pipeline, "pipeline must not be null");
        }
    }

    /// Called when a processing step occurs.
    /// 
    /// @param step the event describing the processing step
    void processing(ProcessStep step);
}
