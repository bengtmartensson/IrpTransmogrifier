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

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.Protocol;

public class Analyzer extends Cleaner {

    private static final Logger logger = Logger.getLogger(Analyzer.class.getName());
    private static final int numberOfDecoders = 4;

    private int timebase;
    private int[] normedTimings;
    private List<Burst> pairs;
    private RepeatFinder.RepeatFinderData repeatfinderData;
    private double frequency;

    public Analyzer(IrSignal irSignal, boolean invokeRepeatFinder, double absoluteTolerance, double relativeTolerance) {
        this(irSignal.toModulatedIrSequence(1), invokeRepeatFinder, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence, double frequency, boolean invokeRepeatFinder, double absoluteTolerance, double relativeTolerance) {
        super(irSequence, absoluteTolerance, relativeTolerance);
        this.frequency = frequency;
        repeatfinderData = getRepeatFinderData(invokeRepeatFinder, irSequence.getLength());
        createNormedTimings();
        createPairs();
    }

    public Analyzer(ModulatedIrSequence irSequence, boolean invokeRepeatFinder, double absoluteTolerance, double relativeTolerance) {
        this(irSequence, irSequence.getFrequency(), invokeRepeatFinder, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence, boolean invokeRepeatFinder) {
        this(irSequence, IrCoreUtils.invalid, invokeRepeatFinder, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Analyzer(IrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        this(irSequence, IrCoreUtils.invalid, false, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence) {
        this(irSequence, IrCoreUtils.invalid, false, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Analyzer(int[] data) throws OddSequenceLenghtException {
        this(new IrSequence(data), IrCoreUtils.invalid, false, (int) IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
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
        getFlashes().stream().forEach((flash) -> {
            getGaps().stream().filter((gap) -> (getNumberPairs(flash, gap) > 0)).forEach((gp) -> {
                pairs.add(new Burst(flash, gp));
            });
        });
        Collections.sort(pairs, (a, b) -> getNumberPairs(b) - getNumberPairs(a));
    }


    private List<Class<?>> selectDecoderClasses(String decoderPattern, boolean regexp) {
        return regexp ? selectDecoderClassesRegexp(decoderPattern) : selectDecoderClassesSubstring(decoderPattern);
    }

    private List<Class<?>> selectDecoderClassesRegexp(String decoderPattern) {
        Pattern pattern = decoderPattern != null ? Pattern.compile(decoderPattern, Pattern.CASE_INSENSITIVE) : null;
        List<Class<?>> decoders = new ArrayList<>(AbstractDecoder.NUMBERDECODERS);
        for (Class<?> decoderClass : AbstractDecoder.decoders)
            if (pattern == null || pattern.matcher(decoderClass.getSimpleName()).matches())
                decoders.add(decoderClass);
        return decoders;
    }

    private List<Class<?>> selectDecoderClassesSubstring(String decoderPattern) {
        List<Class<?>> decoders = new ArrayList<>(AbstractDecoder.NUMBERDECODERS);
        for (Class<?> decoderClass : AbstractDecoder.decoders)
            if (decoderPattern == null
                    || decoderClass.getSimpleName().regionMatches(true, 0, decoderPattern, 0, decoderPattern.length()))
                decoders.add(decoderClass);
        return decoders;
    }

    private List<AbstractDecoder> setupDecoders(Analyzer.AnalyzerParams params, String decoderPattern, boolean regexp) {
        List<Class<?>> decoderClasses = selectDecoderClasses(decoderPattern, regexp);
        List<AbstractDecoder> decoders = new ArrayList<>(AbstractDecoder.NUMBERDECODERS);
        decoderClasses.forEach((decoderClass) -> {
            try {
                Constructor<?> constructor = decoderClass.getConstructor(Analyzer.class, AnalyzerParams.class);
                AbstractDecoder decoder = (AbstractDecoder) constructor.newInstance(this, params);
                decoders.add(decoder);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InstantiationException ex) {
                // consider this as programming error
                throw new ThisCannotHappenException(ex);
            } catch (InvocationTargetException ex) {
                // Likely not a fatal problem, the decoder just did not accept the data.
                logger.log(Level.FINE, String.format("Decoder %1$s failed: %2$s", decoderClass.getSimpleName(), ex.getTargetException().getMessage()));
            }
        });
        return decoders;
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

    public double getFrequency() {
        return frequency;
    }

    public Protocol searchProtocol(AnalyzerParams params, String decoderPattern, boolean regexp) {
        List<AbstractDecoder> decoders = setupDecoders(params, decoderPattern, regexp);
        Protocol bestSoFar = null;
        int weight = Integer.MAX_VALUE;

        for (AbstractDecoder decoder : decoders) {
            try {
                Protocol protocol = decoder.parse();
                int protocolWeight = protocol.weight();
                logger.log(Level.FINE, "{0}: {1} w = {2}", new Object[]{decoder.name(), protocol.toIrpString(), protocolWeight});
                if (protocolWeight < weight) {
                    bestSoFar = protocol;
                    weight = protocolWeight;
                }
            } catch (DecodeException ex) {
                logger.log(Level.FINE, "{0}: {1}", new Object[]{decoder.name(), ex.getMessage()});
            }
        }
        return bestSoFar;
    }

    public int getCleanedTime(int i) {
        return timings.get(indexData[i]);
    }

    public RepeatFinder.RepeatFinderData getRepeatFinderData() {
        return repeatfinderData;
    }

    public void printStatistics(PrintStream out) {
        out.println("Gaps:");
        this.getGaps().stream().forEach((d) -> {
            out.println(this.getName(d) + ":\t" + d + "\t" + this.getNumberGaps(d));
        });

        out.println("Flashes:");
        this.getFlashes().stream().forEach((d) -> {
            out.println(this.getName(d) + ":\t" + d + "\t" + this.getNumberFlashes(d));
        });

        out.println("Pairs:");
        this.getPairs().stream().forEach((pair) -> {
            out.println(this.getName(pair) + ":\t" + this.getNumberPairs(pair));
        });

        out.println("Signal as sequence of pairs:");
        out.println(this.toTimingsString());
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
