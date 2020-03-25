package org.harctoolbox.ircore;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IrSequenceNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final String raw = " +1266 -426 +1266 -426 +422 -1270 +1266 -426 +1266 -426 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +1266 -7096 +1266 -426 +1266 -426 +422 -1270 +1266 -426 +1266 -426 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270  +422 -1270 +1266 -7096   ";

    //private final IrSignal nec1_123456;
    public IrSequenceNGTest() throws InvalidArgumentException {
        //nec1_123456 = new IrSignal("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of get method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGet() {
        System.out.println("get");
        int i = 0;
        IrSequence instance = new IrSequence();
        double expResult = 0.0;
        double result = instance.get(i);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of approximatelyEquals method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testApproximatelyEquals_6args() {
        System.out.println("approximatelyEquals");
        int beginning = 0;
        int compareStart = 0;
        int length = 0;
        double absoluteTolerance = 0.0;
        double relativeTolerance = 0.0;
        double lastLimit = 0.0;
        IrSequence instance = new IrSequence();
        boolean expResult = false;
        boolean result = instance.approximatelyEquals(beginning, compareStart, length, absoluteTolerance, relativeTolerance, lastLimit);
        assertEquals(result, expResult);
    }

    /**
     * Test of approximatelyEquals method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testApproximatelyEquals_5args() {
        System.out.println("approximatelyEquals");
        int beginning = 0;
        int compareStart = 0;
        int length = 0;
        double absoluteTolerance = 0.0;
        double relativeTolerance = 0.0;
        IrSequence instance = new IrSequence();
        boolean expResult = false;
        boolean result = instance.approximatelyEquals(beginning, compareStart, length, absoluteTolerance, relativeTolerance);
        assertEquals(result, expResult);
    }

    /**
     * Test of getGap method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetGap() {
        System.out.println("getGap");
        IrSequence instance = new IrSequence();
        double expResult = 0.0;
        double result = instance.getLastGap();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of isEmpty method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testIsEmpty() {
        System.out.println("isEmpty");
        IrSequence instance = new IrSequence();
        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(result, expResult);
    }

    /**
     * Test of containsZeros method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testContainsZeros() {
        System.out.println("containsZeros");
        IrSequence instance = new IrSequence();
        boolean expResult = false;
        boolean result = instance.containsZeros();
        assertEquals(result, expResult);
    }

    /**
     * Test of replaceZeros method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testReplaceZeros() {
        System.out.println("replaceZeros");
        double replacement = 0.0;
        IrSequence instance = new IrSequence();
        boolean expResult = false;
        boolean result = instance.replaceZeros(replacement);
        assertEquals(result, expResult);
    }

    /**
     * Test of getDuration method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetDuration_0args() {
        System.out.println("getDuration");
        IrSequence instance = new IrSequence();
        double expResult = 0.0;
        double result = instance.getTotalDuration();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getDuration method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetDuration_int_int() {
        System.out.println("getDuration");
        int begin = 0;
        int length = 0;
        IrSequence instance = new IrSequence();
        double expResult = 0.0;
        double result = instance.getTotalDuration(begin, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of toPrintString method, of class IrSequence.
     */
    @Test(enabled = true)
    public void testToPrintString_3args() {
        System.out.println("toString");
        IrSequence instance = new IrSequence();
        String expResult = "[]";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class IrSequence.
     */
    @Test(enabled = true)
    public void testToString_0args() {
        System.out.println("toString");
        IrSequence instance = new IrSequence();
        String expResult = "[]";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class IrSequence.
     */
    @Test(enabled = true)
    public void testToString_boolean() {
        try {
            System.out.println("toString");
            IrSequence instance = new IrSequence(raw);
            String result = instance.toString(true);
            assertEquals(result, "[+1266,-426,+1266,-426,+422,-1270,+1266,-426,+1266,-426,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+1266,-7096,+1266,-426,+1266,-426,+422,-1270,+1266,-426,+1266,-426,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+422,-1270,+1266,-7096]");
            result = instance.toString(false);
            assertEquals(result, "[1266,426,1266,426,422,1270,1266,426,1266,426,422,1270,422,1270,422,1270,422,1270,422,1270,422,1270,1266,7096,1266,426,1266,426,422,1270,1266,426,1266,426,422,1270,422,1270,422,1270,422,1270,422,1270,422,1270,1266,7096]");
        } catch (OddSequenceLengthException ex) {
            Logger.getLogger(IrSequenceNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of stripDecoratedString method, of class IrSequence.
     */
    @Test
    public void testStripDecoratedString() {
        System.out.println("stripDecoratedString");
        String in = "+8900, -4450 + 600, -1600 + 600, - 600 + 600, - 600\n"
                + "+ 600, - 600 + 600, - 550 + 650, - 600 + 600, -1550";
        String expResult = "8900 4450 600 1600 600 600 600 600 600 600 600 550 650 600 600 1550";
        String result = IrSequence.stripDecoratedString(in);
        assertEquals(result, expResult);
    }

    /**
     * Test of normalize method, of class IrSequence.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testNormalize() throws OddSequenceLengthException {
        System.out.println("normalize");
        String in = "+8900, -4450 + 600, -1600 + 600, - 600 + 600, - 600\n"
                + "+ 600, - 600 + 600, - 550 + 650, - 600 + 600";
        String result = IrSequence.normalize(in, 1234.0, true, " ");
        assertEquals(result, "+8900 -4450 +600 -1600 +600 -600 +600 -600 +600 -600 +600 -550 +650 -600 +600 -1234");
        result = IrSequence.normalize(in, 1234.0, false, ", ");
        assertEquals(result, "8900, 4450, 600, 1600, 600, 600, 600, 600, 600, 600, 600, 550, 650, 600, 600, 1234");
    }
}
