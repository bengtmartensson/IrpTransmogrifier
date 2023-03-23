package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrSignal;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IrStreamNGTest {

    private final static String NEC1IRP = "(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)";
    private final static String NEC1INTRO = "16,-8,D:8,S:8,F:8,~F:8,1,^108m";
    private final static String NEC1REPEAT = "(16,-4,1,^108m)*";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final IrStream instance;
    private final IrStream repeat;

    public IrStreamNGTest() {
        instance = new IrStream(NEC1IRP);
        repeat = new IrStream(NEC1REPEAT);
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getRepeatMarker method, of class IrStream.
     */
    @Test
    public void testGetRepeatMarker() {
        System.out.println("getRepeatMarker");
        RepeatMarker result = instance.getRepeatMarker();
        assertEquals(result.getMin(), 1);
        assertEquals(result.getMax(), 1);
        result = repeat.getRepeatMarker();
        assertEquals(result.getMin(), 0L);
        assertTrue(result.isInfinite());
    }

    /**
     * Test of evaluate method, of class IrStream.
     */
    @Test
    public void testEvaluate() {

        //EvaluatedIrStream result = instance.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
    }

    /**
     * Test of toString method, of class IrStream.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = instance.toString();
        assertEquals(result, NEC1IRP);
    }

    /**
     * Test of toIrpString method, of class IrStream.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = instance.toIrpString();
        assertEquals(result, NEC1IRP);
    }

    /**
     * Test of isRepeatSequence method, of class IrStream.
     */
    @Test
    public void testIsRepeatSequence() {
        System.out.println("isRepeatSequence");
        boolean result = instance.isRepeatSequence();
        assertFalse(result);
    }

    /**
     * Test of hasRepeatSequence method, of class IrStream.
     */
    @Test
    public void testHasRepeatSequence() {
        System.out.println("isRepeatSequence");
        boolean result = instance.hasRepeatSequence();
        assertTrue(result);
    }

    /**
     * Test of numberOfBareDurations method, of class IrStream.
     */
    @Test
    public void testNumberOfBareDurations() {
        System.out.println("numberOfBareDurations");
        int result = instance.numberOfBareDurations();
        assertEquals(result, 8);
    }

    /**
     * Test of numberOfBits method, of class IrStream.
     */
    @Test
    public void testNumberOfBits() {
        System.out.println("numberOfBits");
        int result = instance.numberOfBits();
        assertEquals(result, 32);
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class IrStream.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, 1);
    }

    @Test
    public void testExtractPassNec1() {
        System.out.println("extractPassNec1");
        IrStream irStream;
        BareIrStream result;

        irStream = new IrStream(NEC1IRP);
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), NEC1INTRO);
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "16,-4,1,^108m");
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "");
    }

    @Test
    public void testExtractPassNec1Ending() {
        System.out.println("extractPassNec1Ending");
        IrStream irStream;
        BareIrStream result;

        irStream = new IrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)+,123,-456)");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), "16,-8,D:8,S:8,F:8,~F:8,1,^108m,16,-4,1,^108m");
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "16,-4,1,^108m");
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "123,-456");
    }

    @Test
    public void testExtractPassThing() {
        System.out.println("extractPassThing");
        IrStream irStream;
        BareIrStream result;


        //irStream = new IrStream("([11][22][33],-100)+");
        irStream = new IrStream("(11,-100)*");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertTrue(result.isEmpty());

        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "11,-100");


        irStream = new IrStream("(11,-100)+");

        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), "11,-100");

        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "11,-100");

        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "");

        irStream = new IrStream("([11][22][33],-100)+");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), "11,-100");
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "22,-100");
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "33,-100");

        irStream = new IrStream("(111,-222,[11][][33],-100)+");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), "111,-222,11,-100");
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "111,-222");
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "111,-222,33,-100");
    }

    @Test
    public void testExtractPassAnthem() {
        System.out.println("extractPassAnthem");
        IrStream irStream;
        BareIrStream result;

        irStream = new IrStream("((8000u,-4000u,D:8,S:8,F:8,C:8,1,-25m)2,8000u,-4000u,D:8,S:8,F:8,C:8,1,-100)*");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertTrue(result.isEmpty());
        result = irStream.extractPass(IrSignal.Pass.repeat);
        System.out.println(result.toString());
        String expected = "8000u,-4000u,D:8,S:8,F:8,C:8,1,-25m,8000u,-4000u,D:8,S:8,F:8,C:8,1,-25m,8000u,-4000u,D:8,S:8,F:8,C:8,1,-100";
        System.out.println(expected.equals(result.toString()));
        assertEquals(result.toString(), expected);
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractPassSean() {
        System.out.println("extractPassSean");
        IrStream irStream;
        BareIrStream result;

        irStream = new IrStream("(<1|3>(16m,-5m)+)");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertEquals(result.toString(), "<1|3>(16m,-5m)");
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "<1|3>(16m,-5m)");
    }

    @Test
    public void testExtractPassZaptor() {
        System.out.println("extractPassZaptor");
        IrStream irStream;
        BareIrStream result;

        irStream = new IrStream("([][T=0][T=1],8,-6,2,D:8,T:1,S:7,F:8,E:4,C:4,-74m)*{C = (D:4+D:4:4+S:4+S:3:4+8*T+F:4+F:4:4+E)&15}");
        result = irStream.extractPass(IrSignal.Pass.intro);
        assertTrue(result.isEmpty());
        result = irStream.extractPass(IrSignal.Pass.repeat);
        assertEquals(result.toString(), "T=0,8,-6,2,D:8,T:1,S:7,F:8,E:4,C:4,-74m");
        result = irStream.extractPass(IrSignal.Pass.ending);
        assertEquals(result.toString(), "T=1,8,-6,2,D:8,T:1,S:7,F:8,E:4,C:4,-74m");
    }
}
