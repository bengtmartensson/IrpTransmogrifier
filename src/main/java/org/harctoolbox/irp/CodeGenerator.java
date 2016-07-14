/*
Copyright (C) 2016 Bengt Martensson.

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
import java.io.OutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 */
public class CodeGenerator {

    /**
     * Name space for XLST (1.0 and 2.0)
     */
    static final String xsltNamespace = "http://www.w3.org/1999/XSL/Transform";
    private Document document;

    public CodeGenerator(Document document) {
        this.document = document;
    }

    public void printDOM(OutputStream ostr, Document stylesheet, /*HashMap<String, String>parameters,
            boolean binary,*/ String charsetName) throws IOException {
//        if (debug) {
//            XmlUtils.printDOM(new File("girr.girr"), this.document);
//            XmlUtils.printDOM(new File("stylesheet.xsl"), stylesheet);
//        }
        try {
            TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            Transformer tr;
            if (stylesheet == null) {
                tr = factory.newTransformer();

                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, charsetName);

            } else {
//                if (parameters != null)
//                    for (Map.Entry<String, String> kvp : parameters.entrySet()) {
//                        Element e = stylesheet.createElementNS(xsltNamespace, "param");
//                        e.setAttribute("name", kvp.getKey());
//                        e.setAttribute("select", kvp.getValue());
//                        stylesheet.getDocumentElement().insertBefore(e, stylesheet.getDocumentElement().getFirstChild());
//                    }
                NodeList nodeList = stylesheet.getDocumentElement().getElementsByTagNameNS(xsltNamespace, "output");
                if (nodeList.getLength() > 0) {
                    Element e = (Element) nodeList.item(0);
                    e.setAttribute("encoding", charsetName);
                }
//                if (debug)
//                    XmlUtils.printDOM(new File("stylesheet-params.xsl"), stylesheet);
                tr = factory.newTransformer(new DOMSource(stylesheet));
            }
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//            if (binary) {
//                DOMResult domResult = new DOMResult();
//                tr.transform(new DOMSource(document), domResult);
//                Document newDoc = (Document) domResult.getNode();
////                if (debug)
////                    XmlUtils.printDOM(new File("girr-binary.xml"), newDoc);
//                NodeList byteElements = newDoc.getDocumentElement().getElementsByTagName("byte");
//                for (int i = 0; i < byteElements.getLength(); i++) {
//                    int val = Integer.parseInt(((Element) byteElements.item(i)).getTextContent());
//                    ostr.write(val);
//                }
//            } else
                tr.transform(new DOMSource(document), new StreamResult(ostr));
//            if (parameters != null && stylesheet != null) {
//                NodeList nl = stylesheet.getDocumentElement().getChildNodes();
//                for (int i = 0; i < nl.getLength(); i++) {
//                    Node n = nl.item(i);
//                    if (n.getNodeType() != Node.ELEMENT_NODE)
//                        continue;
//                    Element e = (Element) n;
//                    if (e.getLocalName().equals("param") && parameters.containsKey(e.getAttribute("name")))
//                        stylesheet.getDocumentElement().removeChild(n);
//                }
//            }
        } catch (TransformerConfigurationException e) {
            System.err.println(e.getMessage());
        } catch (TransformerException e) {
            System.err.println(e.getMessage());
        }
    }

}
