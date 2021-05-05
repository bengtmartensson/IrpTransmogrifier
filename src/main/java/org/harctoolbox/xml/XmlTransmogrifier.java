/*
Copyright (C) 2020 Bengt Martensson.

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

package org.harctoolbox.xml;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class allows command line access to reading, writing, validating, and xslt transformation of XML files.
 */
public final class XmlTransmogrifier {
    private static JCommander argumentParser;

    private static void usage(int exitcode) {
        argumentParser.usage();
        System.exit(exitcode);
    }

    // Not really tested
    private static NodeList evaluateXpath(Node node, String xpath, MyResolver resolver) throws XPathExpressionException {
        XPath xpather = XPathFactory.newInstance().newXPath();
        if (resolver != null)
            xpather.setNamespaceContext(resolver);
        XPathExpression xpathExpression = xpather.compile(xpath);
        NodeList nodeList = (NodeList) xpathExpression.evaluate(node, XPathConstants.NODESET);
        return nodeList;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        CommandLineArguments commandLineArgs = new CommandLineArguments();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);
        argumentParser.setAllowAbbreviatedOptions(true);


        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.EXIT_USAGE_ERROR);
        }

        if (commandLineArgs.helpRequested)
            usage(IrpUtils.EXIT_SUCCESS);

        PrintStream out = null;
        try {
            out = IrCoreUtils.getPrintStream(commandLineArgs.output, commandLineArgs.encoding);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.EXIT_USAGE_ERROR);
        }

        try {
            Document doc = XmlUtils.openXmlFile(commandLineArgs.argument, commandLineArgs.schema, commandLineArgs.nameSpaceAware, true);

            if (commandLineArgs.xpath != null) {
                MyResolver resolver = commandLineArgs.nameSpaceAware ? new MyResolver(doc) : null;
                NodeList nodeList = evaluateXpath(doc, commandLineArgs.xpath, resolver);
                Element root = doc.createElement("xpath-result");
                doc.replaceChild(root, doc.getDocumentElement());
                int size = nodeList.getLength();
                root.setAttribute("xpath", commandLineArgs.xpath);
                root.setAttribute("size", Integer.toString(size));

                for (int i = 0; i < size; i++) {
                    Node node = nodeList.item(i);
                    root.appendChild(node);
                }
            }

            if (!commandLineArgs.print) {
                System.out.println("Use the --print option if you want output.");
                System.exit(IrpUtils.EXIT_SUCCESS);
            }

            if (commandLineArgs.stylesheet == null) {
                XmlUtils.printDOM(out, doc, commandLineArgs.encoding, null);
            } else {
                Document stylesheet = XmlUtils.openXmlFile(commandLineArgs.stylesheet, null, true, true);
                XmlUtils.printDOM(out, doc, commandLineArgs.encoding, stylesheet, new HashMap<>(0), false);
            }
        } catch (SAXException | IOException | TransformerException | XPathExpressionException ex) {
            ex.printStackTrace();
            System.exit(IrpUtils.EXIT_FATAL_PROGRAM_FAILURE);
        }
        System.exit(IrpUtils.EXIT_SUCCESS);
    }

    private XmlTransmogrifier() {
    }

    public static class CommandLineArguments {
        @Parameter(names = {"-e", "--encoding"}, description = "Output encoding")
        private String encoding = XmlUtils.DEFAULT_CHARSETNAME;

        @Parameter(names = {"-h", "-?", "--help"}, description = "Print help text")
        private boolean helpRequested = false;

        @Parameter(names = {"-n", "--namespaceaware"}, description = "Use name space aware parser")
        private boolean nameSpaceAware = false;

        @Parameter(names = {"-o", "--output"}, description = "Output file; \"-\" for stdout")
        private String output = "-";

        @Parameter(names = {"-p", "--print"}, description = "Print result on the --output argument")
        private boolean print = false;

        @Parameter(names = {"-s", "--schema"}, description = "URL/Filename of schema for validation")
        private String schema = null;

        @Parameter(names = {"--xpath"}, description = "XPath to apply to the document, before stylesheet processing (untested).")
        private String xpath = null;

        @Parameter(names = {"--xslt"}, description = "URL/filename of stylesheet")
        private String stylesheet = null;

        @Parameter(required = true, description = "URL/Filename or - for stdin")
        private String argument = "-";
    }

    // Not really tested
    private static class MyResolver implements NamespaceContext {

        Map<String, String> prefixNamespace;
        Map<String, String> namespacePrefix;

        MyResolver() {
            prefixNamespace = new HashMap<>(0);
            namespacePrefix = new HashMap<>(0);
        }

        MyResolver(Document document) {
            NamedNodeMap attrs = document.getDocumentElement().getAttributes();
            prefixNamespace = new HashMap<>(attrs.getLength());
            namespacePrefix = new HashMap<>(attrs.getLength());

            // If there is a default, non-null namespace, assign the prefix "default" to it.
            String defaultNsAttribute = document.getDocumentElement().getAttribute(XMLNS_ATTRIBUTE);
            if (! defaultNsAttribute.isEmpty()) {
                prefixNamespace.put("default", defaultNsAttribute);
                namespacePrefix.put(defaultNsAttribute, "default");
            }

            for (int i = 0; i < attrs.getLength(); i++) {
                Node att = attrs.item(i);
                String namespaceURI = att.getNamespaceURI();
                if (namespaceURI != null && namespaceURI.equals(XMLNS_ATTRIBUTE_NS_URI)) {
                    prefixNamespace.put(att.getLocalName(), att.getNodeValue());
                    namespacePrefix.put(att.getNodeValue(), att.getLocalName());
                }
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return prefixNamespace.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return namespacePrefix.get(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return prefixNamespace.keySet().iterator();
        }
    }
}
