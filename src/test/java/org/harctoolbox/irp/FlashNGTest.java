package org.harctoolbox.irp;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FlashNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private GeneralSpec generalSpec;
    private NameEngine nameEngine;

    public FlashNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        generalSpec = new GeneralSpec("{40k,1000u}");
        nameEngine = new NameEngine("{A=123, B=73}");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of evaluateWithSign method, of class Flash.
     * @throws java.lang.Exception
     */
    @Test
    public void testEvaluateWithSign() throws Exception {
        System.out.println("evaluateWithSign");
        Flash instance = new Flash("A");
        double result = instance.evaluateWithSign(generalSpec, nameEngine, 0f);
        Assert.assertEquals(result, 123000, 0.0);
    }

//    /**
//     * Test of toElement method, of class Flash.
//     */
//    @Test
//    public void testToElement() throws Exception {
//        System.out.println("toElement");
//        Document document = null;
//        Flash instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of interleavingOk method, of class Flash.
     */
    @Test
    public void testInterleavingOk() {
        System.out.println("interleavingOk");
        Flash instance = new Flash("Z");
        boolean result = instance.interleavingOk(null, null, DurationType.gap, false);
        Assert.assertTrue(result);
    }

}
