package org.harctoolbox.irp;

import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private final IrSequence nrc17Intro;
    private final IrSequence nrc17Repeat;
    private final IrSequence nrc17Ending;

    private final Protocol nec1;
    private final Protocol rc5;
    private final Protocol rc6;
    private final Protocol nokia32;
    private final Protocol nrc17;
    private final Protocol amino;
    private final Protocol mce;
    private final Protocol lumagen;
    private final Protocol arctech;
    private final Protocol xmp;
    private final Protocol directv;
    private final Protocol samsung36;
    private final Protocol tivo;
    private final Protocol denonK;
    private final Protocol sharp;
    private final Protocol apple;
    private final Protocol anthem;
    private final Protocol rc6M56;
    private final Protocol entone;
    private final Protocol rs200;
    private final Protocol solidtek16;
    private final Protocol zaptor;
    private final Protocol xmp1;
    private final Protocol rc5x;
    private final Protocol iodatan;
    private final Protocol velodyne;
    private final Protocol p_48_nec1;
    private final Protocol barco;
    private final Protocol nec1_nofreq;
    private final Protocol blaupunkt;
    private final Protocol elan;
    private final Protocol b_o;
    private final Protocol denon;
    private final Protocol gwts;
    private final Protocol elanplus;
    private final Protocol sony20;
    private final Protocol zenith;
    //private final Protocol aiwa;
    private final Protocol rc6M32;

    public ProtocolNGTest() throws Exception {
        //irpDatabase = new IrpDatabase(IRPDATABASE_PATH);
        nec1 =  new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
        rc5 = new Protocol("{36k,msb,889}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=1-T)[D:0..31,F:0..127,T@:0..1=0]");
        rc6 = new Protocol("{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,0:3,<-2,2|2,-2>(T:1),D:8,F:8,^107m)*,T=1-T)[D:0..255,F:0..255,T@:0..1=0]");
        nokia32 = new Protocol("{36k,1p,msb}<6,-10|6,-16|6,-22|6,-28>((15,-10,D:8,S:8,T:1,X:7,F:8,6,^100m)*,T=1-T)[D:0..255,S:0..255,F:0..255,T@:0..1=0,X:0..127]");
        nrc17 = new Protocol("{500,38k,25%}<-1,1|1,-1>(1,-5,1:1,254:8,255:8,-28,(1,-5,1:1,F:8,D:8,-220)+,1,-5,1:1,254:8,255:8,-200)[D:0..255,F:0..255]");
        amino = new Protocol("{37.3k,268,msb}<-1,1|1,-1>(T=1,(7,-6,3,D:4,1:1,T:1,1:2,0:8,F:8,15:4,C:4,-79m,T=0)+){C=(D:4+4*T+9+F:4+F:4:4+15)&15}[D:0..15,F:0..255]");
        mce = new Protocol("{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,6:3,-2,2,OEM1:8,S:8,T:1,D:7,F:8,^107m)*,T=1-T){OEM1=128}[D:0..127,S:0..255,F:0..255,T@:0..1=0]");
        lumagen = new Protocol("{38.4k,416,msb}<1,-6|1,-12>(D:4,C:1,F:7,1,-26)*{C=(#F+1)&1}[D:0..15,F:0..127]");
        arctech  = new Protocol("{0k,388}<1,-3|3,-1>(<0:2|2:2>((D-1):4,(S-1):4),40:7,F:1,0:1,-10.2m)*[D:1..16,S:1..16,F:0..1]");
        xmp  = new Protocol("{38k,136,msb}<210u,-760u|210u,-896u|210u,-1032u|210u,-1168u|210u,-1304u|210u,-1449u|210u,-1576u|210u,-1712u|210u,-1848u|210u,-1984u|210u,-2120u|210u,-2256u|210u,-2392u|210u,-2528u|210u,-2664u|210u,-2800u>([T=0][T=8],S:4:4,C1:4,S:4,15:4,OEM:8,D:8,210u,-13.8m,S:4:4,C2:4,T:4,S:4,F:16,210u,-80.4m){C1=-(S+S::4+15+OEM+OEM::4+D+D::4),C2=-(S+S::4+T+F+F::4+F::8+F::12)}[F:0..65535,D:0..255,S:0..255,OEM:0..255=68]");
        directv  = new Protocol("{38k,600,msb}<1,-1|1,-2|2,-1|2,-2>([10][5],-2,D:4,F:8,C:4,1,-50){C=7*(F:2:6)+5*(F:2:4)+3*(F:2:2)+(F:2)}[D:0..15,F:0..255]");
        samsung36  = new Protocol("{37.9k,560,33%}<1,-1|1,-3>(4500u,-4500u,D:8,S:8,1,-9,E:4,F:8,~F:8,1,^108m)*[D:0..255,S:0..255,F:0..255,E:0..15]");
        tivo  = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,U:4,~F:4:4,1,-78,(16,-4,1,-173)*)[D:133..133=133,S:48..48=48,F:0..255,U:0..15]");
        denonK  = new Protocol("{37k,432}<1,-1|1,-3>(8,-4,84:8,50:8,0:4,D:4,S:4,F:12,((D*16)^S^(F*16)^(F:8:4)):8,1,-173)*[D:0..15,S:0..15,F:0..4095]");
        sharp  = new Protocol("{38k,264}<1,-3|1,-7>(D:5,F:8,1:2,1,-165,D:5,~F:8,2:2,1,-165)*[D:0..31,F:0..255]");
        apple  = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,C:1,F:7,PairID:8,1,^108m,(16,-4,1,^108m)*){C=1-(#F+#PairID)%2,S=135}[D:0..255=238,F:0..127,PairID:0..255]");
        anthem  = new Protocol("{38.0k,605}<1,-1|1,-3>((8000u,-4000u,D:8,S:8,F:8,C:8,1,-25m)2,8000u,-4000u,D:8,S:8,F:8,C:8,1,-100)*{C=~(D+S+F+255):8}[D:0..255,S:0..255,F:0..255]");
        rc6M32  = new Protocol("{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,M:3,<-2,2|2,-2>(T:1),OEM1:8,OEM2:8,D:8,F:8,^107m)*,T=1-T)[OEM1:0..255,OEM2:0..255,D:0..255,F:0..255,M:0..7,T@:0..1=0]");
        rc6M56  = new Protocol("{36k,444,msb}<-1,1|1,-1>(6,-2,1:1,M:3,<-2,2|2,-2>(T:1),C:56,-131.0m)*[M:0..7,T@:0..1=0,C:0..72057594037927935]");
        entone  = new Protocol("{36k,444,msb}<-1,1|1,-1>(6,-2,1:1,M:3,<-2,2|2,-2>(T:1),0xE60396FFFFF:44,F:8,0:4,-131.0m)*{M=6,T=0}[F:0..255]");
        rs200  = new Protocol("{35.7k,msb}<50p,-120p|21p,-120p>(25:6,(H4-1):2,(H3-1):2,(H2-1):2,(H1-1):2,P:1,(D-1):3,F:2,0:2,sum:4,-1160p)*{P=~(#(D-1)+#F):1,sum=9+((H4-1)*4+(H3-1))+((H2-1)*4+(H1-1))+(P*8+(D-1))+F*4}[H1:1..4,H2:1..4,H3:1..4,H4:1..4,D:1..6,F:0..2]");
        solidtek16  = new Protocol("{38k}<-624,468|468,-624>(S=0,(1820,-590,0:1,D:4,F:7,S:1,C:4,1:1,-143m,S=1)3){C=F:4:0+F:3:4+8*S}[D:0..15,F:0..127]");
        zaptor  = new Protocol("{36k,330,msb}<-1,1|1,-1>([][T=0][T=1],8,-6,2,D:8,T:1,S:7,F:8,E:4,C:4,-74m){C=(D:4+D:4:4+S:4+S:3:4+8*T+F:4+F:4:4+E)&15}[D:0..255,S:0..127,F:0..127,E:0..15]");
        xmp1  = new Protocol("{38k,136,msb}<210u,-760u|210u,-896u|210u,-1032u|210u,-1168u|210u,-1304u|210u,-1449u|210u,-1576u|210u,-1712u|210u,-1848u|210u,-1984u|210u,-2120u|210u,-2256u|210u,-2392u|210u,-2528u|210u,-2664u|210u,-2800u>([T=0][T=8],S:4:4,C1:4,S:4,15:4,OEM:8,D:8,210u,-13.8m,S:4:4,C2:4,T:4,S:4,(F*256):16,210u,-80.4m){C1=-(S+S::4+15+OEM+OEM::4+D+D::4),C2=-(S+S::4+T+256*F+16*F+F+F::4)}[D:0..255,S:0..255,F:0..255,OEM:0..255=68]");
        rc5x  = new Protocol("{36k,msb,889}<1,-1|-1,1>((1,~S:1:6,T:1,D:5,-4,S:6,F:6,^114m)*,T=1-T)[D:0..31,S:0..127,F:0..63,T@:0..1=0]");
        iodatan  = new Protocol("{38k,550}<1,-1|1,-3>(16,-8,x:7,D:7,S:7,y:7,F:8,C:4,1,^108m)*{n=F:4^F:4:4^C:4}[D:0..127,S:0..127,F:0..255,C:0..15=0,x:0..127=0,y:0..127=0]");
        velodyne  = new Protocol("{38k,136,msb}<210u,-760u|210u,-896u|210u,-1032u|210u,-1168u|210u,-1304u|210u,-1449u|210u,-1576u|210u,-1712u|210u,-1848u|210u,-1984u|210u,-2120u|210u,-2256u|210u,-2392u|210u,-2528u|210u,-2664u|210u,-2800u>([T=0][T=8],S:4:4,~C:4,S:4,15:4,D:4,T:4,F:8,210u,-79m){C=(8+S:4+S:4:4+15+D+T+F:4+F:4:4)&15}[D:0..15,S:0..255,F:0..255]");
        p_48_nec1  = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,E:8,~E:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255,E:0..255]");
        barco  = new Protocol("{0k,10}<1,-5|1,-15>(1,-25,D:5,F:6,1,-25,1,-120m)*[D:0..31,F:0..63]");
        nec1_nofreq  = new Protocol("{564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
        blaupunkt  = new Protocol("{30.3k,512}<-1,1|1,-1>(1,-5,1023:10,-44,(1,-5,1:1,F:6,D:3,-236)+,1,-5,1023:10,-44)[F:0..63,D:0..7]");
        elan  = new Protocol("{40.2k,398,msb}<1,-1|1,-2>(3,-2,D:8,~D:8,2,-2,F:8,~F:8,1,^50m)*[D:0..255,F:0..255]");
        b_o  = new Protocol("{455k,3125,msb}<200u,-zeroGap,zeroGap=2,oneGap=3|200u,-oneGap,zeroGap=1,oneGap=2>(200u,-1,200u,-1,200u,-5,D:9,F:8,200u,-4,200u,-100m){zeroGap=1,oneGap=3}[D:0..511,F:0..255]");
        denon  = new Protocol("{38k,264}<1,-3|1,-7>(D:5,F:8,0:2,1,-165,D:5,~F:8,3:2,1,-165)*[D:0..31,F:0..255]");
        gwts  = new Protocol("{38.005k,417,lsb}<1|-1>(0:1,D:8,1:2,F:8,1:2,CRC:8,1:1)[D:0..255=144,F:0..255,CRC:0..255]");
        elanplus  = new Protocol("{40.2k,398,msb}<1,-1|1,-2>(3,-2,D:8,~D:8,2,-2,F:8,~F:8,1,^50m)+ [D:0..255,F:0..255]");
        sony20  = new Protocol("{40k,600}<1,-1|2,-1>(4,-1,F:7,D:5,S:8,^45m)*[D:0..31,S:0..255,F:0..127]");
        zenith  = new Protocol("{40k,520,msb}<1,-10|1,-1,1,-8>(S:1,<1:2|2:2>(F:D),-90m)*[S:0..1,F:0..255,D:5..8]");
        //aiwa  = new Protocol("{38.123k,550}<1,-1|1,-3>(16,-8,D:8,S:5,~D:8,~S:5,F:8,~F:8,1,-42,(16,-8,1,-165)*)[D:0..255,S:0..31,F:0..255]");

        nrc17Intro = new IrSequence(new int[]{500, 2500, 500, 1000, 1000, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 14500, 500, 2500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 1000, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 500, 110000});
        nrc17Repeat = new IrSequence(new int[]{500, 2500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 1000, 500, 500, 1000, 500, 500, 500, 500, 500, 500, 500, 110000});
        nrc17Ending = new IrSequence(new int[]{500, 2500, 500, 1000, 1000, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 100500});
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getFrequency method, of class Protocol.
     */
    @Test
    public void testGetFrequency() {
        try {
            System.out.println("getFrequency");
            Protocol instance = new Protocol("{56.0k,268,msb}<-1,1|1,-1>(T=1,(7,-6,3,D:4,1:1,T:1,1:2,0:8,F:8,15:4,C:4,-79m,T=0)+){C =(D:4+4*T+9+F:4+F:4:4+15)&15} [D:0..15,F:0..255]");
            double expResult = 56000f;
            double result = instance.getFrequency();
            assertEquals(result, expResult, 0.0);
        } catch (InvalidNameException | IrpInvalidArgumentException | NameUnassignedException | UnsupportedRepeatException ex) {
            fail();
        }
    }

    /**
     * Test of numberOfInfiniteRepeats method, of class Protocol.
     */
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testNumberOfInfiniteRepeats() {
        System.out.println("numberOfInfiniteRepeats");
        try {
            new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,(F:8)+,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]");
            fail();
        } catch (UnsupportedRepeatException ex) {
            // success!
        } catch (InvalidNameException | IrpInvalidArgumentException | NameUnassignedException ex) {
            fail();
        }
    }

    @Test
    public void testRandomParametersNec1() throws InvalidNameException {
        System.out.println("randomParametersNec1");
        NameEngine nameEngine = new NameEngine("{D=186,S=13,F=174}");
        Random rng = new Random(42);
        Map<String, Long> params = nec1.randomParameters(rng);
        assertTrue(nameEngine.numericallyEquals(params));
    }

    /**
     * Test of toIrSignal method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testToIrSignalNec1() throws Exception {
        System.out.println("toIrSignalNec1");
        NameEngine nameEngine;
        nameEngine = new NameEngine("{D=12,S=34,F=56}");
        IrSignal result = nec1.toIrSignal(nameEngine);
        IrSignal expected = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
        assertTrue(result.approximatelyEquals(expected));
        nameEngine = new NameEngine("{D=12,F=56}");
        result = nec1.toIrSignal(nameEngine);
        assertTrue(result.approximatelyEquals(Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C")));
    }

    @Test
    public void testToIrSignalRc5() throws Exception {
        System.out.println("toIrSignalRc5");
        // Testing the memory varables/toggles
        IrSignal rc5D12F56T0 = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
        IrSignal rc5D12F56T1 = Pronto.parse("0000 0073 0000 000B 0020 0020 0020 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
        // New protocol, no assignment to toggle
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");
        IrSignal result = rc5.toIrSignal(nameEngine);
        assertEquals(nameEngine.get("T").toNumber(), 1L);
        assertEquals(rc5.getMemoryVariable("T"), 1L);
        assertTrue(result.approximatelyEquals(rc5D12F56T0));

        result = rc5.toIrSignal(nameEngine);
        assertEquals(nameEngine.get("T").toNumber(), 0L);
        assertEquals(rc5.getMemoryVariable("T"), 0L);
        assertTrue(result.approximatelyEquals(rc5D12F56T1));

        nameEngine = new NameEngine("{D=12,F=56,T=1}");
        result = rc5.toIrSignal(nameEngine);
        assertTrue(result.approximatelyEquals(rc5D12F56T1));
        assertEquals(nameEngine.get("T").toNumber(), 0L);
        assertEquals(rc5.getMemoryVariable("T"), 0L);

        nameEngine = new NameEngine("{D=12,F=56, T=0}");
        result = rc5.toIrSignal(nameEngine);
        assertEquals(nameEngine.get("T").toNumber(), 1L);
        assertEquals(rc5.getMemoryVariable("T"), 1L);
        assertTrue(result.approximatelyEquals(rc5D12F56T0));
    }

    @Test
    public void testToIrSignalRc6() throws Exception {
        System.out.println("toIrSignalRc6");
        IrSignal rc6D12F34 = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0020 0010 0BCD");
        IrSignal result = rc6.toIrSignal(new NameEngine("{D=12,F=34}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(rc6D12F34));
    }

    @Test
    public void testToIrSignalNokia32() throws Exception {
        System.out.println("toIrSignalNokia32");
        IrSignal nokia32D12S56F34T0X78 = Pronto.parse("0000 0073 0000 0012 000F 000A 0006 000A 0006 000A 0006 001C 0006 000A 0006 000A 0006 001C 0006 0016 0006 000A 0006 0010 0006 000A 0006 001C 0006 0016 0006 000A 0006 0016 0006 000A 0006 0016 0006 0C86");
        IrSignal result = nokia32.toIrSignal(new NameEngine("{D=12,S=56,F=34,T=0,X=78}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(nokia32D12S56F34T0X78));
    }

    @Test(enabled = true)
    public void testRecognizeNokia32() throws Exception {
        System.out.println("recognizeNokia32");
        IrSignal signal = Pronto.parse("0000 0073 0000 0012"
                + " 000F 000A"
                + " 0006 000A 0006 000A 0006 001B 0006 000A" // D
                + " 0006 000A 0006 001B 0006 0015 0006 000A" // S
                + " 0006 001B 0006 000A 0006 0015 0006 0010" // T,X
                + " 0006 000A 0006 0015 0006 000A 0006 0015" // F
                + " 0006 0C90");
        NameEngine nameEngine = new NameEngine("{D=12,S=56,F=34,T=1,X=73}");
        Map<String, Long> recognizeData = nokia32.recognize(signal, true, 1000.0, 100.0, 0.04, 50000.0);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeNrc17AsSignal() throws Exception {
        System.out.println("recognizeNrc17AsSignal");
        IrSignal signal = new IrSignal(nrc17Intro, nrc17Repeat, nrc17Ending, 38000.0);
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");
        Map<String, Long> recognizeData = nrc17.recognize(signal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeNrc17AsSignalWithoutEnding() throws Exception {
        System.out.println("recognizeNrc17AsSignalWithoutEnding");
        IrSignal signal = new IrSignal(nrc17Intro, nrc17Repeat, null, 38000.0);
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");
        Map<String, Long> recognizeData = nrc17.recognize(signal, false);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
        try {
            nrc17.recognize(signal, true);
            fail();
        } catch (SignalRecognitionException ex) {
        }
    }

    @Test(enabled = true)
    public void testRecognizeNrc17AsIntroSequence() throws Exception {
        System.out.println("recognizeNrc17AsIntroSequence");
        ModulatedIrSequence sequence = new ModulatedIrSequence(IrSequence.concatenate(nrc17Intro, nrc17Repeat, nrc17Ending), 38000.0);
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");
        Decoder.Decode decode = nrc17.recognize(sequence, true, true);
        assertTrue(nameEngine.numericallyEquals(decode.getMap()));

        IrSignal signal = new IrSignal(sequence);
        try {
            nrc17.recognize(signal, false);
            fail();
        } catch (SignalRecognitionException ex) {
        }
    }

    @Test(enabled = true)
    public void testRecognizeNrc17AsSequence() throws Exception {
        System.out.println("recognizeNrc17AsIntroSequence");
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");

        ModulatedIrSequence sequence = new ModulatedIrSequence(IrSequence.concatenate(nrc17Intro, nrc17Ending), 38000.0);
        try {
            Decoder.Decode decode = nrc17.recognize(sequence, true, true);
            fail();
        } catch (SignalRecognitionException ex) {
        }

        Decoder.Decode decode = nrc17.recognize(sequence, false, true);
        assertTrue(nameEngine.numericallyEquals(decode.getMap()));

        sequence = new ModulatedIrSequence(nrc17Intro, 38000.0);
        try {
            nrc17.recognize(sequence, false, true);
            fail();
        } catch (SignalRecognitionException ex) {
        }

        decode = nrc17.recognize(sequence, false, false);
        assertTrue(nameEngine.numericallyEquals(decode.getMap()));

        sequence = new ModulatedIrSequence(IrSequence.concatenate(nrc17Intro, nrc17Repeat, nrc17Repeat, nrc17Repeat, nrc17Ending), 38000.0);

        decode = nrc17.recognize(sequence, true, true);
        assertTrue(nameEngine.numericallyEquals(decode.getMap()));
        assertEquals(3, decode.getNumberOfRepetitions());
    }

    @Test
    public void testToIrSignalAmino() throws Exception {
        System.out.println("toIrSignalAmino");
        IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
        IrSignal result = amino.toIrSignal(new NameEngine("{D=12,F=34}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(aminoD12F34));
    }

    @Test
    public void testSubstituteConstantVariablesMCE() throws UnknownProtocolException, UnsupportedRepeatException, InvalidNameException, IrpInvalidArgumentException, NameUnassignedException {
        System.out.println("substituteConstantVariablesMCE");
        Protocol result = mce.substituteConstantVariables();
        //assertEquals(result.toIrpString(), "{36.0k,444,msb}<-1,1|1,-1>((6,-2,1:1,6:3,-2,2,OEM1:8,S:8,T:1,D:7,F:8,^107m)*,T=(1-T)){OEM1=128}[D:0..127,S:0..255,F:0..255,T@:0..1=0]");
        assertEquals(result.toIrpString(), "{36.0k,444,msb}<-1,1|1,-1>((6,-2,1:1,6:3,-2,2,128:8,S:8,T:1,D:7,F:8,^107m)*,T=(1-T))[D:0..127,S:0..255,F:0..255,T@:0..1=0]");
    }

    @Test
    public void testSubstituteConstantVariablesLumagen() throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        System.out.println("substituteConstantVariablesMCE");
        Protocol result = lumagen.substituteConstantVariables();
        assertEquals(result.toIrpString(), lumagen.toIrpString());
    }

    @Test
    public void testRecognizeAmino() throws Exception {
        System.out.println("recognizeAmino");
        IrSignal aminoD12F34 = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
        NameEngine nameEngine = new NameEngine("{D=12,F=34}");
        Map<String, Long> recognizeData = amino.recognize(aminoD12F34, true, 1000.0, 100.0, 0.1, 50000.0);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    public void testRecognizeErroneousAmino() throws Exception {
        System.out.println("recognizeErroneousAmino");
        IrSignal aminoD12F34Err = Pronto.parse("0000 006F 001C 001C 0046 003C 0028 000A 000A 0014 000A 000A 0014 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 0B83 0046 003C 0028 000A 000A 0014 000A 000A 0014 0014 000A 000A 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 000A 0014 0014 000A 000A 000A 000A 0014 0014 0014 000A 000A 000A 000A 000A 000A 000A 000A 0014 000A 000A 000A 000A 000A 0B83");
        System.out.print("Expect parse error from FiniteBitField: ");
        try {
            amino.recognize(aminoD12F34Err);
            fail();
        } catch (SignalRecognitionException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testToIrSignalArctech() throws Exception {
        System.out.println("toIrSignalArchtech");
        IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
        IrSignal result = arctech.toIrSignal(new NameEngine("{D=12,S=9,F=0}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(irSignal));
    }

    @Test
    public void testToIrSignalXmp() throws Exception {
        System.out.println("toIrSignalXmp");
        IrSignal xmpD12S56F34 = Pronto.parse("0000 006D 0012 0012 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 0022 0008 001D 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF 0008 002C 0008 0027 0008 0046 0008 006A 0008 0032 0008 0032 0008 001D 0008 005B 0008 020C 0008 002C 0008 004B 0008 0046 0008 0046 0008 001D 0008 001D 0008 0027 0008 0027 0008 0BEF");
        IrSignal result = xmp.toIrSignal(new NameEngine("{D=12,S=56,F=34}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(xmpD12S56F34));
    }

    @Test(enabled = true)
    public void testToIrSignalDirectv() throws Exception {
        System.out.println("toIrSignalDirectv");
        IrSignal directvD12F34 = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
        IrSignal result = directv.toIrSignal(new NameEngine("{D=12,F=34}"));
        System.out.println(result);
        assertTrue(result.approximatelyEquals(directvD12F34));
    }

    @Test(enabled = true)
    public void testRecognizeDirecttv() throws Exception {
        System.out.println("recognizeDirectv");
            IrSignal irSignal = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
            NameEngine nameEngine = new NameEngine("{D=12,F=34}");
            Map<String, Long> recognizeData = directv.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of getBitDirection method, of class Protocol.
     */
    @Test
    public void testGetBitDirection() {
        System.out.println("getBitDirection");
        assertEquals(rc5.getBitDirection(), BitDirection.msb);
        assertEquals(nec1.getBitDirection(), BitDirection.lsb);
    }

    /**
     * Test of getUnit method, of class Protocol.
     */
    @Test
    public void testGetUnit() {
        System.out.println("getUnit");
        assertEquals(rc5.getUnit(), 889f, 0.0);
        assertEquals(nec1.getUnit(), 564f, 0.0);
    }

    /**
     * Test of getDutyCycle method, of class Protocol.
     */
    @Test
    public void testGetDutyCycle() {
        System.out.println("getDutyCycle");
        IrCoreUtils.approximatelyEquals(samsung36.getDutyCycle(), 0.33);
        assertTrue(sharp.getDutyCycle() == null);
    }

    /**
     * Test of toIrpString method, of class Protocol.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        String result = rc5.toIrpString();
        assertEquals(result, "{36.0k,889,msb}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=(1-T))[D:0..31,F:0..127,T@:0..1=0]");
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testRecognizeNec1() throws Exception {
        System.out.println("recognize");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
        NameEngine nameEngine = new NameEngine("{D=12,S=34,F=56}");
        Map<String, Long> recognizeData = nec1.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testRecognizeNec1Defaulted() throws Exception {
        System.out.println("recognizeNec1Defaulted");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C");
        NameEngine nameEngine = new NameEngine("{D=12,S=243,F=56}");
        Map<String, Long> recognizeData = nec1.recognize(irSignal, true);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
        nameEngine = new NameEngine("{D=12,F=56}");
        nec1.removeDefaulteds(recognizeData);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testRecognizeNec1LongLeadout() throws Exception {
        System.out.println("recognizeLong");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 1E6C");
        NameEngine nameEngine = new NameEngine("{D=12,S=34,F=56}");
        Map<String, Long> recognizeData = nec1.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testRecognizeNec1ShortLeadout() throws Exception {
        System.out.println("recognizeNec1ShortLeadout");
        // 15ms final leadout
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0240");
        NameEngine nameEngine = new NameEngine("{D=12,S=34,F=56}");
        try {
            nec1.recognize(irSignal);
            fail();
        } catch (SignalRecognitionException ex) {
        }

        Map<String, Long> recognizeData = nec1.recognize(irSignal, true, 2000.0, 100.0, 0.04, 10000.0);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    // Tests domain test
    public void testRecognizeTivo() throws Exception {
        System.out.println("recognize");
        try {
            IrSignal irSignal = Pronto.parse("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            System.out.print("Expect SignalRecognitionException(DomainViolationException) ");
            tivo.recognize(irSignal);
            fail();
        } catch (SignalRecognitionException ex) {
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testRecognizeRc5() throws Exception {
        System.out.println("recognize");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 000B 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
        NameEngine nameEngine = new NameEngine("{D=12,F=56,T=0}");
        Map<String, Long> recognizeData = rc5.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
        nameEngine = new NameEngine("{D=12,F=56}");
        rc5.removeDefaulteds(recognizeData);
        //Map<String, Long> recognizeData = rc5.recognize(irSignal, true);
        assertTrue(nameEngine.numericallyEquals(recognizeData));

    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testRecognizeRc5RepeatAsIntro() throws Exception {
        System.out.println("recognizeRepeatAsIntro");

        IrSignal irSignal = Pronto.parse("0000 0073 000B 0000 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0CC8");
        NameEngine nameEngine = new NameEngine("{D=12,F=56}");
        Map<String, Long> recognizeData;
        try {
            recognizeData = rc5.recognize(irSignal, false);
            rc5x.removeDefaulteds(recognizeData);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
        } catch (SignalRecognitionException ex) {
            fail();
        }
        try {
            rc5.recognize(irSignal, true);
            fail();
        } catch (SignalRecognitionException ex) {
        }
    }

    /**
     * Test of recognize method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testRecognizeDenonK() throws Exception {
        System.out.println("recognize");
        IrSignal irSignal = Pronto.parse("0000 0070 0000 0032 0080 0040 0010 0010 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0ACD");
        NameEngine nameEngine = new NameEngine("{D=12,S=3,F=56}");
        Map<String, Long> recognizeData = denonK.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeAdNotam() throws Exception {
        System.out.println("recognizeAdNotam");
        Protocol adnotam = new Protocol("{35.7k,895,msb}<1,-1|-1,1>(1,-2,1,D:6,F:6,^114m)*[D:0..63,F:0..63]");
        IrSignal irSignal = Pronto.parse("0000 0074 0000 000C 0020 0040 0040 0020 0020 0040 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0020 0020 0020 0040 0020 0020 0020 0C67");
        NameEngine nameEngine = new NameEngine("{D=12,F=3}");
        Map<String, Long> recognizeData = adnotam.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    public void testRecognizeRc6() throws Exception {
        System.out.println("recognizeRc6");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 0013 0060 0020 0010 0020 0010 0010 0010 0010 0030 0030 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0010 0BCD");
        NameEngine nameEngine = new NameEngine("{D=31,F=30,T=1}");
        Map<String, Long> recognizeData = rc6.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    public void testRecognizeArcTech() throws Exception {
        System.out.println("recognizeArcTech");
        IrSignal irSignal = Pronto.parse("0100 000A 0000 0019 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 00A1 01E2 01E2 00A1 00A1 01E2 01E2 00A1 00A1 01E2 00A1 01E2 00A1 1267");
        NameEngine nameEngine = new NameEngine("{D=12,S=5,F=0}");
        Map<String, Long> recognizeData = arctech.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeApple() throws Exception {
        System.out.println("recognizeApple");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002"
                + " 015B 00AD"
                + " 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016"
                + " 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041"
                + " 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016"
                + " 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016"
                + " 0016 0622"
                + " 015B 0057 0016 0E6C");
        NameEngine nameEngine = new NameEngine("{D=12,F=34,PairID=123}");
        Map<String, Long> recognizeData = apple.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test
    public void testRecognizeAppleErr() throws Exception {
        System.out.println("recognizeAppleErr");
        IrSignal irSignal = Pronto.parse("0000 006C 0022 0002"
                + " 015B 00AD"
                + " 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016"
                + " 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041"
                /* ---> */ + " 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016"
                + " 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016"
                + " 0016 0622"
                + " 015B 0057 0016 0E6C");
        System.out.print("Expect NameConflictException: ");
        try {
            apple.recognize(irSignal);
            fail();
        } catch (SignalRecognitionException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeAnthem() throws Exception {
        System.out.println("recognizeAnthem");
        IrSignal irSignal = Pronto.parse("0000 006D 0000 0066 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 08FB");
        NameEngine nameEngine = new NameEngine("{D=12,F=34,S=56}");
        Map<String, Long> recognizeData = anthem.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeBrokenAnthem() throws Exception {
        System.out.println("recognizeAnthem");
        IrSignal irSignal = Pronto.parse("0000 006D 0066 0000 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 03B6 0130 0098 0017 0017 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0017 0045 0017 0045 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0017 0017 0017 0017 0045 0017 0017 0017 0045 0017 0045 0017 0017 0017 0017 0017 0045 0017 08FB");
        NameEngine nameEngine = new NameEngine("{D=12,F=34,S=56}");
        Map<String, Long> recognizeData = anthem.recognize(irSignal, false);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
        try {
            anthem.recognize(irSignal, true); // should throw
            fail();
        } catch (SignalRecognitionException ex) {
        }
    }

    @Test(enabled = true)
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void testRecognizeRc6M32() throws Exception {
            System.out.println("recognizeRc6M32");
            IrSignal irSignal = Pronto.parse("0000 0073 0000 001E 0060 0020 0010 0020 0020 0010 0010 0010 0020 0020 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0020 0010 0010 0020 0020 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 09DD");
            NameEngine nameEngine = new NameEngine("{D=11,T=1,M=3,OEM1=227,F=111,OEM2=215}");
            //NamedProtocol rc6M56 = irpDatabase.getNamedProtocol("rc6-M-32");
            Map<String, Long> recognizeData = rc6M32.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeRc6M56() throws Exception {
        System.out.println("recognizeRc6M56");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 0039"
                + " 0060 0020 0010 0010"
                + " 0010 0020 0020 0010 0020 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 126C");
        NameEngine nameEngine = new NameEngine("{M=5, C=806354200, T=1}");
        //NamedProtocol rc6M56 = irpDatabase.getNamedProtocol("rc6-M-56");
        Map<String, Long> recognizeData = rc6M56.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeMce() throws Exception {
        System.out.println("recognizeMce");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 0020 0060 0020 0010 0010 0010 0010 0010 0020 0010 0020 0030 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0020 0020 0010 0010 0010 0010 0020 0020 0010 09CD");
        NameEngine nameEngine = new NameEngine("{D=12, S=56, F=34, T=1}");
        Map<String, Long> recognizeData = mce.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeEntone() throws Exception {
        System.out.println("recognizeEntone");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 0035 0060 0020 0010 0010 0010 0010 0010 0020 0010 0020 0030 0010 0010 0010 0010 0020 0010 0010 0020 0010 0010 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0010 0010 0020 0010 0010 0020 0020 0020 0010 0010 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0020 0010 0010 0020 0020 0020 0020 0020 0020 0010 0010 0010 0010 0010 0010 0010 0010 0010 126C");
        NameEngine nameEngine = new NameEngine("{F=42}");
        Map<String, Long> recognizeData = entone.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeRc5x() throws Exception {
        System.out.println("recognizeRc5x");
        IrSignal irSignal = Pronto.parse("0000 0073 0000 000F 0040 0020 0020 0040 0020 0020 0020 0020 0040 0020 0020 00C0 0040 0040 0040 0040 0040 0020 0020 0020 0020 0040 0020 0020 0020 0020 0020 0020 0020 0AA8");
        NameEngine nameEngine = new NameEngine("{D=28, S=106, F=15}");
        Map<String, Long> recognizeData = rc5x.recognize(irSignal, true, 1000.0, 100.0, 0.1, 50000.0);
        rc5x.removeDefaulteds(recognizeData);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeRs200() throws Exception {
            System.out.println("recognizeRs200");
            IrSignal irSignal = Pronto.parse("0000 0074 0000 001A 0032 0078 0015 0078 0015 0078 0032 0078 0032 0078 0015 0078 0015 0078 0015 0078 0015 0078 0032 0078 0032 0078 0015 0078 0032 0078 0032 0078 0015 0078 0032 0078 0015 0078 0032 0078 0015 0078 0032 0078 0032 0078 0032 0078 0015 0078 0015 0078 0032 0078 0015 0500");
            NameEngine nameEngine = new NameEngine("{D=3, F=2, H1=1, H2=2, H3=3, H4=4}");
            Map<String, Long> recognizeData = rs200.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeSolidtek16() throws Exception {
        System.out.println("recognizeSolidtek16");
        IrSignal irSignal = Pronto.parse("0000 006D 002E 0000 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0012 0018 0012 0018 0012 0018 0012 0018 0024 0018 0012 1552 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0024 002F 0012 0018 0012 0018 0012 0018 0024 1552 0045 002E 0012 0018 0012 0018 0024 0018 0012 0018 0012 0018 0012 0018 0012 002F 0024 002F 0012 0018 0024 002F 0012 0018 0012 0018 0012 0018 0024 1552");
        NameEngine nameEngine = new NameEngine("{D=12, F=23}");
        Map<String, Long> recognizeData = solidtek16.recognize(irSignal);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeZaptor() throws Exception {
        try {
            System.out.println("recognizeZaptor");
            IrSequence repeat = new IrSequence(new int[]{2640, 1980, 660, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 660, 660, 660, 660, 660, 330, 330, 330, 330, 660, 330, 330, 660, 660, 330, 330, 330, 330, 330, 330, 660, 660, 660, 660, 330, 330, 660, 660, 74330});
            IrSequence ending = new IrSequence(new int[]{2640, 1980, 660, 330, 660, 660, 330, 330, 660, 660, 330, 330, 660, 330, 330, 660, 660, 660, 660, 660, 660, 660, 330, 330, 330, 330, 660, 330, 330, 660, 660, 330, 330, 330, 330, 330, 330, 660, 660, 660, 330, 330, 660, 660, 660, 74330});
            IrSignal irSignal = new IrSignal(repeat, repeat, ending, 36000d);
            NameEngine nameEngine = new NameEngine("{D=73, F=55, S=42, E=10}");
            //NamedProtocol zaptor = irpDatabase.getNamedProtocol("zaptor-36");
            Map<String, Long> recognizeData = zaptor.recognize(irSignal);
            assertTrue(nameEngine.numericallyEquals(recognizeData));
            IrSignal silly = new IrSignal(repeat, repeat, repeat, 36000d);
            System.err.print("Expect SignalRecognitionException: ");
            zaptor.recognize(silly);
            fail();
        } catch (SignalRecognitionException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test(enabled = true)
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void testRecognizeIodatan() throws Exception {
        System.out.println("recognizeIodatan");
        IrSignal irSignal = Pronto.parse("0000 006D 0000 002A 014E 00A7 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0627");
        NameEngine nameEngine = new NameEngine("{D=12, F=23, S=34}");
        Map<String, Long> recognizeData = iodatan.recognize(irSignal, true);
        iodatan.removeDefaulteds(recognizeData);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognizeVelodyne() throws Exception {
        System.out.println("recognizeVelodyne");
        IrSignal irSignal = Pronto.parse("0000 006D 0009 0009 0008 0060 0008 0051 0008 0041 0008 006A 0008 0037 0008 001D 0008 0032 0008 0022 0008 0BBA 0008 0060 0008 0027 0008 0041 0008 006A 0008 0037 0008 0046 0008 0032 0008 0022 0008 0BBA");
        NameEngine nameEngine = new NameEngine("{D=5, F=65, S=215}");
        try {
            velodyne.recognize(irSignal);
            fail();
        } catch (SignalRecognitionException ex) {
        }
        Map<String, Long> recognizeData = velodyne.recognize(irSignal, true, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE, 0.04, IrCoreUtils.DEFAULT_MINIMUM_LEADOUT);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeXmp() throws Exception {
        System.out.println("recognizeXmp");
        IrSignal irSignal = Pronto.parse("0000 006D 0012 0012 0008 006A 0008 0056 0008 003C 0008 006A 0008 0065 0008 006A 0008 003C 0008 0065 0008 020C 0008 006A 0008 0051 0008 001D 0008 003C 0008 002C 0008 0046 0008 0046 0008 0065 0008 0BEF 0008 006A 0008 0056 0008 003C 0008 006A 0008 0065 0008 006A 0008 003C 0008 0065 0008 020C 0008 006A 0008 0027 0008 0046 0008 003C 0008 002C 0008 0046 0008 0046 0008 0065 0008 0BEF");
        NameEngine nameEngine = new NameEngine("{D=110, F=14478, S=246, OEM=239}");
        Map<String, Long> recognizeData = xmp.recognize(irSignal, true, 500f, 50f, 0.02, 10000d);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    @SuppressWarnings("UseSpecificCatch")
    public void testRecognizeXmp1() throws Exception {
        System.out.println("recognizeXmp1");
        IrSignal irSignal = Pronto.parse("0000 006D 0012 0012 0008 006A 0008 001D 0008 003C 0008 006A 0008 0032 0008 0032 0008 003C 0008 0065 0008 020C 0008 006A 0008 0027 0008 001D 0008 003C 0008 004B 0008 001D 0008 001D 0008 001D 0008 0BEF 0008 006A 0008 001D 0008 003C 0008 006A 0008 0032 0008 0032 0008 003C 0008 0065 0008 020C 0008 006A 0008 0051 0008 0046 0008 003C 0008 004B 0008 001D 0008 001D 0008 001D 0008 0BEF");
        NameEngine nameEngine = new NameEngine("{D=110, F=144, S=246}");
        Map<String, Long> recognizeData = xmp1.recognize(irSignal, true, 500f, 50f, 0.02, 10000d);
        xmp1.removeDefaulteds(recognizeData);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    @Test(enabled = true)
    public void testRecognize48_nec1() throws Exception {
        System.out.println("recognize48_nec1");
        IrSignal irSignal = Pronto.parse("0000 006C 0032 0002 015B 00AD 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0190 015B 0057 0016 0E6C");
        NameEngine nameEngine = new NameEngine("{D=110, F=41, E=17, S=118}");
        //NamedProtocol p_48_nec1 = irpDatabase.getNamedProtocol("48-nec1");
        Map<String, Long> recognizeData = p_48_nec1.recognize(irSignal, true, 500f, 50f, 0.02, 10000d);
        assertTrue(nameEngine.numericallyEquals(recognizeData));
    }

    /**
     * Test of warningFrequency method, of class Protocol.
     */
    @Test
    public void testWarningFrequency() {
        System.out.println("warningFrequency");
        String expResult = "";
        String result = nec1.warningFrequency();
        assertEquals(result, expResult);
        assertEquals(barco.warningFrequency(), "");
        assertEquals(nec1_nofreq.warningFrequency().trim(), "Warning: Frequency is missing, using default frequency = 38000.0.");
        assertEquals(blaupunkt.warningFrequency().trim(), "Warning: Uncommon frequency = 30300.");
        assertEquals(elan.warningFrequency().trim(), "Warning: Uncommon frequency = 40200.");
    }

    /**
     * Test of warningStartsWithFlash method, of class Protocol.
     */
    @Test
    public void testWarningStartsWithFlash() {
        System.out.println("warningStartsWithFlash");
        assertEquals(nec1.warningStartsWithFlash(), "");
        assertEquals(denon.warningStartsWithFlash().trim(), "Warning: Protocol does not start with a Duration/Flash.");
    }

    /**
     * Test of warningTrivialBitspec method, of class Protocol.
     */
    @Test
    public void testWarningTrivialBitspec() {
        System.out.println("warningTrivialBitspec");
            assertEquals(nec1.warningTrivialBitspec(), "");
            assertEquals(gwts.warningTrivialBitspec().trim(), "Warning: Protocol uses trivial bitspec.");
    }

    /**
     * Test of warningRepeatPlus method, of class Protocol.
     */
    @Test
    public void testWarningRepeatPlus() {
        System.out.println("warningRepeatPlus");
        assertEquals(nec1.warningRepeatPlus(), "");
        assertEquals(elanplus.warningRepeatPlus().trim(), "Warning: Protocol uses infinite repeat with min > 0.");
    }

    /**
     * Test of warningsInterleaving method, of class Protocol.
     */
    @Test
    public void testWarningsInterleaving() {
            System.out.println("warningsInterleaving");
            assertEquals(nec1.warningsInterleaving(), "");
            assertEquals(sony20.warningsInterleaving().trim(), "Warning: Protocol not interleaving, but is Sony-like.");
    }

    /**
     * Test of warningNonConstantLengthBitFields method, of class Protocol.
     */
    @Test
    public void testWarningNonConstantLengthBitFields() {
        System.out.println("warningNonConstantLengthBitFields");
        assertEquals(nec1.warningNonConstantLengthBitFields(), "");
        assertEquals(zenith.warningNonConstantLengthBitFields().trim(), "Warning: Protocol contains bitfields with non-constant lengths.");
    }

    /**
     * Test of warningNoParameterSpecs method, of class Protocol.
     * @throws java.lang.Exception
     */
    @Test
    public void testWarningNoParameterSpecs() throws Exception {
        System.out.println("warningNoParameterSpecs");
        assertEquals(nec1.warningNoParameterSpecs(), "");
        Protocol nec1_noparmspec = new Protocol("{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)");
        assertEquals(nec1_noparmspec.warningNoParameterSpecs().trim(), "Warning: ParameterSpecs missing from the protocol.");
    }

    @Test
    public void testAllDurationsInMicros() throws Exception {
        System.out.println("allDurationsInMicros");
        TreeSet<Double> durations = nec1.allDurationsInMicros();
        assertEquals(durations.size(), 5);
        durations = b_o.allDurationsInMicros();
        assertEquals(durations.size(), 6);
    }

    @Test
    public void testMinDurationDiff() throws Exception {
        System.out.println("minDurationsDiff");
        //Protocol prot = irpDatabase.getProtocol("NEC1");
        //TreeSet<Double> durations = prot.allDurationsInMicros();
        double min = nec1.minDurationDiff();
        assertEquals(min, 564.0, 0.00001);
    }
}
