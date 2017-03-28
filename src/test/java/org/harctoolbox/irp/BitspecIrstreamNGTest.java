package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BitspecIrstreamNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final BitspecIrstream instance;
    public BitspecIrstreamNGTest() {
        instance = new BitspecIrstream("<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrpString method, of class BitspecIrstream.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

//    /**
//     * Test of toElement method, of class BitspecIrstream.
//     */
//    @Test
//    public void testToElement() throws Exception {
//        System.out.println("toElement");
//        Document document = null;
//        BitspecIrstream instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of isEmpty method, of class BitspecIrstream.
//     */
//    @Test
//    public void testIsEmpty() throws Exception {
//        System.out.println("isEmpty");
//        NameEngine nameEngine = null;
//        BitspecIrstream instance = null;
//        boolean expResult = false;
//        boolean result = instance.isEmpty(nameEngine);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of evaluate method, of class BitspecIrstream.
     * @throws java.lang.Exception
     */
    @Test
    public void testEvaluate_4args() throws Exception {
        System.out.println("evaluate");
//        NameEngine nameEngine = null;
//        GeneralSpec generalSpec = null;
//        IrSignal.Pass pass = null;
//        double elapsed = 0.0;
//        BitspecIrstream instance = null;
//        EvaluatedIrStream expResult = null;
//        EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, pass, elapsed);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of evaluate method, of class BitspecIrstream.
     * @throws java.lang.Exception
     */
    @Test
    public void testEvaluate_5args() throws Exception {
        System.out.println("evaluate");
//        NameEngine nameEngine = null;
//        GeneralSpec generalSpec = null;
//        BitSpec bitSpec = null;
//        IrSignal.Pass pass = null;
//        double elapsed = 0.0;
//        BitspecIrstream instance = null;
//        EvaluatedIrStream expResult = null;
//        EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
//        assertEquals(result, expResult);
    }

    /**
     * Test of numberOfBitSpecs method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBitSpecs() {
        System.out.println("numberOfBitSpecs");
        int result = instance.numberOfBitSpecs();
        assertEquals(result, 1);
    }

    /**
     * Test of interleavingOk method, of class BitspecIrstream.
     */
    @Test
    public void testInterleavingOk() {
        System.out.println("interleavingOk");
        boolean result = instance.interleavingOk(new GeneralSpec(), null);
        assertTrue(result);
    }

    /**
     * Test of numberOfBits method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        int result = instance.numberOfBits();
        assertEquals(result, 32);
    }

    /**
     * Test of numberOfBareDurations method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        int result = instance.numberOfBareDurations(true);
        assertEquals(result, 8);
        result = instance.numberOfBareDurations(false);
        assertEquals(result, 4);
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, 1);
    }

}
