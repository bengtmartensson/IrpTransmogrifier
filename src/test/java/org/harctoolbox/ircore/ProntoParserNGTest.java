package org.harctoolbox.ircore;

import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ProntoParserNGTest {
    private static final String NEC1_12_34_56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String NEC1_12_34_56_JUNK = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C 1234 5678";
    private static final String NEC1_12_34_56_WRONG_COUNT = "0000 006C 0022 0003 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String NEC1_12_34_56_SEMANITC = "0000 006C 0022 0002 15B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String NEC1_12_34_56_SILLY_NUMBER = "0000 006C 0022 0002 Z15B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private static final String NEC1_RAW = "+9024 -4512 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -44268";

    public ProntoParserNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrSignalAsPronto method, of class ProntoRawParser.
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignalAsPronto() throws Pronto.NonProntoFormatException, InvalidArgumentException {
        System.out.println("toIrSignalAsPronto");
        ProntoParser instance = new ProntoParser(NEC1_12_34_56);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);
    }

    /**
     * Test of toIrSignal method, of class ProntoRawParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testToIrSignal() throws InvalidArgumentException {
        System.out.println("toIrSignal");
        ProntoParser instance = new ProntoParser(NEC1_12_34_56);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);

        double fallbackFrequency = 12345.0;
        result = instance.toIrSignal(fallbackFrequency);
        assertNotNull(result);

        instance = new ProntoParser(NEC1_12_34_56_WRONG_COUNT);
        try {
            result = instance.toIrSignal(fallbackFrequency);
            fail();
        } catch (InvalidArgumentException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        instance = new ProntoParser(NEC1_RAW);
        result = instance.toIrSignal(fallbackFrequency);
        assertNull(result);
        instance = new ProntoParser(NEC1_12_34_56_SEMANITC);
        result = instance.toIrSignal(fallbackFrequency);
        assertNull(result);
    }

    /**
     * Test of parse method, of class ProntoParser.
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testParse() {
        System.out.println("parse");

        IrSignal result = null;
        try {
            result = ProntoParser.parse(NEC1_12_34_56);
            assertNotNull(result);
        } catch (InvalidArgumentException ex) {
            fail();
        }

        try {
            result = ProntoParser.parse(NEC1_RAW);
            assertNull(result);
        } catch (InvalidArgumentException ex) {
            fail();
        }

        try {
            result = ProntoParser.parse("0000 006c 0000 0000");
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Pronto Hex is invalid since it is just 4 < 6 numbers long.");
        }

        try {
            ProntoParser.parse("0123 006c 0002 0000 1111 1111 1111 1111");
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Pronto Hex type 0x0123 not supported.");
        }

        try {
            ProntoParser.parse("0123 006c 0002 0000 1111 1111 1111 1111 1111");
            fail();
        } catch (OddSequenceLengthException ex) {
            assertEquals(ex.getMessage(), "Pronto Hex is invalid since it has an odd number (9) of durations.");
        } catch (InvalidArgumentException ex) {
            fail();
        }

        try {
            result = ProntoParser.parse("0123 006c 0002 000 1111 1111 1111 1111");
            assertNull(result);
        } catch (InvalidArgumentException ex) {
            fail();
        }

        try {
            ProntoParser.parse(NEC1_12_34_56_WRONG_COUNT);
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Inconsistent length in Pronto Hex (claimed 37 pairs, was 36 pairs).");
        }

        try {
            result = ProntoParser.parse(NEC1_12_34_56_JUNK);
            assertEquals(result.getRepeatLength(), 4);
            fail();
        } catch (InvalidArgumentException ex) {
            assertEquals(ex.getMessage(), "Inconsistent length in Pronto Hex (claimed 36 pairs, was 37 pairs).");
        }

        try {
            result = ProntoParser.parse(NEC1_12_34_56_SILLY_NUMBER);
            assertNull(result);
        } catch (InvalidArgumentException ex) {
            fail();
        }
    }
}
