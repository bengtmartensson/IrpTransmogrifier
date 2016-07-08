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
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    @Override
    public String toString(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return (complement ? "~" : "") + data.toNumber(nameEngine) + ":" + (reverse ? "-" : "") + width.toNumber(nameEngine) + ":" + chop.toNumber(nameEngine);
    }

    @Override
    EvaluatedIrStream evaluate(NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec, IrSignal.Pass pass, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("finite_bitfield");
        element.setAttribute("reverse", Boolean.toString(reverse));
        element.setAttribute("compliment", Boolean.toString(complement));
        Element dataElement = document.createElement("data");
        dataElement.appendChild(data.toElement(document));
        element.appendChild(dataElement);
        Element widthElement = document.createElement("width");
        widthElement.appendChild(width.toElement(document));
        element.appendChild(widthElement);
        if (!(chop instanceof Number && ((Number) chop).data == 0)) {
            Element chopElement = document.createElement("chop");
            chopElement.appendChild(chop.toElement(document));
            element.appendChild(chopElement);
        }
        return element;
    }

    @Override
    int numberOfBits() {
        try {
            return (int) getWidth(new NameEngine());
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return -99999;
        }
    }

    @Override
    int numberOfBareDurations() {
        return 0;
    }
}
