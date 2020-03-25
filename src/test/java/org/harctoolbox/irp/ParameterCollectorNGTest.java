package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrCoreUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ParameterCollectorNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private ParameterCollector parameterCollector;

    public ParameterCollectorNGTest() {
        try {
            //NameEngine nameEngine = new NameEngine("{X=Y+Z}");
            parameterCollector = new ParameterCollector();
            parameterCollector.add("answer", 42);
            parameterCollector.add("F", 5, IrCoreUtils.ones(5));
        } catch (ParameterInconsistencyException ex) {
            fail();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of add method, of class ParameterCollector.
     */
    @Test
    public void testAdd_3args() {
        try {
            System.out.println("add");
            parameterCollector.add("F", 1024L, 1024L);
            assertEquals(parameterCollector.getValue("F"), 1024|5);
            parameterCollector.add("F", 1L, 1L);
            assertEquals(parameterCollector.getValue("F"), 1024|5);
        } catch (ParameterInconsistencyException ex) {
            fail();
        }
        try {
            parameterCollector.add("F", 0L, 1L);
            fail();
        } catch (ParameterInconsistencyException ex) {
        }
    }

    /**
     * Test of add method, of class ParameterCollector.
     */
    @Test
    public void testAdd_String_long() {
        System.out.println("add");
        try {
            parameterCollector.add("sheldon", 73);
        } catch (ParameterInconsistencyException ex) {
            fail();
        }
        assertEquals(parameterCollector.getValue("sheldon"), 73);
        assertEquals(parameterCollector.getValue("penny"), ParameterCollector.INVALID);
        try {
            parameterCollector.add("sheldon", 43);
            fail();
        } catch (ParameterInconsistencyException ex) {
        }
    }

    /**
     * Test of get method, of class ParameterCollector.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        long expResult = 42L;
        long result = parameterCollector.getValue("answer");
        assertEquals(result, expResult);
    }

    /**
     * Test of clone method, of class ParameterCollector.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        ParameterCollector result = parameterCollector.clone();
        String str = parameterCollector.toString();
        assertEquals(result.toString(), str);
        try {
            result.add("F", 2048L, 2048L);
        } catch (ParameterInconsistencyException ex) {
            fail();
        }
        assertEquals(parameterCollector.toString(), str);
        assertFalse(result.toString().equals(str));
    }

    /**
     * Test of toString method, of class ParameterCollector.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        assertEquals(parameterCollector.toString(), "{answer=42&1111111111111111111111111111111111111111111111111111111111111111;F=1029&10000011111;sheldon=73&1111111111111111111111111111111111111111111111111111111111111111}");
    }

    /**
     * Test of overwrite method, of class ParameterCollector.
     */
    @Test
    public void testOverwrite() {
        System.out.println("overwrite");

        String name = "junk";
        long value = 123L;
        long anotherValue = 456L;
        ParameterCollector instance = parameterCollector.clone();
        try {
            instance.add(name, value);
        } catch (ParameterInconsistencyException ex) {
        }
        try {
            instance.add(name, anotherValue);
            fail();
        } catch (ParameterInconsistencyException ex) {
            // should go here
        }
        assertEquals(instance.getValue(name), value);
    }
}
