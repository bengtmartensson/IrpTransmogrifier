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

import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.ThisCannotHappenException;

abstract class OnePartExpression extends Expression {

    @SuppressWarnings("null")
    public static Expression newExpression(ParseTree ctx, ParseTree child) {
        Objects.requireNonNull(child);
        if (child instanceof IrpParser.Primary_itemContext)
            return PrimaryItemExpression.newExpression(ctx, (IrpParser.Primary_itemContext) child);
        else if (child instanceof IrpParser.BitfieldContext)
            return BitFieldExpression.newExpression(ctx, (IrpParser.BitfieldContext) child);
        else
            throw new ThisCannotHappenException("Unknown OneOpExpression" + child.getClass());
    }

    protected OnePartExpression(ParseTree ctx) {
        super(ctx);
    }

    @Override
    public final int weight() {
        return 1;
    }
}
