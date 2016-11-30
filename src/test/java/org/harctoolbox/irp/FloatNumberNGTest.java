package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FloatNumberNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public FloatNumberNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toString method, of class FloatNumber.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        assertEquals(new FloatNumber(".42").toString(), "0.42");
        assertEquals(new FloatNumber("3.12345").toString(), "3.12345");
    }

    /**
     * Test of parse method, of class FloatNumber.
     */
    @Test
    public void testParse_String() {
        System.out.println("parseFloatNumber");
        assertEquals(FloatNumber.parse(".42"), 0.42, 0.0000001);
        assertEquals(FloatNumber.parse("3.12345"), 3.12345, 0.000001);
    }
}
