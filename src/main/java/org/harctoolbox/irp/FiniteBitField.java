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

import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 *
 */
public class FiniteBitField extends BitField {
    private PrimaryItem width;
    private boolean reverse;

    public FiniteBitField(String str) throws IrpSyntaxException {
        this((IrpParser.Finite_bitfieldContext) new ParserDriver(str).getParser().bitfield());
    }

    public FiniteBitField(IrpParser.Finite_bitfieldContext ctx) throws IrpSyntaxException {
        int index = 0;
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext)) {
            complement = true;
            index++;
        }
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        width = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
        chop = ctx.primary_item().size() > 2 ? PrimaryItem.newPrimaryItem(ctx.primary_item(2)) : PrimaryItem.newPrimaryItem(0);
        reverse = ! (ctx.getChild(index+2) instanceof IrpParser.Primary_itemContext);
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long x = data.toNumber(nameEngine) >> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;
        x &= ((1L << width.toNumber(nameEngine)) - 1L);
        if (reverse)
            x = IrpUtils.reverse(x, (int) width.toNumber(nameEngine));

        return x;
    }

    @Override
    public long getWidth(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return width.toNumber(nameEngine);
    }

    public String toString(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return (complement ? "~" : "") + data.toNumber(nameEngine) + ":" + (reverse ? "-" : "") + width.toNumber(nameEngine) + ":" + chop.toNumber(nameEngine);
    }
}
