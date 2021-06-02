package org.harctoolbox.irp;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
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
        instance.aggregate(new BitwiseParameter(0x11200, 0xFFF0F));
        assertEquals(instance.getValue(), 0x11230L);
        assertEquals(instance.getBitmask(), 0xFFFFFL);
    }

    /**
     * Test of append method, of class BitwiseParameter.
     */
    @Test
    public void testAppend() {
        System.out.println("append");
        BitwiseParameter parameter = new BitwiseParameter(0x1200, 0xFF00);
        BitwiseParameter instance = new BitwiseParameter(0x34,0xFF/*, 0x34L*/);
        instance.append(parameter);
        assertEquals(instance.getValue(), 0x341200L);
        assertEquals(instance.getBitmask(), 0xFFFF00L);
        instance.append(new BitwiseParameter(0x11200, 0xFFF0F));
        assertEquals(instance.getValue(), 0x34120011200L);
        assertEquals(instance.getBitmask(), 0xFFFF00FFF0FL);
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

//    /**
//     * Test of clone method, of class BitwiseParameter.
//     */
//    @Test
//    public void testClone() {
//        System.out.println("clone");
//        BitwiseParameter instance = new BitwiseParameter(0x1234,0xF0CF);
//        BitwiseParameter result = instance.clone();
//        assertEquals(result, instance);
//    }

    /**
     * Test of isEmpty method, of class BitwiseParameter.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        BitwiseParameter instance = new BitwiseParameter();
        assertTrue(instance.isEmpty());
        instance = new BitwiseParameter(123);
        assertFalse(instance.isEmpty());
    }

    /**
     * Test of isConsistent method, of class BitwiseParameter.
     */
    @Test
    public void testIsConsistent_BitwiseParameter() {
        System.out.println("isConsistent");
        BitwiseParameter instance = new BitwiseParameter(3, 3);
        assertTrue(instance.isConsistent(new BitwiseParameter(1023)));
        assertFalse(instance.isConsistent(new BitwiseParameter(1024)));
        //instance.setExpected(0L);
        //assertFalse(instance.isConsistent(new BitwiseParameter(1023)));
        //assertFalse(instance.isConsistent(new BitwiseParameter(1024)));
        //assertTrue(instance.isConsistent(new BitwiseParameter(0L)));
    }

    /**
     * Test of isConsistent method, of class BitwiseParameter.
     */
    @Test
    public void testIsConsistent_long() {
        System.out.println("isConsistent");
        BitwiseParameter instance = new BitwiseParameter(3, 3);
        assertTrue(instance.isConsistent(1023));
        assertFalse(instance.isConsistent(1024));
        //instance.setExpected(0L);
        //assertFalse(instance.isConsistent(1023));
        //assertTrue(instance.isConsistent(1024));
    }

    /**
     * Test of length method, of class BitwiseParameter.
     */
    @Test
    public void testLength() {
        System.out.println("length");
        BitwiseParameter instance = new BitwiseParameter();
        int expResult = 0;
        int result = instance.length();
        assertEquals(result, expResult);
        instance = new BitwiseParameter(0x12,0xFF);
        assertEquals(instance.length(), 8);
    }
}
