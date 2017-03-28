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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements GeneralSpec as given in Chapter 2 of Dixon: "Specification of IRP Notation", second draft.
 * This class is immutable; can only be build by the constructor, and not altered.
 */
public class GeneralSpec extends IrpObject implements AggregateLister {
    private final static int WEIGHT = 0;
    public final static BitDirection defaultBitDirection = BitDirection.lsb;
    public final static double defaultFrequency = ModulatedIrSequence.defaultFrequency;
    public final static double defaultUnit = 1;

    /** Carrier frequency in Hz */
    private Double frequency = null;

    /** Duty cycle in percent. IrpUtils.invalid (-1) is defined to denote "don't care". */
    private Double dutyCycle = null;

    /** BitDirection */
    private BitDirection bitDirection = defaultBitDirection;

    /** Timing unit in us */
    private double unit = defaultUnit;


    /**
     * This constructor is intended for debugging and testing only.
     *
     * @param bitDirection
     * @param unit
     * @param frequency
     * @param dutyCycle
     */
    public GeneralSpec(BitDirection bitDirection, Double unit, Double frequency, Double dutyCycle) {
        super(null);
        this.bitDirection = bitDirection;
        this.unit = unit != null ? unit : defaultUnit;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
    }

    public GeneralSpec(BitDirection bitDirection, Double unit, Double frequency) {
        this(bitDirection, unit, frequency, null);
    }

    /**
     * Copy constructor.
     * @param src
     */
    private GeneralSpec(GeneralSpec src) {
        super(src.getParseTree());
        this.bitDirection = src.bitDirection;
        this.unit = src.unit;
        this.frequency = src.frequency;
        this.dutyCycle = src.dutyCycle;
    }

    /** This constructor is intended for debugging and testing only */
    public GeneralSpec() {
        this(defaultBitDirection, defaultUnit, null, null);
    }

    public GeneralSpec(String str) throws IrpInvalidArgumentException {
        this(new ParserDriver(str).getParser().generalspec());
    }

    public GeneralSpec(IrpParser.ProtocolContext ctx) throws IrpInvalidArgumentException {
        this(ctx.generalspec());
    }

    public GeneralSpec(IrpParser.GeneralspecContext ctx) throws IrpInvalidArgumentException {
        this(ctx.generalspec_list());
    }

    /**
     *
     * @param ctx
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     */
    public GeneralSpec(IrpParser.Generalspec_listContext ctx) throws IrpInvalidArgumentException {
        super(ctx);
        Double unitInPeriods = null;
        for (IrpParser.Generalspec_itemContext node : ctx.generalspec_item()) {
            ParseTree item = node.getChild(0);
            if (item instanceof IrpParser.Frequency_itemContext) {
                double kHz = NumberWithDecimals.parse(((IrpParser.Frequency_itemContext) item).number_with_decimals());
                frequency = IrCoreUtils.khz2Hz(kHz);
            } else if (item instanceof IrpParser.Unit_itemContext) {
                IrpParser.Unit_itemContext unitItem = (IrpParser.Unit_itemContext) item;
                if (unitItem.getChildCount() == 1 || unitItem.getChild(1).getText().equals("u")) {
                    unitInPeriods = null;
                    unit = NumberWithDecimals.parse(unitItem.number_with_decimals());
                } else
                    unitInPeriods = NumberWithDecimals.parse(unitItem.number_with_decimals());
            } else if (item instanceof IrpParser.Dutycycle_itemContext) {
                dutyCycle = IrCoreUtils.percent2real(NumberWithDecimals.parse(((IrpParser.Dutycycle_itemContext) item).number_with_decimals()));
            } else if (item instanceof IrpParser.Order_itemContext) {
                bitDirection = item.getText().equals("lsb")
                        ? BitDirection.lsb : BitDirection.msb;
            }
        }
        if (unitInPeriods != null) {
            if (IrCoreUtils.approximatelyEquals(frequency, 0d))
                throw new IrpInvalidArgumentException("Units in p and frequency == 0 do not go together.");
            unit = IrCoreUtils.seconds2microseconds(unitInPeriods / (frequency != null ? frequency : defaultFrequency));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeneralSpec))
            return false;

        GeneralSpec other = (GeneralSpec) obj;
        return IrCoreUtils.approximatelyEquals(frequency, other.getFrequency())
                && IrCoreUtils.approximatelyEquals(dutyCycle, other.getDutyCycle())
                && IrCoreUtils.approximatelyEquals(unit, other.getUnit())
                && bitDirection == other.getBitDirection();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.frequency) ^ (Double.doubleToLongBits(this.frequency) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.dutyCycle) ^ (Double.doubleToLongBits(this.dutyCycle) >>> 32));
        hash = 67 * hash + Objects.hashCode(this.bitDirection);
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.unit) ^ (Double.doubleToLongBits(this.unit) >>> 32));
        return hash;
    }

    public final BitDirection getBitDirection() {
        return bitDirection;
    }

    public final Double getFrequency() {
        return frequency;
    }

    public final double getFrequencyWitDefault() {
        return frequency != null ? frequency : defaultFrequency;
    }

    public final double getUnit() {
        return unit;
    }

    public final Double getDutyCycle() {
        return dutyCycle;
    }

    @Override
    public String toIrpString(int radix) {
        return toIrpString(false);
    }

    public String toIrpString(boolean usePeriods) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        if (getFrequency() != null)
            joiner.add(String.format(Locale.US, "%2.1f", IrCoreUtils.hz2khz(getFrequency())) + "k");
        joiner.add(usePeriods && getFrequency() != null
                ? (Math.round(IrCoreUtils.us2Periods(unit, getFrequency())) + "p")
                : Long.toString(Math.round(getUnit())));
        joiner.add(getBitDirection().toString());
        if (getDutyCycle() != null)
            joiner.add(Math.round(IrCoreUtils.real2percent(getDutyCycle())) + "%");
        return joiner.toString();
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("frequency", Long.toString(Math.round(getFrequency())));
        element.setAttribute("bitDirection", getBitDirection().toString());
        element.setAttribute("unit", Long.toString(Math.round(getUnit())));
        if (getDutyCycle() != null)
            element.setAttribute("dutyCycle", Long.toString(100 * Math.round(getDutyCycle())));
        return element;
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(2);
        // bitDirection and unit deliberately left out
        map.put("frequency", Math.round(frequency));
        if (dutyCycle != null)
            map.put("dutyCycle", dutyCycle);
        return map;
    }
}
