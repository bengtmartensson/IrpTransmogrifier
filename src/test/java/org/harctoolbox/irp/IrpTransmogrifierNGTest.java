package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrpTransmogrifierNGTest {

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
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:32,6,-89m)*{A=0xc38c922}\tweight = 16");
        args = "-a 20 -r 0.04 analyze --radix 16 -t 1p 0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001B 0006 000A 0006 000A 0006 001B 0006 0015 0006 000A 0006 001B 0006 000A 0006 0015 0006 0010 0006 000A 0006 0015 0006 000A 0006 0015 0006 0C90";
        result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{36.0k,1p,msb}<6,-10|6,-16|6,-21|6,-27>(15,-10,A:16,B:16,6,-89m)*{A=0xc38,B=0xc922}\tweight = 19");
    }

    /**
     * Test of main method, of class IrpTransmogrifier.
     */
    @Test(enabled = true)
    public void testAnalyze2() {
        System.out.println("analyze2");
        String args = "analyze --radix 16 --chop 30000 --ire --maxparameterwidth 32 -- +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +573 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "{573,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m,(16,-4,1,-96m)*){A=0x30441ce3}\tweight = 16");
    }

    @Test(enabled = true)
    public void testAnalyze3() {
        System.out.println("analyze3");
        String args = "analyze --radix 16 --maxparameterwidth 32 -- " +
                "[+9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293]"
                + "[+9024 -4512 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -1692 +564 -39756]";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result, "Seq. #0: {569,msb}<1,-1|1,-3>(16,-8,A:32,1,-44m){A=0x30441ce3}\tweight = 11\n"
                + "Seq. #1: {569,msb}<1,-1|1,-3>(16,-8,A:32,1,-39.756m){A=0xff00ff}\tweight = 11");
    }

    @Test(enabled = true)
    public void testDecodeRepeatedNec1() {
        System.out.println("decodeRepeatedNec1");
        String args = "decode  -r  -- +9041 -4507 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -1694 +573 -573 +573 -573 +573 -573 +573 -1694 +573 -1694 +573 -44293 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193 +9041 -2267 +626 -96193";
        String result = IrpTransmogrifier.execute(args);
        assertEquals(result.split("\r?\n")[0], "NEC1: {D=12,F=56,S=34}");
    }

    @Test(enabled = true)
    public void testListIrp() {
        System.out.println("listIrp");
        String result = IrpTransmogrifier.execute("list --irp nec1");
        String expResult = "NEC1\t{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255]";
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
        result = IrpTransmogrifier.execute("expression -n D=244  D:-6:2"); // invalid expression
        assertEquals(result, null);
        result = IrpTransmogrifier.execute("expression -n D=244  (D:-6:2)");
        assertEquals(result, "47");
    }

    @Test
    public void testLirc() {
        System.out.println("lirc");
        String result = IrpTransmogrifier.execute("lirc src/test/resources/RX-V995.lircd.conf");
        assertEquals(result, "yamaha-amp:	{38.0k,1,msb}<642u,-1600u|642u,-470u>(9067u,-4393u,pre_data:16,F:16,642u,-39597u,(9065u,-2139u,642u,-39597u)*){pre_data=0xa15e}[F:0x0..0xffff]");
    }
}
