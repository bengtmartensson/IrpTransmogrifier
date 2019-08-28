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
package org.harctoolbox.ircore;

import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class, containing only static functions, allows for the creation of integer arrays
 * and strings containing Pronto (CCF) form of the signal. It cannot be instantiated,
 * since there are no "Pronto"s, it is just IrSignals in "another coordinates".
 */
public abstract class Pronto {

    /**
     * Number of characters in the hexadecimal digits of Pronto strings.
     */
    public final static int CHARS_IN_DIGIT = 4;

    /**
     * Constant used for computing the frequency code from the frequency
     */
    public final static double FREQUENCY_CONSTANT = 0.241246;

    /**
     * For non-modulated IR signals, use this as the second argument of the CCF
     * form.
     */
    private final static int FREQUENCY_ZERO_FALLBACK_FREQUNCY_CODE = 10;

    /**
     * Format code used to format integers in the Pronto Hex.
     */
    public final static String HEX_STRING_FORMAT = "%04X";

    /**
     * Constant denoting "learned", modulated signals.
     */
    protected final static int LEARNED_CODE = 0x0000;

    /**
     * Constant denoting "learned", non-modulated signals.
     */
    protected final static int LEARNED_UNMODULATED_CODE = 0x0100;

    protected final static int TYPE_INDEX = 0;
    protected final static int FREQUENCY_INDEX = 1;
    protected final static int INTRO_LENGTH_INDEX = 2;
    protected final static int REPEAT_LENGTH_INDEX = 3;
    protected final static int NUMBER_METADATA = 4;
    protected final static int MIN_CCF_LENGTH = NUMBER_METADATA + 2;

    private static final Logger logger = Logger.getLogger(Pronto.class.getName());

    /**
     * Formats an integer like seen in CCF strings, in printf-ish, using "%04X".
     * @param n Integer to be formatted.
     * @return Formatted string
     */
    public static String formatInteger(int n) {
        return String.format(HEX_STRING_FORMAT, n);
    }

    /**
     * Returns frequency code from frequency in Hz (the second number in the CCF).
     *
     * @param frequency Frequency in Hz.
     * @return code for the frequency.
     */
    public static int frequencyCode(double frequency) {
        return frequency > 0
                ? (int) Math.round(1000000d / (frequency * FREQUENCY_CONSTANT))
                : FREQUENCY_ZERO_FALLBACK_FREQUNCY_CODE;
    }

    /**
     * Computes the carrier frequency in Hz.
     * @param code Pronto frequency code
     * @return Frequency in Hz.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public static double frequency(int code) throws InvalidArgumentException {
        return 1d / pulseTime(code);
    }

    /**
     * Computes pulse time in seconds.
     * @param code Pronto frequency code.
     * @return Duration of one pulse of the carrier in seconds.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public static double pulseTime(int code) throws InvalidArgumentException { // in microseconds
        if (code <= 0)
            throw new InvalidArgumentException();

        return IrCoreUtils.microseconds2seconds(code * FREQUENCY_CONSTANT);
    }

    /**
     * Computes number of cycles of the carrier the first argument will require.
     * For frequency == 0, use 414514 as substitute.
     *
     * @param time duration in seconds
     * @param frequency
     * @return number of pulses
     * @throws org.harctoolbox.ircore.InvalidArgumentException if frequency &lt;= 0.
     */
    public static int pulses(double time, double frequency) throws InvalidArgumentException {
        if (frequency < 0)
            throw new InvalidArgumentException("Frequency must be >= 0");
        double actualFrequency = frequency > 0 ? frequency : frequency(FREQUENCY_ZERO_FALLBACK_FREQUNCY_CODE);
        return Math.min((int)Math.round(time * actualFrequency), 0xFFFF);
    }

    /**
     * Computes number of cycles of the carrier the first argument will require.
     *
     * @param time duration in micro seconds
     * @param frequency
     * @return number of pulses
     * @throws org.harctoolbox.ircore.InvalidArgumentException if frequency &lt;= 0.
     */
    public static int pulsesMicroSeconds(double time, double frequency) throws InvalidArgumentException {
        return pulses(IrCoreUtils.microseconds2seconds(time), frequency);
    }

    private static double[] usArray(int frequencyCode, int[] ccfArray, int beg, int end) throws InvalidArgumentException {
        double pulseTimeMicrosSeconds = IrCoreUtils.seconds2microseconds(pulseTime(frequencyCode));
        double[] data = new double[end - beg];
        for (int i = beg; i < end; i++)
            data[i-beg] = pulseTimeMicrosSeconds*ccfArray[i];

        return data;
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF signal.
     * @param ccf CCF signal
     * @return  IrSignal
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public static IrSignal parse(int[] ccf) throws OddSequenceLengthException, InvalidArgumentException {
        return parse(ccf, false);
    }

    private static IrSignal parse(int[] ccf, boolean loose) throws OddSequenceLengthException, InvalidArgumentException {
        if (ccf.length < MIN_CCF_LENGTH)
            throw new InvalidArgumentException("Pronto Hex is invalid since it is just " + ccf.length + " < " + MIN_CCF_LENGTH + " numbers long.");
        if (ccf.length % 2 != 0)
            throw new OddSequenceLengthException("Pronto Hex is invalid since it has an odd number ("
                    + ccf.length + ") of durations.");

        int type = ccf[TYPE_INDEX];
        int frequencyCode = ccf[FREQUENCY_INDEX];
        int introLength = ccf[INTRO_LENGTH_INDEX];
        int repeatLength = ccf[REPEAT_LENGTH_INDEX];
        int expectedLength = NUMBER_METADATA + 2 * (introLength + repeatLength);
        if (expectedLength != ccf.length) {
            if (loose) {
                logger.log(Level.WARNING, "Inconsistent length in Pronto Hex (claimed {0} pairs, was {1} pairs). "
                        + "Intro length set to {1} pairs; repeat length set to 0.",
                        new Object[]{introLength + repeatLength, (ccf.length - NUMBER_METADATA) / 2});
                introLength = (ccf.length - NUMBER_METADATA) / 2;
                repeatLength = 0;
            } else
                throw new InvalidArgumentException("Inconsistent length in Pronto Hex (claimed "
                        + (introLength + repeatLength) + " pairs, was " + (ccf.length - NUMBER_METADATA) / 2 + " pairs).");
        }
        IrSignal irSignal = null;

        switch (type) {
            case LEARNED_CODE: // 0x0000
            case LEARNED_UNMODULATED_CODE: // 0x0100
                double[] intro = usArray(frequencyCode, ccf, NUMBER_METADATA, NUMBER_METADATA + 2*introLength);
                double[] repeat = usArray(frequencyCode, ccf, NUMBER_METADATA + 2*introLength, NUMBER_METADATA + 2*(introLength + repeatLength));
                IrSequence introSequence = new IrSequence(intro);
                IrSequence repeatSequence = new IrSequence(repeat);
                irSignal = new IrSignal(introSequence, repeatSequence, null,
                        type == LEARNED_CODE ? frequency(frequencyCode) : 0,
                        null);
                break;

            default:
                throw new InvalidArgumentException("Pronto Hex type 0x" + formatInteger(type) + " not supported.");
                // ... but see the class org.harctoolbox.irp.ShortPronto...
        }
        return irSignal;
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param ccfString CCF signal
     * @return IrSignal
     * @throws InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static IrSignal parse(String ccfString) throws NonProntoFormatException, InvalidArgumentException {
        return parse(ccfString, false);
    }

    public static IrSignal parseLoose(String ccfString) throws NonProntoFormatException, InvalidArgumentException {
        return parse(ccfString, true);
    }

    static IrSignal parse(String ccfString, boolean loose) throws NonProntoFormatException, InvalidArgumentException {
        int[] ccf = parseAsInts(ccfString);
        return parse(ccf, loose);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param array Strings representing hexadecimal numbers
     * @param begin Starting index
     * @return IrSignal
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public static IrSignal parse(String[] array, int begin) throws NonProntoFormatException, InvalidArgumentException {
        int[] ccf = parseAsInts(array, begin);
        return parse(ccf);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param array Strings representing hexadecimal numbers
     * @return IrSignal
     * @throws InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static IrSignal parse(String[] array) throws InvalidArgumentException, NonProntoFormatException {
        return parse(array, 0);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param list Strings representing hexadecimal numbers
     * @return IrSignal
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static IrSignal parse(List<String> list) throws InvalidArgumentException, NonProntoFormatException {
        int[] ccf = parseAsInts(list);
        return parse(ccf);
    }

    /**
     * Tries to parse the string as argument.
     * Can be used to test "Proto-ness" of an unknown string.
     *
     * @param ccfString Input string, to be parsed/tested.
     * @return Integer array of numbers if successful, null if unsuccessful.
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    protected static int[] parseAsInts(String ccfString) throws NonProntoFormatException {
        String[] array = ccfString.trim().split("\\s+");
        return parseAsInts(array, 0);
    }

    /**
     * Tries to parse the strings as argument.
     * Can be used to test "Proto-ness" of an unknown array of strings.
     *
     * @param array Input strings, to be parsed/tested.
     * @param begin Starting index
     * @return Integer array of numbers if successful, null if unsuccessful (e.g. by NumberFormatException).
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static int[] parseAsInts(String[] array, int begin) throws NonProntoFormatException {
        int[] ccf = new int[array.length];

        for (int i = begin; i < array.length; i++) {
            String string = array[i];
            if (string == null || string.isEmpty() || string.charAt(0) == '-' || string.charAt(0) == '+' || string.length() != CHARS_IN_DIGIT)
                throw new NonProntoFormatException(string, i);
            try {
                ccf[i] = Integer.parseInt(string, 16);
            } catch (NumberFormatException ex) {
                throw new NonProntoFormatException(string, i);
            }
        }
        return ccf;
    }

    protected static int[] parseAsInts(List<String> list) throws NonProntoFormatException {
        String[] x = list.toArray(new String[list.size()]);
        return parseAsInts(x, 0);
    }

    /**
     * CCF array of complete signal, i.e. the CCF string before formatting, including the header.
     * @param irSignal
     * @return CCF array
     * @throws OddSequenceLengthException
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static int[] toArray(IrSignal irSignal) throws OddSequenceLengthException, InvalidArgumentException {
        if (irSignal.getIntroLength() % 2 != 0 || irSignal.getRepeatLength() % 2 != 0)
            // Probably forgot normalize() if I get here.
            throw new OddSequenceLengthException();
        if (irSignal.getEndingLength() != 0)
            logger.log(Level.WARNING,
                    "When computing the Pronto representation, a (non-empty) ending sequence was ignored");

        int[] data = new int[4 + irSignal.getIntroLength() + irSignal.getRepeatLength()];
        int index = 0;
        int frequency = irSignal.getFrequencyWithDefault().intValue();
        data[index++] = frequency > 0 ? LEARNED_CODE : LEARNED_UNMODULATED_CODE;
        data[index++] = frequencyCode(frequency);
        data[index++] = irSignal.getIntroLength()/2;
        data[index++] = irSignal.getRepeatLength()/2;
        for (int i = 0; i < irSignal.getIntroLength(); i++)
            data[index++] = pulsesMicroSeconds(irSignal.getIntroDouble(i), frequency);

        for (int i = 0; i < irSignal.getRepeatLength(); i++)
            data[index++] = pulsesMicroSeconds(irSignal.getRepeatDouble(i), frequency);

        return data;
    }

    /**
     * Computes the ("long", raw) CCF string, if possible.
     * Since a CCF does not have an ending sequence,
     * a nonempty ending sequence will be ignored.
     * @param irSignal
     * @return CCF string
     */
    public static String toString(IrSignal irSignal) {
        try {
            return toString(toArray(irSignal));
        } catch (InvalidArgumentException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    /**
     * Formats a CCF as string.
     * @param array CCF in form of an integer array.
     * @return CCF string.
     */
    public static String toString(int[] array) {
        StringJoiner s = new StringJoiner(" ");
        for (int n : array)
            s.add(String.format(HEX_STRING_FORMAT, n));
        return s.toString();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            IrSignal irSignal = args.length == 1 ? parse(args[0]) : parse(args);
            System.out.println(irSignal);
        } catch (InvalidArgumentException | NonProntoFormatException ex) {
            Logger.getLogger(Pronto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected Pronto() {
    }

    public static class NonProntoFormatException extends IrCoreException {

        public NonProntoFormatException(String string, int pos) {
            super("Position " + pos + ": \"" + string + "\" is not a four digit hexadecimal string.");
        }

        private NonProntoFormatException(NumberFormatException ex) {
            super(ex);
        }
    }
}
