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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XML_NS_URI;
import javax.xml.validation.Schema;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.xml.DumbHtmlRenderer;
import org.harctoolbox.xml.XmlUtils;
import static org.harctoolbox.xml.XmlUtils.HTML_NAMESPACE_URI;
import static org.harctoolbox.xml.XmlUtils.SCHEMA_LOCATION_ATTRIBUTE_NAME;
import static org.harctoolbox.xml.XmlUtils.W3C_SCHEMA_NAMESPACE_ATTRIBUTE_NAME;
import static org.harctoolbox.xml.XmlUtils.XINCLUDE_NAMESPACE_ATTRIBUTE_NAME;
import static org.harctoolbox.xml.XmlUtils.XINCLUDE_NAMESPACE_URI;
import static org.harctoolbox.xml.XmlUtils.XML_NAMESPACE_ATTRIBUTE_NAME;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * This class is a data bases manager for the data base of IRP protocols.
 * It reads a configuration file containing definitions for IR format in the IRP-Notation.
 */
// NOTE: The program must work also if the schema cannot be retrieved.
public final class IrpDatabase implements Iterable<NamedProtocol> {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());

    public static final String DEFAULT_CONFIG_FILE = "/IrpProtocols.xml";
    public static final String IRP_PROTOCOL_NS = "http://www.harctoolbox.org/irp-protocols";
    public static final String IRP_SCHEMA_FILE = "/irp-protocols.xsd";
    public static final String IRP_PROTOCOL_SCHEMA_LOCATION = "http://www.harctoolbox.org/schemas/irp-protocols.xsd";
    public static final String IRP_NAMESPACE_PREFIX = "irp";
    private static final int MAX_RECURSION_DEPTH = 5;

    public static final String UNNAMED = "unnamed_protocol";
    public static final String PROTOCOL_NAME = "protocol";
    public static final String PROTOCOLS_NAME = "protocols";
    public static final String NAME_NAME = "name";
    public static final String CNAME_NAME = "c-name";
    public static final String IRP_NAME = "irp";
    public static final String IRP_ELEMENT_NAME = "Irp";
    public static final String USABLE_NAME = "usable";
    public static final String VERSION_NAME = "version";
    public static final String PROG_VERSION_NAME = "program-version";
    public static final String DOCUMENTATION_NAME = "documentation";
    public static final String DOCUMENTATION_ELEMENT_NAME = "Documentation";
    public static final String PARAMETER_NAME = "parameter";
    public static final String DECODABLE_NAME = "decodable";
    public static final String FREQUENCY_TOLERANCE_NAME = "frequency-tolerance";
    public static final String FREQUENCY_LOWER_NAME = "frequency-lower";
    public static final String FREQUENCY_UPPER_NAME = "frequency-upper";
    public static final String RELATIVE_TOLERANCE_NAME = "relative-tolerance";
    public static final String ABSOLUTE_TOLERANCE_NAME = "absolute-tolerance";
    public static final String MINIMUM_LEADOUT_NAME = "minimum-leadout";
    public static final String PREFER_OVER_NAME = "prefer-over";
    public static final String ALT_NAME_NAME = "alt_name";
    public static final String REJECT_REPEATLESS_NAME = "reject-repeatless";
    public static final String TYPE_NAME = "type";
    public static final String XML_NAME = "xml";
    public static final String FALSE_NAME = "false";
    public static final String HTML_NAME = "Html";
    public static final String VALUE_NAME = "Value";
    public static final String PARAMETERS_NAME = "Parameters";
    public static final String PARAMETER_ELEMENT_NAME = "Parameter";
    public static final String NAMED_PROTOCOLS_NAME = "named-protocols";
    public static final String DECODE_ONLY_NAME = "decode-only";
    public static final String PROTOCOL_NAME_NAME = "protocolName";
    public static final String PROTOCOL_CNAME_NAME = "cProtocolName";
    public static final String META_DATA_NAME = "metaData";

    private static boolean validating = false;
    private static Schema schema = null;

    public static boolean isValidating() {
        return validating;
    }

    public static void setValidating(boolean newValidating) throws SAXException {
        validating = newValidating;
        if (validating) {
            if (schema == null)
                schema = XmlUtils.readSchema(IrpDatabase.class.getResourceAsStream(IRP_SCHEMA_FILE));
        } else
            schema = null;
    }

    public static Schema getSchema() {
        return schema;
    }

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
                || key.equals(FREQUENCY_LOWER_NAME)
                || key.equals(FREQUENCY_UPPER_NAME)
                || key.equals(RELATIVE_TOLERANCE_NAME)
                || key.equals(ABSOLUTE_TOLERANCE_NAME)
                || key.equals(MINIMUM_LEADOUT_NAME)
                || key.equals(PREFER_OVER_NAME)
                || key.equals(ALT_NAME_NAME);
    }

    public static boolean isKnown(String protocolsPath, String protocol) throws IOException, IrpParseException, SAXException {
        return (new IrpDatabase(protocolsPath)).isKnown(protocol);
    }

    /**
     * Static version of getIrp.
     *
     * @param configFilename
     * @param protocolName
     * @return String with IRP representation
     * @throws org.harctoolbox.irp.IrpParseException
     * @throws org.harctoolbox.irp.UnknownProtocolException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public static String getIrp(String configFilename, String protocolName) throws IrpParseException, UnknownProtocolException, IOException, SAXException {
        IrpDatabase irpMaster = new IrpDatabase(configFilename);
        return irpMaster.getIrp(protocolName);
    }

    public static IrpDatabase parseIrp(String protocolName, String irp, String documentation) throws IrpParseException {
        return parseIrp(protocolName, irp, XmlUtils.stringToDocumentFragment(documentation));
    }

    public static IrpDatabase parseIrp(String protocolName, String irp, DocumentFragment documentation) throws IrpParseException {
        Map<String, UnparsedProtocol> protocols = new HashMap<>(1);
        UnparsedProtocol protocol = new UnparsedProtocol(protocolName, irp, documentation);
        protocols.put(protocolName, protocol);
        return new IrpDatabase(protocols);
    }

    public static IrpDatabase parseIrp(Map<String, String> map) throws IrpParseException {
        return new IrpDatabase(toUnparsedProtocols(map));
    }

    private static Document openXmlStream(InputStream inputStream) throws IOException, SAXException {
        return XmlUtils.openXmlStream(inputStream, getSchema(), true, true);
    }

    private static Document openXmlReader(Reader reader) throws IOException, SAXException {
        return XmlUtils.openXmlReader(reader, getSchema(), true, true);
    }

    private static Document openXmlFile(File file) throws IOException, SAXException {
        return XmlUtils.openXmlFile(file, getSchema(), true, true);
    }

    private static Map<String, UnparsedProtocol> toUnparsedProtocols(Map<String, String>protocols) {
        Map<String, UnparsedProtocol> map = new HashMap<>(protocols.size());
        protocols.entrySet().forEach((kvp) -> {
            String name = kvp.getKey().toLowerCase(Locale.US);
            map.put(name, new UnparsedProtocol(name, kvp.getValue(), null));
        });
        return map;
    }

    private static InputStream mkStream(String file) throws FileNotFoundException {
        return (file == null || file.isEmpty()) ? IrpDatabase.class.getResourceAsStream(DEFAULT_CONFIG_FILE) : IrCoreUtils.getInputSteam(file);
    }

    public static IrpDatabase newDefaultIrpDatabase() {
        try {
            return new IrpDatabase((String) null);
        } catch (IOException | IrpParseException | SAXException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    private static String getDocumentation(DocumentFragment frag) {
        return DumbHtmlRenderer.render(frag);
    }

    private final StringBuilder version;

    // The key is the protocol name folded to lower case. Case preserved name is in UnparsedProtocol.name.
    private Map<String, UnparsedProtocol> protocols;

    private Map<String, String> aliases;

    public IrpDatabase(Reader reader) throws IOException, IrpParseException, SAXException {
        this(openXmlReader(reader));
    }

    public IrpDatabase(InputStream inputStream) throws IOException, IrpParseException, SAXException {
        this(openXmlStream(inputStream));
    }

    public IrpDatabase(File file) throws IOException, IrpParseException, SAXException {
        this(openXmlFile(file));
    }

    public IrpDatabase(String file) throws IOException, IrpParseException, SAXException {
        this(mkStream(file));
    }

    public IrpDatabase(Iterable<File> files) throws IrpParseException, IOException, SAXException {
        this();
        for (File file : files)
            patch(file);

        expand();
        rebuildAliases();
    }

    private IrpDatabase() throws IrpParseException {
        this(new LinkedHashMap<>(16));
    }

    private IrpDatabase(Map<String, UnparsedProtocol> protocols) throws IrpParseException {
        this.version = new StringBuilder(64);
        this.protocols = protocols;
        this.aliases = new LinkedHashMap<>(8);
        expand();
        rebuildAliases();
    }

    public IrpDatabase(Document doc) throws IrpParseException {
        this();
        patch(doc);
        expand();
        rebuildAliases();
    }

    public void patch(Reader reader) throws IOException, SAXException {
        patch(openXmlReader(reader));
    }

    public void patch(File file) throws IOException, SAXException {
        patch(openXmlFile(file));
    }

    public void patch(String file) throws IOException, SAXException {
        patch(new File(file));
    }

    public void patch(Document document) {
        Element root = document.getDocumentElement();
        appendToVersion(root.getAttribute("version"));
        NodeList nodes = root.getElementsByTagNameNS(IRP_PROTOCOL_NS, PROTOCOL_NAME);
        for (int i = 0; i < nodes.getLength(); i++)
            patchProtocol((Element) nodes.item(i));
    }

    private void patchProtocol(Element current) {
        patchProtocol(new UnparsedProtocol(current));
    }

    private void patchProtocol(UnparsedProtocol proto) {
        String name = proto.getName();
        if (name == null) // likely usable = "false"
            return;

        String nameLower = name.toLowerCase(Locale.US);
        UnparsedProtocol existing = protocols.get(nameLower);
        if (existing != null) {
            if (proto.isEmpty())
                protocols.remove(nameLower);
            else
                existing.patch(proto);
        } else {
            protocols.put(nameLower, proto);
        }

        buildAliases(proto);
    }

    public void addProtocol(String protocolName, String irp) throws IrpParseException {
        addProtocol(protocolName, irp, null);
    }

    public void addProtocol(String protocolName, String irp, DocumentFragment doc) throws IrpParseException {
        patchProtocol(new UnparsedProtocol(protocolName, irp, doc));
        expand(protocolName);
    }

    private Document emptyDocument() {
        Document doc = XmlUtils.newDocument(true);
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"IrpProtocols2html.xsl\"");
        doc.appendChild(pi);
        Element root = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":" + PROTOCOLS_NAME);
        root.setAttribute(W3C_SCHEMA_NAMESPACE_ATTRIBUTE_NAME, W3C_XML_SCHEMA_INSTANCE_NS_URI);
        root.setAttribute(XINCLUDE_NAMESPACE_ATTRIBUTE_NAME, XINCLUDE_NAMESPACE_URI);
        root.setAttribute(XML_NAMESPACE_ATTRIBUTE_NAME, XML_NS_URI);
        root.setAttribute(XMLNS_ATTRIBUTE, HTML_NAMESPACE_URI);
        root.setAttribute(SCHEMA_LOCATION_ATTRIBUTE_NAME, IRP_PROTOCOL_NS + " " + IRP_PROTOCOL_SCHEMA_LOCATION);
        root.setAttribute(VERSION_NAME, version.toString());
        doc.appendChild(root);
        return doc;
    }

    public Document toDocument() {
        return toDocument(protocols.keySet());
    }

    public Document toDocument(Iterable<String> list) {
        Document doc = emptyDocument();
        Element root = doc.getDocumentElement();
        for (String protocolName : list)
            root.appendChild(protocols.get(protocolName.toLowerCase(Locale.US)).toElement(doc));

        return doc;
    }

    public Document toXml(List<String> protocolNames, Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance, boolean override) {
        Document document = XmlUtils.newDocument();
        Element root = document.createElement("NamedProtocols");
        root.setAttribute(PROG_VERSION_NAME, Version.versionString);
        root.setAttribute(VERSION_NAME, this.getConfigFileVersion());
        root.setAttribute(ABSOLUTE_TOLERANCE_NAME, Double.toString(IrCoreUtils.getAbsoluteTolerance(absoluteTolerance)));
        root.setAttribute(RELATIVE_TOLERANCE_NAME, Double.toString(IrCoreUtils.getRelativeTolerance(relativeTolerance)));
        root.setAttribute(FREQUENCY_TOLERANCE_NAME, Double.toString(IrCoreUtils.getFrequencyTolerance(frequencyTolerance)));
        document.appendChild(root);

        protocolNames.forEach((String pname) -> {
            try {
                logger.log(Level.FINE, "Processing {0} ...", pname);
                NamedProtocol protocol = getNamedProtocol(pname);
                Element element = protocol.toElement(document, absoluteTolerance, relativeTolerance, frequencyTolerance, override);
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
        return version.toString();
    }

    public boolean isAlias(String protocol) {
        return aliases.containsKey(protocol.toLowerCase(Locale.US));
    }

    public String expandAlias(String protocol) {
        return isAlias(protocol) ? aliases.get(protocol.toLowerCase(Locale.US)) : protocol;
    }

    public boolean isKnown(String protocol) {
        return protocol != null && protocols.containsKey(protocol.toLowerCase(Locale.US));
    }

    public boolean isKnownExpandAlias(String protocol) {
        return isKnown(expandAlias(protocol));
    }

    public String getIrp(String name) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot.getIrp();
    }

    public String getIrpExpandAlias(String name) throws UnknownProtocolException {
        return getIrp(expandAlias(name));
    }

    private UnparsedProtocol getUnparsedProtocol(String protocolName) throws UnknownProtocolException {
        UnparsedProtocol unparsedProtocol = protocols.get(protocolName.toLowerCase(Locale.US));
        if (unparsedProtocol == null)
            throw new UnknownProtocolException(protocolName);
        return unparsedProtocol;
    }

    /**
     * Returns the keys of the protocol data base, which happens to be the protocol names converted to lower case.
     * @return
     */
    public Set<String> getKeys() {
        return protocols.keySet();
    }

    public List<String> getNames() {
        List<String> answer = new ArrayList<>(protocols.size());
        protocols.values().forEach((prot) -> {
            answer.add(prot.getName());
        });
        return answer;
    }

    public Set<String> getAliases() {
        return aliases.keySet();
    }

    public String getName(String name) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        return prot.getName();
    }

    public String getCName(String name) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(name);
        String storedCName = prot.getCName();
        return storedCName != null ? storedCName : IrpUtils.toCIdentifier(prot.getName());
    }

    public String getNameExpandAlias(String name) throws UnknownProtocolException {
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
                String s = urlDecode ? URLDecoder.decode(str, "UTF-8") : str; // See Javadoc for URLDecoder.decode
                result.addAll(regexp ? getMatchingNamesRegexp(s) : getMatchingNamesExact(s));
            } catch (UnsupportedEncodingException ex) {
                throw new ThisCannotHappenException();
            }
        }

        return result;
    }

    public String getDocumentation(String protocolName) throws UnknownProtocolException {
        DocumentFragment fragment = getHtmlDocumentation(protocolName);
        return fragment == null ? null : getDocumentation(fragment);
    }

    public DocumentFragment getHtmlDocumentation(String protocolName) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        return prot.getHtmlDocumentation();
    }

    public String getDocumentationExpandAlias(String protocolName) throws UnknownProtocolException {
        return getDocumentation(expandAlias(protocolName));
    }

    public String getFirstProperty(String protocolName, String key) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        return prot.getFirstProperty(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> getProperties(String protocolName, String key) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        return prot.getProperties(key);
    }

    public void addProperty(String protocolName, String key, String value) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        prot.addProperty(key, value);
    }

    public void setProperties(String protocolName, String key, List<String> properties) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        prot.setProperties(key, properties);
    }

    public void removeProperties(String protocolName, String key) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        prot.removeProperties(key);
    }

    @SuppressWarnings("unchecked")
    public List<DocumentFragment> getXmlProperties(String protocolName, String key) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        return prot.getXmlProperties(key);
    }

    public void setXmlProperties(String protocolName, String key, List<DocumentFragment> properties) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        prot.setXmlProperties(key, properties);
    }

    public void removeXmlProperties(String protocolName, String key) throws UnknownProtocolException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        prot.removeProperties(key);
    }

    public NamedProtocol getNamedProtocol(String protocolName) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        UnparsedProtocol prot = getUnparsedProtocol(protocolName);
        return prot.toNamedProtocol();
    }

    public NamedProtocol getNamedProtocolExpandAlias(String protocolName) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        return getNamedProtocol(expandAlias(protocolName));
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

    private void expand() throws IrpParseException {
        for (String protocol : protocols.keySet())
            expand(protocol);
    }

    private void expand(String name) throws IrpParseException {
        expand(0, name);
    }

    private void expand(int depth, String name) throws IrpParseException {
        UnparsedProtocol p = protocols.get(name.toLowerCase(Locale.US));
        if (!p.getIrp().contains("{"))
            throw new IrpParseException(p.getIrp(), "`{' not found.");

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

    public void remove(List<String> blackList) throws UnknownProtocolException {
        if (blackList == null)
            return;

        for (String protocol : blackList)
            remove(protocol);
    }

    public void remove(String protocolName) throws UnknownProtocolException {
        if (!isKnown(protocolName))
            throw new UnknownProtocolException(protocolName);

        protocols.remove(protocolName.toLowerCase(Locale.US));
        removeAliases(protocolName);
    }

    private void removeAliases(String protocolName) {
        ArrayList<String> result = new ArrayList<>(4);
        aliases.entrySet().stream().filter((kvp) -> (kvp.getValue().equals(protocolName))).forEachOrdered((kvp) -> {
            result.add(kvp.getKey());
        });
        result.forEach((s) -> {
            aliases.remove(s);
        });
    }

    public List<String> evaluateProtocols(List<String> protocols, boolean sort, boolean regexp, boolean urlDecode) {
        List<String> list = (protocols == null || protocols.isEmpty())
                ? new ArrayList<>(getKeys()) : getMatchingNames(protocols, regexp, urlDecode);
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

    /**
     * Checks if the data base is sorted with respect to the protocol name's.
     * @return First offending protocol, or null if the database is sorted.
     */
    public String checkSorted() {
        String last = " ";
        for (String protocol : protocols.keySet()) {
            if (protocol.compareTo(last) < 0)
                return protocol;

            last = protocol;
        }
        return null;
    }

    public IrSignal render(String protocolName, Map<String, Long> params) throws IrpException {
        Protocol protocol = getProtocolExpandAlias(protocolName);
        try {
            return protocol.toIrSignal(params);
        } catch (OddSequenceLengthException ex) {
            throw new IrpException("IrSequence does not end with a gap,");
        }
    }

    /**
     * This is a comparatively expensive operation, while its next()
     * performs actual parsing of the IRP string.
     * @return
     */
    @Override
    public Iterator<NamedProtocol> iterator() {
        return new NamedProtocolIterator(protocols);
    }

    private void appendToVersion(String version) {
        if (version.isEmpty())
            return;

        if (this.version.length() > 0)
            this.version.append("+");
        this.version.append(version);
    }

    private void buildAliases(UnparsedProtocol proto) {
        List<String> altNameList = proto.getProperties(ALT_NAME_NAME);
        if (altNameList == null)
            return;
        String nameLower = proto.getName().toLowerCase(Locale.US);
        altNameList.stream().map((altName) -> {
            String old = aliases.get(altName.toLowerCase(Locale.US));
            if (old != null && !old.equals(nameLower))
                logger.log(Level.WARNING, "alt_name \"{0}\" defined more than once, to different targets. Keeping the last.", altName);
            return altName;
        }).forEachOrdered((altName) -> {
            aliases.put(altName.toLowerCase(Locale.US), proto.getName().toLowerCase(Locale.US));
        });
    }

    private void rebuildAliases() {
        aliases.clear();
        this.protocols.values().forEach((protocol) -> {
            buildAliases(protocol);
        });
    }

    public String getVersion() {
        return version.toString();
    }

    private static class NamedProtocolIterator implements Iterator<NamedProtocol> {
        private Iterator<UnparsedProtocol> unparsedIterator;

        private NamedProtocolIterator(Map<String, UnparsedProtocol> map) {
            unparsedIterator = map.values().iterator();
        }

        @Override
        public boolean hasNext() {
            return unparsedIterator.hasNext();
        }

        @Override
        public NamedProtocol next() {
            try {
                UnparsedProtocol unparsed = unparsedIterator.next();
                return unparsed.toNamedProtocol();
            } catch (IrpException ex) {
                throw new ThisCannotHappenException(ex);
            }
        }
    }

    private static class UnparsedProtocol {
        private static final int APRIORI_SIZE = 4;

        private static DocumentFragment nodeListToDocumentFragment(NodeList childNodes) {
            Document doc = XmlUtils.newDocument();
            DocumentFragment fragment = doc.createDocumentFragment();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                fragment.appendChild(doc.importNode(node, true));
            }
            return fragment;
        }

        private static <T> void patchMap(Map<String, List<T>> existingMap, Map<String, List<T>> patchMap) {
            patchMap.entrySet().forEach((Map.Entry<String, List<T>> kvp) -> {
                String name = kvp.getKey();
                if (!(name.equals(NAME_NAME))) {
                    List<T> newList = kvp.getValue();
                    if (newList == null)
                        existingMap.remove(name);
                    else if (existingMap.containsKey(name)) {
                        List<T> oldList = existingMap.get(name);
                        if (name.equals(DOCUMENTATION_NAME) || name.equals(IRP_NAME))
                            oldList.clear();

                        newList.forEach((t) -> {
                            if (t == null)
                                oldList.clear();
                            else if (!oldList.contains(t))
                                oldList.add(t);
                        });
                    } else {
                        existingMap.put(name, newList);
                    }
                }
            });
        }

        @SuppressWarnings("unchecked")
        private static <T> List<T> getOrEmptyList(Map<String, List<T>> map, String key) {
            List<T> list = map.get(key);
            return list != null ? list : Collections.EMPTY_LIST;
        }

        private Map<String, List<String>> map;
        private Map<String, List<DocumentFragment>> xmlMap;

        UnparsedProtocol() {
            map = new LinkedHashMap<>(APRIORI_SIZE); // want to preserve order
            xmlMap = new HashMap<>(APRIORI_SIZE);
        }

        UnparsedProtocol(String irp) {
            this(UNNAMED, irp, null);
        }

        UnparsedProtocol(String name, String irp, DocumentFragment documentation) {
            this();
            addProperty(NAME_NAME, name);
            addProperty(IRP_NAME, irp);
            if (documentation != null)
                addXmlProperty(DOCUMENTATION_NAME, documentation);
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

        UnparsedProtocol(String key, String irp) {
            this(key, irp, null);
        }

        private void patch(UnparsedProtocol patchProtocol) {
            patchMap(map, patchProtocol.map);
            patchMap(xmlMap, patchProtocol.xmlMap);
        }

        private void addXmlProperty(String key, DocumentFragment fragment) {
            List<DocumentFragment> list = xmlMap.get(key);
            if (list == null) {
                list = new ArrayList<>(1);
                xmlMap.put(key, list);
            }
            list.add(fragment.hasChildNodes() ? fragment : null);
        }

        private void addProperty(String key, String val) {
            String value = val.trim();
            List<String> list = map.get(key);
            if (list == null) {
                list = new ArrayList<>(1);
                map.put(key, list);
            }
            list.add(value.isEmpty() ? null : value);
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
            if (usable.equalsIgnoreCase(FALSE_NAME))
                return;

            String name = element.getAttribute(NAME_NAME);
            addProperty(NAME_NAME, name);
            String cName = element.getAttribute(CNAME_NAME);
            if (!cName.isEmpty())
                addProperty(CNAME_NAME, cName);
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;
                switch (e.getLocalName()) {
                    case IRP_NAME:
                        addProperty(IRP_NAME, e.getTextContent());
                        break;
                    case DOCUMENTATION_NAME:
                        addXmlProperty(DOCUMENTATION_NAME, nodeListToDocumentFragment(e.getChildNodes()));
                        break;
                    case PARAMETER_NAME:
                        boolean isXml = e.getAttribute(TYPE_NAME).toLowerCase(Locale.US).equals(XML_NAME);
                        if (isXml)
                            addXmlProperty(e.getAttribute(NAME_NAME), nodeListToDocumentFragment(e.getChildNodes()));
                        else
                            addProperty(e.getAttribute(NAME_NAME), e.getTextContent());
                        break;
                    default:
                        throw new ThisCannotHappenException("unknown tag: " + e.getTagName());
                }
            }
        }

        private List<DocumentFragment> getXmlProperties(String key) {
            return getOrEmptyList(xmlMap, key);
        }

        void setXmlProperties(String key, List<DocumentFragment> list) {
            xmlMap.put(key, list);
        }

        void removeXmlProperties(String key) {
            xmlMap.remove(key);
        }

        private List<String> getProperties(String key) {
            return getOrEmptyList(map, key);
        }

        void setProperties(String key, List<String> list) {
            map.put(key, list);
        }

        void removeProperties(String key) {
            map.remove(key);
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

        DocumentFragment getHtmlDocumentation() {
            List<DocumentFragment> list = getXmlProperties(DOCUMENTATION_NAME);
            return list.isEmpty() ? null : list.get(0);
        }

        boolean isUsable() {
            String str = getFirstProperty(USABLE_NAME);
            return str == null || Boolean.parseBoolean(str) || str.equalsIgnoreCase("yes");
        }

        private NamedProtocol toNamedProtocol() throws InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
            return new NamedProtocol(getName(), getCName(), getIrp(), getHtmlDocumentation(),
                    getFirstProperty(FREQUENCY_TOLERANCE_NAME), getFirstProperty(FREQUENCY_LOWER_NAME), getFirstProperty(FREQUENCY_UPPER_NAME),
                    getFirstProperty(ABSOLUTE_TOLERANCE_NAME), getFirstProperty(RELATIVE_TOLERANCE_NAME),
                    getFirstProperty(MINIMUM_LEADOUT_NAME), getFirstProperty(DECODABLE_NAME), getFirstProperty(REJECT_REPEATLESS_NAME), getProperties(PREFER_OVER_NAME), map);
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

            xmlMap.entrySet().forEach((kvp) -> {
                List<DocumentFragment> list = kvp.getValue();
                if (list != null)
                    list.forEach((documentFragment) -> {
                        Element param = doc.createElementNS(IRP_PROTOCOL_NS, IRP_NAMESPACE_PREFIX + ":" + PARAMETER_NAME);
                        element.appendChild(param);
                        param.setAttribute(NAME_NAME, kvp.getKey());
                        param.setAttribute(XmlUtils.XML_SPACE_ATTRIBUTE_NAME, XmlUtils.PRESERVE); // to prevent extra white space from being inserted
                        doc.adoptNode(documentFragment);
                        param.appendChild(documentFragment);
                    });
            });

            return element;
        }

        private boolean isEmpty() {
            // There is always the name
            return map.size() <= 1 && xmlMap.isEmpty();
        }
    }
}
