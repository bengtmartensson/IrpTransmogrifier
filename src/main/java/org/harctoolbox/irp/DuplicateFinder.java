/*
Copyright (C) 2018 Bengt Martensson.

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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.harctoolbox.ircore.ThisCannotHappenException;

public final class DuplicateFinder {
    private int counter;
    private final Map<String, DuplicateCollection> result;
    private final Iterable<Protocol> protocols;

    /**
     * Constructor
     * @param protocols
     * @param bitUsage
     * @throws NameUnassignedException Thrown if not all protocols contain all variables in bitUsage.
     */
    public DuplicateFinder(Iterable<Protocol> protocols, Map<String, BitCounter> bitUsage) throws NameUnassignedException {
        this.protocols = protocols;
        counter = 0;
        result = new LinkedHashMap<>(bitUsage.size());
        for (Map.Entry<String, BitCounter> kvp : bitUsage.entrySet()) {
            String name = kvp.getKey();
            DuplicateCollection duplicates = findDuplicates(name, kvp.getValue());
            result.put(name, duplicates);
        }
    }

    public DuplicateFinder(String varName, Iterable<Protocol> protocols) throws NameUnassignedException {
        this(protocols, BitCounter.scrutinizeProtocols(protocols));
    }

    private DuplicateCollection findDuplicates(String name, BitCounter bitCounter) throws NameUnassignedException {
        DuplicateCollection duplicates = new DuplicateCollection(bitCounter);
        int pos = bitCounter.getNumberBits() - 1;
        while (pos > 0) {
            if (bitCounter.getType(pos) == BitCounter.BitCounterType.varying) {
                DuplicateEntry duplicate = findDuplicate(name, bitCounter, pos);
                if (duplicate != null)
                    duplicates.add(duplicate);
            }
            pos--;
        }
        duplicates.combine();
        return duplicates;
    }

    private DuplicateEntry findDuplicate(String name, BitCounter bitCounter, int pos) throws NameUnassignedException {
        List<DuplicateEntry.Occurance> occurances = new ArrayList<>(8);
        occurances.add(new DuplicateEntry.Occurance(pos));

        for (int i = pos - 1; i >= 0; i--) {
            if (bitCounter.getType(i) == BitCounter.BitCounterType.varying) {

                CompareType cmp = compare(name, pos, i);
                switch (cmp) {
                    case identical:
                        occurances.add(new DuplicateEntry.Occurance(i, false));
                        break;
                    case inverted:
                        occurances.add(new DuplicateEntry.Occurance(i, true));
                        break;
                    default:
                    // No match
                }
            }
        }

        DuplicateEntry answer;
        if (occurances.size() > 1) {
            answer = new DuplicateEntry(1, occurances, counter);
            counter++;
        } else
            answer = null;
        return answer;
    }

    private CompareType compare(String name, int pos, int i) throws NameUnassignedException {
        boolean identical = true;
        boolean inverted = true;
        for (Protocol protocol : protocols) {
            boolean equal = compare(name, protocol, pos, i);
            identical = identical && equal;
            inverted = inverted && !equal;
            if (!identical && !inverted)
                return CompareType.none;
        }

        return identical ? CompareType.identical
                : inverted ? CompareType.inverted
                : CompareType.none;
    }

    private boolean compare(String name, Protocol protocol, int pos, int i) throws NameUnassignedException {
        Number x = protocol.getDefinitions().get(name).toNumber();
        return x.testBit(pos) == x.testBit(i);
    }

    public Map<String, DuplicateCollection> getDuplicates() {
        return Collections.unmodifiableMap(result);
    }

    private static enum CompareType {
        identical,
        inverted,
        none
    }

    public static class DuplicateEntry {
        private static String mkName(int counter) {
            return new String(new char[]{(char) ('a' + counter)});
        }

        private final String name;
        private int length;
        private final List<Occurance> occurances;

        public DuplicateEntry(int length, List<Occurance> occurances, int counter) {
            this.length = length;
            this.occurances = occurances;
            name = mkName(counter);
        }

        public int getFirstOccurance() {
            return occurances.get(0).position;
        }

        @Override
        public String toString() {
            StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
            occurances.forEach((occurence) -> {
                stringJoiner.add(occurence.toString());
            });
            return name + ":" + length + stringJoiner;
        }

        private String getName() {
            return name;
        }

        private String occuranceIsInvertedChar(int i) {
            return occuranceIsInverted(i) ? "~" : "";
        }

        private boolean occuranceIsInverted(int i) {
            for (Occurance occurance : occurances)
                if (occurance.position == i)
                    return occurance.inverted;

            throw new ThisCannotHappenException(); // programming error
        }

        public static class Occurance {
            public final int position;
            public final boolean inverted;

            public Occurance(int position) {
                this(position, false);
            }

            public Occurance(int position, boolean inverted) {
                this.position = position;
                this.inverted = inverted;
            }

            @Override
            public String toString() {
                return (inverted ? "~" : "") + Integer.toString(position);
            }
        }
    }

    public static class DuplicateCollection {

        private final List<DuplicateEntry> list;
        private final Map<Integer, DuplicateEntry> index;
        private final BitCounter bitCounter;

        private DuplicateCollection(BitCounter bitCounter) {
            this.bitCounter = bitCounter;
            list = new ArrayList<>(8);
            index = new HashMap<>(8);
        }

        public void add(DuplicateEntry duplicate) {
            if (!index.containsKey(duplicate.getFirstOccurance())) {
                list.add(duplicate);
                addToIndex(duplicate);
            }
        }

        private void addToIndex(DuplicateEntry duplicate) {
            duplicate.occurances.forEach((occurance) -> {
                index.put(occurance.position, duplicate);
            });
        }

        public DuplicateEntry getPosition(int n) {
            return index.get(n);
        }

        @Override
        public String toString() {
            return toString("");
        }

        public String toString(CharSequence delimiter) {
            StringJoiner str = new StringJoiner(delimiter);
            int i = bitCounter.getNumberBits() - 1;
            while (i >= 0) {
                DuplicateEntry de = index.get(i);
                if (de != null) {
                    str.add("(" + de.occuranceIsInvertedChar(i) + de.name + ":" + de.length + ")");
                    i -= de.length;
                } else {
                    str.add(bitCounter.getType(i).toString());
                    i--;
                }
            }
            return str.toString();
        }

        public List<Integer> getRecommendedParameterWidths() {
            List<Integer> result = new ArrayList<>(8);
            int pos = bitCounter.getNumberBits() - 1;
            boolean lastWasConstant = true;
            int current = 0;
            while (pos >= 0) {
                DuplicateEntry variable = index.get(pos);
                if (variable != null) {
                    if (current > 0)
                        result.add(current);
                    result.add(variable.length);
                    pos -= variable.length;
                    current = 0;
                    lastWasConstant = false;
                } else {
                    BitCounter.BitCounterType type = bitCounter.getType(pos);
                    if (type == BitCounter.BitCounterType.zero || type == BitCounter.BitCounterType.one) {
                        if (lastWasConstant) {
                            current++;
                        } else {
                            if (current > 0)
                                result.add(current);
                            current = 1;
                        }

                        lastWasConstant = true;
                    } else {
                        if (lastWasConstant) {
                            if (current > 0)
                                result.add(current);
                            current = 1;
                        } else {
                            current = 1;
                        }
                        lastWasConstant = false;
                    }
                    pos--;
                }
            }
            if (current > 0)
                result.add(current);

            return result;
        }

        public List<String> getNames() {
            List<String> names = new ArrayList<>(list.size());
            list.forEach((de) -> {
                names.add(de.getName());
            });
            return names;
        }

        private void combine() {
            List<DuplicateEntry> entries = new ArrayList<>(list);
            entries.stream().filter((entry) -> list.contains(entry)).forEachOrdered((entry) -> {
                int n = hasSameSuccessors(entry);
                if (n > 0) {
                    entry.length += n;
                    nuke(entry, n);
                }
            });

            rebuildIndex();
        }

        private int hasSameSuccessors(DuplicateEntry entry) {
            int length = 1;
            while (true) {
                for (DuplicateEntry.Occurance occurence : entry.occurances) {
                    boolean ok = hasSameSuccessor(entry, length, occurence);
                    if (!ok)
                        return length - 1;
                }
                length++;
            }
        }

        private boolean hasSameSuccessor(DuplicateEntry entry, int length, DuplicateEntry.Occurance occurence) {
            DuplicateEntry first = index.get(entry.getFirstOccurance() - length);
            DuplicateEntry second = index.get(occurence.position - length);
            return first != null ? first.equals(second) : false;
        }

        private void nuke(DuplicateEntry entry, int n) {
            for (int i = 1; i <= n; i++) {
                DuplicateEntry de = index.get(entry.getFirstOccurance() - i);
                list.remove(de);
            }
        }

        private void rebuildIndex() {
            index.clear();
            list.forEach((de) -> {
                addToIndex(de);
            });
        }

        String getRecommendedParameterWidthsAsString() {
            List<Integer> lst = getRecommendedParameterWidths();
            StringJoiner stringJoiner = new StringJoiner(",");
            lst.forEach((x) -> {
                stringJoiner.add(Integer.toString(x));
            });
            return stringJoiner.toString();
        }
    }
}
