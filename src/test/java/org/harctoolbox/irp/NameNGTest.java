package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.testng.Assert;
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
public class NameNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public NameNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toString method, of class Name.
     *
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        Name instance = new Name("godzilla");
        String expResult = "godzilla";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of parseName method, of class Name.
     * @throws org.harctoolbox.irp.InvalidNameException
     */
    @Test
    public void testParseName() throws InvalidNameException {
        System.out.println("parseName");
        String name = "May the Schwarz be with you";
        String expResult = "May";
        String result = Name.parse(name);
        assertEquals(result, expResult);
    }

    /**
     * Test of validName method, of class Name.
     */
    @Test
    public void testValidName() {
        System.out.println("validName");
        Assert.assertFalse(Name.validName(""));
        Assert.assertTrue(Name.validName(" ksdjfk "));
        Assert.assertFalse(Name.validName(" 4ksdjfk "));
        Assert.assertTrue(Name.validName(" _4ksdjfk "));
        Assert.assertTrue(Name.validName("msb"));
        Assert.assertTrue(Name.validName("lsb"));
        Assert.assertTrue(Name.validName("k"));
        Assert.assertTrue(Name.validName("u"));
        Assert.assertTrue(Name.validName("p"));
        Assert.assertTrue(Name.validName("m"));
        Assert.assertFalse(Name.validName("a@b"));
        Assert.assertFalse(Name.validName("May the force be with you"));

    }

    /**
     * Test of toNumber method, of class Name.
     */
    @Test
    public void testToNumber_NameEngine() {
        try {
            System.out.println("toNumber");
            NameEngine nameEngine = new NameEngine("{A = B * C, B = 2, C=3}");
            Name instance = new Name("A");
            long expResult = 6L;
            long result = instance.toNumber(nameEngine);
            assertEquals(result, expResult);
        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            fail();
        }
    }

    /**
     * Test of toIrpString method, of class Name.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        Name instance = new Name("imhotep");
        assertEquals(instance.toIrpString(), "imhotep");
    }

    /**
     * Test of parse method, of class Name.
     */
    @Test
    public void testParse_String() {
        System.out.println("parse");
        String result = null;
        try {
            result = Name.parse("irscrutinizer");
        } catch (InvalidNameException ex) {
            Logger.getLogger(NameNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(result, "irscrutinizer");
    }

    /**
     * Test of toFloat method, of class Name.
     */
    @Test
    public void testToFloat() {
        try {
            System.out.println("toFloat");
            NameEngine nameEngine = new NameEngine("{answer=42}");
            GeneralSpec generalSpec = null;
            Name instance = new Name("answer");
            assertEquals(instance.toFloat(nameEngine, generalSpec), 42f, 0.000001);
        } catch (IrpSyntaxException | ArithmeticException | IncompatibleArgumentException | UnassignedException ex) {
            fail();
        }
    }
}
