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

package org.harctoolbox.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;

public abstract class IrpRenderer {

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

    protected static long bitCount(long number) {
        return Long.bitCount(number);
    }

    private IrSequence intro;
    private IrSequence repeat;
    private IrSequence ending;
    private IrSignal irSignal;

    protected IrpRenderer() {
    }

    protected final void setup() {
        intro = setupIntro().toIrSequence();
        repeat = setupRepeat().toIrSequence();
        ending = setupEnding().toIrSequence();
        irSignal = new IrSignal(intro, repeat, ending, getFrequency(), getDutyCycle());
    }

    abstract double getFrequency();

    abstract double getDutyCycle();

    public IrSignal toIrSignal() {
        return new IrSignal(intro, repeat, ending, getFrequency(), getDutyCycle());
    }

    protected abstract IrList setupIntro();
    protected abstract IrList setupRepeat();
    protected abstract IrList setupEnding();

    public abstract IrSignal compute(Map<String, Long> parameters);

    /**
     * @return the irSignal
     */
    public IrSignal getIrSignal() {
        return irSignal;
    }

    protected abstract static class IrList {
        private final List<Double> data;
        private int extentOrigin;
        private final boolean lsbFirst;

        IrList(boolean lsbFirst) {
            this.lsbFirst = lsbFirst;
            data = new ArrayList<>(64);
            extentOrigin = 0;
        }

        private boolean lastWasFlash() {
            return data.size() % 2 != 0;
        }

        public boolean getLsbFirst() {
            return lsbFirst;
        }

        IrSequence toIrSequence() {
            double[] array = new double[data.size()];
            for (int i = 0; i < data.size(); i++)
                array[i] = data.get(i);
            try {
                return new IrSequence(array);
            } catch (OddSequenceLengthException ex) {
                // Programming error
                throw new RuntimeException(ex);
            }
        }

        private void flashGap(double duration, boolean flash) {
            if (lastWasFlash() == flash) {
                int last = data.size() - 1;
                if (last >= 0)
                    data.set(last, data.get(last) + duration);
            } else
                data.add(duration);
        }

        void flash(double duration) {
            flashGap(duration, true);
        }

        void gap(double duration) {
            flashGap(duration, false);
        }

        void extent(double duration) {
            double sum = IrCoreUtils.l1Norm(data, extentOrigin);
            gap(duration - sum);
            extentOrigin = data.size();
        }

        void bitField(long data, long width, long chop, boolean complement, boolean reverse) {
            long realdata = preprocessFiniteBitField(data, width, chop, complement, reverse);
            bitField(realdata, width);
        }

        abstract void bitField(long data, long width);
    }

    protected abstract static class Pwm2IrList extends IrList {
        private final double zeroGap;
        private final double zeroFlash;
        private final double oneGap;
        private final double oneFlash;

        Pwm2IrList(boolean lsbFirst, double zeroFlash, double  zeroGap, double oneFlash, double oneGap) {
            super(lsbFirst);
            this.zeroFlash = zeroFlash;
            this.zeroGap = zeroGap;
            this.oneFlash = oneFlash;
            this.oneGap = oneGap;
        }

        @Override
        void bitField(long data, long width) {
            for (int i = 0; i < (int) width; i++) {
                int shiftamount = getLsbFirst() ? i : (int) width - i -1;
                switch (((int) data >> shiftamount) & 1) {
                    case 0:
                        flash(zeroFlash);
                        gap(zeroGap);
                        break;
                    case 1:
                        flash(oneFlash);
                        gap(oneGap);
                        break;
                    default:
                        throw new ThisCannotHappenException();
                }
            }
        }
    }

    protected abstract static class Pwm4IrList extends IrpRenderer.IrList {

        public static final int BITSPEC_LENGTH = 2;
        public static final int CHUNKSIZE = 2;

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

        Pwm4IrList(boolean lsbFirst, double zeroFlash, double zeroGap, double oneFlash, double oneGap,
                double twoFlash, double twoGap, double threeFlash, double threeGap) {
            super(lsbFirst);
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
        void bitField(long inData, long inWidth) {
            long data = inData;
            long width = inWidth;
            if (pendingBits > 0L) {
                // This code is valid for msb only
                data &= (1L << width) - 1L;
                data |= pendingData << width;
                width += pendingBits;
                pendingBits = 0;
            }
            if (width % 2L != 0L) {
                pendingData = data;
                pendingBits = (int) width;
                width = 0L;
            }
            for (int i = 0; i < (int) width; i += CHUNKSIZE) {
                switch (((int) data >> (width - i - 2)) & 3) {
                    case 0:
                        flash(zeroFlash);
                        gap(zeroGap);
                        break;
                    case 1:
                        flash(oneFlash);
                        gap(oneGap);
                        break;
                    case 2:
                        flash(twoFlash);
                        gap(twoGap);
                        break;
                    case 3:
                        flash(threeFlash);
                        gap(threeGap);
                        break;
                    default:
                        throw new ThisCannotHappenException();
                }
            }
        }
    }

    protected abstract static class BiPhaseIrList extends IrList {

        private static int invert(boolean inverted, int x) {
            return ((x == 0) != inverted) ? 0 : 1;
        }

        private final double halfPeriod;
        private final boolean inverted;

        BiPhaseIrList(boolean lsbFirst, boolean inverted, double halfPeriod) {
            super(lsbFirst);
            this.inverted = inverted;
            this.halfPeriod = halfPeriod;
        }

        @Override
        void bitField(long data, long width) {
            for (int i = 0; i < (int) width; i++) {
                int shiftamount = getLsbFirst() ? i : (int) width - i -1;
                switch (invert(inverted, ((int) data >> shiftamount) & 1)) {
                    case 0:
                        gap(halfPeriod);
                        flash(halfPeriod);
                        break;
                    case 1:
                        flash(halfPeriod);
                        gap(halfPeriod);
                        break;
                    default:
                        throw new ThisCannotHappenException();
                }
            }
        }
    }
}
