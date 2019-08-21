package org.harctoolbox.irp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.harctoolbox.cmdline.ProgramExitStatus;
import org.harctoolbox.ircore.IrCoreUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrpTransmogrifierNGTest {

    private static final String IRPPROTOCOLS_XML="src/main/resources/IrpProtocols.xml";
    private static final String ACDATA = "3120, 1588, 548, 1068, 524, 1064, 548, 300, 528, 292, 544, 300, 524, 1064, 548, 300, 528, 320, 528, 1060, 520, 1092, 528, 292, 524, 1092, 520, 300, 528, 320, 524, 1064, 520, 1096, 524, 296, 540, 1076, 528, 1056, 552, 296, 520, 300, 548, 1068, 524, 320, 528, 292, 524, 1092, 528, 292, 524, 324, 524, 296, 520, 328, 520, 300, 544, 300, 528, 320, 528, 292, 524, 324, 524, 296, 520, 328, 516, 304, 524, 320, 528, 292, 576, 272, 524, 296, 552, 296, 528, 320, 528, 292, 524, 320, 528, 1060, 528, 320, 528, 292, 576, 1040, 520, 1068, 544, 300, 524, 296, 552, 296, 520, 300, 548, 300, 528, 320, 524, 1060, 524, 324, 520, 1068, 524, 1092, 520, 300, 524, 320, 528, 292, 576, 272, 524, 296, 552, 1064, 524, 320, 528, 292, 524, 328, 520, 296, 520, 328, 520, 300, 524, 320, 528, 292, 576, 272, 524, 296, 572, 276, 520, 324, 520, 300, 528, 320, 528, 292, 524, 320, 528, 292, 524, 324, 520, 300, 580, 268, 528, 292, 576, 272, 520, 324, 524, 296, 520, 328, 520, 300, 528, 316, 528, 292, 524, 324, 524, 296, 572, 276, 520, 300, 576, 272, 524, 320, 548, 272, 556, 292, 556, 264, 548, 1064, 528, 1060, 520, 1068, 556, 1060, 520, 324, 552, 272, 576, 268, 560, 1028, 572, 1044, 524, 10000";
    private static final String ACDATA1 = "4400,4250, 550,1600, 550,1600, 550,1600, 550,1550, 600,500, 550,500, 550,1600, 550,500, 550,550, 550,500, 550,500, 600,500, 550,1600, 550,1550, 550,550, 550,1600, 550,500, 550,500, 550,550, 550,500, 550,550, 550,1550, 550,550, 550,1550, 600,1550, 550,1600, 550,1600, 550,1600, 550,1550, 600,500, 550,1600, 550,500, 550,500, 550,550, 550,500, 600,450, 600,500, 550,500, 550,550, 550,1550, 550,550, 550,1600, 550,1550, 550,1600, 550,550, 550,500, 550,500, 550,550, 550,500, 550,500, 600,500, 550,500, 550,550, 550,500, 550,500, 600,500, 550,500, 550,550, 550,500, 550,500, 600,500, 550,500, 550,550, 550,500, 550,500, 550,550, 550,1600, 550,500, 550,500, 600,500, 550,500, 550,1600, 550,550, 500,550, 550,500, 550,550, 500,550, 550,550, 500,1600, 550,550, 500,550, 550,1600, 550,500, 550,1600, 550,550, 500,550, 550,1550, 550,550, 550";
    private static final String BIPHASE = "0000 0067 0000 0045 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 031F";
    private static final String NEC1INTRO = "+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293";
    private static final String NEC1DITTO = "+9041 -2267 +626 -96193";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrpTransmogrifierNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test(enabled = true)
    public void testFrequencyParser() {
        System.out.println("FrequencyParser");
        IrpTransmogrifier.FrequencyParser frequencyParser = new IrpTransmogrifier.FrequencyParser();
        assertEquals(frequencyParser.convert("38123"), 38123.0);
        assertEquals(frequencyParser.convert("38.1k"), 38100.0);
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     */
    @Test(enabled = true)
    public void testAnalyze1() {
        System.out.println("analyze1");
        String args = "-a 10 -r 0.01 analyze --radix 16 -M 32 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:32,6,-89m)*{A=0xc38c922}");
        args = "-a 10 -r 0.04 analyze --radix 16 -M 16 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:16,B:16,6,-89m)*{A=0xc38,B=0xc922}");
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     */
    @Test(enabled = true)
    public void testAnalyze2() {
        System.out.println("analyze2");
        String args = "-r 0.1 -a 100.0 analyze --radix 16 --chop 30000 --ire --maxparameterwidth 32 +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +573 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{573,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m,(16,-4,1,-96m)*){A=0x30441ce3}");
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     */
    @Test(enabled = true)
    public void testAnalyze2_1() {
        System.out.println("analyze2_1");
        String args = "-r 0.1 -a 100.0 analyze --radix 16 --chop 30000 --maxparameterwidth 32 +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +573 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result,
                "#0\t{573,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m){A=0x30441ce3}"
                + IrCoreUtils.LINE_SEPARATOR
                + "#1\t{573,msb}<>(16,-4,1,-96m)");
    }

    @Test(enabled = true)
    public void testAnalyze3() {
        System.out.println("analyze3");
        String args = "analyze --radix 16 --maxparameterwidth 32 --ire " +
                "[+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293]"
                + "[+9024 -4512 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -39756]";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{569,msb}<1,-1|1,-3>(16,-8,A:32,1,-42m,(16,-8,B:32,1,-42m)*){A=0x30441ce3,B=0xff00ff}");
    }

    @Test(enabled = true)
    public void testAnalyze3_1() {
        System.out.println("analyze3_1");
        String args = "analyze --eliminate-vars --radix 16 --maxparameterwidth 32 --ire " +
                "[+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293]"
                + "[+9024 -4512 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -39756]";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{569,msb}<1,-1|1,-3>(16,-8,0x30441ce3:32,1,-42m,(16,-8,0xff00ff:32,1,-42m)*)");
    }

    @Test(enabled = true)
    public void testAnalyze4() {
        System.out.println("analyze4");
        String args = "-a 100 -g 10000 analyze --decoder pwm2 --radix 16 --maxparameterwidth 32 -r -- "
                + "+2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -11859";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{615,msb}<1,-1|2,-1>(4,-1,A:20,-11.981m)5{A=0x4b8f}");
    }

    @Test(enabled = true)
    public void testAnalyze4_1() {
        System.out.println("analyze4_1");
        String args = "-a 100 -g 10000 analyze --decoder pwm2 --radix 16 --maxparameterwidth 32 -r "
                + "+2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, null);
    }

    @Test(enabled = true)
    public void testAnalyze4_2() {
        System.out.println("analyze4_2");
        String args = "-a 100 -g 10000 analyze --decoder pwm2 --radix 16 --maxparameterwidth 32 --trailinggap 12000 -r "
                + "+2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -657 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157 -12780 +2340 -631 +579 -631 +579 -631 +579 -631 +579 -631 +579 -631 +1157 -631 +579 -657 +579 -631 +1157 -631 +579 -631 +1157 -631 +1157 -631 +1157 -631 +579 -631 +579 -657 +579 -631 +1157 -631 +1157 -631 +1157 -631 +1157";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{615,msb}<1,-1|2,-1>(4,-1,A:20,-12m)5{A=0x4b8f}");
    }

    @Test(enabled = true)
    public void testAnalyze5() {
        System.out.println("analyze5");
        String args = "analyze "
                + "+1740 -1839 +1576 -1576 +1576 -1576 +1576 -1609 +1642 -1839 +1871 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1707 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1576 -1576 +1576 -1740 +1642 -1871 +1576 -1576 +1576 -1609 +1642 -1871 +1707 -1871 +1609 -1609 +1576 -1576 +1576 -3251 +1740 -3185 +1576 -1576 +1576 -1609 +1642 -1839 +1707 -3185 +1609 -1576 +1576 -1576 +1576 -3284 +1576 -1740";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{1644,msb}<1,-1|1,-2>(A:63,B:27){A=0x800020a200080,B=0x5100312}");
    }

    @Test(enabled = true)
    public void testAnalyze6() {
        System.out.println("analyze6");
        String args = "-a 100.0 -r 0.1 analyze --maxparameterwidth 32 " + ACDATA;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{137,msb}<4,-2|4,-8>(23,-1588u,A:32,B:32,C:32,D:16,4,-10m){A=0xc4d36480,B=0x4c0b0,C=0x40000000,D=0x1e3}");
        args = "-a 100.0 -r 0.1 analyze --maxparameterwidth 1024 --decoder pwm2 " + ACDATA;
        result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{137,msb}<4,-2|4,-8>(23,-1588u,A:112,4,-10m){A=0xc4d364800004c0b04000000001e3}");
   }

    @Test(enabled = true)
    public void testAnalyze7() {
        System.out.println("analyze7");
        String args = "--absol 200 analyze -M 332 --trailinggap 10000 " + ACDATA1;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{539,msb}<1,-1|1,-3>(8,-8,A:88,1,-10m){A=0xf20d05fa01700000210252}");
   }

    @Test(enabled = true)
    public void testAnalyze8() {
        System.out.println("analyze8");
        String args = "--absol 200 analyze --decode covfefe -M 332 --trailinggap 10000 " + ACDATA1;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, null);
   }

    @Test(enabled = true)
    public void testAnalyze9() {
        System.out.println("analyze9");
        String args = "analyze --maxparameterwidth 63 " + BIPHASE;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{40.2k,398,msb}<1,-1|-1,1>(A:63,B:13,-19.456m)*{A=0x7ff0404040400004,B=0x80}");
    }

    @Test(enabled = true)
    public void testAnalyze10() {
        System.out.println("analyze10");
        String args = "analyze -f 12345 --maxparameterwidth 63 " + BIPHASE;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{12.3k,398,msb}<1,-1|-1,1>(A:63,B:13,-19.456m)*{A=0x7ff0404040400004,B=0x80}");
    }

    @Test(enabled = true)
    public void testDecodeRc5() {
        System.out.println("decodeRc5");
        String args = "decode --strict -p rc5 0000 0073 0000 000B 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0040 0040 0020 0CA8";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "RC5: {D=7,F=5}");
    }

    @Test(enabled = true)
    public void testDecodeRc5x() {
        System.out.println("decodeRc5x");
        String args = "decode --strict -p rc5x 0000 0073 0000 000E 0040 0040 0040 0020 0020 0040 0040 0040 0020 00A0 0040 0040 0020 0020 0040 0020 0020 0040 0040 0020 0020 0020 0020 0020 0020 0020 0020 0AC8";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "RC5x: {D=5,F=32,S=108,T=1}");
    }

    @Test(enabled = true)
    public void testDecodeRc5_1() {
        System.out.println("decodeRc5_1");
        String args = "decode -p rc5 0000 0073 000B 0000 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0040 0040 0020 0CA8";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "RC5: {D=7,F=5}, beg=0, end=21, reps=1");
    }

    @Test(enabled = true)
    public void testDecodeRc5_2_strict() {
        System.out.println("decodeRc5_2_strict");
        String args = "decode --strict -p rc5 0000 0073 000B 0000 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0040 0040 0020 0CA8";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "");
        args = "decode --strict -p rc5 0000 0073 0000 000B 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0040 0040 0040 0020 0CA8";
        result = IrpTransmogrifier.execute(args);
        assertEquals(result, "RC5: {D=7,F=5}");
    }

    @Test(enabled = true)
    public void testDecodeRepeatedNec1() {
        System.out.println("decodeRepeatedNec1");
        String args = "decode -p nec1 -r  +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}");
    }

    @Test(enabled = true)
    public void testDecodeNec1OneDitto() {
        System.out.println("testDecodeNec1OneDitto");
        String args = "decode -p nec1 +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}, beg=0, end=71, reps=1");
    }

    @Test(enabled = true)
    public void testDecodeNec1OneDittoNec1TwoDittoJunk() {
        System.out.println("testDecodeNec1OneDittoNec1TwoDittoJunk");
        String sequence = NEC1INTRO + " " + NEC1DITTO + " " + NEC1INTRO + " " + NEC1DITTO + " " + NEC1DITTO + "+1234 -54678";
        String args = "decode -p nec1 " + sequence;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}, beg=0, end=71, reps=1 {UNDECODED. length=78}");
        args = "decode -p nec1 --recursive " + sequence;
        result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}, beg=0, end=71, reps=1 {NEC1: {D=12,F=56,S=34}, beg=72, end=147, reps=2 {UNDECODED. length=2}}");
    }

    @Test(enabled = true)
    public void testDecodeRepeatedNecMissingTrailing() {
        System.out.println("decodeRepeatedNec1Trailing");
        String args = "decode  -r  +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, null);
    }

    @Test(enabled = true)
    public void testDecodeRecs80Junk() {
        System.out.println("testDecodeRecs80Junk");
        String recs80 = "+158 -7426 +158 -7426 +158 -7426 +158 -7426 +158 -4898 +158 -7426 +158 -7426 +158 -7426 +158 -4898 +158 -4898 +158 -4898 +158 -45000";
        String junk = " 1234 5678";
        String result = IrpTransmogrifier.execute("decode " + recs80);
        assertEquals(result, "RECS80: {D=6,F=56,T=1}, beg=0, end=23, reps=1");
        result = IrpTransmogrifier.execute("decode " + recs80 + junk);
        assertEquals(result, "RECS80: {D=6,F=56,T=1}, beg=0, end=23, reps=1 {UNDECODED. length=2}");
        result = IrpTransmogrifier.execute("decode --strict " + recs80);
        assertEquals(result, "");
        result = IrpTransmogrifier.execute("decode --strict [][" + recs80 + "]");
        assertEquals(result, "RECS80: {D=6,F=56,T=1}");
    }

    @Test(enabled = true)
    public void testDecodeRecs80Multiple() {
        System.out.println("testDecodeRecs80Multiple");
        String recs80Multiple = "+200 -7300 +200 -7350 +150 -4850 +200 -7350 +150 -4850 +200 -4800 +200 -4850 +150 -4850 +200 -4800 +200 -4850 +150 -7350 +200 -30100 +150 -7350 +200 -7350 +150 -4850 +200 -7300 +200 -4850 +150 -4850 +200 -4800 +200 -4850 +150 -4850 +150 -4850 +200 -7350 +150 -30100 +150 -7350 +200 -4800 +200 -4850 +150 -7350 +200 -4800 +200 -4850 +150 -4850 +200 -4800 +200 -4850 +150 -4850 +150 -7350 +200 -30100 +200 -7350 +150 -4850 +200 -4800 +200 -7350 +150 -4850 +200 -4800 +200 -4850 +150 -4850 +200 -4800 +200 -4850 +150 -7350 +200 -30100 +200 -7300 +200 -4850 +150 -4850 +200 -7350 +150 -4850 +150 -4850 +200 -4800 +200 -4850 +150 -4850 +200 -4800 +200 -7350 +150 -30100";
        String result = IrpTransmogrifier.execute("decode --keep-defaulted --recursive " + recs80Multiple);
        System.out.println(result);
        assertEquals(result, "RECS80: {D=2,F=1,T=1}, beg=0, end=47, reps=2 {RECS80: {D=2,F=1,T=0}, beg=48, end=119, reps=3}");
    }

    @Test(enabled = true)
    public void testDecodeAkaiMitsubishi() {
        System.out.println("testDecodeAkaiMitsubishi");
        String akaiMitsubishi = "0000 006C 0000 0011 000A 0047 000A 0047 000A 0047 000A 001E 000A 001E 000A 001E 000A 0047 000A 001E 000A 0047 000A 0047 000A 0047 000A 001E 000A 0047 000A 001E 000A 001E 000A 001E 000A 031D";
        String result = IrpTransmogrifier.execute("-f -1 decode " + akaiMitsubishi);
        System.out.println(result);
        assertEquals(result,"Mitsubishi: {D=71,F=23}, beg=0, end=33, reps=1");
    }

    @Test(enabled = true)
    public void testDecodeAkaiMitsubishiAll() {
        System.out.println("testDecodeAkaiMitsubishiAll");
        String akaiMitsubishi = "0000 006C 0000 0011 000A 0047 000A 0047 000A 0047 000A 001E 000A 001E 000A 001E 000A 0047 000A 001E 000A 0047 000A 0047 000A 0047 000A 001E 000A 0047 000A 001E 000A 001E 000A 001E 000A 031D";
        String result = IrpTransmogrifier.execute("-f -1 decode -a " + akaiMitsubishi);
        System.out.println(result);
        assertEquals(result,
                "Akai: {D=7,F=104}, beg=0, end=21, reps=1 {UNDECODED. length=12}" + IrCoreUtils.LINE_SEPARATOR
                + "  Mitsubishi: {D=71,F=23}, beg=0, end=33, reps=1");
    }

    @Test(enabled = true)
    public void testDecodeSony15() {
        System.out.println("testDecodeSony15");
        String sony15 = "0000 0068 0010 0000 0060 0018 0030 0018 0018 0018 0030 0018 0030 0018 0030 0018 0030 0018 0018 0018 0018 0018 0018 0018 0030 0018 0018 0018 0018 0018 0030 0018 0018 0018 0030 0100";
        String result = IrpTransmogrifier.execute("decode -p sony15 " + sony15);
        System.out.println(result);
        assertEquals(result, "Sony15: {D=164,F=61}, beg=0, end=31, reps=1");
    }

    @Test(enabled = true)
    public void testDecodeSony15NoLeadout() {
        System.out.println("testDecodeSony15NoLeadout");
        String sony15 = "0000 0068 0010 0000 0060 0018 0030 0018 0018 0018 0030 0018 0030 0018 0030 0018 0030 0018 0018 0018 0018 0018 0018 0018 0030 0018 0018 0018 0018 0018 0030 0018 0018 0018 0030 0018";
        String result = IrpTransmogrifier.execute("decode -p sony15 " + sony15);
        System.out.println(result);
        assertEquals(result, "");
    }

    @Test(enabled = true)
    public void testDecodeFrequency() {
        System.out.println("testDecodeFrequency");
        String amino = "0000 004A 001B 001C 0069 005A 002D 000F 000F 000F 001E 000F 000F 000F 000F 000F 000F 001E 001E 001E 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 001E 001E 000F 000F 001E 000F 000F 000F 000F 001E 001E 000F 000F 000F 000F 000F 000F 000F 000F 001E 001E 001E 001E 1157 0069 005A 002D 000F 000F 000F 001E 000F 000F 000F 000F 001E 000F 000F 001E 001E 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 000F 001E 001E 000F 000F 001E 000F 000F 000F 000F 001E 001E 000F 000F 000F 000F 000F 000F 000F 000F 001E 000F 000F 000F 000F 001E 1157";
        String result = IrpTransmogrifier.execute("decode " + amino);
        assertEquals(result, "Amino-56: {D=3,F=157}");
        result = IrpTransmogrifier.execute("decode -f 38k " + amino);
        assertEquals(result, "Amino: {D=3,F=157}");
    }

    @Test(enabled = true)
    public void testDecodeSim2AsRepeat() {
        System.out.println("testDecodeSim2AsRepeat");
        String sim2AsRepeat = "0000 006B 0000 0012 005D 006C 002F 002E 002F 002E 002F 006B 002F 006B 002F 002E 002F 006B 002F 006B 002F 006B 002F 006B 002F 002E 002F 006B 002F 002E 002F 002E 002F 002E 002F 002E 002F 006B 002E 0928";
        String result = IrpTransmogrifier.execute("decode --proto sim2 --keep-defaulted " + sim2AsRepeat);
        assertEquals(result, "SIM2: {D=236,F=133}, beg=0, end=35");
        result = IrpTransmogrifier.execute("decode --proto sim2 --strict " + sim2AsRepeat);
        assertEquals(result, "");
    }

    @Test(enabled = true)
    public void testDecodeRepeatedNecMissingTrailing_1() {
        System.out.println("decodeRepeatedNec1Trailing_1");
        String args = "decode  -r --trailinggap 100000 +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\\v+")[0], "NEC1: {D=12,F=56,S=34}");
    }

    @Test(enabled = true)
    public void testListIrp() {
        System.out.println("listIrp");
        String result = IrpTransmogrifier.execute("list --irp nec1");
        String expResult = "name=NEC1"
                + IrCoreUtils.LINE_SEPARATOR
                + "irp={38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255]";
        assertEquals(result, expResult);
    }

    @Test(enabled = true)
    public void testListIrpQuiet() {
        System.out.println("listIrp");
        String result = IrpTransmogrifier.execute("--quiet list --irp nec1");
        String expResult = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255]";
        assertEquals(result, expResult);
    }

    @Test(enabled = true)
    public void testListIrpClassify() {
        System.out.println("listIrpClassify");
        String result = IrpTransmogrifier.execute("--irp {40.244k,398,msb}<1,-1|-1,1>(1,A:31,F:1,F:8,D:23,D:8,0:4,-19.5m)*{A=0x7fe08080}[F:0..1,D:0..255] list -c");
        String expResult = "name=user_protocol"
                + IrCoreUtils.LINE_SEPARATOR
                + "classification=398\t40244\t\tBiphase\t\t\t\t\t\t\t\tSWD";
        assertEquals(result, expResult);
    }

    @Test
    public void testRenderSilly() {
        System.out.println("renderSilly");
        IrpTransmogrifier instance = new IrpTransmogrifier();
        ProgramExitStatus status = instance.run("-i silly render -p".split(" "));
        String expResult = "Parse error in \"silly\": `{' not found.";
        assertEquals(status.getMessage(), expResult);
    }

    @Test
    public void testRenderI() {
        System.out.println("renderI");
        String result = IrpTransmogrifier.execute("--irp {38.4k,564}<1,-1|1,-5>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255] render -n D=12,F=34 -p");
        String expResult = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 006C 0016 006C 0016 0016 0016 0016 0016 0016 0016 0016 0016 006C 0016 006C 0016 0016 0016 0016 0016 006C 0016 006C 0016 006C 0016 006C 0016 0016 0016 006C 0016 0016 0016 0016 0016 0016 0016 006C 0016 0016 0016 0016 0016 006C 0016 0016 0016 006C 0016 006C 0016 006C 0016 0016 0016 006C 0016 006C 0016 0342 015B 0057 0016 0E6C";
        assertEquals(result, expResult);
    }

    @Test
    public void testExpression() {
        System.out.println("Expression");
        String result = IrpTransmogrifier.execute("expression -n a=1,b=11 -- -1?a+b:9");
        assertEquals(result, "12");

    }

    @Test
    public void testExpression1() {
        System.out.println("Expression1");
        String result = IrpTransmogrifier.execute("expression -n D=244  D   + 1 +");
        assertEquals(result, null);
        result = IrpTransmogrifier.execute("expression -n D=244  D:-6:2");
        assertEquals(result, "47");
        result = IrpTransmogrifier.execute("expression -n D=244  (D:-6:2)");
        assertEquals(result, "47");
    }

    @Test
    public void testPrintParameters() {
        System.out.println("PrintParameters");
        String result = IrpTransmogrifier.execute("--seed 1 render --random --printparameters nec1");
        assertEquals(result, "{D=187,F=104,S=25}");
    }

    @Test
    public void testLirc() {
        System.out.println("lirc");
        String result = IrpTransmogrifier.execute("lirc src/test/lirc/RX-V995.lircd.conf");
        assertEquals(result, "yamaha-amp:	{38.0k,1,msb}<642u,-1600u|642u,-470u>(9067u,-4393u,pre_data:16,F:16,642u,-39597u,(9065u,-2139u,642u,-39597u)*){pre_data=0xa15e}[F:0x0..0xffff]");
    }

    @Test
    public void testListDecoders() {
        System.out.println("listDecoders");
        String result = IrpTransmogrifier.execute("analyze --decoder list");
        assertEquals(result, "Available decoders: TrivialDecoder, Pwm2Decoder, Pwm4Decoder, Pwm4AltDecoder,"
                + IrCoreUtils.LINE_SEPARATOR
                + "XmpDecoder, BiphaseDecoder, BiphaseInvertDecoder, BiphaseWithStartbitDecoder,"
                + IrCoreUtils.LINE_SEPARATOR
                + "BiphaseWithStartbitInvertDecoder, BiphaseWithDoubleToggleDecoder,"
                + IrCoreUtils.LINE_SEPARATOR
                + "SerialDecoder");
    }

    @Test(enabled = true)
    public void testDecodingFiles() throws IOException {
        System.out.println("testDecodingFiles");
        File testDir = new File("src/test/decoderfiles");
        if (!testDir.isDirectory())
            return;
        File outputDir = new File("target/testdecodeoutput");
        if (!outputDir.isDirectory()) {
            boolean status = outputDir.mkdirs();
            if (!status)
                throw new IOException("directory could not be created.");
        }
        File[] files = testDir.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            System.out.println(file.getCanonicalPath());
            File out = new File(outputDir, file.getName() + ".out");
            List<String> args = new ArrayList<>(8);
            args.add("-c");
            args.add(IRPPROTOCOLS_XML); // "The built-in" may not be reliable at this point
            args.add("-f");
            args.add("-1");
            args.add("-o");
            args.add(out.getCanonicalPath());
            args.add("decode");
            args.add("-r");
            args.add("--namedinput");
            args.add(file.getCanonicalPath());
            //System.out.println(String.join(" ", args));
            String result = IrpTransmogrifier.execute(args.toArray(new String[args.size()]));
            assertTrue(result != null);
            //System.out.println(result);
        }
    }
}
