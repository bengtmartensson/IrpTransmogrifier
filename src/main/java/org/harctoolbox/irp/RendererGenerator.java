/*
Copyright (C) 2015 Bengt Martensson.

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

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;

/**
 * This class generates code for a particular protocol.
 */
public class RendererGenerator {

    // from irpmaster.XmlExport
    public static void printDom(Document doc, OutputStream ostr, Document stylesheet, String doctypeSystemid) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer tr;
            if (stylesheet == null) {
                tr = factory.newTransformer();

                tr.setOutputProperty(OutputKeys.METHOD, "xml");

            } else {
                tr = factory.newTransformer(new DOMSource(stylesheet));
            }
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            if (doctypeSystemid != null)
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystemid);
            tr.transform(new DOMSource(doc), new StreamResult(ostr));
        } catch (TransformerException e) {
            Logger.getLogger(RendererGenerator.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void main(String[] args) {
        String necIrp = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
        String necDocu = "A few devices use NEC1 protocol at 40Khz, rather than the typical frequency.  When getting a decode of NEC1, if you notice that the frequency is closer to 40Khz than to 38Khz, examine multiple learns from the same device to estimate whether the 40Khz frequency is a learning error or a true characteristic of the device. If the 40Khz is correct, there are methods in JP1, or MakeHex (whichever you are using) to reproduce NEC1 at 40Khz rather than the usual frequency.";
        try {
            NamedProtocol protocol = new NamedProtocol("nec1", necIrp, necDocu);
            //System.out.println(protocol.toStringTree());
            Document doc = protocol.toDocument();
            printDom(doc, System.out, null, null);
        } catch (IrpSyntaxException | IrpSemanticException | ArithmeticException | IncompatibleArgumentException ex) {
            Logger.getLogger(RendererGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
