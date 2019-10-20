package org.harctoolbox.irp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.DocumentFragment;

/**
 *
 * @author bengt
 */
public class IrpDatabaseNGTest {

    private static final String CONFIGFILE = "src/main/resources/IrpProtocols.xml";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final IrpDatabase instance;

    public IrpDatabaseNGTest() throws IOException, IrpParseException {
        instance = new IrpDatabase(CONFIGFILE);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of checkSorted method, of class IrpDatabase.
     * This guarantees that the data base will be, and stay, sorted.
     * @throws java.io.IOException
     * @throws org.harctoolbox.irp.IrpParseException
     */
    @Test
    public void testCheckSorted() throws IOException, IrpParseException {
        System.out.println("checkSorted");
        IrpDatabase db = new IrpDatabase(CONFIGFILE);
        String result = db.checkSorted();
        assertNull(result);
    }

    /**
     * Test of isKnown method, of class IrpDatabase.
     * @throws java.io.IOException
     * @throws org.harctoolbox.irp.IrpParseException
     */
    @Test(enabled = false)
    public void testIsKnown_String_String() throws IOException, IrpParseException {
        System.out.println("isKnown");
        String protocolsPath = CONFIGFILE;
        assertFalse(IrpDatabase.isKnown(protocolsPath, "covfefe"));
        assertTrue(IrpDatabase.isKnown(protocolsPath, "NEC1"));
        assertTrue(IrpDatabase.isKnown(protocolsPath, "RC6-6-32"));
    }

    /**
     * Test of getConfigFileVersion method, of class IrpDatabase.
     */
    @Test
    public void testGetConfigFileVersion() {
        System.out.println("getConfigFileVersion");
        String result = instance.getConfigFileVersion();
        // Don't want to update this file everytime the version number changes...
        assertEquals(result.substring(0, 3), "201");
    }

    /**
     * Test of isKnown method, of class IrpDatabase.
     */
    @Test
    public void testIsKnown_String() {
        System.out.println("isKnown");
        assertFalse(instance.isKnown("covfefe"));
        assertTrue(instance.isKnown("NEC1"));
        assertTrue(instance.isKnownExpandAlias("NEC1"));
        assertFalse(instance.isKnown("RC6-6-32"));
        assertTrue(instance.isKnownExpandAlias("RC6-6-32"));
    }

    /**
     * Test of getIrp method, of class IrpDatabase.
     */
    @Test
    public void testGetIrp_String() {
        System.out.println("getIrp");
        String name = "NEC1";
        String expResult = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*)[D:0..255,S:0..255=255-D,F:0..255]";
        String result = instance.getIrp(name);
        assertEquals(result, expResult);
        name = "RC6-6-32";
        expResult = "{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,6:3,-2,2,OEM1:8,S:8,T:1,D:7,F:8,^107m)*,T=1-T){OEM1=128}[D:0..127,S:0..255,F:0..255,T@:0..1=0]";
        assertEquals(instance.getIrp(name), null);
        assertEquals(instance.getIrpExpandAlias(name), expResult);
    }

    /**
     * Test of getNames method, of class IrpDatabase.
     */
    @Test
    public void testGetKeys() {
        System.out.println("getKeys");
        Set<String> result = instance.getKeys();
        assertTrue(result.size() > 100);
    }

    /**
     * Test of getName method, of class IrpDatabase.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String name = "nec1";
        String expResult = "NEC1";
        String result = instance.getName(name);
        assertEquals(result, expResult);
        assertEquals(instance.getName("rc6-6-32"), null);
        assertEquals(instance.getNameExpandAlias("rc6-6-32"), "MCE");
    }

    /**
     * Test of getCName method, of class IrpDatabase.
     */
    @Test
    public void testGetCName() {
        System.out.println("getCName");
        String name = "48-nec1";
        String expResult = "x48_NEC1";
        String result = instance.getCName(name);
        assertEquals(result, expResult);
    }

    /**
     * Test of getCName method, of class IrpDatabase.
     */
    @Test
    public void testGetCName1() {
        System.out.println("getCName1");
        String name = "Blaupunkt_relaxed";
        String expResult = "Blaupunkt_relaxed";
        String result = instance.getCName(name);
        assertEquals(result, expResult);
    }


    /**
     * Test of getMatchingNamesRegexp method, of class IrpDatabase.
     */
    @Test
    public void testGetMatchingNamesRegexp() {
        System.out.println("getMatchingNamesRegexp");
        String regexp = "NEC.*";
        List result = instance.getMatchingNamesRegexp(regexp);
        assertEquals(result.size(), 11);
        result = instance.getMatchingNamesRegexp("RC6.*");
        assertTrue(result.contains("rc6-6-32"));
    }

    /**
     * Test of getDocumentation method, of class IrpDatabase.
     */
    @Test
    public void testGetDocumentation() {
        System.out.println("getDocumentation");
        String name = "Anthem_relaxed";
        String expResult = "Relaxed version of the Anthem protocol.";
        String result = instance.getDocumentation(name);
        assertEquals(result, expResult);
        assertEquals(instance.getDocumentation("GI Cable"), null);
        assertEquals(instance.getDocumentationExpandAlias("ruwido r-step").substring(0, 40), "The repeat frames are not all identical.");
        assertEquals(instance.getDocumentation("adnotam"), "Very similar to RC5, except AdNotam uses two start bits, and no toggle bit.");
    }

    /**
     * Test of getProtocol method, of class IrpDatabase.
     */
    @Test
    public void testGetProtocol() {
        System.out.println("getProtocol");
        Protocol result;
        try {
            instance.getProtocol("covfefe");
            fail();
        } catch (UnknownProtocolException ex) {
        } catch (UnsupportedRepeatException | NameUnassignedException | InvalidNameException | IrpInvalidArgumentException ex) {
            fail();
        }
        try {
            instance.getProtocol("nec1");
            instance.getProtocolExpandAlias("rc6-6-32");
        } catch (UnknownProtocolException | UnsupportedRepeatException | NameUnassignedException | InvalidNameException | IrpInvalidArgumentException ex) {
            fail();
        }
        try {
            instance.getProtocol("rc6-6-32");
            fail();
        } catch (UnsupportedRepeatException | NameUnassignedException | InvalidNameException | IrpInvalidArgumentException ex) {
            fail();
        } catch (UnknownProtocolException ex) {
        }
    }

    /**
     * Test of getNormalFormIrp method, of class IrpDatabase.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetNormalFormIrp() throws Exception {
        System.out.println("getNormalFormIrp");
        String protocolName = "CanalSat";
        int radix = 10;
        String expResult = "{55.5k,250,msb}<-1,1|1,-1>([T=0,1,-1,D:7,S:6,T:1,0:1,F:7,-89m,T=1][1,-1,D:7,S:6,T:1,0:1,F:7,-89m,T=1])*[D:0..127,S:0..63,F:0..127]";
        String result = instance.getNormalFormIrp(protocolName, radix);
        assertEquals(result, expResult);
    }

    @Test
    public void testIterator() {
        System.out.println("testIterator");
        String candidate = "";
        for (NamedProtocol namedProtocol : instance)
            if (namedProtocol.getName().length() > candidate.length())
                candidate = namedProtocol.getName();
        assertEquals(candidate, "Fujitsu_Aircon_old");
    }

    @Test
    public void testProperties() throws UnknownProtocolException {
        System.out.println("testProperties");
        List<String> props = instance.getProperties("nec1", "prefer-over");
        int n = props.size();
        instance.addProperty("nec1", "prefer-over", "halleluja");
        props = instance.getProperties("nec1", "prefer-over");
        assertEquals(props.size(), n+1);
        instance.removeProperties("nec1", "prefer-over");
        props = instance.getProperties("nec1", "prefer-over");
        assertNull(props);
    }

    @Test
    public void testAddProtocol() throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        System.out.println("testAddProtocol");
        String name = "OrtekMCE{2}";
        instance.addProtocol(name,
                "{38.6k,480}<1,-1|-1,1>([][P=1][P=2],4,-1,D:5,P:2,F:6,C:4,-48m)+{C=3+#D+#P+#F}[D:0..31,F:0..63]");
        instance.addProperty(name, "monster", "godzilla");
        NamedProtocol namedProtocol = instance.getNamedProtocol(name);
        assertEquals(namedProtocol.getName(), name);
    }

    @Test
    public void testAddProtocolExpand() throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        System.out.println("testAddProtocolExpand");
        String name = "junk";
        instance.addProtocol(name, "NEC1{D=F,S=F}[F:0..255]");
        instance.getNamedProtocol(name);
        String irp = instance.getIrp(name);
        assertEquals(irp, "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*){D=F,S=F}[F:0..255]");
    }


    @Test
    public void testRemove() throws IOException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException, UnknownProtocolException {
        System.out.println("testRemove");
        IrpDatabase irpDatabase = new IrpDatabase((String) null);
        irpDatabase.getNamedProtocolExpandAlias("RC6-6-32");
        irpDatabase.remove("mce");
        try {
            irpDatabase.getNamedProtocolExpandAlias("RC6-6-32");
            fail();
        } catch (UnknownProtocolException ex) {
        }
    }

    @Test
    public void testPatchFile() throws IOException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, UnknownProtocolException, IrpParseException {
        System.out.println("testPatchFile");
        IrpDatabase irpDatabase = new IrpDatabase((String) null);
        List<String> necExecutors = irpDatabase.getProperties("nec1", "uei-executor");
        assertFalse(necExecutors.isEmpty());
        List<DocumentFragment> aminoExecutors = irpDatabase.getXmlProperties("amino", "uei-executor");
        assertFalse(aminoExecutors.isEmpty());
        irpDatabase.patch(new File("src/test/resources/IrpProtocols-test.xml"));
        try {
            irpDatabase.getNamedProtocol("nec2");
            fail();
        } catch (UnknownProtocolException ex) {
        }

        List<String> list = irpDatabase.getProperties("nec1", "foo");
        assertEquals(list.size(), 1);
        assertTrue(list.contains("bar"));
        NamedProtocol nec1 = irpDatabase.getNamedProtocol("nec1");
        assertEquals(nec1.getDocumentation(), "Nec1 new doc");
        List<String> po = irpDatabase.getProperties("nec1", "prefer-over");
        assertTrue(po.contains("foobar"));

        NamedProtocol foobar = irpDatabase.getNamedProtocol("foobar");
        assertEquals(foobar.getDocumentation(), "Lorem Ipsum");

        List<String> necExecutorsx = irpDatabase.getProperties("nec1", "uei-executofdsfggsfsr");
        List<DocumentFragment> aminoExecutorsx = irpDatabase.getXmlProperties("amino", "uei-sdfsfdfsexecutor");

        irpDatabase.getNamedProtocolExpandAlias("necropHile");
        necExecutors = irpDatabase.getProperties("nec1", "uei-executor");
        assertNull(necExecutors);
        aminoExecutors = irpDatabase.getXmlProperties("amino", "uei-executor");
        assertNull(aminoExecutors);

        NamedProtocol amino = irpDatabase.getNamedProtocol("amino");
        String doc = amino.getDocumentation();
        assertNull(doc);

        assertFalse(irpDatabase.isKnown("covfefe"));
    }
}
