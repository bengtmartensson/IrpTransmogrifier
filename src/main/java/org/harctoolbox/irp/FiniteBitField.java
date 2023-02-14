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
import java.util.TreeSet;
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
    }

    private PrimaryItem width;
    private boolean reverse;

    public FiniteBitField(String str) {
        this(new ParserDriver(str));
    }

    private FiniteBitField(ParserDriver parserDriver) {
        this((IrpParser.Finite_bitfieldContext) parserDriver.getParser().bitfield());
    }

    public FiniteBitField(String name, long width) throws InvalidNameException {
        this(name, width, false);
    }

    public FiniteBitField(String name, long width, boolean complement) throws InvalidNameException {
        this(new Name(name), new Number(width), new Number(0), complement, false);
    }

    public FiniteBitField(long data, long width, long chop, boolean complement, boolean reverse) throws InvalidNameException {
        this(new Number(data), new Number(width), new Number(chop), complement, reverse);
    }

    private FiniteBitField(PrimaryItem data, PrimaryItem width, PrimaryItem chop, boolean complement, boolean reverse) throws InvalidNameException {
        super(null);
        this.data = data;
        this.width = width;
        this.chop = chop;
        this.complement = complement;
        this.reverse = reverse;
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
    public FiniteBitField substituteConstantVariables(Map<String, Long> constantVariables) {
        try {
            return new FiniteBitField(data.substituteConstantVariables(constantVariables),
                    width.substituteConstantVariables(constantVariables),
                    chop.substituteConstantVariables(constantVariables), complement, reverse);
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
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
    public long toLong(NameEngine nameResolver) throws NameUnassignedException {
        return toLong(data.toLong(nameResolver), width.toLong(nameResolver), chop.toLong(nameResolver), complement, reverse);
    }

    @Override
    public BitwiseParameter toBitwiseParameter(RecognizeData nameResolver) {
        BitwiseParameter payload = this.data.toBitwiseParameter(nameResolver);
        if (payload == null)
            return BitwiseParameter.NULL;
        long wid = width.toBitwiseParameter(nameResolver).longValueExact();
        long ch = chop.toBitwiseParameter(nameResolver).longValueExact();
        long value = toLong(payload.getValue(), wid, ch, complement, reverse);
        long bitmap = IrCoreUtils.ones(wid) & (payload.getBitmask() >> ch);
        return new BitwiseParameter(value, bitmap);
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
        String str = Long.toBinaryString(toLong(nameEngine));
        int wid = (int) getWidth(nameEngine);
        int len = str.length();
        if (len > wid)
            return str.substring(len - wid);

        for (int i = len; i < wid; i++)
            str = "0" + str;

        return str;
    }

    @Override
    public long getWidth(NameEngine nameEngine) throws NameUnassignedException {
        long wid = width.toLong(nameEngine);
        if (wid > MAXWIDTH) {
            wid = MAXWIDTH;
        }
        return wid;
    }

    @Override
    public BitwiseParameter getWidth(RecognizeData recognizeData) {
        return width.toBitwiseParameter(recognizeData);
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString = "";
        if (hasChop()) {
            try {
                chopString =Long.toString(chop.toLong(nameEngine));
            } catch (NameUnassignedException ex) {
                chopString = chop.toIrpString(10);
            }
            chopString = ":" + chopString;
        }

        String dataString;
        try {
            dataString = Long.toString(data.toLong(nameEngine));
        } catch (NameUnassignedException ex) {
            dataString = data.toIrpString(10);
        }

        String widthString;
        try {
            widthString = Long.toString(getWidth(nameEngine));
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
            return (int) getWidth(NameEngine.EMPTY);
        } catch (NameUnassignedException ex) {
            return null;
        }
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack, boolean isLast) throws SignalRecognitionException {
        logger.log(recognizeData.logRecordEnter(this));
        try {
            long payload = collectData(recognizeData, bitSpecStack);

            // Can the data be computed with already present data?
            boolean success = isChecksum(recognizeData, payload);
            if (success) {
                logger.log(recognizeData.logRecordExit(this));
                return;
            }

            // no, it is a parameter assignment,
            // We now have the "equation" data == payload, we are turning that into an assignment
            //PrimaryItem expression = data;
            //Long rhs = payload;

            Equation equation = new Equation(this, payload, width.toLong(recognizeData.getNameEngine()), recognizeData);
            String origEquation = equation.toString();
            boolean solved = equation.solve();
            if (!solved) {
                throw new SignalRecognitionException("Could not solve equation: " + origEquation);
            }
            recognizeData.add(equation.getName(), equation.getValue());

            solved = equation.expandLhsSolve();
            if (solved)
                recognizeData.add(equation.getName(), equation.getValue());
        } catch (NameUnassignedException ex) {
            throw new SignalRecognitionException(ex);
        }
    }

    private boolean isChecksum(RecognizeData recognizeData, long payload) throws SignalRecognitionException {
        BitwiseParameter expected = this.toBitwiseParameter(recognizeData);
        return expected.check(payload, assignmentNeededBitmask(recognizeData));
    }

    private long collectData(RecognizeData recognizeData, List<BitSpec> bitSpecStack) throws SignalRecognitionException, NameUnassignedException {
        BitSpec bitSpec = bitSpecStack.get(bitSpecStack.size() - 1);
        int chunkSize = bitSpec.getChunkSize();
        long payload = 0L;
        int numWidth = (int) width.toBitwiseParameter(recognizeData).longValueExact();
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
                inData.setLevel(recognizeData.getLevel() + 1);
                List<BitSpec> poppedStack = new ArrayList<>(bitSpecStack);
                poppedStack.remove(poppedStack.size() - 1);

                try {
                    bitSpec.get(bareIrStreamNo).decode(inData, poppedStack, false);
                    // match!
                    break;
                } catch (SignalRecognitionException ex) {
                    // No match, just try the next one
                }
            }
            assert (inData != null);

            if (bareIrStreamNo == bitSpec.size())
                throw new SignalRecognitionException("FiniteBitField did not parse");
            if (recognizeData.getGeneralSpec().getBitDirection() == BitDirection.lsb) // <---
                bareIrStreamNo = IrCoreUtils.reverse(bareIrStreamNo, chunkSize);

            recognizeData.setPosition(inData.getPosition());
            recognizeData.setHasConsumed(inData.getHasConsumed());
            payload = ((payload << (long) chunkSize)) | (long) bareIrStreamNo;
            recognizeData.getNameEngine().add(inData.getNameEngine());
        }

        if (rest != 0) {
            // this has been tested only with bitorder = msb.
            int bitsToStore = chunkSize - rest;
            long bitmask = IrCoreUtils.ones(bitsToStore);
            recognizeData.setDanglingBitFieldData(payload, bitmask);
            payload >>= bitsToStore;
        }
        if (recognizeData.getGeneralSpec().getBitDirection() == BitDirection.lsb)
            payload = IrCoreUtils.reverse(payload, numWidth);
        return payload;
    }

    @Override
    public BitwiseParameter invert(BitwiseParameter rhs, RecognizeData recognizeData/*, long oldBitmask*/) throws NameUnassignedException {
        long ch = getChop(recognizeData.getNameEngine());
        long wid = getWidth(recognizeData.getNameEngine());
        long payload = rhs.getValue();
        if (complement)
            payload = ~payload;
        if (reverse)
            payload = IrCoreUtils.reverse(payload, (int) wid);
        long bitmask = (rhs.getBitmask() & IrCoreUtils.ones(wid)) << ch;
        payload <<= ch;
        return new BitwiseParameter(payload, bitmask);
    }

    @Override
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecs) {
        return true; // ????
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs) {
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
        try {
            return super.weight() + (int) width.toLong()/8;
        } catch (NameUnassignedException ex) {
            // Something weird has happened
            logger.log(Level.WARNING, "Cannot compute weight of {0}", toString());
            return 1000;
        }
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
            width.toLong();
        } catch (NameUnassignedException ex) {
            return true;
        }
        return false;
    }

    @Override
    public Integer guessParameterLength(String name) {
        try {
            return data.toString().equals(name) ? (int) width.toLong() : null;
        } catch (NameUnassignedException ex) {
            return null;
        }
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        return new TreeSet<>();
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return super.constant(nameEngine) && width.constant(nameEngine);
    }

    private long assignmentNeededBitmask(RecognizeData recognizeData) {
        return getWidth(recognizeData).longValueExact() << getChop(recognizeData).longValueExact();
    }

    @Override
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
        if (! (data instanceof Name))
            throw new InvalidNameException(data.toIrpString() + " cannot be used in createParameterSpecs");

        String name = data.toString();
        long max = (1L << (long) this.numberOfBits()) - 1L;
        parameterSpecs.tweak(name, 0L, max);
    }
}
