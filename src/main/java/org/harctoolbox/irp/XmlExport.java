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
     */
    public Element toElement(Document document);
}
