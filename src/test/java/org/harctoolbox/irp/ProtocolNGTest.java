package org.harctoolbox.irp;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class ProtocolNGTest {
    private static final String irpDatabasePath = "src/main/config/IrpProtocols.xml";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final IrpDatabase irpDatabase;

    public ProtocolNGTest() throws IOException, SAXException {
        irpDatabase = new IrpDatabase(irpDatabasePath);
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
        } catch (IrpException | ArithmeticException ex) {
            fail();
        }
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class Protocol.
     */
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        try {
            new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,(F:8)+,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
            fail();
        } catch (UnsupportedRepeatException ex) {
            // success!
        } catch (IrpException | ArithmeticException ex) {
            fail();
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
            NamedProtocol nec1 = irpDatabase.getNamedProtocol("nec1");
            IrSignal result = nec1.toIrSignal(nameEngine);
            IrSignal expected = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            assertTrue(result.approximatelyEquals(expected));

            nameEngine = new NameEngine("{D=12,F=56}");
            result = nec1.toIrSignal(nameEngine);
            assertTrue(result.approximatelyEquals(Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C")));
        } catch (IrpException | InvalidArgumentException | ArithmeticException ex) {
            fail();
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
            NamedProtocol rc5 = irpDatabase.getNamedProtocol("rc5");
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
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalRc6() {
        System.out.println("toIrSignalRc6");
        try {
            IrSignal rc6D12F34 = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0020 0010 0BCD");
            NamedProtocol rc6 = irpDatabase.getNamedProtocol("rc6");
            IrSignal result = rc6.toIrSignal(new NameEngine("{D=12,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(rc6D12F34));
        } catch (InvalidArgumentException | IrpException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalNokia32() {
        System.out.println("toIrSignalNokia32");
        try {
            IrSignal nokia32D12S56F34T0X78 = Pronto.parse("0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001C 0006 000A 0006 000A 0006 001C 0006 0016 0006 000A 0006 0010 0006 000A 0006 001C 0006 0016 0006 000A 0006 0016 0006 000A 0006 0016 0006 0C86");
            NamedProtocol nokia32 = irpDatabase.getNamedProtocol("nokia32");
            IrSignal result = nokia32.toIrSignal(new NameEngine("{D=12,S=56,F=34,T=0,X=78}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(nokia32D12S56F34T0X78));
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeNokia32() {
        System.out.println("recognizeNokia32");
        try {
            IrSignal signal = Pronto.parse("0000 0073 0000 0012"
                    + " 000F 000A"
                    + " 0006 000A 0006 000A 0006 001B 0006 000A" // D
                    + " 0006 000A 0006 001B 0006 0015 0006 000A" // S
                    + " 0006 001B 0006 000A 0006 0015 0006 0010" // T,X
                    + " 0006 000A 0006 0015 0006 000A 0006 0015" // F
                    + " 0006 0C90");
            NameEngine nameEngine = new NameEngine("{D=12,S=56,F=34,T=1,X=73}");
            NamedProtocol nokia32 = irpDatabase.getNamedProtocol("nokia32");
            Map<String, Long> recognizeData = nokia32.recognize(signal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalAmino() {
        System.out.println("toIrSignalAmino");
        try {
            IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            NamedProtocol amino = irpDatabase.getNamedProtocol("amino");
            IrSignal result = amino.toIrSignal(new NameEngine("{D=12,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(aminoD12F34));
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeAmino() {
        System.out.println("recognizeAmino");
        try {
            IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            NameEngine nameEngine = new NameEngine("{D=12,F=34}");
            NamedProtocol amino = irpDatabase.getNamedProtocol("amino");
            Map<String, Long> recognizeData = amino.recognize(aminoD12F34);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeErroneousAmino() {
        System.out.println("recognizeErroneousAmino");
        try {
            IrSignal aminoD12F34Err = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
            System.out.print("Expect parse error from FiniteBitField: ");
            Protocol amino = irpDatabase.getNamedProtocol("amino");
            amino.recognize(aminoD12F34Err);
            fail();
        } catch (InvalidArgumentException | ArithmeticException | DomainViolationException | NameConflictException | UnassignedException | InvalidNameException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        } catch (IrpSignalParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testToIrSignalArctech() {
        System.out.println("toIrSignalArchtech");
        try {
            IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
            Protocol arctech = irpDatabase.getNamedProtocol("arctech");
            IrSignal result = arctech.toIrSignal(new NameEngine("{D=12,S=9,F=0}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(irSignal));
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test
    public void testToIrSignalXmp() {
        System.out.println("toIrSignalXmp");
        try {
            IrSignal xmpD12S56F34 = Pronto.parse("0000 006D 0012 0012 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 0022 0008 001D 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 004B 0008 0046 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF");
            NamedProtocol xmp = irpDatabase.getNamedProtocol("xmp");
            IrSignal result = xmp.toIrSignal(new NameEngine("{D=12,S=56,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(xmpD12S56F34));
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testToIrSignalDirectv() {
        System.out.println("toIrSignalDirectv");
        try {
            IrSignal directvD12F34 = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
            Protocol directv = irpDatabase.getNamedProtocol("directv");
            IrSignal result = directv.toIrSignal(new NameEngine("{D=12,F=34}"));
            System.out.println(result);
            assertTrue(result.approximatelyEquals(directvD12F34));
        } catch (IrpException | InvalidArgumentException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeDirecttv() {
        System.out.println("recognizeDirectv");
        try {
            IrSignal irSignal = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
            NameEngine nameEngine = new NameEngine("{D=12,F=34}");
            Protocol directv = irpDatabase.getNamedProtocol("directv");
            Map<String, Long> recognizeData = directv.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    /**
     * Test of getBitDirection method, of class Protocol.
     */
    @Test
    public void testGetBitDirection() {
        try {
            System.out.println("getBitDirection");
            Protocol rc5 = irpDatabase.getNamedProtocol("rc5");
            Protocol nec1 = irpDatabase.getNamedProtocol("nec1");
            assertEquals(rc5.getBitDirection(), BitDirection.msb);
            assertEquals(nec1.getBitDirection(), BitDirection.lsb);
        } catch (UnknownProtocolException | IrpSemanticException | InvalidNameException | UnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of getUnit method, of class Protocol.
     */
    @Test
    public void testGetUnit() {
        try {
            System.out.println("getUnit");
            Protocol rc5 = irpDatabase.getNamedProtocol("rc5");
            Protocol nec1 = irpDatabase.getNamedProtocol("nec1");
            assertEquals(rc5.getUnit(), 889f, 0.0);
            assertEquals(nec1.getUnit(), 564f, 0.0);
        } catch (UnknownProtocolException | IrpSemanticException | InvalidNameException | UnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of getDutyCycle method, of class Protocol.
     */
    @Test
    public void testGetDutyCycle() {
        System.out.println("getDutyCycle");
        Protocol samsung36;
        try {
            samsung36 = irpDatabase.getNamedProtocol("samsung36");
            IrCoreUtils.approximatelyEquals(samsung36.getDutyCycle(), 0.33);
            Protocol sharp = irpDatabase.getNamedProtocol("sharp");
            assertEquals(sharp.getDutyCycle(), null);
        } catch (UnknownProtocolException | IrpSemanticException | InvalidNameException | UnassignedException ex) {
            Logger.getLogger(ProtocolNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of toIrpString method, of class Protocol.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            NamedProtocol rc5 = irpDatabase.getNamedProtocol("rc5");
            String result = rc5.toIrpString();
            assertEquals(result, "{36.0k,889,msb}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=(1-T)){}[D:0..31,F:0..127,T@:0..1=0]");
        } catch (UnknownProtocolException | IrpSemanticException | InvalidNameException | UnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognizeNec1() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            NameEngine nameEngine = new NameEngine("{D=12,S=34,F=56}");
            Protocol nec1 = irpDatabase.getNamedProtocol("nec1");
            Map<String, Long> recognizeData = nec1.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    // Tests domain test
    public void testRecognizeTivo() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            System.out.print("Expect DomainViolationException: ");
            Protocol tivo = irpDatabase.getNamedProtocol("tivo");
            tivo.recognize(irSignal);
            fail();
        } catch (InvalidArgumentException | IrpSignalParseException | NameConflictException | UnassignedException | InvalidNameException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        } catch (DomainViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognizeRc5() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            NameEngine nameEngine = new NameEngine("{D=12,F=56}");
            Protocol rc5 = irpDatabase.getNamedProtocol("rc5");
            Map<String, Long> recognizeData = rc5.recognize(irSignal,false);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
            nameEngine = new NameEngine("{D=12,F=56,T=0}");
            recognizeData = rc5.recognize(irSignal,true);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     */
    @Test
    public void testRecognizeDenonK() {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 0070 0000 0032 0080 0040 0010 0010 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0ACD");
            NameEngine nameEngine = new NameEngine("{D=12,S=3,F=56}");
            NamedProtocol denonK = irpDatabase.getNamedProtocol("denon-k");
            Map<String, Long> recognizeData = denonK.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeAdNotam() {
        System.out.println("recognizeAdNotam");
        try {
            Protocol adnotam = new Protocol("{35.7k,895,msb}<1,-1|-1,1>(1,-2,1,D:6,F:6,^114m)*[D:0..63,F:0..63]");
            IrSignal irSignal = Pronto.parse("0000 0074 0000 000C 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0020 0020 0C67");
            NameEngine nameEngine = new NameEngine("{D=12,F=3}");
            Map<String, Long> recognizeData = adnotam.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpException | InvalidArgumentException | ArithmeticException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRecognizeRc6() {
        System.out.println("recognizeRc6");
        try {
            IrSignal irSignal = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0030 0030 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0010 0BCD");
            NameEngine nameEngine = new NameEngine("{D=31,F=30,T=1}");
            Protocol rc6 = irpDatabase.getNamedProtocol("rc6");
            Map<String, Long> recognizeData = rc6.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeArcTech() {
        System.out.println("recognizeArcTech");
        try {
            IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
            NameEngine nameEngine = new NameEngine("{D=12,S=5,F=0}");
            Protocol arctech = irpDatabase.getNamedProtocol("arctech");
            Map<String, Long> recognizeData = arctech.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeApple() {
        System.out.println("recognizeApple");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002"
                    + " 015B 00AD"
                    + " 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016"
                    + " 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041"
                    + " 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016"
                    + " 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016"
                    + " 0016 0622"
                    + " 015B 0057 0016 0E6C");
            NameEngine nameEngine = new NameEngine("{D=12,F=34,PairID=123}");
            Protocol apple = irpDatabase.getNamedProtocol("apple");
            Map<String, Long> recognizeData = apple.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (IrpSyntaxException | InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test
    public void testRecognizeAppleErr() {
        System.out.println("recognizeAppleErr");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002"
                    + " 015B 00AD"
                    + " 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016"
                    + " 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041"
    /* ---> */      + " 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016"
                    + " 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016"
                    + " 0016 0622"
                    + " 015B 0057 0016 0E6C");
            System.out.print("Expect NameConflictException: ");
            NamedProtocol apple = irpDatabase.getNamedProtocol("apple");
            apple.recognize(irSignal);
            fail();
        } catch (InvalidArgumentException | ArithmeticException | IrpSignalParseException | DomainViolationException | UnassignedException | InvalidNameException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        } catch (NameConflictException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test(enabled = true)
    public void testRecognizeAnthem() {
        try {
            System.out.println("recognizeAnthem");
            IrSignal irSignal = Pronto.parse("0000 006D 0000 0066 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 08FB");
            NameEngine nameEngine = new NameEngine("{D=12,F=34,S=56}");
            NamedProtocol anthem = irpDatabase.getNamedProtocol("anthem");
            Map<String, Long> recognizeData = anthem.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeRc6M32() {
        try {
            System.out.println("recognizeRc6M32");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 001E 0060 0020 0010 0020 0020 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0020 0010 0010 0020 0020 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 09DD");
            NameEngine nameEngine = new NameEngine("{D=11,T=1,M=3,OEM1=227,F=111,OEM2=215}");
            NamedProtocol rc6M56 = irpDatabase.getNamedProtocol("rc6-M-32");
            Map<String, Long> recognizeData = rc6M56.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeRc6M56() {
        try {
            System.out.println("recognizeRc6M56");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 0039"
                    + " 0060 0020 0010 0010"
                    + " 0010 0020 0020 0010 0020 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 126C");
            NameEngine nameEngine = new NameEngine("{M=5, C=806354200, T=1}");
            NamedProtocol rc6M56 = irpDatabase.getNamedProtocol("rc6-M-56");
            Map<String, Long> recognizeData = rc6M56.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeMce() {
        try {
            System.out.println("recognizeMce");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0020 0010 09CD");
            NameEngine nameEngine = new NameEngine("{D=12, S=56, F=34, T=1}");
            NamedProtocol mce = irpDatabase.getNamedProtocol("mce");
            Map<String, Long> recognizeData = mce.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeEntone() {
        try {
            System.out.println("recognizeEntone");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 0035 0060 0020 0010 0010 0010 0010 0010 0020 0010 0020 0030 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0020 0020 0020 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0020 0020 0020 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 126C");
            NameEngine nameEngine = new NameEngine("{F=42}");
            NamedProtocol entone = irpDatabase.getNamedProtocol("entone");
            Map<String, Long> recognizeData = entone.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeRc5x() {
        try {
            System.out.println("recognizeRc5x");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 000F 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 00C0 0040 0040 0040 0040 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0AA8");
            NameEngine nameEngine = new NameEngine("{D=28, S=106, F=15}");
            NamedProtocol rc5x = irpDatabase.getNamedProtocol("rc5x");
            Map<String, Long> recognizeData = rc5x.recognize(irSignal, false);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeRs200() {
        try {
            System.out.println("recognizeRs200");
            IrSignal irSignal = Pronto.parse("0000 0074 0000 001A 0032 0078 0015 0078 0015 0078 0032 0078 0032 0078 0015 0078 0015 0078 0015 0078 0015 0078 0032 0078 0032 0078 0015 0078 0032 0078 0032 0078 0015 0078 0032 0078 0015 0078 0032 0078 0015 0078 0032 0078 0032 0078 0032 0078 0015 0078 0015 0078 0032 0078 0015 0500");
            NameEngine nameEngine = new NameEngine("{D=3, F=2, H1=1, H2=2, H3=3, H4=4}");
            NamedProtocol rs200 = irpDatabase.getNamedProtocol("rs200");
            Map<String, Long> recognizeData = rs200.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeSolidtek16() {
        try {
            System.out.println("recognizeSolidtek16");
            IrSignal irSignal = Pronto.parse("0000 006D 002E 0000 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0012 0018 0012 0018 0012 0018 0012 0018 0024 0018 0012 1552 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0024 002F 0012 0018 0012 0018 0012 0018 0024 1552 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0024 002F 0012 0018 0012 0018 0012 0018 0024 1552");
            NameEngine nameEngine = new NameEngine("{D=12, F=23}");
            NamedProtocol solidtek16 = irpDatabase.getNamedProtocol("solidtek16");
            Map<String, Long> recognizeData = solidtek16.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeZaptor() {
        try {
            System.out.println("recognizeZaptor");
            IrSequence repeat = new IrSequence(new int[]{2640, 1980, 660, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 660, 660, 660, 660, 660, 330, 330, 330, 330, 660, 330, 330, 660, 660, 330, 330, 330, 330, 330, 330, 660, 660, 660, 660, 330, 330, 660, 660, 74330});
            IrSequence ending = new IrSequence(new int[]{2640, 1980, 660, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 330, 330, 660, 660, 660, 660, 660, 660, 660, 330, 330, 330, 330, 660, 330, 330, 660, 660, 330, 330, 330, 330, 330, 330, 660, 660, 660, 330, 330, 660, 660, 660, 74330});
            IrSignal irSignal = new IrSignal(repeat, repeat, ending, 36000d);
            NameEngine nameEngine = new NameEngine("{D=73, F=55, S=42, E=10}");
            NamedProtocol zaptor = irpDatabase.getNamedProtocol("zaptor-36");
            Map<String, Long> recognizeData = zaptor.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
            IrSignal silly = new IrSignal(repeat, repeat, repeat, 36000d);
            System.err.print("Expect IrpSignalParseException: ");
            zaptor.recognize(silly);
            fail();
        } catch (InvalidNameException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | OddSequenceLengthException | UnknownProtocolException ex) {
            fail();
        } catch (IrpSignalParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test(enabled = true)
    public void testRecognizeIodatan() {
        try {
            System.out.println("recognizeIodatan");
            IrSignal irSignal = Pronto.parse("0000 006D 0000 002A 014E 00A7 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0627");
            NameEngine nameEngine = new NameEngine("{D=12, F=23, S=34}");
            NamedProtocol iodatan = irpDatabase.getNamedProtocol("iodatan");
            Map<String, Long> recognizeData = iodatan.recognize(irSignal, false);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeVelodyne() {
        try {
            System.out.println("recognizeVelodyne");
            IrSignal irSignal = Pronto.parse("0000 006D 0009 0009 0008 0060 0008 0051 0008 0041 0008 006A 0008 0037 0008 001D 0008 0032 0008 0022 0008 0BBA 0008 0060 0008 0027 0008 0041 0008 006A 0008 0037 0008 0046 0008 0032 0008 0022 0008 0BBA");
            NameEngine nameEngine = new NameEngine("{D=5, F=65, S=215}");
            NamedProtocol velodyne = irpDatabase.getNamedProtocol("velodyne");
            Map<String, Long> recognizeData = velodyne.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeXmp() {
        try {
            System.out.println("recognizeXmp");
            IrSignal irSignal = Pronto.parse("0000 006D 0012 0012 0008 006A 0008 0056 0008 003C 0008 006A 0008 0065 0008 006A 0008 003C 0008 0065 0008 020C 0008 006A 0008 0051 0008 001D 0008 003C 0008 002C 0008 0046 0008 0046 0008 0065 0008 0BEF 0008 006A 0008 0056 0008 003C 0008 006A 0008 0065 0008 006A 0008 003C 0008 0065 0008 020C 0008 006A 0008 0027 0008 0046 0008 003C 0008 002C 0008 0046 0008 0046 0008 0065 0008 0BEF");
            NameEngine nameEngine = new NameEngine("{D=110, F=14478, S=246, OEM=239}");
            NamedProtocol xmp = irpDatabase.getNamedProtocol("xmp");
            Map<String, Long> recognizeData = xmp.recognize(irSignal, false, 500f, 50f, 0.02);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }

    @Test(enabled = true)
    public void testRecognizeXmp1() {
        try {
            System.out.println("recognizeXmp1");
            IrSignal irSignal = Pronto.parse("0000 006D 0012 0012 0008 006A 0008 001D 0008 003C 0008 006A 0008 0032 0008 0032 0008 003C 0008 0065 0008 020C 0008 006A 0008 0027 0008 001D 0008 003C 0008 004B 0008 001D 0008 001D 0008 001D 0008 0BEF 0008 006A 0008 001D 0008 003C 0008 006A 0008 0032 0008 0032 0008 003C 0008 0065 0008 020C 0008 006A 0008 0051 0008 0046 0008 003C 0008 004B 0008 001D 0008 001D 0008 001D 0008 0BEF");
            NameEngine nameEngine = new NameEngine("{D=110, F=144, S=246}");
            NamedProtocol xmp1 = irpDatabase.getNamedProtocol("xmp-1");
            Map<String, Long> recognizeData = xmp1.recognize(irSignal, false, 500f, 50f, 0.02);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (InvalidArgumentException | IrpSyntaxException | IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | IrpSemanticException | UnknownProtocolException ex) {
            fail();
        }
    }
}
