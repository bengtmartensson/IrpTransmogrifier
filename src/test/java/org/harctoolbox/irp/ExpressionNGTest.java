package org.harctoolbox.irp;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExpressionNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final NameEngine nameEngine;
    public ExpressionNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{A=12,B=3,C=2}");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber() {
        System.out.println("toNumber");
        try {
            assertEquals(Expression.newExpression("A+2*B*C").toNumber(nameEngine), 24);
        } catch (NameUnassignedException ex) {
            fail();
        }
        try {
            Expression.newExpression("A+2*B*C+").toNumber(nameEngine);
            fail();
        } catch (ParseCancellationException ex) {
        } catch (NameUnassignedException ex) {
            fail();
        }

        try {
            assertEquals(Expression.newExpression("2**3").toNumber(), 8);
            assertEquals(Expression.newExpression("2**3**3").toNumber(), 134217728);
        } catch (NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of toString method, of class Expression.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Expression instance = Expression.newExpression("A+2*B*C");
        String result = instance.toString();
        assertEquals(result, "(A+((2*B)*C))");
    }

    /**
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber_0args() {
        System.out.println("toNumber");
        Expression instance = Expression.newExpression("A+2*B*C");
        try {
            instance.toNumber();
            fail();
        } catch (NameUnassignedException ex) {
        }
    }

    /**
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber_NameEngine() {
        try {
            System.out.println("toNumber");
            Expression instance = Expression.newExpression("A+2*B*C");
            long result = instance.toNumber(nameEngine);
            assertEquals(result, 24);
        } catch (NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of toIrpString method, of class Expression.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        Expression instance = Expression.newExpression("A*#5+3*4");
        String result = instance.toIrpString();
        assertEquals(result, "((A*(#5))+(3*4))");
    }

    @Test
    public void testLong() {
        System.out.println("testLong");
        long answer = 42;
        Expression instance = new NumberExpression(answer);
        String result = instance.toIrpString();
        assertEquals("42", result);
    }
}
