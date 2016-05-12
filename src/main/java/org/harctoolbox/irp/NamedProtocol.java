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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class NamedProtocol extends Protocol {
    private String name;
    private String documentation;

    public NamedProtocol(String name, String irp, String documentation) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException {
        super(irp);
        this.name = name;
        this.documentation = documentation;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    public Document toDocument() throws IrpSyntaxException {
        Document document = XmlUtils.newDocument();
        document.appendChild(toElement(document));
        return document;
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element root = super.toElement(document);
        root.setAttribute("name", name);
        Element docu = document.createElement("documentation");
        docu.appendChild(document.createCDATASection("\n" + documentation + "\n"));
        root.appendChild(docu);
        return root;
    }

    /**
     * Testing only.
     *
     * @param args
     */
    public static void main(String[] args) {
        String irpString
                = //"{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
                "{38.4k,22p,33%,msb}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
        try {
            NamedProtocol protocol = new NamedProtocol("name", irpString, "dox");
            System.out.println(protocol);
        } catch (IrpSyntaxException | IrpSemanticException ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArithmeticException | IncompatibleArgumentException ex) {
            Logger.getLogger(NamedProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}