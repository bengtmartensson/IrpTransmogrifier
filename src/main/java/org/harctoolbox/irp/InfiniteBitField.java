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

    public static long toLong(long data, long chop, boolean complement) {
        return toLong(data, MAXWIDTH, chop, complement, false);
    }

    public InfiniteBitField(String str) {
        this(new ParserDriver(str));
    }

    public InfiniteBitField(ParserDriver parserDriver) {
        this((IrpParser.Infinite_bitfieldContext) parserDriver.getParser().bitfield());
    }

    public InfiniteBitField(IrpParser.Infinite_bitfieldContext ctx) {
        super(ctx);
        complement = ! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext);
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        chop = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
    }

    private InfiniteBitField(PrimaryItem data, PrimaryItem chop, boolean complement) {
        super(null);
        this.data = data;
        this.chop = chop;
        this.complement = complement;
    }

    @Override
    public InfiniteBitField substituteConstantVariables(Map<String, Long> constantVariables) {
        return new InfiniteBitField(data.substituteConstantVariables(constantVariables),
                chop.substituteConstantVariables(constantVariables), complement);
    }

    @Override
    public long toLong(NameEngine nameEngine) throws NameUnassignedException {
        return toLong(data.toLong(nameEngine), chop.toLong(nameEngine), complement);
    }

    @Override
    public BitwiseParameter toBitwiseParameter(RecognizeData recognizeData) {
        BitwiseParameter payload = this.data.toBitwiseParameter(recognizeData);
        if (payload == null)
            return new BitwiseParameter();
        long ch = chop.toBitwiseParameter(recognizeData).longValueExact();
        long value = toLong(payload.getValue(), ch, complement);
        return new BitwiseParameter(value, payload.getBitmask() >> ch);
    }

    @Override
    public long getWidth(NameEngine nameEngine) {
        return MAXWIDTH;
    }

    @Override
    protected BitwiseParameter getWidth(RecognizeData nameResolver) throws NameUnassignedException {
        return new BitwiseParameter(MAXWIDTH);
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString;
        try {
            chopString = Long.toString(chop.toLong(nameEngine));
        } catch (NameUnassignedException ex) {
            chopString = chop.toIrpString(10);
        }

        String dataString;
        try {
            dataString = Long.toString(data.toLong(nameEngine));
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
        if (!(chop instanceof Number && ((Number) chop).toLong() == 0)) {
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

    @Override
    public BitwiseParameter invert(BitwiseParameter rhs, RecognizeData recognizeData/*, long oldBitmask*/) throws NameUnassignedException {
        long ch = getChop(recognizeData.getNameEngine());
        long payload = rhs.getValue();
        if (complement)
            payload = ~payload;
        long bitmask = rhs.getBitmask() << ch;
        payload <<= ch;
        return new BitwiseParameter(payload, bitmask);
    }
}
