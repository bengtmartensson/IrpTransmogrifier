package org.harctoolbox.analyze;

import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.Protocol;
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
public class Pwm2DecoderNGTest {
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
    private final Pwm2Decoder pwm;

    public Pwm2DecoderNGTest() throws Exception {
        this.analyzer = null;
        this.irSequence = null;
        irSequence = new IrSequence(nec12_34_56_durations);
        Analyzer.AnalyzerParams analyzerParams = new Analyzer.AnalyzerParams(38400d, null, BitDirection.msb, true, null, false);
        analyzer = new Analyzer(irSequence);
        pwm = new Pwm2Decoder(analyzer, analyzerParams);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of process method, of class Pwm2Decoder.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("process");
        Protocol result;
        result = pwm.parse()[0];
        System.out.println(result.toIrpString(16, false));
        System.out.println("Expect warnings for missing parameterspec");
        Protocol expResult = new Protocol("{38.4k,564,msb}<1,-1|1,-3>(16,-8,A:32,1,^108m){A=0x30441ce3}");
        assertEquals(result, expResult);
    }
}
