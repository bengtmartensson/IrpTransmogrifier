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
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class FiniteBitField extends BitField implements IrStreamItem {

    private static final Logger logger = Logger.getLogger(FiniteBitField.class.getName());

    public static FiniteBitField newFiniteBitField(IrpParser.Finite_bitfieldContext ctx) {
        return new FiniteBitField(ctx);
        //instance.parseTree = ctx;
        //return instance;
    }

//    public static Expression newExpression(IrpParser.Finite_bitfieldContext ctx) throws IrpSemanticException {
//        return FiniteBitFieldExpression.newExpression(newFiniteBitField(ctx));
//    }

    private PrimaryItem width;
    private boolean reverse;

    public FiniteBitField(String str) {
        this(new ParserDriver(str));
    }

    private FiniteBitField(ParserDriver parserDriver) {
        this((IrpParser.Finite_bitfieldContext) parserDriver.getParser().bitfield());
//        this.parser = new ParserDriver(str).getParser();
//        int last = parseTree.getStop().getStopIndex();
//            if (last != str.length() - 1)
//                logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", str.substring(0, last + 1));
    }

    public FiniteBitField(String name, long width) throws InvalidNameException {
        this(name, width, false);
    }

    public FiniteBitField(String name, long width, boolean complement) throws InvalidNameException {
        super(null);
        this.complement = complement;
        data = new Name(name);
        this.width = new Number(width);
        this.chop = new Number(0);
        this.reverse = false;
    }

    public FiniteBitField(IrpParser.Finite_bitfieldContext ctx) {
        super(ctx);
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
    public long toNumber(NameEngine nameEngine) throws NameUnassignedException {
        long x = data.toNumber(nameEngine) >> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;
        x &= ((1L << width.toNumber(nameEngine)) - 1L);
        if (reverse)
            x = IrpUtils.reverse(x, (int) width.toNumber(nameEngine));

        return x;
    }

    public String toBinaryString(NameEngine nameEngine, boolean reverse) throws NameUnassignedException {
        String str = toBinaryString(nameEngine);
        return reverse ? reverse(str) : str;
    }

    private String reverse(String str) {
        StringBuilder s = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++)
            s.append(str.charAt(str.length()-1-i));
        return s.toString();
    }

    public String toBinaryString(NameEngine nameEngine) throws NameUnassignedException {
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
    public long getWidth(NameEngine nameEngine) throws NameUnassignedException {
        return width.toNumber(nameEngine);
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString = "";
        if (hasChop()) {
            try {
                chopString =Long.toString(chop.toNumber(nameEngine));
            } catch (NameUnassignedException ex) {
                chopString = chop.toIrpString(10);
            }
            chopString = ":" + chopString;
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (NameUnassignedException ex) {
            dataString = data.toIrpString(10);
        }

        String widthString;
        try {
            widthString = Long.toString(width.toNumber(nameEngine));
        } catch (NameUnassignedException ex) {
            widthString = width.toIrpString(10);
        }

        return (complement ? "~" : "") + dataString + ":" + (reverse ? "-" : "") + widthString + chopString;
    }

    @Override
    public String toIrpString(int radix) {
        return (complement ? "~" : "") + data.toIrpString(radix) + ":" + (reverse ? "-" : "") + width.toIrpString(10)
                + (hasChop() ? (":" + chop.toIrpString(10)) : "");
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) throws NameUnassignedException {
        BitStream bitStream = new BitStream(this, renderData.getGeneralSpec(), renderData.getNameEngine());
        renderData.add(bitStream);
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) throws NameUnassignedException {
        BitStream bitStream = new BitStream(this, renderData.getGeneralSpec(), renderData.getNameEngine());
        renderData.add(bitStream);
    }

    @Override
    public List<IrStreamItem> extractPass(IrSignal.Pass pass, IrSignal.Pass state) {
        return IrpUtils.mkIrStreamItemList(this);
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
    public Integer numberOfBits() {
        try {
            return (int) getWidth(NameEngine.empty);
        } catch (NameUnassignedException ex) {
            return null;
        }
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack) throws SignalRecognitionException {
        try {
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
            int noChunks = rest == 0 ? numWidth / chunkSize : numWidth / chunkSize + 1;

            for (int chunk = 0; chunk < noChunks; chunk++) {
                RecognizeData inData = null;
                int bareIrStreamNo;
                for (bareIrStreamNo = 0; bareIrStreamNo < bitSpec.size(); bareIrStreamNo++) {
                    inData = recognizeData.clone();
                    List<BitSpec> poppedStack = new ArrayList<>(bitSpecStack);
                    poppedStack.remove(poppedStack.size()-1);

                    try {
                        bitSpec.get(bareIrStreamNo).decode(inData, poppedStack);
                        // match!
                        break;
                    } catch (SignalRecognitionException ex) {
                        // No match, just try the next one
                    }
                }
                assert(inData != null);

                if (bareIrStreamNo == bitSpec.size())
                    throw new SignalRecognitionException("FiniteBitField did not parse");

                recognizeData.setPosition(inData.getPosition());
                recognizeData.setHasConsumed(inData.getHasConsumed());
                payload = ((payload << (long) chunkSize)) | (long) bareIrStreamNo;
                recognizeData.getNameEngine().add(inData.getNameEngine());
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

            // We now have the "equation" data == payload, we are turning that into an assignment
            PrimaryItem expression = data;
            Long rhs = payload;

            NameEngine nameEngine = recognizeData.toNameEngine();
            while (expression != null && rhs != null && !((expression instanceof Name) || (expression instanceof Number))) {
                rhs = expression.invert(rhs, nameEngine, bitmask);
                //rhs &= bitmask;
                expression = expression.leftHandSide();
            }

            if (expression != null && rhs != null) {
                // equation solving succeeded!
                if (expression instanceof Name) {
                    // perform assignment

                    Name name = (Name) expression;
                    logger.log(Level.FINE, "Assignment: {0}={1}&{2}", new Object[]{data.toIrpString(10), payload, bitmask});
                    if (data instanceof Name)
                        recognizeData.add(name.toString(), rhs, bitmask);
                    else
                        recognizeData.add(name.toString(), rhs);
                } else if (expression instanceof Number) {
                    // check, barf if not OK
                    long expected = this.toNumber(recognizeData.getParameterCollector().toNameEngine());
                    if (expected != rhs)
                        throw new SignalRecognitionException("Constant FiniteBitField did not evaluated to expected value");
                } else
                    throw new ThisCannotHappenException();
            } else {
                logger.log(Level.WARNING, "Unsolvable equation Assignment: {0}={1}&{2}", new Object[]{data.toIrpString(10), payload, bitmask});
            }
        } catch (NameUnassignedException ex) {
            throw new SignalRecognitionException(ex);
        }
    }

    @Override
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return true; // ????
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
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
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        //ItemCodeGenerator itemCodeGenerator = codeGenerator.newItemCodeGenerator(this);
        Map<String, Object> map = super.propertiesMap(false/*state, pass*/, generalSpec, nameEngine);
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

    @Override
    public Integer numberOfDurations() {
        return null;
    }

    @Override
    public boolean nonConstantBitFieldLength() {
        try {
            width.toNumber();
        } catch (NameUnassignedException ex) {
            return true;
        }
        return false;
    }
}
