package org.harctoolbox.ircore;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class ProntoParserNGTest {
   private static final String nec1_12_34_56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String nec1_12_34_56_err = "0000 006C 0022 0003 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String nec1_12_34_56_semantic = "0000 006C 0022 0002 15B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String nec_raw = "+9024 -4512 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -44268";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ProntoParserNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrSignalAsPronto method, of class ProntoRawParser.
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignalAsPronto() throws Pronto.NonProntoFormatException, InvalidArgumentException {
        System.out.println("toIrSignalAsPronto");
        MultiParser instance = ProntoParser.newProntoRawParser(nec1_12_34_56);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);
    }

    /**
     * Test of toIrSignal method, of class ProntoRawParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testToIrSignal() throws InvalidArgumentException {
        System.out.println("toIrSignal");
        MultiParser instance = ProntoParser.newProntoRawParser(nec1_12_34_56);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);

        double fallbackFrequency = 12345.0;
        result = instance.toIrSignal(fallbackFrequency);
        assertNotNull(result);

        instance = ProntoParser.newProntoRawParser(nec1_12_34_56_err);
        try {
            result = instance.toIrSignal(fallbackFrequency);
            fail();
        } catch (InvalidArgumentException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        instance = ProntoParser.newProntoRawParser(nec_raw);
        result = instance.toIrSignal(fallbackFrequency);
        assertNotNull(result);

        instance = ProntoParser.newProntoRawParser(nec1_12_34_56_semantic);
        try {
            result = instance.toIrSignal(fallbackFrequency);
            fail();
        } catch (NumberFormatException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }
}
