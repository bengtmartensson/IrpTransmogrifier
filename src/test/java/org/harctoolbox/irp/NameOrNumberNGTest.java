package org.harctoolbox.irp;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class NameOrNumberNGTest {

    private final GeneralSpec generalSpec;
    private final NameEngine nameEngine;
    private final NameOrNumber a;
    private final NameOrNumber b;

    public NameOrNumberNGTest() throws InvalidNameException, IrpInvalidArgumentException {
        generalSpec = new GeneralSpec("{40k,1000u}");
        nameEngine = new NameEngine("{A=123, B=73}");
        a = new NameOrNumber("A");
        b = new NameOrNumber("13.1415926");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toString method, of class NameOrNumber.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        assertEquals(a.toString(), "A");
        assertEquals(b.toString(), "13.1415926");
    }

    /**
     * Test of toFloat method, of class NameOrNumber.
     * @throws java.lang.Exception
     */
    @Test
    public void testToFloat() throws Exception {
        System.out.println("toFloat");
        assertTrue(IrCoreUtils.approximatelyEquals(a.toFloat(generalSpec, nameEngine), 123d, 0.000001, 0));
        assertTrue(IrCoreUtils.approximatelyEquals(b.toFloat(generalSpec, nameEngine), 13.1415926, 0.000001, 0));
    }

    /**
     * Test of toIrpString method, of class NameOrNumber.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        assertEquals(a.toIrpString(), "A");
        assertEquals(b.toIrpString(), "13.1415926");
    }

    /**
     * Test of toRawNumber method, of class NameOrNumber.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToRawNumber() throws InvalidArgumentException {
        System.out.println("toRawNumber");

        assertEquals(b.toRawNumber(), 13.1415926);
        try {
            a.toRawNumber();
            fail();
        } catch (InvalidArgumentException ex) {
        }
    }
}
