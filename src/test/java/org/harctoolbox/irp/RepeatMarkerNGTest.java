/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
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
public class RepeatMarkerNGTest {

    public RepeatMarkerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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

}
