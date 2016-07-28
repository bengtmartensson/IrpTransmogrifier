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
public class InfiniteBitField extends BitField {

    public InfiniteBitField(String str) throws IrpSyntaxException {
        this((IrpParser.Infinite_bitfieldContext) (new ParserDriver(str)).getParser().bitfield());
    }

    public InfiniteBitField(IrpParser.Infinite_bitfieldContext ctx) throws IrpSyntaxException {
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext))
            complement = true;
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        chop = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long x = data.toNumber(nameEngine) >>> chop.toNumber(nameEngine);
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
    public String toString(NameEngine nameEngine) {
        String chopString;
        try {
            chopString = Long.toString(chop.toNumber(nameEngine));
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            chopString = chop.toIrpString();
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            dataString = data.toIrpString();
        }

        return (complement ? "~" : "") + dataString + "::" + chopString;
    }

    @Override
    public String toIrpString() {
        return (complement ? "~" : "") + data.toIrpString() + "::" + chop.toIrpString();
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("infinite_bitfield");
        //element.setAttribute("reverse", Boolean.toString(reverse));
        element.setAttribute("complement", Boolean.toString(complement));
        Element dataElement = document.createElement("data");
        dataElement.appendChild(data.toElement(document));
        element.appendChild(dataElement);
        //Element widthElement = document.createElement("width");
        //widthElement.appendChild(width.toElement(document));
        //element.appendChild(widthElement);
        if (!(chop instanceof Number && ((Number) chop).data == 0)) {
            Element chopElement = document.createElement("chop");
            chopElement.appendChild(chop.toElement(document));
            element.appendChild(chopElement);
        }
        return element;
    }

    @Override
    int numberOfBits() {
        return -99999;
    }

    @Override
    int numberOfBareDurations() {
        return -99999;
    }
}
