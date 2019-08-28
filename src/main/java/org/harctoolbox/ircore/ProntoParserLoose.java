/*
Copyright (C) 2019 Bengt Martensson.

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

public class ProntoParserLoose extends ProntoParser {

    //private final static Logger logger = Logger.getLogger(ProntoParserLoose.class.getName());

    public static IrSignal parse(String str) throws InvalidArgumentException {
        ProntoParserLoose instance = new ProntoParserLoose(str);
        return instance.toIrSignal();
    }

    public ProntoParserLoose(String source) {
        super(source);
    }

    public ProntoParserLoose(Iterable<? extends CharSequence> args) {
        super(args);
    }

    /**
     * Tries to interpret the string argument as Pronto.
     *
     * @param fallbackFrequency Modulation frequency to use, if it cannot be
     * inferred from the first parameter.
     * @param dummyGap
     * @return IrSignal, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException If the signal looks like a Pronto, but is not correctly parseable.
     */
    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        return toIrSignal(fallbackFrequency, dummyGap, true);
    }

    @Override
    public String getName() {
        return "Pronto Hex (Loose)";
    }
}
