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

public abstract class Duration extends IrpObject implements IrStreamItem, Floatable, Evaluatable {

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

    private void compute(GeneralSpec generalSpec, NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
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

    private double multiplicator(GeneralSpec generalSpec) {
        return unit.equals("p") ? IrCoreUtils.seconds2microseconds(1/generalSpec.getFrequency())
                : unit.equals("m") ? 1000
                : unit.equals("u") ? 1
                : time_units;
    }


    public abstract double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws UnassignedException, IrpSemanticException;

    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws UnassignedException, IrpSemanticException {
        compute(generalSpec, nameEngine);
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

    public double evaluate(GeneralSpec generalSpec, NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return evaluate(generalSpec, nameEngine, 0);
    }

    @Override
    public final boolean isEmpty(NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return evaluate(null, nameEngine, 0f) == 0;
    }

    @Override
    public double toFloat(GeneralSpec generalSpec, NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return evaluate(generalSpec, nameEngine, 0f);
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) {
        renderData.add(this);
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack) throws UnassignedException, InvalidNameException, IrpSemanticException, NameConflictException, IrpSignalParseException {
        if (!recognizeData.check(isOn())) {
            IrpUtils.exiting(logger, Level.FINEST, "recognize", "wrong parity");
            throw new IrpSignalParseException("Found flash when gap expected, or vice versa");
        }
        double actual = recognizeData.get();
        double wanted = toFloat(recognizeData.getGeneralSpec(), recognizeData.toNameEngine());
        recognize(recognizeData, actual, wanted);
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) {
        renderData.add(this);
    }

    @Override
    public List<IrStreamItem> extractPass(IrSignal.Pass pass, IrSignal.Pass state) {
        return IrpUtils.mkIrStreamItemList(this);
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
    public Integer numberOfBareDurations(boolean recursive) {
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

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    protected void recognize(RecognizeData recognizeData, double actual, double wanted) throws IrpSignalParseException {
        boolean equals = IrCoreUtils.approximatelyEquals(actual, wanted, recognizeData.getAbsoluteTolerance(), recognizeData.getRelativeTolerance());
        if (equals) {
            recognizeData.consume();
        } else if (actual > wanted && recognizeData.allowChopping()) {
            recognizeData.consume(wanted);
        } else
            throw new IrpSignalParseException("Duration does not parse");
    }


    protected abstract boolean isOn();

    @Override
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return last == DurationType.none || last == DurationType.newDurationType(!isOn());
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
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
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("kind", this.getClass().getSimpleName());
        try {
            long num = Math.round(toFloat(generalSpec, null));
            map.put("microseconds", num);
            return map;
        } catch (ArithmeticException | UnassignedException | IrpSemanticException ex) {
        }
        map.put("name", nameOrNumber.toString());
        map.put("multiplicator", multiplicator(generalSpec));
        return map;
    }

//   @Override
//    public void propertiesMap(PropertiesMapData propertiesMapData, GeneralSpec generalSpec) {
//        Map<String, Object> map = new HashMap<>(2);
//        propertiesMapData.getList().add(map);
//        map.put("kind", this.getClass().getSimpleName());
//        try {
//            long num = Math.round(toFloat(generalSpec, null));
//            map.put("microseconds", num);
//            //return map;
//        } catch (ArithmeticException | UnassignedException | IrpSemanticException ex) {
//        }
//        //map.put("name", nameOrNumber.toString());
//        //map.put("multiplicator", multiplicator(generalSpec));
//        propertiesMapData.incrementDurations();
//        //return map;
//    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        try {
            return this.evaluate(generalSpec, nameEngine);
        } catch (UnassignedException | IrpSemanticException ex) {
            return null;
        }
    }
}
