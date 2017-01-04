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

import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.BitspecIrstream;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrStream;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.RepeatMarker;

public abstract class AbstractDecoder {

    private static final Logger logger = Logger.getLogger(AbstractDecoder.class.getName());

    static final Class<?>[] decoders = {
        TrivialDecoder.class,
        Pwm2Decoder.class,
        Pwm4Decoder.class,
        XmpDecoder.class,
        BiphaseDecoder.class,
        BiphaseWithStartbitDecoder.class,
        SerialDecoder.class,
    };

    static final int NUMBERDECODERS = decoders.length;

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

    public Protocol parse() throws DecodeException {
        nameEngine = new NameEngine();
        noPayload = 0;
        RepeatFinder.RepeatFinderData repeatfinderData = analyzer.getRepeatFinderData();
        List<IrStreamItem> items = parse(0, repeatfinderData.getBeginLength());
        List<IrStreamItem> repeatItems = parse(repeatfinderData.getBeginLength(), repeatfinderData.getRepeatLength());
        List<IrStreamItem> endingItems = parse(repeatfinderData.getEndingStart(), repeatfinderData.getEndingLength());
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

    protected Duration newFlashOrGap(boolean isFlash, int time) {
        return isFlash ? newFlash(time) : newGap(time);
    }

    protected void saveParameter(ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection) {
        saveParameter(parameterData, items, bitDirection, false);
    }

    protected void saveParameter(ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection, boolean complement) {
        if (parameterData.isEmpty())
            return;

        parameterData.fixBitDirection(bitDirection);
        if (complement)
            parameterData.invertData();
        String name = Analyzer.mkName(noPayload);
        noPayload++;
        try {
            nameEngine.define(name, parameterData.getData());
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException();
        }
        items.add(new FiniteBitField(name, parameterData.getNoBits()));
    }

    protected abstract List<IrStreamItem> parse(int beginStart, int beginLength) throws DecodeException;

    protected int getNoBitsLimit(List<Integer> parameterWidths) {
        return (parameterWidths == null || noPayload >= parameterWidths.size()) ? Integer.MAX_VALUE : parameterWidths.get(noPayload);
    }

    public String name() {
        return getClass().getSimpleName();
    }

    protected void dumpParameters(ParameterData data, List<IrStreamItem> items, int noBitsLimit) {
        ParameterData lowerParam = data.reduce(noBitsLimit);
        saveParameter(lowerParam, items, params.getBitDirection(), params.isInvert());
    }

    protected static class ParameterData {

        private long data;
        private int noBits;
        private final int chunkSize;

        private ParameterData(long data, int noBits, int chunkSize) {
            this.data = data;
            this.noBits = noBits;
            this.chunkSize = chunkSize;
        }

        ParameterData(int chunkSize) {
            this(0L, 0, chunkSize);
        }

        ParameterData() {
            this(1);
        }

        @Override
        public String toString() {
            return Long.toString(getData()) + ":" + Integer.toString(getNoBits());
        }

        public void update(int amount) {
            update(amount, chunkSize);
        }

        public void update(int amount, int bits) {
            data <<= bits;
            data += amount;
            noBits += bits;
        }

        public void update(boolean invert) {
            update(invert ? 1 : 0);
        }

        public ParameterData reduce(int maxBits) {
            ParameterData pd;
            if (noBits <= maxBits) {
                pd = new ParameterData(data, noBits, chunkSize);
                data = 0L;
                noBits = 0;
            } else {
                pd = new ParameterData(data >> (noBits - maxBits), maxBits, chunkSize);
                noBits -= maxBits;
                data &= IrpUtils.ones(noBits);
            }
            return pd;
        }

        private void invertData() {
            data = IrCoreUtils.maskTo(~data, noBits);
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
