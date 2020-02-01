/*
Copyright (C) 2020 Bengt Martensson.

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

import org.harctoolbox.ircore.AbstractIrParser;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignalParser;
import org.harctoolbox.ircore.Pronto;

public class ShortProntoParser extends AbstractIrParser implements IrSignalParser {

    //private final static Logger logger = Logger.getLogger(ProntoParserLoose.class.getName());

    public static IrSignal parse(String str) throws InvalidArgumentException {
        ShortProntoParser instance = new ShortProntoParser(str);
        return instance.toIrSignal();
    }

    public ShortProntoParser(String source) {
        super(source);
    }

    public ShortProntoParser(Iterable<? extends CharSequence> args) {
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
        try {
            return ShortPronto.parse(getSource());
        } catch (Pronto.NonProntoFormatException ex) {
            throw new InvalidArgumentException(ex);
        }
    }

    @Override
    public String getName() {
        return "Short Pronto Hex";
    }
}
