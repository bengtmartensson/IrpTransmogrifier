package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class BitFieldNGTest {

    public BitFieldNGTest() {
    }

    /**
     * Test of parse method, of class BitField.
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
}
