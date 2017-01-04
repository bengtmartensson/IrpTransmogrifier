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

public abstract class PwmDecoder extends AbstractDecoder {

    private static BitSpec mkBitSpec(Burst[] bursts, double timebase) {
        List<BareIrStream> list = new ArrayList<>(bursts.length);
        for (Burst burst : bursts)
            list.add(burst.toBareIrStream(timebase));

        return new BitSpec(list);
    }

    protected static Burst[] mkBursts(Burst... bursts) {
        return bursts;
    }

    private final Burst[] bursts;
    private final int chunksize;

    public PwmDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst[] bursts) {
        super(analyzer, params);
        this.bursts = bursts.clone();
        chunksize = (int) IrpUtils.log2(bursts.length);
        bitSpec = mkBitSpec(bursts, timebase);
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(16);
        int noBitsLimit = Integer.MAX_VALUE;
        ParameterData data = new ParameterData(chunksize);
        for (int i = beg; i < beg + length - 1; i += 2) {
            noBitsLimit = params.getNoBitsLimit(noPayload);
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(mark, space);
            boolean hit = false;
            int n = 0;
            while (!hit && n < bursts.length) {
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