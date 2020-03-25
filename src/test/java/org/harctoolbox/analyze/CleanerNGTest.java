package org.harctoolbox.analyze;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class CleanerNGTest {
    private final static String NEC_12_34_56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final ModulatedIrSequence irSequence;
    private final ModulatedIrSequence noisy;
    private final IrSignal irSignal;

    public CleanerNGTest() throws Pronto.NonProntoFormatException, InvalidArgumentException {
        IrSequence.initRandom(997);
        irSignal = Pronto.parse(NEC_12_34_56);
        irSequence = irSignal.toModulatedIrSequence(5);
        noisy = irSequence.addNoise(60.0);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of clean method, of class Cleaner.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testClean_IrSequence() throws InvalidArgumentException {
        System.out.println("clean");
        final double absolute = 60.0;
        final double relative = 0.1;
        IrSequence verynoisy = irSequence.addNoise(absolute);
        IrSequence cleaned = Cleaner.clean(verynoisy, absolute, relative);
        //Assert.assertFalse(irSequence.approximatelyEquals(verynoisy, IrCoreUtils.defaultAbsoluteTolerance, 0.1));
        Assert.assertTrue(irSequence.approximatelyEquals(cleaned, absolute, relative));

        IrSequence reallynoisy = irSequence.addNoise(200);
        cleaned = Cleaner.clean(reallynoisy, absolute, relative);
        Assert.assertFalse(irSequence.approximatelyEquals(reallynoisy, absolute, relative));
        Assert.assertFalse(irSequence.approximatelyEquals(cleaned, absolute, relative));
        //Assert.assertFalse(irSequence.approximatelyEquals(cleaned));
    }

    /**
     * Test of getIndexData method, of class Cleaner.
     */
//    @Test
//    public void testGetIndexData() {
//        System.out.println("getIndexData");
//        Cleaner instance = new Cleaner(noisy, 60, 0.2);
//        int[] expResult = new int[] { 4,3,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,0,1,0,5,4,2,0,6,4,2,0,6,4,2,0,6,4,2,0,6};
//        int[] result = instance.getIndexData();
//        assertEquals(result, expResult);
//    }
    /**
     * Test of toTimingsString method, of class Cleaner.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToTimingsString() throws InvalidArgumentException {
        System.out.println("toTimingsString");
        Cleaner instance = new Cleaner(noisy, 60d, 0.2);
        String expResult = "ED AA AA AB AB AA AA AA AA AA AB AA AA AA AB AA AA AA AA AA AB AB AB AA AA AB AB AB AA AA AA AB AB AF EC AG EC AG EC AG EC AG";
        String result = instance.toTimingsString();
        assertEquals(result, expResult);
    }

    /**
     * Test of clean method, of class Cleaner.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testClean_3args_1() throws InvalidArgumentException {
        System.out.println("clean");
        double absoluteTolerance = 120.0;
        double relativeTolerance = 0.1;
        IrSequence result = Cleaner.clean(noisy, absoluteTolerance, relativeTolerance);
        Assert.assertTrue(result.approximatelyEquals(irSequence, 3.0, 0.01));
    }

    /**
     * Test of clean method, of class Cleaner.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testClean_3args_2() throws InvalidArgumentException {
        System.out.println("clean");
        double absoluteTolerance = 280.0;
        double relativeTolerance = 0.1;
        ModulatedIrSequence result = Cleaner.clean(noisy, absoluteTolerance, relativeTolerance);
        Assert.assertTrue(result.approximatelyEquals(irSequence));
    }

    /**
     * Test of mkName method, of class Cleaner.
     */
    @Test
    public void testMkName() {
        System.out.println("mkName");
        try {
            assertEquals(Cleaner.mkName(null), "?");
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            assertEquals(Cleaner.mkName(-1), "?");
            fail();
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(Cleaner.mkName(0), "A");
        assertEquals(Cleaner.mkName(25), "Z");
        assertEquals(Cleaner.mkName(26), "BA");
        assertEquals(Cleaner.mkName(27), "BB");
        assertEquals(Cleaner.mkName(1000), "BMM");
        assertEquals(Cleaner.mkName(10000), "OUQ");
        assertEquals(Cleaner.mkName(26 * 26 * 26 * 26), "BAAAA");
    }
}
