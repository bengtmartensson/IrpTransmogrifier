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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Durations in Chapter 3 and 4.
 * Depends on its Protocol (GeneralSpec and NameEngine), except for this, it is immutable.
 */

public abstract class Duration extends IrStreamItem implements Floatable, Evaluatable {

    private static final Logger logger = Logger.getLogger(Duration.class.getName());
    private static final double DUMMYTIMEUNIT = 999;

    public static Duration newDuration(String str) {
        IrpParser parser = new ParserDriver(str).getParser();
        try {
            return newDuration(parser.duration());
        } catch (ParseCancellationException ex) {
            return newDuration(parser.extent());
        }
    }

    public static Duration newDuration(IrpParser.DurationContext d) {
        ParseTree child = d.getChild(0);
        Duration instance = (child instanceof IrpParser.FlashContext)
                ? new Flash((IrpParser.FlashContext) child)
                : child instanceof IrpParser.GapContext
                ? new Gap((IrpParser.GapContext) child)
                : new Extent((IrpParser.ExtentContext) child);
        instance.parseTree = (ParserRuleContext) child;
        return instance;
    }

    public static Duration newDuration(IrpParser.ExtentContext e) {
        return new Extent(e);
    }

    public static boolean isOn(int index) {
        return index % 2 == 0;
    }

    protected double us = IrCoreUtils.invalid;
    protected double time_periods = IrCoreUtils.invalid;
    protected double time_units = IrCoreUtils.invalid;
    protected NameOrNumber nameOrNumber = null;
    protected String unit = null;
    protected ParserRuleContext parseTree = null;

    protected Duration(double d, String unit) {
        nameOrNumber = new NameOrNumber(d);
        this.unit = unit != null ? unit : "";
    }

    protected Duration(double us) {
        this(us, "u");
    }

    protected Duration(IrpParser.Name_or_numberContext ctx, String unit) {
        super();
        nameOrNumber = new NameOrNumber(ctx);
        this.unit = unit != null ? unit : "";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.us) ^ (Double.doubleToLongBits(this.us) >>> 32));
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.time_periods) ^ (Double.doubleToLongBits(this.time_periods) >>> 32));
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.time_units) ^ (Double.doubleToLongBits(this.time_units) >>> 32));
        hash = 43 * hash + Objects.hashCode(this.nameOrNumber);
        hash = 43 * hash + Objects.hashCode(this.unit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Duration))
            return false;

        Duration other = (Duration) obj;
        return unit.equals(other.unit)
                && IrCoreUtils.approximatelyEquals(us, other.us)
                && IrCoreUtils.approximatelyEquals(time_periods, other.time_periods)
                && IrCoreUtils.approximatelyEquals(time_units, other.time_units)
                && nameOrNumber.equals(other.nameOrNumber);
    }

    private void compute(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, IrpSemanticException {
        double time = nameOrNumber.toFloat(nameEngine, generalSpec);

        switch (unit) {
            case "p":
                time_periods = time;
                break;
            case "m":
                us = IrCoreUtils.milliseconds2microseconds(time);
                break;
            case "u":
                us = time;
                break;
            default:
                time_units = time;
                break;
        }
    }

    private double multiplicator(GeneralSpec generalSpec) {
        return unit.equals("p") ? IrCoreUtils.seconds2microseconds(1/generalSpec.getFrequency())
                : unit.equals("m") ? 1000
                : unit.equals("u") ? 1
                : time_units;
    }


    public abstract double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws UnassignedException, IrpSemanticException;

    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws UnassignedException, IrpSemanticException {
        compute(nameEngine, generalSpec);
        if (time_periods != IrCoreUtils.invalid) {
            if (generalSpec == null)
                return DUMMYTIMEUNIT;
            else if (generalSpec.getFrequency() > 0)
                return IrCoreUtils.seconds2microseconds(time_periods/generalSpec.getFrequency());
            else
                throw new ArithmeticException("Units in p and frequency == 0 do not go together.");

        } else if (time_units != IrCoreUtils.invalid) {
            if (generalSpec == null)
                return time_units * DUMMYTIMEUNIT;
            if (generalSpec.getUnit() > 0)
                return time_units * generalSpec.getUnit();
            else
                throw new ArithmeticException("Relative units and unit == 0 do not go together.");
        } else {
            return us;
        }
    }

    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, IrpSemanticException {
        return evaluate(nameEngine, generalSpec, 0);
    }

    @Override
    public final boolean isEmpty(NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return evaluate(nameEngine, null, 0f) == 0;
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, IrpSemanticException {
        return evaluate(nameEngine, generalSpec, 0f);
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec) {
        EvaluatedIrStream evaluatedIrStream = new EvaluatedIrStream(nameEngine, generalSpec, pass);

        if (state == pass)
            evaluatedIrStream.add(this);

        IrpUtils.exiting(logger, "evaluate", evaluatedIrStream);
        return evaluatedIrStream;
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("unit", unit);
        element.appendChild(nameOrNumber.toElement(document));
        return element;
    }

    @Override
    public String toIrpString() {
        return nameOrNumber.toIrpString() + unit;
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    int numberOfBareDurations() {
        return 1;
    }

    @Override
    int numberOfBits() {
        return 0;
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws InvalidNameException, UnassignedException, IrpSemanticException {
        IrpUtils.entering(logger, Level.FINEST, "recognize", this);
        if (!recognizeData.check(isOn())) {
            IrpUtils.exiting(logger, Level.FINEST, "recognize", "wrong parity");
            return false;
        }
        double actual = recognizeData.get();
        double wanted = toFloat(recognizeData.toNameEngine(), recognizeData.getGeneralSpec());
        boolean success = recognize(recognizeData, actual, wanted);
        IrpUtils.exiting(logger, Level.FINEST, "recognize", "%s; expected: %8.1f, was: %8.1f", success ? "pass" : "fail", wanted, actual);
        return success;
    }

    protected boolean recognize(RecognizeData recognizeData, double actual, double wanted) {
        boolean equals = IrCoreUtils.approximatelyEquals(actual, wanted, recognizeData.getAbsoluteTolerance(), recognizeData.getRelativeTolerance());
        if (equals) {
            recognizeData.consume();
        } else if (actual > wanted && recognizeData.allowChopping()) {
            recognizeData.consume(wanted);
        } else
            recognizeData.setSuccess(false);

        return recognizeData.isSuccess();
    }


    protected abstract boolean isOn();

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        return last == DurationType.none || last == DurationType.newDurationType(!isOn());
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.newDurationType(isOn());
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.newDurationType(isOn());
    }

    @Override
    public int weight() {
        return nameOrNumber.weight();
    }

    public String getUnit() {
        return unit;
    }

    public double getTimeInUnits() throws IrpSemanticException {
        return nameOrNumber.toRawNumber();
    }

    @Override
    public boolean hasExtent() {
        return false; // overridden in Extent
    }

    @Override
    public Set<String> assignmentVariables() {
        return new HashSet<>(0);
    }

    @Override
    public Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("kind", this.getClass().getSimpleName());
        try {
            long num = Math.round(toFloat(null, generalSpec));
            map.put("microseconds", num);
            return map;
        } catch (ArithmeticException | UnassignedException | IrpSemanticException ex) {
        }
        map.put("name", nameOrNumber.toString());
        map.put("multiplicator", multiplicator(generalSpec));
        return map;
    }
}
