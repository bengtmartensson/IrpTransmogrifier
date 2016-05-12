package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * Interface for classes that can produce a numerical (long) result.
 */
public interface Numerical {
    /**
     * Produces a numerical result.
     * @param nameEngine used to resolve names in expressions.
     * @return result, long.
     * @throws UnassignedException
     * @throws IrpSyntaxException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     */
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException;
}
