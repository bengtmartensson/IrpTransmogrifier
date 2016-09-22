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
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.BitspecIrstream;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.InvalidRepeatException;
import org.harctoolbox.irp.IrStream;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.IrpSyntaxException;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.RepeatMarker;

/**
 *
 */
public class Analyzer extends Cleaner {

    private static final double maxRoundingError = 0.3f;
    private static final double maxUnits = 20f;
    private static final double maxUs = 10000f;

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
    private List<MarkSpace> pairs;
    private MarkSpace zero;
    private MarkSpace one;
    private RepeatFinder.RepeatFinderData repeatfinderData;
    private NameEngine nameEngine;
    private int noPayload;

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
        zero = pairs.get(0);
        one = pairs.get(1);
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
                    getPairs().add(new MarkSpace(mark, space));
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
    public List<MarkSpace> getPairs() {
        return pairs;
    }

    public String getName(MarkSpace pair) {
        return getName(pair.getMarkDuration()) + getName(pair.getSpaceDuration());
    }

    public int getNumberPairs(MarkSpace pair) {
        return getNumberPairs(pair.markDuration, pair.spaceDuration);
    }

//    private int normalize(int x) {
//        return (int) Math.round(((double) x)/timebase);
//    }

    public Protocol process(BitDirection bitDirection, boolean useExtents) throws IrpSyntaxException, InvalidRepeatException {
        //HashMap<String, Long> payloads = new LinkedHashMap<>(4);
        List<BareIrStream> list = new ArrayList<>(2);
        list.add(zero.toBareIrStream());
        list.add(one.toBareIrStream());
        BitSpec bitSpec = new BitSpec(list);
        //System.out.println(bitSpec.toIrpString());
        nameEngine = new NameEngine();
        noPayload = 0;

        List<IrStreamItem> items = process(0, repeatfinderData.getBeginLength(), bitDirection, useExtents);
        List<IrStreamItem> repeatItems = process(repeatfinderData.getBeginLength(), repeatfinderData.getRepeatLength(), bitDirection, useExtents);
        if (!repeatItems.isEmpty()) {
            RepeatMarker repeatMarker = new RepeatMarker(repeatfinderData.getNumberRepeats());
            IrStream repeat = new IrStream(repeatItems, repeatMarker);
            items.add(repeat);
        }

        List<IrStreamItem> endingItems = process(repeatfinderData.getBeginLength() + repeatfinderData.getNumberRepeats() * repeatfinderData.getRepeatLength(),
                repeatfinderData.getEndingLength(), bitDirection, useExtents);
        if (!endingItems.isEmpty()) {
            IrStream ending = new IrStream(endingItems);
            items.add(ending);
        }

        IrStream irStream = new IrStream(items);
        BitspecIrstream bitspecIrstream = new BitspecIrstream(bitSpec, irStream);
        Protocol protocol = new Protocol(getGeneralSpec(), bitspecIrstream, nameEngine, null, null);
        return protocol;
    }

    private List<IrStreamItem> process(int beg, int length, BitDirection bitDirection, boolean useExtents) throws IrpSyntaxException {
        List<IrStreamItem> items = new ArrayList<>(16);
        long data = 0;
        int noBits = 0;
        for (int i = beg; i < beg + length - 1; i += 2) {

            int mark = timings.get(indexData[i]);
            int space = timings.get(indexData[i + 1]);
            MarkSpace burst = new MarkSpace(mark, space);
            if (burst.equals(zero)) {
                data *= 2;
                noBits++;
            } else if (burst.equals(one)) {
                data *= 2;
                data++;
                noBits++;
            } else {
                if (noBits > 0) {
                    if (bitDirection == BitDirection.lsb)
                        data = IrpUtils.reverse(data, noBits);
                    String name = mkName(noPayload++);
                    nameEngine.define(name, data);
                    //payloads.put(name, data);
                    items.add(new FiniteBitField(name, noBits));
                    //System.out.println(name + ":" + length);
                    data = 0L;
                    noBits = 0;
                }
                Flash flash = newFlash(mark);
                items.add(flash);
                if (i == beg + length - 2 && useExtents) {
                    int total = getTotalDuration(beg, length);
                    Extent extent = newExtent(total);
                    items.add(extent);
                } else {
                    Gap gap = newGap(space);
                    items.add(gap);
                }
                //System.out.println(flash.toIrpString() + "," + gap.toIrpString());
            }
        }
        return items;
    }

    private Duration newFlashOrGap(boolean isFlash, int us) {
        double units = (double)us/timebase;
        double roundingError = Math.round(units) - units;
        String unit = (units < maxUnits && Math.abs(roundingError) < maxRoundingError) ? ""
                : us < maxUs ? "u"
                : "m";
        double duration = unit.isEmpty() ? Math.round(units)
                : unit.equals("m") ? Math.round(us/1000f)
                : us;
        return isFlash ? new Flash(duration, unit) : new Gap(duration, unit);
    }

    private Extent newExtent(int us) {
        double units = (double)us/timebase;
        double roundingError = Math.round(units) - units;
        String unit = (units < maxUnits && Math.abs(roundingError) < maxRoundingError) ? ""
                : us < maxUs ? "u"
                : "m";
        double duration = unit.isEmpty() ? Math.round(units)
                : unit.equals("m") ? Math.round(us/1000f)
                : us;
        return new Extent(duration, unit);
    }

    private Flash newFlash(int duration) {
        return (Flash) newFlashOrGap(true, duration);
    }

    private Gap newGap(int duration) {
        return (Gap) newFlashOrGap(false, duration);
    }

    public class MarkSpace {
        private final int spaceDuration;
        private final int markDuration;

        MarkSpace(int mark, int space) {
            spaceDuration = space;
            markDuration = mark;
        }

        /**
         * @return the spaceDuration
         */
        public int getSpaceDuration() {
            return spaceDuration;
        }

        /**
         * @return the markDuration
         */
        public int getMarkDuration() {
            return markDuration;
        }

        public BareIrStream toBareIrStream() {
            List<IrStreamItem> items = new ArrayList<>(2);
            Flash flash = newFlash(markDuration);
            items.add(flash);
            Gap gap = newGap(spaceDuration);
            items.add(gap);
            return new BareIrStream(items);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MarkSpace))
                return false;

            return markDuration == ((MarkSpace) obj).markDuration
                    && spaceDuration == ((MarkSpace) obj).spaceDuration;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + this.spaceDuration;
            hash = 67 * hash + this.markDuration;
            return hash;
        }
    }
}
