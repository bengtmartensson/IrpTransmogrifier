package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VariationNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public VariationNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrpString method, of class Variation.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        Variation instance = new Variation("[P=0][P=1][P=2]");
        String result = instance.toIrpString();
        assertEquals(result, "[P=0][P=1][P=2]");
        instance = new Variation("[P=0][][P=2]");
        result = instance.toIrpString();
        assertEquals(result, "[P=0][][P=2]");
        instance = new Variation("[P=0][P=1]");
        result = instance.toIrpString();
        assertEquals(result, "[P=0][P=1]");
        instance = new Variation("[P=0][P=1][]");
        result = instance.toIrpString();
        assertEquals(result, "[P=0][P=1]");
    }
}
