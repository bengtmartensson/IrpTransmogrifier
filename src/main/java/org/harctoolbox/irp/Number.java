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

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.harctoolbox.ircore.IrCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Number extends PrimaryItem {
    private final static int WEIGHT = 1;

    public final static int SIZE = Long.SIZE;

    private static final long UINT8_MAX = 255L;
    private static final long UINT16_MAX = 65535L;
    private static final long UINT24_MAX = 16777215L;
    private static final long UINT32_MAX = 4294967295L;
    private static final long UINT64_MAX = -1L;// "18446744073709551615"

    static NumberExpression newExpression(IrpParser.NumberContext child) {
        return newExpression(child.getText());
    }

    static NumberExpression newExpression(String str) {
        java.lang.Number num = parse(str);
        return new NumberExpression(num);
    }

    static java.lang.Number parse(IrpParser.NumberContext ctx) {
        return parse(ctx.getText());
    }

    public static java.lang.Number parse(String str) {
        int radix;
        String s;
        if (str.length() >= 3 && str.substring(0, 2).equals("0x")) {
            radix = 16;
            s = str.substring(2);
        } else if (str.length() >= 3 && str.substring(0, 2).equals("0b")) {
            radix = 2;
            s = str.substring(2);
        } else if (str.length() >= 2 && str.substring(0, 1).equals("0")) {
            radix = 8;
            s = str.substring(1);
        } else {
            radix = 10;
            s = str;
        }

        return parse(s, radix);
    }

    public static java.lang.Number parse(String str, int radix) {
        try {
            return Long.parseLong(str, radix);
        } catch (NumberFormatException ex) {
            return str.equals("UINT8_MAX") ? UINT8_MAX
                    : str.equals("UINT16_MAX") ? UINT16_MAX
                    : str.equals("UINT24_MAX") ? UINT24_MAX
                    : str.equals("UINT32_MAX") ? UINT32_MAX
                    : str.equals("UINT64_MAX") ? UINT64_MAX
                    : new BigInteger(str, radix);
        }
    }

    private static String pad(String rawString, int length, double noBits) {
        StringBuilder str = new StringBuilder(rawString);
        int effectiveLength = (int) Math.ceil(length/noBits);
        while (str.length() < effectiveLength)
            str.insert(0, '0');
        return str.toString();
    }

    private java.lang.Number data;

    public Number(java.lang.Number n) {
        super(null);
        data = n;
    }

    public Number(IrpParser.NumberContext ctx) {
        this(ctx.getText());
    }

    public Number(TerminalNode n) {
        this(n.getText());
    }

    public Number(String str) {
        this(parse(str));
    }

    public long longValueExact() throws ArithmeticException {
        return data instanceof BigInteger
                ? ((BigInteger) data).longValueExact()
                : data.longValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Number))
            return false;

        Number other = (Number) obj;

        if (data.equals(other.data))
            return true;

        try {
            return longValueExact() == other.longValueExact();
        } catch (ArithmeticException ex) {
            return false;
        }
    }

    public boolean isZero() {
        return data instanceof Long ? data.longValue() == 0 : data.equals(BigInteger.ZERO);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.data);
        return hash;
    }

    public Number shiftRight(int n) {
        return data instanceof Long
                ? new Number(((Long)data) >> n)
                : new Number(((BigInteger) data).shiftRight(n));
    }

    long and(long mask) {
         return data instanceof Long
                ? (Long)data & mask
                : ((BigInteger) data).and(BigInteger.valueOf(mask)).longValueExact();
    }

    public String toString(int radix) {
        return IrCoreUtils.radixPrefix(radix)
                + (data instanceof BigInteger
                        ? ((BigInteger) data).toString(radix)
                        : Long.toUnsignedString(data.longValue(), radix));
    }

    @Override
    public long toLong(NameEngine nameEngine) throws ArithmeticException {
        return toLong();
    }

    @Override
    public BitwiseParameter toBitwiseParameter(RecognizeData recognizeData) {
        return new BitwiseParameter(toLong());
    }

    @Override
    public long toLong() throws ArithmeticException {
        return longValueExact();
    }

    @Override
    public String toIrpString(int radix) {
        return toString(radix);
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
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(1);
        map.put("value", toString());
        return map;
    }

    @Override
    public BitwiseParameter invert(BitwiseParameter rhs, RecognizeData nameEngine) {
        return rhs;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return this;
    }

    @Override
    public PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return this;
    }

    public String formatIntegerWithLeadingZeros(int radix, int length) {
        return data instanceof Long ? IrCoreUtils.formatIntegerWithLeadingZeros(data.longValue(), radix, length)
                : pad(((BigInteger) data).toString(radix), length, Math.log(radix)/Math.log(2.0));
    }

    public boolean testBit(int n) {
         return data instanceof Long
                ? (((Long)data) & (1L << n)) != 0
                : ((BigInteger) data).testBit(n);
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return true;
    }
}
