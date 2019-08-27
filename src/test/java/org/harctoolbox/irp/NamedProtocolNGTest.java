package org.harctoolbox.irp;

import java.io.IOException;
import java.util.Map;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.irp.Decoder.Decode;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NamedProtocolNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final IrSignal irSignal;
    private final Decoder decoder;

    public NamedProtocolNGTest() throws Pronto.NonProntoFormatException, InvalidArgumentException, IrpParseException, IOException {
        irSignal = Pronto.parse("0000 006D 0012 0012 0008 0027 0008 003C 0008 0022 0008 006A 0008 0032 0008 0032 0008 001D 0008 001D 0008 020C 0008 0027 0008 0060 0008 001D 0008 0022 0008 001D 0008 001D 0008 001D 0008 001D 0008 0BEF 0008 0027 0008 003C 0008 0022 0008 006A 0008 0032 0008 0032 0008 001D 0008 001D 0008 020C 0008 0027 0008 0037 0008 0046 0008 0022 0008 001D 0008 001D 0008 001D 0008 001D 0008 0BEF");
        decoder = new Decoder();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test(enabled = false)
    public void testDecode_XMP() {
        System.out.println("decode_XMP");
        Decoder.setDebugProtocolRegExp("xmp-1");
        Decoder.DecoderParameters params = new Decoder.DecoderParameters();
        params.setRemoveDefaultedParameters(true);
        Map<String, Decode> result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 4);
        assertEquals(result.get("XMP-1").toString(), "XMP-1: {D=0,F=0,S=33}");

        params.setRelativeTolerance(0.3); // Destroys decoding
        result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 0);

        params.setOverride(false); // Make decoding work again
        result = decoder.decodeIrSignal(irSignal, params);
        assertEquals(result.size(), 4);
    }
}
