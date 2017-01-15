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
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.xml.sax.SAXException;

public class Decoder {
    private static final Logger logger = Logger.getLogger(Decoder.class.getName());

    public static void main(String[] args) {
        try {
            IrpDatabase irp = new IrpDatabase("src/main/config/IrpProtocols.xml");
            irp.expand();
            Decoder decoder = new Decoder(irp);
            decoder.decodePrint("0000 0073 0000 000D 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0CC8");
            decoder.decodePrint("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            decoder.decodePrint("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            decoder.decodePrint("0000 0068 0000 0015 0060 0018 0018 0018 0018 0018 0018 0018 0030 0018 0030 0018 0030 0018 0018 0018 0018 0018 0018 0018 0030 0018 0030 0018 0018 0018 0018 0018 0030 0018 0018 0018 0018 0018 0018 0018 0030 0018 0018 0018 0018 0240");
            decoder.decodePrint("0000 0068 0044 0022 0169 00B4 0017 0017 0017 0044 0017 0044 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0044 0017 0017 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0044 0017 0017 0017 0017 0017 0017 0017 0044 0017 0044 0017 0044 0017 0017 0017 0017 0017 0044 0017 0044 0017 0044 0017 0017 0017 0017 0017 0017 0017 0044 0017 0044 0017 0636 0169 00B4 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0017 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0017 0017 0044 0017 0044 0017 0044 0017 0044 0017 0017 0017 0044 0017 0017 0017 0017 0017 0017 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0044 0017 0044 0017 0044 0017 0017 0017 0044 0017 0044 0017 0636 0169 00B4 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0017 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0017 0017 0044 0017 0044 0017 0044 0017 0044 0017 0017 0017 0044 0017 0017 0017 0017 0017 0017 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0044 0017 0044 0017 0044 0017 0017 0017 0044 0017 0044 0017 0636");
        } catch (InvalidArgumentException | IOException | SAXException | IrpSyntaxException ex) {
            Logger.getLogger(Decoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final IrpDatabase irpDatabase;
    private final boolean keepDefaultedParameters;
    private final double frequencyTolerance;
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private final Map<String, NamedProtocol> parsedProtocols;

    public Decoder(IrpDatabase irpDatabase) {
        this(irpDatabase, null, true, IrCoreUtils.defaultFrequencyTolerance, IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Decoder(IrpDatabase irpDatabase, List<String> names, boolean keepDefaultedParameters,
            double frequencyTolerance, double absoluteTolerance, double relativeTolerance) {
        this.irpDatabase = irpDatabase;
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
        parsedProtocols.values().parallelStream().forEach((namedProtocol) -> {
            Map<String, Long> parameters = namedProtocol.recognize(irSignal, keepDefaultedParameters,
                    frequencyTolerance, absoluteTolerance, relativeTolerance);
            if (parameters != null)
                output.put(namedProtocol.getName(), new Decode(namedProtocol, parameters));
            else
                logger.log(Level.FINE, "Protocol {0} did not decode", namedProtocol.getName());
        });

        if (!noPreferredDecodes) {
            List<String> protocols = new ArrayList<>(output.keySet());
            protocols.forEach((name) -> {
                NamedProtocol prot = output.get(name).getNamedProtocol();
                if (prot != null) {
                    List<String> preferreds = prot.getPreferredDecode();
                    if (preferreds != null)
                        preferreds.stream().filter((pref) -> (output.containsKey(pref))).forEachOrdered((_item) -> {
                            output.remove(name);
                        });
                }
            });
        }

        return output;
    }

    public void decodePrint(IrSignal irSignal, boolean noPreferredDecodes, PrintStream out) {
        Map<String, Decode> result = decode(irSignal, noPreferredDecodes);
        result.entrySet().forEach((kvp) -> {
            out.println(kvp.getKey() + ": " + kvp.getValue().toString());
        });
    }

    public void decodePrint(String str, boolean noPreferredDecodes) throws InvalidArgumentException {
        IrSignal irSignal = new IrSignal(str);
        decodePrint(irSignal, noPreferredDecodes, System.out);
    }

    public void decodePrint(String str) throws InvalidArgumentException {
        decodePrint(str, false);
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
