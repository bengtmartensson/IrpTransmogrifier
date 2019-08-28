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

public class MultiParser extends AbstractIrParser {

    public static List<IrSignalParser> ircoreParsersList(String source) {
        List<IrSignalParser> parsersList = new ArrayList<>(4);
        parsersList.add(new ProntoParserLoose(source));
        parsersList.add(new BracketedIrSignalParser(source));
        parsersList.add(new MultilineIrSignalParser(source));
        return parsersList;
    }

    public static MultiParser newIrCoreParser(String source) {
        return new MultiParser(ircoreParsersList(source), source);
    }

    public static MultiParser newIrCoreParser(List<? extends CharSequence> args) {
        return newIrCoreParser(String.join(" ", args));
    }

    private final List<IrSignalParser> parsers;

    public MultiParser(List<IrSignalParser> parsers, String source) {
        super(source);
        this.parsers = parsers;
    }

    public MultiParser(List<IrSignalParser> parsers, Iterable<? extends CharSequence> args) {
        this(parsers, String.join(" ", args));
    }

    public MultiParser(Iterable<? extends CharSequence> args) {
        this(String.join(" ", args));
    }

    public MultiParser(String source) {
        this(ircoreParsersList(source), source);
    }

    public void addParser(IrSignalParser newParser) {
        parsers.add(0, newParser);
    }

    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        for (IrSignalParser parser : parsers) {
            IrSignal irSignal = parser.toIrSignal(fallbackFrequency, dummyGap);
            if (irSignal != null)
                return irSignal;
        }

        return null;
    }

    @Override
    public IrSequence toIrSequence(Double dummyGap) throws InvalidArgumentException {
         for (IrSignalParser parser : parsers) {
            IrSequence irSequence = parser.toIrSequence(dummyGap);
            if (irSequence != null)
                return irSequence;
        }

        return null;
    }

    @Override
    public ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
         for (IrSignalParser parser : parsers) {
            ModulatedIrSequence modulatedirSequence = parser.toModulatedIrSequence(fallbackFrequency, dummyGap);
            if (modulatedirSequence != null)
                return modulatedirSequence;
        }

        return null;
    }

    /**
     *
     * @param dummyGap
     * @return
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Override
    public List<IrSequence> toList(Double dummyGap) throws OddSequenceLengthException, InvalidArgumentException {
        for (IrSignalParser parser : parsers) {
            List<IrSequence> list = parser.toList(dummyGap);
            if (list != null)
                return list;
        }

        return null;
    }
}
