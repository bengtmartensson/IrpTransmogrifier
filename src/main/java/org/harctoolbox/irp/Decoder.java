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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.xml.sax.SAXException;

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
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        try {
            Decoder decoder = new Decoder();
            IrSignal irSignal = Pronto.parse(args);
            SimpleDecodesSet decodes = decoder.decodeIrSignal(irSignal);
            for (Decode decode : decodes)
                System.out.println(decode);
        } catch (IOException | Pronto.NonProntoFormatException | InvalidArgumentException | IrpParseException | SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private final Map<String, NamedProtocol> parsedProtocols;

    private Decoder(File irpDatabasePath) throws IOException, IrpParseException, SAXException {
        this(new IrpDatabase(irpDatabasePath), null);
    }

    public Decoder(IrpDatabase irpDatabase) throws IrpParseException {
        this(irpDatabase, null);
    }

    public Decoder() throws IrpParseException, IOException, SAXException {
        this(new IrpDatabase((String) null));
    }

    /**
     * Mainly for testing and debugging.
     * @param names
     * @throws java.io.IOException
     * @throws org.harctoolbox.irp.IrpParseException
     * @throws org.xml.sax.SAXException
     */
    public Decoder(String... names) throws IOException, IrpParseException, SAXException {
        this(new IrpDatabase((String) null), Arrays.asList(names));
    }

    /**
     * This is the main constructor.
     * @param irpDatabase will be expanded.
     * @param names If non-null and non-empty, include only the protocols with these names.
     * @throws org.harctoolbox.irp.IrpParseException
     */
    public Decoder(IrpDatabase irpDatabase, List<String> names) throws IrpParseException {
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
        Map<Integer, Map<String, TrunkDecodeTree>> map = new HashMap<>(16);
        DecodeTree decodes = decode(irSequence, 0, params, 0, map);
        if (decodes.isEmpty() && params.isIgnoreLeadingGarbage()) {
            int newStart = irSequence.firstBigGap(0, params.minimumLeadout) + 1;
            return newStart > 0 ? decode(irSequence, newStart, params, 0, map) : decodes;
        } else
            return decodes;
    }

    private DecodeTree decode(ModulatedIrSequence irSequence, int position, DecoderParameters params, int level, Map<Integer, Map<String, TrunkDecodeTree>>map) {
        logger.log(Level.FINE, String.format("level = %1$d position = %2$d", level, position));
        DecodeTree decodeTree = new DecodeTree(irSequence.getLength() - position);
        if (decodeTree.length == 0)
            return decodeTree;

        parsedProtocols.values().forEach((namedProtocol) -> {
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
                        map.put(position, new HashMap<>(4));
                    }
                    map.get(position).put(namedProtocol.getName(), decode);
                }
                decodeTree.add(decode);
            } catch (SignalRecognitionException ex) {
                logger.log(Level.FINER, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
            }
        });

        if (!params.isAllDecodes()) {
            decodeTree.reduce(parsedProtocols);
            if (decodeTree.isComplete())
                decodeTree.removeIncompletes();
        }
        decodeTree.sort();
        return decodeTree;
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
    public SimpleDecodesSet decodeIrSignal(IrSignal irSignal, DecoderParameters parameters) {
        List<Decode> decodes = new ArrayList<>(8);
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
                decodes.add(decode);
            } catch (/*DomainViolationException |*/ SignalRecognitionException ex) {
                logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
                throw new ThisCannotHappenException();
            }
        });
        SimpleDecodesSet simpleDecodesSet = new SimpleDecodesSet(decodes);


        if (!parameters.isAllDecodes())
            simpleDecodesSet.reduce(parsedProtocols);
        simpleDecodesSet.sort();
        return simpleDecodesSet;
    }

    public SimpleDecodesSet decodeIrSignal(IrSignal irSignal) {
        return decodeIrSignal(irSignal, new DecoderParameters());
    }

    /**
     * Basically for testing; therefore package private.
     * @return the parsedProtocols
     */
    Collection<NamedProtocol> getParsedProtocols() {
        return parsedProtocols.values();
    }

    public AbstractDecodesCollection<? extends ElementaryDecode> decodeLoose(IrSignal irSignal, DecoderParameters decoderParams) {
        if (decoderParams.ignoreLeadingGarbage || (!decoderParams.strict && (irSignal.introOnly() || irSignal.repeatOnly()))) {
            ModulatedIrSequence sequence = irSignal.toModulatedIrSequence();
            return decode(sequence, decoderParams);
        } else
            return decodeIrSignal(irSignal, decoderParams);
    }

    public static final class DecoderParameters {

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

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            sj.add(Boolean.toString(strict));
            sj.add(Boolean.toString(allDecodes));
            sj.add(Boolean.toString(removeDefaultedParameters));
            sj.add(Boolean.toString(recursive));
            sj.add(Double.toString(frequencyTolerance));
            sj.add(Double.toString(absoluteTolerance));
            sj.add(Double.toString(relativeTolerance));
            sj.add(Double.toString(minimumLeadout));
            sj.add(Boolean.toString(override));
            sj.add(Boolean.toString(ignoreLeadingGarbage));
            return sj.toString();
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

    public static abstract class AbstractDecodesCollection<T extends ElementaryDecode> implements Iterable<T> {

        protected Map<String, T> map;

        AbstractDecodesCollection(Iterable<T> iterable, int size) {
            map = new LinkedHashMap<>(size);
            iterable.forEach((e) -> {
                add(e);
            });
        }

        AbstractDecodesCollection(List<T> list) {
            this(list, list.size());
        }

        AbstractDecodesCollection(Map<String, T>map) {
            this.map = new HashMap<>(map);
        }

        @Override
        public Iterator<T> iterator() {
            return map.values().iterator();
        }

        /**
         * Manipulates its argument, removing some decodes.
         *
         * @param parsedProtocols
         */
        void reduce(Map<String, NamedProtocol> parsedProtocols) {

            Map<String, T> old = new HashMap<>(map);
            for (Map.Entry<String, T> kvp : old.entrySet())
                if (toBeRemoved(kvp.getValue(), parsedProtocols))
                    map.remove(kvp.getKey());
        }

        /**
         * Returns true if any of the NamedProtocols in the second argument is prefered-over the first.
         * @param removeCandidate
         * @param parsedProtocols
         * @return
         */
        private boolean toBeRemoved(T removeCandidate, Map<String, NamedProtocol> parsedProtocols) {
            for (T remover : map.values()) {
                if (!remover.equals(removeCandidate))
                    if (toBeRemoved(removeCandidate, remover, parsedProtocols, 0))
                        return true;
            }
            return false;
        }

        private boolean toBeRemoved(T removeCandidate, HasPreferOvers remover, Map<String, NamedProtocol> parsedProtocols, int level) {
            if (level > MAX_PREFER_OVER_NESTING) {
                logger.log(Level.SEVERE, "Max prefer-over depth reached using protocol {0}, cycle likely. Please report.", remover.getName());
                return false;
            }

            logger.log(Level.FINEST, "Is {0} to be removed from {1}, level {2}", new Object[]{removeCandidate.getName(), remover.getName(), level});
            Set<String> preferOvers = remover.getPreferOverNames();
            boolean remove = preferOvers.contains(removeCandidate.getName());
            if (remove) {
                logger.log(Level.FINE, "Decode {0} removed by {1}",
                        new Object[]{removeCandidate.getName(), remover.getName()});
                return true;
            } else {
                for (String preferOver : preferOvers) {
                    T t = this.map.get(preferOver);
                    HasPreferOvers hpo = t != null ? t.getDecode() : parsedProtocols.get(preferOver.toLowerCase(Locale.US));
                    boolean success = toBeRemoved(removeCandidate, hpo, parsedProtocols, level + 1);
                    if (success)
                        return true;
                }
            }
            return false;
        }

        public void add(T decode) {
            map.put(decode.getName(), decode);
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public T get(String name) {
            return map.get(name);
        }

        public boolean contains(String name) {
            return map.containsKey(name);
        }

        public void remove(String protName) {
            if (protName != null && map.containsKey(protName)) {
                map.remove(protName);
                //logger.log(Level.FINE, "Protocol {0} removed by prefer-over from {1}.", new Object[]{decode.getName(), prot.getName()});
            }
        }

        public void remove(T decode) {
            remove(decode.getName());
        }

        public int size() {
            return map.size();
        }

        public final void sort() {
            List<T> list = sortedValues();
            map.clear();
            list.forEach((d) -> {
                add(d);
            });
        }

        abstract List<T> sortedValues();

        public T first() {
            Iterator<T> it = iterator();
            return it.hasNext() ? it.next() : null;
        }

        public void println(PrintStream out, int radix, String separator, boolean quiet) {
            if (isEmpty())
                printNoDecodes(out, quiet);
            map.values().forEach((T decode) -> {
                out.println("\t" + decode.toString(radix, separator));
            });
        }

        protected void printNoDecodes(PrintStream out, boolean quiet) {
            out.println();
        }
    }

    public static final class SimpleDecodesSet extends AbstractDecodesCollection<Decode> {

        public SimpleDecodesSet(List<Decode> list) {
            super(list);
        }

        /**
         * Creates a SimpleDecodeSet from a DecodeTree.  Note: This often throws away a lot of information.
         * @param decodeTree
         */
        public SimpleDecodesSet(DecodeTree decodeTree) {
            this(decodeTree.toList());
        }

        @Override
        List<Decode> sortedValues() {
            List<Decode> list = new ArrayList<>(map.values());
            Collections.sort(list);
            return list;
        }
    }

    public static final class DecodeTree extends AbstractDecodesCollection<TrunkDecodeTree> implements Comparable<DecodeTree> {
        private int length;

        private DecodeTree(int length) {
            super(new ArrayList<TrunkDecodeTree>(8));
            this.length = length;
        }

        private DecodeTree(DecodeTree old) {
            super(old.map);
            this.length = old.length;
        }

        public String toString(int radix, String separator) {
            if (isVoid())
                return "";
            StringJoiner stringJoiner = new StringJoiner(separator, "{", "}");
            if (map.isEmpty())
                stringJoiner.add("UNDECODED. length=" + Integer.toString(length, 10));

            map.values().forEach((decode) -> {
                stringJoiner.add(decode.toString(radix, separator));
            });

            return stringJoiner.toString();
        }

        /**
         * Creates a List of the Trunks of a DecodeTree. Note: This often throws away a lot of information.
         * @return
         */
        public List<Decode> toList() {
            List<Decode> result = new ArrayList<>(size());
            map.values().forEach((TrunkDecodeTree tdt) -> {
                result.add(tdt.getTrunk());
            });
            return result;
        }

        @Override
        public String toString() {
            return toString(10, ", ");
        }

        private void removeIncompletes() {
            DecodeTree old = new DecodeTree(this);
            for (TrunkDecodeTree decode : old) {
                 if (!decode.isComplete())
                    remove(decode);
            }
        }

        boolean isVoid() {
            return isEmpty() && length == 0;
        }

        TrunkDecodeTree getAlternative(String protocolName) {
            return get(protocolName);
        }

        private boolean isComplete() {
            if (length == 0)
                return true;

            return map.values().stream().anyMatch((d) -> (d.isComplete()));
        }

        @Override
        public int compareTo(DecodeTree o) {
            Iterator<TrunkDecodeTree> it = this.iterator();
            Iterator<TrunkDecodeTree> jt = o.iterator();
            for (;it.hasNext() && jt.hasNext();) {
                TrunkDecodeTree d = it.next();
                TrunkDecodeTree od = jt.next();
                int c = d.compareTo(od);
                if (c != 0)
                    return c;
            }
            return size() - o.size();
        }

        @Override
        public int hashCode() {
            int hash = 5 + super.hashCode();
            hash = 83 * hash + this.length;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return compareTo((DecodeTree) obj) == 0;
        }

        @Override
        List<TrunkDecodeTree> sortedValues() {
            ArrayList<TrunkDecodeTree> list = new ArrayList<>(map.values());
            Collections.sort(list);
            return list;
        }

        @Override
        protected void printNoDecodes(PrintStream out, boolean quiet) {
            out.println(quiet ? "" : "No decodes.");
        }
    }

    public static final class TrunkDecodeTree implements ElementaryDecode, Comparable<TrunkDecodeTree> {
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

        @Override
        public String toString(int radix, String separator) {
            StringJoiner stringJoiner = new StringJoiner(separator);
            stringJoiner.add(trunk.toString(radix, separator));
            if (!rest.isVoid())
                stringJoiner.add(rest.toString(radix, separator));
            return stringJoiner.toString();
        }

        @Override
        public String getName() {
            return trunk.getName();
        }

        @Override
        public Map<String, Long> getMap() {
            return trunk.map;
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return compareTo((TrunkDecodeTree) obj) == 0;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.trunk);
            hash = 47 * hash + Objects.hashCode(this.rest);
            return hash;
        }

        @Override
        public Set<String> getPreferOverNames() {
            return trunk.getPreferOverNames();
        }

        @Override
        public Decode getDecode() {
            return trunk;
        }
    }

    public static final class Decode implements ElementaryDecode, Comparable<Decode> {
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

        @Override
        public String toString(int radix, String separator) {
            return namedProtocol.getName() + ": " + mapToString(radix)
                    + (begPos != -1 ? ("," + separator + "beg=" + begPos) : "")
                    + (endPos != -1 ? (", end=" + endPos) : "")
                    + (numberOfRepetitions != 0 ? (", reps=" + numberOfRepetitions) : "");
        }

        @Override
        public Set<String> getPreferOverNames() {
            List<PreferOver> preferOvers = namedProtocol.getPreferOver();
            Set<String> result = new HashSet<>(preferOvers.size());

            for (PreferOver preferOver : preferOvers) {
                String removee = preferOver.toBeRemoved(map);
                if (removee != null)
                    result.add(removee);
            }
            return result;
        }

        /**
         * @return the namedProtocol
         */
        public NamedProtocol getNamedProtocol() {
            return namedProtocol;
        }

        @Override
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
        @Override
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
                stringJoiner.add(key + "=" + IrCoreUtils.radixPrefix(radix) + Long.toString(map.get(key), radix));
            });
            return stringJoiner.toString();
        }

        @Override
        public int compareTo(Decode other) {
            return IrCoreUtils.lexicalCompare(namedProtocol.compareTo(other.namedProtocol),
                    this.begPos - other.begPos,
                    this.endPos - other.endPos,
                    this.numberOfRepetitions - other.numberOfRepetitions);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return compareTo((Decode) obj) == 0;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.namedProtocol);
            hash = 19 * hash + Objects.hashCode(this.map);
            hash = 19 * hash + this.begPos;
            hash = 19 * hash + this.endPos;
            hash = 19 * hash + this.numberOfRepetitions;
            return hash;
        }

        @Override
        public Decode getDecode() {
            return this;
        }
    }
}
