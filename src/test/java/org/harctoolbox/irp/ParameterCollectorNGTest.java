package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
            parameterCollector.add("F", 5, IrpUtils.ones(5));
        } catch (NameConflictException ex) {
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
        } catch (NameConflictException ex) {
            fail();
        }
        try {
            parameterCollector.add("F", 0L, 1L);
            fail();
        } catch (NameConflictException ex) {
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
        } catch (NameConflictException ex) {
            fail();
        }
        assertEquals(parameterCollector.getValue("sheldon"), 73);
        assertEquals(parameterCollector.getValue("penny"), ParameterCollector.INVALID);
        try {
            parameterCollector.add("sheldon", 43);
            fail();
        } catch (NameConflictException ex) {
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
        } catch (NameConflictException ex) {
            fail();
        }
        assertEquals(parameterCollector.toString(), str);
        assertFalse(result.toString().equals(str));
    }

    /**
     * Test of toString method, of class ParameterCollector.
     */
    @Test
    public void testToString() { // FIXME
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
        } catch (NameConflictException ex) {
            Logger.getLogger(ParameterCollectorNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            instance.add(name, anotherValue);
            fail();
        } catch (NameConflictException ex) {
            // should go here
        }
        assertEquals(instance.getValue(name), value);
        instance.overwrite(name, anotherValue);
        assertEquals(instance.getValue(name), anotherValue);
    }

    /**
     * Test of checkConsistencyWith method, of class ParameterCollector.
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
//    @Test
//    public void testCheckConsistencyWith() throws UnassignedException, IncompatibleArgumentException, IrpSyntaxException {
//        System.out.println("checkConsistencyWith");
//        NameEngine nameEngine;
//        NameEngine nameEngine1;
//
//        nameEngine = new NameEngine("{answer=42}");
//        nameEngine1 = new NameEngine("{answer=43}");
//
//        try {
//            parameterCollector.checkConsistencyWith(nameEngine);
//        } catch (NameConflictException ex) {
//            fail();
//        }
//        try {
//            parameterCollector.checkConsistencyWith(nameEngine1);
//            fail();
//        } catch (NameConflictException ex) {
//
//        }
//    }

//    /**
//     * Test of toNameEngine method, of class ParameterCollector.
//     */
//    @Test
//    public void testToNameEngine() {
//        try {
//            System.out.println("toNameEngine");
//            NameEngine expResult = new NameEngine("{answer=42, F=5}");
//            //ParameterCollector instance = new ParameterCollector(expResult);
//            NameEngine result = parameterCollector.toNameEngine();
//            assertEquals(result, expResult);
//        } catch (IrpSyntaxException ex) {
//            Logger.getLogger(ParameterCollectorNGTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    /**
//     * Test of toHashMap method, of class ParameterCollector.
//     */
//    @Test
//    public void testToHashMap() {
//        System.out.println("toHashMap");
//        //ParameterCollector instance = new ParameterCollector();
//        HashMap<String, Long> expResult = new HashMap<>(2);
//        expResult.put("answer", 42L);
//        expResult.put("F", 5L);
//        HashMap result = parameterCollector.toHashMap();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of addToNameEngine method, of class ParameterCollector.
//     */
//    @Test
//    public void testAddToNameEngine() {
//        try {
//            System.out.println("addToNameEngine");
//            NameEngine nameEngine = new NameEngine("{xxx=123}");
//            parameterCollector.addToNameEngine(nameEngine);
//            assertTrue(nameEngine.get("answer").toNumber() == 42L);
//        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
//            Logger.getLogger(ParameterCollectorNGTest.class.getName()).log(Level.SEVERE, null, ex);
//            fail();
//        }
//    }
}
