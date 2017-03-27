package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrCoreUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrpTransmogrifierNGTest {

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
     */
    @Test(enabled = true)
    public void testAnalyze() {
        System.out.println("analyze");
        String args = "-a 20 -r 0.04 analyze --radix 16 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        String result = IrpTransmogrifier.runMain(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:32,6,-89m)*{A=0xc38c922}\tweight = 16" + IrCoreUtils.LINESEPARATOR);

        args = "analyze --radix 16 --chop 30000 --ire -- +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +573 -96193";
        result = IrpTransmogrifier.runMain(args);
        assertEquals(result, "{573,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m,(16,-4,1,-96m)*){A=0x30441ce3}\tweight = 16" + IrCoreUtils.LINESEPARATOR);
    }

    @Test(enabled = true)
    public void testFrequencyParser() {
        System.out.println("FrequencyParser");
        IrpTransmogrifier.FrequencyParser frequencyParser = new IrpTransmogrifier.FrequencyParser();
        assertEquals(frequencyParser.convert("38123"), 38123.0);
        assertEquals(frequencyParser.convert("38.1k"), 38100.0);
    }
}
