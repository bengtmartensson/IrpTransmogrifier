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

    /**
     *
     * @param str
     * @param dummyGap
     * @return
     * @throws OddSequenceLengthException
     */
    public static List<IrSequence> parseIntoSeveral(String str, Double dummyGap) throws OddSequenceLengthException {
        String[] parts = chop(str);
        ArrayList<IrSequence> result = new ArrayList<>(parts.length);
        for (String s : parts)
            result.add(parseRaw(s, dummyGap));

        return result;
    }

    private static String[] chop(String str) {
        return str.trim().startsWith("[")
                ? str.replace("[", "").split("\\]")
                : new String[]{str};
    }

    public static IrSequence parseProntoOrRaw(List<String> line, Double dummyGap) throws OddSequenceLengthException {
        try {
            IrSignal irSignal = Pronto.parse(line.get(0));
            return irSignal.toModulatedIrSequence();
        } catch (InvalidArgumentException | Pronto.NonProntoFormatException ex) {
            IrSequence intro = parseRaw(line.get(0), dummyGap);
            IrSequence repeat = line.size() > 1 ? parseRaw(line.get(1), dummyGap) : new IrSequence();
            IrSequence ending = line.size() > 2 ? parseRaw(line.get(2), dummyGap) : new IrSequence();
            return IrSequence.concatenate(intro, repeat, ending);
        }
    }

    public static IrSequence parseRaw(String line, Double dummyGap) throws OddSequenceLengthException {
        return dummyGap != null ? new IrSequence(line, dummyGap) : new IrSequence(line);
    }

    private IrSequenceParsers() {
    }
}
