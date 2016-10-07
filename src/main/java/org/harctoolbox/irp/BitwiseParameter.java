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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * This object represents a parameter, for which only some bit positions are known.
 */
class BitwiseParameter implements Cloneable {

    public final static long ALLBITS = -1L;
    public final static long NOBITS = 0L;
    private final static Logger logger = Logger.getLogger(BitwiseParameter.class.getName());

    private static String toString(long value, long bitmask) {
        return Long.toString(value) + "&" + Long.toBinaryString(bitmask);
    }

    private long value;

    /**
     * 1 for bits known
     */
    private long bitmask;

    //private boolean needsChecking;

    BitwiseParameter(long value) {
        this(value, ALLBITS);
    }

    BitwiseParameter() {
        this(0L, NOBITS);
    }

    BitwiseParameter(long value, long bitmask) {
        this.value = value & bitmask;
        this.bitmask = bitmask;
        //this.needsChecking = false;
    }

    boolean isEmpty() {
        return bitmask == NOBITS;
    }

    private void canonicalize() {
        value &= bitmask;
    }

    public boolean isConsistent(BitwiseParameter parameter) {
        return ((value ^ parameter.value) & bitmask & parameter.bitmask) == 0L;
    }

    boolean isConsistent(long val) {
        return ((value ^ val) & bitmask) == 0L;
    }

    void aggregate(BitwiseParameter parameter) {
        parameter.canonicalize();
        logger.log(Level.FINEST, "Changing {0} to {1}", new Object[] { toString(), toString(value | parameter.value, bitmask | parameter.bitmask)});
        value |= parameter.value;
        bitmask |= parameter.bitmask;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BitwiseParameter))
            return false;
        BitwiseParameter other = (BitwiseParameter) obj;
        return bitmask == other.bitmask
                && ((value ^ other.value) & bitmask) == 0L;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 37 * hash + (int) (this.bitmask ^ (this.bitmask >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return toString(value, bitmask);
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

//    /**
//     * @return the needsChecking
//     */
//    public boolean isNeedsChecking() {
//        return needsChecking;
//    }
//
//    /**
//     * @param needsChecking the needsChecking to set
//     */
//    public void setNeedsChecking(boolean needsChecking) {
//        this.needsChecking = needsChecking;
//    }
}
