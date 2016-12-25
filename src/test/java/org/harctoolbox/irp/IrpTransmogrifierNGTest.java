package org.harctoolbox.irp;

import java.io.UnsupportedEncodingException;
import org.harctoolbox.ircore.IrCoreUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrpTransmogrifierNGTest {

    private static final String charSet = "US-ASCII";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrpTransmogrifierNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     * @throws java.io.UnsupportedEncodingException
     */
    @Test(enabled = true)
    public void testAnalyze() throws UnsupportedEncodingException {
        System.out.println("analyze");
        String args = "-a 20 analyze -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        String result = IrpTransmogrifier.runMain(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:32,6,-89m){A=0xc38c922}\tweight = 15" + IrCoreUtils.lineSeparator);
    }
}
