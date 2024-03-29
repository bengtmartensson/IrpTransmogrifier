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
import java.util.TreeSet;
import java.util.logging.Logger;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Durations in Chapter 3 and 4.
 * Depends on its Protocol (GeneralSpec and NameEngine), except for this, it is immutable.
 */

public abstract class Duration extends IrpObject implements IrStreamItem, Floatable, Evaluatable {

    private static final Logger logger = Logger.getLogger(Duration.class.getName());
    private static final double DUMMYTIMEUNIT = 999;

    public static Duration newDuration(String str) {
        return newDuration(new ParserDriver(str));
    }

    public static Duration newDuration(ParserDriver parserDriver) {
        IrpParser parser = parserDriver.getParser();
        try {
            return newDuration(parser.duration());
        } catch (ParseCancellationException ex) {
            return newDuration(parser.extent());
        }
    }

    public static Duration newDuration(IrpParser.DurationContext d) {
        ParseTree child = d.getChild(0);
        return (child instanceof IrpParser.FlashContext)
                ? new Flash((IrpParser.FlashContext) child)
                : child instanceof IrpParser.GapContext
                ? new Gap((IrpParser.GapContext) child)
                : new Extent((IrpParser.ExtentContext) child);
    }

    public static Duration newDuration(IrpParser.ExtentContext e) {
        return new Extent(e);
    }

    public static boolean isOn(int index) {
        return index % 2 == 0;
    }

    protected Double us = null;
    protected Double time_periods = null;
    protected Double time_units = null;
    protected NameOrNumber nameOrNumber = null;
    protected String unit = null;
    //protected ParserRuleContext parseTree = null;

    protected Duration(double d, String unit) {
        super(null);
        nameOrNumber = new NameOrNumber(d);
        this.unit = unit != null ? unit : "";
    }

    protected Duration(double us) {
        this(us, "u");
    }

    protected Duration(IrpParser.Name_or_numberContext ctx, String unit) {
        super(ctx);
        nameOrNumber = new NameOrNumber(ctx);
        this.unit = unit != null ? unit : "";
    }

    protected Duration(NameOrNumber nameOrNumber, String unit) {
        super(null);
        this.nameOrNumber = nameOrNumber;
        this.unit = unit;
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return nameOrNumber == null || nameOrNumber.constant(nameEngine);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + IrCoreUtils.hashForDouble(this.us);
        hash = 43 * hash + IrCoreUtils.hashForDouble(this.time_periods);
        hash = 43 * hash + IrCoreUtils.hashForDouble(this.time_units);
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

    private void compute(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        double time = nameOrNumber.toFloat(generalSpec, nameEngine);

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

    @SuppressWarnings("null")
    private double multiplicator(GeneralSpec generalSpec) {
        Objects.requireNonNull(time_units);
        return unit.equals("p") ? IrCoreUtils.seconds2microseconds(1/generalSpec.getFrequencyWitDefault())
                : unit.equals("m") ? 1000
                : unit.equals("u") ? 1
                : time_units;
    }

    public abstract double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws NameUnassignedException, IrpInvalidArgumentException;

    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws NameUnassignedException, IrpInvalidArgumentException {
        compute(generalSpec, nameEngine);
        if (time_periods != null) {
            if (generalSpec == null)
                return DUMMYTIMEUNIT;
            else if (generalSpec.getFrequencyWitDefault()> 0)
                return IrCoreUtils.seconds2microseconds(time_periods/generalSpec.getFrequencyWitDefault());
            else
                throw new ThisCannotHappenException("Units in p and frequency == 0 do not go together.");

        } else if (time_units != null) {
            if (generalSpec == null)
                return time_units * DUMMYTIMEUNIT;
            if (generalSpec.getUnit() > 0)
                return time_units * generalSpec.getUnit();
            else
                throw new ThisCannotHappenException("Relative units and unit == 0 do not go together.");
        } else {
            return us;
        }
    }

    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        return evaluate(generalSpec, nameEngine, 0);
    }

    /**
     * Deliver an "evaluated" Duration (variables resolved) of the same subclass.
     * @param generalSpec
     * @param nameEngine
     * @return
     * @throws NameUnassignedException
     * @throws IrpInvalidArgumentException
     */
    protected abstract Duration evaluatedDuration(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException;

    @Override
    public final boolean isEmpty(NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        return evaluate(null, nameEngine, 0f) == 0;
    }

    @Override
    public double toFloat(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        return evaluate(generalSpec, nameEngine, 0f);
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) throws NameUnassignedException {
        try {
            renderData.add(evaluatedDuration(renderData.getGeneralSpec(), renderData.getNameEngine()));
        } catch (IrpInvalidArgumentException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack, boolean isLast) throws SignalRecognitionException {
        logger.log(recognizeData.logRecordEnter(this));
        if (!recognizeData.check(isOn()))
            throw new SignalRecognitionException("Either end of sequence, or found flash when gap expected, or vice versa");

        double actual = recognizeData.get();
        double wanted;
        try {
            wanted = toFloat(recognizeData.getGeneralSpec(), recognizeData.getNameEngine());
        } catch (IrpInvalidArgumentException | NameUnassignedException ex) {
            throw new SignalRecognitionException(ex);
        }
        recognize(recognizeData, actual, wanted, isLast);
        logger.log(recognizeData.logRecordExit(this));
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) throws NameUnassignedException {
        render(renderData, bitSpecStack);
    }

    @Override
    public BareIrStream extractPass(IrSignal.Pass pass, IrStream.PassExtractorState state) {
        return new BareIrStream(this);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("unit", unit);
        element.appendChild(nameOrNumber.toElement(document));
        return element;
    }

    @Override
    public String toIrpString(int radix) {
        return nameOrNumber.toIrpString(radix) + unit;
    }

    @Override
    public Integer numberOfBareDurations() {
        return 1;
    }

    @Override
    public Integer numberOfDurations() {
        return 1;
    }

    @Override
    public Integer numberOfBits() {
        return 0;
    }

    protected void recognize(RecognizeData recognizeData, double actual, double wanted, boolean isLast) throws SignalRecognitionException {
        boolean equals = IrCoreUtils.approximatelyEquals(actual, wanted, recognizeData.getAbsoluteTolerance(), recognizeData.getRelativeTolerance());
        if (equals)
            recognizeData.consume();
        else if (actual > wanted && recognizeData.allowChopping())
            recognizeData.consume(wanted);
        else if (recognizeData.leadoutOk(isLast))
            recognizeData.consume();
        else
            throw new SignalRecognitionException("Duration does not parse, wanted " + wanted + ", was " + actual + ", position = " + recognizeData.getPosition());
    }


    protected abstract boolean isOn();

    @Override
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecs) {
        return last == DurationType.none || last == DurationType.newDurationType(!isOn());
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = DurationType.newDurationType(isOn());
        return !(current == toCheck && last == current);
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

    public double getTimeInUnits() throws InvalidArgumentException {
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
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("kind", this.getClass().getSimpleName());
        try {
            long num = Math.round(toFloat(generalSpec, null));
            map.put("microseconds", num);
            return map;
        } catch (IrpInvalidArgumentException | NameUnassignedException ex) {
        }
        map.put("name", nameOrNumber.toString());
        map.put("multiplicator", multiplicator(generalSpec));
        return map;
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        try {
            return this.evaluate(generalSpec, nameEngine);
        } catch (IrpInvalidArgumentException | NameUnassignedException ex) {
            return null;
        }
    }

    @Override
    public boolean nonConstantBitFieldLength() {
        return false;
    }

    @Override
    public Integer guessParameterLength(String name) {
        return null;
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        TreeSet<Double> result = new TreeSet<>();
        result.add(microSeconds(generalSpec, nameEngine));
        return result;
    }
}
