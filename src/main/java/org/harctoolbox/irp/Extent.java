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

    @Override
    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        double time = super.evaluate(nameEngine, generalSpec, 0f) - elapsed;
        if (time < 0)
            throw new IncompatibleArgumentException("Argument of extent smaller than actual duration.");
        return time;
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            BitSpec bitSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        //double duration = evaluateWithSign(nameEngine, generalSpec, elapsed);
        if (pass != state)
            return null;
        
        EvaluatedIrStream evaluatedIrStream = new EvaluatedIrStream(nameEngine, generalSpec, bitSpec, pass);
        double time = super.evaluate(nameEngine, generalSpec, 0f) - elapsed;
        evaluatedIrStream.add(new Gap(time));
        return evaluatedIrStream;
    }

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
    boolean interleavingOk() {
        return true;
    }
}
