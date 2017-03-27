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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.Protocol;

public class Analyzer extends Cleaner {

    private static final Logger logger = Logger.getLogger(Analyzer.class.getName());

    public static int[] mkIndices(List<IrSequence> irSequenceList) {
        int[] indices = new int[irSequenceList.size()];
        for (int i = 0; i < irSequenceList.size(); i++)
            indices[i] = irSequenceList.get(i).getLength() + (i > 0 ? indices[i - 1] : 0);
        return indices;
    }

//    public static int[] mkIndices(IrSignal irSignal) {
//        int[] indices = new int[3];
//        indices[0] = irSignal.getIntroLength();
//        indices[1] = indices[0] + irSignal.getRepeatLength();
//        indices[2] = indices[1] + irSignal.getEndingLength();
//        return indices;
//    }

    private int timebase;
    private int[] normedTimings;
    private List<Burst> pairs;
    private final RepeatFinder.RepeatFinderData[] repeatFinderData;
    private Double frequency;

    public Analyzer(IrSignal irSignal, Double absoluteTolerance, Double relativeTolerance) {
        this(irSignal.toIrSequences(), mkIndices(irSignal.toIrSequences()), true, irSignal.getFrequency(), false, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence, Double frequency, boolean invokeRepeatFinder, Double absoluteTolerance, Double relativeTolerance) {
        this(Arrays.asList(irSequence), frequency, invokeRepeatFinder, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(List<IrSequence> irSequenceList, Double frequency, boolean invokeRepeatFinder, Double absoluteTolerance, Double relativeTolerance) {
        this(irSequenceList, mkIndices(irSequenceList), false, frequency, invokeRepeatFinder, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(List<IrSequence> irSequenceList, int[] indices, boolean signalMode, Double frequency, boolean invokeRepeatFinder, Double absoluteTolerance, Double relativeTolerance) {
        super(IrSequence.toInts(irSequenceList), indices, signalMode, absoluteTolerance != null ? absoluteTolerance : IrCoreUtils.DEFAULTABSOLUTETOLERANCE,
                relativeTolerance != null ? relativeTolerance : IrCoreUtils.DEFAULTRELATIVETOLERANCE);
        if (frequency == null)
            logger.log(Level.FINE, String.format(Locale.US, "No frequency given, using default frequency = %d Hz", (int) ModulatedIrSequence.defaultFrequency));
        this.frequency = frequency;
//        indices = new int[irSequenceList.size()];
//        for (int i = 0; i < irSequenceList.size(); i++)
//            indices[i] = irSequenceList.get(i).getLength() + (i > 0 ? indices[i - 1] : 0);
        repeatFinderData = new RepeatFinder.RepeatFinderData[irSequenceList.size()];
        for (int i = 0; i < irSequenceList.size(); i++)
            repeatFinderData[i] = getRepeatFinderData(invokeRepeatFinder, i);
        createNormedTimings();
        createPairs();
    }

    public Analyzer(ModulatedIrSequence irSequence, boolean invokeRepeatFinder, Double absoluteTolerance, Double relativeTolerance) {
        this(irSequence, irSequence.getFrequency(), invokeRepeatFinder, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence, boolean invokeRepeatFinder) {
        this(irSequence, null, invokeRepeatFinder, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    public Analyzer(IrSequence irSequence, Double absoluteTolerance, Double relativeTolerance) {
        this(irSequence, null, false, absoluteTolerance, relativeTolerance);
    }

    public Analyzer(IrSequence irSequence) {
        this(irSequence, null, false, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    public Analyzer(int[] data) throws OddSequenceLengthException {
        this(new IrSequence(data), null, false, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE);
    }

    private RepeatFinder.RepeatFinderData getRepeatFinderData(boolean invokeRepeatFinder, int number) {
        int beg = getSequenceBegin(number);
        int length = getSequenceLength(number);
        try {
            return invokeRepeatFinder ? new RepeatFinder(toDurations(beg, length)).getRepeatFinderData()
                    : new RepeatFinder.RepeatFinderData(length);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    RepeatFinder.RepeatFinderData getRepeatFinderData(int number) {
        return repeatFinderData[number];
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

    public Double getFrequency() {
        return frequency;
    }

    public ArrayList<Protocol> searchProtocol(AnalyzerParams params, String decoderPattern, boolean regexp) {
        List<AbstractDecoder> decoders = setupDecoders(params, decoderPattern, regexp);
        ArrayList<Protocol> result = new ArrayList<>(getNoSequences());
        for (int i = 0; i < getNoSequences(); i++)
            result.add(searchProtocol(decoders, i));

        return result;
    }

    public Protocol searchProtocol(List<AbstractDecoder> decoders, int number) {
        Protocol bestSoFar = null;
        int weight = Integer.MAX_VALUE;

        for (AbstractDecoder decoder : decoders) {
            try {
                Protocol protocol = decoder.parse(number, isSignalMode());
                int protocolWeight = protocol.weight();
                logger.log(Level.FINE, "{0}: {1} w = {2}", new Object[]{decoder.name(), protocol.toIrpString(10), protocolWeight});
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

    public void printStatistics(PrintStream out, AnalyzerParams params) {
        out.println("Gaps:");
        this.getGaps().stream().forEach((d) -> {
            out.println(this.getName(d) + ":\t" + d + "\t" + multiplierString(d, params.getTimebase()) + "\t" + this.getNumberGaps(d));
        });
        out.println();

        out.println("Flashes:");
        this.getFlashes().stream().forEach((d) -> {
            out.println(this.getName(d) + ":\t" + d + "\t" + multiplierString(d, params.getTimebase()) + "\t" + this.getNumberFlashes(d));
        });
        out.println();

        out.println("Pairs:");
        this.getPairs().stream().forEach((pair) -> {
            out.println(this.getName(pair) + ":\t" + this.getNumberPairs(pair));
        });
    }

    private String multiplierString(int us, Double timebase) {
        double tick = timebase != null ? timebase : timings.get(0);
        Integer mult = Burst.multiplier(us, tick);
        return mult != null ? "= " + mult.toString() + "*" + Long.toString(Math.round(tick)) + "  " : "\t";
    }


    public static class AnalyzerParams {
        private final Double frequency;
        private final Double timebase;
        private final boolean preferPeriods;
        private final BitDirection bitDirection;
        private final boolean useExtents;
        private final boolean invert;
        private final List<Integer> parameterWidths;

        public AnalyzerParams(Double frequency, String timeBaseString, BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths, boolean invert) {
            this.frequency = frequency;
            this.bitDirection = bitDirection;
            this.useExtents = useExtents;
            this.invert = invert;
            this.parameterWidths = parameterWidths == null ? new ArrayList<>(0) : parameterWidths;

            if (timeBaseString == null) {
                timebase = null;
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
        public Double getTimebase() {
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
