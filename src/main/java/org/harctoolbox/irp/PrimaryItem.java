/*
Copyright (C) 2016 Bengt Martensson.

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

/**
 *
 */
public abstract class PrimaryItem extends IrpObject implements Numerical {

    public static PrimaryItem newPrimaryItem(IrpParser.Primary_itemContext ctx) {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.NameContext)
                ? new Name((IrpParser.NameContext) child)
                : (child instanceof IrpParser.NumberContext)
                ? new Number((IrpParser.NumberContext) child)
                : (child instanceof IrpParser.Para_expressionContext)
                ? new Expression((IrpParser.Para_expressionContext) child)
                : new Name(child.getText());
    }

    public static PrimaryItem newPrimaryItem(long n) {
        return new Number(n);
    }

    public static PrimaryItem newPrimaryItem(String name) {
        try {
            return new Number(Long.parseLong(name));
        } catch (NumberFormatException ex) {
        }
        return name.trim().startsWith("(") ? new Expression(name) : new Name(name);
    }
    protected PrimaryItem() {
    }

    public abstract Name toName();

    public abstract long invert(long rhs) throws UnassignedException;

    public abstract String toIrpString(int radix);

    public abstract boolean isUnary();

    //public abstract String code(boolean eval, CodeGenerator codeGenerator);

    public abstract Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine);

    protected Map<String, Object> propertiesMap(int noProps) {
        return IrpUtils.propertiesMap(noProps, this);
    }
}