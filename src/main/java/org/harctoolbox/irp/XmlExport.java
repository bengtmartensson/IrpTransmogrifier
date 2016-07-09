package org.harctoolbox.irp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for classes that can produce XML code.
 */
public interface XmlExport {

    /**
     *
     * @param document
     * @return
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    public Element toElement(Document document) throws IrpSyntaxException;
}
