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
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Flash as per Chapter 3.
 *
 */
public class Flash extends Duration {

    public Flash(String str) throws IrpSyntaxException {
        this((new ParserDriver(str)).getParser().flash());
    }

    public Flash(IrpParser.FlashContext ctx) throws IrpSyntaxException {
        super(ctx.name_or_number(), ctx.getChildCount() > 1 ? ctx.getChild(1).getText() : null);
    }

    @Override
    public double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return evaluate(nameEngine, generalSpec, elapsed);
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        return toElement(document, "flash");
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        if (recognizeData.getPosition() >= recognizeData.getIrSequence().getLength())
            return false;
        if (recognizeData.getPosition() % 2 != 0)
            return false;
        //if (recognizeData.hasRestFlash())
        //    return false;

        double physical = recognizeData.getIrSequence().get(recognizeData.getPosition()) + recognizeData.getRest();
        double theoretical = toFloat(/*recognizeData.getNameEngine()*/null, recognizeData.getGeneralSpec());
        return recognize(recognizeData, physical, theoretical);
    }
    /*
    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
//        if (recognizeData.getState() != pass)
//            return new RecognizeData(recognizeData.getIrSequence(), recognizeData.getStart(), 0, recognizeData.getState(), recognizeData.getNameEngine());

        if (recognizeData.getPosition() >= recognizeData.getIrSequence().getLength())
            return false;
        if (recognizeData.hasRestGap())
            return false;
        double physical;
        if (recognizeData.getPosition() % 2 != 0) {
            if (recognizeData.hasRestFlash()) {
                physical = recognizeData.getRest();

            } else
                return false;
        } else {
            physical = recognizeData.getIrSequence().get(recognizeData.getPosition()) + recognizeData.getRest();
            recognizeData.incrementPosition(1);
        }
        recognizeData.clearRest();

        double theoretical = toFloat(/*recognizeData.getNameEngine()* /null, recognizeData.getGeneralSpec());

        boolean equals = IrCoreUtils.approximatelyEquals(physical, theoretical);
        if (equals) {
            recognizeData.clearRest();
        } else if (IrCoreUtils.approximatelyEquals(physical, 2 * theoretical)) {
            recognizeData.setRest(physical - theoretical, this instanceof Flash);
        } else
            recognizeData.setSuccess(false);

        return recognizeData.isSuccess();
    }*/
}
