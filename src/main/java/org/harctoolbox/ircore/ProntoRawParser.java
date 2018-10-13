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

public class ProntoRawParser extends RawParser {

    private final static Logger logger = Logger.getLogger(ProntoRawParser.class.getName());

    public ProntoRawParser(String source) {
        super(source);
    }

    public ProntoRawParser(Iterable<? extends CharSequence> args) {
        super(args);
    }

    public final IrSignal toIrSignalAsPronto() throws Pronto.NonProntoFormatException, InvalidArgumentException {
        return Pronto.parse(getSource());
    }

    @Override
    public ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        try {
            IrSignal irSignal = toIrSignalAsPronto();
            if (fallbackFrequency != null)
                logger.log(Level.SEVERE, "Explicit frequency with a Pronto type signal meaningless, thus ignored.");
            return irSignal.toModulatedIrSequence();
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, try it as raw
            return super.toModulatedIrSequence(fallbackFrequency, dummyGap);
        }
    }

    /**
     * Tries to interpret the string argument as one of our known formats, and return an IrSignal.
     * First tries to interpret as Pronto.
     * If this fails, falls back to RawParser.toIrSignal().
     *
     * @param fallbackFrequency Modulation frequency to use, if it cannot be inferred from the first parameter.
     * @param dummyGap
     * @return IrSignal, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        try {
            IrSignal irSignal = toIrSignalAsPronto();
            // If Pronto.NonProntoFormatException is not thrown, the signal is probably
            // an erroneous Pronto wannabe, do not catch other exceptions than Pronto.NonProntoFormatException
            if (fallbackFrequency != null)
                logger.log(Level.SEVERE, "Explicit frequency with a Pronto type signal meaningless, thus ignored.");
            return irSignal;
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, try it as raw
            return super.toIrSignal(fallbackFrequency, dummyGap);
        }
    }
}
