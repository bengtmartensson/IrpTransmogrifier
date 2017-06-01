package org.harctoolbox.irp;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author bengt
 */
public class IrpDatabaseNGTest {

    private static final String CONFIGFILE = "src/main/resources/IrpProtocols.xml";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrpDatabaseNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of checkSorted method, of class IrpDatabase.
     * This guarantees that the data base will be, and stay, sorted.
     */
    @Test
    public void testCheckSorted() {
        try {
            System.out.println("checkSorted");
            IrpDatabase instance = new IrpDatabase(CONFIGFILE);
            boolean expResult = false;
            boolean result = instance.checkSorted();
            assertTrue(result);
        } catch (IOException | SAXException ex) {
            fail();
        }
    }
}
