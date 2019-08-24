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
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.NonUniqueBitCodeException;

public abstract class AbstractBiphaseDecoder extends AbstractDecoder {

    protected static BitSpec mkBitSpec(double duration, double timebase, boolean invert, Burst.Preferences burstPrefs) {
        Flash on = Burst.newFlash(duration, timebase, burstPrefs);
        Gap off = Burst.newGap(duration, timebase, burstPrefs);
        List<IrStreamItem> listOffOn = new ArrayList<>(2);
        listOffOn.add(off);
        listOffOn.add(on);

        List<IrStreamItem> listOnOff = new ArrayList<>(2);
        listOnOff.add(on);
        listOnOff.add(off);

        List<BareIrStream> list = new ArrayList<>(2);
        if (invert) {
            list.add(new BareIrStream(listOnOff));
            list.add(new BareIrStream(listOffOn));
        } else {
            list.add(new BareIrStream(listOffOn));
            list.add(new BareIrStream(listOnOff));
        }
        try {
            return new BitSpec(list);
        } catch (NonUniqueBitCodeException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    protected final int half;
    protected final int full;
    protected ParameterData data;
    private final boolean invert;

    public AbstractBiphaseDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int half, int full, boolean invert)  {
        super(analyzer, params);
        this.invert = invert != params.isInvert();
        bitSpec = mkBitSpec(timebase, timebase, this.invert, params.getBurstPrefs());
        this.half = half;
        this.full = full;
    }

    public AbstractBiphaseDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, boolean invert)  {
        this(analyzer, params, analyzer.getTiming(0), analyzer.getTiming(1), invert);
    }

    @Override
    protected List<IrStreamItem> parse(int beg, int length) throws DecodeException {
        List<IrStreamItem> items = new ArrayList<>(2*length);
        data = new ParameterData();
        int foundStartBits = 0;
        BiphaseState state = BiphaseState.start;
        for (int index = beg; index < beg + length; index++) {
            int noBitsLimit = params.getNoBitsLimit(noPayload);
            boolean isFlash = index % 2 == 0;
            boolean useExtent = params.isUseExtents() && (index == beg + length - 1);
            int time = analyzer.getCleanedTime(index);
            boolean isShort = time == half;
            boolean isLong = time == full;

            switch (state) {
                case start:
                    if (startDurations() == 0) {
                        if (!isFlash)
                            throw new ThisCannotHappenException();
                        if (isShort) {
                            if (invert) {
                                data.update(1);
                                state = BiphaseState.zero;
                            } else {
                                state = BiphaseState.pendingFlash;
                            }
                        } else {
                            saveParameter(data, items, params.getBitDirection());
                            data = new ParameterData();
                            items.add(newFlash(time));
                            state = BiphaseState.zero;
                        }
                    } else {
                        items.add(newFlashOrGap(isFlash, time));
                        foundStartBits++;
                        if (foundStartBits == startDurations())
                            state = BiphaseState.zero;
                    }
                    break;

                case pendingGap:
                    if (!isFlash)
                        throw new ThisCannotHappenException();

                    data.update(invert);
                    if (isShort) {
                        state = BiphaseState.zero;
                    } else if (isLong) {
                        state = BiphaseState.pendingFlash;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        //items.add(newGap(half));
                        items.add(newFlash(time - half));
                        state = BiphaseState.zero;
                    }
                    break;

                case pendingFlash:
                    if (isFlash)
                        throw new ThisCannotHappenException();

                    data.update(!invert);
                    if (isShort) {
                        state = BiphaseState.zero;
                    } else if (isLong) {
                        state = BiphaseState.pendingGap;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        //items.add(newGap(half));
                        items.add(useExtent ? newExtent(analyzer.getTotalDuration(beg, length - 1) + time - half) : newGap(time - half));
                        state = BiphaseState.zero;
                    }
                    break;

                case zero:
                    if (isShort) {
                        state = isFlash ? BiphaseState.pendingFlash : BiphaseState.pendingGap;
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

    protected abstract int startDurations();

    private enum BiphaseState {
        start,
        pendingGap,
        pendingFlash,
        zero;
    }
}
