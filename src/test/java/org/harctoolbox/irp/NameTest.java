/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import org.testng.Assert;
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
public class NameTest {

    public NameTest() {
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
     * Test of toString method, of class Name.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    @Test
    public void testToString_0args() throws IrpSyntaxException {
        System.out.println("toString");
        Name instance = new Name("godzilla");
        String expResult = "godzilla";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of parseName method, of class Name.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    @Test
    public void testParseName() throws IrpSyntaxException {
        System.out.println("parseName");
        String name = "May the Schwarz be with you";
        String expResult = "May";
        String result = Name.parseName(name);
        assertEquals(result, expResult);
    }

    /**
     * Test of validName method, of class Name.
     */
    @Test
    public void testValidName() {
        System.out.println("validName");
        String name = "";
        boolean expResult = false;
        boolean result = Name.validName(name);
        Assert.assertTrue(Name.validName(" ksdjfk "));
        Assert.assertFalse(Name.validName(" 4ksdjfk "));
        Assert.assertTrue(Name.validName(" _4ksdjfk "));
        Assert.assertTrue(Name.validName("msb"));
        Assert.assertFalse(Name.validName("a@b"));
        Assert.assertFalse(Name.validName("May the force be with you"));
        Assert.assertEquals(result, expResult);
    }

    /**
     * Test of toNumber method, of class Name.
     * /
    @Test
    public void testToNumber_IrpParserNameContext_NameEngine() throws Exception {
        System.out.println("toNumber");
        IrpParser.NameContext ctx = null;
        NameEngine nameEngine = null;
        long expResult = 0L;
        long result = Name.toNumber(ctx, nameEngine);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Name.
     * /
    @Test
    public void testToString_IrpParserNameContext() {
        System.out.println("toString");
        IrpParser.NameContext ctx = null;
        String expResult = "";
        String result = Name.toString(ctx);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toInfixCode method, of class Name.
     * /
    @Test
    public void testToInfixCode() {
        System.out.println("toInfixCode");
        Name instance = null;
        String expResult = "";
        String result = instance.toInfixCode();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of toNumber method, of class Name.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.UnassignedException
     */
    @Test
    public void testToNumber_NameEngine() throws IrpSyntaxException, UnassignedException {
        System.out.println("toNumber");
        NameEngine nameEngine = new NameEngine("{A = B * C, B = 2, C=3}");
        Name instance = new Name("A");
        long expResult = 6L;
        long result = instance.toNumber(nameEngine);
        assertEquals(result, expResult);
    }
}
