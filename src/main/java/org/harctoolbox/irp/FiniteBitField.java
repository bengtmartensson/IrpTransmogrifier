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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FiniteBitField extends BitField {

    private static final Logger logger = Logger.getLogger(FiniteBitField.class.getName());

    private PrimaryItem width;
    private boolean reverse;

    public FiniteBitField(String str) {
        this((IrpParser.Finite_bitfieldContext) new ParserDriver(str).getParser().bitfield());
        this.parser = new ParserDriver(str).getParser();
        int last = parseTree.getStop().getStopIndex();
            if (last != str.length() - 1)
                logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", str.substring(0, last + 1));
    }

    public FiniteBitField(String name, int width) {
        this(name, width, false);
    }

    public FiniteBitField(String name, int width, boolean complement) {
        this.complement = complement;
        data = new Name(name);
        this.width = new Number(width);
        this.chop = new Number(0);
        this.reverse = false;
    }

    public FiniteBitField(IrpParser.Finite_bitfieldContext ctx) {
        int index = 0;
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext)) {
            complement = true;
            index++;
        }
        parseTree = ctx;
        data = PrimaryItem.newPrimaryItem(ctx.primary_item(0));
        width = PrimaryItem.newPrimaryItem(ctx.primary_item(1));
        chop = ctx.primary_item().size() > 2 ? PrimaryItem.newPrimaryItem(ctx.primary_item(2)) : PrimaryItem.newPrimaryItem(0);
        reverse = ! (ctx.getChild(index+2) instanceof IrpParser.Primary_itemContext);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FiniteBitField))
            return false;

        FiniteBitField other = (FiniteBitField) obj;
        return super.equals(obj) && (reverse == other.reverse) && width.equals(other.width);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.width);
        hash = 97 * hash + (this.reverse ? 1 : 0);
        return hash;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException {
        long x = data.toNumber(nameEngine) >> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;
        x &= ((1L << width.toNumber(nameEngine)) - 1L);
        if (reverse)
            x = IrpUtils.reverse(x, (int) width.toNumber(nameEngine));

        return x;
    }

    public String toBinaryString(NameEngine nameEngine, boolean reverse) throws UnassignedException {
        String str = toBinaryString(nameEngine);
        return reverse ? reverse(str) : str;
    }

    private String reverse(String str) {
        StringBuilder s = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++)
            s.append(str.charAt(str.length()-1-i));
        return s.toString();
    }

    public String toBinaryString(NameEngine nameEngine) throws UnassignedException {
        String str = Long.toBinaryString(toNumber(nameEngine));
        int wid = (int) width.toNumber(nameEngine);
        int len = str.length();
        if (len > wid)
            return str.substring(len - wid);

        for (int i = len; i < wid; i++)
            str = "0" + str;

        return str;
    }

    @Override
    public long getWidth(NameEngine nameEngine) throws UnassignedException {
        return width.toNumber(nameEngine);
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString = "";
        if (hasChop()) {
            try {
                chopString =Long.toString(chop.toNumber(nameEngine));
            } catch (UnassignedException ex) {
                chopString = chop.toIrpString();
            }
            chopString = ":" + chopString;
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (UnassignedException ex) {
            dataString = data.toIrpString();
        }

        String widthString;
        try {
            widthString = Long.toString(width.toNumber(nameEngine));
        } catch (UnassignedException ex) {
            widthString = width.toIrpString();
        }

        return (complement ? "~" : "") + dataString + ":" + (reverse ? "-" : "") + widthString + chopString;
    }

    @Override
    public String toIrpString() {
        return (complement ? "~" : "") + data.toIrpString() + ":" + (reverse ? "-" : "") + width.toIrpString()
                + (hasChop() ? (":" + chop.toIrpString()) : "");
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException {
        IrpUtils.entering(logger, "evaluate", this.toString());
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        if (state == pass) {
            BitStream bitStream = new BitStream(this, nameEngine, generalSpec);
            result.add(bitStream);
        }
        IrpUtils.exiting(logger, "evaluate", result);
        return result;
    }

    @Override
    public void render(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException {
        IrpUtils.entering(logger, "evaluate", this.toString());
        //EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        //if (state == pass) {
        BitStream bitStream = new BitStream(this, renderData.getNameEngine(), renderData.getGeneralSpec());
        renderData.add(bitStream);
        //}
        IrpUtils.exiting(logger, "evaluate", true);
    }

    @Override
    public void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        //recognizeData.preprocess(this, pass, bitSpecs);
        recognizeData.postprocess(this, pass, bitSpecs);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("reverse", Boolean.toString(reverse));
        element.setAttribute("complement", Boolean.toString(complement));
        Element dataElement = document.createElement("Data");
        dataElement.appendChild(data.toElement(document));
        element.appendChild(dataElement);
        Element widthElement = document.createElement("Width");
        widthElement.appendChild(width.toElement(document));
        element.appendChild(widthElement);
        Element chopElement = document.createElement("Chop");
        chopElement.appendChild(chop.toElement(document));
        element.appendChild(chopElement);
        return element;
    }

    @Override
    int numberOfBits() throws UnassignedException {
        return (int) getWidth(new NameEngine());
    }

    @Override
    int numberOfBareDurations(boolean recursive) {
        return 0;
    }

    @Override
    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecStack) throws InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException, IrpSemanticException {
        IrpUtils.entering(logger, "recognize", this);
        BitSpec bitSpec = bitSpecStack.get(bitSpecStack.size() - 1);
        int chunkSize = bitSpec.getChunkSize();
        long payload = 0L;
        int numWidth = (int) width.toNumber(recognizeData.toNameEngine());
        BitwiseParameter danglingData = recognizeData.getDanglingBitFieldData();
        if (!danglingData.isEmpty()) {
            payload = danglingData.getValue();
            numWidth -= Long.bitCount(danglingData.getBitmask());
            recognizeData.setDanglingBitFieldData();
        }
        int rest = numWidth % chunkSize;
        int noChunks = rest == 0 ? numWidth/chunkSize : numWidth/chunkSize + 1;

        for (int chunk = 0; chunk < noChunks; chunk++) {
            RecognizeData inData = null;
            int bareIrStreamNo;
            for (bareIrStreamNo = 0; bareIrStreamNo < bitSpec.size(); bareIrStreamNo++) {
                inData = recognizeData.clone();
                List<BitSpec> poppedStack = new ArrayList<>(bitSpecStack);
                poppedStack.remove(poppedStack.size()-1);

                try {
                    bitSpec.get(bareIrStreamNo).traverse(inData, pass, poppedStack);
                    // match!
                    break;
                } catch (IrpSignalParseException ex) {
                    // No match
                }
            }
            assert(inData != null);

            if (bareIrStreamNo == bitSpec.size())
                throw new IrpSignalParseException("FiniteBitField did not parse");

            recognizeData.setPosition(inData.getPosition());
            recognizeData.setHasConsumed(inData.getHasConsumed());
            payload = ((payload << (long) chunkSize)) | (long) bareIrStreamNo;
        }

        if (rest != 0) {
            // this has been tested only with bitorder = msb.
            int bitsToStore = chunkSize - rest;
            int bitmask = IrCoreUtils.ones(bitsToStore);
            recognizeData.setDanglingBitFieldData(payload, bitmask);
            payload >>= bitsToStore;
        }
        if (this.reverse ^ recognizeData.getGeneralSpec().getBitDirection() == BitDirection.lsb)
            payload = IrCoreUtils.reverse(payload, noChunks);
        if (this.complement)
            payload = ~payload;
        payload <<= (int) chop.toNumber(recognizeData.toNameEngine());
        long bitmask = IrCoreUtils.ones(width.toNumber(recognizeData.toNameEngine())) << chop.toNumber(recognizeData.toNameEngine());
        payload &= bitmask;
        Name name = data.toName();
        if (name != null) {
            logger.log(Level.FINE, "Assignment: {0}={1}&{2}", new Object[]{data.toIrpString(), payload, bitmask});
            if (data.isUnary())
                recognizeData.add(name.toString(), payload, bitmask);
            else
                recognizeData.add(name.toString(), data.invert(payload));
        } else {
            long expected = this.toNumber(recognizeData.getParameterCollector().toNameEngine()); // FIXME
            if (expected != payload)
                throw new IrpSignalParseException("FiniteBitField did not evaluated to expected value");//return false;
        }

        IrpUtils.exiting(logger, "recognize", payload);
    }

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        return true; // ????
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        return true; // ????
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return gapFlashBitSpecs ? DurationType.flash : DurationType.gap;
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return gapFlashBitSpecs ? DurationType.gap : DurationType.flash;
    }

    @Override
    public int weight() {
        return super.weight() + width.weight();
    }

    @Override
    public Set<String> assignmentVariables() {
        return new HashSet<>(0);
    }

    @Override
    public Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        //ItemCodeGenerator itemCodeGenerator = codeGenerator.newItemCodeGenerator(this);
        Map<String, Object> map = super.propertiesMap(state, pass, generalSpec, nameEngine);
        map.put("width", width.propertiesMap(true, generalSpec, nameEngine));
        map.put("reverse", reverse);
        return map;
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        //Map<String, Object> map = propertiesMap(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine);
        Map<String, Object> map = super.propertiesMap(eval, generalSpec, nameEngine);
        map.put("width", width.propertiesMap(true, generalSpec, nameEngine));
        map.put("reverse", reverse);
        if (eval)
            map.put("kind", "FiniteBitFieldExpression");
        return map;
    }
}
