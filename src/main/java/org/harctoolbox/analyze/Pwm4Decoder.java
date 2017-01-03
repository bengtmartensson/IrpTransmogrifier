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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.harctoolbox.irp.IrStreamItem;

public class Pwm4Decoder extends AbstractDecoder {

    public final static int CHUNKSIZE = 2;

    private final Burst zero;
    private final Burst one;
    private final Burst two;
    private final Burst three;

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst zero, Burst one, Burst two, Burst three) {
        super(analyzer, params);
        setBitSpec(zero, one, two, three);
        this.zero = zero;
        this.one = one;
        this.two = two;
        this.three = three;
    }

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int flash, int zeroGap, int oneGap, int twoGap, int threeGap) {
        this(analyzer, params, new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
    }

    public Pwm4Decoder(Analyzer analyzer, Analyzer.AnalyzerParams params) throws DecodeException {
        super(analyzer, params);//, new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
        if (analyzer.getNumberOfGaps() < 4)
            throw new DecodeException();
        List<Integer> gaps = new ArrayList<>(4);
        gaps.add(analyzer.getSortedGaps(0));
        gaps.add(analyzer.getSortedGaps(1));
        gaps.add(analyzer.getSortedGaps(2));
        gaps.add(analyzer.getSortedGaps(3));
        Collections.sort(gaps);
        zero = new Burst(analyzer.getSortedFlashes(0), gaps.get(0));
        one  = new Burst(analyzer.getSortedFlashes(0), gaps.get(1));
        two  = new Burst(analyzer.getSortedFlashes(0), gaps.get(2));
        three= new Burst(analyzer.getSortedFlashes(0), gaps.get(3));
        setBitSpec(zero, one, two, three);
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(16);
        ParameterData data = new ParameterData(CHUNKSIZE);
        for (int i = beg; i < beg + length - 1; i += 2) {
            int noBitsLimit = params.getNoBitsLimit(noPayload);
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(mark, space);
            if (burst.equals(zero)) {
                data.update(0);
            } else if (burst.equals(one)) {
                data.update(1);
            } else if (burst.equals(two)) {
                data.update(2);
            } else if (burst.equals(three)) {
                data.update(3);
            } else {
                if (!data.isEmpty()) {
                    saveParameter(data, items, params.getBitDirection());
                    data = new ParameterData();
                }

                items.add(newFlash(mark));
                if (i == beg + length - 2 && params.isUseExtents())
                    items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                else
                    items.add(newGap(space));
            }

            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, params.getBitDirection());
                data = new ParameterData(CHUNKSIZE);
            }
        }
        if (!data.isEmpty())
            saveParameter(data, items, params.getBitDirection());

        return items;
    }
}