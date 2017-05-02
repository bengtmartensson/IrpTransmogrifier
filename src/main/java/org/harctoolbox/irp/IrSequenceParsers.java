/*
Copyright (C) 2011,2012,2014,2016, 2017 Bengt Martensson.

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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;

public class IrSequenceParsers {

    private final static Logger logger = Logger.getLogger(IrSequence.class.getName());

//    public static IrSequence mkIrSequence(String str, boolean fixOddSequences) throws OddSequenceLengthException {
//        return fixOddSequences ? new IrSequence(str, IrSequence.DUMMYGAPDURATION) : new IrSequence(str);
//    }

    /**
     *
     * @param str
     * @return
     * @throws OddSequenceLengthException
     */
    public static List<IrSequence> parseIntoSeveral(String str) throws OddSequenceLengthException {
        String[] parts = str.trim().startsWith("[")
                ? str.replace("[", "").split("\\]")
                : new String[]{str};

        ArrayList<IrSequence> result = new ArrayList<>(parts.length);
        for (String s : parts)
            result.add(new IrSequence(s));

        return result;
    }

    public static IrSequence parseProntoOrRaw(List<String> line) {
        try {
            IrSignal irSignal = new IrSignal(line.get(0));
            return irSignal.toModulatedIrSequence(1);
        } catch (InvalidArgumentException | Pronto.NonProntoFormatException ex) {
            IrSequence intro = new IrSequence(line.get(0), IrSequence.DUMMYGAPDURATION);
            IrSequence repeat = line.size() > 1 ? new IrSequence(line.get(1), IrSequence.DUMMYGAPDURATION) : new IrSequence();
            IrSequence ending = line.size() > 2 ? new IrSequence(line.get(2), IrSequence.DUMMYGAPDURATION) : new IrSequence();
            return IrSequence.concatenate(intro, repeat, ending);
        }
    }

    public static IrSequence parseRaw(String line, boolean fixOddSequences) throws OddSequenceLengthException {
        return fixOddSequences ? new IrSequence(line, IrSequence.DUMMYGAPDURATION) : new IrSequence(line);
    }

    private IrSequenceParsers() {
    }
}
