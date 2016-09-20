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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: allow more than one file; include directive.

/**
 * This class is a data bases manager for the data base of IRP protocols.
 * It reads a configuration file containing definitions for IR format in the IRP-Notation.
 */
public class IrpDatabase {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());
    private static JCommander argumentParser;

    public static final String irpProtocolNS = "http://www.harctoolbox.org/irp-protocols";
    public static final String irpProtocolLocation = "file:///home/bengt/harctoolbox/IrpTransmogrifier/src/main/schemas/irp-protocols.xsd";
    public static final String irpProtocolPrefix = "irp";


    private final static int maxRecursionDepth = 5;
    public static boolean isKnown(String protocolsPath, String protocol) throws FileNotFoundException, IncompatibleArgumentException, UnsupportedEncodingException, IOException, SAXException {
        return (new IrpDatabase(protocolsPath)).isKnown(protocol);
    }
    /**
     * Static version of getIrp.
     *
     * @param configFilename
     * @param protocolName
     * @return String with IRP representation
     * @throws java.io.UnsupportedEncodingException
     * @throws org.xml.sax.SAXException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     */
    public static String getIrp(String configFilename, String protocolName) throws UnsupportedEncodingException, IOException, SAXException, IncompatibleArgumentException {
        IrpDatabase irpMaster = null;
        try {
            irpMaster = new IrpDatabase(configFilename);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return irpMaster == null ? null : irpMaster.getIrp(protocolName);
    }
    /**
     *
     * @param datafile
     * @return
     * @throws IOException
     */
    public static Document readIni(String datafile) throws IOException {
        try (InputStreamReader is = new InputStreamReader(IrpUtils.getInputSteam(datafile), "US-ASCII")) {
            return readIni(is);
        }
    }
    /**
     *
     * @param reader
     * @return
     */
    public static Document readIni(Reader reader) {
        Document doc = XmlUtils.newDocument(true);
        Element root = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":protocols");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");
        root.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        root.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        root.setAttribute("xsi:schemaLocation", irpProtocolNS + " " + irpProtocolLocation);
        doc.appendChild(root);
        //protocols = new LinkedHashMap<>();
        //encoding = charSet;
        BufferedReader in = new BufferedReader(reader);
        HashMap<String, String> currentProtocol = null;
        int lineNo = 0;
        try {
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
                    payload = payload.substring(0, payload.length()-1);
                    payload = payload.trim();
                    String continuation = in.readLine();
                    if (continuation != null)
                        continuation = continuation.trim();
                    payload += continuation;
                    lineNo++;
                }

                if (line.equals("[version]")) {
                    String configFileVersion = in.readLine();
                    lineNo++;
                    root.setAttribute("version", configFileVersion);
                } else if (line.equals("[protocol]")) {
                    if (currentProtocol != null) {
                        currentProtocol.put(UnparsedProtocol.documentationName, documentation.toString().trim());
                        root.appendChild(toElement(doc, currentProtocol));
                    }
                    currentProtocol = new HashMap<>(3);
                    documentation = new StringBuilder(1000);
                    isDocumentation = false;
                } else if (isDocumentation && currentProtocol != null) {
                    // Everything is added to the documentation
                    if (line.isEmpty())
                        documentation.append("\n\n");
                    else {
                        if (documentation.length() > 0 && !documentation.substring(documentation.length()-1).equals("\n"))
                            documentation.append(" ");
                        documentation.append(line);
                    }
                } else if (line.equals("[documentation]")) {
                    if (currentProtocol != null)
                        isDocumentation = true;
                } else if (keyword.equals("name")) {
                    if (currentProtocol != null)
                        currentProtocol.put(UnparsedProtocol.nameName, payload);
                } else if (keyword.equals("irp")) {
                    if (currentProtocol != null)
                        currentProtocol.put(UnparsedProtocol.irpName, payload);
                } else if (keyword.equals(UnparsedProtocol.usableName)) {
                    if (currentProtocol != null)
                        currentProtocol.put(keyword, payload);
                } else if (keyword.length() > 1) {
                    if (currentProtocol != null)
                        currentProtocol.put(keyword, payload);
                } else {
                    if (!line.isEmpty())
                        logger.log(Level.FINER, "Ignored line: {0}", line);
                }
            }
            if (currentProtocol != null) {
                currentProtocol.put(UnparsedProtocol.documentationName, documentation.toString().trim());
                root.appendChild(toElement(doc, currentProtocol));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private static Element toElement(Document doc, HashMap<String, String> currentProtocol) {
        Element element = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":protocol");
        for (Map.Entry<String, String> kvp : currentProtocol.entrySet()) {
            switch (kvp.getKey()) {
                case UnparsedProtocol.nameName:
                    element.setAttribute(UnparsedProtocol.nameName, kvp.getValue());
                    element.setAttribute("c-name", toCName(kvp.getValue()));
                    break;
                case UnparsedProtocol.usableName:
                    element.setAttribute(UnparsedProtocol.usableName, Boolean.toString(!kvp.getValue().equals("no")));
                    break;
                case UnparsedProtocol.irpName: {
                    Element irp = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":" + UnparsedProtocol.irpName);
                    irp.setTextContent(kvp.getValue());
                    element.appendChild(irp);
                }
                break;
                case UnparsedProtocol.documentationName: {
                    Element docu = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":" + UnparsedProtocol.documentationName);
                    docu.setTextContent(kvp.getValue());
                    element.appendChild(docu);
                }
                break;
                default: {
                    Element param = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":" + UnparsedProtocol.parameterName);
                    param.setAttribute("name", kvp.getKey());
                    param.setTextContent(kvp.getValue());
                    element.appendChild(param);
                }
            }
        }
        return element;
    }
    public static String toCName(String name) {
        String newName = name.replaceAll("[^0-9A-Za-z_]", "");
        return newName.matches("\\d.*") ? ("X" + newName) : newName;
    }

    private String configFileVersion;
    private String encoding;

    // The key is the protocol name folded to lower case. Case preserved name is in UnparsedProtocol.name.
    private LinkedHashMap<String, UnparsedProtocol> protocols;
    private IrpDatabase() {
        this.configFileVersion = "";
        protocols = new LinkedHashMap<>(200);
    }
    /**
     * Sets up a new IrpMaster from its first argument.
     *
     * @param reader
     * @throws FileNotFoundException
     * @throws org.xml.sax.SAXException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws java.io.UnsupportedEncodingException
     */

    public IrpDatabase(Reader reader) throws IOException, SAXException, IncompatibleArgumentException {
        this(XmlUtils.openXmlReader(reader, (Schema) null, true, true));
    }
    public IrpDatabase(InputStream inputStream) throws IOException, SAXException, IncompatibleArgumentException {
        this(XmlUtils.openXmlStream(inputStream, null, true, true));
    }
    public IrpDatabase(File file) throws IOException, SAXException, IncompatibleArgumentException {
        this(XmlUtils.openXmlFile(file, (Schema) null, true, true));
    }
    public IrpDatabase(String file) throws IOException, SAXException, IncompatibleArgumentException {
        this(IrpUtils.getInputSteam(file));
    }
    /**
     *
     * @param doc
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     */
    public IrpDatabase(Document doc) throws IncompatibleArgumentException {
        Element root = doc.getDocumentElement();
        configFileVersion = root.getAttribute("version");
        NodeList nodes = root.getElementsByTagNameNS(irpProtocolNS, "protocol");
        protocols = new LinkedHashMap<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++)
            addProtocol((Element)nodes.item(i));
        // FIXME
        //expand();
    }

    public Document toDocument() {
        Document doc = XmlUtils.newDocument(true);
        Element root = doc.createElementNS(irpProtocolNS, irpProtocolPrefix + ":protocols");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");
        root.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        root.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        root.setAttribute("xsi:schemaLocation", irpProtocolNS + " " + irpProtocolLocation);
        root.setAttribute("version", configFileVersion);
        doc.appendChild(root);

        for (UnparsedProtocol protocol : this.protocols.values()) {
            Element element = toElement(doc, protocol.map);
            root.appendChild(element);
        }

        return doc;
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


    /**
     *
     * @param name
     * @return String with IRP representation
     */
    public final String getIrp(String name) {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot == null ? null : prot.getIrp();
    }


    public final Set<String> getNames() {
        return protocols.keySet();
    }

    public List<String> getMatchingNames(String regexp) {
        Pattern pattern = Pattern.compile(regexp.toLowerCase(Locale.US));
        List<String> result = new ArrayList<>(10);
        protocols.keySet().stream().filter((candidate) -> (pattern.matcher(candidate).matches())).forEach((candidate) -> {
            result.add(candidate);
        });
        return result;
    }

    public List<String> getMatchingNames(Iterable<String> iterable) {
        List<String> result = new ArrayList<>(10);
        for (String s : iterable) {
            result.addAll(getMatchingNames(s));
        }
        return result;
    }

    public final String getDocumentation(String name) {
        UnparsedProtocol prot = protocols.get(name);
        return prot == null ? null : prot.getDocumentation();
    }

    public String getProperty(String name, String key) {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        return prot == null ? null : prot.getProperty(key);
    }

    public NamedProtocol getNamedProtocol(String name) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException {
        UnparsedProtocol prot = protocols.get(name.toLowerCase(Locale.US));
        if (prot == null)
            throw new UnknownProtocolException(name);
        return prot.toNamedProtocol();
    }


    public void expand() throws IncompatibleArgumentException {
        for (String protocol : protocols.keySet())
            expand(0, protocol);
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

    private void addProtocol(Element current) {

        if (current.getAttribute(UnparsedProtocol.usableName).equals("no")
                || current.getAttribute(UnparsedProtocol.usableName).equals("false"))
            return;

        UnparsedProtocol proto = new UnparsedProtocol(current);
        if (proto.getName() == null || proto.getIrp() == null)
            return;

        String nameLower = proto.getName().toLowerCase(Locale.US);

        if (protocols.containsKey(nameLower))
            logger.log(Level.WARNING, "Multiple definitions of protocol `{0}''. Keeping the last.", nameLower);
        protocols.put(nameLower, proto);
    }

    private static class UnparsedProtocol {

        public static final String unnamed = "unnamed_protocol";
        public static final String nameName = "name";
        public static final String irpName = "irp";
        public static final String usableName = "usable";
        public static final String documentationName = "documentation";
        private static final String parameterName = "parameter";

        private HashMap<String, String> map;

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
            map = new LinkedHashMap<>(4);
        }

        UnparsedProtocol(HashMap<String, String> map) {
            this.map = map;
        }

        UnparsedProtocol(Element element) {
            this();
            map.put(nameName, element.getAttribute(nameName));
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;
                switch (e.getLocalName()) {
                    case irpName:
                        map.put(irpName, e.getTextContent());
                        break;
                    case documentationName:
                        map.put(documentationName, e.getTextContent());
                        break;
                    case parameterName:
                        map.put(e.getAttribute("name"), e.getTextContent());
                        break;
                    default:
                        throw new InternalError("unknown tag: " + e.getTagName());
                }
            }
        }
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

        NamedProtocol toNamedProtocol() throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException {
            return new NamedProtocol(getName(), getIrp(), getDocumentation());
        }

        @Override
        public String toString() {
            return getName() + "\t" + getIrp();
        }
    }
}
