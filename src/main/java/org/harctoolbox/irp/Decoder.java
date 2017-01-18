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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.xml.sax.SAXException;

public class Decoder {
    private static final Logger logger = Logger.getLogger(Decoder.class.getName());

    public static void main(String[] args) {
        ParameterSpec.initRandom(1);
        IrpDatabase irp = null;
        Decoder decoder = null;
        Collection<String> protocolNames = null;
        try {
            irp = new IrpDatabase("src/main/config/IrpProtocols.xml");
            irp.expand();
            protocolNames = (args.length == 0) ? irp.getNames() : Arrays.asList(args);
            decoder = new Decoder(irp, args.length == 0 ? null : protocolNames);
        } catch (IOException | SAXException | IrpSyntaxException ex) {
            Logger.getLogger(Decoder.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        for (String protocolName : protocolNames) {
            NamedProtocol protocol;
            IrSignal irSignal = null;
            try {
                protocol = irp.getNamedProtocol(protocolName);
                if (!protocol.isDecodeable())
                    continue;
                NameEngine nameEngine = new NameEngine(protocol.randomParameters());
                if (args.length > 0)
                    System.out.println(nameEngine);
                irSignal = protocol.toIrSignal(nameEngine);
            } catch (UnknownProtocolException | IrpSemanticException | InvalidNameException | UnassignedException | DomainViolationException | OddSequenceLenghtException ex) {
                Logger.getLogger(Decoder.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(2);
            }
            decoder.decodePrint(irSignal);
        }
    }

    private final boolean keepDefaultedParameters;
    private final Double frequencyTolerance;
    private final Double absoluteTolerance;
    private final Double relativeTolerance;
    private final Map<String, NamedProtocol> parsedProtocols;

    public Decoder(IrpDatabase irpDatabase) {
        this(irpDatabase, null, true, IrCoreUtils.defaultFrequencyTolerance, IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Decoder(IrpDatabase irpDatabase, Collection<String> names) {
        this(irpDatabase, names, true, null, null, null);
    }

    public Decoder(IrpDatabase irpDatabase, Collection<String> names, boolean keepDefaultedParameters,
            Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance) {
        this.keepDefaultedParameters = keepDefaultedParameters;
        this.frequencyTolerance = frequencyTolerance;
        this.absoluteTolerance = absoluteTolerance;
        this.relativeTolerance = relativeTolerance;
        this.parsedProtocols = new LinkedHashMap<>(irpDatabase.size());
        Collection<String> list = names != null ? names : irpDatabase.getNames();
        list.parallelStream().forEach((protocolName) -> {
            try {
                NamedProtocol namedProtocol = irpDatabase.getNamedProtocol(protocolName);
                parsedProtocols.put(protocolName, namedProtocol);
            } catch (IrpException ex) {
                // Only happens when there are errors in the Irp database
                throw new ThisCannotHappenException(ex);
            }
        });
    }

    public Map<String, Decode> decode(IrSignal irSignal, boolean noPreferredDecodes) {
        Map<String, Decode> output = new HashMap<>(4);
        //Analyzer analyzer = new Analyzer(irSignal, true /* repeatFinder*/, absoluteTolerance, relativeTolerance);
        for (NamedProtocol namedProtocol :  parsedProtocols.values()) {
            Map<String, Long> parameters = namedProtocol.recognize(irSignal, keepDefaultedParameters,
                    frequencyTolerance, absoluteTolerance, relativeTolerance);
            if (parameters != null)
                output.put(namedProtocol.getName(), new Decode(namedProtocol, parameters));
            else
                logger.log(Level.FINE, "Protocol {0} did not decode", namedProtocol.getName());
        }

        if (!noPreferredDecodes) {
            List<String> protocols = new ArrayList<>(output.keySet());
            protocols.forEach((name) -> {
                Decode decode = output.get(name);
                if (decode != null) {
                    NamedProtocol prot = output.get(name).getNamedProtocol();
                    if (prot != null) {
                        List<String> preferOvers = prot.getPreferOver();
                        if (preferOvers != null)
                            preferOvers.forEach((protName) -> {
                                output.remove(protName);
                            });
                    }
                }
            });
        }

        return output;
    }

    public void decodePrint(IrSignal irSignal, boolean noPreferredDecodes, PrintStream out) {
        Map<String, Decode> result = decode(irSignal, noPreferredDecodes);
        if (result.size() != 1)
            System.out.println(result.size());
        result.values().forEach((kvp) -> {
            out.println(kvp.toString());
        });
    }

    public void decodePrint(String str, boolean noPreferredDecodes) throws InvalidArgumentException {
        IrSignal irSignal = new IrSignal(str);
        decodePrint(irSignal, noPreferredDecodes, System.out);
    }

    public void decodePrint(String str) throws InvalidArgumentException {
        decodePrint(str, false);
    }

    public void decodePrint(IrSignal irSignal) {
        decodePrint(irSignal, false, System.out);
    }

    public static class Decode {
        private final NamedProtocol namedProtocol;
        private final NameEngine nameEngine;
        private final String notes;

        public Decode(NamedProtocol namedProtocol, Map<String, Long> map) {
            this(namedProtocol, new NameEngine(map), null);
        }

        public Decode(NamedProtocol namedProtocol, NameEngine nameEngine) {
            this(namedProtocol, nameEngine, null);
        }

        public Decode(NamedProtocol namedProtocol, NameEngine nameEngine, String notes) {
            this.namedProtocol = namedProtocol;
            this.nameEngine = nameEngine;
            this.notes = notes == null ? "" : notes;
        }

        @Override
        public String toString() {
            return namedProtocol.getName() + ": " + nameEngine.toString()
                    + (notes.isEmpty() ? "" : " (" + notes + ")");
        }

        /**
         * @return the namedProtocol
         */
        public NamedProtocol getNamedProtocol() {
            return namedProtocol;
        }

        /**
         * @return the nameEngine
         */
        public NameEngine getNameEngine() {
            return nameEngine;
        }

        /**
         * @return the notes
         */
        public String getNotes() {
            return notes;
        }

        public String getName() {
            return namedProtocol.getName();
        }
    }
}
