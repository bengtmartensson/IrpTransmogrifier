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

import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a "particularly efficient" implementation of Expression for the
 * special case of the expression being an integer (= long).
 */
class IntegerExpression extends Expression {

    private final long data;

    IntegerExpression(long value) {
        super((IrpParser.ExpressionContext) null);
        data = value;
    }


    @Override
    public int hashCode() {
        return (int) data;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IntegerExpression))
            return false;

        IntegerExpression other = (IntegerExpression) obj;

        return data == other.data;
    }

    @Override
    public String toString() {
        return Long.toString(data);
    }

    @Override
    public String toStringTree(IrpParser parser) {
        return toString();
    }

    @Override
    public String toStringTree() {
        return toString();
    }

    @Override
    public long invert(long rhs) throws UnassignedException {
        return rhs;
    }

    @Override
    public boolean isUnary() {
        return true;
    }

    @Override
    public int numberNames() {
        return 0;
    }

    @Override
    public Name toName() {
        return null;
    }

    @Override
    public long toNumber() throws UnassignedException {
        return data;
    }

    @Override
    public long toNumber(NameEngine nameEngine) {
        return data;
    }

    @Override
    public Element toElement(Document document) {
        return new Number(data).toElement(document);
    }

    @Override
    public String toIrpString(int radix) {
        return Long.toString(data, radix);
    }

    @Override
    public int weight() {
        return 1;
    }

    @Override
    public String code(boolean eval, CodeGenerator codeGenerator) {
            return ""; // ???
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(0);
        return map;
    }
}
