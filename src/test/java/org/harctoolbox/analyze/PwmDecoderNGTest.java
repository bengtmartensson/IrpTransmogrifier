package org.harctoolbox.analyze;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.InvalidRepeatException;
import org.harctoolbox.irp.IrpSemanticException;
import org.harctoolbox.irp.IrpSyntaxException;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.UnassignedException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class PwmDecoderNGTest {
    private final static int[] nec12_34_56_durations
            = new int[]{9024, 4512, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 44268};

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private IrSequence irSequence;
    private Analyzer analyzer;
    private PwmDecoder pwm;

    public PwmDecoderNGTest() {
        this.analyzer = null;
        this.irSequence = null;
        try {
            irSequence = new IrSequence(nec12_34_56_durations);
            Analyzer.AnalyzerParams analyzerParams = new Analyzer.AnalyzerParams(38400f, null, BitDirection.msb, true, null, false);
            analyzer = new Analyzer(irSequence);
            pwm = new PwmDecoder(analyzer, analyzerParams);
        } catch (OddSequenceLenghtException ex) {
            fail();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of process method, of class PwmDecoder.
     */
    @Test
    public void testProcess() {
        System.out.println("process");
        Protocol result;
        try {
            result = pwm.process();
        } catch (DecodeException ex) {
            fail();
            return;
        }
        System.out.println(result.toIrpString(16, false));
        try {
            Protocol expResult = new Protocol("{38.4k,564,msb}<1,-1|1,-3>(16,-8,A:32,1,^108m){A=0x30441ce3}");
            assertEquals(result, expResult);
        } catch (IrpSemanticException | IrpSyntaxException | ArithmeticException | InvalidArgumentException | InvalidRepeatException | UnassignedException ex) {
            Logger.getLogger(PwmDecoderNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
