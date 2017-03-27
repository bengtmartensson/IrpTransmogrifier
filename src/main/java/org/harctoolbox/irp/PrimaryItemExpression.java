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

import org.antlr.v4.runtime.tree.ParseTree;

abstract class PrimaryItemExpression extends OnePartExpression {

    static Expression newExpression(IrpParser.Primary_itemContext ctx) throws IrpException {
        ParseTree child = ctx.getChild(0);
        if (child instanceof IrpParser.NameContext)
            return NameExpression.newExpression((IrpParser.NameContext) child);
        else if (child instanceof IrpParser.NumberContext)
            return NumberExpression.newExpression((IrpParser.NumberContext) child);
        else if (child instanceof IrpParser.Para_expressionContext)
            return Expression.newExpression((IrpParser.Para_expressionContext) child);
        else
            throw new IrpSemanticException("Unknown PrimaryItemExpression");
    }

    protected PrimaryItemExpression(ParseTree ctx) {
        super(ctx);
    }
}
