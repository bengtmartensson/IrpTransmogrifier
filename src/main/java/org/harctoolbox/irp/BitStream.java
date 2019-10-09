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

package org.harctoolbox.irp;

import java.math.BigInteger;
import java.util.Objects;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class BitStream extends IrpObject implements Evaluatable {

    private static final Logger logger = Logger.getLogger(BitStream.class.getName());
    private static final int WEIGHT = 2;

    private long length;
    private BigInteger data;

    BitStream() {
        super(null);
        data = BigInteger.ZERO;
        length = 0;
    }

    @SuppressWarnings("null")
    BitStream(BitField bitField, GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException {
        super(null);
        Objects.requireNonNull(bitField);
        if (bitField instanceof InfiniteBitField)
            throw new ThisCannotHappenException("Infinite bitfields cannot be converted to BitStreams.");

        data = BigInteger.valueOf((generalSpec != null && generalSpec.getBitDirection() == BitDirection.msb)
                    ? bitField.toLong(nameEngine)
                    : IrCoreUtils.reverse(bitField.toLong(nameEngine), (int) bitField.getWidth(nameEngine)));
        length = bitField.getWidth(nameEngine);

    }
    @Override
    public String toIrpString(int radix) {
        return "BitStream(" + data.toString(radix) + ":" + length + ")";
    }

    void add(BitStream bitStream, GeneralSpec generalSpec, NameEngine nameEngine) {
        data = data.shiftLeft((int)bitStream.length).or(bitStream.data);
        length += bitStream.length;
    }

    /** Extracts bit n*chunksize .. (n+1)*chunksize-1 */
    private int getChunkNo(int n, int chunksize) {
        if (n < 0 || (length > 0 && (n+1)*chunksize-1 >= length))
            throw new IndexOutOfBoundsException("Illegal bit " + n + " in getChunkNo");
        long mask = IrCoreUtils.ones(chunksize);
        return data.shiftRight(n*chunksize).and(BigInteger.valueOf(mask)).intValueExact();
    }

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine, BitSpec bitSpec) throws NameUnassignedException {
        EvaluatedIrStream list = new EvaluatedIrStream(nameEngine, generalSpec, pass);

        if (bitSpec == null || length % bitSpec.getChunkSize() != 0) {
            list.add(this);
        } else {
            int noChunks = ((int) length) / bitSpec.getChunkSize();
            for (int n = 0; n < noChunks; n++) {
                int chunkNo = noChunks - n - 1;
                BareIrStream irs = bitSpec.get(getChunkNo(chunkNo, bitSpec.getChunkSize()));
                EvaluatedIrStream evaluatedIrStream = irs.evaluate(state, pass, generalSpec, nameEngine);
                list.add(evaluatedIrStream);
            }
        }
        return list;
    }

    @Override
    public Element toElement(Document document) {
        throw new UnsupportedOperationException("Not supported.");
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
}
