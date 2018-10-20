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

public class BracketedIrSignalParser extends AbstractIrParser implements IrSignalParser {

    private static String[] splitBracketed(String str) {
        return str.startsWith("[")
                ? str.replace("[", "").split("\\]")
                : new String[]{str};
    }

    public BracketedIrSignalParser(String source) {
        super(fixIrRemoteSilliness(source));
    }

    public BracketedIrSignalParser(Iterable<? extends CharSequence> args) {
        this(String.join(" ", args));
    }

    @Override
    public IrSignal toIrSignal(Double frequency, Double dummyGap) throws OddSequenceLengthException {
        if (!getSource().startsWith("["))
            return null;

        return mkIrSignal(splitBracketed(getSource()), frequency, dummyGap);
    }

    @Override
    public List<IrSequence> toList(Double dummyGap) throws OddSequenceLengthException {
        return toList(splitBracketed(getSource()), dummyGap);
    }

    @Override
    public String getName() {
        return "BracketedIrSignal";
    }
}
