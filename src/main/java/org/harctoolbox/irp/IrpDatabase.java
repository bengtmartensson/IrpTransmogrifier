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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.validation.Schema;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * This class is a data bases manager for the data base of IRP protocols.
 * It reads a configuration file containing definitions for IR format in the IRP-Notation.
 */
public final class IrpDatabase {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());

    public static final String IRP_PROTOCOL_NS = "http://www.harctoolbox.org/irp-protocols";
    public static final String IRP_PROTOCOL_SCHEMA_LOCATION = "http://www.harctoolbox.org/schemas/irp-protocols.xsd";
    public static final String IRP_NAMESPACE_PREFIX = "irp";
    private static final int MAX_RECURSION_DEPTH = 5;
    private static final int APRIORI_NUMBER_PROTOCOLS = 200;
    private static final String INI_CHARSET = "WINDOWS-1252"; // Don't ever consider making this configurable!!

    public static final String UNNAMED = "unnamed_protocol";
    public static final String PROTOCOL_NAME = "protocol";
    public static final String NAME_NAME = "name";
    public static final String CNAME_NAME = "c-name";
    public static final String IRP_NAME = "irp";
    public static final String USABLE_NAME = "usable";
    public static final String DOCUMENTATION_NAME = "documentation";
    public static final String PARAMETER_NAME = "parameter";
    public static final String DECODABLE_NAME = "decodable";
    public static final String FREQUENCY_TOLERANCE_NAME = "frequency-tolerance";
    public static final String RELATIVE_TOLERANCE_NAME = "relative-tolerance";
    public static final String ABSOLUTE_TOLERANCE_NAME = "absolute-tolerance";
    public static final String MINIMUM_LEADOUT_NAME = "minimum-leadout";
    public static final String PREFER_OVER_NAME = "prefer-over";
    public static final String ALT_NAME_NAME = "alt_name";

    static boolean isKnownKeyword(String key) {
        return key.equals(PROTOCOL_NAME)
                || key.equals(NAME_NAME)
                || key.equals(CNAME_NAME)
                || key.equals(IRP_NAME)
                || key.equals(USABLE_NAME)
                || key.equals(DOCUMENTATION_NAME)
                || key.equals(PARAMETER_NAME)
                || key.equals(DECODABLE_NAME)
                || key.equals(FREQUENCY_TOLERANCE_NAME)
                || key.equals(RELATIVE_TOLERANCE_NAME)
                || key.equals(ABSOLUTE_TOLERANCE_NAME)
                || key.equals(MINIMUM_LEADOUT_NAME)
                || key.equals(PREFER_OVER_NAME)
                || key.equals(ALT_NAME_NAME);
    }

    public static boolean isKnown(String protocolsPath, String protocol) throws IOException {
        return (new IrpDatabase(protocolsPath)).isKnown(protocol);
    }

    /**
     * Static version of getIrp.
     *
     * @param configFilename
     * @param protocolName
     * @return String with IRP representation
     */
    public static String getIrp(String configFilename, String protocolName) {
        try {
            IrpDatabase irpMaster = new IrpDatabase(configFilename);
            return irpMaster.getIrp(protocolName);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static IrpDatabase readIni(String datafile) throws IOException {
        try (InputStreamReader is = new InputStreamReader(IrCoreUtils.getInputSteam(datafile), INI_CHARSET)) {
            return readIni(is);
        }
    }

    public static IrpDatabase readIni(Reader reader) throws IOException {
        Map<String, UnparsedProtocol> protocols = new LinkedHashMap<>(APRIORI_NUMBER_PROTOCOLS);
        Map<String, String> aliases = new LinkedHashMap<>(16);
        BufferedReader in = new BufferedReader(reader);
        Map<String, String> currentProtocol = null;
        String configFileVersion = null;
        int lineNo = 0;
        boolean isDocumentation = false;
        StringBuilder documentation = new StringBuilder(1000);
        while (true) {
            String lineRead = in.readLine();
            if (lineRead == null)
                break;
            lineNo++;
            String line = lineRead.trim();
            logger.log(Level.FINEST, "Line {0}: {1}", new Object[]{lineNo, line});
            if (line.startsWith("#"))
                // comment, ignore
                continue;

            String[] kw = line.split("=", 2);
            String keyword = kw[0].toLowerCase(Locale.US);
            String payload = kw.length > 1 ? kw[1].trim() : null;
            while (payload != null && payload.endsWith("\\")) {
                payload = payload.substring(0, payload.length() - 1);
                payload = payload.trim();
                String continuation = in.readLine();
                if (continuation != null)
                    continuation = continuation.trim();
                payload += continuation;
                lineNo++;
            }

            if (line.equals("[version]")) {
                configFileVersion = in.readLine();
                lineNo++;
            } else if (line.equals("[protocol]")) {
                if (currentProtocol != null) {
                    currentProtocol.put(DOCUMENTATION_NAME, documentation.toString().trim());
                    addProtocol(protocols, aliases, new UnparsedProtocol(currentProtocol));
                }
                currentProtocol = new HashMap<>(UnparsedProtocol.APRIORI_SIZE);
                documentation = new StringBuilder(1000);
                isDocumentation = false;
            } else if (isDocumentation && currentProtocol != null) {
                // Everything is added to the documentation
                if (line.isEmpty())
                    documentation.append("\n\n");
                else {
                    if (documentation.length() > 0 && !documentation.substring(documentation.length() - 1).equals("\n"))
                        documentation.append(" ");
                    documentation.append(line);
                }
            } else if (line.equals("[documentation]")) {
                if (currentProtocol != null)
                    isDocumentation = true;
            } else if (keyword.equals("name")) {
                if (currentProtocol != null)
                    currentProtocol.put(NAME_NAME, payload);
            } else if (keyword.equals("irp")) {
                if (currentProtocol != null)
                    currentProtocol.put(IRP_NAME, payload);
            } else if (keyword.equals(USABLE_NAME)) {
                if (currentProtocol != null)
                    currentProtocol.put(keyword, payload);
            } else if (keyword.length() > 1) {
                if (currentProtocol != null)
                    currentProtocol.put(keyword, payload);
            } else if (!line.isEmpty())
                logger.log(Level.FINER, "Ignored line: {0}", line);
        }
        if (currentProtocol != null) {
            currentProtocol.put(DOCUMENTATION_NAME, documentation.toString().trim());
            addProtocol(protocols, aliases, new UnparsedProtocol(currentProtocol));
        }
        return new IrpDatabase(protocols, aliases, configFileVersion);
    }

    private static void addProtocol(Map<String, UnparsedProtocol> protocols, Map<String, String> aliases, UnparsedProtocol proto) {
        if (!proto.isUsable() || proto.getName() == null || proto.getIrp() == null)
            return;

        String nameLower = proto.getName().toLowerCase(Locale.US);
        if (protocols.containsKey(nameLower))
            logger.log(Level.WARNING, "Multiple definitions of protocol `{0}''. Keeping the last.", nameLower);
        protocols.put(nameLower, proto);
        List<String> altNameList = proto.getProperties(ALT_NAME_NAME);
        if (altNameList != null) {
            altNameList.stream().map((name) -> {
                String n = name.toLowerCase(Locale.US);
                if (aliases.containsKey(n))
                    logger.log(Level.WARNING, "alt_name \"{0}\" defined more than once.", name);
                return n;
            }).forEachOrdered((n) -> {
                aliases.put(n, proto.getName());
            });
        }
    }
    private static Document openXmlStream(InputStream inputStream) throws IOException {
        try {
            return XmlUtils.openXmlStream(inputStream, null, true, true);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

    private static Document openXmlReader(Reader reader) throws IOException {
        try {
            return XmlUtils.openXmlReader(reader, null, true, true);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

    private static Document openXmlFile(File file) throws IOException {
        try {
            return XmlUtils.openXmlFile(file, (Schema) null, true, true);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

    private String configFileVersion;

    // The key is the protocol name folded to lower case. Case preserved name is in UnparsedProtocol.name.
    private Map<String, UnparsedProtocol> protocols;

    private Map<String, String> aliases;

    private IrpDatabase() {
        this.configFileVersion = "";
        protocols = new LinkedHashMap<>(APRIORI_NUMBER_PROTOCOLS);
        aliases = new LinkedHashMap<>(16);
    }

    public IrpDatabase(Reader reader) throws IOException {
        this(openXmlReader(reader));
    }

    public IrpDatabase(InputStream inputStream) throws IOException {
        this(openXmlStream(inputStream));
    }

    public IrpDatabase(File file) throws IOException {
        this(openXmlFile(file));
    }

    public IrpDatabase(String file) throws IOException {
        this(IrCoreUtils.getInputSteam(file));
    }

    /**
     *
     * @param doc
     */
    public IrpDatabase(Document doc) {
        Element root = doc.getDocumentElement();
        configFileVersion = root.getAttribute("version");
        NodeList nodes = root.getElementsByTagNameNS(IRP_PROTOCOL_NS, "protocol");
        protocols = new LinkedHashMap<>(nodes.getLength()); // to preserve order
        aliases = new LinkedHashMap<>(16);
        for (int i = 0; i < nodes.getLength(); i++)
            addProtocol((Element)nodes.item(i));
    }

    private IrpDatabase(Map<String, UnparsedProtocol> protocols, Map<String, String> aliases, String version) {
        this.configFileVersion = version;
        this.protocols = protocols;
        this.aliases = aliases;
    }

    private void addProtocol(UnparsedProtocol proto) {
        addProtocol(protocols, aliases, proto);
    }

    private Document emptyDocument() {
        Document doc = XmlUtils.newDocument(true);
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"IrpProtocols2html.xsl\"");
        doc.appendChild(pi);
        Element root = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":protocols");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");
        root.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        root.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        root.setAttribute("xsi:schemaLocation", IRP_PROTOCOL_NS + " " + IRP_PROTOCOL_SCHEMA_LOCATION);
        root.setAttribute("version", configFileVersion);
        doc.appendChild(root);
        return doc;
    }

    public Document toDocument() {
        Document doc = emptyDocument();
        Element root = doc.getDocumentElement();
        protocols.values().forEach((protocol) -> {
            root.appendChild(protocol.toElement(doc));
        });

        return doc;
    }

    public Document toDocument(List<String> protocolNames) {
        Document document = XmlUtils.newDocument();
        Element root = document.createElement("NamedProtocols");
        document.appendChild(root);

        protocolNames.forEach((String pname) -> {
            try {
                logger.log(Level.FINE, "Processing {0} ...", pname);
                NamedProtocol protocol = getNamedProtocol(pname);
                Element element = protocol.toElement(document);
                root.appendChild(element);
            } catch (NameUnassignedException | ArithmeticException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                logger.log(Level.WARNING, "{0}; protocol ignored", ex.getMessage());
            } catch (UnknownProtocolException ex) {
                throw new ThisCannotHappenException(ex);
            }
        });

        return document;
    }

    /**
     * @return the configFileVersion
     */
    public String getConfigFileVersion() {
        return configFileVersion;
    }

    private void dump(PrintStream ps, String name) {
        ps.println(getUnparsedProtocol(name));
    }

    private void dump(PrintStream ps) {
        protocols.keySet().stream().forEach((s) -> {
            dump(ps, s);
        });
    }

    public void dump(String filename) throws FileNotFoundException {
        dump(IrCoreUtils.getPrintSteam(filename));
    }

    public void dump(String filename, String name) throws FileNotFoundException {
        dump(IrCoreUtils.getPrintSteam(filename), name);
    }

    public boolean isAlias(String protocol) {
        return aliases.containsKey(protocol.toLowerCase(Locale.US));
    }

    public String expandAlias(String protocol) {
        return isAlias(protocol) ? aliases.get(protocol.toLowerCase(Locale.US)) : protocol;
    }

    public boolean isKnown(String protocol) {
        return protocols.containsKey(protocol.toLowerCase(Locale.US));
    }

    public boolean isKnownExpandAlias(String protocol) {
        return isKnown(expandAlias(protocol));
    }

    public String getIrp(String name) {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot == null ? null : prot.getIrp();
    }

    public String getIrpExpandAlias(String name) {
        return getIrp(expandAlias(name));
    }

    private UnparsedProtocol getUnparsedProtocol(String name) {
        return protocols.get(name.toLowerCase(Locale.US));
    }

    public Set<String> getNames() {
        return protocols.keySet();
    }

    public Set<String> getAliases() {
        return aliases.keySet();
    }

    public String getName(String name) {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot == null ? null : prot.getName();
    }

    public String getCName(String name) {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot == null ? null : prot.getCName();
    }

    public String getNameExpandAlias(String name) {
        return getName(expandAlias(name));
    }

    public int size() {
        return protocols.size();
    }

    public List<String> getMatchingNamesRegexp(String regexp) {
        Pattern pattern = Pattern.compile(regexp.toLowerCase(Locale.US));
        List<String> result = new ArrayList<>(10);
        protocols.keySet().stream().filter((candidate) -> (pattern.matcher(candidate).matches())).forEach((candidate) -> {
            result.add(candidate);
        });
        aliases.keySet().stream().filter((candidate) -> (pattern.matcher(candidate).matches())).forEach((candidate) -> {
            result.add(candidate);
        });
        return result;
    }

    public List<String> getMatchingNamesExact(String string) {
        List<String> result = new ArrayList<>(10);
        protocols.keySet().stream().filter((candidate) -> (candidate.equalsIgnoreCase(string))).forEachOrdered((candidate) -> {
            result.add(candidate);
        });
        aliases.keySet().stream().filter((candidate) -> (candidate.equalsIgnoreCase(string))).forEachOrdered((candidate) -> {
            result.add(candidate);
        });
        return result;
    }

    public List<String> getMatchingNames(Iterable<String> iterable, boolean regexp, boolean urlDecode) {
        List<String> result = new ArrayList<>(10);
        for (String str : iterable) {
            try {
                String s = urlDecode ? URLDecoder.decode(str, "US-ASCII") : str;
                result.addAll(regexp ? getMatchingNamesRegexp(s) : getMatchingNamesExact(s));
            } catch (UnsupportedEncodingException ex) {
                throw new ThisCannotHappenException();
            }
        }

        return result;
    }

    public String getDocumentation(String name) {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot == null ? null : prot.getDocumentation();
    }

    public String getDocumentationExpandAlias(String name) {
        return getDocumentation(expandAlias(name));
    }

    public String getFirstProperty(String name, String key) {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot == null ? null : prot.getFirstProperty(key);
    }

    public NamedProtocol getNamedProtocol(String name) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        if (prot == null)
            throw new UnknownProtocolException(name);
        return prot.toNamedProtocol();
    }

    public NamedProtocol getNamedProtocolExpandAlias(String name) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        return getNamedProtocol(expandAlias(name));
    }

    public List<NamedProtocol> getNamedProtocol(Collection<String> protocolNames) {
        List<NamedProtocol> list = new ArrayList<>(protocolNames.size());
        protocolNames.stream().forEach((pName) -> {
            try {
                list.add(getNamedProtocol(pName));
            } catch (IrpInvalidArgumentException | InvalidNameException | NameUnassignedException | UnknownProtocolException | UnsupportedRepeatException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        });
        return list;
    }

    public void expand() {
        protocols.keySet().forEach((protocol) -> {
            expand(0, protocol);
        });
    }

    private void expand(int depth, String name) {
        UnparsedProtocol p = getUnparsedProtocol(name);
        if (!p.getIrp().contains("{"))
            throw new ThisCannotHappenException("IRP `" + p.getIrp() + "' does not contain `{'.");

        if (!p.getIrp().startsWith("{")) {
            String p_name = p.getIrp().substring(0, p.getIrp().indexOf('{')).trim();
            UnparsedProtocol ancestor = protocols.get(p_name.toLowerCase(Locale.US));
            if (ancestor != null) {
                String replacement = ancestor.getIrp().lastIndexOf('[') == -1 ? ancestor.getIrp()
                        : ancestor.getIrp().substring(0, ancestor.getIrp().lastIndexOf('['));
                logger.log(Level.FINEST, "Protocol {0}: `{1}'' replaced by `{2}''.", new Object[]{name, p_name, replacement});
                p.setUniqueProperty(IRP_NAME, p.getIrp().replaceAll(p_name, replacement));
                protocols.put(name, p);
                if (depth < MAX_RECURSION_DEPTH)
                    expand(depth + 1, name);
                else
                    logger.log(Level.SEVERE, "Recursion depth in expanding {0} exceeded.", name);
            }
        }
    }

    private void addProtocol(Element current) {
        addProtocol(new UnparsedProtocol(current));
    }

    public List<String> evaluateProtocols(List<String> protocols, boolean sort, boolean regexp, boolean urlDecode) {
        List<String> list = (protocols == null || protocols.isEmpty())
                ? new ArrayList<>(getNames()) : getMatchingNames(protocols, regexp, urlDecode);
        if (sort)
            Collections.sort(list);
        return list;
    }

    public Protocol getProtocol(String protocolName) throws UnknownProtocolException, UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException {
        if (!isKnown(protocolName))
            throw new UnknownProtocolException(protocolName);
        return new Protocol(getIrp(protocolName));
    }

    public Protocol getProtocolExpandAlias(String protocolName) throws UnknownProtocolException, UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException {
        return getProtocol(expandAlias(protocolName));
    }

    public String getNormalFormIrp(String protocolName, int radix) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        Protocol protocol = getProtocol(protocolName);
        return protocol.normalFormIrpString(radix);
    }

    public void printAsIni(PrintStream out) {
        out.println("# Generated by " + Version.versionString);
        out.println("[version]");
        out.println(getConfigFileVersion());
        protocols.keySet().forEach((protocol) -> {
            printAsIni(out, protocol);
        });
    }

    private void printAsIni(PrintStream out, String protocol) {
        out.println("[" + PROTOCOL_NAME + "]");
        UnparsedProtocol prot = getUnparsedProtocol(protocol);
        prot.map.entrySet().forEach((kvp) -> { // assume that "name" comes first
            String key = kvp.getKey();
            if (!key.equals(DOCUMENTATION_NAME))
                kvp.getValue().forEach((value) -> {
                    out.println(key + "=" + value);
                });
        });

        String doc = prot.getDocumentation();
        if (doc != null && ! doc.trim().isEmpty()) {
            out.println("[documentation]");
            out.println(doc);
        }
        out.println();
    }

    public boolean checkSorted() {
        boolean result = true;
        String last = " ";
        for (String protocol : protocols.keySet()) {
            if (protocol.compareTo(last) < 0) {
                result = false;
                logger.log(Level.WARNING, "Protocol {0} violates ordering", protocol);
            }
            last = protocol;
        }
        return result;
    }

    public IrSignal render(String protocolName, Map<String, Long> params) throws IrpException {
        Protocol protocol = getProtocolExpandAlias(protocolName);
        return protocol.toIrSignal(params);
    }

    private static class UnparsedProtocol {
        public static final int APRIORI_SIZE = 4;

        private Map<String, List<String>> map;

        UnparsedProtocol() {
            map = new LinkedHashMap<>(APRIORI_SIZE); // want to preserve order
        }

        UnparsedProtocol(String irp) {
            this(UNNAMED, irp, null);
        }

        UnparsedProtocol(String name, String irp, String documentation) {
            this();
            addProperty(NAME_NAME, name);
            addProperty(IRP_NAME, irp);
            addProperty(DOCUMENTATION_NAME, documentation);
        }

        UnparsedProtocol(Map<String, String> map) {
            this();
            map.entrySet().forEach((kvp) -> {
                addProperty(kvp.getKey(), kvp.getValue());
            });
        }

        UnparsedProtocol(Element element) {
            this();
            parseElement(element);
        }

        private void addProperty(String key, String value) {
            if (!map.containsKey(key))
                map.put(key, new ArrayList<>(1));
            List<String> list = map.get(key);
            list.add(value);
        }

        private void setUniqueProperty(String key, String value) {
            List<String> list = new ArrayList<>(1);
            list.add(value);
            map.put(key, list);
        }

        private String getFirstProperty(String key) {
            List<String> list = map.get(key);
            return list == null ? null : list.get(0);
        }

        private void parseElement(Element element) {
            String usable = element.getAttribute(USABLE_NAME);
            if (!usable.isEmpty())
                addProperty(USABLE_NAME, usable);
            String name = element.getAttribute(NAME_NAME);
            addProperty(NAME_NAME, name);
            String cName = element.getAttribute(CNAME_NAME);
            addProperty(CNAME_NAME, cName.isEmpty() ? IrpUtils.toCIdentifier(name) : cName);
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;
                switch (e.getLocalName()) {
                    case IRP_NAME:
                        addProperty(IRP_NAME, e.getTextContent().replaceAll("\\s+", ""));
                        break;
                    case DOCUMENTATION_NAME:
                        addProperty(DOCUMENTATION_NAME, e.getTextContent().trim().replaceAll("\\s+", " "));
                        break;
                    case PARAMETER_NAME:
                        addProperty(e.getAttribute("name"), e.getTextContent());
                        break;
                    default:
                        throw new ThisCannotHappenException("unknown tag: " + e.getTagName());
                }
            }
        }

        List<String> getProperties(String key) {
            return map.get(key);
        }

        String getName() {
            return getFirstProperty(NAME_NAME);
        }

        String getCName() {
            return getFirstProperty(CNAME_NAME);
        }

        String getIrp() {
            return getFirstProperty(IRP_NAME);
        }

        String getDocumentation() {
            return getFirstProperty(DOCUMENTATION_NAME);
        }

        boolean isUsable() {
            String str = getFirstProperty(USABLE_NAME);
            return str == null || Boolean.parseBoolean(str) || str.equalsIgnoreCase("yes");
        }

        NamedProtocol toNamedProtocol() throws InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
            return new NamedProtocol(getName(), getCName(), getIrp(), getDocumentation(),
                    getFirstProperty(FREQUENCY_TOLERANCE_NAME), getFirstProperty(ABSOLUTE_TOLERANCE_NAME), getFirstProperty(RELATIVE_TOLERANCE_NAME),
                    getFirstProperty(MINIMUM_LEADOUT_NAME), getFirstProperty(DECODABLE_NAME), getProperties(PREFER_OVER_NAME), map);
        }

        @Override
        public String toString() {
            return getName() + "\t" + getIrp();
        }

        Element toElement(Document doc) {
            Element element = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":" + PROTOCOL_NAME);
            for (Map.Entry<String, List<String>> kvp : map.entrySet()) {
                switch (kvp.getKey()) {
                    case NAME_NAME:
                        element.setAttribute(NAME_NAME, kvp.getValue().get(0));
                        element.setAttribute(CNAME_NAME, IrCoreUtils.toCName(kvp.getValue().get(0)));
                        break;
                    case USABLE_NAME:
                        element.setAttribute(USABLE_NAME, Boolean.toString(!kvp.getValue().get(0).equals("no")));
                        break;
                    case IRP_NAME: {
                        Element irp = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":" + IRP_NAME);
                        irp.appendChild(doc.createCDATASection(kvp.getValue().get(0)));
                        element.appendChild(irp);
                    }
                    break;
                    case DOCUMENTATION_NAME:
                        try {
                            String docXml = "<?xml version=\"1.0\"?><irp:documentation xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:irp=\"http://www.harctoolbox.org/irp-protocols\">" + kvp.getValue() + "</irp:documentation>";
                            Document miniDoc = XmlUtils.parseStringToXmlDocument(docXml, true, false);
                            Element root = miniDoc.getDocumentElement();
                            element.appendChild(doc.adoptNode(root));
                        } catch (SAXException ex) {
                            logger.log(Level.WARNING, "{0} {1}", new Object[]{kvp.getValue().get(0), ex.getMessage()});
                        }
                        break;
                    default: {
                        kvp.getValue().stream().map((s) -> {
                            Element param = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":" + PARAMETER_NAME);
                            param.setAttribute(NAME_NAME, kvp.getKey());
                            param.setTextContent(s);
                        return param;
                    }).forEachOrdered((param) -> {
                        element.appendChild(param);
                    });
                    }
                }
            }
            return element;
        }
    }
}
