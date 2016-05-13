/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class NumberWithDecimalsNGTest {

    public NumberWithDecimalsNGTest() {
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

    /**
     * Test of parseNumberWithDecimals method, of class NumberWithDecimals.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parseNumberWithDecimals");
        assertEquals(NumberWithDecimals.parse("73"), 73f, 0.0000001);
        assertEquals(NumberWithDecimals.parse("73.42"), 73.42, 0.0000001);
        assertEquals(NumberWithDecimals.parse(".73"), .73, 0.0000001);
    }
}
