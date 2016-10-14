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

import java.util.Objects;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class NamedProtocol extends Protocol {

    private final String irp;
    private final String name;
    private final String documentation;

    public NamedProtocol(String name, String irp, String documentation) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException {
        super(irp);
        this.irp = irp;
        this.name = name;
        this.documentation = documentation;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.irp);
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.documentation);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedProtocol))
            return false;

        NamedProtocol other = (NamedProtocol) obj;
        return super.equals(obj)
                && irp.equals(other.irp)
                && name.equals(other.name)
                && documentation.equals(other.documentation);
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

    public String getIrp() {
        return irp;
    }

    public Document toDocument() {
        return toDocument(false);
    }

    public Document toDocument(boolean split) {
        Document document = XmlUtils.newDocument();
        document.appendChild(toElement(document, split));
        return document;
    }

    @Override
    public Element toElement(Document document) {
        return toElement(document, false);
    }

    @Override
    public Element toElement(Document document, boolean split) {
        Element root = super.toElement(document, split);
        root.setAttribute("name", name);

        Element docu = document.createElement("Documentation");
        docu.appendChild(document.createTextNode(documentation));
        root.appendChild(docu);

        Element irpElement = document.createElement("Irp");
        irpElement.appendChild(document.createTextNode(irp));
        root.appendChild(irpElement);

        return root;
    }
}
