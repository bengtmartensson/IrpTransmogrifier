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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class consists of a collection of useful static constants and functions.
 */
public final class XmlUtils {

    public static final String DEFAULT_CHARSETNAME                  = "UTF-8";
    public static final String W3C_SCHEMA_NAMESPACE_ATTRIBUTE_NAME  = XMLNS_ATTRIBUTE + ":xsi";
    public static final String HTML_NAMESPACE_ATTRIBUTE_NAME        = XMLNS_ATTRIBUTE + ":html";
    public static final String HTML_NAMESPACE                       = "http://www.w3.org/1999/xhtml";
    public static final String XSLT_NAMESPACE_URI                   = "http://www.w3.org/1999/XSL/Transform";
    public static final String SCHEMA_LOCATION_ATTRIBUTE_NAME       = "xsi:schemaLocation";
    public static final String XML_LANG_ATTRIBUTE_NAME              = XML_NS_PREFIX + ":lang";
    public static final String ENGLISH                              = "en";

    private static final Logger logger = Logger.getLogger(XmlUtils.class.getName());

    public static Document parseStringToXmlDocument(String string, boolean isNamespaceAware, boolean isXIncludeAware) throws SAXException {
        try {
            InputStream stream = new ByteArrayInputStream(string.getBytes("UTF-8"));
            return openXmlStream(stream, null, isNamespaceAware, isXIncludeAware);
        } catch (IOException ex) {
            throw new ThisCannotHappenException();
        }
    }

    public static Document openXmlFile(File file, Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) throws SAXException, IOException {
        final String fname = file.getCanonicalPath();
        DocumentBuilder builder = newDocumentBuilder(schema, isNamespaceAware, isXIncludeAware);

        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
            @Override
            public void error(SAXParseException exception) throws SAXParseException {
                throw new SAXParseException("Parse Error in file " + fname + ", line " + exception.getLineNumber() + ": " + exception.getMessage(), "", fname, exception.getLineNumber(), 0);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXParseException {
                throw new SAXParseException("Parse Error in file " + fname + ", line " + exception.getLineNumber() + ": " + exception.getMessage(), "", fname, exception.getLineNumber(), 0);
            }

            @Override
            public void warning(SAXParseException exception) {
                logger.log(Level.WARNING, "Parse Warning: {0}{1}", new Object[]{exception.getMessage(), exception.getLineNumber()});
            }
        });

        return builder.parse(file);
    }

    public static Document openXmlFile(File file, File schemaFile, boolean isNamespaceAware, boolean isXIncludeAware) throws SAXException, IOException {
        Schema schema = readSchemaFromFile(schemaFile);
        return openXmlFile(file, schema, isNamespaceAware, isXIncludeAware);
    }

    public static Document openXmlFile(File file) throws IOException, SAXException {
        return openXmlFile(file, (Schema) null, true, true);
    }

    // NOTE: By silly reader, makes null as InputStream, producing silly error messages.
    public static Document openXmlReader(Reader reader, Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) throws IOException, SAXException {
        return openXmlStream((new InputSource(reader)).getByteStream(), schema, isNamespaceAware, isXIncludeAware);
    }

    public static Document openXmlStream(InputStream stream, Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) throws IOException, SAXException {
        DocumentBuilder builder = newDocumentBuilder(schema, isNamespaceAware, isXIncludeAware);

        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
            @Override
            public void error(SAXParseException exception) throws SAXParseException {
                throw new SAXParseException("Parse Error in instream, line " + exception.getLineNumber() + ": " + exception.getMessage(), "", "instream", exception.getLineNumber(), 0);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXParseException {
                throw new SAXParseException("Parse Error in instream, line " + exception.getLineNumber() + ": " + exception.getMessage(), "", "instream", exception.getLineNumber(), 0);
            }

            @Override
            public void warning(SAXParseException exception) {
                logger.log(Level.WARNING, "Parse Warning: {0}{1}", new Object[]{exception.getMessage(), exception.getLineNumber()});
            }
        });
        return builder.parse(stream);
    }

    private static DocumentBuilder newDocumentBuilder(Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(isNamespaceAware);
        factory.setXIncludeAware(isXIncludeAware);
        if (schema != null) {
            factory.setSchema(schema);
            factory.setValidating(false);
        }
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            // there is something seriously wrong
            throw new ThisCannotHappenException(ex);
        }
        return builder;
    }

    public static Document newDocument(boolean isNamespaceAware) {
        DocumentBuilder builder = newDocumentBuilder(null, isNamespaceAware, false);
        return builder.newDocument();
    }

    public static Document newDocument() {
        return newDocument(false);
    }

    public static Map<String, Element> createIndex(Element root, String tagName, String idName) {
        HashMap<String, Element> index = new HashMap<>(20);
        NodeList nodes = root.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String key = el.getAttribute(idName);
            if (!key.isEmpty())
                index.put(key, el);
        }
        return index;
    }

    public static void printDOM(OutputStream ostr, Document doc, String encoding, String cdataElements) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            if (encoding != null)
                tr.setOutputProperty(OutputKeys.ENCODING, encoding);
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            if (cdataElements != null)
                tr.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataElements);
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(doc), new StreamResult(ostr));
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "{0}", ex.getMessage());
            throw new ThisCannotHappenException(ex);
        }
    }

    public static void printDOM(File file, Document doc, String encoding, String cdataElements) throws FileNotFoundException {
        printDOM(file != null ? new FileOutputStream(file) : System.out, doc, encoding, cdataElements);
        logger.log(Level.INFO, "File {0} written.", file);
    }

    public static void printDOM(String xmlFileName, Document doc, String encoding, String cdataElements) throws FileNotFoundException {
        PrintStream xmlStream = IrCoreUtils.getPrintSteam(xmlFileName);
        printDOM(xmlStream, doc, encoding, cdataElements);
    }

    // Do not define a function printDOM(File, Document, String),
    // since it would been too error prone.

    public static void printDOM(File file, Document doc) throws FileNotFoundException {
        printDOM(file, doc, null, null);
    }

    public static void printDOM(Document doc) {
        printDOM(System.out, doc, null, null);
    }

    private static Schema readSchemaFromFile(File schemaFile) {
        try {
            return (SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)).newSchema(schemaFile);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new ThisCannotHappenException(ex);
        }
    }

    public static Map<String, Element> buildIndex(Element element, String tagName, String idName) {
        HashMap<String, Element> index = new HashMap<>(20);
        NodeList nl = element.getElementsByTagName(tagName);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String id = el.getAttribute(idName);
            if (!id.isEmpty())
                index.put(id, el);
        }
        return index;
    }

    public static void addBooleanAttributeIfTrue(Element element, String attName, boolean value) {
        if (value)
            element.setAttribute(attName, "true");
    }

    public static void addDoubleAttributeAsInteger(Element element, String attName, double value) {
        element.setAttribute(attName, Long.toString(Math.round(value)));
    }

    public static void main(String[] args) {
        try {
            Schema schema = args.length > 1 ? readSchemaFromFile(new File(args[1])) : null;
            Document doc = openXmlFile(new File(args[0]), schema, true, true);
            System.out.println(doc);
        } catch (SAXException | IOException ex) {
            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private XmlUtils() {
    }
}
