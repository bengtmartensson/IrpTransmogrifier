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

import org.harctoolbox.irp.NonUniqueBitCodeException;

public final class Pwm2Decoder extends PwmDecoder {

    private static Burst smallest(Burst burst0, Burst burst1) {
        return burst0.compare(burst1) <= 0 ? burst0 : burst1;
    }

    private static Burst largest(Burst burst0, Burst burst1) {
        return burst0.compare(burst1) <= 0 ? burst1 : burst0;
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst zero, Burst one) throws NonUniqueBitCodeException {
        super(analyzer, params, mkBursts(zero, one));
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int zeroFlash, int zeroGap, int oneFlash, int oneGap) throws NonUniqueBitCodeException {
        this(analyzer, params, new Burst(zeroFlash, zeroGap), new Burst(oneFlash, oneGap));
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int a, int b) throws NonUniqueBitCodeException {
        this(analyzer, params, a, a, a, b);
    }

    public Pwm2Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params) throws NonUniqueBitCodeException {
        this(analyzer, params, smallest(analyzer.getBurst(0), analyzer.getBurst(1)), largest(analyzer.getBurst(0), analyzer.getBurst(1)));
    }
}