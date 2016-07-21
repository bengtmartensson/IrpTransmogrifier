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

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Durations in Chapter 3 and 4.
 * Depends on its Protocol (GeneralSpec and NameEngine), except for this, it is immutable.
 */

public abstract class Duration extends IrStreamItem implements Floatable, Evaluatable {
    protected double us = IrCoreUtils.invalid;
    protected double time_periods = IrCoreUtils.invalid;
    protected double time_units = IrCoreUtils.invalid;
    protected NameOrNumber nameOrNumber = null;
    protected String unit = null;

    public static Duration newDuration(String str) throws IrpSyntaxException {
        IrpParser parser = new ParserDriver(str).getParser();
        try {
            return newDuration(parser.duration());
        } catch (ParseCancellationException ex) {
            return newDuration(parser.extent());
        }
    }

    public static Duration newDuration(IrpParser.DurationContext d) throws IrpSyntaxException {
        ParseTree child = d.getChild(0);
        return (child instanceof IrpParser.FlashContext)
                ? new Flash((IrpParser.FlashContext) child)
                : child instanceof IrpParser.GapContext
                ? new Gap((IrpParser.GapContext) child)
                : new Extent((IrpParser.ExtentContext) child);
    }

    public static Duration newDuration(IrpParser.ExtentContext e) throws IrpSyntaxException {
        return new Extent(e);
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

    public abstract double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException;

    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed)
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

    public double evaluate(NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        return evaluate(nameEngine, generalSpec, 0);
    }

//    @Override
//    public long toNumber(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        return Math.round(toFloat(nameEngine, generalSpec));
//    }

    @Override
    public final boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return evaluate(nameEngine, null, 0f) == 0;
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        return evaluate(nameEngine, generalSpec, 0f);
    }

    @Override
    EvaluatedIrStream evaluate(NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec, IrSignal.Pass pass, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        //double duration = evaluateWithSign(nameEngine, generalSpec, elapsed);
        EvaluatedIrStream evaluatedIrStream = new EvaluatedIrStream(nameEngine, generalSpec, bitSpec, pass);
        evaluatedIrStream.add(this);
        return evaluatedIrStream;
    }

    public Element toElement(Document document, String tagName) throws IrpSyntaxException {
        Element element = document.createElement(tagName);
        element.setAttribute("unit", unit);
        element.appendChild(nameOrNumber.toElement(document));
        return element;
    }

    @Override
    public String toIrpString() {
        return nameOrNumber.toIrpString() + (unit.equals("1") ? "" : unit);
    }

    @Override
    int numberOfBareDurations() {
        return 1;
    }

    @Override
    int numberOfBits() {
        return 0;
    }
}
