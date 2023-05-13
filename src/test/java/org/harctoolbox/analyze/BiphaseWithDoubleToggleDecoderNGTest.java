package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irp.Protocol;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class BiphaseWithDoubleToggleDecoderNGTest {

    private static final double ABSOLUTE_TOLERANDE = 100.0;
    private static final double RELATIVE_TOLERANCE = 0.1;

    private static final int[] rc6_255_0_0 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 444, 888, 1332, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 83912
    };
    private static final int[] rc6_255_0_1 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 1332, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 83912
    };
    private static final int[] rc6_m_32_0_0_0_0_0_0 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 444, 888, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };
    private static final int[] rc6_m_32_0_0_1_0_0_0 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 1332, 1332, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };
    private static final int[] rc6_m_32_0_0_0_7_0_0 = new int[]{
        2664, 888, 444, 444, 444, 444, 444, 444, 444, 1332, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };
    private static final int[] rc6_m_32_0_0_1_7_0_0 = new int[]{
        2664, 888, 444, 444, 444, 444, 444, 444, 444, 444, 888, 1332, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };
    private static final int[] rc6_m_32_0_0_0_0_255_0 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 444, 888, 1332, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };
    private static final int[] rc6_m_32_0_1_0_255_0_0 = new int[]{
        2664, 888, 444, 888, 444, 444, 444, 444, 1332, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 888, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 444, 69704
    };

    private static Protocol parse(int[] data) throws OddSequenceLengthException, DecodeException, InvalidArgumentException {
        Analyzer analyzer = new Analyzer(new IrSequence(data), ModulatedIrSequence.DEFAULT_FREQUENCY, false, ABSOLUTE_TOLERANDE, RELATIVE_TOLERANCE);

        List<Integer> widths = new ArrayList<>(8);
        widths.add(1);
        widths.add(3);
        widths.add(1);
        widths.add(8);
        widths.add(8);
        widths.add(8);
        widths.add(8);
        widths.add(8);
        Analyzer.AnalyzerParams paramsRc6 = new Analyzer.AnalyzerParams(36000d, null, BitDirection.msb, true, widths, false);
        AbstractBiphaseDecoder decoder = new BiphaseWithDoubleToggleDecoder(analyzer, paramsRc6);
        Protocol result = decoder.parse()[0];
        return result;
    }

    private static void testStuff(int[] data, String expected) throws IrpException, OddSequenceLengthException, DecodeException, InvalidArgumentException {
        Protocol result = parse(data);
        Protocol expResult = new Protocol(expected);
        assertEquals(result, expResult);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testParserc6_255_0_0() throws IrpException, OddSequenceLengthException, DecodeException, InvalidArgumentException {
        System.out.println("processrc6_255_0_0");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_255_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,^107m){A=1,B=0,C=0,D=255,E=0}");
    }

    @Test
    public void testParserc6_255_0_1() throws Exception {
        System.out.println("processrc6_255_0_1");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_255_0_1, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,^107m){A=1,B=0,C=1,D=255,E=0}");
    }

    @Test
    public void testParserc6_m_32_0_0_0_0_0_0() throws Exception {
        System.out.println("processrc6_255_0_1");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_0_0_0_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=0,C=0,D=0,E=0,F=0,G=0}");
    }

    @Test
    public void testParserrc6_m_32_0_0_1_0_0_0() throws Exception {
        System.out.println("processrc6_m_32_0_0_1_0_0_0");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_0_1_0_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=0,C=1,D=0,E=0,F=0,G=0}");
    }

    @Test
    public void testParserc6_m_32_0_0_0_7_0_0() throws Exception {
        System.out.println("processrc6_m_32_0_0_0_7_0_0");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_0_0_7_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=7,C=0,D=0,E=0,F=0,G=0}");
    }

    @Test
    public void testParserrc6_m_32_0_0_1_7_0_0() throws Exception {
        System.out.println("processrc6_m_32_0_0_1_7_0_0");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_0_1_7_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=7,C=1,D=0,E=0,F=0,G=0}");
    }

    @Test
    public void testParserrc6_m_32_0_0_0_0_255_0() throws Exception {
        System.out.println("processrc6_255_0_1");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_0_0_0_255_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=0,C=0,D=255,E=0,F=0,G=0}");
    }

    @Test
    public void testParsercrc6_m_32_0_1_0_255_0_0() throws Exception {
        System.out.println("processrc6_255_0_1");
        System.out.println("Expect warning for missing parameterspec");
        testStuff(rc6_m_32_0_1_0_255_0_0, "{36.0k,444,msb}<-1,1|1,-1>(6,-2,A:1,B:3,<-2,2|2,-2>(C:1),D:8,E:8,F:8,G:8,^107m){A=1,B=0,C=1,D=255,E=0,F=0,G=0}");
    }
}
