/*
Copyright (C) 2019 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
 */
package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.Protocol;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class BiphaseWithTwoDurationsDecoderNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final IrSignal ortekMce;

    private final Analyzer ortek;
    private final Analyzer.AnalyzerParams paramsOrtek;

    public BiphaseWithTwoDurationsDecoderNGTest() throws OddSequenceLengthException, InvalidArgumentException {
        ortekMce = new IrSignal("+1920,-480,+480,-480,+480,-960,+480,-480,+960,-480,+480,-480,+480,-480,+480,-960,+960,-480,+480,-480,+480,-960,+480,-480,+480,-480,+480,-480,+960,-48480",
                "+1920,-480,+480,-480,+480,-960,+480,-480,+960,-960,+960,-480,+480,-960,+960,-480,+480,-480,+480,-960,+960,-480,+480,-480,+480,-960,+480,-48000",
                "+1920,-480,+480,-480,+480,-960,+480,-480,+960,-480,+480,-960,+960,-960,+960,-480,+480,-480,+480,-960,+960,-480,+480,-480,+480,-960,+480,-48000",
                38600.0, null);
        ortek = new Analyzer(ortekMce);
        List<Integer> widths = new ArrayList<>(12);
        widths.add(5);
        widths.add(2);
        widths.add(6);
        widths.add(4);
        widths.add(5);
        widths.add(2);
        widths.add(6);
        widths.add(4);
        widths.add(5);
        widths.add(2);
        widths.add(6);
        widths.add(4);
        paramsOrtek = new Analyzer.AnalyzerParams(38600d, null, BitDirection.lsb, true, widths, true);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of process method, of class BiphaseDecoder.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseOrtekMce() throws Exception {
        System.out.println("processOrtekMce");
        BiphaseWithTwoDurationsDecoder decoder = new BiphaseWithTwoDurationsDecoder(ortek, paramsOrtek);
        Protocol result = decoder.parse(true)[0];
        System.out.println("Expect warning for missing parameterspec");
        Protocol expResult = new Protocol("{38.6k,480,lsb}<1,-1|-1,1>(4,-1,A:5,B:2,C:6,D:4,^66m,(4,-1,E:5,F:2,G:6,H:4,^67m)*,(4,-1,I:5,J:2,K:6,L:4,^67m)){A=12,B=0,C=34,D=7,E=12,F=1,G=34,H=8,I=12,J=2,K=34,L=8}");
        assertEquals(result, expResult);
    }
}
