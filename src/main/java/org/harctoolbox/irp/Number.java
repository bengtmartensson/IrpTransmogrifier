/*
Copyright (C) 2014 Bengt Martensson.

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

/**
 *
 */
public class Number extends PrimaryItem {
    //public final static int SIZE = Integer.MAX_VALUE;
    //BigInteger data;

    public final static int SIZE = Long.SIZE;
    long data;

    public Number(long n) {
        data = n;
    }

    public Number(IrpParser.NumberContext ctx) throws IrpSyntaxException {
        this(ctx.INT());
    }

    public Number(TerminalNode n) throws IrpSyntaxException {
        this(n.getText());
    }

    public Number(String str) throws IrpSyntaxException {
        try {
            data = Long.parseLong(str);
        } catch (NumberFormatException ex) {
            throw new IrpSyntaxException("Invalid number: " + str);
        }
    }

    public static long parse(String str) throws IrpSyntaxException {
        Number number = new Number(str);
        return number.toNumber();
    }

    public static long parse(IrpParser.NumberContext ctx) throws IrpSyntaxException {
        Number number = new Number(ctx);
        return number.toNumber();
    }

    @Override
    public long toNumber(NameEngine nameEngine) {
        return data;
    }

    public long toNumber() {
        return data;
    }

    public String toInfixCode() throws IrpSyntaxException {
        return Long.toString(data);
    }
}
