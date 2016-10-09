package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParameterSpecsNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final ParameterSpecs rc5;
    private final ParameterSpecs nec1;

    public ParameterSpecsNGTest() throws IrpSyntaxException {
        rc5 = new ParameterSpecs("[T@:0..1=0,D:0..31,F:0..127]");
        nec1 = new ParameterSpecs("[D:0..255,S:0..255=255-D,F:0..255]");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of isEmpty method, of class ParameterSpecs.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        ParameterSpecs instance = new ParameterSpecs();
        boolean result = instance.isEmpty();
        assertTrue(result);
        assertFalse(rc5.isEmpty());
    }

//    /**
//     * Test of getNames method, of class ParameterSpecs.
//     */
//    @Test
//    public void testGetNames() {
//        System.out.println("getNames");
//        ParameterSpecs instance = new ParameterSpecs();
//        Set expResult = null;
//        Set result = instance.getNames();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getParams method, of class ParameterSpecs.
//     */
//    @Test
//    public void testGetParams() {
//        System.out.println("getParams");
//        ParameterSpecs instance = new ParameterSpecs();
//        Collection expResult = null;
//        Collection result = instance.getParams();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getParameterSpec method, of class ParameterSpecs.
//     */
//    @Test
//    public void testGetParameterSpec() {
//        System.out.println("getParameterSpec");
//        String name = "";
//        ParameterSpecs instance = new ParameterSpecs();
//        ParameterSpec expResult = null;
//        ParameterSpec result = instance.getParameterSpec(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toString method, of class ParameterSpecs.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = rc5.toString();
        assertEquals(result, "[T@:0..1=0,D:0..31,F:0..127]");
    }

    /**
     * Test of toIrpString method, of class ParameterSpecs.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = nec1.toIrpString();
        assertEquals(result, "[D:0..255,S:0..255=(255-D),F:0..255]");
        result = rc5.toIrpString();
        assertEquals(result, "[T@:0..1=0,D:0..31,F:0..127]");
        ParameterSpecs empty = new ParameterSpecs();
        result = empty.toIrpString();
        assertEquals(result, "");
    }

//    /**
//     * Test of toElement method, of class ParameterSpecs.
//     */
//    @Test
//    public void testToElement() {
//        System.out.println("toElement");
//        Document document = null;
//        ParameterSpecs instance = new ParameterSpecs();
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of check method, of class ParameterSpecs.
     */
    @Test
    public void testCheck() {
        System.out.println("check");
        NameEngine nameEngine = null;



    }

}
