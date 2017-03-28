package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PrimaryItemNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final NameEngine nameEngine;
    public PrimaryItemNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{A = 7, F=244, D=4}");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of newPrimaryItem method, of class PrimaryItem.
     */
    @Test
    public void testNewPrimaryItem_long() {
        try {
            System.out.println("newPrimaryItem");
            PrimaryItem result = PrimaryItem.newPrimaryItem(42);
            assertEquals(result.toNumber(nameEngine), 42);
        } catch (NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of newPrimaryItem method, of class PrimaryItem.
     */
    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testNewPrimaryItem_String() {
        try {
            System.out.println("newPrimaryItem");
            PrimaryItem result = PrimaryItem.newPrimaryItem("A");
            assertTrue(result instanceof Name);
            assertEquals(result.toNumber(nameEngine), 7);
            result = PrimaryItem.newPrimaryItem("54321");
            assertTrue(result instanceof Number);
            assertEquals(result.toNumber(nameEngine), 54321);
            result = PrimaryItem.newPrimaryItem("(#A)");
            assertTrue(result instanceof Expression);
            assertEquals(result.toNumber(nameEngine), 3);
        } catch (InvalidNameException | NameUnassignedException ex) {
            fail();
        }
    }
}
