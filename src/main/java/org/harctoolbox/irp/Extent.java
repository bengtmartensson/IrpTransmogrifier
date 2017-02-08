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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;

/**
 * This class implements Extents as per Chapter 4.
 *
 */
public class Extent extends Duration {

    private static final Logger logger = Logger.getLogger(Extent.class.getName());

    public Extent(String str) {
        this((new ParserDriver(str)).getParser().extent());
    }

    public Extent(IrpParser.ExtentContext ctx) {
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
    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws UnassignedException, IrpSemanticException {
        double time = super.evaluate(generalSpec, nameEngine, 0f) - elapsed;
        if (time < 0)
            throw new IrpSemanticException("Argument of extent smaller than actual duration.");
        return time;
    }

    @Override
    public double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws UnassignedException, IrpSemanticException {
        return -evaluate(generalSpec, nameEngine, elapsed);
    }

    @Override
    public String toIrpString() {
        return "^" + super.toIrpString();
    }

    @Override
    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, IrpSemanticException, IrpSignalParseException {
        IrpUtils.entering(logger, Level.FINEST, "recognize", this);
        double physical = recognizeData.getExtentDuration();
        double theoretical = toFloat(recognizeData.getGeneralSpec(), /*recognizeData.getNameEngine()*/null);
        recognizeData.markExtentStart();
        recognize(recognizeData, physical, theoretical);
        //IrpUtils.exiting(logger, Level.FINEST, "recognize", "%s; expected: %8.1f, was: %8.1f", success ? "pass" : "fail", theoretical, physical);
    }

    @Override
    protected boolean isOn() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash + 31*super.hashCode();
    }

    @Override
    public boolean hasExtent() {
        return true;
    }
}
