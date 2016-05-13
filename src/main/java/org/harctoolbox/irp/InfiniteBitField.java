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
public class InfiniteBitField extends BitField {



    public InfiniteBitField(String str) throws IrpSyntaxException {
        this((IrpParser.Infinite_bitfieldContext) (new ParserDriver(str)).getParser().bitfield());
    }

    public InfiniteBitField(IrpParser.Infinite_bitfieldContext ctx) throws IrpSyntaxException {
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext))
            complement = true;
        data = new PrimaryItem(ctx.primary_item(0));
        chop = new PrimaryItem(ctx.primary_item(2));
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long x = data.toNumber(nameEngine) >> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;
        //x &= ((1L << width.toNumber(nameEngine)) - 1L);
        //if (reverse)
        //    x = IrpUtils.reverse(x, maxWidth);

        return x;
    }

    @Override
    public long getWidth(NameEngine nameEngine) {
        return maxWidth;
    }

    @Override
    public String toString(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return (complement ? "~" : "") + data.toNumber(nameEngine) + "::" + chop.toNumber(nameEngine);
    }
}
