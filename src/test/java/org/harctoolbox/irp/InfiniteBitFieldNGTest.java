package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class InfiniteBitFieldNGTest {

    private NameEngine nameEngine = null;
    private final InfiniteBitField instance;

    public InfiniteBitFieldNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{A = 7, F=244, D=4}");
        instance = new InfiniteBitField("~D::2");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toLong method, of class InfiniteBitField.
     */
    @Test
    public void testToNumber() {
        System.out.println("toLong");
        long result = 0;
        try {
            result = instance.toLong(nameEngine);
        } catch (NameUnassignedException ex) {
            fail();
        }
        assertEquals(result, -2L);
    }

    /**
     * Test of getWidth method, of class InfiniteBitField.
     */
    @Test
    public void testGetWidth() {
        System.out.println("getWidth");
        long result = instance.getWidth(nameEngine);
        assertEquals(result, 63L);
    }

    /**
     * Test of toString method, of class InfiniteBitField.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString(nameEngine);
        assertEquals(result, "~4::2");
    }

    /**
     * Test of toIrpString method, of class InfiniteBitField.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "~D::2");
    }

    /**
     * Test of numberOfBits method, of class InfiniteBitField.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        Integer result = instance.numberOfBits();
        assertTrue(result == 0L);
    }
}