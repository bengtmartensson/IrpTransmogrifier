/*
Copyright (C) 2011-2013 Bengt Martensson.

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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * This class is a data bases manager for the data base of IRP protocols.
 * It reads a configuration file containing definitions for IR format in the IRP-Notation.
 */

// Prefered order for protocol parameters:
// D, S, F, T, and then the rest in alphabetical order.

public class IrpDatabase {
    private static class UnparsedProtocol {

        public static final String unnamed = "unnamed_protocol";
        private static final long serialVersionUID = 1L;
        /** name of protocol. */
        public String name;

        /* Documentation for protocol as XHTML fragment. */
        public String documentation;

        /* IRP code */
        public String irp;

        /* Not presently used. */
        public String efcTranslation;

        /* Nor presently used. */
        public ArrayList<Short> ueiProtocol;

        UnparsedProtocol(String irp) {
            this.irp = irp;
            this.name = unnamed;
            documentation = null;
            efcTranslation = null;
            ueiProtocol = new ArrayList<>();
        }

        UnparsedProtocol() {
            this(null);
        }

        @Override
        public String toString() {
            return name + "\t" + irp;
        }
    }

    private final static int max_recursion_depth_expanding = 5;
    private String configFileVersion = "not found";
    private final static boolean testParse = false;
    //private static String defaultConfigFilename = "IrpProtocols.ini";

    // The key is the protocol name folded to lower case. Case preserved name is in UnparsedProtocol.name.
    private LinkedHashMap<String, UnparsedProtocol> protocols;

    private void dump(PrintStream ps, String name) {
        ps.println(protocols.get(name));
    }

    private void dump(PrintStream ps) {
        for (String s : protocols.keySet())
            dump(ps, s);
    }

    public void dump(String filename) throws FileNotFoundException {
        dump(IrpUtils.getPrintSteam(filename));
    }

    public void dump(String filename, String name) throws FileNotFoundException {
        dump(IrpUtils.getPrintSteam(filename), name);
    }

    //private String get(String name) {
    //    return protocols.get(name).toString();
    //}

    public boolean isKnown(String protocol) {
        return protocols.containsKey(protocol.toLowerCase(Locale.US));
    }

    public static boolean isKnown(String protocolsPath, String protocol) throws FileNotFoundException, IncompatibleArgumentException {
        return (new IrpDatabase(protocolsPath)).isKnown(protocol);
    }

    public String getIrp(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.irp;
    }

    public Set<String> getNames() {
        return protocols.keySet();
    }

    public String getDocumentation(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.documentation;
    }

    public String getEfcTranslation(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.efcTranslation;
    }

    public ArrayList<Short> getUeiProtocol(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.ueiProtocol;
    }

    /**
     * Constructs a new Protocol with requested name, taken from the configuration
     * file/data base within the current IrpMaster.
     *
     * @param name protocol name in the configuration file/data base
     * @return newly parsed protocol
     * @throws UnassignedException
     * @throws ParseException
     * @throws org.harctoolbox.IrpMaster.UnknownProtocolException
     * /

    public Protocol newProtocol(String name) throws UnassignedException, ParseException, UnknownProtocolException {
        UnparsedProtocol protocol = protocols.get(name.toLowerCase(IrpUtils.dumbLocale));
        if (protocol == null)
            throw new UnknownProtocolException(name);
        return new Protocol(protocol.name.toLowerCase(IrpUtils.dumbLocale), protocol.irp, protocol.documentation);
    }*/

    private void expand() throws IncompatibleArgumentException {
        for (String protocol : protocols.keySet()) {
            expand(0, protocol);
        }
    }

    private void expand(int depth, String name) throws IncompatibleArgumentException {
        UnparsedProtocol p = protocols.get(name);
        if (!p.irp.contains("{"))
            throw new IncompatibleArgumentException("IRP `" + p.irp + "' does not contain `{'.");

        if (!p.irp.startsWith("{")) {
            String p_name = p.irp.substring(0, p.irp.indexOf('{')).trim();
            UnparsedProtocol ancestor = protocols.get(p_name.toLowerCase(Locale.US));
            if (ancestor != null) {
                String replacement = ancestor.irp.lastIndexOf('[') == -1 ? ancestor.irp
                        : ancestor.irp.substring(0, ancestor.irp.lastIndexOf('['));
                // Debug.debugConfigfile("Protocol " + name + ": `" + p_name + "' replaced by `" + replacement + "'.");
                Logger.getLogger(IrpDatabase.class.getName()).log(Level.FINE, "Protocol {0}: `{1}'' replaced by `{2}''.", new Object[]{name, p_name, replacement});
                p.irp = p.irp.replaceAll(p_name, replacement);
                protocols.put(name, p);
                if (depth < max_recursion_depth_expanding)
                    expand(depth + 1, name);
                else
                    System.err.println("Recursion depth in expanding " + name + " exceeded.");
            }
        }
    }

    private void addProtocol(UnparsedProtocol current) {
        // if no irp or name, ignore
        if (current == null || current.irp == null || current.name == null)
            return;

        if (current.documentation != null)
            current.documentation = current.documentation.trim();

        if (protocols.containsKey(current.name.toLowerCase(Locale.US)))
            Logger.getLogger(IrpDatabase.class.getName()).log(Level.WARNING, "Multiple definitions of protocol `{0}''. Keeping the last.", current.name.toLowerCase(Locale.US));
        protocols.put(current.name.toLowerCase(Locale.US), current);

        /*if (testParse) {
            IrpLexer lex = null;// new IrpLexer(new ANTLRStringStream(current.irp));
            CommonTokenStream tokens = new CommonTokenStream(lex);

            IrpParser parser = new IrpParser(tokens);
            try {
                parser.protocol();
            } catch (RecognitionException ex) {
                System.out.println(ex.getMessage());
            }
        }*/
    }

    private IrpDatabase() {
        protocols = new LinkedHashMap<>();
        //userComm = new UserComm();
    }

    /**
     * Like the other version, but reads from an InputStream instead.
     *
     * @param inputStream
     * @throws IncompatibleArgumentException
     */
   public IrpDatabase(InputStream inputStream) throws IncompatibleArgumentException {
        this(new InputStreamReader(inputStream, Charset.forName("US-ASCII")));
    }

    /**
     * Like the other version, but reads from a Reader instead.
     *
     * @param reader
     * @throws IncompatibleArgumentException
     */
    public IrpDatabase(Reader reader) throws IncompatibleArgumentException {
        this();
        BufferedReader in = new BufferedReader(reader);
        UnparsedProtocol currentProtocol = null;
        int lineNo = 0;
        try {
            for (String lineRead = in.readLine(); lineRead != null; lineRead = in.readLine()) {
                lineNo++;
                String line = lineRead.trim();
                String[] kw = line.split("=", 2);
                String keyword = kw[0];
                String payload = kw.length > 1 ? kw[1].trim() : null;
                while (payload != null && payload.endsWith("\\")) {
                    payload = payload.substring(0, payload.length()-1)/* + "\n"*/;
                    payload += in.readLine();
                }
                if (line.startsWith("#")) {
                    // comment, ignore
                } else if (line.equals("[version]")) {
                    configFileVersion = in.readLine();
                } else if (line.equals("[protocol]")) {
                    addProtocol(currentProtocol);
                    currentProtocol = new UnparsedProtocol();
                } else if (currentProtocol != null && currentProtocol.documentation != null) {
                    // Everything is added to the documentation
                    currentProtocol.documentation += currentProtocol.documentation.isEmpty() ? line
                            : line.isEmpty() ? "\n\n"
                            : ((currentProtocol.documentation.endsWith("\n") ? "" : " ") + line);
                } else if (line.equals("[documentation]")) {
                    if (currentProtocol != null)
                        currentProtocol.documentation = "";
                } else if (keyword.equals("name")) {
                    if (currentProtocol != null)
                        currentProtocol.name = payload;
                } else if (keyword.equals("irp")) {
                    if (currentProtocol != null)
                        currentProtocol.irp = payload;
                } else if (keyword.equals("EFC_translation")) {
                    if (currentProtocol != null)
                        currentProtocol.efcTranslation = payload;
                } else if (keyword.equals("usable")) {
                    if (payload == null || !payload.equals("yes"))
                        currentProtocol = null;
                } else if (keyword.equals("UEI_protocol")) {
                    if (currentProtocol != null) {
                        String[] str = payload != null ? payload.split("[\\s,;or]+") : new String[0];
                        boolean hasComplained = false;
                        for (String s : str) {
                            try {
                                currentProtocol.ueiProtocol.add(Short.parseShort(s, 16));
                            } catch (NumberFormatException ex) {
                                if (!hasComplained) {
                                    Logger.getLogger(IrpDatabase.class.getName()).log(Level.FINE, "Unparsable UEI protocol in line {0}: {1}", new Object[]{lineNo, line});
                                    hasComplained = true;
                                }
                            }
                        }
                    }
                } else if (keyword.length() > 1) {
                    //if (Debug.debugConfigfile())
                    //    System.out.println("Unknown keyword:" + keyword + " = " + payload);
                } else {
                    if (!line.isEmpty())
                        Logger.getLogger(IrpDatabase.class.getName()).log(Level.FINE, "Ignored line: {0}", line);
                }
            }
            addProtocol(currentProtocol);
        } catch (IOException ex) {
            Logger.getLogger(IrpDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        expand();
        //Debug.debugConfigfile(protocols.size() + " protocols read.");
        Logger.getLogger(IrpDatabase.class.getName()).log(Level.FINE, "{0} protocols read.", protocols.size());
    }

    /**
     * Sets up a new IrpMaster from its first argument.
     *
     * @param datafile Configuration file for IRP protocols.
     * @throws FileNotFoundException
     * @throws IncompatibleArgumentException
     */
    public IrpDatabase(String datafile) throws FileNotFoundException, IncompatibleArgumentException {
        this(IrpUtils.getInputSteam(datafile));
    }

    /**
     * Static version of getIrp.
     *
     * @param configFilename
     * @param protocolName
     * @return String with IRP representation
     */
    public static String getIrp(String configFilename, String protocolName) {
        IrpDatabase irpMaster = null;
        try {
            irpMaster = new IrpDatabase(configFilename);
        } catch (FileNotFoundException | IncompatibleArgumentException ex) {
        }
        return irpMaster == null ? null : irpMaster.getIrp(protocolName);
    }
}
