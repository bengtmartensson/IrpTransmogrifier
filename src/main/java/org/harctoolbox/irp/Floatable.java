package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public Element toElement(Document document) throws IrpSyntaxException;

    public String toIrpString();
}
