/*
Copyright (C) 2011, 2016 Bengt Martensson.

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
 * This class implements Extents as per Chapter 4.
 *
 */
public class Extent extends Duration {

    public Extent(String str) throws IrpSyntaxException {
        this((new ParserDriver(str)).getParser().extent());
    }

    public Extent(IrpParser.ExtentContext ctx) throws IrpSyntaxException {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    public Extent(double d, String unit) {
        super(d, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Extent))
            return false;

        return super.equals(obj);
    }

    @Override
    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        double time = super.evaluate(nameEngine, generalSpec, 0f) - elapsed;
        if (time < 0)
            throw new IncompatibleArgumentException("Argument of extent smaller than actual duration.");
        return time;
    }

//    @Override
//    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
//            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
//        //double duration = evaluateWithSign(nameEngine, generalSpec, elapsed);
//        if (pass != state)
//            return null;
//
//        EvaluatedIrStream evaluatedIrStream = new EvaluatedIrStream(nameEngine, generalSpec, pass);
//        //double time = super.evaluate(nameEngine, generalSpec, 0f) - elapsed;
//        //evaluatedIrStream.add(new Gap(time));
//        evaluatedIrStream.add(this);
//        return evaluatedIrStream;
//    }

    @Override
    public double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return -evaluate(nameEngine, generalSpec, elapsed);
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        return toElement(document, "extent");
    }

    @Override
    public String toIrpString() {
        return "^" + super.toIrpString();
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        double physical = recognizeData.getExtentDuration();
        double theoretical = toFloat(/*recognizeData.getNameEngine()*/null, recognizeData.getGeneralSpec());
        recognizeData.markExtentStart();
        return recognize(recognizeData, physical, theoretical);
    }





//    @Override
//    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass,
//            ArrayList<BitSpec> bitSpecs)
//            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
//         if (recognizeData.getPosition() >= recognizeData.getIrSequence().getLength())
//            return false;
//         if (recognizeData.getPosition() % 2 == 0)
//            return false;
//
//        if (recognizeData.hasRestFlash())
//            return false;
//        double physical = recognizeData.getIrSequence().get(recognizeData.getPosition())
//                + recognizeData.getRest()
//                + recognizeData.getIrSequence().getDuration(0, recognizeData.getPosition() - 1);
//        double theoretical = toFloat(/*recognizeData.getNameEngine()*/null, recognizeData.getGeneralSpec());
//        return recognize(recognizeData, physical, theoretical);
//    }

    @Override
    protected boolean isOn() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash + 31*super.hashCode();
    }
}
