/*
Copyright (C) 2017, 2018, 2019 Bengt Martensson.

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * This class makes a decoder of an IrpDatabase, or optionally a subset thereof.
 * (The subset is essentially to make debugging more comfortable.)
 * There are essentially two member functions, called {@code decode}, operating
 * on {@link org.harctoolbox.ircore.IrSignal} or {@link org.harctoolbox.ircore.ModulatedIrSequence}
 * respectively.
 * These have slightly different semantics.
 */
public final class Decoder {
    private static final Logger logger = Logger.getLogger(Decoder.class.getName());

    private static Pattern debugProtocolNamePattern= null;
    private final static int MAX_PREFER_OVER_NESTING = 10;

    /**
     * For debugging only.
     * @param regexp
     */
    public static void setDebugProtocolRegExp(String regexp) {
        debugProtocolNamePattern = (regexp == null || regexp.isEmpty()) ? null :  Pattern.compile(regexp.toLowerCase(Locale.US));
    }

    public static String getDebugProtocolRegExp() {
        return debugProtocolNamePattern != null ? debugProtocolNamePattern.pattern() : null;
    }

    /**
     * Allows to invoke the decoding from the command line.
     * @param args Pronto hex type IR signal.
     */
    public static void main(String[] args) {
        try {
            Decoder decoder = new Decoder();
            IrSignal irSignal = Pronto.parse(args);
            Map<String, Decode> decodes = decoder.decodeIrSignal(irSignal);
            decodes.values().forEach((kvp) -> {
                System.out.println(kvp);
            });
        } catch (IOException | Pronto.NonProntoFormatException | InvalidArgumentException | IrpParseException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private final Map<String, NamedProtocol> parsedProtocols;

    public Decoder(File irpDatabasePath) throws IOException, IrpParseException {
        this(new IrpDatabase(irpDatabasePath), null);
    }

    public Decoder(IrpDatabase irpDatabase) throws IrpParseException {
        this(irpDatabase, null);
    }

    Decoder() throws IrpParseException, IOException {
        this(new IrpDatabase((String) null));
    }

    /**
     * Mainly for testing and debugging.
     * @param names
     * @throws java.io.IOException
     * @throws org.harctoolbox.irp.IrpParseException
     */
    Decoder(String... names) throws IOException, IrpParseException {
        this(new IrpDatabase((String) null), Arrays.asList(names));
    }

    /**
     * This is the main constructor.
     * @param irpDatabase will be expanded.
     * @param names If non-null and non-empty, include only the protocols with these names.
     * @throws org.harctoolbox.irp.IrpParseException
     */
    public Decoder(IrpDatabase irpDatabase, List<String> names) throws IrpParseException {
        irpDatabase.expand();
        parsedProtocols = new LinkedHashMap<>(irpDatabase.size());
        Collection<String> list = names != null ? names : irpDatabase.getKeys();
        list.forEach((protocolName) -> {
            try {
                NamedProtocol namedProtocol = irpDatabase.getNamedProtocol(protocolName);
                if (namedProtocol.isDecodeable())
                    parsedProtocols.put(protocolName, namedProtocol);
            } catch (NameUnassignedException | UnknownProtocolException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                throw new ThisCannotHappenException(ex);
            }
        });
    }

    /**
     * Delivers a List of Map of Decodes from a ModulatedIrSequence.
     * @param irSequence
     * @param params
     * @return List of decodes.
     */
    public DecodeTree decode(ModulatedIrSequence irSequence, DecoderParameters params) {
        Map<Integer, Map<String, TrunkDecodeTree>> map = new ConcurrentHashMap<>(16);
        DecodeTree decodes = decode(irSequence, 0, params, 0, map);
        if (!decodes.isEmpty() || !params.isIgnoreLeadingGarbage())
            return decodes;

        int newStart = irSequence.firstBigGap(0, params.minimumLeadout) + 1;
        return newStart > 0 ? decode(irSequence, newStart, params, 0, map) : decodes;
    }

    private DecodeTree decode(ModulatedIrSequence irSequence, int position, DecoderParameters params, int level, Map<Integer, Map<String, TrunkDecodeTree>>map) {
        logger.log(Level.FINE, String.format("level = %1$d position = %2$d", level, position));
        DecodeTree list = new DecodeTree(irSequence.getLength() - position);
        if (list.length == 0)
            return list;

        parsedProtocols.values().parallelStream().forEach((namedProtocol) -> {
            try {
                if (debugProtocolNamePattern != null)
                    if (debugProtocolNamePattern.matcher(namedProtocol.getName().toLowerCase(Locale.US)).matches())
                        // This is intended to put a debugger breakpoint here
                        logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                TrunkDecodeTree decode;
                Map<String, TrunkDecodeTree> p = map.get(position);
                if (p != null && p.containsKey(namedProtocol.getName())) {
                    decode = p.get(namedProtocol.getName());
                } else {
                    decode = tryNamedProtocol(namedProtocol, irSequence, position, params, level, map);
                    if (!map.containsKey(position)) {
                        map.put(position, new ConcurrentHashMap<>(4));
                    }
                    map.get(position).put(namedProtocol.getName(), decode);
                }
                list.add(decode);
            } catch (SignalRecognitionException ex) {
                logger.log(Level.FINER, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
            }
        });

        if (!params.isAllDecodes()) {
            list.reduce();
            if (list.isComplete())
                list.removeIncompletes();
        }
        list.sort();
        return list;
    }

    private TrunkDecodeTree tryNamedProtocol(NamedProtocol namedProtocol, ModulatedIrSequence irSequence, int position, DecoderParameters params, int level, Map<Integer, Map<String, TrunkDecodeTree>>map)
            throws SignalRecognitionException, NamedProtocol.ProtocolNotDecodableException {
        Decode decode = namedProtocol.recognize(irSequence, position, params);
        if (params.isRemoveDefaultedParameters())
            decode.removeDefaulteds();
        if (!params.recursive || decode.endPos == irSequence.getLength() - 1)
            return new TrunkDecodeTree(decode, irSequence.getLength());

        DecodeTree rest = decode(irSequence, decode.getEndPos() + 1, params, level + 1, map);
        return new TrunkDecodeTree(decode, rest);
    }

    // Decoding an IrSignal is pretty different from decoding an IrSequence.
    // For example, the return type is completely different.
    // Therefore use different names; do not call all "decode".

    /**
     * Delivers a Map of Decodes from an IrSignal.
     * If {@code strict == true}, the intro, repeat, and ending sequences are required to match exactly.
     * Otherwise, the irSignal's intro may match a Protocol's repeat, and a missing ending sequence is considered
     * to match a Protocol's non-empty ending. on return, a dictionary (Map) is returned, containing all possible decodes of the given
     * input signal.
     * Unless {@code allDecodes == true}, decodes are eliminated from this list according to the
     * NamedProtocol's {@code prefer-over} property.
     * @param irSignal Input data
     * @param parameters
     * @return Map of decodes with protocol name as key.
     */
    public Map<String, Decode> decodeIrSignal(IrSignal irSignal, DecoderParameters parameters) {
        Map<String, Decode> decodes = new HashMap<>(8);
        parsedProtocols.values().forEach((NamedProtocol namedProtocol) -> {
            try {
                if (debugProtocolNamePattern != null)
                    if (debugProtocolNamePattern.matcher(namedProtocol.getName().toLowerCase(Locale.US)).matches())
                        // This is intended to put a debugger breakpoint here
                        logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                Map<String, Long> params = namedProtocol.recognize(irSignal, parameters);
                if (parameters.isRemoveDefaultedParameters())
                    namedProtocol.removeDefaulteds(params);
                Decode decode = new Decode(namedProtocol, params);
                decodes.put(namedProtocol.getName(), decode);
            } catch (/*DomainViolationException |*/ SignalRecognitionException ex) {
                logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
                throw new ThisCannotHappenException();
            }
        });


        if (!parameters.isAllDecodes())
            reduce(decodes);
        return decodes;
    }

    public Map<String, Decode> decodeIrSignal(IrSignal irSignal) {
        return decodeIrSignal(irSignal, new DecoderParameters());
    }

    private void reduce(Map<String, Decode> decodes) {
        List<Decode> decs = new ArrayList<>(decodes.values());
        decs.forEach((Decode decode) -> {
            if (decode != null) {
                NamedProtocol prot = decode.getNamedProtocol();
                if (prot != null) // can it happen that prot == null?
                    reduce(decodes, prot.getPreferOver(), 0);
            }
        });
    }

    private void reduce(Map<String, Decode> decodes, List<String> toBeRemoved, int level) {
        if (level > MAX_PREFER_OVER_NESTING) {
            logger.severe("Max prefer-over depth reached, cycle likely. Please report.");
            return;
        }
        if (toBeRemoved == null)
            return;

        toBeRemoved.forEach((protName) -> {
            NamedProtocol p = parsedProtocols.get(protName.toLowerCase(Locale.US));
            if (p != null)
                reduce(decodes, p.getPreferOver(), level+1);

            decodes.remove(protName);
        });
    }

    /**
     * Basically for testing; therefore package private.
     * @return the parsedProtocols
     */
    Collection<NamedProtocol> getParsedProtocols() {
        return parsedProtocols.values();
    }

    public static class DecoderParameters {

        private boolean strict;
        private boolean allDecodes;
        private boolean removeDefaultedParameters;
        private boolean recursive;
        private Double frequencyTolerance;
        private Double absoluteTolerance;
        private Double relativeTolerance;
        private Double minimumLeadout;
        private boolean override;
        private boolean ignoreLeadingGarbage;
        /**
         *
         * @param strict If true, intro-, repeat-, and ending sequences are
         * required to match exactly.
         * @param allDecodes If true, output all possible decodes. Otherwise,
         * remove decodes according to prefer-over.
         * @param removeDefaultedParameters If true, remove parameters with
         * value equals to their default.
         * @param recursive
         * @param frequencyTolerance
         * @param absoluteTolerance
         * @param relativeTolerance
         * @param minimumLeadout
         * @param override If true, the given parameters override parameter specific parameter values.
         * @param ignoreLeadingGarbage
         */
        public DecoderParameters(boolean strict, boolean allDecodes, boolean removeDefaultedParameters, boolean recursive,
                Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout,
                boolean override, boolean ignoreLeadingGarbage) {
            this.strict = strict;
            this.allDecodes = allDecodes;
            this.removeDefaultedParameters = removeDefaultedParameters;
            this.recursive = recursive;
            this.frequencyTolerance = IrCoreUtils.getFrequencyTolerance(frequencyTolerance);
            this.absoluteTolerance = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
            this.relativeTolerance = IrCoreUtils.getRelativeTolerance(relativeTolerance);
            this.minimumLeadout = IrCoreUtils.getMinimumLeadout(minimumLeadout);
            this.override = override;
            this.ignoreLeadingGarbage = ignoreLeadingGarbage;
        }

        public DecoderParameters() {
            this(false/*, false, true, false, null, null, null, null, true*/);
        }

        public DecoderParameters(boolean strict) {
            this(strict, false, true, false, null, null, null, null, false, false);
        }

        public DecoderParameters(boolean strict, Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout) {
            this(strict, false, true, false, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false, false);
        }

        public DecoderParameters adjust(boolean newStrict, Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout) {
            DecoderParameters copy = new DecoderParameters(strict || newStrict, allDecodes, removeDefaultedParameters, recursive,
                    pick(frequencyTolerance, this.frequencyTolerance, override),
                    pick(absoluteTolerance, this.absoluteTolerance, override),
                    pick(relativeTolerance, this.relativeTolerance, override),
                    pick(minimumLeadout, this.minimumLeadout, override),
                    override, ignoreLeadingGarbage);

            return copy;
        }

        private Double pick(Double standard, Double user, boolean override) {
            return ((override && user != null) || standard == null) ? user : standard;
        }

        /**
         * @return the strict
         */
        public boolean isStrict() {
            return strict;
        }

        /**
         * @return the allDecodes
         */
        public boolean isAllDecodes() {
            return allDecodes;
        }

        /**
         * @return the removeDefaultedParameters
         */
        public boolean isRemoveDefaultedParameters() {
            return removeDefaultedParameters;
        }

        /**
         * @return the recursive
         */
        public boolean isRecursive() {
            return recursive;
        }

        /**
         * @return the frequencyTolerance
         */
        public Double getFrequencyTolerance() {
            return frequencyTolerance;
        }

        /**
         * @return the absoluteTolerance
         */
        public Double getAbsoluteTolerance() {
            return absoluteTolerance;
        }

        /**
         * @return the relativeTolerance
         */
        public Double getRelativeTolerance() {
            return relativeTolerance;
        }

        /**
         * @return the minimumLeadout
         */
        public Double getMinimumLeadout() {
            return minimumLeadout;
        }

        /**
         * @return the override
         */
        public boolean isOverride() {
            return override;
        }

        /**
         * @param strict the strict to set
         */
        public void setStrict(boolean strict) {
            this.strict = strict;
        }

        /**
         * @param allDecodes the allDecodes to set
         */
        public void setAllDecodes(boolean allDecodes) {
            this.allDecodes = allDecodes;
        }

        /**
         * @param removeDefaultedParameters the removeDefaultedParameters to set
         */
        public void setRemoveDefaultedParameters(boolean removeDefaultedParameters) {
            this.removeDefaultedParameters = removeDefaultedParameters;
        }

        /**
         * @param recursive the recursive to set
         */
        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        /**
         * @param frequencyTolerance the frequencyTolerance to set
         */
        public void setFrequencyTolerance(Double frequencyTolerance) {
            this.frequencyTolerance = frequencyTolerance;
        }

        /**
         * @param absoluteTolerance the absoluteTolerance to set
         */
        public void setAbsoluteTolerance(Double absoluteTolerance) {
            this.absoluteTolerance = absoluteTolerance;
        }

        /**
         * @param relativeTolerance the relativeTolerance to set
         */
        public void setRelativeTolerance(Double relativeTolerance) {
            this.relativeTolerance = relativeTolerance;
        }

        /**
         * @param minimumLeadout the minimumLeadout to set
         */
        public void setMinimumLeadout(Double minimumLeadout) {
            this.minimumLeadout = minimumLeadout;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        public void setIgnoreLeadingGarbage(boolean ignoreLeadingGarbage) {
            this.ignoreLeadingGarbage = ignoreLeadingGarbage;
        }

        private boolean isIgnoreLeadingGarbage() {
            return ignoreLeadingGarbage;
        }
    }

    // "Note: this class has a natural ordering that is inconsistent with equals."
    public static class DecodeTree implements Iterable<TrunkDecodeTree>, Comparable<DecodeTree> {
        private final List<TrunkDecodeTree> decodes;
        private int length;

        private DecodeTree(int length) {
            this.length = length;
            decodes = Collections.synchronizedList(new ArrayList<>(4));
        }

        public String toString(int radix, String separator) {
            if (isVoid())
                return "";
            StringJoiner stringJoiner = new StringJoiner(separator, "{", "}");
            if (decodes.isEmpty())
                stringJoiner.add("UNDECODED. length=" + Integer.toString(length, 10));
            synchronized(decodes) {
            decodes.forEach((decode) -> {
                stringJoiner.add(decode.toString(radix, separator));
            });
            }

            return stringJoiner.toString();
        }

        @Override
        public String toString() {
            return toString(10, ", ");
        }

        private void add(TrunkDecodeTree decode) {
            decodes.add(decode);
        }

        public boolean isEmpty() {
            return decodes.isEmpty();
        }

        boolean isVoid() {
            return decodes.isEmpty() && length == 0;
        }

        public synchronized void sort() {
            Collections.sort(decodes);
        }

        // Presently, this does not remove protocols transitively,
        // like in the IrRemote case
        private synchronized void reduce() {
            List<TrunkDecodeTree> decs = new ArrayList<>(decodes);
            decs.forEach((TrunkDecodeTree decode) -> {
                if (decode != null) {
                    NamedProtocol prot = decode.getNamedProtocol();
                    if (prot != null) {
                        List<String> preferOvers = prot.getPreferOver();
                        if (preferOvers != null) {
                            preferOvers.forEach((protName) -> {
                                remove(protName);
                            });
                        }
                    }
                }
            });
        }

        private synchronized void removeIncompletes() {
            List<TrunkDecodeTree> decs = new ArrayList<>(decodes);
            decs.forEach((TrunkDecodeTree decode) -> {
                if (decode != null && !decode.isComplete())
                    decodes.remove(decode);
            });
        }

        private synchronized TrunkDecodeTree findName(String protocol) {
            for (TrunkDecodeTree decode : decodes)
                if (decode.getNamedProtocol().getName().equals(protocol))
                    return decode;

            return null;
        }

        private synchronized void remove(String protName) {
            TrunkDecodeTree decode = findName(protName);
            if (decode != null)
                decodes.remove(decode);
        }

        int size() {
            return decodes.size();
        }

        synchronized TrunkDecodeTree getAlternative(String protocolName) {
            for (TrunkDecodeTree decode : decodes)
                if (decode.trunk.getName().equals(protocolName))
                    return decode;

            return null;
        }

        Decode getDecode(int i) {
            return decodes.get(i).trunk;
        }

        DecodeTree getRest(int i) {
            return decodes.get(i).rest;
        }

        TrunkDecodeTree getAlternative(int i) {
            return decodes.get(i);
        }

        @Override
        public Iterator<TrunkDecodeTree> iterator() {
            return decodes.iterator();
        }

        private boolean isComplete() {
            if (length == 0)
                return true;

            return decodes.stream().anyMatch((d) -> (d.isComplete()));
        }

        @Override
        public int compareTo(DecodeTree o) {
            int min = Math.min(decodes.size(), o.decodes.size());
            for (int i = 0; i < min; i++) {
                int c = decodes.get(i).compareTo(o.decodes.get(i));
                if (c != 0)
                    return c;
            }
            return decodes.size() - o.decodes.size();
        }
    }

    // "Note: this class has a natural ordering that is inconsistent with equals."
    public static class TrunkDecodeTree implements Comparable<TrunkDecodeTree> {
        private Decode trunk;
        private DecodeTree rest;

        private TrunkDecodeTree(Decode decode, int totalLenght) {
            this(decode, new DecodeTree(totalLenght - (decode.getEndPos() /*- decode.begPos*/ + 1)));
        }

        private TrunkDecodeTree(Decode decode, DecodeTree rest) {
            trunk = decode;
            this.rest = rest;
        }

        @Override
        public String toString() {
            return toString(10, " ");
        }

        public String toString(int radix, String separator) {
            StringJoiner stringJoiner = new StringJoiner(separator);
            stringJoiner.add(trunk.toString(radix, separator));
            if (!rest.isVoid())
                stringJoiner.add(rest.toString(radix, separator));
            return stringJoiner.toString();
        }

        private NamedProtocol getNamedProtocol() {
            return trunk.getNamedProtocol();
        }

        public String getName() {
            return trunk.getName();
        }

        public Decode getTrunk() {
            return trunk;
        }

        public DecodeTree getRest() {
            return rest;
        }

        boolean isEmpty() {
            return trunk == null; // ???
        }

        private boolean isComplete() {
            return rest.isComplete();
        }

        @Override
        public int compareTo(TrunkDecodeTree trunkDecodeTree) {
            int c = trunk.compareTo(trunkDecodeTree.trunk);
            return c != 0 ? c : rest.compareTo(trunkDecodeTree.rest);
        }
    }

    // "Note: this class has a natural ordering that is inconsistent with equals."
    public static class Decode implements Comparable<Decode> {
        private final NamedProtocol namedProtocol;
        //private final NameEngine nameEngine;
        private final Map<String, Long> map;
        private final int begPos;
        private final int endPos;
        private final int numberOfRepetitions;

        public Decode(NamedProtocol namedProtocol, Map<String, Long> map, int begPos, int endPos, int numberOfRepetitions) {
            this.namedProtocol = namedProtocol;
            this.map = map;
            this.begPos = begPos;
            this.endPos = endPos;
            this.numberOfRepetitions = numberOfRepetitions;
}

        Decode(NamedProtocol namedProtocol, Map<String, Long> params) {
            this(namedProtocol, params, -1, -1, 0);
        }

        Decode(NamedProtocol namedProtocol, Decode decode) {
            this.namedProtocol = namedProtocol;
            this.map = decode.map;
            //this.notes = notes == null ? "" : notes;
            this.begPos = decode.begPos;
            this.endPos = decode.endPos;
            this.numberOfRepetitions = decode.numberOfRepetitions;
        }

        public boolean same(String protocolName, NameEngine nameEngine) {
            return namedProtocol.getName().equalsIgnoreCase(protocolName)
                    && nameEngine.numericallyEquals(nameEngine);
        }

        @Override
        public String toString() {
            return toString(10, " ");
        }

        public String toString(int radix, String separator) {
            return namedProtocol.getName() + ": " + mapToString(radix)
                    + (begPos != -1 ? ("," + separator + "beg=" + begPos) : "")
                    + (endPos != -1 ? (", end=" + endPos) : "")
                    + (numberOfRepetitions != 0 ? (", reps=" + numberOfRepetitions) : "");
        }

        /**
         * @return the namedProtocol
         */
        public NamedProtocol getNamedProtocol() {
            return namedProtocol;
        }

        public String getName() {
            return namedProtocol.getName();
        }

        /**
         * @return the begPos
         */
        int getBegPos() {
            return begPos;
        }

        /**
         * @return the endPos
         */
        int getEndPos() {
            return endPos;
        }

        /**
         * @return the numberOfRepetitions
         */
        int getNumberOfRepetitions() {
            return numberOfRepetitions;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Map<String, Long> getMap() {
            return map;
        }

        private void removeDefaulteds() {
            namedProtocol.removeDefaulteds(map);
        }

        private String mapToString(int radix) {
            if (map.isEmpty())
                return "";
            StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
            map.keySet().stream().sorted().forEach((key) -> {
                stringJoiner.add(key + "=" + Long.toString(map.get(key), radix));
            });
            return stringJoiner.toString();
        }

        @Override
        public int compareTo(Decode o) {
            return namedProtocol.compareTo(o.namedProtocol);
        }
    }
}
