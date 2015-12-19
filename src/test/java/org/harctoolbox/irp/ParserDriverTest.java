/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class ParserDriverTest {

    public ParserDriverTest() {
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
    @Test
    public void testToStringTree() throws IrpSyntaxException {
        System.out.println("toStringTree");
        String necIrp = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
        ParserDriver instance = new ParserDriver(necIrp);
        String result = instance.toStringTree();
        String expResult = "(protocol (generalspec { (generalspec_list (generalspec_item (frequency_item (number_with_decimals (float_number 38 . 4)) k)) , (generalspec_item (unit_item (number_with_decimals 564)))) }) (bitspec_irstream (bitspec < (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 1))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals 1)))))) | (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 1))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals 3)))))) >) (irstream ( (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 16))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals 8))))) , (irstream_item (bitfield (primary_item (name D)) : (primary_item (number 8)))) , (irstream_item (bitfield (primary_item (name S)) : (primary_item (number 8)))) , (irstream_item (bitfield (primary_item (name F)) : (primary_item (number 8)))) , (irstream_item (bitfield ~ (primary_item (name F)) : (primary_item (number 8)))) , (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 1))))) , (irstream_item (extent ^ (name_or_number (number_with_decimals 108)) m)) , (irstream_item (irstream ( (bare_irstream (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 16))))) , (irstream_item (duration (gap_duration - (name_or_number (number_with_decimals 4))))) , (irstream_item (duration (flash_duration (name_or_number (number_with_decimals 1))))) , (irstream_item (extent ^ (name_or_number (number_with_decimals 108)) m))) ) (repeat_marker *)))) ))) (parameter_specs [ (parameter_spec (name D) : 0 . . 255) , (parameter_spec (name S) : 0 . . 255 = (bare_expression (inclusive_or_expression (exclusive_or_expression (and_expression (shift_expression (additive_expression (multiplicative_expression (exponential_expression (unary_expression (primary_item (number 255))))) - (multiplicative_expression (exponential_expression (unary_expression (primary_item (name D)))))))))))) , (parameter_spec (name F) : 0 . . 255) ]))";
        System.out.println(result);
        Assert.assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getParser method, of class ParserDriver.
     * /
    @Test
    public void testGetParser() throws Exception {
        System.out.println("getParser");
        ParserDriver instance = null;
        IrpParser expResult = null;
        IrpParser result = instance.getParser();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of parseName method, of class ParserDriver.
     * /
    @Test
    public void testParseName() throws Exception {
        System.out.println("parseName");
        String name = "";
        String expResult = "";
        String result = ParserDriver.parseName(name);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of duration method, of class ParserDriver.
     * /
    @Test
    public void testDuration() {
        System.out.println("duration");
        ParserDriver instance = null;
        IrpParser.DurationContext expResult = null;
        IrpParser.DurationContext result = instance.duration();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of protocol method, of class ParserDriver.
     * /
    @Test
    public void testProtocol() throws Exception {
        System.out.println("protocol");
        ParserDriver instance = null;
        IrpParser.ProtocolContext expResult = null;
        IrpParser.ProtocolContext result = instance.protocol();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generalspec method, of class ParserDriver.
     * /
    @Test
    public void testGeneralspec() throws Exception {
        System.out.println("generalspec");
        ParserDriver instance = null;
        IrpParser.GeneralspecContext expResult = null;
        IrpParser.GeneralspecContext result = instance.generalspec();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parameterSpecs method, of class ParserDriver.
     * /
    @Test
    public void testParameterSpecs() throws Exception {
        System.out.println("parameterSpecs");
        ParserDriver instance = null;
        IrpParser.Parameter_specsContext expResult = null;
        IrpParser.Parameter_specsContext result = instance.parameterSpecs();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of expression method, of class ParserDriver.
     * /
    @Test
    public void testExpression() throws Exception {
        System.out.println("expression");
        ParserDriver instance = null;
        IrpParser.ExpressionContext expResult = null;
        IrpParser.ExpressionContext result = instance.expression();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of bare_expression method, of class ParserDriver.
     * /
    @Test
    public void testBare_expression() throws Exception {
        System.out.println("bare_expression");
        ParserDriver instance = null;
        IrpParser.Bare_expressionContext expResult = null;
        IrpParser.Bare_expressionContext result = instance.bare_expression();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of bitfield method, of class ParserDriver.
     * /
    @Test
    public void testBitfield() throws Exception {
        System.out.println("bitfield");
        ParserDriver instance = null;
        IrpParser.BitfieldContext expResult = null;
        IrpParser.BitfieldContext result = instance.bitfield();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of definitions method, of class ParserDriver.
     * /
    @Test
    public void testDefinitions() throws Exception {
        System.out.println("definitions");
        ParserDriver instance = null;
        IrpParser.DefinitionsContext expResult = null;
        IrpParser.DefinitionsContext result = instance.definitions();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserName_or_numberContext_NameEngine() throws Exception {
        System.out.println("parse");
        IrpParser.Name_or_numberContext ctx = null;
        org.harctoolbox.irp.NameEngine nameEngine = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx, nameEngine);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserNameContext_NameEngine() throws Exception {
        System.out.println("parse");
        IrpParser.NameContext ctx = null;
        org.harctoolbox.irp.NameEngine nameEngine = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx, nameEngine);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserNameContext() {
        System.out.println("parse");
        IrpParser.NameContext ctx = null;
        String expResult = "";
        String result = ParserDriver.parse(ctx);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserNumber_with_decimalsContext() {
        System.out.println("parse");
        IrpParser.Number_with_decimalsContext ctx = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserFloat_numberContext() {
        System.out.println("parse");
        IrpParser.Float_numberContext ctx = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserDotIntContext() {
        System.out.println("parse");
        IrpParser.DotIntContext ctx = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class ParserDriver.
     * /
    @Test
    public void testParse_IrpParserIntDotIntContext() {
        System.out.println("parse");
        IrpParser.IntDotIntContext ctx = null;
        double expResult = 0.0;
        double result = ParserDriver.parse(ctx);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
