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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;

/**
 * This class is an abstract superclass of the things that make up an IRStream (see "Directly known subclasses").
 *
 * @author Bengt Martensson
 */
public abstract class IrStreamItem extends IrpObject {
    //    public static IrStreamItem parse(IrpParser.Irstream_itemContext ctx) throws IrpSyntaxException {
//        ParseTree child = ctx.getChild(0);
//        return //ctx instanceof IrpParser.VariationContext ? new Variation((Va))
//                //ctx.bitfield() != null ? new BitField(ctx.bitfield())
//                //: ctx instanceof IrpParser.AssignmentContext ? new Assignment((IrpParser.AssignmentContext) ctx)
//                //: ctx instanceof IrpParser.ExtentContext ? new BitField((IrpParser.ExtentContext) ctx)
//                (child instanceof IrpParser.DurationContext) ? Duration.newDuration(((IrpParser.DurationContext) child))
//                //: ctx instanceof IrpParser.IrstreamContext ? new IrStream(ctx.irstream())
//                //: ctx instanceof IrpParser.Bitspec_irstreamContext ? new BitspecIrstream(ctx.bitspec_irstream())
//                : null;
//    }

    public static IrStreamItem newIrStreamItem(String str) throws IrpSyntaxException, InvalidRepeatException {
        return newIrStreamItem((new ParserDriver(str)).getParser().irstream_item());
    }
    public static IrStreamItem newIrStreamItem(IrpParser.Irstream_itemContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.VariationContext) ? new Variation(((IrpParser.VariationContext) child))
                : (child instanceof IrpParser.BitfieldContext) ? BitField.newBitField((IrpParser.BitfieldContext) child)
                : (child instanceof IrpParser.AssignmentContext) ? new Assignment((IrpParser.AssignmentContext) child)
                //: (child instanceof IrpParser.ExtentContext) ? new Extent((IrpParser.ExtentContext) child)
                : (child instanceof IrpParser.DurationContext) ? Duration.newDuration((IrpParser.DurationContext) child)
                : (child instanceof IrpParser.IrstreamContext) ? new IrStream((IrpParser.IrstreamContext) child)
                : (child instanceof IrpParser.Bitspec_irstreamContext) ? new BitspecIrstream((IrpParser.Bitspec_irstreamContext) child)
                : null;
    }

    //protected Protocol environment;
    //protected int noAlternatives = 0;


    protected IrStreamItem() {
        //environment = env;
        //Debug.debugIrStreamItems(this.getClass().getSimpleName() + " constructed.");
    }
    public abstract boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException;


//    /**
//     * To be overridden in Variation
//     * @return noAlternatives
//     */
//    public int numberOfAlternatives() {
//        return noAlternatives;
//    }

//    protected void debugBegin() {
//
//    }
//
//    protected void debugEnd() {
//
//    }
//
//    protected void debugEnd(List<IrStreamItem>list) {
//
//    }

    abstract EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException;

    int numberOfBitSpecs() {
        return 0;
    }

    public abstract boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs);

    abstract int numberOfBits();

    abstract int numberOfBareDurations();

    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass) {
        return null;
    }

    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass) {
        return null;
    }

    abstract ParserRuleContext getParseTree();

    public abstract boolean recognize(RecognizeData recognizeData,
            IrSignal.Pass pass, ArrayList<BitSpec> bitSpecs)
    throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException;
}
