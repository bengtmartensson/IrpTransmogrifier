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
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This class implements Extents as per Chapter 4.
 *
 */
public final class Extent extends Duration {

    private static final Logger logger = Logger.getLogger(Extent.class.getName());

    public Extent(String str) {
        this(new ParserDriver(str));
    }

    public Extent(ParserDriver parserDriver) {
        this(parserDriver.getParser().extent());
    }

    public Extent(IrpParser.ExtentContext ctx) {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    public Extent(double d, String unit) {
        super(d, unit);
    }

    public Extent(double d) {
        this(d, null);
    }

    public Extent(NameOrNumber non, String unit) {
        super(non, unit);
    }

    @Override
    public IrStreamItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return new Extent(nameOrNumber.substituteConstantVariables(constantVariables), unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Extent))
            return false;

        return super.equals(obj);
    }

    @Override
    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws NameUnassignedException, IrpInvalidArgumentException {
        double time = super.evaluate(generalSpec, nameEngine, 0f) - elapsed;
        if (time < 0)
            throw new IrpInvalidArgumentException("Argument of extent smaller than actual duration.");
        return time;
    }

    @Override
    public double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws NameUnassignedException, IrpInvalidArgumentException {
        return -evaluate(generalSpec, nameEngine, elapsed);
    }

    @Override
    public String toIrpString(int radix) {
        return "^" + super.toIrpString(radix);
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack, boolean isLast) throws SignalRecognitionException {
        logger.log(recognizeData.logRecordEnter(this));
        double elapsed = recognizeData.elapsed();
        double physical = recognizeData.get();
        double theoretical;
        try {
            theoretical = toFloat(recognizeData.getGeneralSpec(), /*recognizeData.getNameEngine()*/null) - elapsed;
        } catch (IrpInvalidArgumentException | NameUnassignedException ex) {
            throw new SignalRecognitionException(ex);
        }
        recognizeData.markExtentStart();
        recognize(recognizeData, physical, theoretical, isLast);
        logger.log(recognizeData.logRecordExit(this));
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

    @Override
    protected Extent evaluatedDuration(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        return new Extent(evaluate(generalSpec, nameEngine), "u");
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        return new TreeSet<>();
    }
}
