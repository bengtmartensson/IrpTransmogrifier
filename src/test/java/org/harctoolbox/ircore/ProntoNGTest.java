package org.harctoolbox.ircore;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ProntoNGTest {

    private static IrSignal nec1;
    private static final int[] necIntArray = new int[]{9041, 4507, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 1694, 573, 573, 573, 573, 573, 573, 573, 1694, 573, 1694, 573, 44293,
        +9041, 2267, 573, 96193};
    private static final String NEC1_D12_S34_F56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";


    public ProntoNGTest() throws InvalidArgumentException {
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        nec1 = new IrSignal(necIntArray, 68, 4, 38400d);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of formatInteger method, of class Pronto.
     */
    @Test
    public void testFormatInteger() {
        System.out.println("formatInteger");
        int n = 4097;
        String expResult = "1001";
        String result = Pronto.formatInteger(n);
        assertEquals(result, expResult);
    }

    /**
     * Test of frequencyCode method, of class Pronto.
     */
    @Test
    public void testFrequencyCode() {
        System.out.println("frequencyCode");
        double frequency = 38400;
        int expResult = 108;
        int result = Pronto.frequencyCode(frequency);
        assertEquals(result, expResult);
    }

    /**
     * Test of frequency method, of class Pronto.
     */
    @Test
    public void testFrequency() {
        System.out.println("frequency");
        int code = 0;
        double expResult = 43178d;
        double result;
        try {
            Pronto.frequency(code);
            fail();
        } catch (InvalidArgumentException ex) {
        }
        try {
            result = Pronto.frequency(0x60);
            assertEquals(result, expResult, 1d);
        } catch (InvalidArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of pulseTime method, of class Pronto.
     */
    @Test
    public void testPulseTime() {
        try {
            System.out.println("pulseTime");
            int code = 0x68;
            double expResult = 25e-6;
            double result = Pronto.pulseTime(code);
            assertEquals(result, expResult, 1e-6);
        } catch (InvalidArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of pulses method, of class Pronto.
     */
    @Test
    public void testPulses() {
        try {
            System.out.println("pulses");
            double time = 100e-6;
            double frequency = 40000;
            int expResult = 4;
            int result = Pronto.pulses(time, frequency);
            assertEquals(result, expResult, 0.1);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ProntoNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of pulses method, of class Pronto.
     */
    @Test
    public void testPulses0() {
        try {
            System.out.println("pulses, frequency = 0");
            double time = 1000e-6;
            double frequency = 0;
            int expResult = 415;
            int result = Pronto.pulses(time, frequency);
            assertEquals(result, expResult, 0.1);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ProntoNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    /**
     * Test of parse method, of class Pronto.
     */
    @Test
    public void testParse_String() {
        System.out.println("parse");
        try {
            IrSignal result = Pronto.parse(NEC1_D12_S34_F56);
            assertTrue(result.approximatelyEquals(nec1));
        } catch (Pronto.NonProntoFormatException | InvalidArgumentException ex) {
            Logger.getLogger(ProntoNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        // non-pronto
        try {
            Pronto.parse("123 456");
            fail();
        } catch (Pronto.NonProntoFormatException ex) {
        } catch (InvalidArgumentException ex) {
            fail();
        }

        // odd length
        try {
            String ccfString = "0000 006C 0022 0002 1111 2222 015B";
            Pronto.parse(ccfString);
            fail();
        } catch (Pronto.NonProntoFormatException ex) {
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Pronto Hex is invalid since it has an odd number (7) of durations.");
        }

        try {
            String ccfString = "0000 006C 0020 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
            //                              ^^
            Pronto.parse(ccfString);
            fail();
        } catch (Pronto.NonProntoFormatException ex) {
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Inconsistent length in Pronto Hex (claimed 34 pairs, was 36 pairs).");
        }

        try {
            String ccfString = "0000 006C 0022 0002 015G 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
            Pronto.parse(ccfString);
            fail();
        } catch (Pronto.NonProntoFormatException ex) {
            assertEquals(ex.getMessage(), "Position 4: \"015G\" is not a four digit hexadecimal string.");
        } catch (InvalidArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of toPrintString method, of class Pronto.
     */
    @Test
    public void testToPrintString_IrSignal() {
        System.out.println("toPrintString");
        String result = Pronto.toString(nec1);
        assertEquals(result.substring(0, 358), NEC1_D12_S34_F56.substring(0, 358));
    }
}
