package org.harctoolbox.irp;

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
    private Protocol nec1;
    private Protocol rc5;

    public ProtocolNGTest() throws IrpSemanticException, IrpSyntaxException, InvalidRepeatException, ArithmeticException, IncompatibleArgumentException, UnassignedException {
        nec1 = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
        rc5 = new Protocol("{36k,msb,889}<1,-1|-1,1>((1:1,~F:1:6,T:1,D:5,F:6,^114m)+,T=1-T)[D:0..31,F:0..127,T@:0..1=0]");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
    public void testToIrSignal() {
        System.out.println("toIrSignal");
        NameEngine nameEngine;
        try {
            nameEngine = new NameEngine("{D=12,S=34,F=56}");
            IrSignal result = nec1.toIrSignal(nameEngine);
            IrSignal expected = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            assertTrue(result.approximatelyEquals(expected));
            nameEngine = new NameEngine("{D=12,F=56}");
            result = nec1.toIrSignal(nameEngine);
            expected = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C");
            assertTrue(result.approximatelyEquals(expected));
            nameEngine = new NameEngine("{D=12,F=56}");
            result = rc5.toIrSignal(nameEngine);
            expected = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            assertTrue(result.approximatelyEquals(expected));
            nameEngine = new NameEngine("{D=12,F=56}");
            result = rc5.toIrSignal(nameEngine);
            expected = Pronto.parse("0000 0073 0000 000B 0020 0020 0020 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
            assertTrue(result.approximatelyEquals(expected));
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
        assertEquals(result, "{36000.0k,889.0,msb}<1,-1|-1,1>((1:1,~F:1:6,T:1,D:5,F:6,^114m)+,T=(1-T))[D:0..31,F:0..127,T@:0..1=0]");
    }
}
