package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AssignmentNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final NameEngine nameEngine;
    public AssignmentNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{answer=42, sheldon=73}");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class Assignment.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        assertEquals(Assignment.parse("x = answer*sheldon", nameEngine), 42*73);
    }

    /**
     * Test of toLong method, of class Assignment.
     */
    @Test
    public void testToNumber() {
        try {
            System.out.println("toLong");
            Assignment instance = new Assignment("x = answer*sheldon");
            long result = instance.toLong(nameEngine);
            assertEquals(result, 42*73);
        } catch (NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of getName method, of class Assignment.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        Assignment instance = new Assignment("x = answer*sheldon");
        String result = instance.getName();
        assertEquals(result, "x");
    }

    /**
     * Test of toString method, of class Assignment.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Assignment instance = new Assignment("x = answer*sheldon");
        String result = instance.toString();
        assertEquals(result, "x=(answer*sheldon)");
    }

    /**
     * Test of toIrpString method, of class Assignment.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        Assignment instance = new Assignment("x = answer*sheldon");
        String result = instance.toIrpString();
        assertEquals(result, "x=(answer*sheldon)");
    }

    /**
     * Test of evaluate method, of class Assignment.
     */
    @Test
    public void testEvaluate() {
        System.out.println("evaluate");
//        NameEngine nameEngine = null;
//        GeneralSpec generalSpec = null;
//        BitSpec bitSpec = null;
//        IrSignal.Pass pass = null;
//        double elapsed = 0.0;
//        Assignment instance = null;
//        EvaluatedIrStream expResult = null;
//        EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
//        assertEquals(result, expResult);
    }

    /**
     * Test of numberOfBits method, of class Assignment.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        Assignment instance = new Assignment("x = answer*sheldon");
        int result = instance.numberOfBits();
        assertEquals(result, 0);
    }

    /**
     * Test of numberOfBareDurations method, of class Assignment.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        Assignment instance = new Assignment("x = answer*sheldon");
        int result = instance.numberOfBareDurations(true);
        assertEquals(result, 0);
    }
}
