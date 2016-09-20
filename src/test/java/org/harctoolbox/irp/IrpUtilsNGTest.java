package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class IrpUtilsNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrpUtilsNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of stringArray method, of class IrpUtils.
     */
    @Test
    public void testStringArray() {
        System.out.println("stringArray");
        int[] array = new int[]{ 42, 73 };
        String expResult = "[42, 73]";
        String result = IrpUtils.stringArray(array);
        assertEquals(result, expResult);
    }

    /**
     * Test of l1Norm method, of class IrpUtils.
     */
    @Test
    public void testL1Norm_DoubleArr() {
        System.out.println("l1Norm");
        Double[] sequence = new Double[] { Double.valueOf(42f), Double.valueOf(73f) };
        double expResult = 115f;
        double result = IrpUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrpUtils.
     */
    @Test
    public void testL1Norm_doubleArr() {
        System.out.println("l1Norm");
        double[] sequence = new double[] { 42f, 73f};
        double expResult = 115f;
        double result = IrpUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrpUtils.
     */
    @Test
    public void testL1Norm_3args() {
        System.out.println("l1Norm");
        double[] sequence = new double[] { 222f, 42f, 73f, 123f};
        int beg = 1;
        int length = 2;
        double expResult = 115f;
        double result = IrpUtils.l1Norm(sequence, beg, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of spaces method, of class IrpUtils.
     */
    @Test
    public void testSpaces() {
        System.out.println("spaces");
        int length = 5;
        String expResult = "     ";
        String result = IrpUtils.spaces(length);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrpUtils.
     */
    @Test
    public void testOnes() {
        System.out.println("ones");
        int n = 5;
        long expResult = 0x1FL;
        long result = IrpUtils.ones(n);
        assertEquals(result, expResult);
        assertEquals(IrpUtils.ones(0), 0L);
        assertEquals(IrpUtils.ones(64), -1L);
        try {
            result = IrpUtils.ones(-1);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            result = IrpUtils.ones(65);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Test of power method, of class IrpUtils.
     */
    @Test
    public void testPower() {
        System.out.println("power");
        assertEquals(IrpUtils.power(2L, 0L), 1L);
        assertEquals(IrpUtils.power(2L, 16L), 65536L);
    }

    /**
     * Test of reverse method, of class IrpUtils.
     */
    @Test
    public void testReverse() {
        System.out.println("reverse");
        assertEquals(IrpUtils.reverse(123, 8), 222);
        assertEquals(IrpUtils.reverse(1, 5), 16);
    }

    /**
     * Test of parseLong method, of class IrpUtils.
     */
    @Test
    public void testParseLong_String() {
        System.out.println("parseLong");
        assertEquals(IrpUtils.parseLong("0x12345"), 0x12345);
        assertEquals(IrpUtils.parseLong("12345"), 12345);
        assertEquals(IrpUtils.parseLong("0b100100"), 0b100100);
        assertEquals(IrpUtils.parseLong("%100100"), 0b100100);
        assertEquals(IrpUtils.parseLong("012345"), 012345);
        assertEquals(IrpUtils.parseLong("0"), 0);
    }
}
