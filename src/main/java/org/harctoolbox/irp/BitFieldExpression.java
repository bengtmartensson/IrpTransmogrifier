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

final class BitFieldExpression extends PrimaryItemExpression {

    static BitFieldExpression newExpression(IrpParser.BitfieldContext ctx) {
        return new BitFieldExpression(ctx, ctx);
    }

    static BitFieldExpression newExpression(ParseTree original, IrpParser.BitfieldContext ctx) {
        return new BitFieldExpression(original, ctx);
    }

    private final BitField bitField;

    private BitFieldExpression(ParseTree original, IrpParser.BitfieldContext ctx) {
        this(original, BitField.newBitField(ctx));
    }

    private BitFieldExpression(ParseTree original, BitField bitField) {
        super(original);
        this.bitField = bitField;
    }

    @Override
    public BitFieldExpression substituteConstantVariables(Map<String, Long> constantVariables) {
        return new BitFieldExpression(null, bitField.substituteConstantVariables(constantVariables));
    }

    @Override
    public BitwiseParameter invert(BitwiseParameter rhs, RecognizeData nameEngine) throws NameUnassignedException {
        return bitField.invert(rhs, nameEngine);
    }

    @Override
    public String toIrpString(int radix) {
        return bitField.toIrpString(radix);
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        return bitField.propertiesMap(true, generalSpec, nameEngine);
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
    public long toLong(NameEngine nameEngine) throws NameUnassignedException {
        return bitField.toLong(nameEngine);
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        el.appendChild(bitField.toElement(document));
        return el;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return bitField.getData();
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return bitField.constant(nameEngine);
    }

    @Override
    public BitwiseParameter toBitwiseParameter(RecognizeData recognizeData) {
        return bitField.toBitwiseParameter(recognizeData);
    }
}
