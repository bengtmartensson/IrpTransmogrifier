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

import java.util.Collection;

/**
 * A ModulatedIrSequence is an IrSequence with the additional properties of a modulation frequency and a duty cycle.
 * The name is slightly misleading since the modulation frequency can be 0; it just needs to be present.
 */
public class ModulatedIrSequence extends IrSequence {
    private static final double allowedFrequencyDeviation = 0.05;
    private static final double zeroModulationLimit = 0.000001;
    public static final double defaultFrequency = 38000.0;
    public static ModulatedIrSequence concatenate(Collection<IrSequence> sequences, double frequency, double dutyCycle) {
        return new ModulatedIrSequence(IrSequence.concatenate(sequences), frequency, dutyCycle);
    }

    /**
     * Modulation frequency in Hz. Use 0 for no modulation. Use
     * <code>unknownFrequency</code> for no information.
     */
    protected Double frequency = null;

    /**
     * Duty cycle of the modulation, a number between 0 and 1. Use <code>unknownDutyCycle</code> for unassigned.
     */
    protected Double dutyCycle = null;

    private ModulatedIrSequence() {
    }

    /**
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param irSequence irSequence to be copied from
     * @param frequency
     * @param dutyCycle
     */
    public ModulatedIrSequence(IrSequence irSequence, Double frequency, Double dutyCycle) {
        super(irSequence.data);
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
     * Constructs a ModulatedIrSequence from its arguments.
     *
     * @param durations
     * @param frequency
     * @param dutyCycle
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
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
        this(new IrSequence(durations), frequency, null);
    }

    /**
     * Concatenates the IrSequences in the argument to a new sequence.
     * Frequency and duty cycle are set to the average between minimum and maximum values by the components, if it makes sense.
     * @param seqs One or more ModulatedIrSequences
     */
    public ModulatedIrSequence(ModulatedIrSequence... seqs) {
        int cumulatedLength = 0;
        double minf = Double.MAX_VALUE;
        double maxf = Double.MIN_VALUE;
        double mindc = Double.MAX_VALUE;
        double maxdc = Double.MIN_VALUE;
        for (ModulatedIrSequence seq : seqs) {
            minf = Math.min(minf, seq.frequency);
            maxf = Math.max(maxf, seq.frequency);
            mindc = Math.min(mindc, seq.frequency);
            maxdc = Math.max(maxdc, seq.frequency);
            cumulatedLength += seq.getLength();
        }

        dutyCycle = mindc > 0 ? (mindc + maxdc)/2 : null;
        frequency = minf > 0 ? (minf + maxf)/2 : 0.0;
        data = new double[cumulatedLength];
        int beginIndex = 0;
        for (ModulatedIrSequence seq : seqs) {
            System.arraycopy(seq.data, 0, data, beginIndex, seq.data.length);
            beginIndex += seq.data.length;
        }
    }

    /**
     *
     * @return modulation frequency in Hz.
     */
    public final Double getFrequency() {
        return frequency;
    }

    public final double getFrequencyWithDefault() {
        return frequency != null ? frequency : defaultFrequency;
    }

    /**
     *
     * @return Duty cycle.
     */
    public final Double getDutyCycle() {
        return dutyCycle;
    }

    @Override
    public String toString() {
        return "{" + Integer.toString((int)Math.round(frequency)) + "," + super.toString() + "}";
    }

    /**
     * Formats IR signal as sequence of durations, with alternating signs, ignoring all signs, or by preserving signs.
     * @param alternatingSigns if true, generate alternating signs (ignoring original signs).
     * @param noSigns remove all signs.
     * @param separator
     * @param brackets if true, enclose result in brackets.
     * @return Printable string.
     */
    @Override
    public String toPrintString(boolean alternatingSigns, boolean noSigns, CharSequence separator, boolean brackets) {
        return toPrintString(alternatingSigns, noSigns, separator, brackets, true);
    }

    /**
     * Formats IR signal as sequence of durations, with alternating signs, ignoring all signs, or by preserving signs.
     * @param alternatingSigns if true, generate alternating signs (ignoring original signs).
     * @param noSigns remove all signs.
     * @param separator
     * @param brackets if true, enclose result in brackets.
     * @param includeFrequency If true, include frequency information
     * @return Printable string.
     */
    public String toPrintString(boolean alternatingSigns, boolean noSigns, CharSequence separator, boolean brackets, boolean includeFrequency) {
        return ((isEmpty() || !includeFrequency) ?  "" : ("f=" + Long.toString(Math.round(frequency))  + separator))
                + super.toPrintString(alternatingSigns, noSigns, separator, brackets);
    }

    /**
     * Formats IR signal as sequence of durations, with alternating signs or by preserving signs.
     * @param alternatingSigns if true, generate alternating signs (ignoring original signs), otherwise preserve signs.
     * @param noSigns
     * @return Printable string.
     */
    @Override
    public String toPrintString(boolean alternatingSigns, boolean noSigns) {
        return toPrintString(alternatingSigns, noSigns, " ", true);
    }

    /**
     * Formats IR signal as sequence of durations, with alternating signs or by preserving signs.
     * @param alternatingSigns if true, generate alternating signs (ignoring original signs), otherwise preserve signs.
     * @return Printable string.
     */
    @Override
    public String toPrintString(boolean alternatingSigns) {
        return toPrintString(alternatingSigns, false);
    }

    /**
     * Formats IR signal as sequence of durations, by preserving signs.
     * @return Printable string.
     */
    @Override
    public String toPrintString() {
        return toPrintString(false, false, " ", true);
    }

    /**
     * Makes the current sequence into an IrSignal by considering the sequence as an intro sequence.
     * @return IrSignal
     */
    public final IrSignal toIrSignal() {
        return new IrSignal(this, new IrSequence(), new IrSequence(), frequency, dutyCycle);
    }

    /**
     * Constructs an IrSignal.
     * @param beginningLength Length of the intro sequence
     * @param repeatLength Length of the repeat sequence
     * @param noRepeats Number of occurrences of the repeat sequence
     * @return IrSignal
     * @throws InvalidArgumentException
     */
    public IrSignal toIrSignal(int beginningLength, int repeatLength, int noRepeats) throws InvalidArgumentException {
        IrSequence intro = truncate(beginningLength);
        IrSequence repeat = subSequence(beginningLength, repeatLength);
        int startEnding = beginningLength + noRepeats * repeatLength;
        int lengthEnding = getLength() - startEnding;
        IrSequence ending = subSequence(startEnding, lengthEnding);
        return new IrSignal(intro, repeat, ending, frequency, dutyCycle);
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
        return IrCoreUtils.approximatelyEquals(this.getFrequency(), irSequence.getFrequency(), 500, 0)
                && super.approximatelyEquals(irSequence, absoluteTolerance, relativeTolerance);
    }

    /**
     *
     * @return true if and only iff the modulation frequency is zero (in numerical sense).
     */
    public final boolean isZeroModulated() {
        return frequency < zeroModulationLimit;
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
            : (Math.abs(frequency - tail.getFrequency())/frequency > allowedFrequencyDeviation))
            throw new InvalidArgumentException("concationation not possible; modulation frequencies differ");
        IrSequence irSequence = super.append(tail);
        return new ModulatedIrSequence(irSequence, frequency, dutyCycle);
    }

    @Override
    public final ModulatedIrSequence[] chop(double amount) {
        IrSequence[] irSequences = super.chop(amount);
        ModulatedIrSequence[] mods = new ModulatedIrSequence[irSequences.length];
        for (int i = 0; i < irSequences.length; i++)
            mods[i] = new ModulatedIrSequence(irSequences[i], frequency, dutyCycle);
        return mods;
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public IrSequence clone() {
        ModulatedIrSequence result = (ModulatedIrSequence) super.clone();
        return result;
    }
}
