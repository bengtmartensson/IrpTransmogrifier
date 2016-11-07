package org.harctoolbox.irp;

import java.io.IOException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
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

    /**
     * Test of code method, of class Duration.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws java.io.IOException
     */
    @Test
//    public void testCode() throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException {
//        GeneralSpec generalSpec = new GeneralSpec("{100u,38k}");
//        System.out.println("code");
//        STGroup stGroup = new STGroupFile("src/main/st/c.stg");
//        String result = new Flash("123").code(true, generalSpec, stGroup);
//        assertEquals(result, "flash(12300)");
//
//        result = new Flash("123p").code(true, generalSpec, stGroup);
//        assertEquals(result, "flash(3237)");
//
//        result = new Flash("108m").code(true, generalSpec, stGroup);
//        assertEquals(result, "flash(108000)");
//
//        result = new Flash("123u").code(true, generalSpec, stGroup);
//        assertEquals(result, "flash(123)");
//
//        result = new Flash("A p").code(true, generalSpec, stGroup);
//        assertEquals(result, "flash(A*26.315789473684212)");
//    }
    public void testCode() throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, IOException {
        GeneralSpec generalSpec = new GeneralSpec("{100u,38k}");
        CodeGenerator codeGenerator = new STCodeGenerator("c", generalSpec);
        System.out.println("code");
        //STGroup stGroup = new STGroupFile("src/main/st/c.stg");
        Flash flash = new Flash("123");
        String result = flash.code(null, IrSignal.Pass.intro, codeGenerator);
        assertEquals(result, "flash(12300)");

        result = new Flash("123p").code(null, null, codeGenerator);
        assertEquals(result, "flash(3237)");

        result = new Flash("108m").code(null, null, codeGenerator);
        assertEquals(result, "flash(108000)");

        result = new Flash("123u").code(null, null, codeGenerator);
        assertEquals(result, "flash(123)");

        result = new Flash("A p").code(null, null, codeGenerator);
        assertEquals(result, "flash(A*26.315789473684212)");
    }

//    public class DurationImpl extends Duration {
//
//        public DurationImpl() {
//            super(0.0);
//        }
//
//        public double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
//            return 0.0;
//        }
//
//        public boolean isOn() {
//            return false;
//        }
//    }
}
