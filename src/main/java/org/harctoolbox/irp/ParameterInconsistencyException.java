package org.harctoolbox.irp;

public class ParameterInconsistencyException extends SignalRecognitionException {

    ParameterInconsistencyException(String name, long newValue, long oldValue) {
        super("Conflicting assignments of " + name + ", new: " + newValue + ", old: " + oldValue);
    }
}
