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
import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a "particularly efficient" implementation of Expression for the
 * special case of the expression being an integer (= long).
 */
final class NumberExpression extends PrimaryItemExpression {

    static NumberExpression newExpression(IrpParser.NumberContext numberContext) {
        return new NumberExpression(numberContext, numberContext);
    }

    static NumberExpression newExpression(ParseTree ctx, IrpParser.NumberContext numberContext) {
        return new NumberExpression(ctx, numberContext);
    }

    private final Number number;

    NumberExpression(java.lang.Number value) {
        super(null);
        number = new Number(value);
    }

    private NumberExpression(ParseTree original, IrpParser.NumberContext ctx) {
        super(original);
        number = new Number(ctx.getText());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.number);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NumberExpression other = (NumberExpression) obj;
        return Objects.equals(this.number, other.number);
    }

    @Override
    public long toNumber() {
        return number.toNumber();
    }

    @Override
    public long toNumber(NameEngine nameEngine) {
        return number.toNumber();
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        el.appendChild(number.toElement(document));
        return el;
    }

    @Override
    public String toIrpString(int radix) {
        return number.toIrpString(radix);
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = number.propertiesMap(eval, generalSpec, nameEngine);
        map.put("scalar", true);
        return map;
    }

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) {
        return rhs & bitmask;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return null;
    }
}
