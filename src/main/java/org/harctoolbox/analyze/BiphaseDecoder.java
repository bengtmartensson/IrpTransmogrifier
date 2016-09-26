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
import java.util.List;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.IrStreamItem;

public class BiphaseDecoder extends AbstractDecoder {

    private final int half;
    private final int full;

    public BiphaseDecoder(Analyzer analizer, Analyzer.AnalyzerParams params, int half, int full)  {
        super(analizer, params);
        setBitSpec(params.getTimebase());
        this.half = half;
        this.full = full;
    }

    public BiphaseDecoder(Analyzer analizer, Analyzer.AnalyzerParams params)  {
        super(analizer, params);
        setBitSpec(params.getTimebase());
        this.half = analizer.getTimings().get(0);
        this.full = analizer.getTimings().get(1);
    }

    @Override
    protected List<IrStreamItem> process(int beg, int length) throws DecodeException {
        List<IrStreamItem> items = new ArrayList<>(16);
        ParameterData data = new ParameterData();
        BiphaseState state = BiphaseState.pendingGap;
        for (int i = beg; i < beg + length; i++) {
            int noBitsLimit = params.getNoBitsLimit(noPayload);
            boolean isFlash = i % 2 == 0;
            boolean isShort = analyzer.getCleanedTime(i) == half;
            boolean isLong = analyzer.getCleanedTime(i) == full;

            if (isShort || isLong) {
                switch (state) {
                    case pendingGap:
                        if (!isFlash)
                            throw new DecodeException(i);
                        data.update(params.isInvert() ? 1 : 0);
                        state = isLong ? BiphaseState.pendingFlash : BiphaseState.zero;
                        break;
                    case pendingFlash:
                        if (isFlash)
                            throw new DecodeException(i);
                        data.update(params.isInvert() ? 0 : 1);
                        state = isLong ? BiphaseState.pendingGap : BiphaseState.zero;
                        break;
                    case zero:
                        if (isLong)
                            throw new DecodeException(i);
                        state = isFlash ? BiphaseState.pendingFlash : BiphaseState.pendingGap;
                        break;
                    default:
                        throw new DecodeException(i);
                }
            } else {
                int time = analyzer.getCleanedTime(i);
                Duration duration;
                if (isFlash) {
                    if (state == BiphaseState.pendingGap) {
                        data.update(params.isInvert() ? 1 : 0);
                        time -= half;
                    }
                    duration = newFlash(time);
                } else {
                    if (state == BiphaseState.pendingFlash) {
                        data.update(params.isInvert() ? 0 : 1);
                        time -= half;
                    }
                    duration = newGap(time);
                }
                saveParameter(data, items, params.getBitDirection());
                data = new ParameterData();
                items.add(duration);
                state = BiphaseState.zero;
            }
            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, params.getBitDirection());
                data = new ParameterData();
            }
        }
        return items;
    }

    private enum BiphaseState {
        pendingGap,
        pendingFlash,
        zero;
    }
}
