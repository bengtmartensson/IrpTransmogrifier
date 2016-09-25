/*
Copyright (C) 2011, 2015 Bengt Martensson.

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

import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements GeneralSpec as given in Chapter 2 of Dixon: "Specification of IRP Notation", second draft.
 * This class is immutable; can only be build by the constructor, and not altered.
 */
public class GeneralSpec extends IrpObject {
    private final static int WEIGHT = 0;
    public final static double defaultDutyCycle = ModulatedIrSequence.unknownDutyCycle;
    public final static BitDirection defaultBitDirection = BitDirection.lsb;
    public final static double defaultUnit = 1;
    static void evaluatePrint(String str) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException {
        GeneralSpec gs = new GeneralSpec(str);
        System.out.println(gs);
    }

    /** Carrier frequency in Hz */
    private double frequency = ModulatedIrSequence.defaultFrequency;

    /** Duty cycle in percent. IrpUtils.invalid (-1) is defined to denote "don't care". */
    private double dutyCycle = defaultDutyCycle;


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
    public GeneralSpec(BitDirection bitDirection, double unit, /*double unit_pulses,*/ double frequency, double dutyCycle) {
        this.bitDirection = bitDirection;
        this.unit = unit;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
    }

    public GeneralSpec(BitDirection bitDirection, double unit, double frequency) {
        this(bitDirection, unit, frequency, ModulatedIrSequence.unknownDutyCycle);
    }

    /**
     * Copy constructor.
     * @param src
     */
    private GeneralSpec(GeneralSpec src) {
        this.bitDirection = src.bitDirection;
        this.unit = src.unit;
        this.frequency = src.frequency;
        this.dutyCycle = src.dutyCycle;
    }

    /** This constructor is intended for debugging and testing only */
    public GeneralSpec() {
        this(defaultBitDirection, defaultUnit, ModulatedIrSequence.defaultFrequency, ModulatedIrSequence.unknownDutyCycle);
    }

    public GeneralSpec(String str) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException {
        this(new ParserDriver(str).getParser().generalspec());
    }

    public GeneralSpec(IrpParser.ProtocolContext ctx) throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException {
        this(ctx.generalspec());
    }

    public GeneralSpec(IrpParser.GeneralspecContext ctx) throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException {
        this(ctx.generalspec_list());
    }

    public GeneralSpec(IrpParser.Generalspec_listContext ctx) throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException {
        double unitInPeriods = -1f;
        for (IrpParser.Generalspec_itemContext node : ctx.generalspec_item()) {
            ParseTree item = node.getChild(0);
            if (item instanceof IrpParser.Frequency_itemContext) {
                double kHz = NumberWithDecimals.parse(((IrpParser.Frequency_itemContext) item).number_with_decimals());
                frequency = IrCoreUtils.khz2Hz(kHz);
            } else if (item instanceof IrpParser.Unit_itemContext) {
                IrpParser.Unit_itemContext unitItem = (IrpParser.Unit_itemContext) item;
                if (unitItem.getChildCount() == 1 || unitItem.getChild(1).getText().equals("u")) {
                    unitInPeriods = -1f;
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
        if (unitInPeriods > 0) {
            if (frequency == 0)
                throw new IrpSemanticException("Units in p and frequency == 0 do not go together.");
            unit = IrCoreUtils.seconds2microseconds(unitInPeriods / frequency);
        }
    }
    @Override
    public String toString() {
        return "Frequency = " + frequency + "Hz, unit = " + unit + "us, " + bitDirection
                + (dutyCycle > 0 ? (", Duty cycle = " + IrCoreUtils.real2percent(dutyCycle) + "%.") : ", Duty cycle: -.");
    }

    public final BitDirection getBitDirection() {
        return bitDirection;
    }

    public final double getFrequency() {
        return frequency;
    }

    public final double getUnit() {
        return unit;
    }

    public final double getDutyCycle() {
        return dutyCycle;
    }


    @Override
    public String toIrpString() {
        return "{"
                + getFrequency()/1000f + "k,"
                + Math.round(getUnit()) + ","
                + getBitDirection()
                + (getDutyCycle() > 0 ? ("," + IrCoreUtils.real2percent(getDutyCycle()) + "%") : "")
                + "}";
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("generalspec");
        element.setAttribute("frequency", Long.toString(Math.round(getFrequency())));
        element.setAttribute("bitdirection", getBitDirection().toString());
        element.setAttribute("timeunit", Long.toString(Math.round(getUnit())));
        if (getDutyCycle() > 0)
            element.setAttribute("dutycycle", Long.toString(100 * Math.round(getDutyCycle())));
        return element;
    }

    @Override
    public int weight() {
        return WEIGHT;
    }
}
