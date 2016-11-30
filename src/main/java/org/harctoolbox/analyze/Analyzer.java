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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.Protocol;

/**
 *
 */
public class Analyzer extends Cleaner {

    private static final Logger logger = Logger.getLogger(Analyzer.class.getName());

    private int timebase;
    private int[] normedTimings;
    private List<Burst> pairs;
    private RepeatFinder.RepeatFinderData repeatfinderData;

    public Analyzer(IrSequence irSequence, boolean invokeRepeatFinder, int absoluteTolerance, double relativeTolerance) {
        super(irSequence, absoluteTolerance, relativeTolerance);
        repeatfinderData = getRepeatFinderData(invokeRepeatFinder, irSequence.getLength());
        createNormedTimings();
        createPairs();
    }

    public Analyzer(IrSequence irSequence, boolean invokeRepeatFinder) {
        this(irSequence, invokeRepeatFinder, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Analyzer(IrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        this(irSequence, false, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence) {
        this(irSequence, false, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Analyzer(int[] data) throws OddSequenceLenghtException {
        this(new IrSequence(data), false, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    private RepeatFinder.RepeatFinderData getRepeatFinderData(boolean invokeRepeatFinder, int length) {
        return invokeRepeatFinder ? new RepeatFinder(toIrSequence()).getRepeatFinderData()
                : new RepeatFinder.RepeatFinderData(length);
    }

    private void createNormedTimings() {
        //List<Integer> timings = cleaner.getTimings();
        timebase = getTiming(0);
        normedTimings = new int[timings.size()];
        for (int i = 0; i < timings.size(); i++) {
            normedTimings[i] = Math.round(timings.get(i) / (float) getTimebase());
        }
    }

    private void createPairs() {
        pairs = new ArrayList<>(16);
        getDistinctFlashes().stream().forEach((mark) -> {
            getDistinctGaps().stream().filter((space) -> (getNumberPairs(mark, space) > 0)).forEach((space) -> {
                pairs.add(new Burst(mark, space));
            });
        });
    }

    /**
     * @return the timebase
     */
    public int getTimebase() {
        return timebase;
    }

    /**
     * @return the pairs
     */
    public List<Burst> getPairs() {
        return Collections.unmodifiableList(pairs);
    }

    public String getName(Burst pair) {
        return getName(pair.getFlashDuration()) + getName(pair.getGapDuration());
    }

    public int getNumberPairs(Burst pair) {
        return getNumberPairs(pair.getFlashDuration(), pair.getGapDuration());
    }

    public Protocol searchProtocol(AnalyzerParams params) {
        List<AbstractDecoder> decoders = new ArrayList<>(4);
        decoders.add(new TrivialDecoder(this, params));
        decoders.add(new PwmDecoder(this, params));
        try {
            decoders.add(new Pwm4Decoder(this, params));
        } catch (DecodeException ex) {
            logger.log(Level.FINE, ex.getMessage());
        }
        decoders.add(new BiphaseDecoder(this, params));

        Protocol best = null;
        int weight = Integer.MAX_VALUE;

        for (AbstractDecoder decoder : decoders) {
            try {
                Protocol protocol = decoder.process();
                logger.log(Level.FINE, "{0}: {1} w = {2}", new Object[]{decoder.name(), protocol.toIrpString(), protocol.weight()});
                if (protocol.weight() < weight) {
                    best = protocol;
                    weight = protocol.weight();
                }
            } catch (Exception ex) {
                logger.log(Level.FINE, "{0}: {1}", new Object[]{decoder.name(), ex.getMessage()});
            }
        }
        return best;
    }

    public int getCleanedTime(int i) {
        return timings.get(indexData[i]);
    }

    public RepeatFinder.RepeatFinderData getRepeatFinderData() {
        return repeatfinderData;
    }

    public static class AnalyzerParams {
        private final double frequency;
        private final double timebase;
        private final boolean preferPeriods;
        private final BitDirection bitDirection;
        private final boolean useExtents;
        private final boolean invert;
        private final List<Integer> parameterWidths;

        public AnalyzerParams(double frequency, String timeBaseString, BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths, boolean invert) {
            this.frequency = frequency;
            this.bitDirection = bitDirection;
            this.useExtents = useExtents;
            this.invert = invert;
            this.parameterWidths = parameterWidths == null ? new ArrayList<>(0) : parameterWidths;

            if (timeBaseString == null) {
                timebase = IrCoreUtils.invalid;
                preferPeriods = false;
            } else {
                preferPeriods = timeBaseString.endsWith("p");
                String str = (timeBaseString.endsWith("p") || timeBaseString.endsWith("u"))
                        ? timeBaseString.substring(0, timeBaseString.length() - 1)
                        : timeBaseString;
                double timeBaseNumber = Double.parseDouble(str);
                timebase = preferPeriods ? IrCoreUtils.seconds2microseconds(timeBaseNumber / frequency) : timeBaseNumber;
            }
        }

        public int getNoBitsLimit(int noPayload) {
            return (parameterWidths == null || noPayload >= parameterWidths.size()) ? Integer.MAX_VALUE : parameterWidths.get(noPayload);
        }

        /**
         * @return the bitDirection
         */
        public BitDirection getBitDirection() {
            return bitDirection;
        }

        /**
         * @return the useExtents
         */
        public boolean isUseExtents() {
            return useExtents;
        }

        /**
         * @param i
         * @return the parameterWidths
         */
        public Integer getParameterWidth(int i) {
            return parameterWidths.get(i);
        }

        /**
         * @return the frequency
         */
        public double getFrequency() {
            return frequency;
        }

        /**
         * @return the timebase
         */
        public double getTimebase() {
            return timebase;
        }

        /**
         * @return the preferPeriods
         */
        public boolean isPreferPeriods() {
            return preferPeriods;
        }

        public GeneralSpec getGeneralSpec(double otherTimebase) {
            return new GeneralSpec(bitDirection, otherTimebase, frequency);
        }

        /**
         * @return the invert
         */
        public boolean isInvert() {
            return invert;
        }
    }
}
