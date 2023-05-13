package org.harctoolbox.irp;

import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ParameterSpecsNGTest {

    private final ParameterSpecs rc5;
    private final ParameterSpecs nec1;

    public ParameterSpecsNGTest() {
        rc5 = new ParameterSpecs("[T@:0..1=0,D:0..31,F:0..127]");
        nec1 = new ParameterSpecs("[D:0..255,S:0..255=255-D,F:0..255]");
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of isEmpty method, of class ParameterSpecs.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        ParameterSpecs instance = new ParameterSpecs();
        boolean result = instance.isEmpty();
        assertTrue(result);
        assertFalse(rc5.isEmpty());
    }

    /**
     * Test of toString method, of class ParameterSpecs.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String result = rc5.toString();
        assertEquals(result, "[T@:0..1=0,D:0..31,F:0..127]");
    }

    /**
     * Test of toIrpString method, of class ParameterSpecs.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = nec1.toIrpString();
        assertEquals(result, "[D:0..255,S:0..255=(255-D),F:0..255]");
        result = rc5.toIrpString();
        assertEquals(result, "[T@:0..1=0,D:0..31,F:0..127]");
        ParameterSpecs empty = new ParameterSpecs();
        result = empty.toIrpString();
        assertEquals(result, "");
    }

    /**
     * Test of hasNonStandardParameters method, of class ParameterSpecs.
     */
    @Test
    public void testHasNonStandardParameters() {
        System.out.println("hasNonStandardParameters");
        assertFalse(rc5.hasNonStandardParameters());
        assertFalse(nec1.hasNonStandardParameters());
        ParameterSpecs instance = new ParameterSpecs("[D:0..255,S:0..255,Z:0..255]");
        assertTrue(instance.hasNonStandardParameters());
    }

    /**
     * Test of compare method, of class ParameterSpecs.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");
        ParameterSpecs instance = rc5;
        int result = instance.compare("january", "february");
        assertTrue(result > 0);
        result = instance.compare("D", "F");
        assertTrue(result < 0);
        result = instance.compare("F", "F");
        assertTrue(result == 0);
    }

    /**
     * Test of sort method, of class ParameterSpecs.
     */
    @Test
    public void testSort() {
        System.out.println("sort");
        Map<String, Long> unsortedMap = new HashMap<>(8);
        unsortedMap.put("january", 1L);
        unsortedMap.put("february", 2L);
        unsortedMap.put("F", 3L);
        unsortedMap.put("D", 4L);
        unsortedMap.put("T", 0L);
        Map<String, Long> sortedMap = rc5.sort(unsortedMap);
        String[] expResult = new String []{ "T", "D", "F", "february", "january" };
        String[] result = sortedMap.keySet().toArray(new String[5]);
        assertEquals(result, expResult);
    }
}
