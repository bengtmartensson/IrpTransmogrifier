package org.harctoolbox.irp;

import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class NameNGTest {

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
     * @throws org.harctoolbox.irp.InvalidNameException
     */
    @Test
    public void testToString_0args() throws InvalidNameException {
        System.out.println("toString");
        Name instance = new Name("godzilla");
        String expResult = "godzilla";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

//    /**
//     * Test of parseName method, of class Name.
//     * @throws org.harctoolbox.irp.InvalidNameException
//     */
//    @Test
//    public void testParseName() {
//        System.out.println("parseName");
//        String name = "May the Schwarz be with you";
//        String expResult = "May";
//        String result = Name.parse(name);
//        assertEquals(result, expResult);
//    }

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
     * Test of toLong method, of class Name.
     */
    @Test
    public void testToNumber_NameEngine() {
        try {
            System.out.println("toLong");
            NameEngine nameEngine = new NameEngine("{A = B * C, B = 2, C=3}");
            Name instance = new Name("A");
            long expResult = 6L;
            long result = instance.toLong(nameEngine);
            assertEquals(result, expResult);
        } catch (InvalidNameException | NameUnassignedException ex) {
            fail();
        }
    }

    /**
     * Test of toIrpString method, of class Name.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            Name instance = new Name("imhotep");
            assertEquals(instance.toIrpString(), "imhotep");
        } catch (InvalidNameException ex) {
            fail();
        }
    }

//    /**
//     * Test of parse method, of class Name.
//     */
//    @Test
//    public void testParse_String() {
//        System.out.println("parse");
//        String result = null;
////        try {
//            result = Name.parse("irscrutinizer");
////        } catch (InvalidNameException ex) {
////            Logger.getLogger(NameNGTest.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        assertEquals(result, "irscrutinizer");
//    }

    /**
     * Test of toFloat method, of class Name.
     */
    @Test
    public void testToFloat() {
        try {
            System.out.println("toFloat");
            NameEngine nameEngine = new NameEngine("{answer=42}");
            GeneralSpec generalSpec = new GeneralSpec();
            Name instance = new Name("answer");
            assertEquals(instance.toFloat(generalSpec, nameEngine), 42f, 0.000001);
        } catch (InvalidNameException | NameUnassignedException ex) {
            fail();
        }
    }
}
