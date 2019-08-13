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
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * This class implements Bitfields as described in Chapter 5, except for that it does not
 * know how to transform itself into a bitstream -- that is what the class BitStream does.
 * Accordingly, it does not know bit directions.
 *
 * @see BitStream
 */
public abstract class BitField extends IrpObject implements Numerical {

    /**
     * Max length of a BitField in this implementation.
     */
    public static final int MAXWIDTH = Long.SIZE - 1; // = 63
    private static final Logger logger = Logger.getLogger(BitField.class.getName());

    public static BitField newBitField(String str) {
        return newBitField(new ParserDriver(str));
    }

    public static BitField newBitField(ParserDriver parserDriver) {
        return newBitField(parserDriver.getParser().bitfield());
//        int last = parser.bitfield().getStop().getStopIndex();
//        if (last != str.length() - 1)
//            logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", str.substring(0, last + 1));
//    }
//
//    private static BitField newBitField(IrpParser parser) {
//        return newBitField(parser.bitfield());
    }

    public static BitField newBitField(IrpParser.BitfieldContext ctx) {
        BitField instance = (ctx instanceof IrpParser.Finite_bitfieldContext)
                ? new FiniteBitField((IrpParser.Finite_bitfieldContext) ctx)
                : new InfiniteBitField((IrpParser.Infinite_bitfieldContext) ctx);
        return instance;
    }

    public static long parse(String str, NameEngine nameEngine) throws NameUnassignedException {
        BitField bitField = newBitField(str);
        return bitField.toLong(nameEngine);
    }

    static Expression newExpression(IrpParser.BitfieldContext ctx) {
        if (ctx instanceof IrpParser.Infinite_bitfieldContext)
            throw new ThisCannotHappenException("Cannot use an infinite bitfield as expression");
        return FiniteBitField.newExpression((IrpParser.Finite_bitfieldContext) ctx);
    }

    protected boolean complement;
    protected PrimaryItem data;
    protected PrimaryItem chop;

    protected BitField(ParseTree ctx) {
        super(ctx);
    }

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
    public long toLong() throws NameUnassignedException {
        return toLong(NameEngine.empty);
    }

    public abstract String toString(NameEngine nameEngine);

    public abstract long getWidth(NameEngine nameEngine) throws NameUnassignedException;

    public boolean isEmpty(NameEngine nameEngine) throws NameUnassignedException {
        return getWidth(nameEngine) == 0;
    }

    public boolean hasChop() {
        try {
            return chop.toLong() != 0;
        } catch (NameUnassignedException ex) {
            return true;
        }
    }

    @Override
    public int weight() {
        return data.weight() + chop.weight();
    }

    public boolean hasExtent() {
        return false;
    }

    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = IrpUtils.propertiesMap(6, this);
        map.put("data", data.propertiesMap(eval, generalSpec, nameEngine));
        //map.put("width", width.propertiesMap(true, generalSpec));
        try {
            long num = chop.toLong(null);
            if (num != 0)
                map.put("chop", chop.propertiesMap(true, generalSpec, nameEngine));
        } catch (NameUnassignedException ex) {
            map.put("chop", chop.propertiesMap(true, generalSpec, nameEngine));
        }
        map.put("complement", complement);
        return map;
    }

    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return null;
    }

    public Integer numberOfBareDurations(boolean recursive) {
        return 0;
    }

    public abstract BitField substituteConstantVariables(Map<String, Long> constantVariables);

    public boolean constant(NameEngine nameEngine) {
        return data.constant(nameEngine) && chop.constant(nameEngine);
    }
}
