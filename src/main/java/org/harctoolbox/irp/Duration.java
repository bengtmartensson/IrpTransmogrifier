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
import java.util.List;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;

/**
 * This class implements Durations in Chapter 3 and 4.
 * Depends on its Protocol (GeneralSpec and NameEngine), except for this, it is immutable.
 */

public abstract class Duration extends PrimitiveIrStreamItem implements Floatable {
    private double us = IrCoreUtils.invalid;
    private double time_periods = IrCoreUtils.invalid;
    private double time_units = IrCoreUtils.invalid;
    private NameOrNumber nameOrNumber = null;
    private String unit = null;

    public static Duration newDuration(String str) throws IrpSyntaxException {
        IrpParser parser = new ParserDriver(str).getParser();

        try {
            IrpParser.DurationContext d = parser.duration();
            return (d instanceof IrpParser.FlashDurationContext)
                    ? new Flash(((IrpParser.FlashDurationContext) d).flash_duration())
                    : new Gap(((IrpParser.GapDurationContext) d).gap_duration());
        } catch (ParseCancellationException ex) {
            return new Extent(parser.extent());
        }
    }

    private void compute(NameEngine nameEngine, GeneralSpec generalSpec)
            throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        double time = nameOrNumber.toFloat(nameEngine, generalSpec);
        if (time == 0f)
            throw new IncompatibleArgumentException("Duration of 0 not sensible");

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

    protected Duration(IrpParser.Name_or_numberContext ctx, String unit) throws IrpSyntaxException {
        super();
        nameOrNumber = new NameOrNumber(ctx);
        this.unit = unit != null ? unit : "1";
    }

    public abstract double evaluateWithSign(double elapsed, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException;

    public double evaluate(double elapsed, NameEngine nameEngine, GeneralSpec generalSpec)
            throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        compute(nameEngine, generalSpec);
        if (time_periods != IrCoreUtils.invalid) {
            if (generalSpec.getFrequency() > 0)
                return IrCoreUtils.seconds2microseconds(time_periods/generalSpec.getFrequency());
            else
                throw new ArithmeticException("Units in p and frequency == 0 do not go together.");

        } else if (time_units != IrCoreUtils.invalid) {
            if (generalSpec.getUnit() > 0)
                return time_units * generalSpec.getUnit();
            else
                throw new ArithmeticException("Relative units and unit == 0 do not go together.");
        } else {
            return us;
        }
    }

    private double evaluate(NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        return evaluate(0, nameEngine, generalSpec);
    }

    @Override
    public List<IrStreamItem> evaluate(BitSpec bitSpec) {
        debugBegin();
        List<IrStreamItem> list = new ArrayList<>(1);
        list.add(this);
        return list;
    }

//    @Override
//    public long toNumber(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        return Math.round(toFloat(nameEngine, generalSpec));
//    }

    @Override
    public final boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return evaluate(0.0, nameEngine, null) == 0;
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        return evaluate(0, nameEngine, generalSpec);
    }
}
