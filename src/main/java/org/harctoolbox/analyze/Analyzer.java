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
import java.util.List;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.Protocol;

/**
 *
 */
public class Analyzer extends Cleaner {
    private final static boolean biPhaseInvert = false; // RC5: invertBiPhase = true

    public static String mkName(int n) {
        return new String(new char[]{ (char) ('A' + n) });
    }

//    private static String toIrpString(Map<String, Long> map, int radix) {
//        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
//        map.entrySet().stream().forEach((kvp) -> {
//            stringJoiner.add(kvp.getKey() + "=" + IrpUtils.radixPrefix(radix) + Long.toString(kvp.getValue(), radix));
//        });
//        return stringJoiner.toString();
//    }

    private int timebase;
    private int[] normedTimings;
    private double frequency;
    private List<Burst> pairs;
    private RepeatFinder.RepeatFinderData repeatfinderData;

    public Analyzer(IrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        super(irSequence, absoluteTolerance, relativeTolerance);
        createNormedTimings();
        createPairs();
    }

    public Analyzer(IrSequence irSequence, double frequency, boolean invokeRepeatFinder) throws OddSequenceLenghtException {
        super(irSequence);
        repeatfinderData = invokeRepeatFinder ? new RepeatFinder(toIrSequence()).getRepeatFinderData()
                : new RepeatFinder.RepeatFinderData(irSequence.getLength());
        this.frequency = frequency;
        createNormedTimings();
        createPairs();
    }

    public Analyzer(IrSequence irSequence, boolean invokeRepeatFinder) throws OddSequenceLenghtException {
        this(irSequence, ModulatedIrSequence.defaultFrequency, invokeRepeatFinder);
    }

    public Analyzer(IrSequence irSequence) throws OddSequenceLenghtException {
        this(irSequence, ModulatedIrSequence.defaultFrequency, true);
    }


    public String getName(int duration) {
        return mkName(getIndex(duration));
    }

    private void createNormedTimings() {
        //List<Integer> timings = cleaner.getTimings();
        timebase = getTimings().get(0);
        normedTimings = new int[timings.size()];
        for (int i = 0; i < timings.size(); i++) {
            normedTimings[i] = Math.round(timings.get(i) / (float) getTimebase());
        }
    }

    public GeneralSpec getGeneralSpec(BitDirection bitDirection) {
        return new GeneralSpec(bitDirection, getTimebase(), getFrequency());
    }

    public GeneralSpec getGeneralSpec() {
        return getGeneralSpec(BitDirection.msb);
    }

    private void createPairs() {
        pairs = new ArrayList<>(16);
        for (int mark : getDistinctMarks())
            for (int space : getDistinctSpaces())
                if (getNumberPairs(mark, space) > 0)
                    getPairs().add(new Burst(mark, space));
    }

    /**
     * @return the timebase
     */
    public int getTimebase() {
        return timebase;
    }

    /**
     * @return the normedTimings
     */
    public int[] getNormedTimings() {
        return normedTimings;
    }

    /**
     * @return the frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @return the pairs
     */
    public List<Burst> getPairs() {
        return pairs;
    }

    public String getName(Burst pair) {
        return getName(pair.getFlashDuration()) + getName(pair.getGapDuration());
    }

    public int getNumberPairs(Burst pair) {
        return getNumberPairs(pair.getFlashDuration(), pair.getGapDuration());
    }

//    private int normalize(int x) {
//        return (int) Math.round(((double) x)/timebase);
//    }

    public Protocol processPWM(BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths) throws DecodeException {
        PwmDecoder pwmDecoder = new PwmDecoder(this, timebase, timings.get(0), timings.get(1));
        return pwmDecoder.process(bitDirection, useExtents, parameterWidths);
    }

    public Protocol processBiPhase(BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths, boolean invert) throws DecodeException {
        BiphaseDecoder biphaseDecoder = new BiphaseDecoder(this, timebase, timings.get(0), timings.get(1), invert);
        return biphaseDecoder.process(bitDirection, useExtents, parameterWidths);
    }

    public int getCleanedTime(int i) {
        return timings.get(indexData[i]);
    }

    public RepeatFinder.RepeatFinderData getRepeatFinderData() {
        return repeatfinderData;
    }
}
