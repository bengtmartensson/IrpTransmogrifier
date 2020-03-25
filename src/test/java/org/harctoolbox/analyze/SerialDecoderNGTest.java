package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.Protocol;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class SerialDecoderNGTest {

    private static final double ABSOLUTE_TOLERANCE = 100.0;
    private static final double RELATIVE_TOLERANCE = 0.1;

    private static final int[] pctv_12_34 = new int[] {
        1664, 6656, 832, 1664, 1664, 4160, 832, 2496, 832, 1664, 1664, 100000
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Analyzer pctv;
    private final Analyzer.AnalyzerParams paramsPctv;
    public SerialDecoderNGTest() throws InvalidArgumentException {
        pctv = new Analyzer(new IrSequence(pctv_12_34), ModulatedIrSequence.DEFAULT_FREQUENCY, false, ABSOLUTE_TOLERANCE, RELATIVE_TOLERANCE);
        List<Integer> widths = new ArrayList<>(5);
        widths.add(2);
        widths.add(8);
        widths.add(1);
        widths.add(8);
        widths.add(8);
        paramsPctv = new Analyzer.AnalyzerParams(38400d, null, BitDirection.lsb, false, widths, false);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class SerialDecoder.
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        SerialDecoder decoder = new SerialDecoder(pctv, paramsPctv);
        Protocol result = decoder.parse()[0];
        System.out.println("Expect warnings on missing ParameterSpec");
        Protocol expResult = new Protocol("{38.4k,832,lsb}<-1|1>(A:2,B:8,C:1,D:8,E:8,F:2,-100m){A=3,B=0,C=1,D=12,E=34,F=3}");
        assertEquals(result, expResult);
    }
}
