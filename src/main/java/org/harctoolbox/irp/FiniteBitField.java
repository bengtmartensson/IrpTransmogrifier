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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class FiniteBitField extends BitField {

    private static final Logger logger = Logger.getLogger(FiniteBitField.class.getName());

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

    public String toBinaryString(NameEngine nameEngine, boolean reverse) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        String str = toBinaryString(nameEngine);
        return reverse ? reverse(str) : str;
    }

    private String reverse(String str) {
        StringBuilder s = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++)
            s.append(str.charAt(str.length()-1-i));
        return s.toString();
    }

    public String toBinaryString(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
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
    public long getWidth(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return width.toNumber(nameEngine);
    }

    @Override
    public String toString(NameEngine nameEngine) {
        String chopString = "";
        if (hasChop()) {
            try {
                chopString =Long.toString(chop.toNumber(nameEngine));
            } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
                chopString = chop.toIrpString();
            }
            chopString = ":" + chopString;
        }

        String dataString;
        try {
            dataString = Long.toString(data.toNumber(nameEngine));
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            dataString = data.toIrpString();
        }

        String widthString;
        try {
            widthString = Long.toString(width.toNumber(nameEngine));
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
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
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this.toString());
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        if (state == pass) {
            BitStream bitStream = new BitStream(this, nameEngine, generalSpec);
//        EvaluatedIrStream result = (bitSpec != null)
//                ? bitStream.evaluate(state, pass, nameEngine, generalSpec, bitSpec, elapsed)
//                : toEvaluatedIrStream(bitStream, pass, nameEngine, generalSpec);
//        IrpUtils.exiting(logger, "evaluate", result);
//        return result;
//    }
//
//    private EvaluatedIrStream toEvaluatedIrStream(BitStream bitStream, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {

            result.add(bitStream);
        }
        IrpUtils.exiting(logger, "evaluate", result);
        return result;
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("finite_bitfield");
        element.setAttribute("reverse", Boolean.toString(reverse));
        element.setAttribute("complement", Boolean.toString(complement));
        Element dataElement = document.createElement("data");
        dataElement.appendChild(data.toElement(document));
        element.appendChild(dataElement);
        Element widthElement = document.createElement("width");
        widthElement.appendChild(width.toElement(document));
        element.appendChild(widthElement);
        if (!(chop instanceof Number && ((Number) chop).toNumber() == 0)) {
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

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, IrpSyntaxException, UnassignedException {
        // first the simplest case: bitSpecs
        BitSpec bitSpec = bitSpecs.get(0);
        int irSequencePostion = recognizeData.getPosition();
        assert(bitSpec.getChunkSize() == 1);
        int noBits = (int) width.toNumber(/*recognizeData.getNameEngine()*/null);
        long payload = 0L;
        //RecognizeData result = null;
        //int consumedDurations = 0;
        RecognizeData inData = null;
        for (int bit = 0; bit < noBits; bit++) {
            int bareIrStreamNo;

            //new RecognizeData(recognizeData.getIrSequence(), irSequencePostion, 0, recognizeData.getState(), recognizeData.getNameEngine());
            //RecognizeData inData = new RecognizeData(recognizeData.getIrSequence(), irSequencePostion, 0, recognizeData.getState(), recognizeData.getNameEngine());
            for (bareIrStreamNo = 0; bareIrStreamNo < bitSpec.size(); bareIrStreamNo++) {
                inData = recognizeData.clone();
                inData.setPosition(irSequencePostion);
                boolean success = bitSpec.get(bareIrStreamNo).recognize(inData, pass, null);
                if (success)
                    break;
            }

            if (bareIrStreamNo == bitSpec.size()) {
                inData.setSuccess(false);
                return false;
            }
            irSequencePostion = inData.getPosition();
            //consumedDurations += inData.getLength();
            payload = (payload << bitSpec.getChunkSize()) | bareIrStreamNo;
        }
        if (this.complement)
            payload = ~payload;
        if (this.reverse ^ recognizeData.getGeneralSpec().getBitDirection() == BitDirection.lsb)
            payload = IrCoreUtils.reverse(payload, noBits);
        long bitmask = IrCoreUtils.ones(noBits) << (int) chop.toNumber(/*recognizeData.getNameEngine()*/null);
        Name name = data.toName();
        if (name != null)
            recognizeData.getParameterCollector().add(name.toString(), payload, bitmask);
        else {
            try {
                long expected = this.toNumber(recognizeData.getParameterCollector().toNameEngine()); // FIXME
                if (expected != payload)
                    return false;
            } catch (UnassignedException ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }
        }

//            if (data.toNumber(null) != null) {
//
//        } else
//            logger.warning("No match");


        recognizeData.setPosition(inData.getPosition());
        return true;//recognizeData;
        //return new RecognizeData(recognizeData.getIrSequence(), recognizeData.getStart(), consumedDurations, recognizeData.getState(), recognizeData.getNameEngine());
    }
}
