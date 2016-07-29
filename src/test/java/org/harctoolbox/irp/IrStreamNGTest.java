package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrStreamNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //private final NameEngine nameEngine;
    private final IrStream instance;
    private final IrStream repeat;

    public IrStreamNGTest() throws IrpSyntaxException, InvalidRepeatException {
        //nameEngine = new NameEngine("{D=12, S=34, F= 56}");
        instance = new IrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
        repeat = new IrStream("(16,-4,1,^108m)*");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getRepeatMarker method, of class IrStream.
     */
    @Test
    public void testGetRepeatMarker() {
        System.out.println("getRepeatMarker");
        RepeatMarker result = instance.getRepeatMarker();
        assertTrue(result == null);
        result = repeat.getRepeatMarker();
        assertEquals(result.getMin(), 0L);
        assertTrue(result.isInfinite());
    }

    /**
     * Test of evaluate method, of class IrStream.
     */
    @Test
    public void testEvaluate() {

        //EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
    }

    /**
     * Test of toString method, of class IrStream.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString();
        assertEquals(result, "(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

    /**
     * Test of toIrpString method, of class IrStream.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

    /**
     * Test of isRepeatSequence method, of class IrStream.
     */
    @Test
    public void testIsRepeatSequence() {
        System.out.println("isRepeatSequence");
        boolean result = instance.isRepeatSequence();
        assertFalse(result);
    }

//    /**
//     * Test of toElement method, of class IrStream.
//     */
//    @Test
//    public void testToElement() throws Exception {
//        System.out.println("toElement");
//        Document document = null;
//        IrStream instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of numberOfBareDurations method, of class IrStream.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        int result = instance.numberOfBareDurations();
        assertEquals(result, 8);
    }

    /**
     * Test of numberOfBits method, of class IrStream.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        int result = instance.numberOfBits();
        assertEquals(result, 32);
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class IrStream.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, 1);
    }
}
