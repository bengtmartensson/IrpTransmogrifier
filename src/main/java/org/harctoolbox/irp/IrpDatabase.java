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
public class IrpDatabase {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());

    public static final String irpProtocolNS = "http://www.harctoolbox.org/irp-protocols";
    public static final String irpProtocolLocation = "file:///home/bengt/harctoolbox/IrpTransmogrifier/src/main/schemas/irp-protocols.xsd"; // FIXME
    public static final String irpNamespacePrefix = "irp";
    private static final int maxRecursionDepth = 5;
    private static final int APRIORI_NUMBER_PROTOCOLS = 200;
    private static final String INI_CHARSET = "WINDOWS-1252"; // Don't ever consider making this configurable!!

    public static final String unnamed = "unnamed_protocol";
    public static final String protocolName = "protocol";
    public static final String nameName = "name";
    public static final String cNameName = "c-name";
    public static final String irpName = "irp";
    public static final String usableName = "usable";
    public static final String documentationName = "documentation";
    public static final String parameterName = "parameter";
    public static final String decodableName = "decodable";
    public static final String frequencyToleranceName = "frequency-tolerance";
    public static final String relativeToleranceName = "relative-tolerance";
    public static final String absoluteToleranceName = "absolute-tolerance";
    public static final String preferOverName = "prefer-over";

    public static boolean isKnown(String protocolsPath, String protocol) throws IOException, IrpException {
        try {
            return (new IrpDatabase(protocolsPath)).isKnown(protocol);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IrpException(ex);
        }
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
        } catch (SAXException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static IrpDatabase readIni(String datafile) throws IOException {
        try (InputStreamReader is = new InputStreamReader(IrpUtils.getInputSteam(datafile), INI_CHARSET)) {
            return readIni(is);
        }
    }

    public static IrpDatabase readIni(Reader reader) throws IOException {
        Map<String, UnparsedProtocol> protocols = new LinkedHashMap<>(APRIORI_NUMBER_PROTOCOLS);
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
                    currentProtocol.put(documentationName, documentation.toString().trim());
                    addProtocol(protocols, new UnparsedProtocol(currentProtocol));
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
                    currentProtocol.put(nameName, payload);
            } else if (keyword.equals("irp")) {
                if (currentProtocol != null)
                    currentProtocol.put(irpName, payload);
            } else if (keyword.equals(usableName)) {
                if (currentProtocol != null)
                    currentProtocol.put(keyword, payload);
            } else if (keyword.length() > 1) {
                if (currentProtocol != null)
                    currentProtocol.put(keyword, payload);
            } else if (!line.isEmpty())
                logger.log(Level.FINER, "Ignored line: {0}", line);
        }
        if (currentProtocol != null) {
            currentProtocol.put(documentationName, documentation.toString().trim());
            addProtocol(protocols, new UnparsedProtocol(currentProtocol));
        }
        return new IrpDatabase(protocols, configFileVersion);
    }

    private static void addProtocol(Map<String, UnparsedProtocol> protocols, UnparsedProtocol proto) {
        if (!proto.isUsable() || proto.getName() == null || proto.getIrp() == null)
            return;

        String nameLower = proto.getName().toLowerCase(Locale.US);
        if (protocols.containsKey(nameLower))
            logger.log(Level.WARNING, "Multiple definitions of protocol `{0}''. Keeping the last.", nameLower);
        protocols.put(nameLower, proto);
    }

    private String configFileVersion;

    // The key is the protocol name folded to lower case. Case preserved name is in UnparsedProtocol.name.
    private Map<String, UnparsedProtocol> protocols;

    private IrpDatabase() {
        this.configFileVersion = "";
        protocols = new LinkedHashMap<>(APRIORI_NUMBER_PROTOCOLS);
    }

    public IrpDatabase(Reader reader) throws IOException, SAXException {
        this(XmlUtils.openXmlReader(reader, (Schema) null, true, true));
    }

    public IrpDatabase(InputStream inputStream) throws IOException, SAXException {
        this(XmlUtils.openXmlStream(inputStream, null, true, true));
    }

    public IrpDatabase(File file) throws IOException, SAXException {
        this(XmlUtils.openXmlFile(file, (Schema) null, true, true));
    }

    public IrpDatabase(String file) throws IOException, SAXException {
        this(IrpUtils.getInputSteam(file));
    }

    /**
     *
     * @param doc
     */
    public IrpDatabase(Document doc) {
        Element root = doc.getDocumentElement();
        configFileVersion = root.getAttribute("version");
        NodeList nodes = root.getElementsByTagNameNS(irpProtocolNS, "protocol");
        protocols = new LinkedHashMap<>(nodes.getLength()); // to preserve order
        for (int i = 0; i < nodes.getLength(); i++)
            addProtocol((Element)nodes.item(i));
        // FIXME
        //expand();
    }

    private IrpDatabase(Map<String, UnparsedProtocol> protocols, String version) {
        this.configFileVersion = version;
        this.protocols = protocols;
    }

    private Document emptyDocument() {
        Document doc = XmlUtils.newDocument(true);
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"IrpProtocols2html.xsl\"");
        doc.appendChild(pi);
        Element root = doc.createElementNS(irpProtocolNS, irpNamespacePrefix + ":protocols");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");
        root.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        root.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        root.setAttribute("xsi:schemaLocation", irpProtocolNS + " " + irpProtocolLocation);
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

        protocolNames.forEach((protocolName) -> {
            try {
                NamedProtocol protocol = getNamedProtocol(protocolName);
                Element element = protocol.toElement(document);
                root.appendChild(element);
            } catch (IrpException | ArithmeticException ex) {
                logger.log(Level.WARNING, "{0}; protocol ignored", ex.getMessage());
            }
        });

        return document;
    }

    /**
     * @return the configFileVersion
     */
    public final String getConfigFileVersion() {
        return configFileVersion;
    }

    private void dump(PrintStream ps, String name) {
        ps.println(protocols.get(name));
    }

    private void dump(PrintStream ps) {
        protocols.keySet().stream().forEach((s) -> {
            dump(ps, s);
        });
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

    public final String getIrp(String name) {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot == null ? null : prot.getIrp();
    }

    public final Set<String> getNames() {
        return protocols.keySet();
    }

    public final int size() {
        return protocols.size();
    }

    public List<String> getMatchingNamesRegexp(String regexp) {
        Pattern pattern = Pattern.compile(regexp.toLowerCase(Locale.US));
        List<String> result = new ArrayList<>(10);
        protocols.keySet().stream().filter((candidate) -> (pattern.matcher(candidate).matches())).forEach((candidate) -> {
            result.add(candidate);
        });
        return result;
    }

    public List<String> getMatchingNamesExact(String string) {
        List<String> result = new ArrayList<>(10);
        protocols.keySet().stream().filter((candidate) -> (candidate.equalsIgnoreCase(string))).forEachOrdered((candidate) -> {
            result.add(candidate);
        });
        return result;
    }

    public List<String> getMatchingNames(Iterable<String> iterable, boolean regexp) {
        List<String> result = new ArrayList<>(10);
        for (String s : iterable)
            result.addAll(regexp ? getMatchingNamesRegexp(s) : getMatchingNamesExact(s));

        return result;
    }

    public final String getDocumentation(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.getDocumentation();
    }

    public String getFirstProperty(String name, String key) {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot == null ? null : prot.getFirstProperty(key);
    }

    public NamedProtocol getNamedProtocol(String name) throws UnknownProtocolException, IrpSemanticException, InvalidNameException, UnassignedException {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        if (prot == null)
            throw new UnknownProtocolException(name);
        return prot.toNamedProtocol();
    }

    public List<NamedProtocol> getNamedProtocol(Collection<String> protocolNames) {
        List<NamedProtocol> list = new ArrayList<>(protocolNames.size());
        protocolNames.stream().forEach((protocolName) -> {
            try {
                list.add(getNamedProtocol(protocolName));
            } catch (IrpSemanticException | InvalidNameException | UnassignedException | UnknownProtocolException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        });
        return list;
    }

    public void expand() throws IrpSyntaxException {
        for (String protocol : protocols.keySet())
            expand(0, protocol);
    }

    private void expand(int depth, String name) throws IrpSyntaxException {
        UnparsedProtocol p = protocols.get(name);
        if (!p.getIrp().contains("{"))
            throw new IrpSyntaxException("IRP `" + p.getIrp() + "' does not contain `{'.");

        if (!p.getIrp().startsWith("{")) {
            String p_name = p.getIrp().substring(0, p.getIrp().indexOf('{')).trim();
            UnparsedProtocol ancestor = protocols.get(p_name.toLowerCase(Locale.US));
            if (ancestor != null) {
                String replacement = ancestor.getIrp().lastIndexOf('[') == -1 ? ancestor.getIrp()
                        : ancestor.getIrp().substring(0, ancestor.getIrp().lastIndexOf('['));
                logger.log(Level.FINEST, "Protocol {0}: `{1}'' replaced by `{2}''.", new Object[]{name, p_name, replacement});
                p.setUniqueProperty(irpName, p.getIrp().replaceAll(p_name, replacement));
                protocols.put(name, p);
                if (depth < maxRecursionDepth)
                    expand(depth + 1, name);
                else
                    logger.log(Level.SEVERE, "Recursion depth in expanding {0} exceeded.", name);
            }
        }
    }

    private void addProtocol(Element current) {
        addProtocol(protocols, new UnparsedProtocol(current));
    }

    public List<String> evaluateProtocols(List<String> protocols, boolean sort, boolean regexp) {
        List<String> list = (protocols == null || protocols.isEmpty())
                ? new ArrayList<>(getNames()) : getMatchingNames(protocols, regexp);
        if (sort)
            Collections.sort(list);
        return list;
    }

    public Protocol getProtocol(String protocolName) throws UnknownProtocolException, IrpSemanticException, InvalidNameException, UnassignedException {
        if (!isKnown(protocolName))
            throw new UnknownProtocolException(protocolName);
        return new Protocol(getIrp(protocolName));
    }

    public String getNormalFormIrp(String protocolName) throws UnknownProtocolException, IrpSemanticException, InvalidNameException, UnassignedException {
        Protocol protocol = getProtocol(protocolName);
        return protocol.normalFormIrpString();
    }

    private static class UnparsedProtocol {
        public static final int APRIORI_SIZE = 4;

        private Map<String, List<String>> map;

        UnparsedProtocol() {
            map = new HashMap<>(APRIORI_SIZE);
        }

        UnparsedProtocol(String irp) {
            this(unnamed, irp, null);
        }

        UnparsedProtocol(String name, String irp, String documentation) {
            this();
            addProperty(nameName, name);
            addProperty(irpName, irp);
            addProperty(documentationName, documentation);
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
            addProperty(nameName, element.getAttribute(nameName));
            String usable = element.getAttribute(usableName);
            if (!usable.isEmpty())
                addProperty(usableName, usable);
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;
                switch (e.getLocalName()) {
                    case irpName:
                        addProperty(irpName, e.getTextContent().replaceAll("\\s+", ""));
                        break;
                    case documentationName:
                        addProperty(documentationName, e.getTextContent());
                        break;
                    case parameterName:
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
            return getFirstProperty(nameName);
        }

        String getIrp() {
            return getFirstProperty(irpName);
        }

        String getDocumentation() {
            return getFirstProperty(documentationName);
        }

        boolean isUsable() {
            String str = getFirstProperty(usableName);
            return str == null || Boolean.parseBoolean(str) || str.equalsIgnoreCase("yes");
        }

        NamedProtocol toNamedProtocol() throws IrpSemanticException, InvalidNameException, UnassignedException {
            return new NamedProtocol(getName(), getIrp(), getDocumentation(),
                    getFirstProperty(frequencyToleranceName), getFirstProperty(absoluteToleranceName), getFirstProperty(relativeToleranceName),
                    getFirstProperty(decodableName), getProperties(preferOverName));
        }

        @Override
        public String toString() {
            return getName() + "\t" + getIrp();
        }

        Element toElement(Document doc) {
            Element element = doc.createElementNS(irpProtocolNS, irpNamespacePrefix + ":" + protocolName);
            for (Map.Entry<String, List<String>> kvp : map.entrySet()) {
                switch (kvp.getKey()) {
                    case nameName:
                        element.setAttribute(nameName, kvp.getValue().get(0));
                        element.setAttribute(cNameName, IrpUtils.toCName(kvp.getValue().get(0)));
                        break;
                    case usableName:
                        element.setAttribute(usableName, Boolean.toString(!kvp.getValue().get(0).equals("no")));
                        break;
                    case irpName: {
                        Element irp = doc.createElementNS(irpProtocolNS, irpNamespacePrefix + ":" + irpName);
                        irp.appendChild(doc.createCDATASection(kvp.getValue().get(0)));
                        element.appendChild(irp);
                    }
                    break;
                    case documentationName:
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
                            Element param = doc.createElementNS(irpProtocolNS, irpNamespacePrefix + ":" + parameterName);
                            param.setAttribute(nameName, kvp.getKey());
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
