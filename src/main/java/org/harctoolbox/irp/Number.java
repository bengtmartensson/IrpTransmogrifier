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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Number extends PrimaryItem {
    private final static int WEIGHT = 1;

    public final static int SIZE = Long.SIZE;

//    static long parse(String str) {
//        return parse(new ParserDriver(str).getParser().number());
//    }

    static long parse(IrpParser.NumberContext ctx) {
        Number number = new Number(ctx);
        return number.toNumber();
    }

    static Expression newExpression(IrpParser.NumberContext child) {
        String str = child.getText();
        long num = child.HEXINT() != null ? Long.parseLong(str.substring(2), 16)
                : child.BININT() != null ? Long.parseLong(str.substring(2), 2)
                : Long.parseLong(str);
        return new NumberExpression(num);
    }

    public static long parse(String str) {
        return str.equals("UINT8_MAX") ? 255L
                : str.equals("UINT16_MAX") ? 65535L
                : str.equals("UINT24_MAX") ? 16777215L
                : str.equals("UINT32_MAX") ? 4294967295L
                : str.equals("UINT64_MAX") ? -1L // 18446744073709551615
                : str.length() >= 3 && str.substring(0, 2).equals("0x") ? Long.parseLong(str.substring(2), 16)
                : str.length() >= 3 && str.substring(0, 2).equals("0b") ? Long.parseLong(str.substring(2), 2)
                : str.length() >= 1 && str.substring(0, 1).equals("0")  ? Long.parseLong(str, 8)
                : Long.parseLong(str);
    }

    private long data;

    public Number(long n) {
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Number))
            return false;

        return this.data == ((Number) obj).data;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (this.data ^ (this.data >>> 32));
        return hash;
    }

    @Override
    public long toNumber(NameEngine nameEngine) {
        return data;
    }

    @Override
    public long toNumber() {
        return data;
    }

//    @Override
//    public String toString() {
//        return Long.toString(data);
//    }

//    @Override
//    public String toIrpString() {
//        return Long.toString(data);
//    }

    @Override
    public String toIrpString(int radix) {
        return IrpUtils.radixPrefix(radix) + Long.toString(data, radix);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setTextContent(toString());
        return element;
    }

//    @Override
//    public Name toName() {
//        return null;
//    }

    @Override
    public int weight() {
        return WEIGHT;
    }

//    @Override
//    public Long invert(long rhs) {
//        return rhs;
//    }

//    @Override
//    public boolean isUnary() {
//        return true;
//    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(1);
        map.put("value", toString());
        return map;
    }

//    @Override
//    public int numberOfNames() {
//        return 0;
//    }

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) {
        return rhs;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return this;
    }

//    @Override
//    public boolean isName() {
//        return false;
//    }
}
