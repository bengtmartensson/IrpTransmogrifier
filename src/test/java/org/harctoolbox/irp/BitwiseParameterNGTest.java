package org.harctoolbox.irp;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BitwiseParameterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public BitwiseParameterNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of isConsistentWith method, of class BitwiseParameter.
     */
    @Test
    public void testIsConsistentWith() {
        System.out.println("isConsistentWith");
        long val = 0x1234L;
        BitwiseParameter instance = new BitwiseParameter(0x34, 255);
        boolean expResult = false;
        assertTrue(instance.isConsistent(val));
        assertTrue(instance.isConsistent(0x1234));
        assertFalse(instance.isConsistent(0x35));
    }

    /**
     * Test of aggregate method, of class BitwiseParameter.
     */
    @Test
    public void testAggregate() {
        System.out.println("aggregate");
        BitwiseParameter parameter = new BitwiseParameter(0x1200, 0xFF00);
        BitwiseParameter instance = new BitwiseParameter(0x34,0xFF);
        instance.aggregate(parameter);
        assertEquals(instance.getValue(), 0x1234L);
        assertEquals(instance.getBitmask(), 0xFFFFL);
    }

    /**
     * Test of equals method, of class BitwiseParameter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        BitwiseParameter instance = new BitwiseParameter(0x1234,0xFFFF);
        BitwiseParameter p1 = new BitwiseParameter(0x1200, 0xFF00);
        BitwiseParameter p2 = new BitwiseParameter(0x34,0xFF);
        p1.aggregate(p2);
        assertEquals(p1, instance);
    }

    /**
     * Test of toString method, of class BitwiseParameter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        BitwiseParameter instance = new BitwiseParameter(0x1234,0xF0FF);
        assertEquals(instance.toString(), "4148&1111000011111111");
    }

    /**
     * Test of clone method, of class BitwiseParameter.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        BitwiseParameter instance = new BitwiseParameter(0x1234,0xF0CF);
        BitwiseParameter result = instance.clone();
        assertEquals(result, instance);
    }
}
