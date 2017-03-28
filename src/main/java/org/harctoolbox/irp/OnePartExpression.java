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
import org.harctoolbox.ircore.ThisCannotHappenException;

abstract class OnePartExpression extends Expression {

    static Expression newExpression(ParseTree ctx) {
        if (ctx instanceof IrpParser.Primary_itemContext)
            return PrimaryItemExpression.newExpression((IrpParser.Primary_itemContext) ctx);
        else if (ctx instanceof IrpParser.BitfieldContext)
            return BitFieldExpression.newExpression((IrpParser.BitfieldContext) ctx);
        else
            throw new ThisCannotHappenException("Unknown OneOpExpression");
    }

    protected OnePartExpression(ParseTree ctx) {
        super(ctx);
    }

    @Override
    public final int weight() {
        return 1;
    }
}
