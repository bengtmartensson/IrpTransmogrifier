package org.harctoolbox.irp;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
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
public class ShortProntoNGTest {

    private static final String nec1_1_2_3 = "0000 006C 0022 0002 015B 00AD 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 06FB 015B 0057 0016 0E6C";
    private static final String nec1_1_3   = "0000 006C 0022 0002 015B 00AD 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C";
    private static final String nec1_12_56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C";
    private static final String rc5_1_1_0  = "0000 0073 0000 000C 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0040 0040 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0CA8";
    private static final String rc5_1_1_1  = "0000 0073 0000 000C 0020 0020 0020 0020 0040 0020 0020 0020 0020 0020 0020 0040 0040 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0CA8";
    private static final String rc5x_1_2_3 = "0000 0073 0000 0012 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0080 0020 0020 0020 0020 0020 0020 0020 0040 0040 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0020 0020 0AA8";
    private static final String rc6_1_3    = "0000 0073 0000 0014 0060 0020 0010 0020 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0BDD";
    private static final String denon      = "0000 006D 0000 0020 000A 0046 000A 001E 000A 001E 000A 001E 000A 001E 000A 0046 000A 0046 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 0677 000A 0046 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 001E 000A 0046 000A 0046 000A 0046 000A 0046 000A 0046 000A 0046 000A 0046 000A 0046 000A 0677";

    private static final String nec1_1_2_3_ccf = "900A 006C 0000 0001 0102 03FC";
    private static final String nec1_1_3_ccf   = "900A 006C 0000 0001 01FE 03FC";
    private static final String nec1_12_56_ccf = "900A 006C 0000 0001 0CF3 38C7";
    private static final String rc5_1_1_ccf    = "5000 0073 0000 0001 0001 0001";
    private static final String rc5x_1_2_3_ccf = "5001 0073 0000 0002 0001 0002 0003 0000";
    private static final String rc6_1_3_ccf    = "6000 0073 0000 0001 0001 0003";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ShortProntoNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of long2short method, of class ShortPronto.
     * @throws java.lang.Exception
     */
    @Test
    public void testLong2short() throws Exception {
        System.out.println("long2short");
        assertEquals(ShortPronto.long2short(nec1_1_2_3), nec1_1_2_3_ccf);
        assertEquals(ShortPronto.long2short(nec1_1_3), nec1_1_3_ccf);
        assertEquals(ShortPronto.long2short(nec1_12_56), nec1_12_56_ccf);
        assertEquals(ShortPronto.long2short(rc5_1_1_0), rc5_1_1_ccf);
        assertEquals(ShortPronto.long2short(rc5_1_1_1), rc5_1_1_ccf);
        assertEquals(ShortPronto.long2short(rc5x_1_2_3), rc5x_1_2_3_ccf);
        assertEquals(ShortPronto.long2short(rc6_1_3), rc6_1_3_ccf);
        assertEquals(ShortPronto.long2short(denon), denon);
    }

    /**
     * Test of parse method, of class ShortPronto.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    @Test
    public void testParse_String() throws InvalidArgumentException, Pronto.NonProntoFormatException {
        System.out.println("parse");
        IrSignal result = ShortPronto.parse(nec1_12_56_ccf);
        IrSignal expected = Pronto.parse(nec1_12_56);
        assertTrue(result.approximatelyEquals(expected));

        result = ShortPronto.parse(rc5_1_1_ccf);
        expected = Pronto.parse(rc5_1_1_0);
        assertTrue(result.approximatelyEquals(expected));

        result = ShortPronto.parse(rc5x_1_2_3_ccf);
        expected = Pronto.parse(rc5x_1_2_3);
        assertTrue(result.approximatelyEquals(expected));

        result = ShortPronto.parse(rc6_1_3_ccf);
        expected = Pronto.parse(rc6_1_3);
        assertTrue(result.approximatelyEquals(expected));
    }
}
