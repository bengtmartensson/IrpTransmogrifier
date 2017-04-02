package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GeneralSpecNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public GeneralSpecNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getBitDirection method, of class GeneralSpec.
     */
    @Test
    public void testGetBitDirection() {
        try {
            System.out.println("getBitDirection");
            GeneralSpec instance = new GeneralSpec("{lsb, msb ,40k , 33.33333% ,10p }");
            BitDirection result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.msb);
            instance = new GeneralSpec("{lsb ,40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
            instance = new GeneralSpec("{40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
        } catch (IrpInvalidArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getFrequency method, of class GeneralSpec.
     */
    @Test
    public void testGetFrequency() {
        try {
            System.out.println("getFrequency");
            GeneralSpec instance = new GeneralSpec("{msb, 12.3k, 33.33333% ,10p }");
            double result = instance.getFrequency();
            Assert.assertEquals(result, 12300d, 0.0001);
            Assert.assertTrue(new GeneralSpec("{msb, 33.33333% ,10p }").getFrequency() == null);
        } catch (IrpInvalidArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getUnit method, of class GeneralSpec.
     */
    @Test
    public void testGetUnit() {
        try {
            System.out.println("getUnit");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 33.33333% ,10p }"); // Do not remove the silly formatting!!
            double expResult = 250f;
            double result = instance.getUnit();
            Assert.assertEquals(result, expResult, 0.0001);
        } catch (IrpInvalidArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getDutyCycle method, of class GeneralSpec.
     */
    @Test
    public void testGetDutyCycle() {
        try {
            System.out.println("getDutyCycle");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
            double expResult = 0.73f;
            double result = instance.getDutyCycle();
            Assert.assertEquals(result, expResult, 0.0001);
        } catch (IrpInvalidArgumentException ex) {
            fail();
        }
    }

//    /**
//     * Test of evaluatePrint method, of class GeneralSpec.
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testEvaluatePrint() throws Exception {
//        System.out.println("evaluatePrint");
//        GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
//        String str = "";
//        instance.evaluatePrint(str);
//    }

    /**
     * Test of toIrpString method, of class GeneralSpec.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
            String expResult = "{40.0k,250,msb,73%}";
            String result = instance.toIrpString();
            assertEquals(result, expResult);

            instance = new GeneralSpec("{42%, 10p,msb,40k}");
            Assert.assertEquals(instance.toIrpString(), "{40.0k,250,msb,42%}");
            assertEquals(new GeneralSpec("{ }").toIrpString(), "{1,lsb}");
            assertEquals(new GeneralSpec("{38.4k,564}").toIrpString(), "{38.4k,564,lsb}");
            assertEquals(new GeneralSpec("{564,38.4k}").toIrpString(), "{38.4k,564,lsb}");
            assertEquals(new GeneralSpec("{22p,40k}").toIrpString(), "{40.0k,550,lsb}");
            assertEquals(new GeneralSpec("{22p,40k}").toIrpString(false), "{40.0k,550,lsb}");
            assertEquals(new GeneralSpec("{22p,40k}").toIrpString(true), "{40.0k,22p,lsb}");
            assertEquals(new GeneralSpec("{msb, 889u}").toIrpString(), "{889,msb}");
            assertEquals(new GeneralSpec("{42%, 10p,msb,40k}").toIrpString(), "{40.0k,250,msb,42%}");
            assertEquals(new GeneralSpec("{msb ,40k , 33.33333% ,10p }").toIrpString(), "{40.0k,250,msb,33%}");
            assertEquals(new GeneralSpec("{msb, 123u, 100k, 10p, 1000k}").toIrpString(), "{1000.0k,10,msb}");
        } catch (IrpInvalidArgumentException ex) {
            fail();
        }
    }
}
