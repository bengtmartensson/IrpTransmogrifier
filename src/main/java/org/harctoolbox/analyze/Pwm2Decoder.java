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

public final class Pwm2Decoder extends PwmDecoder {

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst zero, Burst one) {
        super(analyzer, params, mkBursts(zero, one));
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int zeroFlash, int zeroGap, int oneFlash, int oneGap) {
        this(analyzer, params, new Burst(zeroFlash, zeroGap), new Burst(oneFlash, oneGap));
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int a, int b) {
        this(analyzer, params, a, a, a, b);
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        this(analyzer, params,
                analyzer.getFlashes().get(0), analyzer.getGaps().get(0),
                analyzer.getFlashes().get(0), analyzer.getGaps().get(1));
    }
}