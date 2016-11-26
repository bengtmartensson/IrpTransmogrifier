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

package org.harctoolbox.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLenghtException;

/**
 *
 */
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

        IrList() {
            data = new ArrayList<>(64);
            extentOrigin = 0;
        }

        private boolean lastWasFlash() {
            return data.size() % 2 != 0;
        }

        IrSequence toIrSequence() {
            double[] array = new double[data.size()];
            for (int i = 0; i < data.size(); i++)
                array[i] = data.get(i);
            try {
                return new IrSequence(array);
            } catch (OddSequenceLenghtException ex) {
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
}
