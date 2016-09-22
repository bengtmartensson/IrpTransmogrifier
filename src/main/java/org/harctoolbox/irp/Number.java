/*
Copyright (C) 2014, 2016 Bengt Martensson.

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class Number extends PrimaryItem {

    public final static int SIZE = Long.SIZE;
    static long parse(String str) {
        return parse(new ParserDriver(str).getParser().number());
    }
    static long parse(IrpParser.NumberContext ctx) {
        Number number = new Number(ctx);
        return number.toNumber();
    }
    private long data;

    public Number(long n) {
        data = n;
    }

    public Number(IrpParser.NumberContext ctx) {
        this(ctx.getText());
    }

    public Number(TerminalNode n) {
        this(n.getText());
    }

    public Number(String str) {
        data = str.equals("UINT8_MAX") ? 255L
                : str.equals("UINT16_MAX") ? 65535L
                : str.equals("UINT24_MAX") ? 16777215L
                : str.equals("UINT32_MAX") ? 4294967295L
                : str.equals("UINT64_MAX") ? -1L // FIXME: 18446744073709551615
                : str.length() >= 3 && str.substring(0, 2).equals("0x") ? Long.parseLong(str.substring(2), 16)
                : str.length() >= 3 && str.substring(0, 2).equals("0b") ? Long.parseLong(str.substring(2), 2)
                : str.length() >= 1 && str.substring(0, 1).equals("0")  ? Long.parseLong(str, 8)
                : Long.parseLong(str);
    }

    @Override
    public long toNumber(NameEngine nameEngine) {
        return data;
    }

    public long toNumber() {
        return data;
    }

    @Override
    public String toString() {
        return Long.toString(data);
    }

    @Override
    public String toIrpString() {
        return Long.toString(data);
    }

    public String toIrpString(int radix) {
        return Long.toString(data, radix);
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("number");
        element.setTextContent(toString());
        return element;
    }

    @Override
    public Name toName() {
        return null;
    }
}
