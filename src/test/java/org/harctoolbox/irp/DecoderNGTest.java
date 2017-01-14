package org.harctoolbox.irp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class DecoderNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Decoder decoder;

    public DecoderNGTest() throws IOException, SAXException, IrpSyntaxException {
        IrpDatabase irp = new IrpDatabase("src/main/config/IrpProtocols.xml");
        irp.expand();
        decoder = new Decoder(irp);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of decode method, of class Decoder.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testDecode() throws InvalidArgumentException {
        System.out.println("decode");
        IrSignal irSignal = new IrSignal("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");

        Map<String, Map<String, Long>> result = decoder.decode(irSignal);
        assertEquals(result.size(), 3);
        assertEquals(result.get("NEC1").toString(), "{S=34, D=12, F=56}");
    }
}
