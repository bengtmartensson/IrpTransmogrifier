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
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.IrStreamItem;

public class SerialDecoder extends AbstractDecoder {

    private static BitSpec mkBitSpec(boolean invert) {
        Flash on = new Flash(1f, null);
        Gap off = new Gap(1f, null);
        List<IrStreamItem> listOn = new ArrayList<>(1);
        listOn.add(on);

        List<IrStreamItem> listOff = new ArrayList<>(1);
        listOff.add(off);
        List<BareIrStream> list = new ArrayList<>(2);
        if (invert) {
            list.add(new BareIrStream(listOn));
            list.add(new BareIrStream(listOff));
        } else {
            list.add(new BareIrStream(listOff));
            list.add(new BareIrStream(listOn));
        }
        return new BitSpec(list);
    }

    public SerialDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        super(analyzer, params);
        bitSpec = mkBitSpec(params.isInvert());
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(8);
        ParameterData data = new ParameterData();
        int noBitsLimit = Integer.MAX_VALUE;

        for (int index = beg; index < beg + length; index++) {
            noBitsLimit = params.getNoBitsLimit(noPayload);
            boolean isFlash = index % 2 == 0;
            int time = analyzer.getCleanedTime(index);
            Duration duration = newFlashOrGap(isFlash, time);
            if (duration.getUnit().isEmpty()) {
                int noBits;
                try {
                    noBits = (int) Math.round(duration.getTimeInUnits());
                } catch (Exception ex) {
                    throw new ThisCannotHappenException(ex);
                }
                int amount = isFlash ? IrCoreUtils.ones(noBits) : 0;
                data.update(amount, noBits);
            } else {
                while (!data.isEmpty())
                    dumpParameters(data, items, noBitsLimit);

                if (index == beg + length - 1 && params.isUseExtents())
                    items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                else
                    items.add(duration);
            }

            while (data.getNoBits() >= noBitsLimit)
                dumpParameters(data, items, noBitsLimit);

        }
        while (!data.isEmpty())
            dumpParameters(data, items, noBitsLimit);

        return items;
    }
}
