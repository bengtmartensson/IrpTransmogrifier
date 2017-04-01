/*
Copyright (C) 2017 Bengt Martensson.

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

public final class XmpDecoder extends PwmDecoder {

    private final static int NO_BURSTS = 16;

    private static Burst[] mkBursts(int flash, int gapsBase, int delta) {
        Burst[] array = new Burst[NO_BURSTS];
        for (int n = 0; n < NO_BURSTS; n++) {
            Burst burst = new Burst(flash, gapsBase + n*delta);
            array[n] = burst;
        }
        return array;
    }

    private static Burst[] mkBursts(Analyzer analyzer) {
        int flash = analyzer.getFlashes().get(0);
        List<Integer> gaps = analyzer.getGaps(); // sorted?
        int gapsBase = gaps.get(0);
        int delta = Integer.MAX_VALUE;

        for (int i = 0; i < gaps.size() - 1; i++) {
            int diff = gaps.get(i+1) - gaps.get(i);
            delta = Math.min(delta, diff);
        }
        return mkBursts(flash, gapsBase, delta);
    }

    public XmpDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int flash, int gapsBase, int delta) {
        super(analyzer, params, mkBursts(flash, gapsBase, delta));
    }

    public XmpDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        super(analyzer, params, mkBursts(analyzer));
    }
}
