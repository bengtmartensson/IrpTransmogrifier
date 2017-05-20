package org.harctoolbox.irp;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GapNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private GeneralSpec generalSpec;
    private NameEngine nameEngine;

    public GapNGTest() {
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
        Gap instance = new Gap("-A");
        double result = instance.evaluateWithSign(generalSpec, nameEngine, 0f);
        Assert.assertEquals(result, -123000f, 0.0);
    }

    /**
     * Test of interleavingOk method, of class Flash.
     */
    @Test
    public void testInterleavingOk() {
        System.out.println("interleavingOk");
        Gap instance = new Gap("-Z");
        boolean result = instance.interleavingOk(DurationType.flash, false);
        Assert.assertTrue(result);
        result = instance.interleavingOk(DurationType.gap, false);
        Assert.assertFalse(result);
    }
}
