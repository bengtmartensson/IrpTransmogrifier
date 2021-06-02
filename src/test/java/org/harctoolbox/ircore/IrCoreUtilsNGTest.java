package org.harctoolbox.ircore;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IrCoreUtilsNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrCoreUtilsNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of seconds2microseconds method, of class IrCoreUtils.
     */
    @Test
    public void testSeconds2microseconds() {
        System.out.println("seconds2microseconds");
        double secs = 12.3456;
        double expResult = 12345600.0;
        double result = IrCoreUtils.seconds2microseconds(secs);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of milliseconds2microseconds method, of class IrCoreUtils.
     */
    @Test
    public void testMilliseconds2microseconds() {
        System.out.println("milliseconds2microseconds");
        double ms = 3.145926;
        double expResult = 3145.926;
        double result = IrCoreUtils.milliseconds2microseconds(ms);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of khz2Hz method, of class IrCoreUtils.
     */
    @Test
    public void testKhz2Hz() {
        System.out.println("khz2Hz");
        double khz = 38.4;
        double expResult = 38400.0;
        double result = IrCoreUtils.khz2Hz(khz);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of percent2real method, of class IrCoreUtils.
     */
    @Test
    public void testPercent2real() {
        System.out.println("percent2real");
        double percent = 42.0;
        double expResult = 0.42;
        double result = IrCoreUtils.percent2real(percent);
        assertEquals(result, expResult, 0.000001);
    }

    /**
     * Test of real2percent method, of class IrCoreUtils.
     */
    @Test
    public void testReal2percent() {
        System.out.println("real2percent");
        double x = 0.7342;
        double expResult = 73.42;
        double result = IrCoreUtils.real2percent(x);
        assertEquals(result, expResult, 0.000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_DoubleObjectArr() {
        System.out.println("l1NormDouble");
        Double[] sequence = new Double[] { Double.valueOf(1f), Double.valueOf(2f), Double.valueOf(3f), Double.valueOf(4f) };
        double expResult = 10.0;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_xdoubleArr() {
        System.out.println("l1Normdouble");
        double[] sequence = new double[] { 1f, 2f, 3f, 4f };
        double expResult = 10.0;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_3args() {
        System.out.println("l1Norm");
        double[] sequence = new double[] { 1f, 2f, 3f, 4f };
        int beg = 1;
        int length = 2;
        double expResult = 5.0;
        double result = IrCoreUtils.l1Norm(sequence, beg, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of approximatelyEquals method, of class IrCoreUtils.
     */
    @Test
    public void testApproximatelyEquals_4args_1() {
        System.out.println("approximatelyEquals");
        Assert.assertTrue( IrCoreUtils.approximatelyEquals(73.0, 74.0, 1.0, 0.0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(73.0, 74.0, 0.9, 0.0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(73.0, 74.0, 0.0, 0.01));
        Assert.assertTrue( IrCoreUtils.approximatelyEquals(103.0, 104.0, 0.0, 0.01));
    }

    /**
     * Test of approximatelyEquals method, of class IrCoreUtils.
     */
    @Test
    public void testApproximatelyEquals_4args_2() {
        System.out.println("approximatelyEquals");
        Assert.assertTrue(IrCoreUtils.approximatelyEquals(99, 100, 1, 0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(99, 101, 1, 0));
    }

      /**
     * Test of reverse method, of class IrCoreUtils.
     */
    @Test
    public void testReverse_long_int() {
        System.out.println("reverse");
        long x = 73L;
        int width = 8;
        long expResult = 146L;
        long result = IrCoreUtils.reverse(x, width);
        assertEquals(result, expResult);
        assertEquals(IrCoreUtils.reverse(123, 8), 222);
        assertEquals(IrCoreUtils.reverse(1, 5), 16);
    }

    /**
     * Test of reverse method, of class IrCoreUtils.
     */
    @Test
    public void testReverse_BigInteger_int() {
        System.out.println("reverse");
        BigInteger x = BigInteger.valueOf(123);
        int width = 8;
        BigInteger expResult = BigInteger.valueOf(222);
        BigInteger result = IrCoreUtils.reverse(x, width);
        assertEquals(result, expResult);
        x = new BigInteger("80000000000000000000000000000055", 16);
        expResult = new BigInteger("AA000000", 16);//2852126720L);
        result = IrCoreUtils.reverse(x, 32);
        assertEquals(result, expResult);
        x =         new BigInteger("80000000000000000000000000000055", 16);
        expResult = new BigInteger("AA000000000000000000000000000001", 16);
        result = IrCoreUtils.reverse(x, 128);
        assertEquals(result, expResult);

        x = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        expResult = x;
        result = IrCoreUtils.reverse(x, 128);
        assertEquals(result, expResult);

        x = BigInteger.ONE;
        expResult = new BigInteger("8000000000000000", 16);
        result = IrCoreUtils.reverse(x, 64);
        assertEquals(result, expResult);

        expResult = new BigInteger("80000000000000000", 16);
        result = IrCoreUtils.reverse(x, 68);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrCoreUtils.
     */
    @Test
    public void testOnes_long() {
        System.out.println("ones");
        int width = 7;
        long expResult = 127L;
        long result = IrCoreUtils.ones(width);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrCoreUtils.
     */
    @Test
    public void testOnes_int() {
        System.out.println("ones");
        int width = 9;
        int expResult = 511;
        long result = IrCoreUtils.ones(width);
        assertEquals(result, expResult);
    }

    /**
     * Test of maskTo method, of class IrCoreUtils.
     */
    @Test
    public void testMaskTo_long_long() {
        System.out.println("maskTo");
        long data = 73L;
        int width = 4;
        long expResult = 9L;
        long result = IrCoreUtils.maskTo(data, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of maskTo method, of class IrCoreUtils.
     */
    @Test
    public void testMaskTo_int_int() {
        System.out.println("maskTo");
        int data = 777;
        int width = 8;
        long expResult = 9L;
        long result = IrCoreUtils.maskTo(data, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of microseconds2milliseconds method, of class IrCoreUtils.
     */
    @Test
    public void testMicroseconds2milliseconds() {
        System.out.println("microseconds2milliseconds");
        double us = 1234;
        double expResult = 1.234;
        double result = IrCoreUtils.microseconds2milliseconds(us);
        assertEquals(result, expResult, 0.00001);
    }

    /**
     * Test of hz2khz method, of class IrCoreUtils.
     */
    @Test
    public void testHz2khz() {
        System.out.println("hz2khz");
        double frequency = 45678;
        double expResult = 45.678;
        double result = IrCoreUtils.hz2khz(frequency);
        assertEquals(result, expResult, 0.00001);
    }

    /**
     * Test of us2Periods method, of class IrCoreUtils.
     */
    @Test
    public void testUs2Periods() {
        System.out.println("us2Periods");
        double us = 1050.0;
        double frequency = 40000;
        double expResult = 42;
        double result = IrCoreUtils.us2Periods(us, frequency);
        assertEquals(result, expResult, 0.0001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_DoubleArr() {
        System.out.println("l1Norm");
        Double[] sequence = new Double[] { Double.valueOf(42f), Double.valueOf(73f) };
        double expResult = 115f;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_doubleArr() {
        System.out.println("l1Norm");
        double[] sequence = new double[] { 42f, 73f};
        double expResult = 115f;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of spaces method, of class IrCoreUtils.
     */
    @Test
    public void testSpaces() {
        System.out.println("spaces");
        int length = 5;
        String expResult = "     ";
        String result = IrCoreUtils.spaces(length);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrCoreUtils.
     */
    @Test
    public void testOnes() {
        System.out.println("ones");
        int n = 5;
        long expResult = 0x1FL;
        long result = IrCoreUtils.ones(n);
        assertEquals(result, expResult);
        assertEquals(IrCoreUtils.ones(0), 0L);
        assertEquals(IrCoreUtils.ones(64), -1L);
        try {
            IrCoreUtils.ones(-1);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            IrCoreUtils.ones(65);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Test of power method, of class IrCoreUtils.
     */
    @Test
    public void testPower() {
        System.out.println("power");
        assertEquals(IrCoreUtils.power(2L, 0L), 1L);
        assertEquals(IrCoreUtils.power(2L, 16L), 65536L);
    }

    /**
     * Test of parseLong method, of class IrCoreUtils.
     */
    @Test
    public void testParseLong_String() {
        System.out.println("parseLong");
        assertEquals(IrCoreUtils.parseLong("0x12345"), 0x12345);
        assertEquals(IrCoreUtils.parseLong("12345"), 12345);
        assertEquals(IrCoreUtils.parseLong("0b100100"), 0b100100);
        assertEquals(IrCoreUtils.parseLong("%100100"), 0b100100);
        assertEquals(IrCoreUtils.parseLong("012345"), 012345);
        assertEquals(IrCoreUtils.parseLong("0"), 0);
    }

    /**
     * Test of log2 method, of class IrCoreUtils.
     */
    @Test
    public void testLog2() {
        System.out.println("log2");
        //assertEquals(IrCoreUtils.log2(15), 4);
        assertEquals(IrCoreUtils.log2(16), 4);
        assertEquals(IrCoreUtils.log2(17), 5);
        try {
            assertEquals(IrCoreUtils.log2(0), 5);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Test of microseconds2seconds method, of class IrCoreUtils.
     */
    @Test
    public void testMicroseconds2seconds() {
        System.out.println("microseconds2seconds");
        double us = 12345000;
        double expResult = 12.345;
        double result = IrCoreUtils.microseconds2seconds(us);
        assertEquals(result, expResult, 0.0000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_Iterable() {
        System.out.println("l1Norm");
        List<Double> sequence = new ArrayList<>(3);
        sequence.add(12.34);
        sequence.add(42.0);
        sequence.add(-1.0);
        double expResult = 55.34;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_3args_1() {
        System.out.println("l1Norm");
        double[] sequence = new double[] {12.34, 42.0, -1.0};
        double expResult = 42.0;
        int beg = 1;
        int length = 1;
        double result = IrCoreUtils.l1Norm(sequence, beg, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_List_int() {
        System.out.println("l1Norm");
        List<Double> list = new ArrayList<>(10);
        list.add(12.34);
        list.add(42.0);
        int beg = 1;
        double expResult = 42.0;
        double result = IrCoreUtils.l1Norm(list, beg);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_3args_2() {
        System.out.println("l1Norm");
        int[] sequence = new int[] { 1, 2, 3};
        int beg = 1;
        int length = 1;
        int expResult = 2;
        int result = IrCoreUtils.l1Norm(sequence, beg, length);
        assertEquals(result, expResult);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_intArr() {
        System.out.println("l1Norm");
        int[] sequence = new int[] { 1, 2, 3};
        int expResult = 6;
        int result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult);
    }

    /**
     * Test of toCName method, of class IrCoreUtils.
     */
    @Test
    public void testToCName() {
        System.out.println("toCName");
        String name = "Donald J. Trump";
        String expResult = "DonaldJTrump";
        String result = IrCoreUtils.toCName(name);
        assertEquals(result, expResult);
    }

    /**
     * Test of numberTrue method, of class IrCoreUtils.
     */
    @Test
    public void testNumberTrue() {
        System.out.println("numberTrue");
        Boolean[] args = new Boolean[] { true, false, true};
        int expResult = 2;
        int result = IrCoreUtils.numberTrue(args);
        assertEquals(result, expResult);
    }

    /**
     * Test of radixPrefix method, of class IrCoreUtils.
     */
    @Test
    public void testRadixPrefix() {
        System.out.println("radixPrefix");
        assertEquals(IrCoreUtils.radixPrefix(2), "0b");
        assertEquals(IrCoreUtils.radixPrefix(8), "0");
        assertEquals(IrCoreUtils.radixPrefix(10), "");
        assertEquals(IrCoreUtils.radixPrefix(16), "0x");
    }

    /**
     * Test of approximatelyEquals method, of class IrCoreUtils.
     */
    @Test
    public void testApproximatelyEquals_Double_Double() {
        System.out.println("approximatelyEquals");
        assertFalse(IrCoreUtils.approximatelyEquals(12.34, 112.35));
        assertTrue(IrCoreUtils.approximatelyEquals(12.34, 12.3400005));
    }

    /**
     * Test of maskTo method, of class IrCoreUtils.
     */
    @Test
    public void testMaskTo() {
        System.out.println("maskTo");
        long data = -1L;
        int width = 3;
        long expResult = 7L;
        long result = IrCoreUtils.maskTo(data, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of capitalize method, of class IrCoreUtils.
     */
    @Test
    public void testCapitalize() {
        System.out.println("capitalize");
        String s = "capitalize";
        String expResult = "Capitalize";
        String result = IrCoreUtils.capitalize(s);
        assertEquals(result, expResult);
    }

    /**
     * Test of javaifyString method, of class IrCoreUtils.
     */
    @Test
    public void testJavaifyString() {
        System.out.println("javaifyString");
        String s = "";
        String expResult = "";
        String result = IrCoreUtils.javaifyString(s);
        assertEquals(result, expResult);
    }

    /**
     * Test of hasDuplicatedElements method, of class IrCoreUtils.
     */
    @Test
    public void testHasDuplicatedElements() {
        System.out.println("hasDuplicatedElements");
        List<String> list = new ArrayList<>(10);
        list.add("xyz");
        list.add("78");
        assertFalse(IrCoreUtils.hasDuplicatedElements(list));
        list.add("xyz");
        assertTrue(IrCoreUtils.hasDuplicatedElements(list));
    }

    /**
     * Test of approximateGreatestCommonDivider method, of class IrCoreUtils.
     */
    @Test
    public void testApproximateGreatestCommonDivider() {
        System.out.println("approximateGreatestCommonDivider");
        assertEquals(IrCoreUtils.approximateGreatestCommonDivider(100, 128, 0.01), 4);
        assertEquals(IrCoreUtils.approximateGreatestCommonDivider(997, 57, 0.01), 1);
    }

    /**
     * Test of approximateGreatestCommonDivider method, of class IrCoreUtils.
     */
    @Test
    public void testApproximateGreatestCommonDividerList() {
        System.out.println("approximateGreatestCommonDividerList");
        List<Integer> list = new ArrayList<>(10);
        list.add(100);
        list.add(128);
        assertEquals(IrCoreUtils.approximateGreatestCommonDivider(list, 0.01), 4);
        list.add(2*997);
        assertEquals(IrCoreUtils.approximateGreatestCommonDivider(list, 0.01), 2);
        list.add(3);
        assertEquals(IrCoreUtils.approximateGreatestCommonDivider(list, 0.01), 1);
    }

    /**
     * Test of tabs method, of class IrCoreUtils.
     */
    @Test
    public void testTabs() {
        System.out.println("tabs");
        int length = 5;
        String expResult = "\t\t\t\t\t";
        String result = IrCoreUtils.tabs(length);
        assertEquals(result, expResult);
    }

    /**
     * Test of approximateGreatestCommonDivider method, of class IrCoreUtils.
     */
    @Test
    public void testApproximateGreatestCommonDivider_3args() {
        System.out.println("approximateGreatestCommonDivider");
        int first = 300;
        int second = 400;
        double relTolerance = 0.05;
        int expResult = 100;
        int result = IrCoreUtils.approximateGreatestCommonDivider(first, second, relTolerance);
        assertEquals(result, expResult);
    }

     /**
     * Test of maxLength method, of class IrCoreUtils.
     */
    @Test
    public void testMaxLength_StringArr() {
        System.out.println("maxLength");
        String[] strings = new String[] {"I", "am", "a", "very", "stable", "genious"};
        int expResult = 7;
        int result = IrCoreUtils.maxLength(strings);
        assertEquals(result, expResult);
    }

    /**
     * Test of basename method, of class IrCoreUtils.
     */
    @Test
    public void testBasename() {
        System.out.println("basename");
        String filename = "/a/b/c.d";
        String expResult = "c";
        String result = IrCoreUtils.basename(filename);
        assertEquals(result, expResult);
    }

    /**
     * Test of addExtensionIfNotPresent method, of class IrCoreUtils.
     */
    @Test
    public void testAddExtensionIfNotPresent() {
        System.out.println("addExtensionIfNotPresent");
        String filename = "xxx";
        String extension = "y";
        String expResult = "xxx.y";
        String result = IrCoreUtils.addExtensionIfNotPresent(filename, extension);
        assertEquals(result, expResult);
        filename = "xxx.z";
        expResult = "xxx.z";
        result = IrCoreUtils.addExtensionIfNotPresent(filename, extension);
        assertEquals(result, expResult);
    }

    /**
     * Test of setRadixPrefixes.
     * Silly name is to execute it last, since it has side effects.
     */
    @Test
    public void ztestParseLong_String_WithSetRadixPrefixes() {
        System.out.println("setRadixPrefixes");

        // Set up a map of prefix -> radix mappings
        Map<String, Integer> map = new LinkedHashMap<>(4);
        map.put("0b", 2);
        map.put("0x", 16);
        map.put("0q", 4);
        map.put("0cs", 11); // For Carl Sagan ;-)
        IrCoreUtils.setRadixPrefixes(map);

        assertEquals(IrCoreUtils.parseLong("0x12345"), 0x12345);
        assertEquals(IrCoreUtils.parseLong("12345"), 12345);
        assertEquals(IrCoreUtils.parseLong("0b100100"), 0b100100);
        try {
            IrCoreUtils.parseLong("%100100");
            fail();
        } catch (NumberFormatException ex) {
        }
        assertEquals(IrCoreUtils.parseLong("012345"), 12345);
        assertEquals(IrCoreUtils.parseLong("0"), 0);
        assertEquals(IrCoreUtils.parseLong("0cs1A"), 21);
        assertEquals(IrCoreUtils.parseLong("0q12"), 6);

        assertEquals(IrCoreUtils.radixPrefix(2), "0b");
        assertEquals(IrCoreUtils.radixPrefix(4), "0q");
        assertEquals(IrCoreUtils.radixPrefix(11), "0cs");
        assertEquals(IrCoreUtils.radixPrefix(10), "");
    }

    /**
     * Test of checkEncoding method, of class IrCoreUtils.
     * @throws java.io.UnsupportedEncodingException
     */
    @Test
    public void testCheckEncoding() throws UnsupportedEncodingException {
        System.out.println("checkEncoding");
        IrCoreUtils.checkEncoding("iso-8859-1");
        try {
            IrCoreUtils.checkEncoding("\niso-8859-1");
            fail();
        } catch (UnsupportedEncodingException ex) {
        }
        try {
            IrCoreUtils.checkEncoding("covfefe");
            fail();
        } catch (UnsupportedEncodingException ex) {
        }
        try {
            IrCoreUtils.checkEncoding(null);
            fail();
        } catch (UnsupportedEncodingException ex) {
        }
        try {
            IrCoreUtils.checkEncoding("");
            fail();
        } catch (UnsupportedEncodingException ex) {
        }
    }
}
