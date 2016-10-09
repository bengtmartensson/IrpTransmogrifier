package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RepeatMarkerNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public RepeatMarkerNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    private void testit(String str, int min, int max) throws InvalidRepeatException {
        //RepeatMarker.reset();
        RepeatMarker rep = new RepeatMarker(str);
        assertEquals(rep.getMin(), min);
        assertEquals(rep.getMax(), max);
    }

    /**
     * Test of getMax method, of class RepeatMarker.
     */
    @Test
    public void testGetMax() {
        try {
            System.out.println("getMax");
            testit("*", 0, Integer.MAX_VALUE);
            testit("+", 1, Integer.MAX_VALUE);
            testit("1+", 1, Integer.MAX_VALUE);
            testit("0+", 0, Integer.MAX_VALUE);
            testit("7", 7, 7);
            testit("17+", 17, Integer.MAX_VALUE);
            testit("\t7+   ", 7, Integer.MAX_VALUE);
        } catch (InvalidRepeatException ex) {
            fail();
        }
        RepeatMarker rep;
        try {
            //RepeatMarker.reset();
            rep = new RepeatMarker("*");
            assertEquals(rep.getMin(), 0);
            assertEquals(rep.getMax(), Integer.MAX_VALUE);
        } catch (InvalidRepeatException ex) {
            fail();
        }

//        try {
//            new RepeatMarker("7+");
//            fail();
//        } catch (InvalidRepeatException ex) {
//        }
    }

    /**
     * Test of isInfinite method, of class RepeatMarker.
     */
    @Test
    public void testIsInfinite() {
        System.out.println("isInfinite");
        RepeatMarker instance;
        try {
            instance = new RepeatMarker("*");
            assertTrue(instance.isInfinite());
            instance = new RepeatMarker("33");
            assertFalse(instance.isInfinite());
        } catch (InvalidRepeatException ex) {
            Logger.getLogger(RepeatMarkerNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Test of numberOfInfiniteRepeats method, of class RepeatMarker.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        RepeatMarker instance = new RepeatMarker();
        int expResult = 0;
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

    }

    /**
     * Test of toString method, of class RepeatMarker.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        try {
            RepeatMarker instance = new RepeatMarker("111+");
            String expResult = "111+";
            String result = instance.toString();
            assertEquals(result, expResult);
        } catch (InvalidRepeatException ex) {
            Logger.getLogger(RepeatMarkerNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of toIrpString method, of class RepeatMarker.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        try {
            RepeatMarker instance = new RepeatMarker("3+");
            String expResult = "3+";
            String result = instance.toIrpString();
            assertEquals(result, expResult);
        } catch (InvalidRepeatException ex) {
            Logger.getLogger(RepeatMarkerNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getMin method, of class RepeatMarker.
     */
    @Test
    public void testGetMin() {
        try {
            System.out.println("getMin");
            RepeatMarker instance = new RepeatMarker("7*");
            int expResult = 7;
            int result = instance.getMin();
            assertEquals(result, expResult);
        } catch (InvalidRepeatException ex) {
            Logger.getLogger(RepeatMarkerNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//    /**
//     * Test of toElement method, of class RepeatMarker.
//     */
//    @Test
//    public void testToElement() {
//        System.out.println("toElement");
//        Document document = null;
//        RepeatMarker instance = new RepeatMarker();
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
