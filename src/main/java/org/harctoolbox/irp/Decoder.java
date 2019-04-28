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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
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

    /**
     * Allows to invoke the decoding from the command line.
     * @param args Pronto hex type IR signal.
     */
    public static void main(String[] args) {
        try {
            Decoder decoder = new Decoder();
            IrSignal irSignal = Pronto.parse(args);
            Map<String, Decode> decodes = decoder.decode(irSignal);
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
     * @param level
     * @return List of decodes.
     */
    public DecodeTree decode(ModulatedIrSequence irSequence, DecoderParameters params, int level) {
        return decode(irSequence, 0, params, level);
    }

    private DecodeTree /*List<Map<String, Decode>>*/ decode(ModulatedIrSequence irSequence, int position, DecoderParameters params, int level) {
        logger.log(Level.FINE, String.format("level = %1$d position = %2$d", level, position));
        DecodeTree list = new DecodeTree(irSequence.getLength() - position);
        if (list.length == 0)
            return list;

        parsedProtocols.values().forEach((namedProtocol) -> {
            try {
                //logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                TrunkDecodeTree decode = tryNamedProtocol(namedProtocol, irSequence, position, params, level);
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

        return list;
    }

    private TrunkDecodeTree tryNamedProtocol(NamedProtocol namedProtocol, ModulatedIrSequence irSequence, int position, DecoderParameters params, int level)
            throws SignalRecognitionException, NamedProtocol.ProtocolNotDecodableException {
        Decode decode = namedProtocol.recognize(irSequence, position, params.isStrict(),
                params.getFrequencyTolerance(), params.getAbsoluteTolerance(), params.getRelativeTolerance(), params.getMinimumLeadout());
        if (params.isRemoveDefaultedParameters())
            decode.removeDefaulteds();
        if (!params.recursive || decode.endPos == irSequence.getLength() - 1)
            return new TrunkDecodeTree(decode, irSequence.getLength());

        DecodeTree rest = decode(irSequence, decode.getEndPos() + 1, params, level + 1);
        return new TrunkDecodeTree(decode, rest);
    }

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
    public Map<String, Decode> decode(IrSignal irSignal, DecoderParameters parameters) {
        Map<String, Decode> decodes = new HashMap<>(8);
        parsedProtocols.values().forEach((NamedProtocol namedProtocol) -> {
            try {
                //logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                Map<String, Long> params = namedProtocol.recognize(irSignal, parameters.isStrict(),
                        parameters.getFrequencyTolerance(), parameters.getAbsoluteTolerance(), parameters.getRelativeTolerance(), parameters.getMinimumLeadout());
                if (parameters.isRemoveDefaultedParameters())
                    namedProtocol.removeDefaulteds(params);
                Decode decode = new Decode(namedProtocol, params);
                decodes.put(namedProtocol.getName(), decode);
            } catch (DomainViolationException | SignalRecognitionException ex) {
                logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
            }
        });


        if (!parameters.isAllDecodes())
            reduce(decodes);
        return decodes;
    }

    public Map<String, Decode> decode(IrSignal irSignal) {
        return decode(irSignal, false, false, true, false);
    }

    public Map<String, Decode> decode(IrSignal irSignal, boolean strict, boolean allDecodes, boolean removeDefaultedParameters, boolean recursive) {
        DecoderParameters params = new DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive);
        return decode(irSignal, params);
    }

    private void reduce(Map<String, Decode> decodes) {
        List<Decode> decs = new ArrayList<>(decodes.values());
        decs.forEach((Decode decode) -> {
            if (decode != null) {
                NamedProtocol prot = decode.getNamedProtocol();
                if (prot != null) {
                    List<String> preferOvers = prot.getPreferOver();
                    if (preferOvers != null) {
                        preferOvers.forEach((protName) -> {
                            decodes.remove(protName);
                        });
                    }
                }
            }
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

        private final boolean strict;
        private final boolean allDecodes;
        private final boolean removeDefaultedParameters;
        private final boolean recursive;
        private final Double frequencyTolerance;
        private final Double absoluteTolerance;
        private final Double relativeTolerance;
        private final Double minimumLeadout;
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
         */
        public DecoderParameters(boolean strict, boolean allDecodes, boolean removeDefaultedParameters, boolean recursive,
                Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout) {
            this.strict = strict;
            this.allDecodes = allDecodes;
            this.removeDefaultedParameters = removeDefaultedParameters;
            this.recursive = recursive;
            this.frequencyTolerance = frequencyTolerance;
            this.absoluteTolerance = absoluteTolerance;
            this.relativeTolerance = relativeTolerance;
            this.minimumLeadout = minimumLeadout;
        }

        public DecoderParameters() {
            this(false, false, true, false);
        }

        public DecoderParameters(boolean strict, boolean allDecodes, boolean removeDefaultedParameters, boolean recursive) {
            this(strict, allDecodes, removeDefaultedParameters, recursive, null, null, null, null);
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
    }

    public static class DecodeTree implements Iterable<TrunkDecodeTree> {
        private ArrayList<TrunkDecodeTree> decodes;
        private int length;

        private DecodeTree(int length) {
            this.length = length;
            decodes = new ArrayList<>(4);
        }

        public String toString(int radix, String separator) {
            if (isVoid())
                return "";
            StringJoiner stringJoiner = new StringJoiner(separator, "{", "}");
            if (decodes.isEmpty())
                stringJoiner.add("UNDECODED. length=" + Integer.toString(length, 10));
            decodes.forEach((decode) -> {
                stringJoiner.add(decode.toString(radix, separator));
            });

            return stringJoiner.toString();
        }

        @Override
        public String toString() {
            return toString(10, ", ");
        }

        private void add(TrunkDecodeTree decode) {
            decodes.add(decode);
        }

        boolean isEmpty() {
            return decodes.isEmpty();
        }

        boolean isVoid() {
            return decodes.isEmpty() && length == 0;
        }

        private void reduce() {
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

        private void removeIncompletes() {
            List<TrunkDecodeTree> decs = new ArrayList<>(decodes);
            decs.forEach((TrunkDecodeTree decode) -> {
                if (decode != null && !decode.isComplete())
                    decodes.remove(decode);
            });
        }

        private TrunkDecodeTree findName(String protocol) {
            for (TrunkDecodeTree decode : decodes)
                if (decode.getNamedProtocol().getName().equals(protocol))
                    return decode;

            return null;
        }

        private void remove(String protName) {
            TrunkDecodeTree decode = findName(protName);
            if (decode != null)
                decodes.remove(decode);
        }

        int size() {
            return decodes.size();
        }

        TrunkDecodeTree getAlternative(String protocolName) {
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
    }

    public static class TrunkDecodeTree {
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
    }

    public static class Decode {
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
    }
}
