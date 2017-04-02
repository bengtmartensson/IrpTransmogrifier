package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NameEngineNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final NameEngine instance;
    public NameEngineNGTest() throws InvalidNameException {
        instance = new NameEngine("{A=11,B=22,C=33,D=A-B,E=A-#(C-D),F=UINT8_MAX}");
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
        } catch (InvalidNameException ex) {
        }
    }

    @Test
    public void testDefineValid() {
        System.out.println("define valid");
        String name = "valid";
        long value = 73L;
        NameEngine names = new NameEngine();
        try {
            names.define(name, value);
            //System.out.println(instance.get(name));
            Assert.assertTrue(names.containsKey(name));
            Assert.assertEquals(names.toNumber(name), value);
        } catch (InvalidNameException | NameUnassignedException ex) {
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
        NameEngine names = new NameEngine(str);
        //instance.parseDefinitions(str);
        Assert.assertTrue(names.containsKey("answer"));
        Assert.assertTrue(names.containsKey("sheldon"));
        Assert.assertEquals(names.toNumber("diff"), 31);
    }

    /**
     * Test of define method, of class NameEngine.
     */
    @Test
    public void testDefine() {
        System.out.println("define");
        String name = "X";
        long value = 123L;
        NameEngine names = new NameEngine();
        try {
            names.define(name, value);
        } catch (InvalidNameException ex) {
            fail();
        }
        try {
            assertEquals(names.get(name).toNumber(), value);
        } catch (NameUnassignedException ex) {
            fail();
        }
        try {
            assertEquals(names.get("Z").toNumber(), value);
            fail();
        } catch (NameUnassignedException ex) {
        }
    }

    /**
     * Test of toNumber method, of class NameEngine.
     */
    @Test
    public void testToNumber() {
        System.out.println("toNumber");
        long result;
        try {
            result = instance.toNumber("F");
            assertEquals(result, 255);
        } catch (NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of containsKey method, of class NameEngine.
     */
    @Test
    public void testContainsKey() {
        System.out.println("containsKey");
        String name = "";
        boolean result = instance.containsKey(name);
        assertFalse(result);
        assertTrue(instance.containsKey("F"));
        assertFalse(instance.containsKey("Z"));
    }

    /**
     * Test of toString method, of class NameEngine.
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        String expResult = "{A=11,B=22,C=33,D=(A-B),E=(A-(#(C-D))),F=255}";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toIrpString method, of class NameEngine.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String expResult = "";
        String result = instance.toIrpString();
        assertEquals(result, "{A=11,B=22,C=33,D=(A-B),E=(A-(#(C-D))),F=255}");
    }

    /**
     * Test of parseLoose method, of class NameEngine.
     */
    @Test
    public void testParseLoose() {
        try {
            System.out.println("parseLoose");
            String str = "{D=12 ; ,F=64 S=34 ,X=78}";
            NameEngine expResult = new NameEngine("{D=12 ,F=64, S=34 ,X=78}");
            NameEngine result = NameEngine.parseLoose(str);
            assertEquals(result.toString(), expResult.toString());

            result = NameEngine.parseLoose("");
            assertEquals(result.toString(), "{}");
            result = NameEngine.parseLoose(null);
            assertEquals(result.toString(), "{}");
        } catch (InvalidNameException ex) {
            Logger.getLogger(NameEngineNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
