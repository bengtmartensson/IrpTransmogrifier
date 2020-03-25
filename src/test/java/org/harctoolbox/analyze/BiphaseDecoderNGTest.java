package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.OddSequenceLengthException;
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
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class BiphaseDecoderNGTest {

    private static final int[] rc5_12_3_1 = new int[] {
        889, 889, 889, 889, 1778, 1778, 889, 889, 1778, 889, 889, 889, 889, 889, 889, 889, 889, 889, 889, 1778, 889, 889, 889, 89997
    };
    private static final int[] rc6_12_3_1 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 444, 888, 888, 444, 888, 444, 444, 444, 444, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 84356
        //2664, 888, 444, 888, 444, 444, 444, 444, 1332, 1332, 444, 444, 444, 444, 444, 444, 888, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 84356
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Analyzer rc5;
    private final Analyzer rc6;
    private final Analyzer.AnalyzerParams paramsRc5;
    private final Analyzer.AnalyzerParams paramsRc6;

    public BiphaseDecoderNGTest() throws OddSequenceLengthException, InvalidArgumentException {
        rc5 = new Analyzer(rc5_12_3_1);
        rc6 = new Analyzer(rc6_12_3_1);
        List<Integer> widths = new ArrayList<>(4);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(5);
        paramsRc5 = new Analyzer.AnalyzerParams(36000d, null, BitDirection.msb, true, widths, true);
        paramsRc6 = new Analyzer.AnalyzerParams(36000d, null, BitDirection.msb, true, null, false);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of process method, of class BiphaseDecoder.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseRc5() throws Exception {
        System.out.println("processRc5");
        BiphaseDecoder decoder = new BiphaseDecoder(rc5, paramsRc5);
        Protocol result = decoder.parse()[0];
        System.out.println("Expect warning for missing parameterspec");
        Protocol expResult = new Protocol("{36k,msb,889}<1,-1|-1,1>(A:1,B:1,C:1,D:5,E:6,^114m){A=1,B=1,C=1,D=12,E=3}");
        assertEquals(result, expResult);
    }

    @Test
    public void testParseRc6() throws Exception {
        System.out.println("processRc6");
        AbstractBiphaseDecoder decoder = new BiphaseDecoder(rc6, paramsRc6);
        Protocol result = decoder.parse()[0];
        System.out.println("Expect warning for missing parameterspec");
        Protocol expResult = new Protocol("{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:4,-2,2,B:16,^107m){A=8,B=30723}");
        assertEquals(result, expResult);
    }
}
