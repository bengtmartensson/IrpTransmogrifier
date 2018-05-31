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
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.IrStreamItem;

public final class BiphaseWithDoubleToggleDecoder extends AbstractBiphaseDecoder {

    private final BitSpec doubleLengthBitSpec;
    private final int oneAndAHalf;

    public BiphaseWithDoubleToggleDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int half, int full, int oneAndAHalf) {
        super(analyzer, params, half, full, false);
        doubleLengthBitSpec = mkBitSpec(2*timebase, timebase, params.isInvert(), params.getBurstPrefs());
        this.oneAndAHalf = (oneAndAHalf < 2*full) ? oneAndAHalf : CANNOT_MATCH;
    }

    public BiphaseWithDoubleToggleDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        this(analyzer, params, analyzer.getTiming(0), analyzer.getTiming(1), analyzer.getTiming(2));
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) throws DecodeException {
        List<IrStreamItem> items = new ArrayList<>(2*length);
        data = new ParameterData();
        int foundStartBits = 0;
        BiphaseWithDoubleToggleState state = BiphaseWithDoubleToggleState.start;
        for (int index = beg; index < beg + length; index++) {
            int noBitsLimit = params.getNoBitsLimit(noPayload);
            boolean isFlash = index % 2 == 0;
            boolean useExtent = params.isUseExtents() && (index == beg + length - 1);
            int time = analyzer.getCleanedTime(index);
            boolean isShort = time == half;
            boolean isLong = time == full;
            boolean isOneAndAHalf = time == oneAndAHalf;

            switch (state) {
                case start:
                    if (startBits() == 0) {
                        if (!isFlash)
                            throw new ThisCannotHappenException();

                        if (isShort)
                            if (params.isInvert()) {
                                data.update(1);
                                state = BiphaseWithDoubleToggleState.zero;
                            } else {
                                state = BiphaseWithDoubleToggleState.pendingFlash;
                            }
                        else {
                            saveParameter(data, items, params.getBitDirection());
                            data = new ParameterData();
                            items.add(newFlash(time));
                            state = BiphaseWithDoubleToggleState.zero;
                        }
                    } else {
                        items.add(newFlashOrGap(isFlash, time));
                        if (params.isInvert() == isFlash)
                            foundStartBits++;
                        if (foundStartBits == startBits())
                            state = BiphaseWithDoubleToggleState.zero;
                    }
                    break;

                case pendingGap:
                    if (!isFlash)
                        throw new ThisCannotHappenException();

                    data.update(params.isInvert());
                    if (isShort) {
                        state = BiphaseWithDoubleToggleState.zero;
                    } else if (isLong) {
                        state = BiphaseWithDoubleToggleState.pendingFlash;
                    } else if (isOneAndAHalf) {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        state = BiphaseWithDoubleToggleState.pendingLongFlash;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        items.add(newFlash(time - half));
                        state = BiphaseWithDoubleToggleState.zero;
                    }
                    break;

                case pendingFlash:
                    if (isFlash)
                        throw new ThisCannotHappenException();

                    data.update(!params.isInvert());
                    if (isShort) {
                        state = BiphaseWithDoubleToggleState.zero;
                    } else if (isLong) {
                        state = BiphaseWithDoubleToggleState.pendingGap;
                    } else if (isOneAndAHalf) {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        state = BiphaseWithDoubleToggleState.pendingLongGap;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        items.add(useExtent ? newExtent(analyzer.getTotalDuration(beg, length - 1) + time - half) : newGap(time - half));
                        state = BiphaseWithDoubleToggleState.zero;
                    }
                    break;

                case pendingLongGap:
                    if (!isFlash)
                        throw new ThisCannotHappenException();

                    data.update(params.isInvert());
                    saveParameter(doubleLengthBitSpec, data, items, params.getBitDirection(), false);
                    data = new ParameterData();
                    if (isLong) {
                        state = BiphaseWithDoubleToggleState.zero;
                    } else if (isOneAndAHalf) {
                        state = BiphaseWithDoubleToggleState.pendingFlash;
                    } else
                        throw new DecodeException(index);
                    break;

                case pendingLongFlash:
                    if (isFlash)
                        throw new ThisCannotHappenException();
                    data.update(!params.isInvert());
                    saveParameter(doubleLengthBitSpec, data, items, params.getBitDirection(), false);
                    data = new ParameterData();
                    if (isLong) {
                        state = BiphaseWithDoubleToggleState.zero;
                    } else if (isOneAndAHalf) {
                        state = BiphaseWithDoubleToggleState.pendingGap;
                    } else
                        throw new DecodeException(index);

                    break;

                case zero:
                    if (isShort) {
                        state = isFlash ? BiphaseWithDoubleToggleState.pendingFlash : BiphaseWithDoubleToggleState.pendingGap;
                    } else if (isLong && index > 1) {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        state = isFlash ? BiphaseWithDoubleToggleState.pendingLongFlash : BiphaseWithDoubleToggleState.pendingLongGap;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        items.add(isFlash ? newFlash(time)
                                : useExtent ? newExtent(analyzer.getTotalDuration(beg, length-1) + time)
                                        : newGap(time));
                    }
                    break;

                default:
                    throw new ThisCannotHappenException();
            }
            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, params.getBitDirection());
                data = new ParameterData();
            }
        }
        return items;
    }

    @Override
    protected int startBits() {
        return 0;
    }

    private enum BiphaseWithDoubleToggleState {
        start,
        pendingGap,
        pendingFlash,
        zero,
        pendingLongFlash,
        pendingLongGap;
    }
}
