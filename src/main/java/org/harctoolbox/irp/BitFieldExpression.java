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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class BitFieldExpression extends PrimaryItemExpression {

    static BitFieldExpression newExpression(IrpParser.BitfieldContext ctx) {
        return new BitFieldExpression(ctx);
    }

    private final BitField bitField;

    private BitFieldExpression(IrpParser.BitfieldContext ctx) {
        super(ctx);
        bitField = BitField.newBitField(ctx);
    }

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) {
        return rhs & bitmask;
    }

    @Override
    public String toIrpString(int radix) {
        return bitField.toIrpString(radix);
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = bitField.propertiesMap(true, generalSpec, nameEngine);
        //map.put("scalar", true);
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.bitField);
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
        final BitFieldExpression other = (BitFieldExpression) obj;
        return Objects.equals(this.bitField, other.bitField);
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws NameUnassignedException {
        return bitField.toNumber(nameEngine);
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        el.appendChild(bitField.toElement(document));
        return el;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return bitField.data;
    }
}
