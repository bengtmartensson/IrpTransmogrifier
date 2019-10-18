/*
Copyright (C) 2012, 2014, 2016, 2017, 2019 Bengt Martensson.

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

package org.harctoolbox.ircore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A ModulatedIrSequence is an IrSequence with the additional properties of a modulation frequency and a duty cycle.
 * The name is slightly misleading since the modulation frequency can be 0; it just needs to be present.
 */
public final class ModulatedIrSequence extends IrSequence {
    private static final double ALLOWED_FREQUENCY_DEVIATION = 0.05;
    private static final double ZEROMODULATION_LIMIT = 0.000001;
    public static final double DEFAULT_FREQUENCY = 38000.0;
    public static final double DEFAULT_DUTYCYCLE = 0.4;
    public static final double DEFAULT_DEMODULATE_THRESHOLD = 35.0;

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final ModulatedIrSequence EMPTY = new ModulatedIrSequence();

    public static ModulatedIrSequence concatenate(Collection<IrSequence> sequences, double frequency, double dutyCycle) {
        return new ModulatedIrSequence(IrSequence.concatenate(sequences), frequency, dutyCycle);
    }

    /**
     * Concatenates the IrSequences in the argument to a new sequence.
     * Frequency and duty cycle are set to the average between minimum and maximum values by the components, if it makes sense.
     * @param seqs One or more ModulatedIrSequences
     * @return
     */
    public static ModulatedIrSequence concatenate(ModulatedIrSequence[] seqs) {
        double minf = Double.MAX_VALUE;
        double maxf = Double.MIN_VALUE;
        double mindc = Double.MAX_VALUE;
        double maxdc = Double.MIN_VALUE;
        for (ModulatedIrSequence seq : seqs) {
            minf = Math.min(minf, seq.frequency);
            maxf = Math.max(maxf, seq.frequency);
            mindc = Math.min(mindc, seq.frequency);
            maxdc = Math.max(maxdc, seq.frequency);
        }

        double dutyCycle = mindc > 0 ? (mindc + maxdc)/2 : IrCoreUtils.INVALID;
        double frequency = minf > 0 ? (minf + maxf)/2 : 0;
        return new ModulatedIrSequence(IrSequence.concatenate(seqs), frequency, dutyCycle);
    }

    public static double getFrequencyWithDefault(Double frequency) {
        return frequency != null ? frequency : DEFAULT_FREQUENCY;
    }

    /**
     * Checks the argument for null or validity of duty cycle (0, 1).
     * @param dutyCycle number to be checked. null is allowed.
     * @return
     */
    public static boolean isValidDutyCycle(Double dutyCycle) {
        return dutyCycle == null || (dutyCycle > 0 && dutyCycle < 1);
    }

    /**
     * From a (non-modulated) IrSequence, consisting of on- and off-periods,
     * remove the modulation and determine a modulation frequency and duty cycle,
     * gathered from the statistics of the input IrSequence.
     * @param irSequence input signal
     * @param threshold Gaps less or equal to this quantity are squashed into a modulated flash.
     * @return ModulatedIrSequence
     */
    public static ModulatedIrSequence demodulate(IrSequence irSequence, double threshold) {
        List<Double> list = new ArrayList<>(128);
        double pending = 0.0;
        double sumPulses = 0.0;
        double sumOn = 0.0;
        int numberPulses = 0;
        int begFlash = 0;
        for (int i = 0; i < irSequence.getLength(); i += 2) {
            pending += irSequence.get(i);
            if (irSequence.get(i+1) > threshold) {
                list.add(pending);
                list.add(irSequence.get(i+1));
                pending = 0;
                begFlash = i;
            } else {
                pending += irSequence.get(i+1);
                if (i > begFlash) {
                    sumPulses += irSequence.get(i) + irSequence.get(i + 1);
                    sumOn += irSequence.get(i);
                    numberPulses++;
                }
            }
        }

        double freq = numberPulses/IrCoreUtils.microseconds2seconds(sumPulses);
        double dutyCycle = sumOn/sumPulses;

        try {
            IrSequence seq = new IrSequence(list);
            return new ModulatedIrSequence(seq, freq, dutyCycle);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    /**
     * Equivalent to the two parameter version with a default threshold.
     * @param irSequence input signal, modulated
     * @return ModulatedIrSignal
     */
    public static ModulatedIrSequence demodulate(IrSequence irSequence) {
        return demodulate(irSequence, DEFAULT_DEMODULATE_THRESHOLD);
    }

    public static Double frequencyAverage(Iterable<ModulatedIrSequence> seqs) {
        double sum = 0;
        int index = 0;
        for (ModulatedIrSequence seq : seqs) {
            Double freq = seq.getFrequency();
            if (freq == null)
                return null;
            sum += freq;
            index++;
        }
        return index > 0 ? sum / index : null;
    }

    /**
     * Modulation frequency in Hz. Use 0 for no modulation. Use
     * null for no information.
     */
    private Double frequency;

    /**
     * Duty cycle of the modulation, a number between 0 and 1. Use null for unassigned/unknown.
     */
    private Double dutyCycle;

    public ModulatedIrSequence() {
        super();
        frequency = null;
        dutyCycle = null;
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param irSequence irSequence to be copied from
     * @param frequency
     * @param dutyCycle
     */
    public ModulatedIrSequence(IrSequence irSequence, Double frequency, Double dutyCycle) {
        super(irSequence);
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param irSequence irSequence to be copied from
     * @param frequency
     */
    public ModulatedIrSequence(IrSequence irSequence, Double frequency) {
        this(irSequence, frequency, null);
    }

    /**
     *
     * @param src
     * @param start
     * @param length
     * @throws InvalidArgumentException
     */
    public ModulatedIrSequence(ModulatedIrSequence src, int start, int length) throws InvalidArgumentException {
        super(src, start, length);
        frequency = src.frequency;
        dutyCycle = src.dutyCycle;
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param durations
     * @param frequency
     * @param dutyCycle
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public ModulatedIrSequence(double[] durations, Double frequency, Double dutyCycle) throws OddSequenceLengthException {
        this(new IrSequence(durations), frequency, dutyCycle);
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param durations
     * @param frequency
     * @throws OddSequenceLengthException if duration has odd length.
     */
    public ModulatedIrSequence(double[] durations, Double frequency) throws OddSequenceLengthException {
        this(new IrSequence(durations), frequency, null);
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param durations
     * @param frequency
     * @param dutyCycle
     * @throws OddSequenceLengthException if duration has odd length.
     */
    public ModulatedIrSequence(int[] durations, Double frequency, Double dutyCycle) throws OddSequenceLengthException {
        this(new IrSequence(durations), frequency, dutyCycle);
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param durations
     * @param frequency
     * @throws OddSequenceLengthException if duration has odd length.
     */
    public ModulatedIrSequence(int[] durations, Double frequency) throws OddSequenceLengthException {
        this(durations, frequency, null);
    }

    /**
     *
     * @return modulation frequency in Hz.
     */
    public Double getFrequency() {
        return frequency;
    }

    public double getFrequencyWithDefault() {
        return getFrequencyWithDefault(frequency);
    }

    /**
     *
     * @return Duty cycle.
     */
    public Double getDutyCycle() {
        return dutyCycle;
    }

    @SuppressWarnings("null")
    public double getDutyCycleWithDefault() {
        return dutyCycle != null ? dutyCycle : DEFAULT_DUTYCYCLE;
    }

    public String toStringFrequency(boolean alternatingSigns) {
        return "{"
                + (frequency != null ? (Integer.toString((int) Math.round(frequency)) + ",") : "")
                + (dutyCycle != null ? (Integer.toString((int) Math.round(100 * dutyCycle)) + "%,") : "")
                + super.toString(alternatingSigns)
                + "}";
    }

    public String toStringFrequency() {
        return toStringFrequency(false);
    }

    public IrSignal toIrSignal() {
        return new IrSignal(this);
    }

    public ModulatedIrSequence setFrequency(Double newFrequency) {
        return new ModulatedIrSequence(this, newFrequency != null ? newFrequency : this.frequency);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @param frequencyTolerance tolerance (absolute) for frequency in Hz.
     * @param dutyCycleTolerance tolerance (absolute) for duty cycle (&lt; 1).
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(ModulatedIrSequence irSequence, double absoluteTolerance,
            double relativeTolerance, double frequencyTolerance, double dutyCycleTolerance) {
        return IrCoreUtils.approximatelyEquals(this.getFrequency(), irSequence.getFrequency(), frequencyTolerance, 0.0)
                && IrCoreUtils.approximatelyEquals(this.getDutyCycle(), irSequence.getDutyCycle(), dutyCycleTolerance, 0.0)
                && super.approximatelyEquals(irSequence, absoluteTolerance, relativeTolerance);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @param frequencyTolerance
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(ModulatedIrSequence irSequence, double absoluteTolerance,
            double relativeTolerance, double frequencyTolerance) {
        return approximatelyEquals(irSequence, absoluteTolerance, relativeTolerance, frequencyTolerance, IrCoreUtils.DEFAULT_DUTYCYCLE_TOLERANCE);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(ModulatedIrSequence irSequence, double absoluteTolerance,
            double relativeTolerance) {
        return approximatelyEquals(irSequence, absoluteTolerance, relativeTolerance, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE, IrCoreUtils.DEFAULT_DUTYCYCLE_TOLERANCE);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(ModulatedIrSequence irSequence) {
        return approximatelyEquals(irSequence, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE,
                IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE, IrCoreUtils.DEFAULT_DUTYCYCLE_TOLERANCE);
    }

    /**
     *
     * @return true if and only iff the modulation frequency is zero (in numerical sense).
     */
    public boolean isZeroModulated() {
        return frequency < ZEROMODULATION_LIMIT;
    }

    @Override
    public ModulatedIrSequence addNoise(double max) {
        return new ModulatedIrSequence(super.addNoise(max), frequency, dutyCycle);
    }

    /**
     * Appends a delay to the end of the ModulatedIrSequence. Original is left untouched.
     * @param delay microseconds of silence to be appended to the IrSequence.
     * @return Copy of object with additional delay at end.
     * @throws InvalidArgumentException
     */
    @Override
    public ModulatedIrSequence append(double delay) throws InvalidArgumentException {
        IrSequence irSequence = super.append(delay);
        return new ModulatedIrSequence(irSequence, frequency, dutyCycle);
    }

    public ModulatedIrSequence append(ModulatedIrSequence tail) throws InvalidArgumentException {
        if (isZeroModulated() ? (! tail.isZeroModulated())
            : (Math.abs(frequency - tail.getFrequency())/frequency > ALLOWED_FREQUENCY_DEVIATION))
            throw new InvalidArgumentException("concationation not possible; modulation frequencies differ");
        IrSequence irSequence = super.append(tail);
        return new ModulatedIrSequence(irSequence, frequency, dutyCycle);
    }

    @Override
    public List<IrSequence> chop(double amount) {
        List<IrSequence> irSequences = super.chop(amount);
        List<IrSequence> mods = new ArrayList<>(irSequences.size());
        irSequences.forEach((seq) -> {
            mods.add(new ModulatedIrSequence(seq, frequency, dutyCycle));
        });
        return mods;
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public ModulatedIrSequence clone() {
        ModulatedIrSequence result = (ModulatedIrSequence) super.clone();
        return result;
    }

    /**
     * Generates a IrSequence, consisting of on- and off-periods,
     * containing the modulation, as per the modulation frequency and duty cycle.
     * If the latter is not present in the calling object,
     * a default (currently 0.4 = 40%) is used.
     * @return (non-modulated) IrSequence
     */
    public IrSequence modulate() {
        List<Double> list = new ArrayList<>(1024);
        for (int i = 0; i < getLength(); i += 2) {
            List<Double> pulse = flash(get(i));
            if (pulse.size() % 2 == 1) {
                // ends with flash, slam the gap onto the end
                pulse.add(get(i+1));
            } else {
                // ends with gap, extend that
                double lastGap = pulse.get(pulse.size()-1);
                lastGap += get(i+1);
                pulse.set(pulse.size()-1, lastGap);
            }
            list.addAll(pulse);
        }
        try {
            return new IrSequence(list);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

    private List<Double> flash(double duration) {
        double dutyCyc = getDutyCycleWithDefault();
        double periodTime = 1000000.0 / frequency;
        double onPeriod = periodTime * dutyCyc;
        double offPeriod = periodTime * (1.0 - dutyCyc);
        List<Double> list = new ArrayList<>(128);
        double left = duration;
        while (left > periodTime) {
            list.add(onPeriod);
            list.add(offPeriod);
            left -= periodTime;
        }
        if (left < onPeriod)
            list.add(left);
        else {
            list.add(onPeriod);
            left -= onPeriod;
            list.add(left);
        }
        return list;
    }
}
