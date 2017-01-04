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

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.IrpUtils;

public class XmpDecoder extends AbstractDecoder {

    private final static int NO_BURSTS = 16;
    public final static int CHUNKSIZE = (int) IrpUtils.log2(NO_BURSTS);

    protected static BitSpec mkBitSpec(Burst[] bursts, double timebase) {
        List<BareIrStream> list = new ArrayList<>(bursts.length);
        for (Burst burst : bursts)
            list.add(burst.toBareIrStream(timebase));

        return new BitSpec(list);
    }

    private static Burst[] mkBursts(int flash, int gapsBase, int delta) {
        Burst[] array = new Burst[NO_BURSTS];
        for (int n = 0; n < NO_BURSTS; n++) {
            Burst burst = new Burst(flash, gapsBase + n*delta);
            array[n] = burst;
        }
        return array;
    }

    private final Burst[] bursts;

    public XmpDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int flash, int gapsBase, int delta) {
        super(analyzer, params);
        bursts = mkBursts(flash, gapsBase, delta);
        bitSpec = mkBitSpec(bursts, params.getTimebase());
    }

    public XmpDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) throws DecodeException {
        super(analyzer, params);//, new Burst(flash, zeroGap), new Burst(flash, oneGap), new Burst(flash, twoGap), new Burst(flash, threeGap));
        int flash = analyzer.getDistinctFlashes().get(0);
        List<Integer> gaps = analyzer.getDistinctGaps(); // sorted?
        int gapsBase = gaps.get(0);
        int delta = Integer.MAX_VALUE;

        for (int i = 0; i < gaps.size()-1; i++) {
            int diff = gaps.get(i+1) - gaps.get(i);
            delta = Math.min(delta, diff);
        }

        bursts = mkBursts(flash, gapsBase, delta);
        bitSpec = mkBitSpec(bursts, params.getTimebase());
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(16);
        int noBitsLimit = Integer.MAX_VALUE;
        ParameterData data = new ParameterData(CHUNKSIZE);
        for (int i = beg; i < beg + length - 1; i += 2) {
            noBitsLimit = params.getNoBitsLimit(noPayload);
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(mark, space);
            boolean hit = false;
            int n = 0;
            while (!hit && n < NO_BURSTS) {
                if (burst.equals(bursts[n])) {
                    data.update(n);
                    hit = true;
                }
                n++;
            }
            if (!hit) {
                while (!data.isEmpty())
                    dumpParameters(data, items, noBitsLimit);

                items.add(newFlash(mark));
                if (i == beg + length - 2 && params.isUseExtents())
                    items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                else
                    items.add(newGap(space));
            }

            while (data.getNoBits() >= noBitsLimit)
                dumpParameters(data, items, noBitsLimit);
        }
        while (!data.isEmpty())
            dumpParameters(data, items, noBitsLimit);

        return items;
    }
}