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
import org.harctoolbox.irp.NonUniqueBitCodeException;

public final class Pwm4Decoder extends PwmDecoder {

    private final static int NO_BURSTS = 4;

    private static Burst[] mkBursts(int flash, int zeroGap, int oneGap, int twoGap, int threeGap) {
        return mkBursts(new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
    }

    private static Burst[] mkBursts(Analyzer analyzer) throws DecodeException {
        if (analyzer.getNumberOfGaps() < NO_BURSTS)
            throw new DecodeException();
        int flash = analyzer.getFlashes().get(0);
        List<Integer> gaps = analyzer.getGaps();

        return mkBursts(flash, gaps.get(0), gaps.get(1), gaps.get(2), gaps.get(3));
    }

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst zero, Burst one, Burst two, Burst three) throws NonUniqueBitCodeException {
        super(analyzer, params, mkBursts(zero, one, two, three));
    }

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int flash, int zeroGap, int oneGap, int twoGap, int threeGap) throws NonUniqueBitCodeException {
        this(analyzer, params, new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
    }

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params) throws DecodeException, NonUniqueBitCodeException {
        super(analyzer, params, mkBursts(analyzer));//, new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
    }
}