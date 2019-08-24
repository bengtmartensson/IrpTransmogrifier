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

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProntoParser extends AbstractIrParser implements IrSignalParser {

    private final static Logger logger = Logger.getLogger(ProntoParser.class.getName());

    public static IrSignal parse(String str) throws InvalidArgumentException {
        ProntoParser instance = new ProntoParser(str);
        return instance.toIrSignal();
    }

    public static IrSignal parseDiscardingExcess(String str) throws InvalidArgumentException {
        ProntoParser instance = new ProntoParser(str);
        return instance.toIrSignal(null, null, true);
    }

    public ProntoParser(String source) {
        super(source);
    }

    public ProntoParser(Iterable<? extends CharSequence> args) {
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
        return toIrSignal(fallbackFrequency, dummyGap, false);
    }

    private IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap, boolean discardExcess) throws InvalidArgumentException {
        try {
            IrSignal irSignal = Pronto.parse(getSource(), discardExcess);
            // If Pronto.NonProntoFormatException is not thrown, the signal is probably
            // an erroneous Pronto wannabe, do not catch other exceptions than Pronto.NonProntoFormatException
            if (fallbackFrequency != null)
                logger.log(Level.FINE, "Explicit frequency with a Pronto type signal meaningless, thus ignored.");
            return irSignal;
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, give up
            logger.log(Level.FINER, "Tried as Pronto, gave up ({0})", ex.getMessage());
            return null;
        }
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
    public IrSignal toIrSignalDiscardingExcess(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        return toIrSignal(fallbackFrequency, dummyGap, true);
    }

    @Override
    public String getName() {
        return "Pronto Hex";
    }
}
