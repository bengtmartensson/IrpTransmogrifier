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
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.NonUniqueBitCodeException;

public abstract class PwmDecoder extends AbstractDecoder {

    private static BitSpec mkBitSpec(Burst[] bursts, double timebase, Burst.Preferences burstPrefs) throws NonUniqueBitCodeException {
        List<BareIrStream> list = new ArrayList<>(bursts.length);
        for (Burst burst : bursts)
            list.add(burst.toBareIrStream(timebase, burstPrefs));

        return new BitSpec(list);
    }

    protected static Burst[] mkBursts(Burst... bursts) {
        return bursts;
    }

    private final Burst[] bursts;
    private final int chunksize;
    private final boolean distinctFlashesInBursts;

    public PwmDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, Burst[] bursts) throws NonUniqueBitCodeException {
        super(analyzer, params);
        this.bursts = bursts.clone();
        distinctFlashesInBursts = setupDistinctFlashesInBursts();
        chunksize = (int) IrCoreUtils.log2(bursts.length);
        bitSpec = mkBitSpec(bursts, timebase, params.getBurstPrefs());
    }

    public boolean hasDistinctFlashesInBursts() {
        return distinctFlashesInBursts;
    }

    private boolean setupDistinctFlashesInBursts() {
        ArrayList<Integer> flashes = new ArrayList<>(bursts.length);
        for (Burst burst : bursts) {
            int flash = burst.getFlashDuration();
            if (flashes.contains(flash))
                return false;
            flashes.add(flash);
        }
        return true;
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(16);
        int noBitsLimit = Integer.MAX_VALUE;
        ParameterData data = new ParameterData(chunksize);
        for (int i = beg; i < beg + length - 1; i += 2) {
            noBitsLimit = params.getNoBitsLimit(noPayload);
            int flash = analyzer.getCleanedTime(i);
            int gap = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(flash, gap);
            boolean hit = false;
            int n = 0;
            while (!hit && n < bursts.length) {
                if (burst.equals(bursts[n])) {
                    data.update(n);
                    hit = true;
                }
                n++;
            }
            int overhang = 0;
            if (!hit) {
                if (hasDistinctFlashesInBursts() && !data.isEmpty()) {
                    n = 0;
                    while (!hit && n < bursts.length) {
                        if (burst.equalsWithLongGap(bursts[n])) {
                            data.update(n);
                            hit = true;
                            overhang = burst.overhang(bursts[n]);
                        }
                        n++;
                    }
                }
                while (!data.isEmpty())
                    dumpParameters(data, items, noBitsLimit);

                if (overhang == 0) {
                    items.add(newFlash(flash));
                    if (i == beg + length - 2 && params.isUseExtents())
                        items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                    else
                        items.add(newGap(gap));
                } else {
                    if (i == beg + length - 2 && params.isUseExtents())
                        items.add(newExtent(analyzer.getTotalDuration(beg, length) - gap));
                    else
                        items.add(newGap(overhang));
                }
            }

            while (data.getNoBits() >= noBitsLimit)
                dumpParameters(data, items, noBitsLimit);
        }
        while (!data.isEmpty())
            dumpParameters(data, items, noBitsLimit);

        return items;
    }
}