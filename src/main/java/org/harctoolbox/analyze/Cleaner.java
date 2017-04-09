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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;

public class Cleaner {

    private final static int NUMBEROFINITIALTIMINGSCAPACITY = 20;
    private static final int NO_LETTERS = 26;

    public static IrSequence clean(IrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        Cleaner cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        try {
            return new IrSequence(cleaner.toDurations());
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

    public static IrSequence clean(IrSequence irSequence) {
        return clean(irSequence, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    public static ModulatedIrSequence clean(ModulatedIrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        return new ModulatedIrSequence(clean((IrSequence)irSequence, absoluteTolerance, relativeTolerance),
                irSequence.getFrequency(), irSequence.getDutyCycle());
    }

    public static ModulatedIrSequence clean(ModulatedIrSequence irSequence) {
        return clean(irSequence, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    public static IrSignal clean(IrSignal irSignal, Double absoluteTolerance, Double relativeTolerance) throws InvalidArgumentException {
        ModulatedIrSequence irSequence = irSignal.toModulatedIrSequence(1);
        Cleaner cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        IrSequence cleansed = new IrSequence(cleaner.toDurations());
        return new IrSignal(cleansed, irSignal.getIntroLength(), irSignal.getRepeatLength(), irSignal.getFrequency(), irSignal.getDutyCycle());
    }

    public static String mkName(Integer n) {
        if (n == null || n < 0)
            throw new IllegalArgumentException("mkName requires a non-negative argument");
        return n >= NO_LETTERS ? (mkName(n / NO_LETTERS) + mkName(n % NO_LETTERS))
                : new String(new char[]{(char) ('A' + n)});
    }

    private int rawData[];
    private List<Integer> dumbTimingsTable;
    protected List<Integer> timings;
    private HashMap<Integer, HistoPair> rawHistogram;
    private HashMap<Integer, HistoPair> cleanedHistogram;
    protected int indexData[];
    private int[] sorted;
    private HashMap<Integer, Integer> lookDownTable;
    private List<Integer> gapsSortedAfterFrequency;
    private List<Integer> flashesSortedAfterFrequency;
    private int[] indices; // ending indicies
    private boolean signalMode;

    public Cleaner(IrSequence irSequence) {
        this(irSequence, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    public Cleaner(IrSequence irSequence, Double absoluteTolerance, Double relativeTolerance) {
        this(irSequence.toInts(), new int[]{ irSequence.getLength() }, false, absoluteTolerance, relativeTolerance);
    }

    protected Cleaner(int[] data, int[] indices, boolean signalMode, Double absoluteTolerance, Double relativeTolerance) {
        double relTol = IrCoreUtils.getRelativeTolerance(relativeTolerance);
        double absTol = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
        rawData = data;
        this.indices = indices;
        this.signalMode = signalMode;
        createRawHistogram();
        createDumbTimingsTable(absTol, relTol);
        improveTimingsTable(absTol, relTol);
        createCookedData();
        createCleanHistogram();
        createSortedGapsAndFlashes();
    }

    /**
     * @return the signalMode
     */
    public boolean isSignalMode() {
        return signalMode;
    }

    private void createRawHistogram() {
        rawHistogram = new HashMap<>(NUMBEROFINITIALTIMINGSCAPACITY);
        for (int i = 0; i < rawData.length; i++) {
            boolean isFlash = i % 2 == 0;
            int duration = rawData[i];
            if (!rawHistogram.containsKey(duration))
                rawHistogram.put(duration, new HistoPair());
            rawHistogram.get(duration).increment(isFlash);
        }
    }

    private void createDumbTimingsTable(double absoluteTolerance, double relativeTolerance) {
        dumbTimingsTable = new ArrayList<>(rawData.length);
        sorted = rawData.clone();
        Arrays.sort(sorted);
        int last = Integer.MIN_VALUE;
        for (int d : sorted) {
            if (!IrCoreUtils.approximatelyEquals(d, last, (int) absoluteTolerance, relativeTolerance)) {
                dumbTimingsTable.add(d);
                last = d;
            }
        }
    }

    private void improveTimingsTable(double absoluteTolerance, double relativeTolerance) {
        lookDownTable = new HashMap<>(NUMBEROFINITIALTIMINGSCAPACITY);
        timings = new ArrayList<>(NUMBEROFINITIALTIMINGSCAPACITY);
        int indexInSortedTimings = 0;
        for (int timingsIndex = 0; timingsIndex < dumbTimingsTable.size(); timingsIndex++) {
            int dumbTiming = dumbTimingsTable.get(timingsIndex);
            int sum = 0;
            int terms = 0;
            int lastDuration = -1;
            while (indexInSortedTimings < sorted.length
                    && IrCoreUtils.approximatelyEquals(dumbTiming, sorted[indexInSortedTimings], (int) absoluteTolerance, relativeTolerance)) {
                int duration = sorted[indexInSortedTimings];
                indexInSortedTimings++;
                if (duration == lastDuration)
                    continue;
                lastDuration = duration;
                int noHits = rawHistogram.get(duration).total();
                sum += noHits * duration;
                terms += noHits;
                lookDownTable.put(duration, timingsIndex);
            }
            int average = (int) Math.round(sum/(double)terms);
            timings.add(average);
            lookDownTable.put(average, timingsIndex);
        }
    }

    private void createCookedData() {
        indexData = new int[rawData.length];
        for (int i = 0; i < rawData.length; i++)
            indexData[i] = lookDownTable.get(rawData[i]);
    }

    private void createCleanHistogram() {
        cleanedHistogram = new LinkedHashMap<>(NUMBEROFINITIALTIMINGSCAPACITY);
        timings.stream().forEach((duration) -> {
            cleanedHistogram.put(duration, new HistoPair());
        });
        rawHistogram.entrySet().stream().forEach((kvp) -> {
            int index = lookDownTable.get(kvp.getKey());
            Integer cleanedDuration = timings.get(index);
            cleanedHistogram.get(cleanedDuration).add(kvp.getValue());
        });
    }

    public String getName(int duration) {
        return mkName(getIndex(duration));
    }

    private int[] toDurations() {
        return toDurations(0, rawData.length);
    }

    protected int[] toDurations(int beg, int length) {
        int[] data = new int[length];
        for (int i = 0; i < length; i++)
            data[i] = timings.get(indexData[beg + i]);
        return data;
    }

    protected String toTimingsString(int beg, int length) {
        StringJoiner str = new StringJoiner(" ");
        for (int i = 0; i < length; i += 2) {
            StringBuilder s = new StringBuilder(2);
            s.append(mkName(indexData[beg + i]));
            s.append(mkName(indexData[beg + i + 1]));
            str.add(s);
        }
        return str.toString();
    }

    public String toTimingsString() {
        return toTimingsString(0, rawData.length);
    }

    protected int getTotalDuration(int beg, int length) {
        int sum = 0;
        for (int i = beg; i < beg + length; i++)
            sum += timings.get(indexData[i]);
        return sum;
    }

    public int getTiming(int index) {
        return  timings.get(index);
    }

    public Integer getIndex(int duration) {
        return lookDownTable.get(duration);
    }

    private List<Integer> getFalshesOrGaps(boolean isFlash) {
        List<Integer> list = new ArrayList<>(timings.size());
        timings.stream().filter((d) -> (cleanedHistogram.get(d).get(isFlash) > 0)).forEach((d) -> {
            list.add(d);
        });
        return list;
    }

    public List<Integer> getGaps() {
        return getFalshesOrGaps(false);
    }

    public List<Integer> getFlashes() {
        return getFalshesOrGaps(true);
    }

    /**
     * @return the cleanedHistogram
     */
    public HashMap<Integer, Integer> getCleanedHistogram() {
        HashMap<Integer, Integer> result = new LinkedHashMap<>(cleanedHistogram.size());
        cleanedHistogram.entrySet().stream().forEach((kvp) -> {
            result.put(kvp.getKey(), kvp.getValue().total());
        });
        return result;
    }

    public int getNumberGaps(int duration) {
        return cleanedHistogram.get(duration).numberGaps;
    }

    public int getNumberFlashes(int duration) {
        return cleanedHistogram.get(duration).numberFlashes;
    }

    public int getNumberPairs(int flash, int gap) {
        Integer igap = getIndex(gap);
        Integer iflash = getIndex(flash);
        if (igap == null || iflash == null)
            throw new ThisCannotHappenException();

        int result = 0;
        for (int i = 0; i < indexData.length - 1; i += 2)
            if (indexData[i] == iflash && indexData[i + 1] == igap)
                result++;

        return result;
    }

    private void createSortedGapsAndFlashes() {
        gapsSortedAfterFrequency = getFalshesOrGaps(false);
        Collections.sort(gapsSortedAfterFrequency,    (a, b) -> cleanedHistogram.get(b).numberGaps    - cleanedHistogram.get(a).numberGaps);
        flashesSortedAfterFrequency = getFalshesOrGaps(true);
        Collections.sort(flashesSortedAfterFrequency, (a, b) -> cleanedHistogram.get(b).numberFlashes - cleanedHistogram.get(a).numberFlashes);
    }

    public int getGapsSortedAfterFrequency(int i) {
        return gapsSortedAfterFrequency.get(i);
    }

    public int getFlashesSortedAfterFrequency(int i) {
        return flashesSortedAfterFrequency.get(i);
    }

    public int getNumberOfGaps() {
        return gapsSortedAfterFrequency.size();
    }

    public int getNumberOfFlashes() {
        return flashesSortedAfterFrequency.size();
    }

    protected int getSequenceBegin(int n) {
        return n == 0 ? 0 : indices[n - 1];
    }

    protected int getSequenceLength(int n) {
        return indices[n] - (n > 0 ? indices[n - 1] : 0);
    }

    public int[] toDurations(int nr) {
        return toDurations(getSequenceBegin(nr), getSequenceLength(nr));
    }

    public IrSequence cleanedIrSequence(int nr) {
        try {
            return new IrSequence(toDurations(nr));
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    public List<IrSequence> cleanedIrSequences() {
        List<IrSequence> list = new ArrayList<>(getNoSequences());
        for (int i = 0; i < getNoSequences(); i++)
            list.add(cleanedIrSequence(i));
        return list;
    }

    public String toTimingsString(int nr) {
        return toTimingsString(getSequenceBegin(nr), getSequenceLength(nr));
    }

    public int getCleanedTime(int i) {
        return timings.get(indexData[i]);
    }

    public int getNoSequences() {
        return signalMode ? 1 : indices.length;
    }

    private static class HistoPair {

        int numberGaps;
        int numberFlashes;

        HistoPair() {
            numberGaps = 0;
            numberFlashes = 0;
        }

        int get(boolean isFlash) {
            return isFlash ? numberFlashes : numberGaps;
        }

        void increment(boolean isFlash) {
            if (isFlash)
                numberFlashes++;
            else
                numberGaps++;
        }

        int total() {
            return numberFlashes + numberGaps;
        }

        @Override
        public String toString() {
            return total() + "=" + numberGaps + "+" + numberFlashes;
        }

        private void add(HistoPair op) {
            numberGaps += op.numberGaps;
            numberFlashes += op.numberFlashes;
        }
    }
}
