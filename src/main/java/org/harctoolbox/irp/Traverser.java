package org.harctoolbox.irp;

import java.util.List;
import org.harctoolbox.ircore.IrSignal;

public abstract class Traverser {

    private IrSignal.Pass state;

    protected Traverser(IrSignal.Pass state) {
        this.state = state;
    }

    public abstract void preprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException;

    public abstract void postprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException;

    /**
     * @return the state
     */
    public IrSignal.Pass getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(IrSignal.Pass state) {
        this.state = state;
    }

    boolean isFinished() {
        return true;
    }
}
