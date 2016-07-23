package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrSignal;


interface Evaluatable {
    //public EvaluatedIrStream evaluate();

    public IrSignal.Pass stateWhenEntering();

    public IrSignal.Pass stateWhenExiting();
}
