package org.harctoolbox.irp;

import org.harctoolbox.ircore.InvalidArgumentException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParameterSpecNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ParameterSpecNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }



    /**
     * Test of check method, of class ParameterSpec.
     */
    @Test
    public void testCheck() {
        System.out.println("check");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        NameEngine nameEngine = new NameEngine();
        try {
            instance.check(nameEngine);
            fail();
        } catch (UnassignedException ex) {
        } catch (IrpSyntaxException | InvalidArgumentException | DomainViolationException ex) {
            fail();
        }

        try {
            nameEngine = new NameEngine("{D=15}");
            instance.check(nameEngine);
        } catch (IrpSyntaxException | InvalidArgumentException | DomainViolationException | UnassignedException ex) {
            fail();
        }

        try {
            nameEngine = new NameEngine("{D=9}");
            instance.check(nameEngine);
            fail();
        } catch (IrpSyntaxException | InvalidArgumentException | UnassignedException ex) {
            fail();
        } catch (DomainViolationException ex) {
        }

        try {
            nameEngine = new NameEngine("{D=21}");
            instance.check(nameEngine);
            fail();
        } catch (IrpSyntaxException | InvalidArgumentException | UnassignedException ex) {
            fail();
        } catch (DomainViolationException ex) {
        }

    }


    /**
     * Test of isOk method, of class ParameterSpec.
     */
//    @Test
//    public void testIsOk() {
//        System.out.println("isOK");
//        long x = 0L;
//        ParameterSpec instance = null;
//        boolean expResult = false;
//        boolean result = instance.isOk(x);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of domainAsString method, of class ParameterSpec.
     */
    @Test
    public void testDomainAsString() {
        System.out.println("domainAsString");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        String result = instance.domainAsString();
        assertEquals(result, "10..20");
    }

    /**
     * Test of getName method, of class ParameterSpec.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        String result = instance.getName();
        assertEquals(result, "D");
    }

    /**
     * Test of getDefault method, of class ParameterSpec.
     */
    @Test
    public void testGetDefault() {
        System.out.println("getDefault");
        ParameterSpec instance = new ParameterSpec("S:0..255=255-D");
        Expression result = instance.getDefault();
        assertEquals(result.toIrpString(), "(255-D)");
    }

    /**
     * Test of getMin method, of class ParameterSpec.
     */
    @Test
    public void testGetMin() {
        System.out.println("getMin");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        long result = instance.getMin();
        assertEquals(result, 10L);
    }

    /**
     * Test of getMax method, of class ParameterSpec.
     */
    @Test
    public void testGetMax() {
        System.out.println("getMax");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        long result = instance.getMax();
        assertEquals(result, 20L);
    }

    /**
     * Test of hasMemory method, of class ParameterSpec.
     */
    @Test
    public void testHasMemory() {
        System.out.println("hasMemory");
        ParameterSpec instance = new ParameterSpec("S:0..255=255-D");
        boolean result = instance.hasMemory();
        assertFalse(result);
        instance = new ParameterSpec("T@:0..255=255-D");
        result = instance.hasMemory();
        assertTrue(result);
    }
}
