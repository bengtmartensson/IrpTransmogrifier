package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.ircore.InvalidArgumentException;
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
    public ExpressionNGTest() throws IrpSyntaxException {
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
            assertEquals(new Expression("A+2*B*C").toNumber(nameEngine), 24);
        } catch (IrpSyntaxException | UnassignedException | InvalidArgumentException ex) {
            fail();
        }
        try {
            new Expression("A+2*B*C+").toNumber(nameEngine);
            fail();
        } catch (ParseCancellationException | IrpSyntaxException ex) {
        } catch (UnassignedException | InvalidArgumentException ex) {
            fail();
        }

        try {
            assertEquals(new Expression("2**3").toNumber(), 8);
            assertEquals(new Expression("2**3**3").toNumber(), 134217728);
        } catch (IrpSyntaxException | UnassignedException | InvalidArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of toString method, of class Expression.
     */
    @Test
    public void testToString() {
        try {
            System.out.println("toString");
            Expression instance = new Expression("A+2*B*C");
            String result = instance.toString();
            assertEquals(result, "(A+((2*B)*C))");
        } catch (IrpSyntaxException ex) {
            fail();
        }
    }

//    /**
//     * Test of toStringTree method, of class Expression.
//     */
//    @Test
//    public void testToStringTree_IrpParser() {
//        System.out.println("toStringTree");
//        IrpParser parser = null;
//        Expression instance = null;
//        String expResult = "";
//        String result = instance.toStringTree(parser);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of toStringTree method, of class Expression.
//     */
//    @Test
//    public void testToStringTree_0args() {
//        System.out.println("toStringTree");
//        Expression instance = null;
//        String expResult = "";
//        String result = instance.toStringTree();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber_0args() {
        System.out.println("toNumber");
        try {
            Expression instance = new Expression("A+2*B*C");
            try {
            instance.toNumber();
            fail();
        } catch (IrpSyntaxException | InvalidArgumentException ex) {
            fail();
        } catch (UnassignedException ex) {
        }
        } catch (IrpSyntaxException ex) {
            fail();
            Logger.getLogger(ExpressionNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber_NameEngine() {
        try {
            System.out.println("toNumber");
            Expression instance = new Expression("A+2*B*C");
            long result = instance.toNumber(nameEngine);
            assertEquals(result, 24);
        } catch (IrpSyntaxException | UnassignedException | InvalidArgumentException ex) {
            fail();
        }
    }

//    /**
//     * Test of getParseTree method, of class Expression.
//     */
//    @Test
//    public void testGetParseTree() {
//        System.out.println("getParseTree");
//        Expression instance = null;
//        IrpParser.ExpressionContext expResult = null;
//        IrpParser.ExpressionContext result = instance.getParseTree();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of toElement method, of class Expression.
//     */
//    @Test
//    public void testToElement() {
//        System.out.println("toElement");
//        Document document = null;
//        Expression instance = null;
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of toIrpString method, of class Expression.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            Expression instance = new Expression("A*#5+3*4");
            String result = instance.toIrpString();
            assertEquals(result, "((A*(#5))+(3*4))");
        } catch (IrpSyntaxException ex) {
            fail();
        }
    }
}
