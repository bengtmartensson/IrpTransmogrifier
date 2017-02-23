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

package org.harctoolbox.analyze;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;

public class RepeatFinder {

    private static double defaultMinRepeatLastGap = AnalyzeUtils.defaultMinRepeatLastGap; // 20 milli seconds minimum for a repetition
    private static double defaultRelativeTolerance = AnalyzeUtils.defaultRelativeTolerance;
    private static double defaultAbsoluteTolerance = AnalyzeUtils.defaultAbsoluteTolerance;

    /**
     * @return the defaultMinRepeatLastGap
     */
    public static double getDefaultMinRepeatLastGap() {
        return defaultMinRepeatLastGap;
    }

    /**
     * @param aDefaultMinRepeatLastGap the defaultMinRepeatLastGap to set
     */
    public static void setDefaultMinRepeatLastGap(double aDefaultMinRepeatLastGap) {
        defaultMinRepeatLastGap = aDefaultMinRepeatLastGap;
    }

    /**
     * @return the defaultRelativeTolerance
     */
    public static double getDefaultRelativeTolerance() {
        return defaultRelativeTolerance;
    }

    /**
     * @param aDefaultRelativeTolerance the defaultRelativeTolerance to set
     */
    public static void setDefaultRelativeTolerance(double aDefaultRelativeTolerance) {
        defaultRelativeTolerance = aDefaultRelativeTolerance;
    }

    /**
     * @return the defaultabsoluteTolerance
     */
    public static double getDefaultAbsoluteTolerance() {
        return defaultAbsoluteTolerance;
    }

    /**
     * @param aDefaultAbsoluteTolerance the defaultAbsoluteTolerance to set
     */
    public static void setDefaultAbsoluteTolerance(double aDefaultAbsoluteTolerance) {
        defaultAbsoluteTolerance = aDefaultAbsoluteTolerance;
    }

    public static IrSignal findRepeat(ModulatedIrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        RepeatFinder repeatFinder = new RepeatFinder(irSequence, absoluteTolerance, relativeTolerance);
        return repeatFinder.toIrSignal(irSequence);
    }
    public static IrSignal findRepeat(ModulatedIrSequence irSequence) {
        return findRepeat(irSequence, defaultAbsoluteTolerance, defaultRelativeTolerance);
    }
    public static IrSignal findRepeatClean(ModulatedIrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        RepeatFinder repeatFinder = new RepeatFinder(irSequence, absoluteTolerance, relativeTolerance);
        return repeatFinder.toIrSignalClean(irSequence);
    }
    public static IrSignal findRepeatClean(ModulatedIrSequence irSequence) {
        return findRepeatClean(irSequence, defaultAbsoluteTolerance, defaultRelativeTolerance);
    }

    private double relativeTolerance;
    private double absoluteTolerance;
    private double minRepeatLastGap;
    private IrSequence irSequence;
    private RepeatFinderData repeatFinderData;

    public RepeatFinder(IrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        this.absoluteTolerance = absoluteTolerance;
        this.relativeTolerance = relativeTolerance;
        this.minRepeatLastGap = defaultMinRepeatLastGap;
        this.irSequence = irSequence;
        try {
            analyze();
        } catch (Exception ex) {
        }
    }

    public RepeatFinder(IrSequence irSequence) {
        this(irSequence, defaultAbsoluteTolerance, defaultRelativeTolerance);
    }

    public RepeatFinder(int[] data) {
        this(new IrSequence(data), defaultAbsoluteTolerance, defaultRelativeTolerance);
    }

    private void analyze() {
        RepeatFinderData candidate = new RepeatFinderData(irSequence.getLength());
        for (int length = irSequence.getLength() / 4; length >= 2; length--) {
            for (int beginning = 0; beginning < irSequence.getLength() / 2 - length; beginning++) {
                RepeatFinderData newCandidate;
                try {
                    newCandidate = countRepeats(2*beginning, 2*length);
                } catch (OddSequenceLengthException ex) {
                    throw new ThisCannotHappenException();
                }
                if (newCandidate.numberRepeats > 1
                        && newCandidate.lastGap > minRepeatLastGap
                        && newCandidate.repeatsDuration > candidate.repeatsDuration - 0.1)
                    candidate = newCandidate;
            }
        }
        repeatFinderData = candidate;
    }

    private RepeatFinderData countRepeats(int beginning, int length) throws OddSequenceLengthException {
        RepeatFinderData result = new RepeatFinderData(beginning, length, 0, 0);
        result.lastGap = Math.abs(irSequence.get(beginning + length - 1));
        if (result.lastGap < minRepeatLastGap)
            return result; // will be rejected anyhow, save some computations
        for (int hits = 1;; hits++) {
            boolean hit = compareSubSequences(beginning, beginning + hits*length, length);
            if (!hit) {
                result.numberRepeats = hits;
                result.endingLength = irSequence.getLength() - beginning - hits*length;
                result.repeatsDuration = irSequence.getDuration(beginning, hits*length);
                return result;
            }
        }
    }

    private boolean compareSubSequences(int beginning, int compareStart, int length) {
        if (compareStart + length > irSequence.getLength())
            return false;

        return irSequence.approximatelyEquals(beginning, compareStart, length, absoluteTolerance, relativeTolerance, minRepeatLastGap);
    }

    /**
     * @param irSequence
     * @param frequency
     * @return the irSignal
     */
    public IrSignal toIrSignal(IrSequence irSequence, double frequency) {
        return repeatFinderData.chopIrSequence(new ModulatedIrSequence(irSequence, frequency));
    }

    /**
     * @param irSequence
     * @return the irSignal
     */
    public IrSignal toIrSignal(ModulatedIrSequence irSequence) {
        return repeatFinderData.chopIrSequence(irSequence);
    }

    public IrSignal toIrSignalClean(ModulatedIrSequence irSequence) {
        return repeatFinderData.chopIrSequence(Cleaner.clean(irSequence, (int) absoluteTolerance, relativeTolerance));
    }

    public RepeatFinderData getRepeatFinderData() {
        return repeatFinderData;
    }

    public static class RepeatFinderData {
        private int beginLength;
        private int repeatLength;
        private int numberRepeats;
        private int endingLength;
        private double lastGap;
        private double repeatsDuration;

        private RepeatFinderData() {
            setup(0, 0, 0, 0);
        }

        public RepeatFinderData(int length) {
            setup(length, 0, 0, 0);
        }

        public RepeatFinderData(int beginLength, int repeatLength, int numberRepeats, int endingLength) throws OddSequenceLengthException {
            if (beginLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                throw new OddSequenceLengthException("Lengths and start must be even");
            setup(beginLength, repeatLength, numberRepeats, endingLength);
        }

        private void setup(int beginLength, int repeatLength, int numberRepeats, int endingLength) {
            this.beginLength = beginLength;
            this.repeatLength = repeatLength;
            this.numberRepeats = numberRepeats;
            this.endingLength = endingLength;
            this.lastGap = 0;
            this.repeatsDuration = 0;
        }

        @Override
        public String toString() {
            return "beginLength = " + beginLength
                    + "; repeatLength = " + repeatLength
                    + "; numberRepeats = " + numberRepeats
                    + "; endingLength = " + endingLength
                    + "; totalLength = " + totalLength()
                    + "; repeatsDuration = " + repeatsDuration;
        }

        /**
         * @return the repeatStart
         */
        public int getBeginLength() {
            return beginLength;
        }

        /**
         * @return the numberRepeats
         */
        public int getNumberRepeats() {
            return numberRepeats;
        }

        /**
         * @return the repeatLength
         */
        public int getRepeatLength() {
            return repeatLength;
        }

        public int getEndingLength() {
            return endingLength;
        }

        public int getEndingStart() {
            return beginLength + numberRepeats*repeatLength;
        }

        private int totalLength() {
            return beginLength + numberRepeats*repeatLength + endingLength;
        }

        public IrSignal chopIrSequence(ModulatedIrSequence irSequence) {
            try {
                return numberRepeats > 1
                        ? irSequence.toIrSignal(beginLength, repeatLength, numberRepeats)
                        : // no repeat found, just do the trival
                        irSequence.toIrSignal();
            } catch (InvalidArgumentException ex) {
                assert(false); // cannot happen: repeatStart repeatLength have been checked to be even.
                return null;
            }
        }
    }
}
