/*
Copyright (C) 2012,2013,2014,2016,2018 Bengt Martensson.

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

import org.harctoolbox.analyze.Cleaner;
import org.harctoolbox.analyze.RepeatFinder;

public class InterpretString {

    /**
     * Tries to interpret the string argument as one of our known formats, and return an IrSignal.
     * If the string starts with "[", interpret it as raw data, already split in intro-,
     * repeat-, and ending sequences.
     * If not try to interpret as Pronto Hex.
     * If not successful, it is assumed to be in raw format.
     * If it contains more than on line, assume
     * that the caller has split it into intro, repeat, and ending sequences already.
     * Otherwise invoke RepeatFinder to split it into constituents.
     *
     * @param str String to be interpreted.
     * @param frequency Modulation frequency to use, if it cannot be inferred from the first parameter.
     * @param invokeRepeatFinder
     * @param invokeCleaner
     * @param absouluteTolerance
     * @param relativeTolerance
     * @return IrSignal, or null on failure.
     */
    public static IrSignal interpretString(String str, Double frequency, boolean invokeRepeatFinder, boolean invokeCleaner,
            double absouluteTolerance, double relativeTolerance) {
        IrSignal irSignal = parseBracketedString(str, frequency);
        if (irSignal != null)
            return irSignal;

        try {
            irSignal = Pronto.parse(str);
            return irSignal;
        } catch (Pronto.NonProntoFormatException | InvalidArgumentException ex) {
        }

        irSignal = interpretRawString(str, frequency, invokeRepeatFinder, invokeCleaner, absouluteTolerance, relativeTolerance);
        return irSignal;
        //} catch (NumberFormatException ex) {
        //    throw new InvalidArgumentException("Could not interpret string " + str + " (" + ex.getMessage() + ")");
        //}
    }

    public static IrSignal interpretString(String str, Double frequency, boolean invokeRepeatFinder, boolean invokeCleaner) {
        return interpretString(str, frequency, invokeRepeatFinder, invokeCleaner,
                IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE, IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE);
    }

    public static IrSignal interpretString(String str, Double frequency) {
        return interpretString(str, frequency, true, true);
    }

    public static IrSignal interpretString(String str) {
        return interpretString(str, null);
    }

    private static IrSignal parseBracketedString(String str, Double frequency) {
        if (!str.trim().startsWith("["))
            return null;

        String[] codes = str.trim().substring(1).split("[\\[\\]]+");
        try {
            return new IrSignal(codes[0],
                    codes.length > 1 ? codes[1] : null,
                    codes.length > 2 ? codes[2] : null, frequency, null);
        } catch (OddSequenceLengthException ex) {
            return null;
        }
    }

    // IRremote writes spaces after + and -, sigh...
    private static String fixIrRemoteSilliness(String str) {
        return str.replaceAll("\\+\\s+", "+").replaceAll("-\\s+", "-");
    }

    private static IrSignal interpretRawString(String str, double frequency, boolean invokeRepeatFinder, boolean invokeCleaner,
            double absoluteTolerance, double relativeTolerance) {
        try {

            String fixedString = fixIrRemoteSilliness(str);
            String[] codes = fixedString.split("[\n\r]+");
            if (codes.length > 1 && codes.length <= 3) {
                // already decomposed in sequences?
                return new IrSignal(codes[0], codes[1], codes.length > 2 ? codes[2] : null, frequency, null);
            }

            IrSequence irSequence = new IrSequence(fixedString, frequency);
            ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(irSequence, frequency, null);
            return interpretIrSequence(modulatedIrSequence, invokeRepeatFinder, invokeCleaner, absoluteTolerance, relativeTolerance);
        } catch (NumberFormatException | OddSequenceLengthException ex) {
            //throw new InvalidArgumentException("Could not interpret string " + str + " (" + ex.getMessage() + ")");
            return null;
        }
    }

    /**
     * If invokeRepeatFinder is true, tries to identify intro, repeat, and ending applying a RepeatFinder.
     * If not, the sequence is used as intro on the returned signal.
     * In this case, if invokeCleaner is true, an analyzer is first used to clean the signal.
     * @param modulatedIrSequence
     * @param invokeRepeatFinder If the repeat finder is invoked. This also uses the analyzer.
     * @param absoluteTolerance
     * @param relativeTolerance
     * @param invokeCleaner If the analyzer is invoked for cleaning the signals.
     * @return IrSignal signal constructed according to rules above.
     */
    public static IrSignal interpretIrSequence(ModulatedIrSequence modulatedIrSequence, boolean invokeRepeatFinder, boolean invokeCleaner,
            double absoluteTolerance, double relativeTolerance) {
        ModulatedIrSequence cleaned = invokeCleaner ? Cleaner.clean(modulatedIrSequence, absoluteTolerance, relativeTolerance) : modulatedIrSequence;
        IrSignal irSignal = invokeRepeatFinder ? RepeatFinder.findRepeat(cleaned, absoluteTolerance, relativeTolerance) : new IrSignal(cleaned);
        return irSignal;
    }

    private InterpretString() {
    }
}
