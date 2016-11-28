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
public class IrSequenceNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

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

    @Test(enabled = false)
    public void xxxx(String[] args) {
//        try {
//             IrSequence irSequence = nec1_123456.toModulatedIrSequence(3);
//             .addNoise(10);
//            IrSequence[] seqs = irSequence.chop(25000);
//            for (IrSequence irs : seqs)
//                System.out.println(irs);
//        } catch (IncompatibleArgumentException ex) {
//            fail();
//        }
//        double[] d = {1, 2, 3, -4};
//        String raw = " +1266 -426 +1266 -426 +422 -1270 +1266 -426 +1266 -426 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +1266 -7096 +1266 -426 +1266 -426 +422 -1270 +1266 -426 +1266 -426 +422 -1270 +422 -1270 +422 -1270 +422 -1270 +422 -1270  +422 -1270 +1266 -7096   ";
//        try {
//            IrSequence irs = new IrSequence(d);
//            System.out.println(irs);
//            irs = new IrSequence(raw);
//            System.out.println(irs);
//            System.out.println(new IrSequence());
//        } catch (OddSequenceLenghtException e) {
//            fail();
//        }
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
     * Test of iget method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testIget() {
        System.out.println("iget");
        int i = 0;
        IrSequence instance = new IrSequence();
        int expResult = 0;
        int result = instance.iget(i);
        assertEquals(result, expResult);
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
     * Test of getLength method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetLength() {
        System.out.println("getLength");
        IrSequence instance = new IrSequence();
        int expResult = 0;
        int result = instance.getLength();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGap method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetGap() {
        System.out.println("getGap");
        IrSequence instance = new IrSequence();
        double expResult = 0.0;
        double result = instance.getGap();
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
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumberBursts method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testGetNumberBursts() {
        System.out.println("getNumberBursts");
        IrSequence instance = new IrSequence();
        int expResult = 0;
        int result = instance.getNumberBursts();
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
        double result = instance.getDuration();
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
        double result = instance.getDuration(begin, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of toPrintString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToPrintString_3args() {
        System.out.println("toPrintString");
        boolean alternatingSigns = false;
        boolean noSigns = false;
        String separator = "";
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toPrintString(alternatingSigns, noSigns, separator);
        assertEquals(result, expResult);
    }

    /**
     * Test of toPrintString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToPrintString_boolean_boolean() {
        System.out.println("toPrintString");
        boolean alternatingSigns = false;
        boolean noSigns = false;
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toPrintString(alternatingSigns, noSigns);
        assertEquals(result, expResult);
    }

    /**
     * Test of toPrintString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToPrintString_boolean() {
        System.out.println("toPrintString");
        boolean alternatingSigns = false;
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toPrintString(alternatingSigns);
        assertEquals(result, expResult);
    }

    /**
     * Test of toPrintString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToPrintString_0args() {
        System.out.println("toPrintString");
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toPrintString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToString_0args() {
        System.out.println("toString");
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class IrSequence.
     */
    @Test(enabled = false)
    public void testToString_boolean() {
        System.out.println("toString");
        boolean alternatingSigns = false;
        IrSequence instance = new IrSequence();
        String expResult = "";
        String result = instance.toString(alternatingSigns);
        assertEquals(result, expResult);
    }
}
