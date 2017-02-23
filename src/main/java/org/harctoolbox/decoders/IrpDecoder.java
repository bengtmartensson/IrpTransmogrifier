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
import org.harctoolbox.irp.IrpException;

public abstract class IrpDecoder {

    private static boolean isClose(double x, double y, double tolerance) {
            return x >= y - tolerance && x <= y + tolerance;
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

    protected double getFrequencyTolerance() {
        return IrCoreUtils.defaultFrequencyTolerance;
    }

    public final boolean isValid() {
        return valid;
    }

    public abstract double getFrequency();

    public abstract double getDutyCycle();

    public abstract Map<String, Long> getParameters();

    public void decodeAsIntro(IrSequence irSequence) throws DecodeException {
        if (!irSequence.isEmpty())
           throw new DecodeException("Sequence expected to be empty");
    }

    public void decodeAsRepeat(IrSequence irSequence) throws DecodeException {
        if (!irSequence.isEmpty())
           throw new DecodeException("Sequence expected to be empty");
    }

    public void decodeAsEnding(IrSequence irSequence) throws DecodeException {
        if (!irSequence.isEmpty())
           throw new DecodeException("Sequence expected to be empty");
    }

    protected final void decode(IrSignal irSignal) throws DecodeException {
        checkFrequency(irSignal.getFrequency());
        decodeAsIntro(irSignal.getIntroSequence());
        decodeAsRepeat(irSignal.getRepeatSequence());
        decodeAsEnding(irSignal.getEndingSequence());
        valid = true;
    }

    private void checkFrequency(double frequency) throws DecodeException {
        if (!isClose(frequency, getFrequency(), getFrequencyTolerance()))
            throw new DecodeException();
    }

    protected final boolean assign(BitwiseParameter bitwiseParameter, long value) {
        bitwiseParameter.setExpected(value);
        return true;
    }

    protected static abstract class DecodeSequence {

        // Assumes interleaving
        private final static double largeDurationTolerance = 15000d;
        private final static double largeDurationThreshold = 20000d;

        private final int chunkSize;
        private final boolean lsbFirst;
        private final IrSequence irSequence;
        private int extentBegin;
        private int index;

        DecodeSequence(IrSequence irSequence, boolean lsbFirst, int chunkSize) {
            this.lsbFirst = lsbFirst;
            this.chunkSize = chunkSize;
            this.extentBegin = 0;
            this.irSequence = irSequence;
            index = 0;
        }

        protected boolean getLsbFirst() {
            return lsbFirst;
        }

        protected double getAbsoluteTolerance() {
            return IrCoreUtils.defaultAbsoluteTolerance;
        }

        protected double getRelativeTolerance() {
            return IrCoreUtils.defaultRelativeTolerance;
        }

        private boolean acceptablyClose(double x, double y) {
            return IrCoreUtils.approximatelyEquals(x, y, getAbsoluteTolerance(), getRelativeTolerance());
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

        protected final double actual() throws DecodeException {
            return actual(0);
        }

        protected final double actual(int offset) throws DecodeException {
            try {
                return Math.abs(irSequence.get(index + offset));
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new DecodeException(ex);
            }
        }

        protected void duration(double expected) throws DecodeException {
            boolean result = duration(expected, 0);
            index++;
            if (!result)
                throw new DecodeException();
        }

        protected boolean duration(double expected, int peek) throws DecodeException {
            double actual = actual(peek);
            return acceptablyClose(actual, expected);
//            double  tolerance = expected > largeDurationThreshold ? largeDurationTolerance : durationTolerance;
//            return isClose(actual, expected, tolerance);
        }

        protected final boolean durationLong(double expected, int peek) throws DecodeException {
            double actual = actual(peek);
            return actual >=  expected - getAbsoluteTolerance();
        }

        // override for non-interleaving
        protected final void flash(double expected) throws DecodeException {
            if (!isFlash())
                throw new DecodeException();
            duration(expected);
        }

        // override for non-interleaving
        protected final boolean flash(double expected, int peek) throws DecodeException {
            return isFlash() && duration(expected, peek);
        }

        // override for non-interleaving
        protected void gap(double expected) throws DecodeException {
            if (isFlash())
                throw new DecodeException();
            duration(expected);
        }

        // override for non-interleaving
        protected final void gap(double expected, int peek) throws DecodeException {
            if (isFlash())
                throw new DecodeException();
            duration(expected, peek);
        }

        // override for non-interleaving
        protected final boolean extent(double expected) {
            if (isFlash())
                return false;
            index++;
            double passed = irSequence.getDuration(extentBegin, index - extentBegin);
            extentBegin = index;
            return acceptablyClose(passed, expected);
        }

        protected abstract Integer parseChunk() throws DecodeException;

        /**
         * Consumes data from the ir stream.
         * @param length
         * @return read data
         * @throws org.harctoolbox.decoders.IrpDecoder.DecodeException
         */
        protected long parseData(long length) throws DecodeException {
            long data = 0L;
            for (int i = 0; i < (int) length; i += chunkSize) {
                int chunk = parseChunk();
                data = data << chunkSize | (long) chunk;
            }
            return data;
        }

        protected final BitwiseParameter parseData(long length, long chop, boolean complement, boolean reverse) throws DecodeException {
            long read = parseData(length);
            if (read < 0)
                return null;
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
         * @throws org.harctoolbox.decoders.IrpDecoder.DecodeException
         */
        protected final boolean bitField(long expected, long length, long chop, boolean complement, boolean reverse) throws DecodeException {
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
         * @throws org.harctoolbox.decoders.IrpDecoder.DecodeException
         */
        protected final boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement, boolean reverse) throws DecodeException {
            BitwiseParameter newParam = parseData(length, chop, complement, reverse);
            if (newParam == null || !parameter.isConsistent(newParam))
                return false;
            parameter.aggregate(newParam);
            return true;
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, boolean complement, boolean reverse) throws DecodeException {
            return bitField(parameter, length, 0L, complement, reverse);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement) throws DecodeException {
            return bitField(parameter, length, chop, complement, false);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length) throws DecodeException {
            return bitField(parameter, length, 0L, false, false);
        }

        protected final boolean bitField(BitwiseParameter parameter, long length, boolean complement) throws DecodeException {
            return bitField(parameter, length, 0L, complement, false);
        }
    }

    protected static class Pwm2DecodeSequence extends IrpDecoder.DecodeSequence {
        public static final int bitSpecLength = 2;
        public static final int chunkSize = 1;

        private final double zeroGap;
        private final double zeroFlash;
        private final double oneGap;
        private final double oneFlash;

        Pwm2DecodeSequence(IrSequence irSequence, boolean lsbFirst,
                double zeroFlash, double zeroGap, double oneFlash, double oneGap) {
            super(irSequence, lsbFirst, chunkSize);
            this.zeroGap = zeroGap;
            this.zeroFlash = zeroFlash;
            this.oneGap = oneGap;
            this.oneFlash = oneFlash;
        }

        @Override
        protected Integer parseChunk() throws DecodeException {
            Integer result =
                      (duration(zeroFlash, 0) && duration(zeroGap, 1)) ? 0
                    : (duration(oneFlash, 0) && duration(oneGap, 1)) ? 1
                    : null;
            if (result == null)
                throw new DecodeException("parseChunk");
            pull(bitSpecLength);
            return result;
        }

        /**
         * @return the zeroGap
         */
        protected double getZeroGap() {
            return zeroGap;
        }

        /**
         * @return the zeroFlash
         */
        protected double getZeroFlash() {
            return zeroFlash;
        }

        /**
         * @return the oneGap
         */
        protected double getOneGap() {
            return oneGap;
        }

        /**
         * @return the oneFlash
         */
        protected double getOneFlash() {
            return oneFlash;
        }
    }

    protected static class SonyTypeDecodeSequence extends IrpDecoder.Pwm2DecodeSequence {
        protected double excess;

        SonyTypeDecodeSequence(IrSequence irSequence, boolean lsbFirst,
                double zeroFlash, double zeroGap, double oneFlash, double oneGap) {
            super(irSequence, lsbFirst,
                zeroFlash, zeroGap, oneFlash, oneGap);
            if (IrCoreUtils.approximatelyEquals(zeroFlash, oneFlash))
                throw new ThisCannotHappenException();
            excess = 0d;
        }

        @Override
        protected void gap(double expected) throws DecodeException {
            double stillExpected = expected + excess;
            if (duration(stillExpected, 0)) {
                pull(1);
                excess = 0;
            } else if (durationLong(stillExpected, 0)) {
                excess = expected;
            } else
                throw new DecodeException();
        }

        @Override
        protected Integer parseChunk() throws DecodeException {
            int result;
            boolean success;

            if (duration(getZeroFlash(), 0)) {
                pull(1);
                result = 0;
                gap(getZeroGap());
            } else if (duration(getOneFlash(), 0)) {
                pull(1);
                result = 1;
                gap(getOneGap());
            } else
                throw new DecodeException("");

            return result;
        }
    }

    protected static class Pwm4DecodeSequence extends IrpDecoder.DecodeSequence {

        public static final int bitSpecLength = 2;
        public static final int chunkSize = 2;

        private final double zeroGap;
        private final double zeroFlash;
        private final double oneGap;
        private final double oneFlash;
        private final double twoGap;
        private final double twoFlash;
        private final double threeGap;
        private final double threeFlash;

        private int pendingBits;
        private long pendingData;

        Pwm4DecodeSequence(IrSequence irSequence, boolean lsbFirst, double zeroFlash, double zeroGap, double oneFlash, double oneGap,
                double twoFlash, double twoGap, double threeFlash, double threeGap) {
            super(irSequence, lsbFirst, chunkSize);
            this.zeroGap = zeroGap;
            this.zeroFlash = zeroFlash;
            this.oneGap = oneGap;
            this.oneFlash = oneFlash;
            this.twoGap = twoGap;
            this.twoFlash = twoFlash;
            this.threeGap = threeGap;
            this.threeFlash = threeFlash;
            pendingBits = 0;
            pendingData = 0L;
        }

        @Override
        protected long parseData(long length) throws DecodeException {
            long data = 0L;
            int width = 0;
            if (pendingBits > 0) {
                // This code is valid for msb first only
                assert(!getLsbFirst());
                data = pendingData;
                width = pendingBits;
                pendingBits = 0;
            }

            while (width < (int) length) {
                long chunk = parseChunk();
                data = data << chunkSize | chunk;
                width += chunkSize;
            }
            if (length != width) {
                pendingData = IrCoreUtils.maskTo(data, width - length);
                pendingBits = width - (int) length;
                data >>= width - (int) length;
            }
            return data;
        }

        @Override
        protected Integer parseChunk() throws DecodeException {
            Integer result =
                      (duration(zeroFlash, 0) && duration(zeroGap, 1)) ? 0
                    : (duration(oneFlash, 0) && duration(oneGap, 1)) ? 1
                    : (duration(twoFlash, 0) && duration(twoGap, 1)) ? 2
                    : (duration(threeFlash, 0) && duration(threeGap, 1)) ? 3
                    : null;
            if (result == null)
                throw new DecodeException("parseChunk");
            pull(bitSpecLength);
            return result;
        }
    }

    protected abstract static class NonInterlacedDecodeSequence extends IrpDecoder.DecodeSequence {

        protected double consumed;

        NonInterlacedDecodeSequence(IrSequence irSequence, boolean lsbFirst, int chunkSize) {
            super(irSequence, lsbFirst, chunkSize);
            consumed = 0d;
        }

        @Override
        protected void duration(double expected) throws DecodeException {
            double stillExpected = expected + consumed;
            if (duration(stillExpected, 0)) {
                pull(1);
                consumed = 0;
            } else if (durationLong(stillExpected, 0)) {
                //excess = expected;
                consumed += expected;
            } else
                throw new DecodeException();
        }
    }

    protected static class BiPhaseDecodeSequence extends IrpDecoder.NonInterlacedDecodeSequence {
        public final static int chunkSize = 1;
        private final double halfPeriod;
        private final boolean inverted; // not inverted; gap, flash -> 0 (e.g. RC6)

        BiPhaseDecodeSequence(IrSequence irSequence, boolean lsbFirst, boolean inverted, double halfPeriod) {
            super(irSequence, lsbFirst, chunkSize);
            this.halfPeriod = halfPeriod;
            this.inverted = inverted;
        }

        public boolean isZero() {
            return isFlash() == inverted;
        }

        @Override
        protected Integer parseChunk() throws DecodeException {
            int result = isZero() ? 0 : 1;
            duration(halfPeriod);
            duration(halfPeriod);

            return result;
        }
    }

    protected static class DecodeException extends IrpException {

        DecodeException(String message) {
            super(message);
        }

        DecodeException(Throwable ex) {
            super(ex);
        }

        DecodeException() {
            super("");
        }
    }
}
