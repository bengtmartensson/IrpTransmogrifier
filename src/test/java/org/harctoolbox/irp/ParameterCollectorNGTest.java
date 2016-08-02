/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
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
            assertEquals(parameterCollector.get("F"), 1024|5);
            parameterCollector.add("F", 1L, 1L);
            assertEquals(parameterCollector.get("F"), 1024|5);
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
        assertEquals(parameterCollector.get("sheldon"), 73);
        assertEquals(parameterCollector.get("penny"), ParameterCollector.INVALID);
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
        long result = parameterCollector.get("answer");
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
}
