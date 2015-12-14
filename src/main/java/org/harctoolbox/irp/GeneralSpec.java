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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ModulatedIrSequence;

/**
 * This class implements GeneralSpec as given in Chapter 2 of Dixon: "Specification of IRP Notation", second draft.
 * This class is immutable; can only be build by the constructor, and not altered.
 */
public class GeneralSpec {

    /** Carrier frequency in Hz */
    private double frequency = ModulatedIrSequence.defaultFrequency;

    /** Duty cycle in percent. IrpUtils.invalid (-1) is defined to denote "don't care". */
    private double dutyCycle = defaultDutyCycle;

    public final static double defaultDutyCycle = ModulatedIrSequence.unknownDutyCycle;

    /** BitDirection */
    private BitDirection bitDirection = defaultBitDirection;

    public final static BitDirection defaultBitDirection = BitDirection.lsb;

    /** Timing unit in us */
    private double unit = defaultUnit;

    public final static double defaultUnit = 1;

    @Override
    public String toString() {
        return "Frequency = " + frequency + "Hz, unit = " + unit + "us, " + bitDirection + (dutyCycle > 0 ? (", Duty cycle = " + dutyCycle + "%.") : ", Duty cycle: -.");
    }

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

    public GeneralSpec(String str) throws IrpSyntaxException {
        this(new ParserDriver(str).generalspec());
    }

    public GeneralSpec(IrpParser.ProtocolContext ctx) {
        this(ctx.generalspec());
    }

    public GeneralSpec(IrpParser.GeneralspecContext ctx) {
        this(ctx.generalspec_list());
    }

    public GeneralSpec(IrpParser.Generalspec_listContext ctx) {
        double unitInPeriods = -1f;
        for (IrpParser.Generalspec_itemContext item : ctx.generalspec_item()) {
            if (item instanceof IrpParser.FrequencyContext) {
                frequency = ParserDriver.visit(((IrpParser.FrequencyContext) item).frequency_item().number_with_decimals());
            } else if (item instanceof IrpParser.UnitContext) {
                IrpParser.Unit_itemContext unitItem = ((IrpParser.UnitContext) item).unit_item();
                if (unitItem instanceof IrpParser.UnitInMicrosecondsContext)
                    unit = ParserDriver.visit(((IrpParser.UnitInMicrosecondsContext) unitItem).number_with_decimals());
                else
                    unitInPeriods = ParserDriver.visit(((IrpParser.UnitInPeriodsContext) unitItem).number_with_decimals());
            } else if (item instanceof IrpParser.DutycycleContext) {
                dutyCycle = ParserDriver.visit(((IrpParser.DutycycleContext) item).dutycycle_item().number_with_decimals());
            } else if (item instanceof IrpParser.ByteorderContext) {
                bitDirection = ((IrpParser.ByteorderContext) item).order_item() instanceof IrpParser.OrderLSBContext
                        ? BitDirection.lsb : BitDirection.msb;
            }
        }
        if (unitInPeriods > 0) {
            if (frequency == 0)
                throw new ArithmeticException("Units in p and frequency == 0 do not go together.");
            unit = IrCoreUtils.seconds2microseconds(unitInPeriods / IrCoreUtils.khz2Hz(frequency));
        }
    }

    BitDirection getBitDirection() {
        return bitDirection;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getUnit() {
        return unit;
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    private static void test(String str) throws IrpSyntaxException {
        GeneralSpec gs = new GeneralSpec(str);
        System.out.println(gs);
    }

    /**
     * Just for testing and debugging.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0)
                test(args[0]);
            else {
                //test("{0k,,10p}"); // Thows error
                test("{ }"); // Seem to trigger bug in ANTLR
                test("{38.4k,564}");
                test("{564,38.4k}");
                test("{22p,40k}");
                test("{msb, 889u}");
                test("{42%, 10p,msb,40k}");
                test("{msb ,40k , 33.33333% ,10p }");
                test("{msb, 123u, 100k, 10p, 1000k}");
            }
        } catch (IrpSyntaxException ex) {
            Logger.getLogger(GeneralSpec.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
