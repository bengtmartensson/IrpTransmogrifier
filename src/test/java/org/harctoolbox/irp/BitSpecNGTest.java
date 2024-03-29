package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class BitSpecNGTest {

    private static final String NEC1_BITSPEC = "<1,-1|1,-3>";
    private static final String RC5_BITSPEC = "<1,-1|-1,1>";
    private static final String RC6_BITSPEC = "<-1,1|1,-1>";
    private static final String NOKIA32_BITSPEC = "<164,-276|164,-445|164,-614|164,-783>";
    private static final String NEC1_GENERALSPEC = "{38.4k,564}";

    public BitSpecNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getChunkSize method, of class BitSpec.
     */
    @Test
    public void testGetChunkSize() {
        System.out.println("getChunkSize");
        BitSpec nec1 = new BitSpec(NEC1_BITSPEC);
        BitSpec nokia32 = new BitSpec(NOKIA32_BITSPEC);
        BitSpec rc5 = new BitSpec(RC5_BITSPEC);
        assertEquals(nec1.getChunkSize(), 1);
        assertEquals(rc5.getChunkSize(), 1);
        assertEquals(nokia32.getChunkSize(), 2);
    }

    /**
     * Test of isEmpty method, of class BitSpec.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        assertEquals(new BitSpec().isEmpty(), true);
        assertEquals(new BitSpec(NEC1_BITSPEC).isEmpty(), false);
    }

    /**
     * Test of isStandardPWM method, of class BitSpec.
     */
    @Test
    public void testIsStandardPWM() {
        System.out.println("isStandardPWM");
        BitSpec nec1 = new BitSpec(NEC1_BITSPEC);
        BitSpec nokia32 = new BitSpec(NOKIA32_BITSPEC);
        BitSpec rc5 = new BitSpec(RC5_BITSPEC);
        assertTrue(nec1.isPWM(2));
        assertFalse(nokia32.isPWM(2));
        assertFalse(rc5.isPWM());
    }

    /**
     * Test of isStandardBiPhase method, of class BitSpec.
     */
    @Test
    public void testIsStandardBiPhase() {
        System.out.println("isStandardBiPhase");
        try {
            GeneralSpec generalSpec = new GeneralSpec(NEC1_GENERALSPEC);
            BitSpec nec1 = new BitSpec(NEC1_BITSPEC);
            BitSpec nokia32 = new BitSpec(NOKIA32_BITSPEC);
            BitSpec rc5 = new BitSpec(RC5_BITSPEC);
            BitSpec rc6 = new BitSpec(RC6_BITSPEC);
            //BitSpec empty = new BitSpec();
            assertFalse(nec1.isStandardBiPhase(generalSpec, NameEngine.EMPTY));
            assertFalse(nokia32.isStandardBiPhase(generalSpec, NameEngine.EMPTY));
            assertTrue(rc5.isStandardBiPhase(generalSpec, NameEngine.EMPTY));
            assertTrue(rc6.isStandardBiPhase(generalSpec, NameEngine.EMPTY));
        } catch (IrpInvalidArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class BitSpec.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        BitSpec instance = new BitSpec(NOKIA32_BITSPEC);
        int expResult = 0;
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);
    }

    /**
     * Test of toIrpString method, of class BitSpec.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        BitSpec instance = new BitSpec(NOKIA32_BITSPEC);
        String result = instance.toIrpString();
        assertEquals(result, "<164,-276|164,-445|164,-614|164,-783>");
    }

    /**
     * Test of numberOfBitspecDurations method, of class BitSpec.
     */
    @Test
    public void testNumberOfBitspecDurations() {
        System.out.println("numberOfBitspecDurations");
        int result = new BitSpec(NOKIA32_BITSPEC).numberOfBitspecDurations();
        assertEquals(result, 2);
    }
}
