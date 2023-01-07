package org.harctoolbox.ircore;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IrSignalNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrSignalNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toString method, of class IrSignal.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @Test
    public void testToString_boolean() throws OddSequenceLengthException {
        System.out.println("toString");
        boolean alternatingSigns = true;
        IrSequence irSequence = new IrSequence("12 34 56 78");
        ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(irSequence, 38901.23);
        IrSignal instance = new IrSignal(modulatedIrSequence, modulatedIrSequence, irSequence, 33333.3);
        String expResult = "Freq=33333Hz[+12,-34,+56,-78][+12,-34,+56,-78][+12,-34,+56,-78]";
        String result = instance.toString(alternatingSigns);
        assertEquals(result, expResult);
    }
}
