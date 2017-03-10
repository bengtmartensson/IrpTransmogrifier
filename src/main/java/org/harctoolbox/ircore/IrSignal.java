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
import java.util.Locale;
import java.util.logging.Level;
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

public class IrSignal implements Cloneable {
    private static final Logger logger = Logger.getLogger(IrSignal.class.getName());

    public static IrSignal parse(List<String> args, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        IrSignal irSignal = null;
        try {
            irSignal = Pronto.parse(args);
        } catch (InvalidArgumentException ex) {
        }
        if (irSignal != null) {
            if (frequency != null)
                throw new InvalidArgumentException("Must not use explicit frequency with a Pronto type signal.");
            return irSignal;
        }
        return parseRaw(args, frequency, fixOddSequences);
    }

    public static IrSignal parseRaw(List<String> args, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        if (frequency == null)
            logger.log(Level.WARNING, String.format(Locale.US, "Unknown frequency, assuming default frequency = %d Hz",
                    (int) ModulatedIrSequence.defaultFrequency));

        String str = String.join(" ", args).trim();

        IrSequence intro;
        IrSequence repeat = null;
        IrSequence ending = null;

        if (str.startsWith("[")) {
            String[] parts = str.replace("[", "").split("\\]");
            if (parts.length < 2) {
                throw new InvalidArgumentException("Less than two parts");
            }
            intro = new IrSequence(parts[0]);
            repeat = new IrSequence(parts[1]);
            ending = (parts.length >= 3) ? new IrSequence(parts[2]) : null;
        } else
            intro = new IrSequence(str);

        return new IrSignal(intro, repeat, ending, frequency);
    }

    /** Intro sequence, always sent once. Can be empty, but not null. */
    protected IrSequence introSequence;

    /** Repeat sequence, sent after the intro sequence if the signal is repeating. Can be empty, but  not null. */
    protected IrSequence repeatSequence;

    /** Ending sequence, sent at the end of transmission. Can be empty (but not null),
     * actually, most often is. */
    protected IrSequence endingSequence;

    /** "Table" for mapping Pass to intro, repeat, or ending sequence. */
    protected EnumMap<Pass, IrSequence>map;

    /** Modulation frequency in Hz. Use 0 for not modulated, and null for defaulted. */
    protected Double frequency;

    /** Duty cycle of the modulation. Between 0 and 1. Use null for not assigned. */
    protected Double dutyCycle = null;

    /**
     * Constructs an IrSignal from its arguments.
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     */
    public IrSignal(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, Double frequency, Double dutyCycle) {
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
        // If the given intro sequence is identical to the repeat sequence, reject it.
        this.introSequence = ((introSequence != null) && !introSequence.approximatelyEquals(repeatSequence)) ? introSequence : new IrSequence();
        this.repeatSequence = repeatSequence != null ? repeatSequence : new IrSequence();
        this.endingSequence = ((endingSequence != null) && !endingSequence.approximatelyEquals(repeatSequence)) ? endingSequence : new IrSequence();

        map = new EnumMap<>(Pass.class);

        map.put(Pass.intro, introSequence);
        map.put(Pass.repeat, repeatSequence);
        map.put(Pass.ending, endingSequence);
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
     * Constructs an IrSignal from its arguments.
     *
     * @param durations
     * @param noIntro
     * @param noRepeat
     * @param frequency
     * @throws InvalidArgumentException
     */
    public IrSignal(int[] durations, int noIntro, int noRepeat, Integer frequency) throws InvalidArgumentException {
        this(durations, noIntro, noRepeat, Double.valueOf(frequency), null);
    }
    /**
     * Constructs an IrSignal from its arguments.
     *
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     * @throws OddSequenceLengthException
     */
    public IrSignal(String introSequence, String repeatSequence,
            String endingSequence, double frequency, double dutyCycle) throws OddSequenceLengthException {
        this(new IrSequence(introSequence), new IrSequence(repeatSequence),
                new IrSequence(endingSequence), frequency, dutyCycle);
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
        this(new int[0], 0, 0, ModulatedIrSequence.defaultFrequency, null);
    }

    /**
     * Creates an IrSignal from a CCF string. Also some "short formats" of CCF are recognized.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @see Pronto
     *
     * @param ccf String supposed to represent a valid CCF signal.
     */
    public IrSignal(String ccf) throws InvalidArgumentException {
        copyFrom(Pronto.parse(ccf));
    }

    /**
     * Creates an IrSignal from a CCF array. Also some "short formats" of CCF are recognized.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @see Pronto
     *
     * @param ccf Integer array supposed to represent a valid CCF signal.
     */
    public IrSignal(int[] ccf) throws InvalidArgumentException, OddSequenceLengthException {
        copyFrom(Pronto.parse(ccf));
    }

    /**
     * Creates an IrSignal from a CCF array. Also some "short formats" of CCF are recognized.
     * @param begin starting index
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @see Pronto
     *
     * @param ccf String array supposed to represent a valid CCF signal.
     */
    public IrSignal(String[] ccf, int begin) throws InvalidArgumentException {
        copyFrom(Pronto.parse(ccf, begin));
    }

    public final Double getFrequency() {
        return frequency;
    }

    public final Double getDutyCycle() {
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
    public final int getIntroLength() {
        return introSequence.getLength();
    }

    /**
     * Returns the data in the intro sequence, as a sequence of microsecond durations.
     * @see IrSequence
     * @return integer sequence of durations in microseconds, possibly with sign.
     */
    public final int[] getIntroInts() {
        return introSequence.toInts();
    }

    /**
     * Returns the i'th data in the intro sequence, as double.
     * @see IrSequence
     * @param i index
     * @return duration, possibly with sign.
     */
    public final double getIntroDouble(int i) {
        return introSequence.get(i);
    }

    public final int getRepeatLength() {
        return repeatSequence.getLength();
    }

    public final int[] getRepeatInts() {
        return repeatSequence.toInts();
    }

    public final double getRepeatDouble(int i) {
        return repeatSequence.get(i);
    }

    public final int getEndingLength() {
        return endingSequence.getLength();
    }

    public final int[] getEndingInts() {
        return endingSequence.toInts();
    }

    public final double getEndingDouble(int i) {
        return endingSequence.get(i);
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * one repeat sequence, plus the ending sequence.
     * @return duration in microseconds.
     */
    public final double getTotalDuration() {
        return introSequence.getTotalDuration() + repeatSequence.getTotalDuration() + endingSequence.getTotalDuration();
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * repetitions repeats of the repeat sequence, plus the ending sequence.
     * Uses the count semantic.
     * @param count Uses count semantic.
     * @return duration in microseconds.
     */
    public final double getDuration(int count) {
        return introSequence.getTotalDuration() + repeatsPerCountSemantic(count)*repeatSequence.getTotalDuration() + endingSequence.getTotalDuration();
    }

    public final double getDouble(Pass pass, int i) {
        return map.get(pass).get(i);
    }

    public final int getLength(Pass pass) {
        return map.get(pass).getLength();
    }

    @Override
    public String toString() {
        return "Freq=" + Math.round(frequency) + "Hz " + introSequence + repeatSequence + endingSequence;
    }

    /**
     * Generates nice string. Generate alternating signs according to the parameter.
     *
     * @param alternatingSigns if true generate alternating signs, otherwise remove signs.
     * @return nice string.
     */
    public String toString(boolean alternatingSigns) {
        return "Freq=" + Math.round(frequency) + "Hz " + introSequence.toString(alternatingSigns)
                + repeatSequence.toString(alternatingSigns) + endingSequence.toString(alternatingSigns);
    }

    /**
     * Returns the number of repetitions according to the count semantics.
     * @param count
     * @return introSequence.isEmpty() ? count : count - 1
     */
    public final int repeatsPerCountSemantic(int count) {
        return introSequence.isEmpty() ? count : count - 1;
    }

    /**
     *
     * @return Emptyness of the signal.
     */
    public final boolean isEmpty() {
        return introSequence.isEmpty() && repeatSequence.isEmpty() && endingSequence.isEmpty();
    }

    /**
     * Returns true if and only if the sequence contains durations of zero length.
     * @return existence of zero durations.
     */
    public final boolean containsZeros() {
        return introSequence.containsZeros() || repeatSequence.containsZeros() || endingSequence.containsZeros();
    }

    /**
     * Replace all zero durations. Changes the signal in-place.
     * @param replacement Duration in micro seconds to replace zero durations with.
     */
    public final void replaceZeros(double replacement) {
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
    public final void replaceZeros(int replacement) {
        replaceZeros(frequency > 0
                ? IrCoreUtils.seconds2microseconds(replacement / frequency)
                : replacement);
    }

    /**
     * Returns max gap of intro- and repeat sequences.
     * @return max gap of intro- and repeat sequences.
     */
    public final double getGap() {
        return Math.max(introSequence.getLastGap(), repeatSequence.getLastGap());
    }


    // Plunders the victim. Therefore private, othewise would violate immutability.
    private void copyFrom(IrSignal victim) {
        dutyCycle = victim.dutyCycle;
        frequency = victim.frequency;
        introSequence = victim.introSequence;
        repeatSequence = victim.repeatSequence;
        endingSequence = victim.endingSequence;
        map = victim.map;
    }

    /**
     * Returns a ModulatedIrSequence consisting of one intro sequence,
     * count or count-1 number of repeat sequence, dependent on if intro is empty or not, followed by one ending sequence.
     * @param count Number of times to send signal. Must be &gt; 0.
     * @return ModulatedIrSequence.
     */
    public final ModulatedIrSequence toModulatedIrSequence(int count) {
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
    public final ModulatedIrSequence toModulatedIrSequence(boolean intro, int repetitions, boolean ending) {
        IrSequence seq1 = intro ? introSequence : new IrSequence();
        IrSequence seq2 = seq1.append(repeatSequence, repetitions);
        IrSequence seq3 = seq2.append(endingSequence);

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
    public final IrSignal toOneShot(int count) {
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
        return approximatelyEquals(irSignal, IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE, IrCoreUtils.DEFAULTFREQUENCYTOLERANCE);
    }

    /**
     *
     * @return the intro sequence, as ModulatedIrSequence
     */
    public final ModulatedIrSequence getIntroSequence() {
        return new ModulatedIrSequence(introSequence, frequency, dutyCycle);
    }

    /**
     *
     * @return the repeat sequence, as ModulatedIrSequence
     */
    public final ModulatedIrSequence getRepeatSequence() {
        return new ModulatedIrSequence(repeatSequence, frequency, dutyCycle);
    }

    /**
     *
     * @return the ending sequence, as ModulatedIrSequence
     */
    public final ModulatedIrSequence getEndingSequence() {
        return new ModulatedIrSequence(endingSequence, frequency, dutyCycle);
    }

    /**
     * Computes the CCF form, if possible. Since a CCF does not have an ending sequence,
     * a nonempty ending sequence will be ignored.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @see Pronto
     * @return CCF as string.
     */
    public final String ccfString() throws OddSequenceLengthException {
        return Pronto.toPrintString(this);
    }

    public enum Pass {
        intro,
        repeat,
        ending,
        finish
    }
}
