/*
Copyright (C) 2018 Bengt Martensson.

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
import java.util.List;

public abstract class AbstractIrParser {

    //private final static Logger logger = Logger.getLogger(AbstractIrParser.class.getName());

    // IRremote writes spaces after + and -, sigh...
    protected static String fixIrRemoteSilliness(String str) {
        return str.replaceAll("\\+\\s+", "+").replaceAll("-\\s+", "-");
    }

    protected static IrSignal mkIrSignal(List<IrSequence> list, Double frequency) throws OddSequenceLengthException {
          return (list.size() > 0 && list.size() <= 3)
                ? new IrSignal(list.get(0), list.size() > 1 ? list.get(1) : null, list.size() > 2 ? list.get(2) : null, frequency, null)
                : null;
    }

    protected static IrSignal mkIrSignal(String[] codes, Double fallbackFrequency, Double dummyGap) throws OddSequenceLengthException {
        if (codes.length == 0 || codes.length > 3)
            return null;

        IrSequence intro = new IrSequence(codes[0], dummyGap);
        IrSequence repeat = codes.length > 1 ? new IrSequence(codes[1], dummyGap) : null;
        IrSequence ending = codes.length > 2 ? new IrSequence(codes[2], dummyGap) : null;

        return new IrSignal(intro, repeat, ending, fallbackFrequency);
    }

    private final String source;

    /**
     * Main constructor
     * @param source string to be paraed
     */
    public AbstractIrParser(String source) {
        this.source = source.trim();
    }

    /**
     * Equivalent to RawParser(String.join(" ", args));
     * @param args Will be concatenated, with space in between, then parsed.
     */
    public AbstractIrParser(Iterable<? extends CharSequence> args) {
        this(String.join(" ", args));
    }

    @Override
    public final String toString() {
        return source;
    }

    /**
     * Equivalent to toIrSequence().chop(threshold);
     * @param threshold
     * @param dummyGap
     * @return
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public List<IrSequence> toListChop(double threshold, Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException {
        return toIrSequence(dummyGap).chop(threshold);
    }

    public final List<IrSequence> toListChop(double threshold) throws OddSequenceLengthException, InvalidArgumentException {
        return toListChop(threshold, null);
    }

    /**
     *
     * @param dummyGap
     * @return
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public List<IrSequence> toList(Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException {
        return null;
    }

    /**
     *
     * @param parts
     * @param dummyGap
     * @return
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    protected final List<IrSequence> toList(String[] parts, Double dummyGap) throws OddSequenceLengthException {
        List<IrSequence> result = new ArrayList<>(parts.length);
        for (String s : parts)
            try {
                result.add(new IrSequence(s, dummyGap));
            } catch (NumberFormatException ex) {
            }

        return result;
    }

    public final List<IrSequence> toList() throws OddSequenceLengthException, InvalidArgumentException {
        return toList(null);
    }

    public ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        try {
            IrSignal irSignal = toIrSignal(fallbackFrequency, dummyGap);
            if (irSignal != null)
               return irSignal.toModulatedIrSequence();
        } catch (InvalidArgumentException | NumberFormatException ex) {
        }
        return null;
    }

    public final ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency) throws OddSequenceLengthException, InvalidArgumentException {
        return toModulatedIrSequence(fallbackFrequency, null);
    }

    public final ModulatedIrSequence toModulatedIrSequence() throws OddSequenceLengthException, InvalidArgumentException {
        return toModulatedIrSequence(null);
    }

    /**
     *
     * @return
     */
    protected final String getSource() {
        return source;
    }

    public final IrSequence toIrSequence() throws OddSequenceLengthException, InvalidArgumentException {
        return toIrSequence(null);
    }

    public IrSequence toIrSequence(Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException {
        return new IrSequence(source, dummyGap);
    }

    public final IrSignal toIrSignal() throws OddSequenceLengthException, InvalidArgumentException {
        return toIrSignal(null);
    }

    /**
     * Tries to interpret the string argument as one of our known formats, and return an IrSignal.
     * If the string starts with "[", interpret it as raw data, already split in intro-,
     * repeat-, and ending sequences..
     * If it contains more than on line, assume
     * that the caller has split it into intro, repeat, and ending sequences already.
     * Im particular, if no particular structure is found, returns null.
     *
     * @param fallbackFrequency Modulation frequency to use, if it cannot be inferred from the source string. For no information, use null.
     * @return IrSignal, or null on failure.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public final IrSignal toIrSignal(Double fallbackFrequency) throws OddSequenceLengthException, InvalidArgumentException, NumberFormatException {
        return toIrSignal(fallbackFrequency, null);
    }

    public abstract IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException, NumberFormatException;

    public IrSignal toIrSignalChop(Double fallbackFrequency, double threshold) throws OddSequenceLengthException, InvalidArgumentException {
        List<IrSequence> list = toListChop(threshold);
        return mkIrSignal(list, fallbackFrequency);
    }
}
