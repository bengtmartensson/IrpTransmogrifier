package org.harctoolbox.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtilsNGTest {

    public XmlUtilsNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of openXmlFile method, of class XmlUtils.
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testOpenXmlFile_File() {
        System.out.println("openXmlFile");
        File file = new File("[.girr");
        Document expResult = null;
        Document result;
        try {
            result = XmlUtils.openXmlFile(file);
        } catch (FileNotFoundException ex) {
            // OK, file is really not existing
        } catch (IOException | SAXException ex) {
            fail();
        }
    }
}
