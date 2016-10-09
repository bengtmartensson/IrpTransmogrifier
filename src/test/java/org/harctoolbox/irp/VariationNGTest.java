package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VariationNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public VariationNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of isEmpty method, of class Variation.
//     */
//    @Test
//    public void testIsEmpty() throws Exception {
//        System.out.println("isEmpty");
//        NameEngine nameEngine = null;
//        Variation instance = null;
//        boolean expResult = false;
//        boolean result = instance.isEmpty(nameEngine);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of evaluate method, of class Variation.
//     */
//    @Test
//    public void testEvaluate() throws Exception {
//        System.out.println("evaluate");
//        NameEngine nameEngine = null;
//        GeneralSpec generalSpec = null;
//        BitSpec bitSpec = null;
//        IrSignal.Pass pass = null;
//        double elapsed = 0.0;
//        Variation instance = null;
//        EvaluatedIrStream expResult = null;
//        EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of toElement method, of class Variation.
//     */
//    @Test
//    public void testToElement() throws Exception {
//        System.out.println("toElement");
//        Document document = null;
//        Variation instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of interleavingOk method, of class Variation.
//     */
//    @Test
//    public void testInterleavingOk() {
//        System.out.println("interleavingOk");
//        Variation instance = null;
//        boolean expResult = false;
//        boolean result = instance.interleavingOk();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of numberOfBareDurations method, of class Variation.
//     */
//    @Test
//    public void testNumberOfBareDurations() {
//        System.out.println("numberOfBareDurations");
//        Variation instance = null;
//        int expResult = 0;
//        int result = instance.numberOfBareDurations();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of numberOfBits method, of class Variation.
//     */
//    @Test
//    public void testNumberOfBits() {
//        System.out.println("numberOfBits");
//        Variation instance = null;
//        int expResult = 0;
//        int result = instance.numberOfBits();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toIrpString method, of class Variation.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            Variation instance = new Variation("[P=0][P=1][P=2]");
            String result = instance.toIrpString();
            assertEquals(result, "[P=0][P=1][P=2]");

            instance = new Variation("[P=0][][P=2]");
            result = instance.toIrpString();
            assertEquals(result, "[P=0][][P=2]");

            instance = new Variation("[P=0][P=1]");
            result = instance.toIrpString();
            assertEquals(result, "[P=0][P=1]");

            instance = new Variation("[P=0][P=1][]");
            result = instance.toIrpString();
            assertEquals(result, "[P=0][P=1]");
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            fail();
        }
    }

//    /**
//     * Test of toString method, of class Variation.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        Variation instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
