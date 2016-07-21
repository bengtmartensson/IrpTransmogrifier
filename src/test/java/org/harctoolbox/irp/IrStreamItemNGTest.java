/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class IrStreamItemNGTest {

    public IrStreamItemNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    /**
     * Test of evaluate method, of class IrStreamItem.
     * @throws java.lang.Exception
     */
    @Test
    public void testEvaluate() throws Exception {
        System.out.println("evaluate");
        BitSpec bitSpec = null;
        //IrStreamItem instance = newIrStreamItem("10");

        //assertEquals(result, expResult);

    }

    /**
     * Test of newIrStreamItem method, of class IrStreamItem.
     */
    @Test
    public void testNewIrStreamItem_String() {
        try {
            System.out.println("newIrStreamItem");
            String str = "";
            IrStreamItem expResult = null;
            IrStreamItem result = IrStreamItem.newIrStreamItem("42");
            assertTrue(result instanceof Flash);
            result = IrStreamItem.newIrStreamItem("answer=42");
            assertTrue(result instanceof Assignment);
            result = IrStreamItem.newIrStreamItem("(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
            assertTrue(result instanceof IrStream);
            result = IrStream.newIrStreamItem("<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
            assertTrue(result instanceof BitspecIrstream);
            System.out.println(result);
        } catch (IrpSyntaxException | InvalidRepeatException ex) {
            Logger.getLogger(IrStreamItemNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
