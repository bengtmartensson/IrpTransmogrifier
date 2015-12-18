package org.harctoolbox.irp;

import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class NameEngineTest {

    public NameEngineTest() {
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
            fail("testDefine failed.");
        } catch (IrpSyntaxException ex) {
        }
    }

    @Test
    public void testDefineValid() {
        System.out.println("define valid");
        String name = "valid";
        long value = 0L;
        NameEngine instance = new NameEngine();
        try {
            instance.define(name, value);
            System.out.println(instance.get(name).toStringTree());
            assertTrue(instance.containsKey(name));
        } catch (IrpSyntaxException ex) {
            fail("testDefine valid failed.");
        }
    }

    /**
     * Test of parseDefinitions method, of class NameEngine.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseDefinitions_String() throws Exception {
        System.out.println("parseDefinitions");
        String str = "{answer = 42, sheldon = 73}";
        NameEngine instance = new NameEngine(str);
        //instance.parseDefinitions(str);
        assertTrue(instance.containsKey("answer"));
        assertTrue(instance.containsKey("sheldon"));
    }

    /**
     * Test of parseDefinitions method, of class NameEngine.
     * /
    @Test
    public void testParseDefinitions_IrpParserDefinitionsContext() throws Exception {
        System.out.println("parseDefinitions");
        IrpParser.DefinitionsContext ctx = null;
        NameEngine instance = new NameEngine();
        instance.parseDefinitions(ctx);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class NameEngine.
     * /
    @Test
    public void testGet() {
        System.out.println("get");
        String name = "";
        NameEngine instance = new NameEngine();
        IrpParser.Bare_expressionContext expResult = null;
        IrpParser.Bare_expressionContext result = instance.get(name);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of evaluate method, of class NameEngine.
     * /
    @Test
    public void testEvaluate() throws Exception {
        System.out.println("evaluate");
        String name = "";
        NameEngine instance = new NameEngine();
        String expResult = "";
        IrpParser.Bare_expressionContext result = instance.evaluate(name);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of containsKey method, of class NameEngine.
     * /
    @Test
    public void testContainsKey() {
        System.out.println("containsKey");
        String name = "";
        NameEngine instance = new NameEngine();
        boolean expResult = false;
        boolean result = instance.containsKey(name);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class NameEngine.
     * /
    @Test
    public void testToString() {
        System.out.println("toString");
        NameEngine instance = new NameEngine();
        String expResult = "";
        String result = instance.toString();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of notationString method, of class NameEngine.
     * /
    @Test
    public void testNotationString() {
        System.out.println("notationString");
        String equals = "";
        String separator = "";
        NameEngine instance = new NameEngine();
        String expResult = "";
        String result = instance.notationString(equals, separator);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    private void AssertTrue(boolean containsKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
}
