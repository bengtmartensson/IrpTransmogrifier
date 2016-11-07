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

import org.harctoolbox.ircore.IrCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class FloatNumber extends IrpObject implements Floatable {
    private static final int WEIGHT = 1;

    public static double parse(String str) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(str);
        return floatNumber.toFloat();
    }
    public static double parse(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(ctx);
        return floatNumber.toFloat();
    }

    private final double data;

    public FloatNumber(double x) {
        data = x;
    }

    public FloatNumber(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        data = Double.parseDouble(ctx.getText());
    }

    public FloatNumber(String str) throws IrpSyntaxException {
        this(new ParserDriver((str)).getParser().float_number());
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
        if (!(obj instanceof FloatNumber))
            return false;

        FloatNumber other = (FloatNumber) obj;
        return IrCoreUtils.approximatelyEquals(data, other.data);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.data) ^ (Double.doubleToLongBits(this.data) >>> 32));
        return hash;
    }

    @Override
    public String code(boolean eval, CodeGenerator codeGenerator) {
        return toString();
    }
}
