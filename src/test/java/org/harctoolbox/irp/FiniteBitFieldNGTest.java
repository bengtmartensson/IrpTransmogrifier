package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class FiniteBitFieldNGTest {

    private NameEngine nameEngine = null;
    private final FiniteBitField instance;

    public FiniteBitFieldNGTest() throws InvalidNameException {
        nameEngine = new NameEngine("{A = 7, F=244, D=4}");
        instance = new FiniteBitField("~D:-6:2");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toLong method, of class FiniteBitField.
     */
    @Test
    public void testToNumber() {
        System.out.println("toLong");
        try {

//            FiniteBitField fbf = new FiniteBitField("~D:-6:2");
//            assertEquals(instance.toLong(nameEngine), 31L);
//            FiniteBitField fbf = new FiniteBitField("~D:-6:2");
            assertEquals(instance.toLong(nameEngine), 31L);
        } catch (NameUnassignedException ex) {
            fail();
        }
        try {
            FiniteBitField fbf = new FiniteBitField("~foobar:-6:2");
            fbf.toLong(nameEngine);
            fail();
        } catch (NameUnassignedException ex) {
        }
    }

    /**
     * Test of toBinaryString method, of class FiniteBitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testToBinaryString_NameEngine_boolean() throws Exception {
        System.out.println("toBinaryString");
        //String result = instance.toBinaryString(nameEngine, false);
        //assertEquals(result, "011111");
        //((FiniteBitField fbf = new FiniteBitField("~D:-6:2");
        assertEquals(new FiniteBitField("~D:-6:2").toBinaryString(nameEngine, false), "011111");
    }

    /**
     * Test of toBinaryString method, of class FiniteBitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testToBinaryString_NameEngine() throws Exception {
        System.out.println("toBinaryString");
        String result = instance.toBinaryString(nameEngine);
        assertEquals(result, "011111");
    }

    /**
     * Test of getWidth method, of class FiniteBitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetWidth() throws Exception {
        System.out.println("getWidth");
        long result = instance.getWidth(nameEngine);
        assertEquals(result, 6);
    }

    /**
     * Test of toString method, of class FiniteBitField.
     * @throws java.lang.Exception
     */
    @Test
    public void testToString() throws Exception {
        System.out.println("toString");
        String result = instance.toString(nameEngine);
        assertEquals(result, "~4:-6:2");
    }

    /**
     * Test of toIrpString method, of class FiniteBitField.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, "~D:-6:2");
        FiniteBitField fbf = new FiniteBitField("X:Y");
        assertEquals(fbf.toIrpString(), "X:Y");
        fbf = new FiniteBitField("X:Y:Z");
        assertEquals(fbf.toIrpString(), "X:Y:Z");
        fbf = new FiniteBitField("X:Y");
        assertEquals(fbf.toIrpString(), "X:Y");
        fbf = new FiniteBitField("X:Y:0");
        assertEquals(fbf.toIrpString(), "X:Y");
    }

    @Test
    public void testConstructorWide() {
        System.out.println("testConstructorWide");
        FiniteBitField.setAllowLargeBitfields(false);
        try {
            FiniteBitField bt = new FiniteBitField("12:64:2");
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        FiniteBitField.setAllowLargeBitfields(true);
        FiniteBitField bt = new FiniteBitField("12:64:2");
    }

    /**
     * Test of numberOfBits method, of class FiniteBitField.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        int result = instance.numberOfBits();
        assertEquals(result, 6);
    }

    /**
     * Test of numberOfBareDurations method, of class FiniteBitField.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        int expResult = 0;
        int result = instance.numberOfBareDurations();
        assertEquals(result, expResult);
    }
}
