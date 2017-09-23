package org.harctoolbox.irp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        String args = "-a 20 -r 0.04 analyze --radix 16 -M 32 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:32,6,-89m)*{A=0xc38c922}");
        args = "-a 20 -r 0.04 analyze --radix 16 -M 16 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:16,B:16,6,-89m)*{A=0xc38,B=0xc922}");
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     */
    @Test(enabled = true)
    public void testAnalyze2() {
        System.out.println("analyze2");
        String args = "analyze --radix 16 --chop 30000 --ire --maxparameterwidth 32 +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +573 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{573,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m,(16,-4,1,-96m)*){A=0x30441ce3}");
    }

    @Test(enabled = true)
    public void testAnalyze3() {
        System.out.println("analyze3");
        String args = "analyze --radix 16 --maxparameterwidth 32 -- " +
                "[+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293]"
                + "[+9024 -4512 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -39756]";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "#0\t{569,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m){A=0x30441ce3}\n"
                + "#1\t{569,msb}<1,-1|1,-3>(16,-8,A:32,1,-39.756m){A=0xff00ff}");
    }

    @Test(enabled = true)
    public void testAnalyze3_1() {
        System.out.println("analyze3_1");
        String args = "analyze --eliminate-vars --radix 16 --maxparameterwidth 32 -- " +
                "[+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293]"
                + "[+9024 -4512 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -39756]";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "#0\t{569,msb}<1,-1|1,-3>(16,-8,0x30441ce3:32,1,-44m)\n"
                + "#1\t{569,msb}<1,-1|1,-3>(16,-8,0xff00ff:32,1,-39.756m)");
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
        String args = "ana --dec pwm2  -- "
                + "+1740 -1839 +1576 -1576 +1576 -1576 +1576 -1609 +1642 -1839 +1871 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1707 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1740 +1609 -1576 +1609 -1707 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -3185 +1609 -1576 +1609 -1740 +1609 -1576 +1674 -1871 +1609 -1609 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -3185 +1609 -1576 +1674 -1871 +1609 -1576 +1674 -3185 +1609 -1576 +1576 -1576 +1576 -1740 +1642 -1871 +1576 -1576 +1576 -1609 +1642 -1871 +1707 -1871 +1609 -1609 +1576 -1576 +1576 -3251 +1740 -3185 +1576 -1576 +1576 -1609 +1642 -1839 +1707 -3185 +1609 -1576 +1576 -1576 +1576 -3284 +1576 -1740";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "");
    }

    @Test(enabled = true)
    public void testAnalyze6() {
        System.out.println("analyze6");
        String args = "analyze --maxparameterwidth 32 " + ACDATA;
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{137,msb}<4,-2|4,-8>(23,-1588u,A:32,B:32,C:32,D:16,4,-10m){A=0xc4d36480,B=0x4c0b0,C=0x40000000,D=0x1e3}");
        args = "analyze --maxparameterwidth 1024 --decoder pwm2 " + ACDATA;
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
    public void testDecodeRepeatedNec1() {
        System.out.println("decodeRepeatedNec1");
        String args = "decode  -r  -- +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}");
    }

    @Test(enabled = true)
    public void testDecodeRepeatedNecMissingTrailing() {
        System.out.println("decodeRepeatedNec1Trailing");
        String args = "decode  -r  +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, null);
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
        String expResult = "NEC1" + IrpTransmogrifier.SEPARATOR + "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255]";
        assertEquals(result, expResult);
    }

    @Test
    public void testRenderSilly() {
        System.out.println("renderSilly");
        IrpTransmogrifier instance = new IrpTransmogrifier();
        IrpTransmogrifier.ProgramExitStatus status = instance.run("render -p -i silly");
        String expResult = "Parse error in \"silly\"";
        assertEquals(status.getMessage(), expResult);
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
    public void testLirc() {
        System.out.println("lirc");
        String result = IrpTransmogrifier.execute("lirc src/test/resources/RX-V995.lircd.conf");
        assertEquals(result, "yamaha-amp:	{38.0k,1,msb}<642u,-1600u|642u,-470u>(9067u,-4393u,pre_data:16,F:16,642u,-39597u,(9065u,-2139u,642u,-39597u)*){pre_data=0xa15e}[F:0x0..0xffff]");
    }

    @Test
    public void testListDecoders() {
        System.out.println("listDecoders");
        String result = IrpTransmogrifier.execute("analyze --decoder list");
        assertEquals(result, "Available decoders: TrivialDecoder, Pwm2Decoder, Pwm4Decoder, Pwm4AltDecoder,\n"
                + "XmpDecoder, BiphaseDecoder, BiphaseWithStartbitDecoder, BiphaseWithDoubleToggleDecoder,\n"
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
