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

import java.util.List;

public class MultilineIrSignalParser extends AbstractIrParser implements IrSignalParser {

    //private final static Logger logger = Logger.getLogger(AbstractIrParser.class.getName());

    private static String[] splitLines(String str) {
        return str.split("[\n\r]+");
    }

    /**
     * Main constructor
     * @param source string to be paraed
     */
    public MultilineIrSignalParser(String source) {
        super(fixIrRemoteSilliness(source));
    }

    /**
     * Equivalent to AbstractIrParser(String.join(" ", args));
     * @param args Will be concatenated, with space in between, then parsed.
     */
    public MultilineIrSignalParser(Iterable<? extends CharSequence> args) {
        this(String.join(" ", args));
    }

    /**
     *
     * @param dummyGap
     * @return
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Override
    public List<IrSequence> toList(Double dummyGap) throws OddSequenceLengthException {
        return toList(splitLines(getSource()), dummyGap);
    }

    @Override
    public ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency, Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException {
        try {
            Double frequency = fallbackFrequency;
            String s = getSource().replace(",", " ").trim();
            if (s.startsWith("f=")) {
                int pos = s.indexOf(' ', 3);
                frequency = Double.parseDouble(s.substring(2, pos));
                s = s.substring(pos + 1).trim();
            } else if (s.startsWith("Freq=")) {
                int pos = s.indexOf('H', 6);
                frequency = Double.parseDouble(s.substring(5, pos));
                s = s.substring(pos + 2).trim();
            }
            IrSequence irSequence = new IrSequence(s, dummyGap);
            return new ModulatedIrSequence(irSequence, frequency);
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentException(ex);
        }
    }

    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws OddSequenceLengthException {
        try {
            return mkIrSignal(splitLines(getSource()), fallbackFrequency, dummyGap);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "MultilineIrSignal";
    }
}
