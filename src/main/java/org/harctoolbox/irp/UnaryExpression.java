/*
Copyright (C) 2015 Bengt Martensson.

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
 * This class
 */
public class UnaryExpression implements Numerical {

    private final static String bitCountName = "bitCount";
    //private final BitField bitField;
    //private final PrimaryItem primaryItem;
    private final Numerical thing;
    private final boolean negate;
    private final boolean count;

    public UnaryExpression(IrpParser.Unary_expressionContext ctx) throws IrpSyntaxException {
        if (ctx instanceof IrpParser.Bitfield_expressionContext) {
            thing = BitField.newBitField(((IrpParser.Bitfield_expressionContext)ctx).bitfield());
            //primaryItem = null;
            negate = false;
            count = false;
        } else if (ctx instanceof IrpParser.Primary_item_expressionContext) {
            //bitField = null;
            thing = new PrimaryItem(((IrpParser.Primary_item_expressionContext)ctx).primary_item());
            negate = false;
            count = false;
        } else if (ctx instanceof IrpParser.Minus_bitfield_expressonContext) {
            thing = BitField.newBitField(((IrpParser.Minus_bitfield_expressonContext)ctx).bitfield());
            //primaryItem = null;
            negate = true;
            count = false;
        } else if (ctx instanceof IrpParser.Minus_primary_item_expressionContext) {
            //bitField = null;
            thing = new PrimaryItem(((IrpParser.Minus_primary_item_expressionContext)ctx).primary_item());
            negate = true;
            count = false;
        } else if (ctx instanceof IrpParser.Count_bitfield_expressionContext) {
            thing = BitField.newBitField(((IrpParser.Count_bitfield_expressionContext)ctx).bitfield());
            //primaryItem = null;
            negate = false;
            count = true;
        } else if (ctx instanceof IrpParser.Count_primary_item_expressionContext) {
            //bitField = null;
            thing = new PrimaryItem(((IrpParser.Count_primary_item_expressionContext)ctx).primary_item());
            negate = false;
            count = true;
        } else {
            throw new RuntimeException("This cannot happen");
        }
    }

//    @Override
//    public String toInfixCode() throws IrpSyntaxException {
//        String code = thing.toInfixCode();
//        return negate ? "-(" + code + ")"
//                : count ? bitCountName + "(" + code + ")"
//                : code;
//    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long x = thing.toNumber(nameEngine);
        return negate ? -x
                : count ? (long) Long.bitCount(x)
                : x;
    }
}
