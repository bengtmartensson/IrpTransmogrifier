/*
Copyright (C) 2017 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.irp;

import java.util.Map;
import org.antlr.v4.runtime.tree.ParseTree;

public abstract class PrimaryItem extends IrpObject implements Numerical, EquationSolving {

    public static PrimaryItem newPrimaryItem(IrpParser.Primary_itemContext ctx) {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.NameContext)
                ? new Name((IrpParser.NameContext) child)
                : (child instanceof IrpParser.NumberContext)
                ? new Number((IrpParser.NumberContext) child)
                : (child instanceof IrpParser.Para_expressionContext)
                ? Expression.newExpression((IrpParser.Para_expressionContext) child)
                : null; // error new Name(child.getText());
    }

    public static PrimaryItem newPrimaryItem(long n) {
        return new Number(n);
    }

    public static PrimaryItem newPrimaryItem(String name) throws InvalidNameException {
        try {
            return new Number(Long.parseLong(name));
        } catch (NumberFormatException ex) {
            return name.trim().startsWith("(") ? Expression.newExpression(name) : new Name(name);
        }
    }

    protected PrimaryItem(ParseTree ctx) {
        super(ctx);
    }

    @Override
    public abstract String toIrpString(int radix);

//    public abstract boolean isUnary();

    /**
     * Generate a map of the properties in the very object.
     * @param eval If true, evaluate names.
     * @param generalSpec
     * @param nameEngine
     * @return
     */
    public abstract Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine);

    /**
     * Default implementation, for non-invertible expressions.
     * @param rhs
     * @param nameEngine
     * @param bitmask
     * @return null
     * @throws org.harctoolbox.irp.NameUnassignedException
     */
    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) throws NameUnassignedException {
        return null;
    }

    /**
     * Default implementation, for non-invertible expressions.
     * @return null
     */
    @Override
    public PrimaryItem leftHandSide() {
        return null;
    }

    /**
     * Returns a PrimaryItem of the same type with all occurrences of the
     * variables in the dictionary replaced by their values in the dictionary.
     * Does not change the containing object. May return the object itself,
     * or share sub-objects with it.
     * @param constantVariables Map&lt;String, Long&gt; of variables to replace.
     * @return PrimaryItem of the same type.
     */
    public abstract PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables);

    public abstract boolean constant(NameEngine nameEngine);
}