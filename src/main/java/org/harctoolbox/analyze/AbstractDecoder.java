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

    protected static BitSpec mkBitSpec(Burst zero, Burst one, double timebase) {
        List<BareIrStream> list = new ArrayList<>(2);
        list.add(zero.toBareIrStream(timebase));
        list.add(one.toBareIrStream(timebase));
        try {
            return new BitSpec(list);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            throw new ThisCannotHappenException();
        }
    }

    protected NameEngine nameEngine;
    protected int noPayload;
    protected final int timebase;
    protected final Analyzer analyzer;
    protected final BitSpec bitSpec;

    public AbstractDecoder(Analyzer analyzer, int timebase, BitSpec bitSpec) {
        this.nameEngine = null;
        this.analyzer = analyzer;
        this.timebase = timebase;
        this.bitSpec = bitSpec;
    }

    public Protocol process(BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths) throws DecodeException {
        nameEngine = new NameEngine();
        noPayload = 0;
        RepeatFinder.RepeatFinderData repeatfinderData = analyzer.getRepeatFinderData();
        List<IrStreamItem> items = process(0, repeatfinderData.getBeginLength(), bitDirection, useExtents, parameterWidths);
        List<IrStreamItem> repeatItems = process(repeatfinderData.getBeginLength(), repeatfinderData.getRepeatLength(), bitDirection, useExtents, parameterWidths);
        List<IrStreamItem> endingItems = process(repeatfinderData.getEndingStart(), repeatfinderData.getEndingLength(), bitDirection, useExtents, parameterWidths);
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
        Protocol protocol = new Protocol(analyzer.getGeneralSpec(), bitspecIrstream, nameEngine, null, null);
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

    protected abstract List<IrStreamItem> process(int beginStart, int beginLength, BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths)
            throws DecodeException;

    protected int getNoBitsLimit(List<Integer> parameterWidths) {
        return noPayload >= parameterWidths.size() ? Integer.MAX_VALUE : parameterWidths.get(noPayload);
    }

    protected static class ParameterData {

        private long data;
        private int noBits;

        ParameterData() {
            data = 0L;
            noBits = 0;
        }

        @Override
        public String toString() {
            return Long.toString(getData()) + ":" + Integer.toString(getNoBits());
        }

        public void update(int amount) {
            data *= 2;
            data += amount;
            noBits++;
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
