package org.harctoolbox.irp;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.Decoder.Decode;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DecoderNGTest {
    private final static double NRC17_FREQUENCY = 38000d;
    private final static double RC5_FREQUENCY = 36000d;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Decoder decoder;
    private final IrSequence nrc17Intro;
    private final IrSequence nrc17Repeat;
    private final IrSequence nrc17Ending;
    private final IrSequence rc5Seq;
    private final NameEngine nrc17NameEngine;
    private final NameEngine rc5NameEngine;
    private final NameEngine rc5NameEngineWithDefault;
    private final IrSequence nec1Intro;
    private final IrSequence nec1Repeat;
    private final IrSignal giCable;

    public DecoderNGTest() throws Exception {
        nrc17Intro  = new IrSequence(new int[]{500, 2500, 500, 1000, 1000, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 14500, 500, 2500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 1000, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 500, 110000});
        nrc17Repeat = new IrSequence(new int[]{500, 2500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 1000, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 500, 110000});
        nrc17Ending = new IrSequence(new int[]{500, 2500, 500, 1000, 1000, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 100500});
        nrc17NameEngine = new NameEngine("{D=12,F=56}");
        nec1Intro = new IrSequence(new int[]{9024, 4512, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 1692, 564, 564, 564, 564, 564, 564, 564, 1692, 564, 1692, 564, 39756});
        nec1Repeat = new IrSequence(new int[]{9041, 2267, 573, 96193});
        rc5Seq = new IrSequence(new int[]{889, 889, 1778, 889, 889, 1778, 889, 889, 1778, 889, 889, 1778, 889, 889, 889, 889, 1778, 889, 889, 889, 889, 90886});
        rc5NameEngine = new NameEngine("{D=12,F=56}");
        rc5NameEngineWithDefault = new NameEngine("{D=12,F=56,T=0}");
        giCable = Pronto.parse("0000 006D 001A 0000 0157 00AC 0013 0055 0013 00AC 0013 0055 0013 00AC 0013 00AC 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 00AC 0013 0055 0013 00AC 0013 0055 0013 0498 0157 0055 0013 0D24 0157 0055 0013 0D24 0157 0055 0013 0D24 0157 0055 0013 1365");

        decoder = new Decoder();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private boolean testDecode(long seed) {
        return testDecode(new Random(seed));
    }

    private boolean testDecode(Random random) {
        Decoder.DecoderParameters params = new Decoder.DecoderParameters();
        params.setFrequencyTolerance(1.0);
        params.setAllDecodes(true);
        try {
            for (NamedProtocol protocol : decoder.getParsedProtocols()) {
                NameEngine nameEngine = new NameEngine(protocol.randomParameters(random));
                IrSignal irSignal = protocol.toIrSignal(nameEngine);
                Map<String, Decode> decodes = decoder.decodeIrSignal(irSignal, params);
                boolean success = false;
                for (Decode decode : decodes.values()) {
                    System.out.println(decode);
                    if (decode.same(protocol.getName(), nameEngine)) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    System.out.println(">>>>>>>>> " + protocol.getName() + "\t" + nameEngine.toString());
                    decodes.values().forEach((decode) -> {
                        System.out.println("----------------> " + decode.toString());
                    });

                    return false;
                }
            }
            return true;
        } catch (DomainViolationException | NameUnassignedException | IrpInvalidArgumentException | InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    @Test(enabled = true)
    public void testDecode() throws InvalidArgumentException, Pronto.NonProntoFormatException {
        System.out.println("decode");
        Decoder.setDebugProtocolRegExp("^nec[0-9]$");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C");
        Decoder.DecoderParameters params = new Decoder.DecoderParameters();
        params.setRemoveDefaultedParameters(false);
        Map<String, Decode> result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 1);
        assertEquals(result.get("NEC1").toString(), "NEC1: {D=12,F=35,S=243}");
        Decoder.setDebugProtocolRegExp(null);
        params.setRemoveDefaultedParameters(true);
        result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 1);
        assertEquals(result.get("NEC1").toString(), "NEC1: {D=12,F=35}");
        params.setAllDecodes(true);
        result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 3);
    }

    @Test(enabled = true)
    public void testDecodeGICable() {
        System.out.println("decodeGICable");
        Decoder.setDebugProtocolRegExp("g.i.cable");
        Decoder.DecoderParameters params = new Decoder.DecoderParameters();
        Map<String, Decode> result = decoder.decodeIrSignal(giCable, params);
        assertEquals(result.size(), 0);
        ModulatedIrSequence seq = giCable.toModulatedIrSequence();
        Decoder.DecodeTree decodes = decoder.decode(seq, params);
        assertEquals(decodes.size(), 1);
    }

    /**
     * Test of decode method, of class Decoder.
     */
    @Test(enabled = true)
    public void testDecode_String() {
        System.out.println("decode");
        assertTrue(testDecode(12345));
    }

    /**
     * Test of decode method, of class Decoder.
     * @throws java.lang.Exception
     */
    @Test
    public void testDecode_8args_ModulatedIrSequenceNrc17() throws Exception {
        System.out.println("decode_8args_ModulatedIrSequenceNrc17");
        ModulatedIrSequence irSequence = new ModulatedIrSequence(IrSequence.concatenate(nrc17Intro, nrc17Repeat, nrc17Repeat, nrc17Ending), NRC17_FREQUENCY);
        boolean strict = true;
        boolean allDecodes = true;
        boolean removeDefaultedParameters = false;
        boolean recursive = true;
        Double frequencyTolerance = null;
        Double absoluteTolerance = null;
        Double relativeTolerance = null;
        Double minimumLeadout = null;
        Decoder instance = new Decoder("NRC17");
        Decoder.DecoderParameters params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        Decoder.DecodeTree result = instance.decode(irSequence, params);
        Decode first = result.getDecode(0);
        assertTrue(nrc17NameEngine.numericallyEquals(first));
        assertEquals(first.getBegPos(), 0);
        assertEquals(first.getEndPos(), 163);
        assertEquals(first.getNumberOfRepetitions(), 3);

        irSequence = new ModulatedIrSequence(nrc17Intro, NRC17_FREQUENCY);
        result = instance.decode(irSequence, params);
        assertEquals(result.size(), 0);

        strict = false;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = instance.decode(irSequence, params);
        assertEquals(result.size(), 1);
        first = result.getDecode(0);
        assertTrue(nrc17NameEngine.numericallyEquals(first));
    }

    /**
     * Test of decode method, of class Decoder.
     * @throws org.harctoolbox.irp.IrpParseException
     * @throws java.io.IOException
     */
    @Test
    public void testDecode_8args_ModulatedIrSequence() throws IrpParseException, IOException {
        System.out.println("decode_8args_ModulatedIrSequence");
        ModulatedIrSequence irSequence = new ModulatedIrSequence(IrSequence.concatenate(nec1Intro, nec1Repeat, nec1Repeat, rc5Seq), 37000d);
        boolean strict = true;
        boolean allDecodes = true;
        boolean removeDefaultedParameters = true;
        boolean recursive = true;
        Double frequencyTolerance = null;
        Double absoluteTolerance = null;
        Double relativeTolerance = null;
        Double minimumLeadout = null;
        Decoder instance = new Decoder(/*"nec1", "rc5"*/);
        Decoder.DecoderParameters params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        Decoder.DecodeTree result = instance.decode(irSequence, params);
        assertTrue(result.isEmpty());

        strict = false;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = instance.decode(irSequence, params);
        assertEquals(result.size(), 9);
        Decoder.TrunkDecodeTree first = result.getAlternative("NEC1");
        assertEquals(first.getName(), "NEC1");
        Decoder.TrunkDecodeTree second = first.getRest().getAlternative("RC5");
        assertEquals(second.getName(), "RC5");

        irSequence = new ModulatedIrSequence(IrSequence.concatenate(nec1Intro, rc5Seq), 37000d);
        result = instance.decode(irSequence, params);
        assertEquals(result.size(), 8);

        allDecodes = false;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = instance.decode(irSequence, params);
        System.out.println(result);
        assertEquals(result.size(), 1);
    }

    /**
     * Test of decode method, of class Decoder.
     * @throws java.lang.Exception
     */
    @Test
    public void testDecode_8args_IrSignal() throws Exception {
        System.out.println("decode_IrSignal");
        Decoder dec = new Decoder("NRC17");
        boolean allDecodes = true;
        boolean removeDefaultedParameters = false;
        boolean recursive = true;
        Double frequencyTolerance = null;
        Double absoluteTolerance = null;
        Double relativeTolerance = null;
        Double minimumLeadout = null;

        IrSignal irSignal = new IrSignal(nrc17Intro, nrc17Repeat, nrc17Ending, NRC17_FREQUENCY);
        boolean strict = true;
        Decoder.DecoderParameters params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        Map<String, Decode> result = dec.decodeIrSignal(irSignal, params);
        assertTrue(nrc17NameEngine.numericallyEquals(result.get("NRC17")));

        irSignal = new IrSignal(nrc17Intro, nrc17Repeat, null, NRC17_FREQUENCY);
        strict = true;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = dec.decodeIrSignal(irSignal, params);
        assertEquals(0, result.size());
        strict = false;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = dec.decodeIrSignal(irSignal, params);
        assertTrue(nrc17NameEngine.numericallyEquals(result.get("NRC17")));

        dec = new Decoder("RC5");
        irSignal = new IrSignal(rc5Seq, null, null, RC5_FREQUENCY);
        strict = true;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = dec.decodeIrSignal(irSignal, params);
        assertEquals(0, result.size());
        strict = false;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = dec.decodeIrSignal(irSignal, params);
        assertTrue(rc5NameEngineWithDefault.numericallyEquals(result.get("RC5")));
        removeDefaultedParameters = true;
        params = new Decoder.DecoderParameters(strict, allDecodes, removeDefaultedParameters, recursive, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout, false);
        result = dec.decodeIrSignal(irSignal, params);
        assertTrue(rc5NameEngine.numericallyEquals(result.get("RC5")));
    }
}
