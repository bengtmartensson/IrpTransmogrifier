package org.harctoolbox.ircore;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ModulatedIrSequenceNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ModulatedIrSequenceNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    /**
     * Test of clone method, of class ModulatedIrSequence.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        try {
            ModulatedIrSequence instance = new ModulatedIrSequence(new double[]{1, 3, 4, 5}, 12345d, 0.45);
            ModulatedIrSequence result = instance.clone();
            assertTrue(result.approximatelyEquals(instance));
        } catch (OddSequenceLengthException ex) {
            fail();
        }
    }

    /**
     * Test of modulate method, of class ModulatedIrSequence.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testModulate() throws OddSequenceLengthException {
        System.out.println("modulate");
        double[] data = new double[] {100, 100, 105, 100, 92, 100};

        ModulatedIrSequence instance = new ModulatedIrSequence(data, 100000.0);
        IrSequence expResult = new IrSequence(new double[]{4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,106,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,101,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,2,100});
        IrSequence result = instance.modulate();
        assertTrue(expResult.approximatelyEquals(result));
    }

    /**
     * Test of demodulate method, of class ModulatedIrSequence.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testDemodulate_IrSequence_double() throws OddSequenceLengthException {
        System.out.println("demodulate");
        IrSequence irSequence = new IrSequence(new double[]{4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,106,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,101,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,4,6,2,100});
        double threshold = 20.0;
        ModulatedIrSequence expResult = new ModulatedIrSequence(new double[]{94, 106, 104, 101, 92, 100}, 100000.0, 0.4);
        ModulatedIrSequence result = ModulatedIrSequence.demodulate(irSequence, threshold);
        assertTrue(expResult.approximatelyEquals(result, 0, 0.001, 0.001, 0.001));
    }
}
