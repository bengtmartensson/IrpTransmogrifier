package org.harctoolbox.irp;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BareIrStreamNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public BareIrStreamNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class BareIrStream.
     */
    @Test
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        BareIrStream instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
        int expResult = 1;
        int result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

        instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m))");
        expResult = 0;
        result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

        instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,((16,-4)*,1,^108m)*)");
        expResult = 2;
        result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

        instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,((16,-4)*,1,^108m))");
        expResult = 1;
        result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

        instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(<1,-3>(16,-4)*,1,^108m))");
        expResult = 1;
        result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);

        instance = new BareIrStream("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*,(16,-4,1,^108m)*)");
        expResult = 2;
        result = instance.numberOfInfiniteRepeats();
        assertEquals(result, expResult);
    }
}
