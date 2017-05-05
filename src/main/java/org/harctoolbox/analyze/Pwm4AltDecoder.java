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

public final class Pwm4AltDecoder extends PwmDecoder {

    private static Burst[] mkBursts(Analyzer analyzer) throws DecodeException {
        return new Burst[] { analyzer.getSortedBurst(0), analyzer.getSortedBurst(1), analyzer.getSortedBurst(2), analyzer.getSortedBurst(3) };
    }

    public Pwm4AltDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) throws DecodeException {
        super(analyzer, params, mkBursts(analyzer));
    }
}
