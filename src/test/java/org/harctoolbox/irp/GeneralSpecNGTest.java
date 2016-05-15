package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.testng.Assert;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GeneralSpecNGTest {

    public GeneralSpecNGTest() {
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

    /**
     * Test of toString method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testToString() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("toString");
            GeneralSpec instance = new GeneralSpec("{42%, 10p,msb,40k}");
            Assert.assertEquals(instance.toString(), "Frequency = 40000.0Hz, unit = 250.0us, msb, Duty cycle = 42%.");
            GeneralSpec.evaluatePrint("{ }");
            GeneralSpec.evaluatePrint("{38.4k,564}");
            GeneralSpec.evaluatePrint("{564,38.4k}");
            GeneralSpec.evaluatePrint("{22p,40k}");
            GeneralSpec.evaluatePrint("{msb, 889u}");
            GeneralSpec.evaluatePrint("{42%, 10p,msb,40k}");
            GeneralSpec.evaluatePrint("{msb ,40k , 33.33333% ,10p }");
            GeneralSpec.evaluatePrint("{msb, 123u, 100k, 10p, 1000k}");
            try {
                GeneralSpec.evaluatePrint("{1+2}");
                Assert.fail();
            } catch (ParseCancellationException ex) {
            }
        } catch (ArithmeticException | IncompatibleArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of getBitDirection method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetBitDirection() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getBitDirection");
            GeneralSpec instance = new GeneralSpec("{msb ,40k , 33.33333% ,10p }");
            BitDirection result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.msb);
            instance = new GeneralSpec("{lsb ,40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
            instance = new GeneralSpec("{40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
        } catch (ArithmeticException | IncompatibleArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getFrequency method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetFrequency() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getFrequency");
            GeneralSpec instance = new GeneralSpec("{msb, 12.3k, 33.33333% ,10p }");
            double result = instance.getFrequency();
            Assert.assertEquals(result, 12300f, 0.0001);
            Assert.assertEquals(new GeneralSpec("{msb, 33.33333% ,10p }").getFrequency(), ModulatedIrSequence.defaultFrequency, 0.0001);
        } catch (ArithmeticException | IncompatibleArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getUnit method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetUnit() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getUnit");
            GeneralSpec instance = new GeneralSpec("{msb ,40k , 33.33333% ,10p }"); // Do not remove the silly formatting!!
            double expResult = 250f;
            double result = instance.getUnit();
            Assert.assertEquals(result, expResult, 0.0001);
        } catch (ArithmeticException | IncompatibleArgumentException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
