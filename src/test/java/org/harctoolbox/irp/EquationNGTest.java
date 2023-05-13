package org.harctoolbox.irp;

import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EquationNGTest {

    public EquationNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of solve method, of class Equation.
     * @throws org.harctoolbox.irp.NameUnassignedException
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testSolve() throws NameUnassignedException {
        System.out.println("solve");
        Expression lhs = Expression.newExpression("c*9/5 + 32");
        long rhs = 70L;
        Equation instance = new Equation(lhs, rhs);
        boolean success = instance.solve();
        assertTrue(success);
        long result = instance.getValue().getValue();
        long expResult = 21L;
        assertEquals(result, expResult);

    }

    /**
     * Test of solve method, of class Equation.
     * @throws org.harctoolbox.irp.NameUnassignedException
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testSolveFail() throws NameUnassignedException {
        System.out.println("solveFail");
        Expression lhs = Expression.newExpression("32 + c*9/5");
        long rhs = 70L;
        Equation instance = new Equation(lhs, rhs);
        boolean success = instance.solve();
        assertFalse(success);
    }

    /**
     * Test of solve method, of class Equation.
     * @throws org.harctoolbox.irp.NameUnassignedException
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.ParameterInconsistencyException
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testSolveGraham() throws NameUnassignedException, InvalidNameException, ParameterInconsistencyException {
        System.out.println("solve");
        Expression lhs = Expression.newExpression("H:1:3^H:1:2");
        BitwiseParameter rhs = new BitwiseParameter(0L, 0b1L);
        RecognizeData recognizeData = new RecognizeData();
        recognizeData.add("H", new BitwiseParameter(7L, 0b111L));
        Equation instance = new Equation(lhs, rhs, recognizeData);
        boolean success = instance.solve();

        assertTrue(success);
        BitwiseParameter result = instance.getValue();
        BitwiseParameter expResult = new BitwiseParameter(8L, 0b1000L);
        assertEquals(result, expResult);

    }

    /**
     * Test of solve method, of class Equation.
     * @throws org.harctoolbox.irp.NameUnassignedException
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.ParameterInconsistencyException
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testSolveGraham2() throws NameUnassignedException, InvalidNameException, ParameterInconsistencyException {
        System.out.println("solve");
        Expression lhs = Expression.newExpression("H:1:2^H:1:3");
        BitwiseParameter rhs = new BitwiseParameter(0L, 0b1L);
        RecognizeData recognizeData = new RecognizeData();
        recognizeData.add("H", new BitwiseParameter(7L, 0b111L));
        Equation instance = new Equation(lhs, rhs, recognizeData);
        boolean success = instance.solve();
        assertTrue(success);
        BitwiseParameter result = instance.getValue();
        BitwiseParameter expResult = new BitwiseParameter(0L, 0L);
        assertEquals(result, expResult);
    }
}
