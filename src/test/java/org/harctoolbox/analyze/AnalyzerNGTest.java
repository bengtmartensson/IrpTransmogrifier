package org.harctoolbox.analyze;

import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnalyzerNGTest {

    private static final int[] ALLDATA = new int[]{9041, 4507, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 44293, 9041, 2267, 626, 96193, 9041, 2267, 626, 96193, 9041, 2267, 626, 96193, 9041, 2267, 626, 96193};
    private static final int[] INTRODATA = new int[]{9041, 4507, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 44293};
    private static final int[] REPEATDATA = new int[]{9041, 2267, 626, 96193};

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public AnalyzerNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of repeatReducedIrSignal method, of class Analyzer.
     */
    @Test
    public void testRepeatReducedIrSignal() {
        try {
            System.out.println("repeatReducedIrSignal");
            IrSequence irSequence = new IrSequence(ALLDATA);
            Analyzer instance = new Analyzer(irSequence, 38400d, true);
            IrSignal expResult = new IrSignal(new IrSequence(INTRODATA), new IrSequence(REPEATDATA), new IrSequence(), 38400d);
            IrSignal result = instance.repeatReducedIrSignal(0);
            assertTrue(result.approximatelyEquals(expResult));
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }
}
