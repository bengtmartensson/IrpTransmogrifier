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

public class MultiParser extends RawParser {

    private final Iterable<? extends IrSignalParser> parsers;

    public MultiParser(Iterable<? extends IrSignalParser> parsers, String source) {
        super(source);
        this.parsers = parsers;
    }

    public MultiParser(Iterable<? extends IrSignalParser> parsers, Iterable<? extends CharSequence> args) {
        this(parsers, String.join(" ", args));
    }

    public MultiParser(Iterable<? extends CharSequence> args) {
        this(String.join(" ", args));
    }

    public MultiParser(String source) {
        this(new ArrayList<IrSignalParser>(0), source);
    }

    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        for (IrSignalParser parser : parsers) {
            IrSignal irSignal = parser.toIrSignal(fallbackFrequency, dummyGap);
            if (irSignal != null)
                return irSignal;
        }

        return super.toIrSignal(fallbackFrequency, dummyGap);
    }
}
