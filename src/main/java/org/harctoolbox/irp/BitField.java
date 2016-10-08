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

import java.util.Objects;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * This class implements Bitfields as described in Chapter 5, except for that it does not
 * know how to transform itself into a bitstream -- that is what the class BitStream does.
 * Accordingly, it does not know bit directions.
 *
 * @see BitStream
 */
public abstract class BitField extends IrStreamItem implements Numerical {

    /**
     * Max length of a BitField in this implementation.
     */
    public static final int maxWidth = Long.SIZE - 1; // = 63

    public static BitField newBitField(String str) throws IrpSyntaxException {
        return newBitField(new ParserDriver(str).getParser().bitfield());
    }
    public static BitField newBitField(IrpParser.BitfieldContext ctx) throws IrpSyntaxException {
        BitField instance = (ctx instanceof IrpParser.Finite_bitfieldContext)
                ? new FiniteBitField((IrpParser.Finite_bitfieldContext) ctx)
                : new InfiniteBitField((IrpParser.Infinite_bitfieldContext) ctx);
        instance.parseTree = ctx;
        return instance;
    }

    public static long parse(String str, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        BitField bitField = newBitField(str);
        return bitField.toNumber(nameEngine);
    }
    private IrpParser.BitfieldContext parseTree;

    protected boolean complement;
    protected PrimaryItem data;
    protected PrimaryItem chop;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BitField))
            return false;

        BitField other = (BitField) obj;

        return complement == other.complement
                && data.equals(other.data)
                && chop.equals(other.chop);
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.complement ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.data);
        hash = 53 * hash + Objects.hashCode(this.chop);
        return hash;
    }

    @Override
    public final String toString() {
            return toString(new NameEngine());
    }

    public abstract String toString(NameEngine nameEngine);

    public abstract long getWidth(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException;

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        try {
            return getWidth(nameEngine) == 0;
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return false;
        }
    }

    public boolean hasChop() {
        try {
            return chop.toNumber(null) != 0;
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return true;
        }
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public int weight() {
        return data.weight() + chop.weight();
    }
}
