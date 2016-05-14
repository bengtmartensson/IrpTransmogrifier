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

import java.util.List;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * This class is an abstract superclass of the things that make up an IRStream (see "Directly known subclasses").
 *
 * @author Bengt Martensson
 */
public abstract class IrStreamItem {

    //protected Protocol environment;
    protected int noAlternatives = 0;

    public abstract boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException;

    protected IrStreamItem() {
        //environment = env;
        //Debug.debugIrStreamItems(this.getClass().getSimpleName() + " constructed.");
    }

    public static IrStreamItem parse(IrpParser.Irstream_itemContext ctx) throws IrpSyntaxException {
        return //ctx instanceof IrpParser.VariationContext ? new Variation((Va))
                //ctx.bitfield() != null ? new BitField(ctx.bitfield())
                //: ctx instanceof IrpParser.AssignmentContext ? new Assignment((IrpParser.AssignmentContext) ctx)
                //: ctx instanceof IrpParser.ExtentContext ? new BitField((IrpParser.ExtentContext) ctx)
                (ctx instanceof IrpParser.Duration_asitemContext) ? Duration.newDuration(((IrpParser.Duration_asitemContext) ctx).duration())
                //: ctx instanceof IrpParser.IrstreamContext ? new IrStream(ctx.irstream())
                //: ctx instanceof IrpParser.Bitspec_irstreamContext ? new BitspecIrstream(ctx.bitspec_irstream())
                : null;
    }

    public static IrStreamItem newIrStreamItem(String str) throws IrpSyntaxException {
        return newIrStreamItem((new ParserDriver(str)).getParser().irstream_item());
    }

    public static IrStreamItem newIrStreamItem(IrpParser.Irstream_itemContext ctx) throws IrpSyntaxException {
        //ParseTree child = ctx.getChild(0);
        return (ctx instanceof IrpParser.Variation_asitemContext) ? new Variation(((IrpParser.Variation_asitemContext)ctx).variation())
                : (ctx instanceof IrpParser.Bitfield_asitemContext) ? BitField.newBitField(((IrpParser.Bitfield_asitemContext) ctx).bitfield())
                : (ctx instanceof IrpParser.Assignment_asitemContext) ? new Assignment(((IrpParser.Assignment_asitemContext) ctx).assignment())
                : (ctx instanceof IrpParser.Extent_asitemContext) ? new Extent(((IrpParser.Extent_asitemContext) ctx).extent())
                : (ctx instanceof IrpParser.Duration_asitemContext) ? Duration.newDuration(((IrpParser.Duration_asitemContext) ctx).duration())
                : (ctx instanceof IrpParser.Irstream_asitemContext) ? new IrStream(((IrpParser.Irstream_asitemContext) ctx).irstream())
                : (ctx instanceof IrpParser.Bitspec_irstream_asitemContext) ? new BitspecIrstream(((IrpParser.Bitspec_irstream_asitemContext) ctx).bitspec_irstream())
                : null;
    }

    /**
     * To be overridden in Variation
     * @return noAlternatives
     */
    public int getNoAlternatives() {
        return noAlternatives;
    }

    public abstract List<IrStreamItem> evaluate(BitSpec bitSpec) throws UnassignedException, IncompatibleArgumentException;

    protected void debugBegin() {

    }

    protected void debugEnd() {

    }

    protected void debugEnd(List<IrStreamItem>list) {

    }
}
