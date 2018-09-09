/*
Copyright (C) 2012, 2014, 2016, 2017 Bengt Martensson.

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

    @Override
    public String toString(boolean alternatingSigns) {
        return "{"
                + (frequency != null ? (Integer.toString((int)Math.round(frequency)) + ",") : "")
                + super.toString(alternatingSigns)
                + "}";
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @param frequencyTolerance tolerance (absolute) for frequency in Hz.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(ModulatedIrSequence irSequence, double absoluteTolerance,
            double relativeTolerance, double frequencyTolerance) {
        return IrCoreUtils.approximatelyEquals(this.getFrequency(), irSequence.getFrequency(), IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE, 0d)
                && super.approximatelyEquals(irSequence, absoluteTolerance, relativeTolerance);
    }

    public boolean approximatelyEquals(ModulatedIrSequence instance) {
        return approximatelyEquals(instance, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE,
                IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE);
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
}
