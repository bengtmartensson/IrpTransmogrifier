package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ParameterSpecNGTest {

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
    @SuppressWarnings("UseSpecificCatch")
    public void testCheck() {
        System.out.println("check");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        NameEngine nameEngine = new NameEngine();
        try {
            instance.check(nameEngine);
            fail();
        } catch (NameUnassignedException ex) {
        } catch (DomainViolationException | InvalidNameException ex) {
            fail();
        }

        try {
            nameEngine = new NameEngine("{D=15}");
            instance.check(nameEngine);
        } catch (DomainViolationException | InvalidNameException | NameUnassignedException ex) {
            fail();
        }

        try {
            nameEngine = new NameEngine("{D=9}");
            instance.check(nameEngine);
            fail();
        } catch (DomainViolationException ex) {
        } catch (InvalidNameException | NameUnassignedException ex) {
            fail();
        }

        try {
            nameEngine = new NameEngine("{D=21}");
            instance.check(nameEngine);
            fail();
        } catch (DomainViolationException ex) {
        } catch (InvalidNameException | NameUnassignedException ex) {
            fail();
        }
    }

    //public static void main(String[] args) {
        /*        ParameterSpec dev = null;
        ParameterSpec toggle = null;
        ParameterSpec func = null;
        try {
        dev = new ParameterSpec("d", 0, 255, false, "255-s");
        toggle = new ParameterSpec("t", 0, 1, true, 0);
        func = new ParameterSpec("F", 0, 1, false, 0);
        System.out.println(new ParameterSpec("Fx", 0, 1, false, 0));
        System.out.println(new ParameterSpec("Fx", 0, 1, false));
        System.out.println(new ParameterSpec("Fx", 0, 1));
        System.out.println(new ParameterSpec("D:0..31"));
        System.out.println(new ParameterSpec("D@:0..31=42"));
        System.out.println(new ParameterSpec("D:0..31=42*3+33"));
        System.out.println(dev);
        System.out.println(toggle);
        System.out.println(func);
        System.out.println(dev.isOk(-1));
        System.out.println(dev.isOk(0));
        System.out.println(dev.isOk(255));
        System.out.println(dev.isOk(256));
        } catch (ParseException ex) {
        System.out.println(ex.getMessage());
        }*/
    //}

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

        instance = new ParameterSpec("S:0..255");
        result = instance.getDefault();
        assertTrue(result == null);
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

    /**
     * Test of random method, of class ParameterSpec.
     */
    @Test
    public void testRandom() {
        System.out.println("random");
        ParameterSpec instance = new ParameterSpec("D:10..20");
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < 100; i++) {
            long p = instance.random();
            min = Math.min(min, p);
            max = Math.max(max, p);
        }
        assertEquals(min, 10);
        assertEquals(max, 20);

        instance = new ParameterSpec("Z:10..0x4000000000000000");
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        for (int i = 0; i < 1000; i++) {
            long p = instance.random();
            min = Math.min(min, p);
            max = Math.max(max, p);
        }
        assertTrue(min < 0x0100000000000000L);
        assertTrue(max > 0x3F00000000000000L);
    }

    /**
     * Test of isStandardName.
     */
    @Test
    public void testIsStandardName() {
        System.out.println("isStandardName");
        assertTrue(ParameterSpec.isStandardName("D"));
        assertTrue(ParameterSpec.isStandardName("S"));
        assertTrue(ParameterSpec.isStandardName("F"));
        assertTrue(ParameterSpec.isStandardName("T"));
        assertFalse(ParameterSpec.isStandardName("X"));
    }
}
