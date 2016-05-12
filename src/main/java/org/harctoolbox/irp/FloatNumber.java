/*
Copyright (C) 2014 Bengt Martensson.

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

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 */
public class FloatNumber implements Floatable {

    private double data;

    public FloatNumber(double x) {
        data = x;
    }

    public FloatNumber(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        data = toFloat(ctx);
    }

    public FloatNumber(String str) throws IrpSyntaxException {
        this(new ParserDriver((str)).getParser().float_number());
    }

//    public static double toFloat(IrpParser.Number_with_decimalsContext ctx) throws IrpSyntaxException {
//        return (ctx instanceof IrpParser.IntegerAsFloatContext)
//                ? toFloat(((IrpParser.IntegerAsFloatContext) ctx).INT())
//                : toFloat(((IrpParser.FloatContext) ctx).float_number());
//    }

    private static double toFloat(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        return (ctx instanceof IrpParser.DotIntContext)
                ? toFloat((IrpParser.DotIntContext) ctx)
                : toFloat((IrpParser.IntDotIntContext) ctx);
    }

    private static double toFloat(IrpParser.DotIntContext ctx) throws IrpSyntaxException {
        return toFloat("0." + ctx.INT().getText());
    }

    private static double toFloat(IrpParser.IntDotIntContext ctx) throws IrpSyntaxException {
        return toFloat(ctx.INT(0), ctx.INT(1));
    }

    private static double toFloat(TerminalNode integ, TerminalNode matissa) throws IrpSyntaxException {
        return toFloat(integ.getText() + "." + matissa.getText());
    }

    private static double toFloat(TerminalNode matissa) throws IrpSyntaxException {
        return toFloat(matissa.getText());
    }

    private static double toFloat(String string) throws IrpSyntaxException {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            throw new IrpSyntaxException("Invalid float number: " + string);
        }
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) {
        return toFloat();
    }

    public double toFloat() {
        return data;
    }

    @Override
    public String toString() {
        return Double.toString(data);
    }

    public static double parse(String str) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(str);
        return floatNumber.toFloat();
    }

    public static double parse(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(ctx);
        return floatNumber.toFloat();
    }
}
