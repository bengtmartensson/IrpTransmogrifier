/*
Copyright (C) 2011,2012,2014,2016, 2017 Bengt Martensson.

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
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class models a numerical IR signals.
 * It consists of a frequency, a duty cycle, and an intro sequence,
 * a repeat sequence, and an ending sequence. Any of the latter three,
 * but not all, can be empty, but not null. If the ending sequence is non-empty,
 * the repeat sequence has to be non-empty too.
 * <p>
 * The "count" semantic: The "count" argument in functions like toModulatedIrSequece(int count) is interpreted like this:
 * If the intro sequence is null, then "count" copies or the repeat sequence are used, otherwise count-1.
 * It is believed that this interpretation is consistent with the heuristic meaning of 'sending a signal "count" times'.
 *
 * <p>
 * The "repetitions" semantic: "repetition" number of copies of the repeat sequence are used.
 *
 * <p>This class is immutable.
 *
 * @see IrSequence
 *
 */

public final class IrSignal implements Cloneable {
    private static final Logger logger = Logger.getLogger(IrSignal.class.getName());

    /** Intro sequence, always sent once. Can be empty, but not null. */
    private IrSequence introSequence;

    /** Repeat sequence, sent after the intro sequence if the signal is repeating. Can be empty, but  not null. */
    private IrSequence repeatSequence;

    /** Ending sequence, sent at the end of transmission. Can be empty, but not null.
     * Actually, most often is empty. */
    private IrSequence endingSequence;

    /** "Table" for mapping Pass to intro, repeat, or ending sequence. */
    private EnumMap<Pass, IrSequence> map;

    /** Modulation frequency in Hz. Use 0 for not modulated, and null for defaulted. */
    private Double frequency;

    /** Duty cycle of the modulation. Between 0 and 1. Use null for not assigned. */
    private Double dutyCycle = null;

    /**
     * Constructs an IrSignal from its arguments.
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     */
    public IrSignal(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, Double frequency, Double dutyCycle) {
        setup(introSequence, repeatSequence, endingSequence, frequency, dutyCycle);
    }

   /**
     * Constructs an IrSignal from its arguments.
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public IrSignal(String introSequence, String repeatSequence, String endingSequence, Double frequency, Double dutyCycle) throws OddSequenceLengthException {
        setup(new IrSequence(introSequence), new IrSequence(repeatSequence), new IrSequence(endingSequence), frequency, dutyCycle);
    }

    /**
     *  Constructs an IrSignal from its arguments. The single sequence is made intro sequence.
     * @param introSequence
     * @param frequency
     * @param dutyCycle
     */
    public IrSignal(IrSequence introSequence, Double frequency, Double dutyCycle) {
        this(introSequence, null, null, frequency, dutyCycle);
    }

    /**
     * Constructs an IrSignal from its arguments.
     * @param frequency
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     */
    public IrSignal(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, Double frequency) {
        this(introSequence, repeatSequence, endingSequence, frequency, null);
    }

    /**
     * Convenience constructor for IrSignals having only an intro sequence.
     * @param introSequence
     */
    public IrSignal(ModulatedIrSequence introSequence) {
        this(introSequence, new IrSequence(), new IrSequence(), introSequence.getFrequency(), introSequence.getDutyCycle());
    }

    /**
     * Constructs an IrSignal.
     * @param sequence
     * @param beginningLength Length of the intro sequence
     * @param repeatLength Length of the repeat sequence
     * @param noRepeats Number of occurrences of the repeat sequence
     * @throws InvalidArgumentException
     */
    public IrSignal(ModulatedIrSequence sequence, int beginningLength, int repeatLength, int noRepeats) throws InvalidArgumentException {
        IrSequence intro = sequence.truncate(beginningLength);
        IrSequence repeat = sequence.subSequence(beginningLength, repeatLength);
        int startEnding = beginningLength + noRepeats * repeatLength;
        int lengthEnding = sequence.getLength() - startEnding;
        IrSequence ending = sequence.subSequence(startEnding, lengthEnding);
        setup(intro, repeat, ending, sequence.getFrequency(), sequence.getDutyCycle());
    }

    /**
     * Constructs an IrSignal from its arguments.
     *
     * @param durations
     * @param noIntro
     * @param noRepeat
     * @param frequency
     * @throws InvalidArgumentException
     */
    public IrSignal(int[] durations, int noIntro, int noRepeat, double frequency) throws InvalidArgumentException {
        this(durations, noIntro, noRepeat, frequency, null);
    }

    /**
     * Constructs an IrSignal from its arguments.
     *
     * The first noIntro durations belong to the Intro signal,
     * the next noRepeat to the repetition part, and the remaining to the ending sequence.
     *
     * @param durations Integer array of durations. Signs of the entries are ignored,
     * @param noIntro Number of entries belonging to the intro sequence.
     * @param noRepeat Number of entries belonging to the repeat sequence.
     * @param frequency Modulation frequency in Hz. Use null for default
     * @param dutyCycle Duty cycle of modulation pulse, between 0 and 1. Use null for not specified.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public IrSignal(int[] durations, int noIntro, int noRepeat, Double frequency, Double dutyCycle) throws InvalidArgumentException {
        this(new IrSequence(durations, 0, noIntro),
                new IrSequence(durations, noIntro, noRepeat),
                new IrSequence(durations, noIntro + noRepeat, durations.length - (noIntro + noRepeat)),
                frequency, dutyCycle);
    }

    public IrSignal(IrSequence irSequence, int noIntro, int noRepeat, Double frequency, Double dutyCycle) throws InvalidArgumentException {
        this(irSequence.subSequence(0, noIntro),
                irSequence.subSequence(noIntro, noRepeat),
                irSequence.subSequence(noIntro + noRepeat, irSequence.getLength() - noIntro - noRepeat),
                frequency, dutyCycle);
    }

    /**
     * Constructs an IrSignal of zero length.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public IrSignal() throws InvalidArgumentException {
        this(new int[0], 0, 0, ModulatedIrSequence.DEFAULT_FREQUENCY, null);
    }

    private void setup(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, Double frequency, Double dutyCycle) {
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
        // If the given intro sequence is identical to the repeat sequence, reject it.
        this.introSequence = ((introSequence != null) && !introSequence.approximatelyEquals(repeatSequence)) ? introSequence : new IrSequence();
        this.repeatSequence = repeatSequence != null ? repeatSequence : new IrSequence();
        this.endingSequence = ((endingSequence != null) && !endingSequence.approximatelyEquals(repeatSequence)) ? endingSequence : new IrSequence();

        map = new EnumMap<>(Pass.class);

        map.put(Pass.intro, this.introSequence);
        map.put(Pass.repeat, this.repeatSequence);
        map.put(Pass.ending, this.endingSequence);
    }

    public IrSignal setFrequency(Double newFrequency) {
        return new IrSignal(introSequence, repeatSequence, endingSequence, newFrequency != null ? newFrequency : frequency);
    }

    public Double getFrequency() {
        return frequency;
    }

    public Double getFrequencyWithDefault() {
        return frequency != null ? frequency : ModulatedIrSequence.DEFAULT_FREQUENCY;
    }

    public Double getDutyCycle() {
        return dutyCycle;
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public IrSignal clone() {
        IrSignal result = null;
        try {
            result = (IrSignal) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new ThisCannotHappenException(ex);
        }
        result.introSequence = introSequence.clone();
        result.repeatSequence = repeatSequence.clone();
        result.endingSequence = endingSequence.clone();
        return result;
    }

    /**
     * Returns length of Intro sequence, in number of flashes and gaps.
     * @see IrSequence
     * @return length of intro
     */
    public int getIntroLength() {
        return introSequence.getLength();
    }

    /**
     * Returns the data in the intro sequence, as a sequence of microsecond durations.
     * @see IrSequence
     * @return integer sequence of durations in microseconds, possibly with sign.
     */
    public int[] getIntroInts() {
        return introSequence.toInts();
    }

    /**
     * Returns the i'th data in the intro sequence, as double.
     * @see IrSequence
     * @param i index
     * @return duration, possibly with sign.
     */
    public double getIntroDouble(int i) {
        return introSequence.get(i);
    }

    /**
     * Returns the data in the intro sequence, as a sequence of pulses in the used frequency.
     * @see IrSequence
     * @return integer array of pulses
     */
    public int[] getIntroPulses() {
        return introSequence.toPulses(frequency);
    }

    public int getRepeatLength() {
        return repeatSequence.getLength();
    }

    public int[] getRepeatInts() {
        return repeatSequence.toInts();
    }

    public double getRepeatDouble(int i) {
        return repeatSequence.get(i);
    }

    /**
     * Returns the data in the intro sequence, as a sequence of pulses in the used frequency.
     * @see IrSequence
     * @return integer array of pulses
     */
    public int[] getRepeatPulses() {
        return repeatSequence.toPulses(frequency);
    }

    public int getEndingLength() {
        return endingSequence.getLength();
    }

    public int[] getEndingInts() {
        return endingSequence.toInts();
    }

    public double getEndingDouble(int i) {
        return endingSequence.get(i);
    }

    /**
     * Returns the data in the intro sequence, as a sequence of pulses in the used frequency.
     * @see IrSequence
     * @return integer array of pulses
     */
    public int[] getEndingPulses() {
        return endingSequence.toPulses(frequency);
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * one repeat sequence, plus the ending sequence.
     * @return duration in microseconds.
     */
    public double getTotalDuration() {
        return introSequence.getTotalDuration() + repeatSequence.getTotalDuration() + endingSequence.getTotalDuration();
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * repetitions repeats of the repeat sequence, plus the ending sequence.
     * Uses the count semantic.
     * @param count Uses count semantic.
     * @return duration in microseconds.
     */
    public double getDuration(int count) {
        return introSequence.getTotalDuration() + repeatsPerCountSemantic(count)*repeatSequence.getTotalDuration() + endingSequence.getTotalDuration();
    }

    public double getDouble(Pass pass, int i) {
        return map.get(pass).get(i);
    }

    public int getLength(Pass pass) {
        return map.get(pass).getLength();
    }

    public boolean introOnly() {
        return repeatSequence.isEmpty() && endingSequence.isEmpty();
    }

    public boolean repeatOnly() {
        return introSequence.isEmpty() && endingSequence.isEmpty();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Generates nice string. Generate alternating signs according to the parameter.
     *
     * @param alternatingSigns if true generate alternating signs, otherwise remove signs.
     * @return nice string.
     */
    public String toString(boolean alternatingSigns) {
        return "Freq=" + getFrequencyAsString() + introSequence.toString(alternatingSigns)
                + repeatSequence.toString(alternatingSigns) + endingSequence.toString(alternatingSigns);
    }

    public String getFrequencyAsString() {
        return (frequency != null ? Long.toString(Math.round(frequency)) : "?") + "Hz";
    }

    /**
     * Returns the number of repetitions according to the count semantics.
     * @param count
     * @return introSequence.isEmpty() ? count : count - 1
     */
    public int repeatsPerCountSemantic(int count) {
        return introSequence.isEmpty() ? count : count - 1;
    }

    /**
     *
     * @return Emptyness of the signal.
     */
    public boolean isEmpty() {
        return introSequence.isEmpty() && repeatSequence.isEmpty() && endingSequence.isEmpty();
    }

    /**
     * Returns true if and only if the sequence contains durations of zero length.
     * @return existence of zero durations.
     */
    public boolean containsZeros() {
        return introSequence.containsZeros() || repeatSequence.containsZeros() || endingSequence.containsZeros();
    }

    /**
     * Replace all zero durations. Changes the signal in-place.
     * @param replacement Duration in micro seconds to replace zero durations with.
     */
    public void replaceZeros(double replacement) {
        introSequence.replaceZeros(replacement);
        repeatSequence.replaceZeros(replacement);
        endingSequence.replaceZeros(replacement);
    }

    /**
     * Replace all zero durations. Changes the signal in-place.
     *
     * @param replacement Duration in pulses to replace zero durations with.
     * If frequency == 0, interpret as microseconds instead.
     */
    public void replaceZeros(int replacement) {
        replaceZeros(frequency > 0
                ? IrCoreUtils.seconds2microseconds(replacement / frequency)
                : replacement);
    }

    /**
     * Returns max gap of intro- and repeat sequences.
     * @return max gap of intro- and repeat sequences.
     */
    public double getGap() {
        return Math.max(introSequence.getLastGap(), repeatSequence.getLastGap());
    }

    /**
     * Returns a ModulatedIrSequence consisting of one intro sequence,
     * one repeat sequence, followed by one ending sequence.
     * @return ModulatedIrSequence.
     */
    public ModulatedIrSequence toModulatedIrSequence() {
        return toModulatedIrSequence(true, 1, true);
    }

    /**
     * Returns a ModulatedIrSequence consisting of one intro sequence,
     * count or count-1 number of repeat sequence, dependent on if intro is empty or not, followed by one ending sequence.
     * @param count Number of times to send signal. Must be &gt; 0.
     * @return ModulatedIrSequence.
     */
    public ModulatedIrSequence toModulatedIrSequence(int count) {
        return toModulatedIrSequence(true, this.repeatsPerCountSemantic(count), true);
    }

    /**
     * Returns a ModulatedIrSequence consisting of zero or one intro sequence,
     * repetition number of repeat sequence, and zero or one ending sequence.
     * @param intro inclusion of intro sequence?
     * @param repetitions number of repetitions (repeat semantic)
     * @param ending inclusion of ending sequence.
     * @return ModulatedIrSequence.
     */
    public ModulatedIrSequence toModulatedIrSequence(boolean intro, int repetitions, boolean ending) {
        IrSequence seq1 = intro ? introSequence : new IrSequence();
        IrSequence seq2 = seq1.append(repeatSequence, repetitions);
        IrSequence seq3 = ending ? seq2.append(endingSequence) : seq2;

        return new ModulatedIrSequence(seq3, frequency, dutyCycle);
    }

    public List<IrSequence> toIrSequences() {
        List<IrSequence> list = new ArrayList<>(3);
        list.add(introSequence);
        list.add(repeatSequence);
        list.add(endingSequence);
        return list;
    }

    /**
     * Returns an IrSignal consisting of count repetitions (count semantic) as the intro sequence,
     * while repeat and ending are empty.
     * @param count Number of times to send signal. Must be &gt; 0.
     * @return IrSignal consisting of count repetitions (count semantic) as the intro sequence.
     */
    public IrSignal toOneShot(int count) {
        return new IrSignal(toModulatedIrSequence(count), null, null, frequency, dutyCycle);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSignal to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @param frequencyTolerance tolerance (absolute) for frequency in Hz.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(IrSignal irSignal, double absoluteTolerance, double relativeTolerance,
            double frequencyTolerance) {
        return IrCoreUtils.approximatelyEquals(frequency, irSignal.frequency, frequencyTolerance, 0)
                && introSequence.approximatelyEquals(irSignal.introSequence, absoluteTolerance, relativeTolerance)
                && repeatSequence.approximatelyEquals(irSignal.repeatSequence, absoluteTolerance, relativeTolerance)
                && endingSequence.approximatelyEquals(irSignal.endingSequence, absoluteTolerance, relativeTolerance);
    }

    /**
     * Compares two ModulatedIrSequences for (approximate) equality.
     *
     * @param irSignal to be compared against this.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(IrSignal irSignal) {
        return approximatelyEquals(irSignal, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE, IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE);
    }

    /**
     *
     * @return the intro sequence, as ModulatedIrSequence
     */
    public ModulatedIrSequence getIntroSequence() {
        return new ModulatedIrSequence(introSequence, frequency, dutyCycle);
    }

    /**
     *
     * @return the repeat sequence, as ModulatedIrSequence
     */
    public ModulatedIrSequence getRepeatSequence() {
        return new ModulatedIrSequence(repeatSequence, frequency, dutyCycle);
    }

    /**
     *
     * @return the ending sequence, as ModulatedIrSequence
     */
    public ModulatedIrSequence getEndingSequence() {
        return new ModulatedIrSequence(endingSequence, frequency, dutyCycle);
    }

    public int[] toIntArray(int i) {
        ModulatedIrSequence seq = toModulatedIrSequence(i);
        return seq.toInts();
    }

    public enum Pass {
        intro,
        repeat,
        ending,
        finish
    }
}
