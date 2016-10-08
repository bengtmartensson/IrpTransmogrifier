/*
Copyright (C) 2011,2012,2014 Bengt Martensson.

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

import java.util.EnumMap;
import java.util.logging.Logger;

/**
 * This class models a rendered IR signals.
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

    // TODO: move to test.
    /**
     * Just for testing. Invokes the IrSignal(String ProtocolsIniPath, int offset, String[] args)
     * and tests the result.
     *
     * @param args
     * /
     * public static void main(String[] args) {
     * if (args.length == 0) {
     * int times[] = {
     * -9024, -4512, -564, -1692, +564, -564, +564, -564, +564, -564, +564, -564, +564, -564,
     * +564, -564, +564, -564, +564, -564, +564, -1692, +564, -564, +564, -564, +564, -564, +564, -564,
     * +564, -564, +564, -564, +564, -1692, +564, -1692, +564, -564, +564, -564, +564, -564, +564, -564,
     * +564, -564, +564, -564, +564, -564, +564, -564, +564, -1692, +564, -1692, +564, -1692, +564, -1692,
     * +564, -1692, +564, -1692, +564, -43992,
     * +9024, -2256, +564, -97572};
     * try {
     * IrSignal irSignal = new IrSignal(times, 34, 2, 38400);
     * System.out.println(irSignal.ccfString());
     * System.out.println(irSignal.toString(true));
     * System.out.println(irSignal.toString(false));
     * System.out.println(irSignal);
     * } catch (IncompatibleArgumentException ex) {
     * System.err.println(ex.getMessage());
     * }
     * } else {
     * String protocolsIni = "data/IrpProtocols.ini";
     * int arg_i = 0;
     * if (args[arg_i].equals("-c")) {
     * arg_i++;
     * protocolsIni = args[arg_i++];
     * }
     * try {
     * IrSignal irSignal = new IrSignal(protocolsIni, arg_i, args);
     * System.out.println(irSignal);
     * System.out.println(irSignal.ccfString());
     * DecodeIR.invoke(irSignal);
     * } catch (IrpMasterException | FileNotFoundException ex) {
     * System.err.println(ex.getMessage());
     * }
     * }
     * }*/


    /** Intro sequence, always sent once. Can be empty, but not null. */
    protected IrSequence introSequence;

    /** Repeat sequence, sent after the intro sequence if the signal is repeating. Can be empty, but  not null. */
    protected IrSequence repeatSequence;

    /** Ending sequence, sent at the end of transmission. Can be empty (but not null),
     * actually, most often is. */
    protected IrSequence endingSequence;

    /** "Table" for mapping Pass to intro, repeat, or ending sequence. */
    protected EnumMap<Pass, IrSequence>map;

    /** Modulation frequency in Hz. Use 0 for not modulated. */
    protected double frequency;

    /** Duty cycle of the modulation. Between 0 and 1. Use -1 for not assigned. */
    protected double dutyCycle = ModulatedIrSequence.unknownDutyCycle;

    /**
     * Constructs an IrSignal from its arguments.
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     */
    public IrSignal(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, double frequency, double dutyCycle) {
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
    public IrSignal(IrSequence introSequence, IrSequence repeatSequence, IrSequence endingSequence, double frequency) {
        this(introSequence, repeatSequence, endingSequence, frequency, ModulatedIrSequence.unknownDutyCycle);
    }

    /**
     * Constructs an IrSignal from its arguments.
     *
     * @param durations
     * @param noIntroBursts
     * @param noRepeatBursts
     * @param frequency
     * @throws IncompatibleArgumentException
     */
    public IrSignal(int[] durations, int noIntroBursts, int noRepeatBursts, int frequency) throws IncompatibleArgumentException {
        this(durations, noIntroBursts, noRepeatBursts, frequency, ModulatedIrSequence.unknownDutyCycle);
    }
    /**
     * Constructs an IrSignal from its arguments.
     *
     * @param frequency
     * @param dutyCycle
     * @param introSequence
     * @param repeatSequence
     * @param endingSequence
     * @throws OddSequenceLenghtException
     */
    public IrSignal(String introSequence, String repeatSequence,
            String endingSequence, double frequency, double dutyCycle) throws OddSequenceLenghtException {
        this(new IrSequence(introSequence), new IrSequence(repeatSequence),
                new IrSequence(endingSequence), frequency, dutyCycle);
    }

    /**
     * Constructs an IrSignal from its arguments.
     *
     * The first 2*noIntroBursts durations belong to the Intro signal,
     * the next 2*noRepeatBursts to the repetition part, and the remaining to the ending sequence.
     *
     * @param durations Integer array of durations. Signs of the entries are ignored,
     * @param noIntroBursts Number of bursts (half the number of entries) belonging to the intro sequence.
     * @param noRepeatBursts Number of bursts (half the number of entries) belonging to the intro sequence.
     * @param frequency Modulation frequency in Hz.
     * @param dutyCycle Duty cycle of modulation pulse, between 0 and 1. Use -1 for not specified.
     */
    public IrSignal(int[] durations, int noIntroBursts, int noRepeatBursts, double frequency, double dutyCycle) {
        this(new IrSequence(durations, 0, 2 * noIntroBursts),
                new IrSequence(durations, 2 * noIntroBursts, 2 * noRepeatBursts),
                new IrSequence(durations, 2 * (noIntroBursts + noRepeatBursts), durations.length - 2 * (noIntroBursts + noRepeatBursts)),
                frequency, dutyCycle);
    }

    /**
     * Constructs an IrSignal of zero length.
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     */
    public IrSignal() throws IncompatibleArgumentException {
        this(new int[0], 0, 0, (int) ModulatedIrSequence.defaultFrequency);
    }

    /**
     * Creates an IrSignal from a CCF string. Also some "short formats" of CCF are recognized.
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @see Pronto
     *
     * @param ccf String supposed to represent a valid CCF signal.
     */
    public IrSignal(String ccf) throws IncompatibleArgumentException {
        copyFrom(Pronto.parse(ccf));
    }

    /**
     * Creates an IrSignal from a CCF array. Also some "short formats" of CCF are recognized.
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @see Pronto
     *
     * @param ccf Integer array supposed to represent a valid CCF signal.
     */
    public IrSignal(int[] ccf) throws IncompatibleArgumentException {
        copyFrom(Pronto.parse(ccf));
    }

    /**
     * Creates an IrSignal from a CCF array. Also some "short formats" of CCF are recognized.
     * @param begin starting index
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @see Pronto
     *
     * @param ccf String array supposed to represent a valid CCF signal.
     */
    public IrSignal(String[] ccf, int begin) throws IncompatibleArgumentException {
        copyFrom(Pronto.parse(ccf, begin));
    }

    public final double getFrequency() {
        return frequency;
    }

    public final double getDutyCycle() {
        return dutyCycle;
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public IrSignal clone() {
        IrSignal result = null;
        try {
            result = (IrSignal) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
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
     * Returns number of burst pairs in the intro sequence.
     * @see IrSequence
     * @return number of burst pairs in intro sequence
     */
    public final int getIntroBursts() {
        return introSequence.getNumberBursts();
    }

    /**
     * Returns the data in the intro sequence, as a sequence of microsecond durations.
     * @param alternatingSigns
     * @see IrSequence
     * @return integer sequence of durations in microseconds, possibly with sign.
     */
    public final int[] getIntroInts(boolean alternatingSigns) {
        return introSequence.toInts(alternatingSigns);
    }

    /**
     * Returns the data in the intro sequence, as a sequence of pulses in the used frequency.
     * @see IrSequence
     * @return integer array of pulses
     */
    public final int[] getIntroPulses() {
        return introSequence.toPulses(frequency);
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

    public final int getRepeatBursts() {
        return repeatSequence.getNumberBursts();
    }

    public final int[] getRepeatInts(boolean alternatingSigns) {
        return repeatSequence.toInts(alternatingSigns);
    }

    public final int[] getRepeatPulses() {
        return repeatSequence.toPulses(frequency);
    }

    public final double getRepeatDouble(int i) {
        return repeatSequence.get(i);
    }

    public final int getEndingLength() {
        return endingSequence.getLength();
    }

    public final int getEndingBursts() {
        return endingSequence.getNumberBursts();
    }

    public final int[] getEndingInts(boolean alternatingSigns) {
        return endingSequence.toInts(alternatingSigns);
    }

    public final int[] getEndingPulses() {
        return endingSequence.toPulses(frequency);
    }

    public final double getEndingDouble(int i) {
        return endingSequence.get(i);
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * one repeat sequence, plus the ending sequence.
     * @return duration in microseconds.
     */
    public final double getDuration() {
        return introSequence.getDuration() + repeatSequence.getDuration() + endingSequence.getDuration();
    }

    /**
     * Computes the duration in microseconds of the intro sequence,
     * repetitions repeats of the repeat sequence, plus the ending sequence.
     * Uses the count semantic.
     * @param count Uses count semantic.
     * @return duration in microseconds.
     */
    public final double getDuration(int count) {
        return introSequence.getDuration() + repeatsPerCountSemantic(count)*repeatSequence.getDuration() + endingSequence.getDuration();
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
     * Analog to the IrSequence toPrintString.
     * @param alternatingSigns If true, generated signs will have alternating signs, ignoring original signs, otherwise signs are preserved.
     * @param noSigns If true, suppress explict signs
     * @param separator separator between the numbers
     * @return Nicely formatted string.
     *
     * @see IrSequence
     */
    public String toPrintString(boolean alternatingSigns, boolean noSigns, String separator) {
        return introSequence.toPrintString(alternatingSigns, noSigns, separator) + "\n"
                + repeatSequence.toPrintString(alternatingSigns, noSigns, separator)
                + (endingSequence.getLength() > 0 ? "\n" + endingSequence.toPrintString(alternatingSigns, noSigns, separator) : "");
    }

    public String toPrintString(boolean alternatingSigns) {
        return toPrintString(alternatingSigns, false, " ");
    }

    public String toPrintString() {
        return toPrintString(false);
    }

    // helper function for the next
    private void append(int offset, int[] result, IrSequence seq) {
        for (int i = 0; i < seq.getLength(); i++)
            result[i+offset] = seq.iget(i);
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
     * Returns an integer array of one intro sequence, repeat number of repeat sequence, followed by one ending sequence.
     * The sizes can be obtained with the get*Length()- or get*Bursts()-functions.
     * @param repetitions Number of times of to include the repeat sequence.
     * @return integer array as copy.
     */
    public final int[] toIntArray(int repetitions) {
        int[] result = new int[introSequence.getLength() + repetitions*repeatSequence.getLength() + endingSequence.getLength()];
        append(0, result, introSequence);
        for (int i = 0; i < repetitions; i++)
            append(introSequence.getLength() + i*repeatSequence.getLength(), result, repeatSequence);
        append(introSequence.getLength() + repetitions*repeatSequence.getLength(), result, endingSequence);

        return result;
    }

    /**
     * Equivalent to toIntArray(1)
     * @return array of ints.
     */
    public final int[] toIntArray() {
        return toIntArray(1);
    }

    /**
     * Returns an integer array of one intro sequence, count or count-1 number of repeat sequence, dependent on if intro is empty or not, followed by one ending sequence.
     * The sizes can be obtained with the get*Length()- or get*Bursts()-functions.
     * @param count Number of times of the "signal" to send, according to the count semantic.
     * @return integer array as copy.
     */
    public final int[] toIntArrayCount(int count) {
        return toIntArray(repeatsPerCountSemantic(count));
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
        return Math.max(introSequence.getGap(), repeatSequence.getGap());
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
        return new ModulatedIrSequence(ending ? seq2.append(endingSequence) : seq2, frequency, dutyCycle);
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
        return approximatelyEquals(irSignal, IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance, IrCoreUtils.defaultFrequencyTolerance);
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
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     * @see Pronto
     * @return CCF as string.
     */
    public final String ccfString() throws OddSequenceLenghtException {
        return Pronto.toPrintString(this);
    }

    public enum Pass {
        intro,
        repeat,
        ending,
        finish
        //cancel
    }
}
