/*
Copyright (C) 2011, 2013, 2016 Bengt Martensson.

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
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class BitStream extends IrStreamItem implements Evaluatable {

    private static final Logger logger = Logger.getLogger(BitStream.class.getName());

    private long length;
    private BigInteger data;

    @Override
    public String toString() {
        return "BitStream(" + data + "=0x" + data.toString(16) + "=0b" + data.toString(2) + ":" + length + ")";
    }

//        else {
//            StringBuilder dataString = new StringBuilder();
//            dataString.append("[ ");
//            StringBuilder binString = new StringBuilder();
//            for (int i = data.length - 1; i >= 0; i--) {
//                dataString.append(Long.toString(data[i])).append(" ");
//                binString.append(Long.toBinaryString(data[i])).append(" ");
//            }
//            return "BitStream, length = " + length + ", data = " + dataString + "] = " + binString;
//    }

    @Override
    public String toIrpString() {
        throw new UnsupportedOperationException("Not supported.");
    }

    BitStream() {
        data = BigInteger.ZERO;
        length = 0;
    }

    BitStream(BitField bitField, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        if (bitField instanceof InfiniteBitField)
            throw new IncompatibleArgumentException("Infinite bitfields cannot be converted to BitStreams.");

        data = BigInteger.valueOf(generalSpec.getBitDirection() == BitDirection.msb
                    ? bitField.toNumber(nameEngine)
                    : IrpUtils.reverse(bitField.toNumber(nameEngine), (int) bitField.getWidth(nameEngine)));
        length = bitField.getWidth(nameEngine);

    }

//    public BitStream(Protocol env) {
//        super(env);
//        data = new long[1];
//        data[0] = 0L;
//        length = 0;
//    }

//    public void add(BitField bitField, NameEngine nameEngine) throws IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
//        add(bitField, environment.getBitDirection(), nameEngine);
//    }

    void add(BitStream bitStream, NameEngine nameEngine, GeneralSpec generalSpec) {
        data = data.shiftLeft((int)bitStream.length).or(bitStream.data);
        length += bitStream.length;
    }

//    public void add(BitField bitField, NameEngine nameEngine, GeneralSpec generalSpec)
//            throws IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
//        if (bitField instanceof InfiniteBitField)
//            throw new IncompatibleArgumentException("Infinite bitfields cannot be converted to bitstreams.");
//
//        long newData = generalSpec.getBitDirection() == BitDirection.msb
//                    ? bitField.toNumber(nameEngine)
//                    : IrpUtils.reverse(bitField.toNumber(nameEngine), (int) bitField.getWidth(nameEngine));
//        length += bitField.getWidth(nameEngine);
//
//        if (length > Long.SIZE) {
//            // "abnormal" case
//            if (longsNeeded(length) > data.length) {
//                // need to extend
//                long[] newdata = new long[data.length + 1];
//                System.arraycopy(data, 0, newdata, 0, data.length);
//                newdata[data.length] = 0L;
//                data = newdata;
//            }
//            for (int i = data.length - 1; i > 0; i--) {
//                long x = data[i] << bitField.getWidth(nameEngine) | getLeftmostBits(data[i-1], (int) bitField.getWidth(nameEngine));
//                data[i] = x;
//            }
//        }
//        data[0] = data[0] << bitField.getWidth(nameEngine) | newData;
//    }

//    private long getLeftmostBits(long x, int n) {
//        return x >> (Long.SIZE - n) & ((1L << n) - 1L);
//    }

    // really ceal(n/Long.Size)
//    private int longsNeeded(int n) {
//        return n/Long.SIZE + ((n % Long.SIZE == 0) ? 0 : 1);
//    }

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
        //debugBegin();
        //if (bitSpec == null)
        //    throw new UnassignedException("BitStream " + toString() + " has no associated BitSpec, cannot compute IrStream");

        IrpUtils.entering(logger, "evaluate", this);

        EvaluatedIrStream list = new EvaluatedIrStream(nameEngine, generalSpec, pass);

        if (bitSpec == null || length % bitSpec.getChunkSize() != 0) {
            list.add(this);
            //throw new IncompatibleArgumentException("chunksize (= " + bitSpec.getChunkSize() + ") does not divide bitstream length (= " + length + ").");
        } else {
            int noChunks = ((int) length) / bitSpec.getChunkSize();
            for (int n = 0; n < noChunks; n++) {
                int chunkNo = noChunks - n - 1;
                BareIrStream irs = bitSpec.get(getChunkNo(chunkNo, bitSpec.getChunkSize()));
                EvaluatedIrStream evaluatedIrStream = irs.evaluate(state, pass, nameEngine, generalSpec);
                //evaluatedIrStream.reduce(bitSpec);
                //List<IrStreamItem> items = irs.evaluate(null);
                //list.addAll(items);
                list.add(evaluatedIrStream);
            }
        }
        //Debug.debugBitStream(toString());
        //debugEnd(list);
        IrpUtils.exiting(logger, "evaluate", list);
        return list;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return length == 0;
    }

    @Override
    public Element toElement(Document document) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    boolean interleavingOk() {
        return true;
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
}
