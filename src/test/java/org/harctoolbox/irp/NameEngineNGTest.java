package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class NameEngineNGTest {

    public NameEngineNGTest() {
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
     * Test of define method, of class NameEngine.
     */
    @Test
    public void testDefineInvalid() {
        System.out.println("define silly");
        String name = "In-valid";
        long value = 0L;
        NameEngine instance = new NameEngine();
        try {
            instance.define(name, value);
            Assert.fail("testDefine failed.");
        } catch (IrpSyntaxException ex) {
        }
    }

    @Test
    public void testDefineValid() {
        System.out.println("define valid");
        String name = "valid";
        long value = 73L;
        NameEngine instance = new NameEngine();
        try {
            instance.define(name, value);
            //System.out.println(instance.get(name));
            Assert.assertTrue(instance.containsKey(name));
            Assert.assertEquals(instance.toNumber(name), value);
        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            Assert.fail("testDefine valid failed.");
        }
    }

    /**
     * Test of parseDefinitions method, of class NameEngine.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseDefinitions_String() throws Exception {
        System.out.println("parseDefinitions");
        String str = "{answer = 42, sheldon = 73, diff = sheldon - answer}";
        NameEngine instance = new NameEngine(str);
        //instance.parseDefinitions(str);
        Assert.assertTrue(instance.containsKey("answer"));
        Assert.assertTrue(instance.containsKey("sheldon"));
        Assert.assertEquals(instance.toNumber("diff"), 31);
    }

//    /**
//     * Test of define method, of class NameEngine.
//     */
//    @Test
//    public void testDefine() throws Exception {
//        System.out.println("define");
//        String name = "";
//        long value = 0L;
//        NameEngine instance = new NameEngine();
//        instance.define(name, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of parseDefinitions method, of class NameEngine.
//     */
//    @Test
//    public void testParseDefinitions_IrpParserDefinitionsContext() throws Exception {
//        System.out.println("parseDefinitions");
//        IrpParser.DefinitionsContext ctx = null;
//        NameEngine instance = new NameEngine();
//        instance.parseDefinitions(ctx);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of get method, of class NameEngine.
//     */
//    @Test
//    public void testGet() throws Exception {
//        System.out.println("get");
//        String name = "";
//        NameEngine instance = new NameEngine();
//        Expression expResult = null;
//        Expression result = instance.get(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toNumber method, of class NameEngine.
//     */
//    @Test
//    public void testToNumber() throws Exception {
//        System.out.println("toNumber");
//        String name = "";
//        NameEngine instance = new NameEngine();
//        long expResult = 0L;
//        long result = instance.toNumber(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of containsKey method, of class NameEngine.
//     */
//    @Test
//    public void testContainsKey() {
//        System.out.println("containsKey");
//        String name = "";
//        NameEngine instance = new NameEngine();
//        boolean expResult = false;
//        boolean result = instance.containsKey(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class NameEngine.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        IrpParser parser = null;
//        NameEngine instance = new NameEngine();
//        String expResult = "";
//        String result = instance.toString(parser);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of notationString method, of class NameEngine.
//     */
//    @Test
//    public void testNotationString() throws Exception {
//        System.out.println("notationString");
//        String equals = "";
//        String separator = "";
//        NameEngine instance = new NameEngine();
//        String expResult = "";
//        String result = instance.notationString(equals, separator);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toElement method, of class NameEngine.
//     */
//    @Test
//    public void testToElement() {
//        System.out.println("toElement");
//        Document document = null;
//        NameEngine instance = new NameEngine();
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
