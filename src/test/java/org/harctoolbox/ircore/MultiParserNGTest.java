package org.harctoolbox.ircore;

import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MultiParserNGTest {


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

    public MultiParserNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of toIrSignal method, of class MultiParser.
//     */
//    @Test
//    public void testToIrSignal() throws Exception {
//        System.out.println("toIrSignal");
//        Double fallbackFrequency = null;
//        Double dummyGap = null;
//        MultiParser instance = null;
//        IrSignal expResult = null;
//        IrSignal result = instance.toIrSignal(fallbackFrequency, dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of toIrSequence method, of class MultiParser.
//     */
//    @Test
//    public void testToIrSequence() throws Exception {
//        System.out.println("toIrSequence");
//        Double dummyGap = null;
//        MultiParser instance = null;
//        IrSequence expResult = null;
//        IrSequence result = instance.toIrSequence(dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of toModulatedIrSequence method, of class MultiParser.
//     */
//    @Test
//    public void testToModulatedIrSequence() throws Exception {
//        System.out.println("toModulatedIrSequence");
//        Double fallbackFrequency = null;
//        Double dummyGap = null;
//        MultiParser instance = null;
//        ModulatedIrSequence expResult = null;
//        ModulatedIrSequence result = instance.toModulatedIrSequence(fallbackFrequency, dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

 /**
  * Test of toList method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
  */
    @Test
    public void testToList_double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toList");
        double chop = 30000.0;
        MultiParser instance = MultiParser.newIrCoreParser(nec1Repeat);
        List<IrSequence> result = instance.toListChop(chop);
        assertEquals(result.get(1).getLength(), 4);
     }

    /**
     * Test of toList method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testToList_0args() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toList");
        MultiParser instance = MultiParser.newIrCoreParser(listOfFour);
        //List expResult = null;
        List<IrSequence> result = instance.toList();
        assertEquals(result.size(), 4);

        instance = MultiParser.newIrCoreParser(listOfFourErroneous);
        try {
            instance.toList();
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
        double dummyGap = 3333.0;
        MultiParser instance = MultiParser.newIrCoreParser(nec1capturedOdd);
        ModulatedIrSequence result = instance.toModulatedIrSequence(null, dummyGap);
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
        MultiParser instance = MultiParser.newIrCoreParser(nec1captured);
        //ModulatedIrSequence expResult = null;
        ModulatedIrSequence result = instance.toModulatedIrSequence(null);
        assertEquals(result.getFrequency(), 38461, 0.001);
        assertEquals(result.getLength(), 72);

        instance = MultiParser.newIrCoreParser(nec1capturedOdd);
        try {
            instance.toModulatedIrSequence(null);
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
//        MultiParser instance = null;
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
    @Test   /**
//     * Test of toIrSignal method, of class MultiParser.
//     */
//    @Test
//    public void testToIrSignal() throws Exception {
//        System.out.println("toIrSignal");
//        Double fallbackFrequency = null;
//        Double dummyGap = null;
//        MultiParser instance = null;
//        IrSignal expResult = null;
//        IrSignal result = instance.toIrSignal(fallbackFrequency, dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of toIrSequence method, of class MultiParser.
//     */
//    @Test
//    public void testToIrSequence() throws Exception {
//        System.out.println("toIrSequence");
//        Double dummyGap = null;
//        MultiParser instance = null;
//        IrSequence expResult = null;
//        IrSequence result = instance.toIrSequence(dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of toModulatedIrSequence method, of class MultiParser.
//     */
//    @Test
//    public void testToModulatedIrSequence() throws Exception {
//        System.out.println("toModulatedIrSequence");
//        Double fallbackFrequency = null;
//        Double dummyGap = null;
//        MultiParser instance = null;
//        ModulatedIrSequence expResult = null;
//        ModulatedIrSequence result = instance.toModulatedIrSequence(fallbackFrequency, dummyGap);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    @SuppressWarnings("UnusedAssignment")
    public void testToIrSequence_0args() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSequence");
        MultiParser instance = MultiParser.newIrCoreParser(nec1);
        IrSequence result = instance.toIrSequence();
        System.out.println(result.toString(true, " "));
        assertEquals(result.toString(true, " "), nec1);

        instance = MultiParser.newIrCoreParser(nec1Odd);
        try {
            instance.toIrSequence();
            fail();
        } catch (OddSequenceLengthException ex) {
        }
    }

    /**
     * Test of toIrSequence method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSequence_double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSequence");
        double dummyGap = 1234.0;
        MultiParser instance = MultiParser.newIrCoreParser(nec1Odd);
        instance.toIrSequence(dummyGap);
    }

    /**
     * Test of toIrSignalAsMultiLine method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignalAsMultiLine() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignalAsMultiLine");
        MultiParser instance = MultiParser.newIrCoreParser(ortekmceMultiline);
        @SuppressWarnings("UnusedAssignment")
        IrSignal result = instance.toIrSignal(null);
        assertNotNull(result);

        instance = MultiParser.newIrCoreParser(ortekmceBracketed);
        result = instance.toIrSignal(null);
        assertNotNull(result);
    }

    /**
     * Test of toIrSignalAsBracketedString method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignalAsBracketedString() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignalAsBracketedString");
        MultiParser instance = MultiParser.newIrCoreParser(ortekmceBracketed);
        @SuppressWarnings("UnusedAssignment")
        IrSignal result = instance.toIrSignal(null);
        assertEquals(result.getEndingLength(), 32);
        instance = MultiParser.newIrCoreParser(ortekmceMultiline);
        result = instance.toIrSignal(null);
        assertEquals(result.getEndingLength(), 32);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignal_0args() throws InvalidArgumentException {
        System.out.println("toIrSignal");
        MultiParser instance = MultiParser.newIrCoreParser(ortekmceBracketed);
        IrSignal result = instance.toIrSignal();
        assertNotNull(result);
        instance = MultiParser.newIrCoreParser(ortekmceMultiline);
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
        MultiParser instance = MultiParser.newIrCoreParser(ortekmceBracketed);
        IrSignal result = instance.toIrSignal(null);
        assertNull(result.getFrequency());
        double frequency = 12345.0;
        result = instance.toIrSignal(frequency);
        assertEquals(result.getFrequency(), frequency, 00001);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignal_Double_double() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignal_Double_double");
        double threshold = 30000.0;
        MultiParser instance = MultiParser.newIrCoreParser(nec1Repeat);
        IrSignal result = instance.toIrSignal(null, threshold);
        assertNull(result.getFrequency());
        double fallbackFrequency = 12345.0;
        result = instance.toIrSignal(fallbackFrequency, threshold);
        assertEquals(result.getFrequency(), fallbackFrequency, 0.000001);
    }

    /**
     * Test of toIrSignal method, of class RawParser.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToIrSignal_Double_double_silly() throws OddSequenceLengthException, InvalidArgumentException {
        System.out.println("toIrSignal_Double_double_silly");
        double threshold = 30000.0;
        MultiParser instance = MultiParser.newIrCoreParser("blah blah");
        IrSignal result = instance.toIrSignal(null, threshold);
        assertNull(result);
    }
}
