package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final Protocol nec1;
    private final Protocol rc5;
    private final Protocol rc6;
    private final Protocol nokia32;
    private final Protocol xmp;
    private final Protocol amino;
    private final Protocol denonK;
    private final Protocol arctechsimplified;
    private final Protocol arctech;

    public ProtocolNGTest() throws IrpSemanticException, IrpSyntaxException, InvalidRepeatException, ArithmeticException, IncompatibleArgumentException, UnassignedException {
        nec1 = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
        rc5 = new Protocol("{36k,msb,889}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=1-T)[D:0..31,F:0..127,T@:0..1=0]");
        rc6 = new Protocol("{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,0:3,<-2,2|2,-2>(T:1),D:8,F:8,^107m)*,T=1-T) [D:0..255,F:0..255,T@:0..1=0]");
        nokia32 = new Protocol("{36k,msb}<164,-276|164,-445|164,-614|164,-783>(412,-276,D:8,S:8,T:1,X:7,F:8,164,^100m)* [D:0..255,S:0..255,F:0..255,T:0..1,X:0..127]");
        xmp = new Protocol("{38k,136,msb}<210u,-760u> ( <0:1|0:1,-1|0:1,-2|0:1,-3|0:1,-4|0:1,-5|0:1,-6|0:1,-7|0:1,-8|0:1,-9|0:1,-10|0:1,-11|0:1,-12|0:1,-13|0:1,-14|0:1,-15>   (T=0,      (S:4:4,C1:4,S:4,15:4,OEM:8,D:8,210u,-13.8m,S:4:4,C2:4,T:4,S:4,FF:16,210u,-80.4m,T=8)+   ) ) { C1=-(S+S::4+15+OEM+OEM::4+D+D::4),   C2=-(S+S::4+T+FF+FF::4+FF::8+FF::12) } {FF=F}[F:0..65535,D:0..255,S:0..255,OEM:0..255=68]");
        amino = new Protocol("{37.3k,268,msb}<-1,1|1,-1>(T=1,(7,-6,3,D:4,1:1,T:1,1:2,0:8,F:8,15:4,C:4,-79m,T=0)+){C =(D:4+4*T+9+F:4+F:4:4+15)&15} [D:0..15,F:0..255]");
        denonK = new Protocol("{37k,432}<1,-1|1,-3>(8,-4,84:8,50:8,0:4,D:4,S:4,F:12,((D*16)^S^(F*16)^(F:8:4)):8,1,-173)* [D:0..15,S:0..15,F:0..4095]");
        arctechsimplified = new Protocol("{0k,388}<1,-3|3,-1>(<0:2|2:2>(D:4,S:4),40:7,F:1,0:1,-10.2m)*[D:0..15,S:0..15,F:0..1]");
        arctech = new Protocol("{0k,388}<1,-3|3,-1> (<0:2|2:2>((D-1):4,(S-1):4),40:7,F:1,0:1,-10.2m)*[D:1..16,S:1..16,F:0..1]");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getFrequency method, of class Protocol.
     */
    @Test
    public void testGetFrequency() {
        try {
            System.out.println("getFrequency");
            Protocol instance = new Protocol("{56.0k,268,msb}<-1,1|1,-1>(T=1,(7,-6,3,D:4,1:1,T:1,1:2,0:8,F:8,15:4,C:4,-79m,T=0)+){C =(D:4+4*T+9+F:4+F:4:4+15)&15} [D:0..15,F:0..255]");
            double expResult = 56000f;
            double result = instance.getFrequency();
            assertEquals(result, expResult, 0.0);
        } catch (IrpSyntaxException | IrpSemanticException | ArithmeticException | IncompatibleArgumentException | InvalidRepeatException | UnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class Protocol.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        try {
            new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,(F:8)+,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
            fail();
        } catch (IrpSemanticException | IrpSyntaxException | ArithmeticException | IncompatibleArgumentException | UnassignedException ex) {
            fail();
        } catch (InvalidRepeatException ex) {
            // success!
        }
    }

    /**
     * Test of toIrSignal method, of class Protocol.
     */
    @Test
    public void testToIrSignalNec1() {
        try {
            System.out.println("toIrSignalNec1");
            NameEngine nameEngine;
            nameEngine = new NameEngine("{D=12,S=34,F=56}");
            IrSignal result = nec1.toIrSignal(nameEngine);
            IrSignal expected = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            assertTrue(result.approximatelyEquals(expected));

            nameEngine = new NameEngine("{D=12,F=56}");
            result = nec1.toIrSignal(nameEngine);
            assertTrue(result.approximatelyEquals(Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C")));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testToIrSignalRc5() {
        System.out.println("toIrSignalRc5");
        try {
            // Testing the memory varables/toggles
            IrSignal rc5D12F56T0 = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            IrSignal rc5D12F56T1 = Pronto.parse("0000 0073 0000 000B 0020 0020 0020 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            // New protocol, no assignment to toggle
            NameEngine nameEngine = new NameEngine("{D=12,F=56}");
            IrSignal result = rc5.toIrSignal(nameEngine);
            assertEquals(nameEngine.get("T").toNumber(), 1L);
            assertEquals(rc5.getMemoryVariable("T"), 1L);
            assertTrue(result.approximatelyEquals(rc5D12F56T0));

            result = rc5.toIrSignal(nameEngine);
            assertEquals(nameEngine.get("T").toNumber(), 0L);
            assertEquals(rc5.getMemoryVariable("T"), 0L);
            assertTrue(result.approximatelyEquals(rc5D12F56T1));

            nameEngine = new NameEngine("{D=12,F=56,T=1}");
            result = rc5.toIrSignal(nameEngine);
            assertTrue(result.approximatelyEquals(rc5D12F56T1));
            assertEquals(nameEngine.get("T").toNumber(), 0L);
            assertEquals(rc5.getMemoryVariable("T"), 0L);

            nameEngine = new NameEngine("{D=12,F=56, T=0}");
            result = rc5.toIrSignal(nameEngine);
            assertEquals(nameEngine.get("T").toNumber(), 1L);
            assertEquals(rc5.getMemoryVariable("T"), 1L);
            assertTrue(result.approximatelyEquals(rc5D12F56T0));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalRc6() {
        System.out.println("toIrSignalRc6");
        try {
            IrSignal rc6D12F34 = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0020 0010 0BCD");
            IrSignal result = rc6.toIrSignal(new NameEngine("{D=12,F=34}"));
            assertTrue(result.approximatelyEquals(rc6D12F34));
        } catch (IncompatibleArgumentException | IrpSyntaxException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalNokia32() {
        System.out.println("toIrSignalNokia32");
        try {
            IrSignal nokia32D12S56F34T0X78 = Pronto.parse("0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001C 0006 000A 0006 000A 0006 001C 0006 0016 0006 000A 0006 0010 0006 000A 0006 001C 0006 0016 0006 000A 0006 0016 0006 000A 0006 0016 0006 0C86");
            IrSignal result = nokia32.toIrSignal(new NameEngine("{D=12,S=56,F=34,T=0,X=78}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(nokia32D12S56F34T0X78));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalAmino() {
        System.out.println("toIrSignalAmino");
        try {
            IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            IrSignal result = amino.toIrSignal(new NameEngine("{D=12,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(aminoD12F34));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeAmino() {
        System.out.println("recognizeAmino");
        try {
            IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            NameEngine nameEngine = new NameEngine("{D=12,F=34}");
            NameEngine recognizeData = amino.recognize(aminoD12F34);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException | ArithmeticException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeErroneousAmino() {
        System.out.println("recognizeErroneousAmino");
        try {
            IrSignal aminoD12F34Err = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            NameEngine recognizeData = amino.recognize(aminoD12F34Err);
            assertTrue(recognizeData == null);
        } catch (IncompatibleArgumentException | ArithmeticException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalArctech() {
        System.out.println("toIrSignalArchtech");
        try {
            Protocol arctech = new Protocol("{0k,388}<1,-3|3,-1> (<0:2|2:2>((D-1):4,(S-1):4),40:7,F:1,0:1,-10.2m)+ [D:1..16,S:1..16,F:0..1]");
            IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
            IrSignal result = arctech.toIrSignal(new NameEngine("{D=12,S=9,F=0}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(irSignal));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException | InvalidRepeatException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalXmp() {
        System.out.println("toIrSignalXmp");
        try {
            IrSignal xmpD12S56F34 = Pronto.parse("0000 006D 0012 0012 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 0022 0008 001D 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 004B 0008 0046 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF");
            IrSignal result = xmp.toIrSignal(new NameEngine("{D=12,S=56,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(xmpD12S56F34));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | UnassignedException | DomainViolationException ex) {
            fail();
        }
    }

    /**
     * Test of getBitDirection method, of class Protocol.
     */
    @Test
    public void testGetBitDirection() {
        System.out.println("getBitDirection");
        assertEquals(rc5.getBitDirection(), BitDirection.msb);
        assertEquals(nec1.getBitDirection(), BitDirection.lsb);
    }

    /**
     * Test of getUnit method, of class Protocol.
     */
    @Test
    public void testGetUnit() {
        System.out.println("getUnit");
        assertEquals(rc5.getUnit(), 889f, 0.0);
        assertEquals(nec1.getUnit(), 564f, 0.0);
    }

    /**
     * Test of getDutyCycle method, of class Protocol.
     */
//    @Test
//    public void testGetDutyCycle() {
//        System.out.println("getDutyCycle");
//        Protocol instance = new Protocol();
//        double expResult = 0.0;
//        double result = instance.getDutyCycle();
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toIrpString method, of class Protocol.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = rc5.toIrpString();
        assertEquals(result, "{36.0k,889,msb}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=(1-T)){}[D:0..31,F:0..127,T@:0..1=0]");
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognize_IrSignal_nec1() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            NameEngine nameEngine = new NameEngine("{D=12,S=34,F=56}");
            NameEngine recognizeData = nec1.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognize_IrSignal_rc5() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            NameEngine nameEngine = new NameEngine("{D=12,F=56,T=0}");
            NameEngine recognizeData = rc5.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognize_IrSignal_denon_k() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 0070 0000 0032 0080 0040 0010 0010 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0ACD");
            NameEngine nameEngine = new NameEngine("{D=12,S=3,F=56}");
            NameEngine recognizeData = denonK.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testRecognizeAdNotam() {
        System.out.println("recognizeAdNotam");
        try {
            Protocol adnotam = new Protocol("{35.7k,895,msb}<1,-1|-1,1>(1,-2,1,D:6,F:6,^114m)*[D:0..63,F:0..63]");
            IrSignal irSignal = Pronto.parse("0000 0074 0000 000C 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0020 0020 0C67");
            NameEngine nameEngine = new NameEngine("{D=12,F=3}");
            NameEngine recognizeData = adnotam.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException | IrpSemanticException | ArithmeticException | InvalidRepeatException | UnassignedException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @Test
    public void testRecognizeRc6() {
        System.out.println("recognizeRc6");
        try {
            IrSignal irSignal = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0030 0030 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0010 0BCD");
            NameEngine nameEngine = new NameEngine("{D=31,F=30,T=1}");
            NameEngine recognizeData = rc6.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException | ArithmeticException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @Test
    public void testRecognizeArcTechSimplified() {
        System.out.println("recognizeArcTechSimplified");
        try {
            IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
            NameEngine nameEngine = new NameEngine("{D=11,S=4,F=0}");
            NameEngine recognizeData = arctechsimplified.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException | ArithmeticException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @Test
    public void testRecognizeArcTech() {
        System.out.println("recognizeArcTech");
        try {
            IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
            NameEngine nameEngine = new NameEngine("{D=12,S=5,F=0}");
            NameEngine recognizeData = arctech.recognize(irSignal);
            assertTrue(nameEngine.numbericallyEquals(recognizeData));
        } catch (IrpSyntaxException | IncompatibleArgumentException | ArithmeticException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
