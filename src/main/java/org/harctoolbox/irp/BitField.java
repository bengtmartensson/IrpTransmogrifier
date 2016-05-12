/*
Copyright (C) 2011, 2012, 2013, 2016 Bengt Martensson.

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

import java.util.List;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * This class implements Bitfields as described in Chapter 5, except for that it does not
 * know how to transform itself into a bitstream -- that is what the class BitStream does.
 * Accordingly, it does not know bit directions.
 *
 * @see BitStream
 */
public class BitField extends IrStreamItem implements Numerical {

    /**
     * Max length of a BitField in this implementation.
     */
    public static final int maxWidth = Long.SIZE - 1; // = 63

    private boolean complement;
    boolean reverse;
    boolean infinite;
    //long data;
    //int width = maxWidth;
    //int skip = 0;
    //long value;
    private PrimaryItem data;
    private PrimaryItem width;
    private PrimaryItem chop;

    //private long evaluatePrimaryItem(String s, long deflt) {
    //    return (s == null || s.isEmpty()) ? deflt : Long.parseLong(s);
    //}

    public BitField() {
        data = null;
        chop = null;
        width = null;
        complement = false;
        reverse = false;
        infinite = false;
    }

    public BitField(String str) throws IrpSyntaxException {
        this(new ParserDriver(str).bitfield());
    }

    public BitField(IrpParser.BitfieldContext ctx) throws IrpSyntaxException {
        this();
        init(ctx);
    }

    private void init(IrpParser.BitfieldContext bitfield) throws IrpSyntaxException {
        if (bitfield instanceof IrpParser.Finite_bitfieldContext)
            init((IrpParser.Finite_bitfieldContext) bitfield);
        else
            init((IrpParser.Infinite_bitfieldContext) bitfield);
    }

    private void init(IrpParser.Finite_bitfieldContext ctx) throws IrpSyntaxException {
        int index = 0;
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext)) {
            complement = true;
            index++;
        }
        data = new PrimaryItem(ctx.primary_item(0));
        width = new PrimaryItem(ctx.primary_item(1));
        chop = ctx.primary_item().size() > 2 ? new PrimaryItem(ctx.primary_item(2)) : new PrimaryItem(0);
        reverse = ! (ctx.getChild(index+2) instanceof IrpParser.Primary_itemContext);
    }

    private void init(IrpParser.Infinite_bitfieldContext ctx) throws IrpSyntaxException {
        infinite = true;
        if (! (ctx.getChild(0) instanceof IrpParser.Primary_itemContext))
            complement = true;
        data = new PrimaryItem(ctx.primary_item(0));
        chop = new PrimaryItem(ctx.primary_item(2));
    }

/*
    public void init(boolean complement, boolean reverse, boolean infinite, long data, long width, long skip) throws DomainViolationException {
        if (width > maxWidth)
            throw new DomainViolationException("Max width of bitfields (= " + maxWidth + ") exceeded.");
        if (width < 0)
            throw new DomainViolationException("Width of bitfield must be nonnegative.");
        if (skip > maxWidth)
            throw new DomainViolationException("Max skip value in bitfields (= " + maxWidth + ") exceeded.");
        if (skip < 0)
            throw new DomainViolationException("Skip value of bitfield must be nonnegative.");

        this.complement = complement;
        this.reverse = reverse;
        this.infinite = infinite;
        this.data = data;
        this.width = infinite ? maxWidth : (int) width;
        this.skip = (int) skip;
        compute();
        //Debug.debugBitFields("new Bitfield: " + toString() + " = " + toLong());
    }*/

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long x = data.toNumber(nameEngine) >> chop.toNumber(nameEngine);
        if (complement)
            x = ~x;
        x &= ((1L << width.toNumber(nameEngine)) - 1L);
        if (reverse)
            x = IrpUtils.reverse(x, (int) width.toNumber(nameEngine));

        return x;
        //lsb_value = environment.getBitDirection() == BitDirection.msb ? IrpUtils.reverse(value, width) : value;
    }

    public static long parse(String str, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        BitField bitField = new BitField(str);
        return bitField.toNumber(nameEngine);
    }

    @Override
    public final String toString() {
        try {
            return toString(new NameEngine());
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return "";
        }
    }

    public final String toString(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return (complement ? "~" : "") + data.toNumber(nameEngine) + ":" + (reverse ? "-" : "") + (infinite ? "" : width.toNumber(nameEngine)) + ":" + chop.toNumber(nameEngine);
    }
/*
    public String evaluateAsString() {
        String padding = value >= 0
                ? "0000000000000000000000000000000000000000000000000000000000000000"
                : "1111111111111111111111111111111111111111111111111111111111111111";
        String s = Long.toBinaryString(value);
        if (s.length() > width)
            s = s.substring(s.length() - width);
        else if (s.length() < width)
            s = padding.substring(0, width - s.length()) + s;
        return s;
    }*/

    public long getWidth(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return width.toNumber(nameEngine);
    }

    public boolean isInfinite() {
        return infinite;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        try {
            return width.toNumber(nameEngine) == 0;
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return false;
        }
    }

    @Override
    public List<IrStreamItem> evaluate(BitSpec bitSpec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static void usage(int code) {
        System.out.println("Usage:");
        System.out.println("\tBitfield [-d]? <bitfield> [{ NameEngine }]*");
        System.exit(code);
    }

    public static void main(String[] args) {
        boolean debug = false;
        if (args.length == 0)
            usage(IrpUtils.exitUsageError);
        int arg_i = 0;
        if (args[0].equals("-d")) {
            debug = true;
            arg_i++;
        }
        try {
            NameEngine nameEngine = args.length > arg_i + 1
                    ? new NameEngine(args[arg_i + 1]) : new NameEngine();
            BitField bitField = new BitField(args[arg_i]);
            usage(IrpUtils.exitFatalProgramFailure);
        } catch (ArrayIndexOutOfBoundsException | IrpSyntaxException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.exitUsageError);
        }
    }
}
