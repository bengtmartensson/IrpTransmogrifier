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

import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class NumberWithDecimals extends IrpObject implements Floatable {
    private static final int WEIGHT = 1;
    private static final double equalityTolerance = 1E-6;

    public static double parse(String str) {
        NumberWithDecimals numberWithDecimals = new NumberWithDecimals(str);
        return numberWithDecimals.toFloat();
    }
    public static double parse(IrpParser.Number_with_decimalsContext ctx) {
        NumberWithDecimals numberWithDecimals = new NumberWithDecimals(ctx);
        return numberWithDecimals.toFloat();
    }
    private double data;

    public NumberWithDecimals(String str) {
        this(new ParserDriver(str).getParser().number_with_decimals());
    }

    public NumberWithDecimals(IrpParser.Number_with_decimalsContext ctx) {
        ParseTree child = ctx.getChild(0);
        data = (child instanceof IrpParser.Float_numberContext)
                ? FloatNumber.parse((IrpParser.Float_numberContext) child)
                : Integer.parseInt(child.getText());
    }

    public NumberWithDecimals(double d) {
        data = d;
    }

    public NumberWithDecimals(int i) {
        data = i;
    }

    public NumberWithDecimals(long n) {
        data = n;
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
        return (IrCoreUtils.approximatelyEquals(data, (long)data, 0, 0.000001)
                ? Long.toString((long)data)
                : Double.toString(data));
    }

    @Override
    public String toIrpString() {
        return toString();
    }


    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setTextContent(toString());
        return element;
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NumberWithDecimals))
            return false;

        NumberWithDecimals other = (NumberWithDecimals) obj;
        return IrCoreUtils.approximatelyEquals(data, other.data, equalityTolerance, 0);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.data) ^ (Double.doubleToLongBits(this.data) >>> 32));
        return hash;
    }

//    @Override
//    public String code(boolean eval, CodeGenerator codeGenerator) {
//        return toString();
//    }
}
