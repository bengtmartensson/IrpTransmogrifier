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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 *
 */
public final class BitCounter {

    public static Map<String, BitCounter> scrutinizeProtocols(Iterable<Protocol> protocols) {
        LinkedHashMap<String, BitCounter> result = new LinkedHashMap<>(4);
        for (Protocol protocol : protocols) {
            NameEngine definitions = protocol.getDefinitions();
            for (Map.Entry<String, Expression> kvp : definitions) {
                String name = kvp.getKey();
                Number value = kvp.getValue().toNumber();
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

    public int getNumberBits() {
        return numberBits;
    }

    public BitCounterType getType(int n) {
        BitCounterType val = table.get(n);
        return val != null ? val : unassigned;
    }

    public String toString(CharSequence delimiter) {
        StringJoiner str = new StringJoiner(delimiter);
        for (int i = numberBits - 1; i >= 0; i--)
            str.add(getType(i).toString());
        return str.toString();
    }

    @Override
    public String toString() {
        return toString("");
    }

    public List<Integer> toIntSequence() {
        List<Integer> list = new ArrayList<>(16);
        boolean lastVarying = getType(numberBits - 1) == BitCounterType.varying;
        int length = 1;
        for (int i = numberBits - 2; i >=0; i--) {
            boolean varying = getType(i) == BitCounterType.varying;
            if (varying == lastVarying)
                length++;
            else {
                list.add(length);
                length = 1;
                lastVarying = varying;
            }
        }
        list.add(length);
        return list;
    }

    public String toIntSequenceString() {
        List<Integer> list = toIntSequence();
        StringJoiner stringJoiner = new StringJoiner(",");
        list.forEach(len -> {
            stringJoiner.add(Integer.toString(len));
        });
        return stringJoiner.toString();
    }

    public void aggregate(long x, int length) {
        long left = x;
        int bitNo = 0;
        while (left != 0 || bitNo < length) {
            table.put(bitNo, getType(bitNo).update((int) left & 1));
            left >>= 1;
            bitNo++;
        }
        numberBits = Math.max(numberBits, bitNo);
    }

    public void aggregate(Number x, int length) {
        Number left = x;
        int bitNo = 0;
        while (!left.isZero() || bitNo < length) {
            table.put(bitNo, getType(bitNo).update((int) left.and(1)));
            left = left.shiftRight(1);
            bitNo++;
        }
        numberBits = Math.max(numberBits, bitNo);
    }

    public void aggregate(long x) {
        aggregate(x, numberBits);
    }

    public void aggregate(Number x) {
        aggregate(x, numberBits);
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
