package name.ulbricht.dlx.simulator;

/// Listener for processing of the CPU.
@FunctionalInterface
public interface ProcessingListener {

    /// Event describing a processing step.
    ///
    /// @param cycles         the number of cycles at the step
    /// @param programCounter the program counter at the step
    /// @param halted         whether the CPU is halted after this step
    public record ProcessStep(long cycles, int programCounter, boolean halted) {
    }

    /// Called when a processing step occurs.
    /// 
    /// @param step the event describing the processing step
    void processing(ProcessStep step);
}
