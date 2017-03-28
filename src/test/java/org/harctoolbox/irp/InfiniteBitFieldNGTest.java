package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InfiniteBitFieldNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private NameEngine nameEngine = null;
    private final InfiniteBitField instance;

    public InfiniteBitFieldNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{A = 7, F=244, D=4}");
        instance = new InfiniteBitField("~D::2");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toNumber method, of class InfiniteBitField.
     */
    @Test
    public void testToNumber() {
        System.out.println("toNumber");
        long result = 0;
        try {
            result = instance.toNumber(nameEngine);
        } catch (NameUnassignedException ex) {
            fail();
        }
        assertEquals(result, -2L);
    }

    /**
     * Test of getWidth method, of class InfiniteBitField.
     */
    @Test
    public void testGetWidth() {
        System.out.println("getWidth");
        long result = instance.getWidth(nameEngine);
        assertEquals(result, 63L);
    }

    /**
     * Test of toString method, of class InfiniteBitField.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString(nameEngine);
        assertEquals(result, "~4::2");
    }

    /**
     * Test of toIrpString method, of class InfiniteBitField.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "~D::2");
    }

    /**
     * Test of evaluate method, of class InfiniteBitField.
     */
//    @Test
//    public void testEvaluate() throws Exception {
//        System.out.println("evaluate");
//        NameEngine nameEngine = null;
//        GeneralSpec generalSpec = null;
//        BitSpec bitSpec = null;
//        IrSignal.Pass pass = null;
//        double elapsed = 0.0;
//        InfiniteBitField instance = null;
//        EvaluatedIrStream expResult = null;
//        EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of toElement method, of class InfiniteBitField.
//     */
//    @Test
//    public void testToElement() throws Exception {
//        System.out.println("toElement");
//        Document document = null;
//        InfiniteBitField instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of numberOfBits method, of class InfiniteBitField.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        Integer result = instance.numberOfBits();
        assertTrue(result == 0L);
    }

//    /**
//     * Test of numberOfBareDurations method, of class InfiniteBitField.
//     */
//    @Test
//    public void testNumberOfBareDurations() {
//        System.out.println("numberOfBareDurations");
//        try {
//            instance.numberOfBareDurations(true);
//            fail();
//        } catch (UnsupportedOperationException ex) {
//        }
//    }
}
