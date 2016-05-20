/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
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
public class ExpressionNGTest {

    public ExpressionNGTest() {
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
     * Test of toNumber method, of class Expression.
     */
    @Test
    public void testToNumber() {
        System.out.println("toNumber");
        NameEngine nameEngine = null;
        try {
            nameEngine = new NameEngine("{A=12,B=3,C=2}");
        } catch (IrpSyntaxException ex) {
            fail();
        }
        try {
            assertEquals(new Expression("A+2*B*C").toNumber(nameEngine), 24);
        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            fail();
        }
        try {
            long result = new Expression("A+2*B*C+").toNumber(nameEngine);
            fail();
        } catch (ParseCancellationException | IrpSyntaxException ex) {
        } catch (UnassignedException | IncompatibleArgumentException ex) {
            fail();
        }

        try {
            assertEquals(new Expression("2**3").toNumber(), 8);
            assertEquals(new Expression("2**3**3").toNumber(), 134217728);
        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            fail();
        }
    }
}
