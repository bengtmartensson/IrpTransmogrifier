package org.harctoolbox.irp;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IrStreamItemNGTest {

    public IrStreamItemNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of newIrStreamItem method, of class IrStreamItem.
     */
    @Test
    public void testNewIrStreamItem_String() {
        System.out.println("newIrStreamItem");
        IrStreamItem result = IrStreamItem.newIrStreamItem("42");
        assertTrue(result instanceof Flash);
        result = IrStreamItem.newIrStreamItem("answer=42");
        assertTrue(result instanceof Assignment);
        result = IrStreamItem.newIrStreamItem("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
        assertTrue(result instanceof IrStream);
        result = IrStreamItem.newIrStreamItem("<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
        assertTrue(result instanceof BitspecIrstream);
        System.out.println(result);
    }
}
