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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;

/**
 * This object represents a parameter, for which only some bit positions are known.
 */
public final class BitwiseParameter {

    public final static long ALLBITS = -1L;
    public final static long NOBITS = 0L;
    private final static Logger logger = Logger.getLogger(BitwiseParameter.class.getName());
    public final static BitwiseParameter ZERO = new BitwiseParameter(0L);
    public final static BitwiseParameter NULL = new BitwiseParameter();

    private static String toString(long value, long bitmask) {
        return Long.toString(value) + "&" + Long.toBinaryString(bitmask);
    }

    private static boolean isConsistent(long x, long y, long bitmask) {
        return ((x ^ y) & bitmask) == 0L;
    }

    public static int length(long x) {
        return Long.SIZE - Long.numberOfLeadingZeros(x);
    }

    private static boolean covers(long bitmask, long bm) {
        return (bitmask & bm) == bm;
    }

    private static long arithmeticBitmask(long b1, long b2) {
        return b1 & b2;
    }

    private static boolean isTrue(long value) {
        return value != 0L;
    }

    private static boolean ifFalse(long value) {
        return value == 0L;
    }

    private long value;

    /**
     * 1 for bits known
     */
    private long bitmask;

    public BitwiseParameter(long value) {
        this(value, ALLBITS);
    }

    public BitwiseParameter(boolean b) {
        this(b ? 1L : 0L);
    }

    public BitwiseParameter() {
        this(0L, NOBITS);
    }

    public BitwiseParameter(long value, long bitmask) {
        this.value = value & bitmask;
        this.bitmask = bitmask;
    }

    public BitwiseParameter(BitwiseParameter old) {
        this.value = old.value & old.bitmask;
        this.bitmask = old.bitmask;
    }

    public BitwiseParameter restrict(long newBitmask) {
        return new BitwiseParameter(value, bitmask & newBitmask/*, expected*/);
    }

    public boolean coversWidth(long width) {
        return covers(IrCoreUtils.ones(width));
    }

    public boolean covers(long bm) {
        return covers(bitmask, bm);
    }

    boolean isEmpty() {
        return bitmask == NOBITS;
    }

    public int length() {
        return length(bitmask);
    }

    private void canonicalize() {
        value &= bitmask;
    }

    public boolean isConsistent(BitwiseParameter parameter) {
        Objects.requireNonNull(parameter);
        return isConsistent(value, parameter.value, bitmask & parameter.bitmask);
    }

    @SuppressWarnings("null")
    public boolean isConsistent(long val) {
        return isConsistent(/*expected != null ? expected :*/ value, val, bitmask);
    }

    public void checkConsistency(String name, long val) throws ParameterInconsistencyException {
        if (!isConsistent(val))
            throw new ParameterInconsistencyException(name, getValue(), val);
    }

    public void aggregate(BitwiseParameter parameter) {
        parameter.canonicalize();
        logger.log(Level.FINEST, "Changing {0} to {1}", new Object[] { toString(), toString(value | parameter.value, bitmask | parameter.bitmask)});
        value &= ~parameter.bitmask;
        value |= parameter.getValue();
        bitmask |= parameter.bitmask;
        canonicalize();
    }

    public void append(BitwiseParameter other) {
        if (!other.isEmpty()) {
            long size = other.length();
            value = (value << size) | other.value;
            bitmask = (bitmask << size) | other.bitmask;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BitwiseParameter))
            return false;
        BitwiseParameter other = (BitwiseParameter) obj;
        return bitmask == other.bitmask && ((value ^ other.value) & bitmask) == 0L;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 41 * hash + (int) (this.bitmask ^ (this.bitmask >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return toString(value, bitmask/*, expected*/);
    }

    /**
     * @return the value
     */
    public long getValue() {
        return value & bitmask;
    }

    /**
     * @return the bitmask
     */
    public long getBitmask() {
        return bitmask;
    }

    public void assign(long value) {
        this.value = value;
        bitmask = ALLBITS;
        //expected = null;
    }

    BitwiseParameter bitInvert() {
        return new BitwiseParameter(~value, bitmask);
    }

    BitwiseParameter minus() {
        return new BitwiseParameter(-value, bitmask);
    }

    BitwiseParameter bitCount() {
        return new BitwiseParameter(Long.bitCount(value), bitmask);
    }

    BitwiseParameter negation() {
        return new BitwiseParameter(isFalse());
    }

    public BitwiseParameter minus(BitwiseParameter op) {
        return new BitwiseParameter(this.value - op.getValue(), arithmeticBitmask(op.getBitmask()));
    }

    BitwiseParameter plus(BitwiseParameter op) {
        return new BitwiseParameter(value + op.getValue(), arithmeticBitmask(op.getBitmask()));
    }

    BitwiseParameter div(BitwiseParameter op) {
        if (op.getValue() == 0)
            return BitwiseParameter.NULL;
        long d = IrCoreUtils.log2(op.value);
        long bm = bitmask >> d;
        return new BitwiseParameter(this.value / op.getValue(), bm);
    }

    public BitwiseParameter mul(BitwiseParameter op) {
        boolean opIsPowerOf2 = Long.bitCount(op.getValue()) == 1;
        BitwiseParameter operator = opIsPowerOf2 ? op : this;
        long d = operator.value > 0L ? IrCoreUtils.log2(operator.value) : 0L;
        long bm = (bitmask << d) | IrCoreUtils.ones(d);
        return new BitwiseParameter(this.value * op.getValue(), bm);
    }

    public BitwiseParameter xor(BitwiseParameter op) {
        return new BitwiseParameter(this.value ^ op.getValue(), bitmask & op.getBitmask());
    }

    BitwiseParameter power(BitwiseParameter op) {
        return new BitwiseParameter(IrCoreUtils.power(this.value, op.getValue()), ALLBITS);
    }

    BitwiseParameter mod(BitwiseParameter op) {
        return new BitwiseParameter(this.value % op.getValue(), arithmeticBitmask(op.getBitmask()));
    }

    BitwiseParameter leftShift(BitwiseParameter op) {
        return new BitwiseParameter(this.value << op.getValue(), this.bitmask << op.getBitmask());
    }

    BitwiseParameter rightShift(BitwiseParameter op) {
        return new BitwiseParameter(this.value >> op.getValue(), this.bitmask >>> op.getBitmask());
    }

    BitwiseParameter le(BitwiseParameter op) {
        return new BitwiseParameter(this.value <= op.getValue());
    }

    BitwiseParameter ge(BitwiseParameter op) {
        return new BitwiseParameter(this.value >= op.getValue());
    }

    BitwiseParameter lt(BitwiseParameter op) {
        return new BitwiseParameter(this.value < op.getValue());
    }

    BitwiseParameter gt(BitwiseParameter op) {
        return new BitwiseParameter(this.value > op.getValue());
    }

    BitwiseParameter eq(BitwiseParameter op) {
        return new BitwiseParameter(this.value == op.getValue());
    }

    BitwiseParameter ne(BitwiseParameter op) {
        return new BitwiseParameter(this.value != op.getValue());
    }

    BitwiseParameter and(BitwiseParameter op) {
        return new BitwiseParameter(value & op.getValue(), arithmeticBitmask(op.getBitmask()));
    }

    BitwiseParameter or(BitwiseParameter op) {
        return new BitwiseParameter(op.value & op.getValue(), arithmeticBitmask(op.getBitmask()));
    }

    boolean isFalse() {
        return ifFalse(value);
    }

    boolean isTrue() {
        return isTrue(value);
    }

    BitwiseParameter reverse(BitwiseParameter length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long longValueExact() {
        if (this.bitmask != ALLBITS)
            throw new IllegalArgumentException("value not known");
        return value;
    }

    private long arithmeticBitmask(long bm) {
        return arithmeticBitmask(bitmask, bm);
    }

    /**
     * Checks that the value payload is consistent with the current object, and throws a SignalRecognitionException if not.
     *
     * @param payload
     * @param bitmask
     * @return true if the current object covers the bitmask given as argument.
     * @throws SignalRecognitionException
     */
    public boolean check(long payload, long bitmask) throws SignalRecognitionException {
        if (!this.isConsistent(payload))
            throw new SignalRecognitionException("BitwiseParameter " + toString() + " not consistent with previously read data = " + payload);
        return covers(bitmask);
    }

    public boolean isFinished(Long bitmask) {
        Objects.requireNonNull(bitmask);
        return covers(bitmask);
    }

    public boolean isKnown() {
        return bitmask == ALLBITS;
    }
}
