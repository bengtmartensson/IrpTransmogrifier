package org.harctoolbox.irp;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NumberNGTest {
    private final static long deadbeef = 0xdeadbeefL;
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Number instance;

    public NumberNGTest() {
        instance = new Number("0xdeadbeef");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class Number.
     */
    @Test
    public void testParse_String() {
        System.out.println("parse");
        try {
            assertEquals(Number.parse("UINT8_MAX"), 255L);
            assertEquals(Number.parse("UINT16_MAX"), 65535L);
            assertEquals(Number.parse("UINT24_MAX"), 16777215L);
            assertEquals(Number.parse("UINT32_MAX"), 4294967295L);
            assertEquals(Number.parse("UINT64_MAX"), -1L);
            assertEquals(Number.parse("073"), 59);
            assertEquals(Number.parse("0"), 0L);
            assertEquals(Number.parse("123456789"), 123456789);
            assertEquals(Number.parse("0xdeadbeef"), 0xdeadbeefL);
            assertEquals(Number.parse("0xdeadBeef"), 0xdeadbeefL);
        } catch (ParseCancellationException ex) {
            fail();
        }
    }

    /**
     * Test of toNumber method, of class Number.
     */
    @Test
    public void testToNumber_0args() {
        System.out.println("toNumber");
        assertEquals(instance.toNumber(), 0xdeadbeefL);
    }

    /**
     * Test of toString method, of class Number.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString();
        assertEquals(result, Long.toString(deadbeef));
    }

    /**
     * Test of toIrpString method, of class Number.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "3735928559");
    }
}
