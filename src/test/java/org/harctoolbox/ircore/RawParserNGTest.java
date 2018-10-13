package org.harctoolbox.ircore;

import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class RawParserNGTest {

    private static final String listOfFour = "[-3 -5][3. +55][-1212 234] [+434343 777]";
    private static final String listOfFourErroneous = "[-3 -5][3. +55][-1212 234][+434343 777 333]";
    private static final String nec1captured = "f=38461 +9003 -4513 +566 -561 +564 -559 +564 -561 +564 -562 +564 -561 +564 -559 +566 -559 +564 -1687 +566 -1685 +566 -1687 +564 -1687 +564 -1687 +566 -1687 +564 -1687 +566 -1685 +566 -1686 +566 -1687 +566 -1685 +566 -1687 +564 -1688 +566 -1685 +566 -561 +564 -559 +564 -561 +564 -561 +564 -561 +564 -559 +566 -559 +564 -561 +564 -1687 +566 -1685 +566 -1688 +564 -38886 +9001 -2289 +538 -65535";
    private static final String nec1capturedOdd = "f=38461 +9003 -4513 +566 -561 +564 -559 +564 -561 +564 -562 +564 -561 +564 -559 +566 -559 +564 -1687 +566 -1685 +566 -1687 +564 -1687 +564 -1687 +566 -1687 +564 -1687 +566 -1685 +566 -1686 +566 -1687 +566 -1685 +566 -1687 +564 -1688 +566 -1685 +566 -561 +564 -559 +564 -561 +564 -561 +564 -561 +564 -559 +566 -559 +564 -561 +564 -1687 +566 -1685 +566 -1688 +564 -38886 +9001 -2289 +538";
    private static final String nec1 = "+9003 -4513 +566 -561 +564 -559 +564 -561 +564 -562 +564 -561 +564 -559 +566 -559 +564 -1687 +566 -1685 +566 -1687 +564 -1687 +564 -1687 +566 -1687 +564 -1687 +566 -1685 +566 -1686 +566 -1687 +566 -1685 +566 -1687 +564 -1688 +566 -1685 +566 -561 +564 -559 +564 -561 +564 -561 +564 -561 +564 -559 +566 -559 +564 -561 +564 -1687 +566 -1685 +566 -1688 +564 -38886 +9001 -2289 +538 -65535";
    private static final String nec1Odd = "+9003 -4513 +566 -561 +564 -559 +564 -561 +564 -562 +564 -561 +564 -559 +566 -559 +564 -1687 +566 -1685 +566 -1687 +564 -1687 +564 -1687 +566 -1687 +564 -1687 +566 -1685 +566 -1686 +566 -1687 +566 -1685 +566 -1687 +564 -1688 +566 -1685 +566 -561 +564 -559 +564 -561 +564 -561 +564 -561 +564 -559 +566 -559 +564 -561 +564 -1687 +566 -1685 +566 -1688 +564 -38886 +9001 -2289 +538";
    private static final String nec1Repeat = "+9024 -4512 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -1692 +564 -564 +564 -564 +564 -564 +564 -1692 +564 -1692 +564 -44268"
            + "+9024 -2256 +564 -96156";
    private static final String ortekmceBracketed = "[+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +480 -480 +960 -480 +480 -48480]"
            + "[+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -48480]"
            + "[+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -48480]";
    private static final String ortekmceMultiline = "+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +480 -480 +960 -480 +480 -48480\n"
            + "+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -48480\r\n"
            + "+1920 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -480 +480 -960 +960 -48480";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public RawParserNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


 /**
  * Test of toList method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
  */
    @Test
    public void testToList_double() throws OddSequenceLengthException {
        System.out.println("toList");
        double chop = 30000.0;
        RawParser instance = new RawParser(nec1Repeat);
        List<IrSequence> result = instance.toListChop(chop);
        assertEquals(result.get(1).getLength(), 4);
     }

    /**
     * Test of toList method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToList_0args() throws OddSequenceLengthException {
        System.out.println("toList");
        RawParser instance = new RawParser(listOfFour);
        //List expResult = null;
        List<IrSequence> result = instance.toList();

        instance = new RawParser(listOfFourErroneous);
        try {
            //List expResult = null;
            result = instance.toList();
            fail();
        } catch (OddSequenceLengthException ex) {
        }
    }

    /**
     * Test of toModulatedIrSequence method, of class RawParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToModulatedIrSequence_Double_double() throws InvalidArgumentException {
        System.out.println("toModulatedIrSequence");
        Double fallbackFrequency = null;
        double dummyGap = 3333.0;
        RawParser instance = new RawParser(nec1capturedOdd);
        ModulatedIrSequence result = instance.toModulatedIrSequence(fallbackFrequency, dummyGap);
        assertEquals(result.get(result.getLength()-1), dummyGap, 0.00001);
    }

    /**
     * Test of toModulatedIrSequence method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testToModulatedIrSequence_Double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toModulatedIrSequence");
        Double fallbackFrequency = null;
        RawParser instance = new RawParser(nec1captured);
        //ModulatedIrSequence expResult = null;
        ModulatedIrSequence result = instance.toModulatedIrSequence(fallbackFrequency);
        assertEquals(result.getFrequency(), 38461, 0.001);
        assertEquals(result.getLength(), 72);

        instance = new RawParser(nec1capturedOdd);
        try {
            //ModulatedIrSequence expResult = null;
            result = instance.toModulatedIrSequence(fallbackFrequency);
            fail();
        } catch (OddSequenceLengthException ex) {
        }
    }

//    /**
//     * Test of getSource method, of class RawParser.
//     */
//    @Test
//    public void testGetSource() {
//        System.out.println("getSource");
//        RawParser instance = null;
//        String expResult = "";
//        String result = instance.getSource();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toIrSequence method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testToIrSequence_0args() throws OddSequenceLengthException {
        System.out.println("toIrSequence");
        RawParser instance = new RawParser(nec1);
        IrSequence result = instance.toIrSequence();
        System.out.println(result.toString(true, " "));
        assertEquals(result.toString(true, " "), nec1);

        instance = new RawParser(nec1Odd);
        try {
            result = instance.toIrSequence();
            fail();
        } catch (OddSequenceLengthException ex) {
        }
    }

    /**
     * Test of toIrSequence method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSequence_double() throws OddSequenceLengthException {
        System.out.println("toIrSequence");
        double dummyGap = 1234.0;
        RawParser instance = new RawParser(nec1Odd);
        IrSequence result = instance.toIrSequence(dummyGap);
    }

    /**
     * Test of toIrSignalAsMultiLine method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignalAsMultiLine() throws OddSequenceLengthException {
        System.out.println("toIrSignalAsMultiLine");
        Double frequency = null;
        RawParser instance = new RawParser(ortekmceMultiline);
        @SuppressWarnings("UnusedAssignment")
        IrSignal result = instance.toIrSignalAsMultiLine(frequency);

        instance = new RawParser(ortekmceBracketed);
        result = instance.toIrSignalAsMultiLine(frequency);
        assertNull(result);
    }

    /**
     * Test of toIrSignalAsBracketedString method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignalAsBracketedString() throws OddSequenceLengthException {
        System.out.println("toIrSignalAsBracketedString");
        Double frequency = null;
        RawParser instance = new RawParser(ortekmceBracketed);
        @SuppressWarnings("UnusedAssignment")
        IrSignal result = instance.toIrSignalAsBracketedString(frequency);
        instance = new RawParser(ortekmceMultiline);
        result = instance.toIrSignalAsBracketedString(frequency);
        assertNull(result);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignal_0args() throws InvalidArgumentException {
        System.out.println("toIrSignal");
        RawParser instance = new RawParser(ortekmceBracketed);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);
        instance = new RawParser(ortekmceMultiline);
        result = instance.toIrSignal();
        assertNotNull(result);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignal_Double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignal");
        Double frequency = null;
        RawParser instance = new RawParser(ortekmceBracketed);
        IrSignal result = instance.toIrSignal(frequency);
        assertNull(result.getFrequency());
        frequency = 12345.0;
        result = instance.toIrSignal(frequency);
        assertEquals(result.getFrequency(), frequency, 00001);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignal_Double_double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignal");
        Double fallbackFrequency = null;
        double threshold = 30000.0;
        RawParser instance = new RawParser(nec1Repeat);
        IrSignal result = instance.toIrSignal(fallbackFrequency, threshold);
        assertNull(result.getFrequency());
        fallbackFrequency = 12345.0;
        result = instance.toIrSignal(fallbackFrequency, threshold);
        assertEquals(result.getFrequency(), fallbackFrequency, 0.000001);
    }
}
