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
    private IrpParser.BitfieldContext parseTree;

    protected boolean complement;
    //protected boolean reverse;
    //boolean infinite;
    //long data;
    //int width = maxWidth;
    //int skip = 0;
    //long value;
    protected PrimaryItem data;
    //protected PrimaryItem width;
    protected PrimaryItem chop;

    //private long evaluatePrimaryItem(String s, long deflt) {
    //    return (s == null || s.isEmpty()) ? deflt : Long.parseLong(s);
    //}

//    public BitField() {
//        data = null;
//        chop = null;
//        width = null;
//        complement = false;
//        reverse = false;
//        //infinite = false;
//    }

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

    public static long parse(String str, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        BitField bitField = newBitField(str);
        return bitField.toNumber(nameEngine);
    }

    @Override
    public final String toString() {
            return toString(new NameEngine());
    }

    public abstract String toString(NameEngine nameEngine);


//    public String evaluateAsString() {
//        String padding = value >= 0
//                ? "0000000000000000000000000000000000000000000000000000000000000000"
//                : "1111111111111111111111111111111111111111111111111111111111111111";
//        String s = Long.toBinaryString(value);
//        if (s.length() > width)
//            s = s.substring(s.length() - width);
//        else if (s.length() < width)
//            s = padding.substring(0, width - s.length()) + s;
//        return s;
//    }

    public abstract long getWidth(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException;

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        try {
            return getWidth(nameEngine) == 0;
        } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
            return false;
        }
    }

//    private static void usage(int code) {
//        System.out.println("Usage:");
//        System.out.println("\tBitfield [-d]? <bitfield> [{ NameEngine }]*");
//        System.exit(code);
//    }

//    public static void main(String[] args) {
//        boolean debug = false;
//        if (args.length == 0)
//            usage(IrpUtils.exitUsageError);
//        int arg_i = 0;
//        if (args[0].equals("-d")) {
//            debug = true;
//            arg_i++;
//        }
//        try {
//            NameEngine nameEngine = args.length > arg_i + 1
//                    ? new NameEngine(args[arg_i + 1]) : new NameEngine();
//            BitField bitField = newBitField(args[arg_i]);
//            usage(IrpUtils.exitFatalProgramFailure);
//        } catch (ArrayIndexOutOfBoundsException | IrpSyntaxException ex) {
//            System.err.println(ex.getMessage());
//            usage(IrpUtils.exitUsageError);
//        }
//    }

//    BitField append(BitField bitField, BitDirection bitDirection) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    boolean interleavingOk() {
        return true;
    }

    public boolean hasChop() {
        return !(chop instanceof Number && ((Number) chop).toNumber(null) == 0);
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }
}
