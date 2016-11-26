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
    protected static long finiteBitField(long data, long width, long chop, boolean complement, boolean reverse) {
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

    protected static long mkBitMask(long length, long chop) {
        return ((1L << length) - 1L) << chop;
    }
    protected static long bitCount(long number) {
        return Long.bitCount(number);
    }
    protected static long bitCount(BitwiseParameter bitwiseParameter) {
        return Long.bitCount(bitwiseParameter.getValue());
    }

    private boolean valid;

    protected IrpDecoder() {
        valid = false;
    }

    public boolean isValid() {
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

    protected boolean assign(BitwiseParameter bitwiseParameter, long value) {
        bitwiseParameter.assign(value);
        return true;
    }


    protected static abstract class DecodeSequence {

        private static final double durationTolerance = 50d;
        private static final double extentTolerance = 1000d;
        private final IrSequence irSequence;
        private int extentBegin;
        private int index;

        DecodeSequence(IrSequence irSequence) {
            this.extentBegin = 0;
            this.irSequence = irSequence;
            index = 0;
        }

        protected void pushback(int amount) {
            index -= amount;
            if (index < 0)
                throw new ThisCannotHappenException();
        }

        protected void pushback() {
            pushback(1);
        }

        protected void pull(int amount) {
            index += amount;
        }

        private boolean duration(double expected) {
            double actual = Math.abs(irSequence.get(index));
            index++;
            return isBetween(actual, expected - durationTolerance, expected + durationTolerance);
        }

        protected boolean duration(double expected, int peek) {
            double actual = Math.abs(irSequence.get(index + peek));
            return isBetween(actual, expected - durationTolerance, expected + durationTolerance);
        }

        protected boolean flash(double expected) {
            return duration(expected);
        }

        protected boolean flash(double expected, int peek) {
            return duration(expected, peek);
        }

        protected boolean gap(double expected) {
            return duration(expected);
        }

        protected boolean gap(double expected, int peek) {
            return duration(expected, peek);
        }

        protected boolean extent(double expected) {
            index++;
            double passed = irSequence.getDuration(extentBegin, index - extentBegin);
            extentBegin = index;
            return isBetween(passed, expected - extentTolerance, expected + extentTolerance);
        }

        protected abstract boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement, boolean reverse);

        protected boolean bitField(BitwiseParameter parameter, long length, boolean complement, boolean reverse) {
            return bitField(parameter, length, 0L, complement, reverse);
        }

        protected boolean bitField(BitwiseParameter parameter, long length, long chop, boolean complement) {
            return bitField(parameter, length, chop, complement, false);
        }

        protected boolean bitField(BitwiseParameter parameter, long length) {
            return bitField(parameter, length, 0L, false, false);
        }

        protected boolean bitField(BitwiseParameter parameter, long length, boolean complement) {
            return bitField(parameter, length, 0L, complement, false);
        }
    }
}
