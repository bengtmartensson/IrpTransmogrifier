/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignalParser;
import org.harctoolbox.ircore.ProntoParser;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class RepeatFinderParserNGTest {
    public static final String NEC = "0000 006C 0028 0000 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4"
            + " 015B 0059 0016 0E6C"
            + " 015B 0055 0016 0E6C"
            + " 015B 0057 0016 0E6C";

    public RepeatFinderParserNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toIrSignal method, of class RepeatFinderParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignal() throws InvalidArgumentException {
        System.out.println("toIrSignal");
        List<IrSignalParser> parsers = new ArrayList<>(1);
        parsers.add(new ProntoParser(NEC));
        RepeatFinderParser instance = new RepeatFinderParser(parsers, NEC);
        IrSignal result = instance.toIrSignal();
        assertEquals(result.getRepeatLength(), 4);
    }

    /**
     * Test of toIrSignalClean method, of class RepeatFinderParser.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testToIrSignalClean() throws InvalidArgumentException {
        System.out.println("toIrSignalClean");
        List<IrSignalParser> parsers = new ArrayList<>(1);
        parsers.add(new ProntoParser(NEC));
        RepeatFinderParser instance = new RepeatFinderParser(parsers, NEC);
        IrSignal result = instance.toIrSignalClean();
        assertEquals(result.getRepeatLength(), 4);
    }
}
