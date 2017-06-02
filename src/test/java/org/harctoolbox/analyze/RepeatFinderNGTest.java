package org.harctoolbox.analyze;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class RepeatFinderNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public RepeatFinderNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of findRepeat method, of class RepeatFinder.
     */
    @Test
    public void testFindRepeat_ModulatedIrSequence() {
        System.out.println("findRepeat");
        IrSignal irSignal;
        try {
            irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            ModulatedIrSequence irSequence = irSignal.toModulatedIrSequence(5);
            ModulatedIrSequence junk = new ModulatedIrSequence(new int[] { 1, 2, 3, 4}, irSignal.getFrequency(), irSignal.getDutyCycle());
            IrSignal reference = new IrSignal(irSignal.getIntroSequence(), irSignal.getRepeatSequence(), junk, irSignal.getFrequency(), irSignal.getDutyCycle());
            irSequence = irSequence.append(junk);
            IrSignal rep = RepeatFinder.findRepeat(irSequence);
            assertTrue(reference.approximatelyEquals(rep, 1f, 0.01, 1f));
            // Note: lasts gap is too short, should find three repetitons anyhow.
            int[] arr = new int[] { 9008, 4516, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 1717, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 1717, 552, 1717, 552, 1717, 552, 38902, 9008, 2289, 552, 31080, 9008, 2289, 552, 31080, 9008, 2289, 552, 21080 };
            ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(arr, 38400d);
            RepeatFinder repeatFinder = new RepeatFinder(modulatedIrSequence);
            assertEquals(repeatFinder.getRepeatFinderData().getNumberRepeats(), 3);
        } catch (InvalidArgumentException | Pronto.NonProntoFormatException ex) {
            assert(false);
        }
    }

    /**
     * Test of findRepeat method, of class RepeatFinder.
     */
    @Test
    public void testFindRepeat_ModulatedIrSequence2() {
        System.out.println("findRepeat2");
        IrSignal irSignal;
        try {
            int[] arr = new int[]{
                2336, 646, 572, 621, 1168, 621,
                572, 621, 1168, 621, 572, 621,
                572, 621, 1168, 646, 572, 621,
                1168, 621, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 572, 621,
                572, 646, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 1168, 11604,
                2336, 621, 572, 621, 1168, 621,
                572, 621, 1168, 621, 572, 621,
                572, 621, 1168, 646, 572, 621,
                1168, 621, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 572, 621,
                572, 646, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 1168, 11604,
                2336, 646, 572, 621, 1168, 621,
                572, 621, 1168, 621, 572, 621,
                572, 621, 1168, 646, 572, 621,
                1168, 621, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 572, 621,
                572, 646, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 1168, 11604,
                2336, 621, 572, 621, 1168, 621,
                572, 621, 1168, 621, 572, 621,
                572, 621, 1168, 646, 572, 621,
                1168, 621, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 572, 621,
                572, 646, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 1168, 11604,
                2336, 621, 572, 621, 1168, 621,
                572, 621, 1168, 621, 572, 621,
                572, 621, 1168, 646, 572, 621,
                1168, 621, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 572, 621,
                572, 646, 572, 621, 1168, 621,
                1168, 621, 1168, 621, 1168, 11604
            };
            ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(arr, 38400d);
            RepeatFinder repeatFinder = new RepeatFinder(modulatedIrSequence, 100d, 0.2d, 10000d);
            assertEquals(5, repeatFinder.getRepeatFinderData().getNumberRepeats());
        } catch (OddSequenceLengthException ex) {
            assert(false);
        }
    }

    @Test
    public void testFindRepeat_ModulatedIrSequence3() {
        System.out.println("findRepeat3");
        IrSignal irSignal;
        try {
            int[] arr = new int[]{
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 5750,
                334, 1687, 334, 1687, 334, 2696, 334, 1687, 334, 2696, 334, 500000
            };
            ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(arr, 38400d);
            RepeatFinder repeatFinder = new RepeatFinder(modulatedIrSequence, 100d, 0.2d, 5000d);
            assertEquals(12, repeatFinder.getRepeatFinderData().getNumberRepeats());
        } catch (OddSequenceLengthException ex) {
            assert(false);
        }
    }
}
