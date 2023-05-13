package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class BitspecIrstreamNGTest {

    private final BitspecIrstream instance;
    public BitspecIrstreamNGTest() {
        instance = new BitspecIrstream("<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrpString method, of class BitspecIrstream.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
    }

    /**
     * Test of numberOfBitSpecs method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBitSpecs() {
        System.out.println("numberOfBitSpecs");
        int result = instance.numberOfBitSpecs();
        assertEquals(result, 1);
    }

    /**
     * Test of interleavingOk method, of class BitspecIrstream.
     */
    @Test
    public void testInterleavingOk() {
        System.out.println("interleavingOk");
        boolean result = instance.interleavingOk();
        assertTrue(result);
    }

    /**
     * Test of numberOfBits method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        int result = instance.numberOfBits();
        assertEquals(result, 32);
    }

    /**
     * Test of numberOfBareDurations method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        int result = instance.numberOfBareDurations(true);
        assertEquals(result, 8);
        result = instance.numberOfBareDurations(false);
        assertEquals(result, 4);
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class BitspecIrstream.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, 1);
    }

}
