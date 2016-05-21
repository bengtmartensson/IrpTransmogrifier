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

import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 */
public class NumberWithDecimals implements Floatable {
    private double data;

    public NumberWithDecimals(String str) throws IrpSyntaxException {
        this(new ParserDriver(str).getParser().number_with_decimals());
    }

    public NumberWithDecimals(IrpParser.Number_with_decimalsContext ctx) throws IrpSyntaxException {
        ParseTree child = ctx.getChild(0);
        data = (child instanceof IrpParser.Float_numberContext)
                ? FloatNumber.parse((IrpParser.Float_numberContext) child)
                : (double) Integer.parseInt(child.getText());
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
        NumberWithDecimals numberWithDecimals = new NumberWithDecimals(str);
        return numberWithDecimals.toFloat();
    }

    public static double parse(IrpParser.Number_with_decimalsContext ctx) throws IrpSyntaxException {
        NumberWithDecimals numberWithDecimals = new NumberWithDecimals(ctx);
        return numberWithDecimals.toFloat();
    }
}
