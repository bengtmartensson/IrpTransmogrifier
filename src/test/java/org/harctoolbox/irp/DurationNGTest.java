package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DurationNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public DurationNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of newDuration method, of class Duration.
     */
    @Test
    public void testNewDuration_3args() {
        try {
            System.out.println("newDuration");
            NameEngine nameEngine = new NameEngine("{A = 150}");
            GeneralSpec generalSpec = new GeneralSpec("{40k, 200}");
            Duration result = Duration.newDuration("15p");
            assertTrue(result instanceof Flash);
            assertEquals(result.toFloat(nameEngine, generalSpec), 375f, 0.0001);

            result = Duration.newDuration("-1m");
            assertTrue(result instanceof Gap);
            assertEquals(result.toFloat(nameEngine, generalSpec), 1000f, 0.0001);

            result = Duration.newDuration("3");
            assertTrue(result instanceof Flash);
            assertEquals(result.toFloat(nameEngine, generalSpec), 600f, 0.0001);

            result = Duration.newDuration("A u");
            assertTrue(result instanceof Flash);
            assertEquals(result.toFloat(nameEngine, generalSpec), 150f, 0.0001);

            result = Duration.newDuration("-A");
            assertTrue(result instanceof Gap);
            assertEquals(result.toFloat(nameEngine, generalSpec), 30000f, 0.0001);

            result = Duration.newDuration("-20m");
            assertTrue(result instanceof Gap);
            assertEquals(result.toFloat(nameEngine, generalSpec), 20000f, 0.0001);

            result = Duration.newDuration("^A");
            assertTrue(result instanceof Extent);
            assertEquals(result.toFloat(nameEngine, generalSpec), 30000f, 0.0001);

            result = Duration.newDuration("^A p");
            assertTrue(result instanceof Extent);
            assertEquals(result.toFloat(nameEngine, generalSpec), 3750f, 0.0001);
        } catch (IrpSyntaxException | IrpSemanticException | ArithmeticException | IncompatibleArgumentException | UnassignedException ex) {
            fail();
        }
    }
}
