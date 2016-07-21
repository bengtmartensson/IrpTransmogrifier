/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author bengt
 */
public class ParserDriverNGTest {

    public ParserDriverNGTest() {
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
     * Test of toStringTree method, of class ParserDriver.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
//    @Test
//    public void testToStringTree() throws IrpSyntaxException {
//        System.out.println("toStringTree");
//        String necIrp = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
//        ParserDriver instance = new ParserDriver(necIrp);
//        String result = instance.toStringTree();
//        String expResult = "(protocol (generalspec { (generalspec_list (generalspec_item (frequency_item (number_with_decimals (float_number 38 . 4)) k)) , (generalspec_item (unit_item (number_with_decimals (number 564))))) }) (bitspec_irstream (bitspec < (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 1)))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals (number 1))))))) | (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 1)))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals (number 3))))))) >) (irstream ( (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 16)))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals (number 8)))))) , (irstream_item (bitfield (primary_item (name D)) : (primary_item (number 8)))) , (irstream_item (bitfield (primary_item (name S)) : (primary_item (number 8)))) , (irstream_item (bitfield (primary_item (name F)) : (primary_item (number 8)))) , (irstream_item (bitfield ~ (primary_item (name F)) : (primary_item (number 8)))) , (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 1)))))) , (irstream_item (extent ^ (name_or_number (number_with_decimals (number 108))) m)) , (irstream_item (irstream ( (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 16)))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals (number 4)))))) , (irstream_item (duration (flash_duration (name_or_number (number_with_decimals (number 1)))))) , (irstream_item (extent ^ (name_or_number (number_with_decimals (number 108))) m))) ) (repeat_marker *)))) ))) (parameter_specs [ (parameter_spec (name D) : (number 0) .. (number 255)) , (parameter_spec (name S) : (number 0) .. (number 255) = (expression (expression (primary_item (number 255))) - (expression (primary_item (name D))))) , (parameter_spec (name F) : (number 0) .. (number 255)) ]))";
//        Assert.assertEquals(result, expResult);
//    }
}
