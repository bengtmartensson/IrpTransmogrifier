/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.ircore;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class ModulatedIrSequenceNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ModulatedIrSequenceNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    /**
     * Test of clone method, of class ModulatedIrSequence.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        try {
            ModulatedIrSequence instance = new ModulatedIrSequence(new double[]{1, 3, 4, 5}, 12345d, 0.45);
            ModulatedIrSequence expResult = null;
            ModulatedIrSequence result = instance.clone();
            assertTrue(result.approximatelyEquals(instance));
        } catch (OddSequenceLengthException ex) {
            fail();
        }
    }
}
