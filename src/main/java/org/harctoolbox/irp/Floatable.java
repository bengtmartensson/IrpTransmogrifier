package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * Interface for classes that can produce an floating point number.
 */
public interface Floatable {
    /**
     * Produces a floating result.
     * @param nameEngine
     * @param generalSpec
     * @return Floating representation of class.
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws ArithmeticException
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException;
}
