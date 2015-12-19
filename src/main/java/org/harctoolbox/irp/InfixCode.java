package org.harctoolbox.irp;

/**
 * Interface for classes that can produce C-like code, with infix operators.
 */
public interface InfixCode extends Numerical {
    /**
     * Generate C-like code, with infix operators.
     * @return String with C-like code for an expression.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    public String toInfixCode() throws IrpSyntaxException;
}
