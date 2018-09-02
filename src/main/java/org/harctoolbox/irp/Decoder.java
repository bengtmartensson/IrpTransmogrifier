/*
Copyright (C) 2017, 2018 Bengt Martensson.

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

    public Decoder() throws IrpParseException, IOException {
        this(new IrpDatabase());
    }

    /**
     * Mainly for testing and debugging.
     * @param names
     * @throws java.io.IOException
     * @throws org.harctoolbox.irp.IrpParseException
     */
    public Decoder(String... names) throws IOException, IrpParseException {
        this(new IrpDatabase(), Arrays.asList(names));
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
        Collection<String> list = names != null ? names : irpDatabase.getNames();
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
     * @param strict If true, intro-, repeat-, and ending sequences are required to match exactly.
     * @param allDecodes If true, output all possible decodes. Otherwise, remove decodes according to prefer-over.
     * @param removeDefaultedParameters If true, remove parameters with value equals to their default.
     * @param frequencyTolerance
     * @param absoluteTolerance
     * @param relativeTolerance
     * @param minimumLeadout
     * @return List of Maps of decodes.
     */
    public List<Map<String, Decode>> decode(ModulatedIrSequence irSequence, boolean strict, boolean allDecodes, boolean removeDefaultedParameters,
            Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout) {
        int pos = 0;
        int oldPos;
        List<Map<String, Decode>> list = new ArrayList<>(1);
        do {
            Map<String, Decode> decodes = new LinkedHashMap<>(8);
            oldPos = pos;
            for (NamedProtocol namedProtocol : parsedProtocols.values()) {
                try {
                    //logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                    Decode decode = namedProtocol.recognize(irSequence, oldPos, strict,
                            frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout);
                    if (removeDefaultedParameters)
                        decode.removeDefaulteds();
                    decodes.put(namedProtocol.getName(), decode);
                } catch (SignalRecognitionException ex) {
                    logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
                } catch (NamedProtocol.ProtocolNotDecodableException ex) {
                }
            }
            if (!decodes.isEmpty()) {
                if (!allDecodes)
                    reduce(decodes);
                pos = checkForConsistency(decodes);
                list.add(decodes);
            }

        } while (pos > oldPos && pos < irSequence.getLength() - 2);
        return list;
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
     * @param strict If true, intro-, repeat-, and ending sequences are required to match exactly.
     * @param allDecodes If true, output all possible decodes. Otherwise, remove decodes according to prefer-over.
     * @param removeDefaultedParameters If true, parameters with value equals to their defaults in the Protocol are removed.
     * @param frequencyTolerance
     * @param absoluteTolerance
     * @param relativeTolerance
     * @param minimumLeadout
     * @return Map of decodes with protocol name as key.
     */
    public Map<String, Decode> decode(IrSignal irSignal, boolean strict, boolean allDecodes, boolean removeDefaultedParameters,
            Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance, Double minimumLeadout) {
        Map<String, Decode> decodes = new HashMap<>(8);
        parsedProtocols.values().forEach((NamedProtocol namedProtocol) -> {
            try {
                //logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());
                Map<String, Long> params = namedProtocol.recognize(irSignal, strict,
                        frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout);
                if (removeDefaultedParameters)
                    namedProtocol.removeDefaulteds(params);
                Decode decode = new Decode(namedProtocol, params);
                decodes.put(namedProtocol.getName(), decode);
            } catch (DomainViolationException | SignalRecognitionException ex) {
                logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
            }
        });

        if (!allDecodes)
            reduce(decodes);
        return decodes;
    }

    public Map<String, Decode> decode(IrSignal irSignal) {
        return decode(irSignal, false, false, true);
    }

    public Map<String, Decode> decode(IrSignal irSignal, boolean strict, boolean allDecodes, boolean removeDefaultedParameters) {
        return decode(irSignal, strict, allDecodes, removeDefaultedParameters, null, null, null, null);
    }

    private void reduce(Map<String, Decode> decodes) {
        List<String> protocols = new ArrayList<>(decodes.keySet());
        protocols.forEach((name) -> {
            Decode decode = decodes.get(name);
            if (decode != null) {
                NamedProtocol prot = decodes.get(name).getNamedProtocol();
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

    private int checkForConsistency(Map<String, Decode> decodes) {
        int min = 99999;
        int max = -9999;
        for (Decode decode : decodes.values()) {
            min = Math.min(min, decode.endPos);
            max = Math.max(max, decode.endPos);
        }
        if (min != max) {
            logger.warning("Decodes of different length found. Keeping only the longest");
            List<Decode> decs = new ArrayList<>(decodes.values());
            for (Decode d : decs) {
                if (d.getEndPos() < max)
                    decodes.remove(d.namedProtocol.getName());
            }
        }
        return max;
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
            return toString(10);
        }

        public String toString(int radix) {
            return namedProtocol.getName() + ": " + mapToString(radix)
                    + (begPos != -1 ? (", beg=" + begPos) : "")
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
