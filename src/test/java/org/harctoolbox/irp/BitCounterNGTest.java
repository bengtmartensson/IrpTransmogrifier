package org.harctoolbox.irp;

import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BitCounterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public BitCounterNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        BitCounter bitCounter = new BitCounter();
        assertEquals(bitCounter.toString(), "");
        bitCounter.aggregate(0L, 2);
        assertEquals(bitCounter.toString(), "00");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "1000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "1000");
        bitCounter.aggregate(2L, 2);
        assertEquals(bitCounter.toString(), "10*0");
    }

    @Test
    public void testSomeMethodA() {
        BitCounter bitCounter = new BitCounter(true);
        assertEquals(bitCounter.toString(), "");
        bitCounter.aggregate(0L, 2);
        assertEquals(bitCounter.toString(), "00");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "*000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "*000");
        bitCounter.aggregate(2L, 2);
        assertEquals(bitCounter.toString(), "*0*0");
    }

    @Test
    public void testSomeMethod1() {
        BitCounter bitCounter = new BitCounter(10);
        assertEquals(bitCounter.toString(), "          ");
        bitCounter.aggregate(0L, 2);
        assertEquals(bitCounter.toString(), "        00");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "      1000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "      1000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "      *0*0");
    }

    @Test
    public void testSomeMethod1A() {
        BitCounter bitCounter = new BitCounter(10, true);
        assertEquals(bitCounter.toString(), "0000000000");
        bitCounter.aggregate(0L, 2);
        assertEquals(bitCounter.toString(), "0000000000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "000000*000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "000000*000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "000000*0*0");
    }

    @Test
    public void testSomeMethod2() {
        BitCounter bitCounter = new BitCounter(10);
        assertEquals(bitCounter.toString(), "          ");
        bitCounter.aggregate(16L, 2);
        assertEquals(bitCounter.toString(), "     10000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "     1*000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "     1*000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "     1*0*0");
        bitCounter.aggregate(0L);
        assertEquals(bitCounter.toString(), "00000**0*0");
    }

    @Test
    public void testSomeMethod2A() {
        BitCounter bitCounter = new BitCounter(10, true);
        assertEquals(bitCounter.toString(), "0000000000");
        bitCounter.aggregate(16L, 2);
        assertEquals(bitCounter.toString(), "00000*0000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "00000**000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "00000**000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "00000**0*0");
        bitCounter.aggregate(0L);
        assertEquals(bitCounter.toString(), "00000**0*0");
    }

    @Test
    public void testSomeMethod2B() {
        BitCounter bitCounter = new BitCounter(true);
        assertEquals(bitCounter.toString(), "");
        bitCounter.aggregate(16L, 2);
        assertEquals(bitCounter.toString(), "*0000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "**000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "**000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "**0*0");
        bitCounter.aggregate(0L);
        assertEquals(bitCounter.toString(), "**0*0");
    }

    /**
     * Test of toIntSequence method, of class BitCounter.
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testToIntSequence() {
        System.out.println("toIntSequence");
        BitCounter bitCounter = new BitCounter(true);
        assertEquals(bitCounter.toString(), "");
        bitCounter.aggregate(16L, 2);
        assertEquals(bitCounter.toString(), "*0000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "**000");
        bitCounter.aggregate(8L, 4);
        assertEquals(bitCounter.toString(), "**000");
        bitCounter.aggregate(2L, 4);
        assertEquals(bitCounter.toString(), "**0*0");
        bitCounter.aggregate(0L);
        assertEquals(bitCounter.toString(), "**0*0");
        List result = bitCounter.toIntSequence();
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), 2);
        assertEquals(result.get(1), 1);
        assertEquals(result.get(2), 1);
        assertEquals(result.get(3), 1);
    }
}
