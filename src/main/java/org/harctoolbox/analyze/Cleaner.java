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
import java.util.Map;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;

/**
 *
 */
public class Cleaner {

    private int rawData[];
    private List<Integer> dumbTimingsTable;
    private List<Integer> timings;
    private HashMap<Integer, Integer> rawHistogram;
    private HashMap<Integer, Integer> cleanedHistogram;
    private int indexData[];
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
        rawHistogram = new HashMap<>();
        for (int d : rawData) {
            int old = rawHistogram.containsKey(d) ? rawHistogram.get(d) : 0;
            rawHistogram.put(d, old + 1);
        }
    }

    public int[] getIndexData() {
        return indexData;
    }

//    HashMap<Integer, Integer> getRawHistogram() {
//        return rawHistogram;
//    }

    private void createDumbTimingsTable(int absoluteTolerance, double relativeTolerance) {
        dumbTimingsTable = new ArrayList<>();
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
        lookDownTable = new HashMap<>();
        timings = new ArrayList<>();
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
                int noHits = rawHistogram.get(duration);
                sum += noHits * duration;
                terms += noHits;
                lookDownTable.put(duration, timingsIndex);
            }
            int average = (int) Math.round((double)sum/(double)terms);
            timings.add(average);
        }
    }

    private void createCookedData() {
        indexData = new int[rawData.length];
        for (int i = 0; i < rawData.length; i++)
            indexData[i] = lookDownTable.get(rawData[i]);
    }

    private void createCleanHistogram() {
        cleanedHistogram = new LinkedHashMap<>();
        for (int duration : timings)
            cleanedHistogram.put(duration, 0);
        for (Map.Entry<Integer, Integer> kvp : rawHistogram.entrySet()) {
            int index = lookDownTable.get(kvp.getKey());
            Integer cleanedDuration = timings.get(index);
            cleanedHistogram.put(cleanedDuration, cleanedHistogram.get(cleanedDuration) + kvp.getValue());
        }
    }


    private int[] toDurations() {
        int[] data = new int[rawData.length];
        for (int i = 0; i < rawData.length; i++)
            data[i] = timings.get(indexData[i]);
        return data;
    }

    public String toTimingsString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < rawData.length; i++)
            str.append((char) ((int)'A' + indexData[i]));
        return str.toString();
    }

    private IrSequence toIrSequence() {
        try {
            return new IrSequence(toDurations());
        } catch (OddSequenceLenghtException ex) {
            assert(false);
            return null;
        }
    }

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

    /**
     * @return the timings
     */
    public List<Integer> getTimings() {
        return timings;
    }

    /**
     * @return the cleanedHistogram
     */
    public HashMap<Integer, Integer> getCleanedHistogram() {
        return cleanedHistogram;
    }
}
