package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrSignal;


interface Evaluatable {
    //public EvaluatedIrStream evaluate();

    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass);

    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass);
}
