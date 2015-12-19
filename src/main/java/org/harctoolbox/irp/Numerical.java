package org.harctoolbox.irp;

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
     */
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException;
}
