/*
Copyright (C) 2016 Bengt Martensson.

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

package org.harctoolbox.analyze;

import java.util.List;
import org.harctoolbox.ircore.IrSequence;

/**
 *
 */
public class Analyzer {

    private final Cleaner cleaner;
    private int timebase;
    private int[] normedTimings;

    public Analyzer(IrSequence irSequence, int absoluteTolerance, double relativeTolerance) {
        cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        createNormedTimings();
    }

    private void createNormedTimings() {
        List<Integer> timings = cleaner.getTimings();
        timebase = cleaner.getTimings().get(0);
        normedTimings = new int[timings.size()];
        for (int i = 0; i < timings.size(); i++) {
            normedTimings[i] = Math.round((float) timings.get(i) / (float) timebase);
        }
    }

}
