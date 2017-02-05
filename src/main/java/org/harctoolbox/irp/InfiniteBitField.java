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

public class InfiniteBitField extends BitField {

    public InfiniteBitField(String str) {
        this((IrpParser.Infinite_bitfieldContext) (new ParserDriver(str)).getParser().bitfield());
    }

    public InfiniteBitField(IrpParser.Infinite_bitfieldContext ctx) {
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext))
            complement = true;
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        chop = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException {
        long x = data.toNumber(nameEngine) >>> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;

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
        } catch (UnassignedException ex) {
            chopString = chop.toIrpString();
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (UnassignedException ex) {
            dataString = data.toIrpString();
        }

        return (complement ? "~" : "") + dataString + "::" + chopString;
    }

    @Override
    public String toIrpString() {
        return (complement ? "~" : "") + data.toIrpString() + "::" + chop.toIrpString();
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

//    @Override
//    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) {
//        throw new ThisCannotHappenException("Not supported.");
//    }
//
//    @Override
//    public void render(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
//        throw new UnsupportedOperationException("Not supported.");
//    }
//
//    @Override
//    public Set<String> assignmentVariables() {
//        return new HashSet<>(0);
//    }

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
