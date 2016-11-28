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
package org.harctoolbox.ircore;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class, containing only static functions, allows for the creation of integer arrays
 * and strings containing Pronto (CCF) form of the signal. It cannot be instantiated,
 * since there are no "Pronto"s, it is just IrSignals in "another coordinates".
 */
public class Pronto {
    /** Number of characters in the hexadecimal digits of Pronto strings. */
    public final static int charsInDigit = 4;

    /** Constant used for computing the frequency code from the frequency */
    public final static double prontoConstant = 0.241246;
    private final static double dummyFrequency = 100000.0/prontoConstant;

    /** Format code used to format integers in the Pronto Hex. */
    public final static String formattingCode = "%04X";

    protected final static int learnedCode = 0x0000;
    protected final static int learnedZeroFrequencyCode = 0x0100;

    private static final Logger logger = Logger.getLogger(Pronto.class.getName());

    /**
     * Formats an integer like seen in CCF strings, in printf-ish, using "%04X".
     * @param n Integer to be formatted.
     * @return Formatted string
     */
    public static String formatInteger(int n) {
        return String.format(formattingCode, n);
    }

    /**
     * Returns frequency code from frequency in Hz (the second number in the CCF).
     *
     * @param f Frequency in Hz.
     * @return code for the frequency.
     */
    public static int getProntoCode(double f) {
        return (int) Math.round(1000000.0 / ((f>0 ? f : dummyFrequency) * prontoConstant));
    }

    /**
     * Computes the carrier frequency in Hz.
     * @param code Pronto frequency code
     * @return Frequency in Hz.
     */
    public static double getFrequency(int code) {
        return code == 0
                ? ModulatedIrSequence.unknownFrequency // Invalid value
                : 1000000.0 / (code * prontoConstant);
    }

    /**
     * Computes pulse time in microseconds.
     * @param code Pronto frequency code.
     * @return Duration of one pulse of the carrier in microseconds.
     */
    public static double getPulseTime(int code) { // in microseconds
        return code == 0
                ? ModulatedIrSequence.unknownPulseTime // Invalid value
                : code * prontoConstant;
    }

    // TODO: fix for f=0,
    /**
     * Computes number of cycles of the carrier the first argument will require.
     *
     * @param us duration in microseconds
     * @param frequency
     * @return number of pulses
     */
    public static int pulses(double us, double frequency) {
        return Math.min((int)Math.round(Math.abs(us) * (frequency > 0 ? frequency : dummyFrequency)/1000000.0), 0xFFFF);
    }

    private static double[] usArray(int frequencyCode, int[] ccfArray, int beg, int end) {
        double pulseTime = getPulseTime(frequencyCode);
        double[] data = new double[end - beg];
        for (int i = beg; i < end; i++)
            data[i-beg] = pulseTime*ccfArray[i];

        return data;
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF signal.
     * @param ccf CCF signal
     * @return  IrSignal
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(int[] ccf) throws OddSequenceLenghtException, InvalidArgumentException {
        if (ccf.length < 4)
            throw new InvalidArgumentException("CCF is invalid since less than 4 numbers long.");
        if (ccf.length % 2 != 0)
            throw new OddSequenceLenghtException("CCF is invalid since it has an odd number ("
                    + ccf.length + ") of durations.");
        int index = 0;
        int type = ccf[index++];
        int frequencyCode = ccf[index++];
        int introLength = ccf[index++];
        int repeatLength = ccf[index++];
        if (index + 2*(introLength+repeatLength) != ccf.length)
            throw new InvalidArgumentException("Inconsistent length in CCF (claimed "
                    + (introLength + repeatLength) + " pairs, was " + (ccf.length - 4)/2 + " pairs).");
        IrSignal irSignal = null;

        switch (type) {
            case learnedCode: // 0x0000
            case learnedZeroFrequencyCode: // 0x0100
                double[] intro = usArray(frequencyCode, ccf, index, index + 2*introLength);
                double[] repeat = usArray(frequencyCode, ccf, index + 2*introLength, ccf.length);
                IrSequence introSequence = new IrSequence(intro);
                IrSequence repeatSequence = new IrSequence(repeat);
                irSignal = new IrSignal(introSequence, repeatSequence, null,
                        type == learnedCode ? getFrequency(frequencyCode) : 0,
                        ModulatedIrSequence.unknownDutyCycle);
                break;

            default:
                throw new InvalidArgumentException("CCF type 0x" + Integer.toHexString(type) + " not supported");
        }
        return irSignal;
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param ccfString CCF signal
     * @return IrSignal
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(String ccfString) throws InvalidArgumentException {
        int[] ccf = parseAsInts(ccfString);
        return parse(ccf);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param array Strings representing hexadecimal numbers
     * @param begin Starting index
     * @return IrSignal
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(String[] array, int begin) throws InvalidArgumentException {
        int[] ccf;
        try {
            ccf = parseAsInts(array, begin);
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentException("Non-parseable CCF strings");
        }
        if (ccf == null)
            throw new InvalidArgumentException("Invalid CCF strings");

        return parse(ccf);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param list Strings representing hexadecimal numbers
     * @return IrSignal
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(List<String> list) throws InvalidArgumentException {
        int[] ccf;
        try {
            ccf = parseAsInts(list);
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentException("Non-parseable CCF strings");
        }
        if (ccf == null)
            throw new InvalidArgumentException("Invalid CCF strings");

        return parse(ccf);
    }

    /**
     * Tries to parse the string as argument.
     * Can be used to test "Proto-ness" of an unknown string.
     *
     * @param ccfString Input string, to be parsed/tested.
     * @return Integer array of numbers if successful, null if unsuccessful.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    static int[] parseAsInts(String ccfString) throws InvalidArgumentException {
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
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    static int[] parseAsInts(String[] array, int begin) throws InvalidArgumentException {
        int[] ccf = new int[array.length];

        for (int i = begin; i < array.length; i++) {
            if (array[i].length() != charsInDigit)
                throw new InvalidArgumentException("Non-pronto format of " + array[i]);
            try {
                ccf[i] = Integer.parseInt(array[i], 16);
            } catch (NumberFormatException ex) {
                throw new InvalidArgumentException(ex);
            }
        }
        return ccf;
    }

    protected static int[] parseAsInts(List<String> list) throws InvalidArgumentException {
        String[] x = list.toArray(new String[list.size()]);
        return parseAsInts(x, 0);
    }

    /**
     * CCF array of complete signal, i.e. the CCF string before formatting, including the header.
     * @param irSignal
     * @return CCF array
     * @throws OddSequenceLenghtException
     */
    public static int[] toArray(IrSignal irSignal) throws OddSequenceLenghtException {
        if (irSignal.getIntroLength() % 2 != 0 || irSignal.getRepeatLength() % 2 != 0)
            // Probably forgot normalize() if I get here.
            throw new OddSequenceLenghtException("IR Sequences must be of even length.");
        if (irSignal.getEndingLength() != 0)
            logger.log(Level.WARNING,
                    "When computing the Pronto representation, a (non-empty) ending sequence was ignored");

        int[] data = new int[4 + irSignal.getIntroLength() + irSignal.getRepeatLength()];
        int index = 0;
        data[index++] = irSignal.getFrequency() > 0 ? learnedCode : learnedZeroFrequencyCode;
        data[index++] = getProntoCode(irSignal.getFrequency());
        data[index++] = irSignal.getIntroLength()/2;
        data[index++] = irSignal.getRepeatLength()/2;
        for (int i = 0; i < irSignal.getIntroLength(); i++)
            data[index++] = pulses(irSignal.getIntroDouble(i), irSignal.getFrequency());

        for (int i = 0; i < irSignal.getRepeatLength(); i++)
            data[index++] = pulses(irSignal.getRepeatDouble(i), irSignal.getFrequency());

        return data;
    }

    /**
     * Computes the ("long", raw) CCF string
     * @param irSignal
     * @return CCF string
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     */
    public static String toPrintString(IrSignal irSignal) throws OddSequenceLenghtException {
        return toPrintString(toArray(irSignal));
    }

    /**
     * Formats a CCF as string.
     * @param array CCF in form of an integer array.
     * @return CCF string.
     */
    public static String toPrintString(int[] array) {
        StringBuilder s = new StringBuilder(array.length*5);
        for (int i = 0; i < array.length; i++)
            s.append(String.format((i > 0 ? " " : "") + formattingCode, array[i]));
        return s.toString();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            IrSignal irSignal;

            if (args.length == 1) {
                irSignal = parse(args[0]);
            } else {
                int[] ccf = new int[args.length];
                for (int i = 0; i < args.length; i++) {
                    ccf[i] = Integer.parseInt(args[i], 16);
                }
                irSignal = parse(ccf);
            }
            System.out.println(irSignal);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(Pronto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected Pronto() {
    }
}
