/*
Copyright (C) 2016 Bengt Martensson.

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

/**
 * This class encapsulates the IrpParser.
 */
public class IrpStatic {

    private IrpStatic() {
    }
/*
    public static double toFloat(IrpParser.Name_or_numberContext ctx, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException {
        ParseTree child = ctx.getChild(0);
        return child instanceof IrpParser.NameContext ? (double) toNumber((IrpParser.NameContext) child, nameEngine)
                : toFloat((IrpParser.Number_with_decimalsContext) child);
    }

    public static long toNumber(IrpParser.NameContext ctx, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException {
        Expression exp = nameEngine.get(toString(ctx));
        return exp.toNumber(nameEngine);
    }*/

/*
    public static double toFloat(IrpParser.Number_with_decimalsContext ctx) {
        return (ctx instanceof IrpParser.IntegerAsFloatContext)
                ? (double) toNumber(((IrpParser.IntegerAsFloatContext) ctx).INT())
                : toFloat(((IrpParser.FloatContext) ctx).float_number());
    }

    public static double toFloat(IrpParser.Float_numberContext ctx) {
        return (ctx instanceof IrpParser.DotIntContext)
                ? toFloat((IrpParser.DotIntContext) ctx)
                : toFloat((IrpParser.IntDotIntContext) ctx);
    }

    public static double toFloat(IrpParser.DotIntContext ctx) {
        return toFloat(ctx.INT());
    }

    public static double toFloat(IrpParser.IntDotIntContext ctx) {
        return toFloat(ctx.INT(0), ctx.INT(1));
    }


    private static double toFloat(TerminalNode integ, TerminalNode matissa) {
        return Double.parseDouble(integ.getText() + "." + matissa.getText());
    }

    private static double toFloat(TerminalNode matissa) {
        return Double.parseDouble("0." + matissa.getText());
    }
    */
}
