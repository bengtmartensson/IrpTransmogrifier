package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrpUtilsNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrpUtilsNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toCIdentifier method, of class IrpUtils.
     */
    @Test
    public void testToCIdentifier() {
        System.out.println("toCIdentifier");
        assertEquals("xyz0", IrpUtils.toCIdentifier("xyz0"));
        assertEquals("x0xyz0", IrpUtils.toCIdentifier("0xyz0"));
        assertEquals("xy_z0", IrpUtils.toCIdentifier("xy$z0"));
        assertEquals("xyzA0", IrpUtils.toCIdentifier("xyzA0"));
        assertEquals("xyz0_", IrpUtils.toCIdentifier("xyz0ä"));
        assertEquals("xyz0_x", IrpUtils.toCIdentifier("xyz0\tx"));
    }
}
