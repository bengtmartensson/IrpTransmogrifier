package org.harctoolbox.irp;

import java.math.BigInteger;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NumberNGTest {

    private final static long DEADBEEF = 0xdeadbeefL;

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
            assertEquals(Number.parse("073"), 59L);
            assertEquals(Number.parse("0"), 0L);
            assertEquals(Number.parse("123456789"), 123456789L);
            assertEquals(Number.parse("0xdeadbeef"), 0xdeadbeefL);
            assertEquals(Number.parse("0xdeadBeef"), 0xdeadbeefL);
        } catch (ParseCancellationException ex) {
            fail();
        }
    }

    /**
     * Test of toLong method, of class Number.
     */
    @Test
    public void testToNumber_0args() {
        System.out.println("toLong");
        assertEquals(instance.toLong(), 0xdeadbeefL);
    }

    /**
     * Test of toString method, of class Number.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString();
        assertEquals(result, Long.toString(DEADBEEF));
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

    /**
     * Test of newExpression method, of class Number.
     */
    @Test
    public void testNewExpression_String() {
        System.out.println("newExpression");
        NumberExpression expResult = new NumberExpression(0xdeadbeefL);
        NumberExpression result = Number.newExpression("0xdeadbeef");
        assertEquals(result, expResult);
        expResult = new NumberExpression(0xeadbeef);
        result = Number.newExpression("0xeadbeef");
        assertEquals(result, expResult);
    }

    /**
     * Test of parse method, of class Number.
     */
    @Test
    public void testParse_String_int() {
        System.out.println("parse");
        java.lang.Number expResult = 4242;
        java.lang.Number result = Number.parse("4242", 10);
        assertEquals(result, expResult.longValue());
        expResult = 4242L;
        result = Number.parse("4242", 10);
        assertEquals(result, expResult);
    }

    /**
     * Test of longValueExact method, of class Number.
     */
    @Test
    public void testLongValueExact() {
        System.out.println("longValueExact");
        Number instance = new Number(0xdeadbeefL);
        long expResult = 0xdeadbeefL;
        long result = instance.longValueExact();
        assertEquals(result, expResult);

        BigInteger bigInt = new BigInteger("1234567890123456789");
        instance = new Number(bigInt);
        result = instance.longValueExact();
        expResult = 1234567890123456789L;
        assertEquals(result, expResult);

        bigInt = new BigInteger("123456789012345678901234567890123456789012345678901234567890");
        instance = new Number(bigInt);
        try {
            instance.longValueExact();
            fail();
        } catch (ArithmeticException ex) {
        }
    }

    /**
     * Test of equals method, of class Number.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = new Number(42);
        Number instance = new Number(42);
        boolean result = instance.equals(obj);
        assertTrue(result);

        instance = new Number(42L);
        result = instance.equals(obj);
        assertTrue(result);
        result = obj.equals(instance);
        assertTrue(result);

        BigInteger bigInt = new BigInteger("1234567890123456789");
        instance = new Number(bigInt);
        obj = new Number(1234567890123456789L);
        result = obj.equals(instance);
        assertTrue(result);

        bigInt = new BigInteger("123456789012345678901234567890");
        instance = new Number(bigInt);
        result = obj.equals(instance);
        assertFalse(result);
        obj = new Number("123456789012345678901234567890");
        result = obj.equals(instance);
        assertTrue(result);
    }

    /**
     * Test of weight method, of class Number.
     */
    @Test
    public void testWeight() {
        System.out.println("weight");
        Number instance = new Number(42);
        int expResult = 1;
        int result = instance.weight();
        assertEquals(result, expResult);
    }
}
