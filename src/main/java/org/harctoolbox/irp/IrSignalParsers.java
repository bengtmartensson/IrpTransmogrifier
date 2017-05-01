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
package org.harctoolbox.irp;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;

public class IrSignalParsers {

    private final static Logger logger = Logger.getLogger(IrSequence.class.getName());

//    public static IrSequence mkIrSequence(String str, boolean fixOddSequences) throws OddSequenceLengthException {
//        return fixOddSequences ? new IrSequence(str, IrSequence.DUMMYGAPDURATION) : new IrSequence(str);
//    }

//    /**
//     *
//     * @param str
//     * @return
//     * @throws OddSequenceLengthException
//     */
//    public static List<IrSequence> parseMulti(String str) throws OddSequenceLengthException {
//        String[] parts = str.trim().startsWith("[")
//                ? str.replace("[", "").split("\\]")
//                : new String[]{str};
//
//        ArrayList<IrSequence> result = new ArrayList<>(parts.length);
//        for (String s : parts)
//            result.add(new IrSequence(s));
//
//        return result;
//    }
//
//    public static IrSequence parseProntoOrRaw(List<String> line) {
//        try {
//            IrSignal irSignal = new IrSignal(line.get(0));
//            return irSignal.toModulatedIrSequence(1);
//        } catch (InvalidArgumentException | Pronto.NonProntoFormatException ex) {
//            IrSequence intro = new IrSequence(line.get(0), IrSequence.DUMMYGAPDURATION);
//            IrSequence repeat = new IrSequence(line.get(1), IrSequence.DUMMYGAPDURATION);
//            IrSequence ending = line.size() > 2 ? new IrSequence(line.get(2), IrSequence.DUMMYGAPDURATION) : new IrSequence();
//            return intro.concatenate(repeat, ending);
//        }
//    }

//    public static IrSignal parseRawWithDefaultFrequency(String intro, String repeat, String ending, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
//        if (frequency == null)
//            logger.log(Level.WARNING, String.format(Locale.US, "Frequency missing, assuming default frequency = %d Hz",
//                    (int) ModulatedIrSequence.DEFAULT_FREQUENCY));
//        IrSequence introSeq = mkIrSequence(intro, fixOddSequences);
//        IrSequence repeatSeq = mkIrSequence(repeat, fixOddSequences);
//        IrSequence endingSeq = mkIrSequence(ending, fixOddSequences);
//
//        return new IrSignal(introSeq, repeatSeq, endingSeq, frequency != null ? frequency : ModulatedIrSequence.DEFAULT_FREQUENCY);
//    }

//    public static IrSignal parseRawWithDefaultFrequency(List<String> args, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
//        if (frequency == null)
//            logger.log(Level.WARNING, String.format(Locale.US, "Frequency missing, assuming default frequency = %d Hz",
//                    (int) ModulatedIrSequence.DEFAULT_FREQUENCY));
//        return parseRaw(args, frequency != null ? frequency : ModulatedIrSequence.DEFAULT_FREQUENCY, fixOddSequences);
//    }

    public static IrSignal parseRaw(String string, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        if (frequency == null)
            logger.log(Level.WARNING, String.format(Locale.US, "Frequency missing, assuming default frequency = %d Hz",
                    (int) ModulatedIrSequence.DEFAULT_FREQUENCY));
        return parseRaw(string, frequency != null ? frequency : ModulatedIrSequence.DEFAULT_FREQUENCY, fixOddSequences);
    }

    public static IrSignal parseRaw(List<String> args, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        return parseRaw(String.join(" ", args).trim(), frequency, fixOddSequences);
    }

    public static IrSignal parseRaw(String str, double frequency, boolean fixOddSequences) throws InvalidArgumentException {
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

    public static IrSignal parseProntoOrRaw(List<String> args, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        try {
            IrSignal irSignal = Pronto.parse(args);
            if (frequency != null)
                throw new InvalidArgumentException("Must not use explicit frequency with a Pronto type signal.");
            return irSignal;
            // Do not catch InvalidArgumentException here, if that is thrown
            // likely erroneous wanna-be Pronto
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, try it as raw
            return parseRaw(args, frequency, fixOddSequences);
        }
    }

    static IrSignal parseProntoOrRawFromLines(List<String> line, Double frequency, boolean fixOddSequences) throws InvalidArgumentException {
        try {
            IrSignal irSignal = Pronto.parse(line.get(0));
            if (frequency != null)
                throw new InvalidArgumentException("Must not use explicit frequency with a Pronto type signal.");
            return irSignal;
            // Do not catch InvalidArgumentException here, if that is thrown
            // likely erroneous wanna-be Pronto
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, try it as raw
            return parseRawFromLines(line, frequency, fixOddSequences);
        }
    }

    private static IrSignal parseRawFromLines(List<String> line, Double frequency, boolean fixOddSequences) throws OddSequenceLengthException {
        IrSequence intro = IrSequenceParsers.parseRaw(line.get(0), fixOddSequences);
        IrSequence repeat = line.size() > 1 ? IrSequenceParsers.parseRaw(line.get(1), fixOddSequences) : null;
        IrSequence ending = line.size() > 2 ? IrSequenceParsers.parseRaw(line.get(2), fixOddSequences) : null;
        IrSignal irSignal = new IrSignal(intro, repeat, ending, frequency);
        return irSignal;
    }

    private IrSignalParsers() {
    }
}
