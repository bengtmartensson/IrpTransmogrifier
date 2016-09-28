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
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.BitspecIrstream;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.InvalidRepeatException;
import org.harctoolbox.irp.IrStream;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.IrpSyntaxException;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.RepeatMarker;

public abstract class AbstractDecoder {

    private static BitSpec mkBitSpec(List<BareIrStream> list, double timebase) {
        try {
            return new BitSpec(list);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            throw new ThisCannotHappenException();
        }
    }

    protected static BitSpec mkBitSpec(Burst zero, Burst one, double timebase) {
        List<BareIrStream> list = new ArrayList<>(2);
        list.add(zero.toBareIrStream(timebase));
        list.add(one.toBareIrStream(timebase));
        return mkBitSpec(list, timebase);
    }

    protected static BitSpec mkBitSpec(Burst zero, Burst one, Burst two, Burst three, double timebase) {
        List<BareIrStream> list = new ArrayList<>(4);
        list.add(zero.toBareIrStream(timebase));
        list.add(one.toBareIrStream(timebase));
        list.add(two.toBareIrStream(timebase));
        list.add(three.toBareIrStream(timebase));
        return mkBitSpec(list, timebase);
    }

    protected static BitSpec mkBitSpec(double timebase) {
        List<BareIrStream> list = new ArrayList<>(0);
        return mkBitSpec(list, timebase);
    }

    private static BitSpec mkBitSpec(double timebase, boolean invert) {
        Flash on = Burst.newFlash(timebase, timebase);
        Gap off = Burst.newGap(timebase, timebase);
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
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            throw new ThisCannotHappenException();
        }
    }

    protected NameEngine nameEngine;
    protected int noPayload;
    protected final double timebase;
    protected final Analyzer analyzer;
    protected BitSpec bitSpec;
    protected final Analyzer.AnalyzerParams params;

    public AbstractDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        this.nameEngine = null;
        this.analyzer = analyzer;
        this.params = params;
        this.timebase = params.getTimebase() > 0 ? params.getTimebase() : analyzer.getTiming(0);
        this.bitSpec = new BitSpec();
    }

    public Protocol process() throws DecodeException {
        nameEngine = new NameEngine();
        noPayload = 0;
        RepeatFinder.RepeatFinderData repeatfinderData = analyzer.getRepeatFinderData();
        List<IrStreamItem> items = process(0, repeatfinderData.getBeginLength());
        List<IrStreamItem> repeatItems = process(repeatfinderData.getBeginLength(), repeatfinderData.getRepeatLength());
        List<IrStreamItem> endingItems = process(repeatfinderData.getEndingStart(), repeatfinderData.getEndingLength());
        IrStream irStream;
        RepeatMarker repeatMarker = new RepeatMarker(repeatfinderData.getNumberRepeats());
        if (repeatfinderData.getBeginLength() == 0 && repeatfinderData.getEndingLength() == 0) {
            irStream = new IrStream(repeatItems, repeatMarker);
        } else {
            if (!repeatItems.isEmpty())
                items.add(new IrStream(repeatItems, repeatMarker));
            if (!endingItems.isEmpty())
                items.add(new IrStream(endingItems));
            irStream = new IrStream(items);
        }
        BitspecIrstream bitspecIrstream = new BitspecIrstream(bitSpec, irStream);
        Protocol protocol = new Protocol(params.getGeneralSpec(timebase), bitspecIrstream, nameEngine, null, null);
        return protocol;
    }

    protected Flash newFlash(int mark) {
        return Burst.newFlash(mark, timebase);
    }

    protected Extent newExtent(int total) {
        return Burst.newExtent(total, timebase);
    }

    protected Gap newGap(int space) {
        return Burst.newGap(space, timebase);
    }

    protected void saveParameter(ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection) {
        if (parameterData.isEmpty())
            return;

        parameterData.fixBitDirection(bitDirection);
        String name = Analyzer.mkName(noPayload++);
        try {
            nameEngine.define(name, parameterData.getData());
        } catch (IrpSyntaxException ex) {
            throw new ThisCannotHappenException();
        }
        items.add(new FiniteBitField(name, parameterData.getNoBits()));
    }

    protected abstract List<IrStreamItem> process(int beginStart, int beginLength)
            throws DecodeException;

    protected int getNoBitsLimit(List<Integer> parameterWidths) {
        return (parameterWidths == null || noPayload >= parameterWidths.size()) ? Integer.MAX_VALUE : parameterWidths.get(noPayload);
    }

    protected final void setBitSpec(Burst zero, Burst one) {
        bitSpec = mkBitSpec(zero, one, timebase);
    }

    protected final void setBitSpec(Burst zero, Burst one, Burst two, Burst three) {
        bitSpec = mkBitSpec(zero, one, two, three, timebase);
    }

    protected final void setBitSpec(double timebase) {
        bitSpec = mkBitSpec(timebase, params.isInvert());
    }

    public String name() {
        return getClass().getSimpleName();
    }

    protected static class ParameterData {

        private long data;
        private int noBits;
        private final int chunkSize;

        ParameterData(int chunkSize) {
            data = 0L;
            noBits = 0;
            this.chunkSize = chunkSize;
        }

        ParameterData() {
            this(1);
        }

        @Override
        public String toString() {
            return Long.toString(getData()) + ":" + Integer.toString(getNoBits());
        }

        public void update(int amount) {
            data <<= chunkSize;
            data += amount;
            noBits += chunkSize;
        }

        public void update(boolean invert) {
            update(invert ? 1 : 0);
        }

        private void fixBitDirection(BitDirection bitDirection) {
            if (bitDirection == BitDirection.lsb)
                data = IrpUtils.reverse(getData(), getNoBits());
        }

        /**
         * @return the data
         */
        public long getData() {
            return data;
        }

        /**
         * @return the noBits
         */
        public int getNoBits() {
            return noBits;
        }

        public boolean isEmpty() {
            return noBits == 0;
        }
    }
}
