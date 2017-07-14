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

import java.math.BigInteger;
import java.util.ArrayList;
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
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.RepeatMarker;

public abstract class AbstractDecoder {

    private static final Logger logger = Logger.getLogger(AbstractDecoder.class.getName());

    static final Class<?>[] decoders = {
        TrivialDecoder.class,
        Pwm2Decoder.class,
        Pwm4Decoder.class,
        Pwm4AltDecoder.class,
        XmpDecoder.class,
        BiphaseDecoder.class,
        BiphaseWithStartbitDecoder.class,
        BiphaseWithDoubleToggleDecoder.class,
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
        this.timebase = analyzer.getTimeBaseFromData(params);
        this.bitSpec = new BitSpec();
    }

    public Protocol[] parse() throws DecodeException {
        return parse(false);
    }

    public Protocol[] parse(boolean signalMode) throws DecodeException {
        Protocol[] result = new Protocol[analyzer.getNoSequences()];
        for (int i = 0; i < analyzer.getNoSequences(); i++)
            result[i] = parse(i, signalMode);
        return result;
    }

    Protocol parse(int number, boolean signalMode) throws DecodeException {
        assert (!(signalMode && number > 0));
        noPayload = 0;
        nameEngine = new NameEngine();
        IrStream irStream;
        if (signalMode) {
            List<IrStreamItem> items       = parse(analyzer.getSequenceBegin(0), analyzer.getSequenceLength(0));
            List<IrStreamItem> repeatItems = parse(analyzer.getSequenceBegin(1), analyzer.getSequenceLength(1));
            List<IrStreamItem> endingItems = parse(analyzer.getSequenceBegin(2), analyzer.getSequenceLength(2));

            RepeatMarker repeatMarker = new RepeatMarker("*");
            if (analyzer.getSequenceLength(0) == 0 && analyzer.getSequenceLength(2) == 0) {
                irStream = new IrStream(repeatItems, repeatMarker);
            } else {
                if (!repeatItems.isEmpty())
                    items.add(new IrStream(repeatItems, repeatMarker));
                if (!endingItems.isEmpty())
                    items.add(new IrStream(endingItems));
                irStream = new IrStream(items);
            }
        } else {
            int begin = analyzer.getSequenceBegin(number);
            RepeatFinder.RepeatFinderData repeatfinderData = analyzer.getRepeatFinderData(number);
            List<IrStreamItem> items = parse(begin, repeatfinderData.getBeginLength());
            List<IrStreamItem> repeatItems = parse(begin + repeatfinderData.getBeginLength(), repeatfinderData.getRepeatLength());
            List<IrStreamItem> endingItems = parse(begin + repeatfinderData.getEndingStart(), repeatfinderData.getEndingLength());
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
        }
        BitspecIrstream bitspecIrstream = new BitspecIrstream(bitSpec, irStream);
        Protocol protocol = new Protocol(params.getGeneralSpec(timebase), bitspecIrstream, nameEngine, null, null);
        return protocol;
    }

    protected Flash newFlash(int flash) {
        return Burst.newFlash(flash, timebase, params.getBurstPrefs());
    }

    protected Extent newExtent(int total) {
        return Burst.newExtent(total, timebase, params.getBurstPrefs());
    }

    protected Gap newGap(int gap) {
        return Burst.newGap(gap, timebase, params.getBurstPrefs());
    }

    protected Duration newFlashOrGap(boolean isFlash, int time) {
        return isFlash ? newFlash(time) : newGap(time);
    }

    protected void saveParameter(ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection) {
        saveParameter(parameterData, items, bitDirection, false);
    }

    protected void saveParameter(ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection, boolean complement) {
        saveParameter(null, parameterData, items, bitDirection, complement);
    }

    protected void saveParameter(BitSpec bitSpec, ParameterData parameterData, List<IrStreamItem> items, BitDirection bitDirection, boolean complement) {
       if (parameterData.isEmpty())
            return;

        parameterData.fixBitDirection(bitDirection);
        if (complement)
            parameterData.invertData();
        String name = Analyzer.mkName(noPayload);
        noPayload++;
        try {
            nameEngine.define(name, parameterData.getData());
            FiniteBitField bitField = new FiniteBitField(name, parameterData.getNoBits());
            if (bitSpec == null) {
                items.add(bitField);
            } else {
                List<IrStreamItem> list = new ArrayList<>(1);
                list.add(bitField);
                IrStream irStream = new IrStream(list);
                BitspecIrstream bitspecIrstream = new BitspecIrstream(bitSpec, irStream);
                items.add(bitspecIrstream);
            }
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException();
        }
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

        private BigInteger data;
        private int noBits;
        private final int chunkSize;

        private ParameterData(long data, int noBits, int chunkSize) {
            this(BigInteger.valueOf(data), noBits, chunkSize);
        }

        private ParameterData(BigInteger data, int noBits, int chunkSize) {
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

        public String toString(int radix) {
            return IrCoreUtils.radixPrefix(radix)  + getData().toString(radix) + ":" + Integer.toString(getNoBits());
        }

        @Override
        public String toString() {
            return toString(10);
        }

        public void update(int amount) {
            update(amount, chunkSize);
        }

        public void update(int amount, int bits) {
            update(BigInteger.valueOf(amount), bits);
        }

        public void update(BigInteger amount, int bits) {
            data = data.shiftLeft(bits);
            data = data.or(amount);
            noBits += bits;
        }

        public void update(boolean invert) {
            update(invert ? 1 : 0);
        }

        public ParameterData reduce(int maxBits) {
            ParameterData pd;
            if (noBits <= maxBits) {
                pd = new ParameterData(data, noBits, chunkSize);
                data = BigInteger.ZERO;
                noBits = 0;
            } else {
                pd = new ParameterData(data.shiftRight(noBits - maxBits), maxBits, chunkSize);
                noBits -= maxBits;
                data = data.and(BigInteger.valueOf(IrCoreUtils.ones(noBits)));
            }
            return pd;
        }

        private void invertData() {
            BigInteger mask = BigInteger.valueOf(IrCoreUtils.ones(noBits));
            data = data.xor(mask);
        }

        private void fixBitDirection(BitDirection bitDirection) {
            if (bitDirection == BitDirection.lsb)
                data = IrCoreUtils.reverse(getData(), getNoBits());
        }

        /**
         * @return the data
         */
        public BigInteger getData() {
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
