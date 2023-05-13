package org.harctoolbox.irp;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ExpressionNGTest {

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
     * Test of toLong method, of class Expression.
     */
    @Test
    public void testToNumber() {
        System.out.println("toLong");
        try {
            assertEquals(Expression.newExpression("A+2*B*C").toLong(nameEngine), 24);
        } catch (NameUnassignedException ex) {
            fail();
        }
        try {
            Expression.newExpression("A+2*B*C+").toLong(nameEngine);
            fail();
        } catch (ParseCancellationException ex) {
        } catch (NameUnassignedException ex) {
            fail();
        }

        try {
            assertEquals(Expression.newExpression("2**3").toLong(), 8);
            assertEquals(Expression.newExpression("2**3**3").toLong(), 134217728);
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
     * Test of toLong method, of class Expression.
     */
    @Test
    public void testToNumber_0args() {
        System.out.println("toLong");
        Expression instance = Expression.newExpression("A+2*B*C");
        try {
            instance.toLong();
            fail();
        } catch (NameUnassignedException ex) {
        }
    }

    /**
     * Test of toLong method, of class Expression.
     */
    @Test
    public void testToNumber_NameEngine() {
        try {
            System.out.println("toLong");
            Expression instance = Expression.newExpression("A+2*B*C");
            long result = instance.toLong(nameEngine);
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

    @Test
    public void testLongBitFieldExpression() throws InvalidNameException, NameUnassignedException {
        Expression expr = Expression.newExpression("D:S");
        NameEngine names = new NameEngine("{D=255,S=10000}");
        FiniteBitField.setAllowLargeBitfields(true);
        expr.toLong(names);
        FiniteBitField.setAllowLargeBitfields(false);
        try {
            expr.toLong(names);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testShortCircuiting() throws InvalidNameException, NameUnassignedException {
        System.out.println("testShortCircuiting");
        NameEngine nameEngine = new NameEngine("{A=1,B=0}");
        Expression exp = Expression.newExpression("A || C");
        exp.toLong(nameEngine);
        try {
            exp = Expression.newExpression("A && C");
            exp.toLong(nameEngine);
            fail();
        } catch (NameUnassignedException ex) {
        }
        exp = Expression.newExpression("A ? B : C");
        exp.toLong(nameEngine);

        try {
            exp = Expression.newExpression("!A ? B : C");
            exp.toLong(nameEngine);
            fail();
        } catch (NameUnassignedException ex) {
        }
        exp = Expression.newExpression("!A ? C : B");
        exp.toLong(nameEngine);
    }
}
