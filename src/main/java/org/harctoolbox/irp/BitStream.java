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

package org.harctoolbox.irp;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class BitStream extends IrStreamItem implements Evaluatable {

    private static final Logger logger = Logger.getLogger(BitStream.class.getName());
    private static final int WEIGHT = 2;

    private long length;
    private BigInteger data;

    BitStream() {
        data = BigInteger.ZERO;
        length = 0;
    }

    BitStream(BitField bitField, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        if (bitField instanceof InfiniteBitField)
            throw new IncompatibleArgumentException("Infinite bitfields cannot be converted to BitStreams.");

        data = BigInteger.valueOf((generalSpec != null && generalSpec.getBitDirection() == BitDirection.msb)
                    ? bitField.toNumber(nameEngine)
                    : IrpUtils.reverse(bitField.toNumber(nameEngine), (int) bitField.getWidth(nameEngine)));
        length = bitField.getWidth(nameEngine);

    }
    @Override
    public String toString() {
        return "BitStream(" + data + "=0x" + data.toString(16) + "=0b" + data.toString(2) + ":" + length + ")";
    }

    @Override
    public String toIrpString() {
        throw new UnsupportedOperationException("Not supported.");
    }

    void add(BitStream bitStream, NameEngine nameEngine, GeneralSpec generalSpec) {
        data = data.shiftLeft((int)bitStream.length).or(bitStream.data);
        length += bitStream.length;
    }

    /** Extracts bit n*chunksize .. (n+1)*chunksize-1 */
    private int getChunkNo(int n, int chunksize) {
        if (n < 0 || (length > 0 && (n+1)*chunksize-1 >= length))
            throw new IndexOutOfBoundsException("Illegal bit " + n + " in getChunkNo");
        // If a chunk goes over the data[] limits, this has to be implemented extra.
        // I have more interesting thing to do :-)
        //if (((n+1)*chunksize-1)/Long.SIZE != (n*chunksize)/Long.SIZE)
        //    throw new RuntimeException("Case not implemented");
        //int chunk = (int)(data[n*chunksize/Long.SIZE] >> n*chunksize) & ((1 << chunksize)- 1);
        long mask = (1L << chunksize) - 1L;
        return data.shiftRight(n*chunksize).and(BigInteger.valueOf(mask)).intValueExact();
    }


    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this);

        EvaluatedIrStream list = new EvaluatedIrStream(nameEngine, generalSpec, pass);

        list.add(this);
        IrpUtils.exiting(logger, "evaluate", list);
        return list;
    }

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this);

        EvaluatedIrStream list = new EvaluatedIrStream(nameEngine, generalSpec, pass);

        if (bitSpec == null || length % bitSpec.getChunkSize() != 0) {
            list.add(this);
        } else {
            int noChunks = ((int) length) / bitSpec.getChunkSize();
            for (int n = 0; n < noChunks; n++) {
                int chunkNo = noChunks - n - 1;
                BareIrStream irs = bitSpec.get(getChunkNo(chunkNo, bitSpec.getChunkSize()));
                EvaluatedIrStream evaluatedIrStream = irs.evaluate(state, pass, nameEngine, generalSpec);
                list.add(evaluatedIrStream);
            }
        }
        IrpUtils.exiting(logger, "evaluate", list);
        return list;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return length == 0;
    }

    @Override
    public Element toElement(Document document) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    int numberOfBits() {
        return 0;
    }

    @Override
    int numberOfBareDurations() {
        return (int) length;
    }

    @Override
    ParserRuleContext getParseTree() {
        return null;
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        return true;
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.newDurationType(gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.newDurationType(!gapFlashBitSpecs);
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BitStream))
            return false;

        BitStream other = (BitStream) obj;
        return length == other.length && data.equals(other.data);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (this.length ^ (this.length >>> 32));
        hash = 53 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean hasExtent() {
        return false;
    }

    @Override
    public String code(IrSignal.Pass state, IrSignal.Pass pass, CodeGenerator codeGenerator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
