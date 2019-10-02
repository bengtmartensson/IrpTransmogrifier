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
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * This object represents a parameter, for which only some bit positions are known.
 */
public final class BitwiseParameter implements Cloneable {

    public final static long ALLBITS = -1L;
    public final static long NOBITS = 0L;
    private final static Logger logger = Logger.getLogger(BitwiseParameter.class.getName());

    private static String toString(long value, long bitmask, Long expected) {
        return Long.toString(value) + "&" + Long.toBinaryString(bitmask)
                + (expected != null ? (" (" + expected + ")") : "");
    }

    private static String toString(long value, long bitmask) {
        return toString(value, bitmask, null);
    }

    private static boolean isConsistent(long x, long y, long bitmask) {
        return ((x ^ y) & bitmask) == 0L;
    }

    private long value;

    /**
     * 1 for bits known
     */
    private long bitmask;

    private Long expected;

    public BitwiseParameter(long value) {
        this(value, ALLBITS);
    }

    public BitwiseParameter() {
        this(0L, NOBITS);
    }

    public BitwiseParameter(long value, long bitmask, Long expected) {
        this.value = value & bitmask;
        this.bitmask = bitmask;
        this.expected = expected;
    }

    public BitwiseParameter(long value, long bitmask) {
        this(value, bitmask, null);
    }

    public void setExpected(Long expected) {
        this.expected = expected;
    }

    boolean isEmpty() {
        return bitmask == NOBITS;
    }

    public int length() {
        int result = 0;
        long bm = bitmask;
        while (bm != 0) {
            bm >>= 1;
            result++;
        }
        return result;
    }

    private void canonicalize() {
        value &= bitmask;
    }

    public boolean isConsistent(BitwiseParameter parameter) {
        Objects.requireNonNull(parameter);
        return parameter.expected != null ? isConsistent(parameter.expected)
                : expected != null ? parameter.isConsistent(this)
                : isConsistent(value, parameter.value, bitmask & parameter.bitmask);
    }

    @SuppressWarnings("null")
    public boolean isConsistent(long val) {
        return isConsistent(expected != null ? expected : value, val, bitmask);
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
        expected = null;
    }

    public void append(BitwiseParameter other) {
        if (!other.isEmpty()) {
            long size = other.length();
            value = (value << size) | other.value;
            bitmask = (bitmask << size) | other.bitmask;
            expected = (expected != null && other.expected != null)
                    ? (expected << size) | other.expected
                    : null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BitwiseParameter))
            return false;
        BitwiseParameter other = (BitwiseParameter) obj;
        return bitmask == other.bitmask
                && ((expected == null && other.expected == null)
                    || ((expected != null && other.expected != null) && (expected.equals(other.expected))))
                && ((value ^ other.value) & bitmask) == 0L;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 41 * hash + (int) (this.bitmask ^ (this.bitmask >>> 32));
        hash = 41 * hash + Objects.hashCode(this.expected);
        return hash;
    }

    @Override
    public String toString() {
        return toString(value, bitmask, expected);
    }


    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public BitwiseParameter clone() {
        try {
            return (BitwiseParameter) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new ThisCannotHappenException();
        }
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
        expected = null;
    }

    @SuppressWarnings("null")
    public long getValuePreferExpected() {
        return expected != null ? expected : getValue();
    }
}
