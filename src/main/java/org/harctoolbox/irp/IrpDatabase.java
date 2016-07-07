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

// This file is derived from org.harctoolbox.IrpMaster.IrpMaster.java

package org.harctoolbox.irp;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;

// TODO: allow more than one file; include directive.

/**
 * This class is a data bases manager for the data base of IRP protocols.
 * It reads a configuration file containing definitions for IR format in the IRP-Notation.
 */
public class IrpDatabase {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());
    private static JCommander argumentParser;

    private static class UnparsedProtocol {

        public static final String unnamed = "unnamed_protocol";
        public static final String nameName = "name";
        public static final String irpName = "irp";
        public static final String documentationName = "documentation";

        private HashMap<String, String> map;

        String getProperty(String key) {
            return map.get(key);
        }

        void setProperty(String key, String value) {
            map.put(key, value);
        }

        String getName() {
            return map.get(nameName);
        }

        String getIrp() {
            return map.get(irpName);
        }

        String getDocumentation() {
            return map.get(documentationName);
        }

        UnparsedProtocol(String irp) {
            this(unnamed, irp, null);
        }

        UnparsedProtocol(String name, String irp, String documentation) {
            this();
            map.put(nameName, name);
            map.put(irpName, irp);
            map.put(documentationName, documentation);
        }

        UnparsedProtocol() {
            map = new HashMap<>();
        }

        UnparsedProtocol(HashMap<String, String> map) {
            this.map = map;
        }

        NamedProtocol toNamedProtocol() throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException {
            return new NamedProtocol(getName(), getIrp(), getDocumentation());
        }

        @Override
        public String toString() {
            return getName() + "\t" + getIrp();
        }
    }

    public static final String defaultEncoding = "WINDOWS-1252";

    private final static int maxRecursionDepth = 5;
    private String configFileVersion;
    private String encoding;

    /**
     * @return the configFileVersion
     */
    public final String getConfigFileVersion() {
        return configFileVersion;
    }

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

    public final boolean isKnown(String protocol) {
        return protocols.containsKey(protocol.toLowerCase(Locale.US));
    }

    public static boolean isKnown(String protocolsPath, String protocol) throws FileNotFoundException, IncompatibleArgumentException, WrongCharSetException, UnsupportedEncodingException, IOException {
        return (newIrpDatabase(protocolsPath)).isKnown(protocol);
    }

    /**
     *
     * @param name
     * @return String with IRP representation
     */
    public final String getIrp(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.getIrp();
    }

    /**
     * Static version of getIrp.
     *
     * @param configFilename
     * @param protocolName
     * @return String with IRP representation
     * @throws org.harctoolbox.irp.IrpDatabase.WrongCharSetException
     * @throws java.io.UnsupportedEncodingException
     */
    public static String getIrp(String configFilename, String protocolName) throws WrongCharSetException, UnsupportedEncodingException, IOException {
        IrpDatabase irpMaster = null;
        try {
            irpMaster = newIrpDatabase(configFilename);
        } catch (FileNotFoundException | IncompatibleArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return irpMaster == null ? null : irpMaster.getIrp(protocolName);
    }

    public final Set<String> getNames() {
        return protocols.keySet();
    }

    public final String getDocumentation(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.getDocumentation();
    }

    public String getProperty(String name, String key) {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot == null ? null : prot.getProperty(key);
    }

    public NamedProtocol getNamedProtocol(String name) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot.toNamedProtocol();
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
        return new Protocol(protocol.name.toLowerCase(IrpUtils.dumbLocale), protocol.getIrp(), protocol.documentation);
    }*/

    private void expand() throws IncompatibleArgumentException {
        for (String protocol : protocols.keySet()) {
            expand(0, protocol);
        }
    }

    private void expand(int depth, String name) throws IncompatibleArgumentException {
        UnparsedProtocol p = protocols.get(name);
        if (!p.getIrp().contains("{"))
            throw new IncompatibleArgumentException("IRP `" + p.getIrp() + "' does not contain `{'.");

        if (!p.getIrp().startsWith("{")) {
            String p_name = p.getIrp().substring(0, p.getIrp().indexOf('{')).trim();
            UnparsedProtocol ancestor = protocols.get(p_name.toLowerCase(Locale.US));
            if (ancestor != null) {
                String replacement = ancestor.getIrp().lastIndexOf('[') == -1 ? ancestor.getIrp()
                        : ancestor.getIrp().substring(0, ancestor.getIrp().lastIndexOf('['));
                // Debug.debugConfigfile("Protocol " + name + ": `" + p_name + "' replaced by `" + replacement + "'.");
                logger.log(Level.FINER, "Protocol {0}: `{1}'' replaced by `{2}''.", new Object[]{name, p_name, replacement});
                p.setProperty(UnparsedProtocol.irpName, p.getIrp().replaceAll(p_name, replacement));
                protocols.put(name, p);
                if (depth < maxRecursionDepth)
                    expand(depth + 1, name);
                else
                    logger.log(Level.SEVERE, "Recursion depth in expanding {0} exceeded.", name);
            }
        }
    }

    private void addProtocol(HashMap<String, String> current) {
        // if no irp or name, ignore
        if (current == null
                || current.get(UnparsedProtocol.irpName) == null
                || current.get(UnparsedProtocol.nameName) == null)
            return;

        String nameLower = current.get(UnparsedProtocol.nameName).toLowerCase(Locale.US);

        if (protocols.containsKey(nameLower))
            logger.log(Level.WARNING, "Multiple definitions of protocol `{0}''. Keeping the last.", nameLower);
        protocols.put(nameLower, new UnparsedProtocol(current));
    }

    private IrpDatabase() {
        this.configFileVersion = "";
        protocols = new LinkedHashMap<>();
    }

    public class WrongCharSetException extends RuntimeException {
        WrongCharSetException(String charSet) {
            super(charSet);
        }
    }

    /**
     * Like the other version, but reads from an InputStream instead, using US-ASCII.
     *
     * @param inputStream
     * @throws org.harctoolbox.irp.IrpDatabase.WrongCharSetException
     * @throws java.io.UnsupportedEncodingException
     * @throws IncompatibleArgumentException
     */
//   public IrpDatabase(InputStream inputStream) throws WrongCharSetException, UnsupportedEncodingException, IncompatibleArgumentException {
//        this(inputStream, defaultEncoding);
//    }

    /**
     * Like the other version, but reads from an InputStream instead, using
     * US-ASCII.
     *
     * @param inputStream
     * @param charSet
     * @throws java.io.UnsupportedEncodingException
     * @throws org.harctoolbox.irp.IrpDatabase.WrongCharSetException
     * @throws IncompatibleArgumentException
     */
//    public static IrpDatabase newIrpDatabase(InputStream inputStream) throws UnsupportedEncodingException, WrongCharSetException, IncompatibleArgumentException {
//        return newIrpDatabase(inputStream)
//        this(new InputStreamReader(inputStream, charSet), charSet);
//    }

    private static String determineCharSet(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line = in.readLine();
        if (!line.trim().equals("[encoding]"))
            return null;
        line = in.readLine();
        return line.trim();
    }

    /**
     * Sets up a new IrpMaster from its first argument.
     *
     * @param datafile Configuration file for IRP protocols.
     * @return
     * @throws FileNotFoundException
     * @throws IncompatibleArgumentException
     * @throws org.harctoolbox.irp.IrpDatabase.WrongCharSetException
     * @throws java.io.UnsupportedEncodingException
     */
    public static IrpDatabase newIrpDatabase(String datafile) throws IOException, WrongCharSetException, IncompatibleArgumentException {
        String charSet;
        try (InputStreamReader is = new InputStreamReader(IrpUtils.getInputSteam(datafile))) {
            charSet = determineCharSet(is);
        }
        IrpDatabase db = new IrpDatabase(new InputStreamReader(IrpUtils.getInputSteam(datafile)), charSet);
        return db;
    }

//    public IrpDatabase newIrpDatabase(Reader reader) throws IOException {
//        String charSet = determineCharSet(reader);
//        reader.
//    }

    /**
     * Like the other version, but reads from a Reader instead.
     *
     * @param reader
     * @param charSet
     * @throws org.harctoolbox.irp.IrpDatabase.WrongCharSetException
     * @throws IncompatibleArgumentException
     */
    private IrpDatabase(Reader reader, String charSet) throws WrongCharSetException, IncompatibleArgumentException {
        protocols = new LinkedHashMap<>();
        encoding = charSet;
        BufferedReader in = new BufferedReader(reader);
        HashMap<String, String> currentProtocol = null;
        int lineNo = 0;
        try {
            boolean isDocumentation = false;
            StringBuilder documentation = new StringBuilder();
            while (true) {
                String lineRead = in.readLine();
                if (lineRead == null)
                    break;
                lineNo++;
                String line = lineRead.trim();
                logger.log(Level.FINEST, "Line {0}: {1}", new Object[]{lineNo, line});
                String[] kw = line.split("=", 2);
                String keyword = kw[0].toLowerCase(Locale.US);
                String payload = kw.length > 1 ? kw[1].trim() : null;
                while (payload != null && payload.endsWith("\\")) {
                    payload = payload.substring(0, payload.length()-1)/* + "\n"*/;
                    payload += in.readLine();
                    lineNo++;
                }
                if (line.startsWith("#")) {
                    // comment, ignore
                } else if (line.equals("[encoding]")) {
                    String fileEncoding = in.readLine().toLowerCase(Locale.US);
                    if (!fileEncoding.equalsIgnoreCase(encoding))
                        throw new WrongCharSetException(fileEncoding);
                } else if (line.equals("[version]")) {
                    configFileVersion = in.readLine();
                    lineNo++;
                } else if (line.equals("[protocol]")) {
                    if (currentProtocol != null) {
                        currentProtocol.put(UnparsedProtocol.documentationName, documentation.toString().trim());
                        addProtocol(currentProtocol);
                    }
                    currentProtocol = new HashMap<>();
                    documentation = new StringBuilder();
                    isDocumentation = false;
                } else if (isDocumentation && currentProtocol != null) {
                    // Everything is added to the documentation
                    documentation.append(line).append(" ");
                    if (line.isEmpty())
                        documentation.append("\n\n");
                } else if (line.equals("[documentation]")) {
                    if (currentProtocol != null)
                        isDocumentation = true;
                } else if (keyword.equals("name")) {
                    if (currentProtocol != null)
                        currentProtocol.put(UnparsedProtocol.nameName, payload);
                } else if (keyword.equals("irp")) {
                    if (currentProtocol != null)
                        currentProtocol.put(UnparsedProtocol.irpName, payload);
                } else if (keyword.equals("usable")) {
                    if (payload == null || !payload.equals("yes"))
                        currentProtocol = null;
                } else if (keyword.length() > 1) {
                    if (currentProtocol != null)
                        currentProtocol.put(keyword, payload);
                } else {
                    if (!line.isEmpty())
                        logger.log(Level.FINER, "Ignored line: {0}", line);
                }
            }
            addProtocol(currentProtocol);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        expand();
        logger.log(Level.FINE, "{0} protocols read.", protocols.size());
    }

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder();
        argumentParser.usage(str);

        str.append("\n"
                + "parameters: <protocol> <deviceno> [<subdevice_no>] commandno [<toggle>]\n"
                + "   or       <Pronto code>");

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode);
    }

    private static void doExit(int exitcode) {
        System.exit(exitcode);
    }

    private final static class CommandLineArgs {
        private final static int defaultTimeout = 2000;

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file")
        private String configfile = "src/main/config/IrpProtocols.ini";

        @Parameter(names = {"-p", "--parse"}, description = "Test parse the protocol(s)")
        private boolean parse = false;

        @Parameter(description = "protocols")
        private List<String> protocols = new ArrayList<>();
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName("IrpDatabase");

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.exitUsageError);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        try {
            IrpDatabase db = newIrpDatabase(commandLineArgs.configfile);
            System.out.println("Version: " + db.getConfigFileVersion());
            if (commandLineArgs.protocols.isEmpty()) {
                for (String proto : db.getNames()) {
                    System.out.println(proto);
                    if (commandLineArgs.parse)
                        new Protocol(db.getIrp(proto));
                }
            } else {
                for (String proto : commandLineArgs.protocols) {
                    db.dump("-", proto);
                    if (commandLineArgs.parse)
                        new Protocol(db.getIrp(proto));
                }
            }
        } catch (IOException | WrongCharSetException | IncompatibleArgumentException | IrpSyntaxException | IrpSemanticException | ArithmeticException | InvalidRepeatException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
