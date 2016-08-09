package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class BitFieldNGTest {

    private NameEngine names = null;

    public BitFieldNGTest() {
        try {
            names = new NameEngine("{A = 7, F=244, D=4}");
        } catch (IrpSyntaxException ex) {
            Logger.getLogger(BitFieldNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of parse method, of class BitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        NameEngine nameEngine = new NameEngine("{D = 244}"); // 0b11110100
        assertEquals(BitField.parse("D:6", nameEngine),   0b110100L);
        assertEquals(BitField.parse("D:6:0", nameEngine), 0b110100L);
        assertEquals(BitField.parse("D:6:2", nameEngine), 0b111101L);

        nameEngine = new NameEngine("{D = -5}");
        assertEquals(BitField.parse("D:6", nameEngine),   0b111011L);
        assertEquals(BitField.parse("(-5):6", nameEngine),0b111011L);

        nameEngine = new NameEngine("{D = 244}");
        assertEquals(BitField.parse("~D:6:2", nameEngine),0b000010L);
        //~D:6:2 gives the binary form 000010,
        assertEquals(BitField.parse("D:-6:2", nameEngine), 0b101111L);
        //D:-6:2 gives 101111 and ~D:-6:2 gives 010000.
        assertEquals(BitField.parse("~D:-6:2", nameEngine), 0b010000L);

        nameEngine = new NameEngine("{A = 7, F=244, D=4}");
        //But F:-D with D = 4 is valid, and does mean F:-4.
        assertEquals(BitField.parse("F:-D", nameEngine), BitField.parse("F:-4", nameEngine));

        //If A = 7 then F:A:2 means F:7:2
        assertEquals(BitField.parse("F:A:2", nameEngine), BitField.parse("F:7:2", nameEngine));

        //but F:(A:2) means F:3,
        assertEquals(BitField.parse("F:(A:2)", nameEngine), BitField.parse("F:3", nameEngine));
    }

    /**
     * Test of newBitField method, of class BitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testNewBitField_String() throws Exception {
        System.out.println("newBitField");
        BitField bitfield = BitField.newBitField("~D:-6:2");
        assertTrue(bitfield instanceof FiniteBitField);
        bitfield = BitField.newBitField("~D::2");
        assertTrue(bitfield instanceof InfiniteBitField);
    }

    /**
     * Test of toString method, of class BitField.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    @Test
    public void testToString_0args() throws IrpSyntaxException {
        System.out.println("toString");
        BitField instance = BitField.newBitField("~D:-6:2");
        assertEquals(instance.toString(), "~D:-6:2");
    }

    /**
     * Test of toString method, of class BitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testToString_NameEngine() throws Exception {
        System.out.println("toString");
        BitField instance = BitField.newBitField("~D:-6:2");
        String result = instance.toString(names);
        assertEquals(result, "~4:-6:2");
    }

    /**
     * Test of getWidth method, of class BitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetWidth() throws Exception {
        System.out.println("getWidth");
        BitField instance = BitField.newBitField("~D:-D:2");
        long expResult = 4L;
        long result = instance.getWidth(names);
        assertEquals(result, expResult);
    }

    /**
     * Test of isEmpty method, of class BitField.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    @Test
    public void testIsEmpty() throws IrpSyntaxException {
        System.out.println("isEmpty");
        BitField instance = BitField.newBitField("~D:-6:2");
        boolean expResult = false;
        boolean result = instance.isEmpty(names);
        assertEquals(result, expResult);
    }

    /**
     * Test of interleavingOk method, of class BitField.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    @Test
    public void testInterleavingOk() throws IrpSyntaxException {
        System.out.println("interleavingOk");
        BitField instance = BitField.newBitField("~D:-6:2");
        boolean result = instance.interleavingOk(null, null, DurationType.flash, false);
        assertTrue(result);
    }
}
