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

package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 *
 */
public class Cleaner {
    private final static int numberOfTimingsCapacity = 20;
    public static IrSequence clean(IrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        Cleaner cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        return cleaner.toIrSequence();
    }
    public static IrSequence clean(IrSequence irSequence) {
        return clean(irSequence, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }
    public static ModulatedIrSequence clean(ModulatedIrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        return new ModulatedIrSequence(clean((IrSequence)irSequence, absoluteTolerance, relativeTolerance),
                irSequence.getFrequency(), irSequence.getDutyCycle());
    }
    public static ModulatedIrSequence clean(ModulatedIrSequence irSequence) {
        return clean(irSequence, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    private int rawData[];
    private List<Integer> dumbTimingsTable;
    protected List<Integer> timings;
    private HashMap<Integer, HistoPair> rawHistogram;
    private HashMap<Integer, HistoPair> cleanedHistogram;
    protected int indexData[];
    private int[] sorted;
    private HashMap<Integer, Integer> lookDownTable;

    public Cleaner(IrSequence irSequence) {
        this(irSequence, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Cleaner(IrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        rawData = irSequence.toInts();
        createRawHistogram();
        createDumbTimingsTable(absoluteTolerance, relativeTolerance);
        improveTimingsTable(absoluteTolerance, relativeTolerance);
        createCookedData();
        createCleanHistogram();
        //createNormedTimings();
        //System.out.println(toTimingsString());
    }

    private void createRawHistogram() {
        rawHistogram = new HashMap<>(numberOfTimingsCapacity);
        for (int i = 0; i < rawData.length; i++) {
            boolean isMark = i % 2 == 0;
            int duration = rawData[i];
            if (!rawHistogram.containsKey(duration))
                rawHistogram.put(duration, new HistoPair());
            rawHistogram.get(duration).increment(isMark);
        }
//        for (int d : rawData) {
//            int old = rawHistogram.containsKey(d) ? rawHistogram.get(d) : 0;
//            rawHistogram.put(d, old + 1);
//        }
    }

    public int[] getIndexData() {
        return indexData;
    }

//    HashMap<Integer, Integer> getRawHistogram() {
//        return rawHistogram;
//    }

    private void createDumbTimingsTable(int absoluteTolerance, double relativeTolerance) {
        dumbTimingsTable = new ArrayList<>(rawData.length);
        sorted = rawData.clone();
        Arrays.sort(sorted);
        int last = Integer.MIN_VALUE;
        for (int d : sorted) {
            if (!IrCoreUtils.approximatelyEquals(d, last, absoluteTolerance, relativeTolerance)) {
                dumbTimingsTable.add(d);
                last = d;
            }
        }
    }

    private void improveTimingsTable(int absoluteTolerance, double relativeTolerance) {
        lookDownTable = new HashMap<>(numberOfTimingsCapacity);
        timings = new ArrayList<>(numberOfTimingsCapacity);
        int indexInSortedTimings = 0;
        for (int timingsIndex = 0; timingsIndex < dumbTimingsTable.size(); timingsIndex++) {
            int dumbTiming = dumbTimingsTable.get(timingsIndex);
            int sum = 0;
            int terms = 0;
            int lastDuration = -1;
            while (indexInSortedTimings < sorted.length
                    && IrCoreUtils.approximatelyEquals(dumbTiming, sorted[indexInSortedTimings], absoluteTolerance, relativeTolerance)) {
                int duration = sorted[indexInSortedTimings++];
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
        cleanedHistogram = new LinkedHashMap<>(numberOfTimingsCapacity);
        timings.stream().forEach((duration) -> {
            cleanedHistogram.put(duration, new HistoPair());
        });
        rawHistogram.entrySet().stream().forEach((kvp) -> {
            int index = lookDownTable.get(kvp.getKey());
            Integer cleanedDuration = timings.get(index);
            cleanedHistogram.get(cleanedDuration).add(kvp.getValue());
        });
    }


    private int[] toDurations() {
        int[] data = new int[rawData.length];
        for (int i = 0; i < rawData.length; i++)
            data[i] = timings.get(indexData[i]);
        return data;
    }

    public String toTimingsString() {
        StringBuilder str = new StringBuilder(16);
        for (int i = 0; i < rawData.length; i++)
            str.append((char) ('A' + indexData[i]));
        return str.toString();
    }

    public IrSequence toIrSequence() {
        try {
            return new IrSequence(toDurations());
        } catch (OddSequenceLenghtException ex) {
            assert(false);
            return null;
        }
    }

    protected int getTotalDuration(int beg, int length) {
        int sum = 0;
        for (int i = beg; i < beg + length; i++)
            sum += timings.get(indexData[i]);
        return sum;
    }

    /**
     * @return the timings
     */
    public List<Integer> getTimings() {
        return timings;
    }

    public Integer getIndex(int duration) {
        return lookDownTable.get(duration);
    }

    private List<Integer> getMarksOrSpaces(boolean isSpace) {
        List<Integer> list = new ArrayList<>(timings.size());
        for (int d : timings) {
            if (cleanedHistogram.get(d).get(isSpace) > 0)
                list.add(d);
        }
        return list;
    }

    public List<Integer> getDistinctSpaces() {
        return getMarksOrSpaces(false);
    }

    public List<Integer> getDistinctMarks() {
        return getMarksOrSpaces(true);
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

    public int getNumberSpaces(int duration) {
        return cleanedHistogram.get(duration).numberSpaces;
    }

    public int getNumberMarks(int duration) {
        return cleanedHistogram.get(duration).numberMarks;
    }

    public int getNumberPairs(int mark, int space) {
        Integer ispace = getIndex(space);
        Integer imark = getIndex(mark);
        if (ispace == null || imark == null)
            throw new ThisCannotHappenException();

        int result = 0;
        for (int i = 0; i < indexData.length - 1; i += 2)
            if (indexData[i] == imark && indexData[i + 1] == ispace)
                result++;

        return result;
    }

    private static class HistoPair {

        int numberSpaces;
        int numberMarks;

        HistoPair() {
            numberSpaces = 0;
            numberMarks = 0;
        }

        int get(boolean isMark) {
            return isMark ? numberMarks : numberSpaces;
        }

        void increment(boolean isMark) {
            if (isMark)
                numberMarks++;
            else
                numberSpaces++;
        }

        int total() {
            return numberMarks + numberSpaces;
        }

        @Override
        public String toString() {
            return total() + "=" + numberSpaces + "+" + numberMarks;
        }

        private void add(HistoPair op) {
            numberSpaces += op.numberSpaces;
            numberMarks += op.numberMarks;
        }
    }
}
