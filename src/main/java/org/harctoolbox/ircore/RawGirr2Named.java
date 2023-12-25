/*
Copyright (C) 2023 Bengt Martensson.

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

package org.harctoolbox.ircore;

import static org.harctoolbox.xml.XmlUtils.parseStringToXmlDocument;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class RawGirr2Named {
    private final static String XSLT_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
+ "<!--"
+ "This transformation takes a Girr document and produces a text stream that"
+ "IrpTransmogrifier can read with the - -namedinput option."
+ "-->"
+ ""
+ "<xsl:stylesheet xmlns:girr=\"http://www.harctoolbox.org/Girr\""
+ "                xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:java=\"http://xml.apache.org/xalan/java\""
+ "                version=\"1.0\">"
+ "    <xsl:output method=\"text\" />"
+ ""
+ "    <xsl:template match=\"/\">"
+ "        <xsl:apply-templates select=\"//girr:command\"/>"
+ "    </xsl:template>"
+ ""
+ "    <xsl:template match=\"girr:command\"/>"
+ ""
+ "    <xsl:template match=\"girr:command[girr:raw]\">"
+ "        <xsl:value-of select=\"@name\"/>"
+ "        <xsl:text>"
+ "</xsl:text>"
+ "        <xsl:apply-templates select=\"girr:raw\"/>"
+ "    </xsl:template>"
+ ""
+ "    <xsl:template match=\"girr:raw\">"
+ "        <xsl:text>Freq=</xsl:text>"
+ "        <xsl:value-of select=\"@frequency\"/>"
+ "        <xsl:if test=\"not(girr:intro)\">"
+ "            <xsl:text>[]</xsl:text>"
+ "        </xsl:if>"
+ "        <xsl:apply-templates select=\"*\"/>"
+ "        <xsl:text>"
+ ""
+ "</xsl:text>"
+ "    </xsl:template>"
+ ""
+ "    <xsl:template match=\"girr:intro|girr:repeat|girr:ending\">"
+ "        <xsl>[</xsl>"
+ "        <xsl:value-of select=\"text()\"/>"
+ "        <xsl:apply-templates select=\"*\"/>  <!-- For fat form -->"
+ "        <xsl:text>]</xsl:text>"
+ "    </xsl:template>"
+ ""
+ "    <!-- For fat form -->"
+ "    <xsl:template match=\"girr:flash|girr:gap\">"
+ "        <xsl:value-of select=\".\"/>"
+ "        <xsl:text> </xsl:text>"
+ "    </xsl:template>"
+ ""
+ "</xsl:stylesheet>";
    
    public static final Document xslt = safeParse();
    
    private static Document safeParse() {
        try {
            return parseStringToXmlDocument(XSLT_STRING, true, false);
        } catch (SAXException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }
}
