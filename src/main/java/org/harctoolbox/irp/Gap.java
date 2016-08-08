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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Gap as per Chapter 3.
 *
 */
public class Gap extends Duration {

    public Gap(String str) throws IrpSyntaxException {
        this((new ParserDriver(str)).getParser().gap());
    }

    Gap(IrpParser.GapContext ctx) throws IrpSyntaxException {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    public Gap(double us) {
        super(us);
    }

    @Override
    public double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return -evaluate(nameEngine, generalSpec, elapsed);
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        return toElement(document, "gap");
    }

    @Override
    public String toIrpString() {
        return "-" + super.toIrpString();
    }

/*
    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
//        if (recognizeData.getState() != pass)
//            return new RecognizeData(recognizeData.getIrSequence(), recognizeData.getStart(), 0, recognizeData.getState(), recognizeData.getNameEngine());

        if (recognizeData.getPosition() >= recognizeData.getIrSequence().getLength())
            return false;
        if (recognizeData.hasRestFlash())
            return false;
        double physical;
        if (recognizeData.getPosition() % 2 == 0) {
            if (recognizeData.hasRestGap()) {
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
        } else if (physical > theoretical) {
            recognizeData.setRest(physical - theoretical, false);
        } else
            recognizeData.setSuccess(false);

        return recognizeData.isSuccess();
    }*/

    @Override
    protected boolean isOn() {
        return false;
    }
}

