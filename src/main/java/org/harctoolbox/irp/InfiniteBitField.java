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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class InfiniteBitField extends BitField {

    public InfiniteBitField(String str) {
        this(new ParserDriver(str));
    }

    public InfiniteBitField(ParserDriver parserDriver) {
        this((IrpParser.Infinite_bitfieldContext) parserDriver.getParser().bitfield());
    }

    public InfiniteBitField(IrpParser.Infinite_bitfieldContext ctx) {
        super(ctx);
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext))
            complement = true;
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        chop = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws NameUnassignedException {
        long x = data.toNumber(nameEngine) >>> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;

        return x;
    }

    @Override
    public long getWidth(NameEngine nameEngine) {
        return MAXWIDTH;
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString;
        try {
            chopString = Long.toString(chop.toNumber(nameEngine));
        } catch (NameUnassignedException ex) {
            chopString = chop.toIrpString(10);
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (NameUnassignedException ex) {
            dataString = data.toIrpString(10);
        }

        return (complement ? "~" : "") + dataString + "::" + chopString;
    }

    @Override
    public String toIrpString(int radix) {
        return (complement ? "~" : "") + data.toIrpString(radix) + "::" + chop.toIrpString(10);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("complement", Boolean.toString(complement));
        Element dataElement = document.createElement("Data");
        dataElement.appendChild(data.toElement(document));
        element.appendChild(dataElement);
        if (!(chop instanceof Number && ((Number) chop).toNumber() == 0)) {
            Element chopElement = document.createElement("Chop");
            chopElement.appendChild(chop.toElement(document));
            element.appendChild(chopElement);
        }
        return element;
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        //Map<String, Object> map = propertiesMap(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine);
        Map<String, Object> map = super.propertiesMap(eval, generalSpec, nameEngine);
        map.put("kind", "InfiniteBitFieldExpression");
        return map;
    }

    @Override
    public Integer numberOfBits() {
        return 0;
    }
}
