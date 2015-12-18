package org.harctoolbox.irp;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class GeneralSpecTest {

    public GeneralSpecTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private static void test(String str) throws IrpSyntaxException, IrpSemanticException {
        GeneralSpec gs = new GeneralSpec(str);
        System.out.println(gs);
    }

    /**
     * Test of toString method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testToString() throws IrpSyntaxException, IrpSemanticException {
        System.out.println("toString");
        test("{ }"); // Seem to trigger bug in ANTLR
        test("{38.4k,564}");
        test("{564,38.4k}");
        test("{22p,40k}");
        test("{msb, 889u}");
        test("{42%, 10p,msb,40k}");
        test("{msb ,40k , 33.33333% ,10p }");
        test("{msb, 123u, 100k, 10p, 1000k}");
    }

    /**
     * Test of getBitDirection method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetBitDirection() throws IrpSyntaxException, IrpSemanticException {
        System.out.println("getBitDirection");
        GeneralSpec instance = new GeneralSpec("{msb ,40k , 33.33333% ,10p }");
        BitDirection result = instance.getBitDirection();
        Assert.assertEquals(result, BitDirection.msb);
    }

    /**
     * Test of getFrequency method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetFrequency() throws IrpSyntaxException, IrpSemanticException {
        System.out.println("getFrequency");
        GeneralSpec instance = new GeneralSpec("{msb, 12.3k, 33.33333% ,10p }");
        double expResult = 12300f;
        double result = instance.getFrequency();
        Assert.assertEquals(result, expResult, 0.0001);
    }

    /**
     * Test of getUnit method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetUnit() throws IrpSyntaxException, IrpSemanticException {
        System.out.println("getUnit");
        GeneralSpec instance = new GeneralSpec("{msb ,40k , 33.33333% ,10p }");
        double expResult = 250f;
        double result = instance.getUnit();
        Assert.assertEquals(result, expResult, 0.0001);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDutyCycle method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetDutyCycle() throws IrpSyntaxException, IrpSemanticException {
        System.out.println("getDutyCycle");
        GeneralSpec instance = new GeneralSpec("{msb ,40k , 33% ,10p }");
        double expResult = 0.33;
        double result = instance.getDutyCycle();
        Assert.assertEquals(result, expResult, 0.0001);
    }
}
