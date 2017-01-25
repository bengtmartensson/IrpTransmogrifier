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

package org.harctoolbox.decoders;

import java.util.Map;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitwiseParameter;

public abstract class IrpDecoder {
    private static final double frequencyTolerance = 500d;

    private static boolean isBetween(double actual, double lower, double upper) {
            return actual >= lower && actual <= upper;
    }

    // functions evaluating BitFields in expressions
    protected static final long finiteBitField(long data, long width, long chop, boolean complement, boolean reverse) {
        long realdata = preprocessFiniteBitField(data, width, chop, complement, reverse);
        return IrCoreUtils.maskTo(realdata, width);
    }

    private static long preprocessFiniteBitField(long data, long width, long chop, boolean complement, boolean reverse) {
        long realdata = complement ? ~data : data;
        realdata >>>= chop;
        if (reverse)
            realdata = IrCoreUtils.reverse(realdata, (int) width);
        return realdata;
    }

    protected static final long invertFiniteBitField(long data, long width, long chop, boolean complement, boolean reverse) {
        long realdata = complement ? ~data : data;
        realdata &= mkBitMask(width, 0);
        if (reverse)
            realdata = IrCoreUtils.reverse(realdata, (int) width);
        realdata <<= chop;
        return realdata;
    }

    protected static final long mkBitMask(long length, long chop) {
        return ((1L << length) - 1L) << chop;
    }

    protected static final long bitCount(long number) {
        return Long.bitCount(number);
    }

    protected static final long bitCount(BitwiseParameter bitwiseParameter) {
        return Long.bitCount(bitwiseParameter.getValue());
    }

    private boolean valid;

    protected IrpDecoder() {
        valid = false;
    }

    public final boolean isValid() {
        return valid;
    }

    public abstract double getFrequency();

    public abstract double getDutyCycle();

    public abstract Map<String, Long> getParameters();

    public abstract boolean decodeAsIntro(IrSequence irSequence);

    public abstract boolean decodeAsRepeat(IrSequence irSequence);

    public abstract boolean decodeAsEnding(IrSequence irSequence);

    protected final boolean decode(IrSignal irSignal) {
        valid = checkFrequency(irSignal.getFrequency())
                && decodeAsIntro(irSignal.getIntroSequence())
                && decodeAsRepeat(irSignal.getRepeatSequence())
                && decodeAsEnding(irSignal.getEndingSequence());
        return valid;
    }

    private boolean checkFrequency(double frequency) {
        return isBetween(frequency, getFrequency() - frequencyTolerance, getFrequency() + frequencyTolerance);
    }

    protected final boolean assign(BitwiseParameter bitwiseParameter, long value) {
        bitwiseParameter.setExpected(value);
        return true;
    }

    protected static abstract class DecodeSequence {

        // Assumes interleaving
        protected final static int chunkSize = 1;
        protected final static int bitSpecLength = 2;

        protected static final double durationTolerance = 50d;
        protected static final double extentTolerance = 1000d;
        private final boolean lsbFirst;
        protected final IrSequence irSequence;
        protected int extentBegin;
        protected int index;

        DecodeSequence(IrSequence irSequence, boolean lsbFirst) {
            this.lsbFirst = lsbFirst;
            this.extentBegin = 0;
            this.irSequence = irSequence;
            index = 0;
        }

        protected final boolean isFlash() {
            return index % 2 == 0;
        }

        protected final void pushback(int amount) {
            index -= amount;
            if (index < 0)
                throw new ThisCannotHappenException();
        }

        protected final void pushback() {
            pushback(1);
        }

        protected final void pull(int amount) {
            index += amount;
        }

        protected boolean duration(double expected) {
            double actual = Math.abs(irSequence.get(index));
            index++;
            return expected > 50000 ? isBetween(actual, expected - 10000, expected + 10000)
                    : isBetween(actual, expected - durationTolerance, expected + durationTolerance);
        }

        protected boolean duration(double expected, int peek) {
            double actual = Math.abs(irSequence.get(index + peek));
            return isBetween(actual, expected - durationTolerance, expected + durationTolerance);
        }

        // override for non-interleaving
        protected final boolean flash(double expected) {
            return isFlash() && duration(expected);
        }

        // override for non-interleaving
        protected final boolean flash(double expected, int peek) {
            return isFlash() && duration(expected, peek);
        }

        // override for non-interleaving
        protected final boolean gap(double expected) {
            return !isFlash() && duration(expected);
        }

        // override for non-interleaving
        protected final boolean gap(double expected, int peek) {
            return !isFlash() && duration(expected, peek);
        }

        // override for non-interleaving
        protected final boolean extent(double expected) {
            if (isFlash())
                return false;
            index++;
            double passed = irSequence.getDuration(extentBegin, index - extentBegin);
            extentBegin = index;
            return isBetween(passed, expected - extentTolerance, expected + extentTolerance);
        }

        protected abstract int parseChunk();

        /**
         * Consumes data from the ir stream.
         * @param length
         * @return read data
         */
        protected final long parseData(long length) {
            long data = 0L;
            for (int i = 0; i < (int) length; i += chunkSize) {
                int chunk = parseChunk();
                if (chunk == -1)
                    return -1L;
                data = data << chunkSize | (long) chunk;
            }
            return data;
        }

        protected final BitwiseParameter parseData(long length, long chop, boolean complement, boolean reverse) {
            long read = parseData(length);
            long data = invertFiniteBitField(read, length, chop, complement, reverse != lsbFirst);
            return new BitwiseParameter(data, mkBitMask(length, chop));
        }

        /**
         * Checks that the next bitfield is the value contained in the first argument; returns true if so.
         * @param expected
         * @param length
         * @param chop
         * @param complement
         * @param reverse
         * @return
         */
        protected final boolean bitField(long expected, long length, long chop, boolean complement, boolean reverse) {
            BitwiseParameter newParam = parseData(length, chop, complement, reverse);
            return newParam.isConsistent(expected);
        }

        /**
         * Fills in the parameter give as first argument, returns true if successful.
         * @param parameter
         * @param length
         * @param chop
         * @param complement
         * @param reverse
         * @return
         */
        protected final boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement, boolean reverse) {
            BitwiseParameter newParam = parseData(length, chop, complement, reverse);
            if (!parameter.isConsistent(newParam))
                return false;
            parameter.aggregate(newParam);
            return true;
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, boolean complement, boolean reverse) {
            return bitField(parameter, length, 0L, complement, reverse);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement) {
            return bitField(parameter, length, chop, complement, false);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length) {
            return bitField(parameter, length, 0L, false, false);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, boolean complement) {
            return bitField(parameter, length, 0L, complement, false);
        }
    }

    protected static class Pwm2DecodeSequence extends IrpDecoder.DecodeSequence {

        private final double zeroGap;
        private final double zeroFlash;
        private final double oneGap;
        private final double oneFlash;

        Pwm2DecodeSequence(IrSequence irSequence, boolean lsbFirst,
                double zeroFlash, double zeroGap, double oneFlash, double oneGap) {
            super(irSequence, lsbFirst);
            this.zeroGap = zeroGap;
            this.zeroFlash = zeroFlash;
            this.oneGap = oneGap;
            this.oneFlash = oneFlash;
        }

        @Override
        protected int parseChunk() {
            int result =
                      (duration(zeroFlash, 0) && duration(zeroGap, 1)) ? 0
                    : (duration(oneFlash, 0) && duration(oneGap, 1)) ? 1
                    : -1;
            pull(bitSpecLength);
            return result;
        }
    }

    protected static class BiPhaseDecodeSequence extends IrpDecoder.DecodeSequence {

        private final double halfPeriod;
        private final double fullPeriod;
        private boolean between;
        private final boolean inverted; // not inverted; gap, flash -> 0 (e.g. RC6)

        BiPhaseDecodeSequence(IrSequence irSequence, boolean lsbFirst, boolean inverted, double halfPeriod) {
            super(irSequence, lsbFirst);
            this.halfPeriod = halfPeriod;
            this.fullPeriod = 2*halfPeriod;
            this.inverted = inverted;
            between = false;
        }
        protected boolean durationLong(double expected, int peek) {
            double actual = Math.abs(irSequence.get(index + peek));
            return actual >=  expected - durationTolerance;
        }

        public boolean isZero() {
            return isFlash() == inverted;
        }

        @Override
        protected boolean duration(double expected) {
            if (duration(expected, 0)) {
                pull(1);
                between = false;
                return true;
            } else if (durationLong(expected, 0)) {
                between = true;
                return true;
            } else
                return false;
        }

        @Override
        protected int parseChunk() {
            if (between) {
                if (!(duration(fullPeriod, 0) && durationLong(halfPeriod, 1)))
                    return -1;
            } else {
                if (!(duration(halfPeriod, 0) && durationLong(halfPeriod, 1)))
                    return -1;
            }
            int result = isZero() ? 0 : 1;
            between = !duration(halfPeriod, 1);
            pull(between ? 1 : 2);
            return result;
        }
    }
}
