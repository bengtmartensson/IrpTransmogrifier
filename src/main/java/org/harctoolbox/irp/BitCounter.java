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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BitCounter {

    public static Map<String, BitCounter> scrutinizeProtocols(Iterable<Protocol> protocols) throws NameUnassignedException {
        LinkedHashMap<String, BitCounter> result = new LinkedHashMap<>(4);
        for (Protocol protocol : protocols) {
            NameEngine definitions = protocol.getDefinitions();
            for (Map.Entry<String, Expression> kvp : definitions) {
                String name = kvp.getKey();
                long value = kvp.getValue().toLong();
                if (!result.containsKey(name)) {
                    Integer length = protocol.guessParameterLength(name);
                    result.put(name, length != null ? new BitCounter(length, false) : new BitCounter(true));
                }
                BitCounter bitCounter = result.get(name);
                bitCounter.aggregate(value);
            }
        }
        return result;
    }

    private final Map<Integer, BitCounterType> table;
    private int numberBits;
    private final BitCounterType unassigned;

    public BitCounter() {
        this(0, false);
    }


    public BitCounter(boolean unassignedIsZero) {
        this(0, unassignedIsZero);
    }

    public BitCounter(int length, boolean unassignedIsZero) {
        table = new HashMap<>(length);
        numberBits = length;
        this.unassigned = unassignedIsZero ? BitCounterType.zero : BitCounterType.virgin;
    }

    public BitCounter(int length) {
        this(length, false);
    }

    private BitCounterType getCount(int n) {
        return table.containsKey(n) ? table.get(n) : unassigned;
    }

    public List<String> toStringList() {
        List<String> result = new ArrayList<>(numberBits);
        for (int i = numberBits - 1; i >= 0; i--)
            result.add(getCount(i).toString());
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String separator) {
        return String.join(separator, toStringList());
    }

    public void aggregate(long x, int length) {
        long left = x;
        int bitNo = 0;
        while (left != 0 || bitNo < length) {
            table.put(bitNo, getCount(bitNo).update((int) left & 1));
            left >>= 1;
            bitNo++;
        }
        numberBits = Math.max(numberBits, bitNo);
    }

    public void aggregate(long x) {
        aggregate(x, numberBits);
    }

    public void aggregate(BigInteger x) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static enum BitCounterType {
        zero,
        one,
        varying,
        virgin;

        public BitCounterType update(int bit) {
            return this == zero ? (bit == 0 ? zero : varying)
                    : this == one ? (bit != 0 ? one : varying)
                    : this == varying ? varying
                    : (bit == 0 ? zero : one);
        }

        @Override
        public String toString() {
            return this == zero ? "0"
                    : this == one ? "1"
                    : this == virgin ? " "
                    : "*";
        }
    }
}
