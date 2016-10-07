/*
Copyright (C) 2011, 2015 Bengt Martensson.

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

package org.harctoolbox.irp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the Protocol, per Chapter 1.6--1.7.
 *
 * There are too many public functions in the API...
 *
 */
public class Protocol extends IrpObject {

    private final static Logger logger = Logger.getLogger(Protocol.class.getName());
    // from irpmaster.XmlExport
    public static Document newDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException e) {
        }
        return doc;
    }

    //private String name;
    //private String documentation;
    //private String irp;
    private GeneralSpec generalSpec;
    //private NameEngine nameEngine;
    private ParameterSpecs parameterSpecs;
    private BitspecIrstream bitspecIrstream;
    private IrpParser.ProtocolContext parseTree;
    private ParserDriver parseDriver;
    private NameEngine definitions;
    private NameEngine memoryVariables;

    // True the first time render is called, then false -- to be able to initialize.
    //private boolean virgin = true;

    //private int count = 0;

//    private Document doc = null;
//    private Element root = null;
//    private Element currentElement = null;

    public Protocol(GeneralSpec generalSpec, BitspecIrstream bitspecIrstream, NameEngine definitions, ParameterSpecs parameterSpecs,
            IrpParser.ProtocolContext parseTree) {
        this.parseTree = parseTree;
        this.generalSpec = generalSpec;
        this.bitspecIrstream = bitspecIrstream;
        this.definitions = definitions;
        this.parameterSpecs = parameterSpecs != null ? parameterSpecs : new ParameterSpecs() ;
    }

    /**
     *
     * @param generalSpec
     */
    public Protocol(GeneralSpec generalSpec) {
        this(generalSpec, null, null, null, null);
    }

    public Protocol() {
        this(new GeneralSpec());
    }

    /**
     * Main constructor.
     *
     * @param irpString
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws org.harctoolbox.irp.InvalidRepeatException
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public Protocol(String irpString)
            throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException {
        this(new ParserDriver(irpString));
    }

    public Protocol(ParserDriver parserDriver)
            throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException {
        this(parserDriver.getParser().protocol());
        this.parseDriver = parserDriver;
//        setup(parserDriver.getParser().protocol());

    }

//    public Protocol(/*String name,*/ String irpString/*, String documentation*/) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException {
//        if (irpString == null)
//            throw new NullPointerException("IrpString cannot be null");
//
//        this.irp = irpString;
//        //this.nameEngine = new NameEngine();
//
//
//            ParserDriver.reset();
//            parseDriver = new ParserDriver(irpString);
//            parseTree = parseDriver.getParser().protocol();
//    }

    public Protocol(IrpParser.ProtocolContext parseTree)
            throws IrpSemanticException, IrpSyntaxException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException {
        this(new GeneralSpec(parseTree), new BitspecIrstream(parseTree), new NameEngine(), new ParameterSpecs(parseTree), parseTree);
//        setup(parseTree);
//    }
//
//    private void setup(IrpParser.ProtocolContext parseTree) throws IrpSemanticException, IrpSyntaxException, InvalidRepeatException, ArithmeticException, IncompatibleArgumentException, UnassignedException {
//        this.parseTree = parseTree;
//        try {
//            generalSpec = new GeneralSpec(parseTree);
//            bitspecIrstream = new BitspecIrstream(parseTree);
//            definitions = new NameEngine();
        for (IrpParser.DefinitionsContext defs : parseTree.definitions())
            definitions.parseDefinitions(defs);

        //definitionss = new Defi
        parameterSpecs = new ParameterSpecs(parseTree);
//        } catch (ParseCancellationException ex) {
//            throw new IrpSyntaxException(ex);
//        }

        memoryVariables = new NameEngine();
        for (ParameterSpec parameter : parameterSpecs) {
            if (parameter.hasMemory()) {
                String name = parameter.getName();
                long initVal = parameter.getDefault().toNumber(null);
                memoryVariables.define(name, initVal);
            }
        }

        checkSanity();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Protocol))
            return false;
        Protocol other = (Protocol) obj;
        return
                generalSpec.equals(other.generalSpec)
                && bitspecIrstream.equals(other.bitspecIrstream)
                && parameterSpecs.equals(other.parameterSpecs)
                && definitions.equals(other.definitions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.generalSpec);
        hash = 31 * hash + Objects.hashCode(this.parameterSpecs);
        hash = 31 * hash + Objects.hashCode(this.bitspecIrstream);
        hash = 31 * hash + Objects.hashCode(this.definitions);
        return hash;
    }

    private void checkSanity() throws InvalidRepeatException, IrpSemanticException {
        if (numberOfInfiniteRepeats() > 1) {
            throw new InvalidRepeatException("More than one infinite repeat found. The program does not handle this.");
        }

        if (parameterSpecs.isEmpty()) {
            logger.log(Level.WARNING, "Parameter specs are missing from protocol. Runtime errors due to unassigned variables are possile. Also silent truncation of parameters can occur. Further messages on parameters will be suppressed.");
            parameterSpecs = new ParameterSpecs();
        }
        if (generalSpec == null) {
            throw new IrpSemanticException("GeneralSpec missing from protocol");
        }
    }

    public IrpParser.ProtocolContext getParseTree() {
        return parseTree;
    }

    /**
     *
     * @param nameEngine
     * @return
     * @throws IncompatibleArgumentException
     * @throws IrpSemanticException
     * @throws ArithmeticException
     * @throws UnassignedException
     * @throws IrpSyntaxException
     * @throws org.harctoolbox.irp.DomainViolationException
     */
    public IrSignal toIrSignal(NameEngine nameEngine)
            throws IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, IrpSyntaxException, DomainViolationException {
        IrpUtils.entering(logger, "toIrSignal");
        parameterSpecs.check(nameEngine);
        fetchMemoryVariables(nameEngine);
        nameEngine.add(definitions);

        IrSequence intro  = toIrSequence(nameEngine, Pass.intro);
        IrSequence repeat = toIrSequence(nameEngine, Pass.repeat);
        IrSequence ending = toIrSequence(nameEngine, Pass.ending);
        saveMemoryVariables(nameEngine);
        IrpUtils.entering(logger, "toIrSignal");
        return new IrSignal(intro, repeat, ending, getFrequency(), getDutyCycle());
    }

    private void fetchMemoryVariables(NameEngine nameEngine) throws IrpSyntaxException {
        for (Map.Entry<String, Expression> kvp : memoryVariables) {
            String name = kvp.getKey();
            if (!nameEngine.containsKey(name)) {
                nameEngine.define(name, kvp.getValue());
            }
        }
    }

    private void saveMemoryVariables(NameEngine nameEngine) throws IrpSyntaxException, UnassignedException {
        for (Map.Entry<String, Expression> kvp : memoryVariables) {
            String name = kvp.getKey();
            memoryVariables.define(name, nameEngine.get(name));
        }
    }

    /**
     *
     * @param nameEngine, NameEngine, may be altered.
     * @param pass
     * @return
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.DomainViolationException
     */
    public ModulatedIrSequence toModulatedIrSequence(NameEngine nameEngine, Pass pass) throws IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, IrpSyntaxException, DomainViolationException {
        return new ModulatedIrSequence(toIrSequence(nameEngine, pass), getFrequency(), getDutyCycle());
    }

    /**
     *
     * @param nameEngine Name engine, may be altered
     * @param pass
     * @return
     * @throws org.harctoolbox.ircore.IncompatibleArgumentException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.DomainViolationException
     */
    private IrSequence toIrSequence(NameEngine nameEngine, Pass pass)
            throws IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, IrpSyntaxException, DomainViolationException {
        IrpUtils.entering(logger, "toIrSequence", pass);
        EvaluatedIrStream evaluatedIrStream = bitspecIrstream.evaluate(IrSignal.Pass.intro, pass, nameEngine, generalSpec);
        IrSequence irSequence = evaluatedIrStream.toIrSequence();
        IrpUtils.exiting(logger, "toIrSequence", pass);
        return irSequence;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitspecIrstream.numberOfInfiniteRepeats();
    }

/*
    public long evaluateName(String name) throws UnassignedException, DomainViolationException {
        long result;
        //Debug.debugNameEngine("evaluateName(" + name + ") called");
        IrpParser.Bare_expressionContext tree = nameEngine.get(name);
        if (tree == null)
            throw new UnassignedException("Name `" + name + "' not assigned.");
        try {
            result = ASTTraverser.expression(this, tree);
            Debug.debugExpressions("finished evaluating `" + name + "' = " + result + " without exceptions");
        } catch (StackOverflowError ex) {
            throw new UnassignedException("Name `" + name + "' appears to be recursively defined; stack overflow catched.");
        }
        return result;
    }

    public long evaluateName(String name, long dflt) {
        try {
            return evaluateName(name);
        } catch (UnassignedException | DomainViolationException ex) {
            return dflt;
        }
    }

    public long tryEvaluateName(String name) {
        try {
            return evaluateName(name);
        } catch (UnassignedException ex) {
            System.err.println("Variablename " + name + " not currently assigned.");
            return 0;
        } catch (DomainViolationException ex) {
            System.err.println(ex.getMessage());
            return 0;
        }
    }

    public void assign(String name, long value) {
        nameEngine.assign(name, value);
    }

    public void assign(String str) throws IncompatibleArgumentException {
        String s = str.trim();
        if (s.startsWith("{"))
            nameEngine.readDefinitions(s);
        else {
            String[] kw = s.split("=");
            if (kw.length != 2)
                throw new IncompatibleArgumentException("Invalid assignment: " + s);

            assign(kw[0], IrpUtils.parseLong(kw[1], false));
        }
    }

    public void assign(String[] args, int skip) throws IncompatibleArgumentException {
        for (int i = skip; i < args.length; i++)
            assign(args[i]);
    }*/


    public BitDirection getBitDirection() {
        return generalSpec.getBitDirection();
    }

    public double getFrequency() {
        return generalSpec.getFrequency();
    }

    public double getUnit() {
        return generalSpec.getUnit();
    }

    public double getDutyCycle() {
        return generalSpec.getDutyCycle();
    }

    public String toStringTree() throws IrpSyntaxException {
        return parseDriver != null ? parseTree.toStringTree(parseDriver.getParser()) : null;
    }

    long getMemoryVariable(String name) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return memoryVariables.get(name).toNumber();
    }

    boolean hasMemoryVariable(String name) {
        return memoryVariables.containsKey(name);
    }

    public boolean isStandardPWM() {
        return bitspecIrstream.isStandardPWM(definitions, generalSpec);
    }

    public boolean isPWM4() {
        return bitspecIrstream.isPWM4(definitions, generalSpec);
    }

    boolean isPWM16() {
        return bitspecIrstream.isPWM16(definitions, generalSpec);
    }

    public boolean isBiphase() {
        return bitspecIrstream.isBiphase(definitions, generalSpec);
    }

    public boolean isTrivial(boolean inverted) {
        return bitspecIrstream.isTrivial(definitions, generalSpec, inverted);
    }

    public boolean interleavingOk() {
        return bitspecIrstream.interleavingOk(definitions, generalSpec);
    }

    public boolean isRPlus() {
        return bitspecIrstream.isRPlus();
    }

    public boolean startsWithDuration() {
        return bitspecIrstream.startsWithDuration();
    }

    public boolean hasVariation() {
        return bitspecIrstream.hasVariation(true);
    }

//    public Document toDocument() throws IrpSyntaxException {
//        Document document = newDocument();
//        document.appendChild(toElement(document));
//        return document;
//    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element root = document.createElement("protocol");
        //root.setAttribute("name", name);
        //Element irpElement = document.createElement("irp");
        //irpElement.appendChild(document.createCDATASection(irp));
        //root.appendChild(irpElement);
        //Element docu = document.createElement("documentation");
        //docu.appendChild(document.createCDATASection("\n" + documentation + "\n"));
        //root.appendChild(docu);

        //Element stringTree = document.createElement("stringTree");
        //stringTree.appendChild(document.createCDATASection(toStringTree()));
        //root.appendChild(stringTree);

        Element renderer = document.createElement("implementation");
        root.appendChild(renderer);
        Element generalSpecElement = generalSpec.toElement(document);
        renderer.appendChild(generalSpecElement);
        //renderer.appendChild(nameEngine.toElement(document));
        Element bitspecIrstreamElement = bitspecIrstream.toElement(document);
        renderer.appendChild(bitspecIrstreamElement);
        Element definitionsElement = definitions.toElement(document);
        renderer.appendChild(definitionsElement);
        renderer.appendChild(parameterSpecs.toElement(document));
        return root;
    }

    @Override
    public String toIrpString() {
        return toIrpString(10, false);
    }

    public String toIrpString(int radix, boolean usePeriods) {
        return
                generalSpec.toIrpString(usePeriods)
                + bitspecIrstream.toIrpString()
                + definitions.toIrpString(radix)
                + parameterSpecs.toIrpString();
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    public Map<String, Long> randomParameters() throws IrpSyntaxException {
        return parameterSpecs.random();
    }

    public Map<String, Long> recognize(IrSignal irSignal) {
        return recognize(irSignal, true);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean keepDefaulted) {
        return recognize(irSignal, keepDefaulted, true, IrCoreUtils.defaultFrequencyTolerance, IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean keepDefaulted, boolean checkFrequency,
            double frequencyTolerance, double absoluteTolerance, double relativeTolerance) {
        IrpUtils.entering(logger, Level.FINE, "recognize", this);
        Map<String, Long> names = new HashMap<>(8);

        boolean success = (!checkFrequency || IrCoreUtils.approximatelyEquals(getFrequency(), irSignal.getFrequency(), frequencyTolerance, 0.0));
        if (success)
            success = process(names, irSignal.getIntroSequence(), IrSignal.Pass.intro, absoluteTolerance, relativeTolerance);
        if (success)
            success = process(names, irSignal.getRepeatSequence(), IrSignal.Pass.repeat, absoluteTolerance, relativeTolerance);
        if (success)
            success = process(names, irSignal.getEndingSequence(), IrSignal.Pass.ending, absoluteTolerance, relativeTolerance);
        if (!success) {
            IrpUtils.exiting(logger, "recognize", "fail");
            return null;
        }

        parameterSpecs.reduceNamesMap(names, keepDefaulted);

        IrpUtils.entering(logger, Level.FINE, "recognize", names);
        return names;
    }

    private boolean process(Map<String, Long> names, IrSequence irSequence, IrSignal.Pass pass, double absoluteTolerance, double relativeTolerance) {
        //RecognizeData inData = new RecognizeData(irSequence);
        RecognizeData recognizeData = new RecognizeData(generalSpec, definitions, irSequence, interleavingOk(), names, absoluteTolerance, relativeTolerance);
        boolean status = recognize(recognizeData, pass);
        if (!status)
            return false;

//        try {
            //        //if (recognizeData.needsFinalParameterCheck())
//        try {
//            //recognizeData.getParameterCollector().refresh();
//            recognizeData.getParameterCollector().checkConsistencyWith(nameEngine);
//        } catch (NameConflictException | IrpSyntaxException | IncompatibleArgumentException ex) {
//            logger.warning(ex.getMessage());
//            return false;
//        } catch (UnassignedException ex) {
//            logger.log(Level.WARNING, "Equation solving not implemented: {0}", ex.getMessage());
//            return false;
//        }
//
        try {
            recognizeData.transferToNamesMap(names);
            recognizeData.checkConsistency(names);
        } catch (NameConflictException ex) {
            return false;
        } catch (IrpSyntaxException | IncompatibleArgumentException | UnassignedException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
//        } catch (IrpSyntaxException | NameConflictException | UnassignedException | IncompatibleArgumentException ex) { // FIXME
//            logger.log(Level.SEVERE, null, ex);
//        }
        return recognizeData.isSuccess();
    }

    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass) {
        IrpUtils.entering(logger, "recognize " + pass, this);
        //RecognizeData recognizeData;
        boolean success = false;
        try {
            success = bitspecIrstream.recognize(recognizeData, pass, new ArrayList<>(0));
        } catch (NameConflictException | ArithmeticException ex) {
            //recognizeData = null;
            logger.log(Level.INFO, ex.getMessage());
        }
        //IrpUtils.exiting(logger, "recognize", recognizeData != null ? recognizeData.getParameterCollector().toString() : "null");
        IrpUtils.exiting(logger, "recognize " + pass, success ? "pass" : "fail");
        return success;
    }

    @Override
    public int weight() {
        return generalSpec.weight() + bitspecIrstream.weight()
                + definitions.weight() + parameterSpecs.weight();
    }

    /*
    public long getParameterMin(String name) throws UnassignedException {
        ParameterSpec ps = parameterSpecs.getParameterSpec(name);
        if (ps == null)
            throw new UnassignedException("Parameter " + name + " not assigned.");

        return ps.getMin();
    }

    public long getParameterMax(String name) throws UnassignedException {
        ParameterSpec ps = parameterSpecs.getParameterSpec(name);
        if (ps == null)
            throw new UnassignedException("Parameter " + name + " not assigned.");

        return ps.getMax();
    }

    public boolean hasParameter(String name) {
        return parameterSpecs.getNames().contains(name);
    }

    /**
     * Returns a set of the names of all parameters present in the protocol.
     * @return a Set of the names of all parameters present in the protocol.
     * /
    public Set<String> getParameterNames() {
        return parameterSpecs.getNames();
    }

    /**
     * Checks if the named parameter has memory.
     * @param name Name of the parameters.
     * @return existence of memory for the parameter given as argument-
     * @throws UnassignedException If there is no parameters with the name given as parameter.
     * /
    public boolean hasParameterMemory(String name) throws UnassignedException {
        ParameterSpec parameterSpec = parameterSpecs.getParameterSpec(name);
        if (parameterSpec == null)
            throw new UnassignedException("Parameter " + name + " not assigned.");
        return parameterSpec.hasMemory();
    }

    /**
     * Does this protocol have parameters other than the standard ones (F, D, S, T)?
     * @return existence of other parameters.
     * /
    public boolean hasAdvancedParameters() {
        for (String param : parameterSpecs.getNames())
            if (!(param.equals("F") || param.equals("D") || param.equals("S") || param.equals("T")))
                return true;

        return false;
    }

    public String getIrp() {
        return irpString;
    }

    /**
     *
     * @param name
     * @param actualParameters
     * @return Default value of parameter in first argument, taking variable assignment in second input into account.
     * @throws UnassignedException
     * @throws DomainViolationException
     * /
    public long getParameterDefault(String name, HashMap<String, Long> actualParameters) throws UnassignedException, DomainViolationException {
        ParameterSpec ps = parameterSpecs.getParameterSpec(name);
        if (ps == null)
            throw new UnassignedException("Parameter " + name + " not assigned.");

        CommonTree t = ps.getDefault();
        if (t == null)
            return IrpUtils.invalid;

        //CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        Protocol newProtocol = new Protocol();
        try {
            newProtocol.nameEngine.loadActualParameters(actualParameters, parameterSpecs);
        } catch (DomainViolationException ex) {
            System.err.println(ex.getMessage());
        }
        return ASTTraverser.expression(newProtocol, t);
    }

    /**
     * Checks if the named parameter exists and have a default.
     * @param name
     * @return true if the parameter exists and have a default.
     * /
    public boolean hasParameterDefault(String name) {
        ParameterSpec ps = parameterSpecs.getParameterSpec(name);
        return ps != null && ps.getDefault() != null;
    }

    /**
     * Debugging and testing purposes only
     *
     * @return NameEngine as String.
     * /
    public String nameEngineString() {
        return nameEngine.toString();
    }

    /**
     * Creates consisting of parameter values that can be used as part of filenames etc.
     * @param equals String between name and value, often "=",
     * @param separator String between name-value pairs, often ",".
     * @return String
     * /
    public String notationString(String equals, String separator) {
        return nameEngine.notationString(equals, separator);
    }

    @Override
    public String toString() {
        return name + ": " + AST.toStringTree();
    }*/

    //public String toDOT() {
        //DOTTreeGenerator gen = new DOTTreeGenerator();
        //StringTemplate st = (new DOTTreeGenerator()).toDOT(AST);
        //return parseTree.toStringTree(parser);//st.toString();
    //}
/*
    public void setupDOM() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException ex) {
            System.err.println(ex.getMessage());
        }

        root = doc.createElement("protocol");
        root.setAttribute("name", name);
        doc.appendChild(root);
        root.setAttribute("frequency", Long.toString(Math.round(generalSpec.getFrequency())));
        if (generalSpec.getDutyCycle() > 0)
            root.setAttribute("dutycycle", Double.toString(generalSpec.getDutyCycle()));
    }

    public void addSignal(HashMap<String, Long> actualParameters) {
        Element el = doc.createElement("signal");
        for (Entry<String, Long> entry : actualParameters.entrySet())
            el.setAttribute(entry.getKey(), Long.toString(entry.getValue()));

        root.appendChild(el);
        currentElement = el;

    }

    public void addXmlNode(String gid, String content) {
        Element el = doc.createElement(gid);
        el.setTextContent(content);
        currentElement.appendChild(el);
    }

    public void addRawSignalRepresentation(IrSignal irSignal) {
        Element raw_el = doc.createElement("raw");
        currentElement.appendChild(raw_el);
        insertXMLNode(raw_el, irSignal, Pass.intro);
        insertXMLNode(raw_el, irSignal, Pass.repeat);
        insertXMLNode(raw_el, irSignal, Pass.ending);
    }

    private void insertXMLNode(Element parent, IrSignal irSignal, Pass pass) {
        if (irSignal.getLength(pass) > 0) {
            Element el = doc.createElement(pass.name());
            parent.appendChild(el);
            for (int i = 0; i < irSignal.getLength(pass); i++) {
                double time = irSignal.getDouble(pass, i);
                Element duration = doc.createElement(time > 0 ? "flash" : "gap");
                duration.setTextContent(Long.toString(Math.round(Math.abs(time))));
                el.appendChild(duration);
            }
        }
    }

    public void printDOM(OutputStream ostream) {
        (new XmlExport(doc)).printDOM(ostream, null);
    }

    public void printDOM(OutputStream ostream, Document stylesheet) {
        (new XmlExport(doc)).printDOM(ostream, stylesheet, null);
    }

    public Document toDOM() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException ex) {
            System.err.println(ex.getMessage());
            return null;
        }

        Element root = doc.createElement("PROTOCOL");
        root.setAttribute("name", name);
        doc.appendChild(root);
        if (documentation != null) {
            Element docu = doc.createElement("DOCUMENTATION");
            docu.appendChild(doc.createCDATASection(documentation));
            root.appendChild(docu);
        }
        CommonTree t = AST;
        Element parent = root;
        parseTree(doc, t, parent);
        return doc;
    }

    // Traverse the CommonTree, thus populating the DOM tree as first argument.
    private void parseTree(Document doc, CommonTree t, Element parent) {
        parseTree(doc, t, parent, 0);
    }

    private void parseTree(Document doc, CommonTree t, Element parent, int ignore /* = 0 * /) {
        for (int i = ignore; i < t.getChildCount(); i++) {
            CommonTree child = (CommonTree) t.getChild(i);
            String label = child.getText();
            if ("+-* /%?".contains(label) || label.equals("**"))
                label = "OPERATOR";
            //System.out.println(label);
            Element e = null;
            boolean isInteger = false;
            try {
                Integer.parseInt(label);
                isInteger = true;
            } catch (NumberFormatException ex) {
                isInteger = false;
            }

            if (isInteger) {
                e = doc.createElement("INT");
                e.setAttribute("value", label);
                parent.appendChild(e);
            } else if (label.equals("FREQUENCY") || label.equals("DUTYCYCLE") || label.equals("FLASH") || label.equals("GAP") || label.equals("EXTENT") || label.equals("UNIT")) {
                e = doc.createElement(label);
                parent.appendChild(e);

                if (child.getChild(0).getText().equals("FLOAT")) {
                    CommonTree val = (CommonTree) child.getChild(0);
                    e.setAttribute("value", val.getChild(0).getText() + "." + val.getChild(1).getText());
                } else
                    e.setAttribute("value", child.getChild(0).getText());
                if (child.getChildCount() >= 2)
                    e.setAttribute("unit", child.getChild(1).getText());
            } else if (label.equals("PARAMETER_SPEC") || label.equals("PARAMETER_SPEC_MEMORY")) {
                e = doc.createElement(label);
                parent.appendChild(e);
                e.setAttribute("name", child.getChild(0).getText());
                e.setAttribute("min", child.getChild(1).getText());
                e.setAttribute("max", child.getChild(2).getText());
                if (child.getChildCount() >= 4)
                    parseTree(doc, child, e, 3);

            } else if (label.equals("ASSIGNMENT")) {
                e = doc.createElement(label);
                parent.appendChild(e);
                e.setAttribute("name", child.getChild(0).getText());
                parseTree(doc, child, e, 1);

            } else if (label.equals("BITDIRECTION")) {
                e = doc.createElement(label);
                parent.appendChild(e);
                e.setAttribute("dir", child.getChild(0).getText());
            } else if (label.equals("REPEAT_MARKER")) {
                e = doc.createElement(label);
                parent.appendChild(e);
                e.setAttribute("type", child.getChild(0).getText());
            } else if (label.equals("OPERATOR")) {
                e = doc.createElement(label);
                e.setAttribute("type", child.getText());
                parent.appendChild(e);
                parseTree(doc, child, e);
            } else if (label.equals("PARAMETER_SPECS") || label.equals("GENERALSPEC") || label.equals("DEFINITIONS") || label.equals("DEFINITION") || label.equals("IRSTREAM") || label.equals("BARE_IRSTREAM") || label.equals("COMPLEMENT") || label.equals("BITFIELD") || label.equals("BITSPEC_IRSTREAM")|| label.equals("BITSPEC")) {
                e = doc.createElement(label);
                parent.appendChild(e);
                parseTree(doc, child, e);
            } else {
                e = doc.createElement("NAME");
                e.setAttribute("label", label);
                parent.appendChild(e);
                parseTree(doc, child, e);
            }
        }
    }

    public void interactiveRender(UserComm userComm, LinkedHashMap actualVars) {
        int passNo = 0;
        boolean initial = true;
        boolean done = false;
        boolean finalState = false;

        userComm.printMsg(irpString);
        while (! done) {
            try {
                PrimaryIrStream irStream = process(actualVars, passNo, /*considerRepeatMin* / true, initial);
                initial = false;
                finalState = false;
                userComm.printMsg(irStream.toString());
                userComm.printMsg(nameEngine.toString());
                if (passNo == this.evaluateName("$final_state", IrpUtils.invalid)) {
                    userComm.printMsg("Final state reached");
                    finalState = true;
                }
                String line = userComm.getLine("Enter one of `arsiq' for advance, repeat, start, initialize, quit >");
                switch (line.charAt(0)) {
                    case 'a':
                        passNo = finalState ? 0 : passNo + 1;
                        break;
                    case 'i':
                        passNo = 0;
                        initial = true;
                        break;
                    case 'q':
                        done = true;
                        break;
                    case 'r':
                        break;
                    case 's':
                        passNo = 0;
                        break;
                    default:
                        userComm.errorMsg("Unknown command: " + line);
                        done = true;
                        break;
                }
                //done = true;
             } catch (IOException | IrpMasterException ex) {
                userComm.errorMsg(ex.getMessage());
                done = true;
             }
        }
    }

    public PrimaryIrStream process(HashMap<String, Long> actualVars, int passNo, boolean considerRepeatMin) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return process(actualVars, passNo, considerRepeatMin, virgin);
    }

    public PrimaryIrStream process(HashMap<String, Long> actualVars, Pass pass, boolean considerRepeatMin) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return process(actualVars, pass.toInt(), considerRepeatMin, virgin);
    }

    public PrimaryIrStream process(HashMap<String, Long> actualVars, int passNo, boolean considerRepeatMin, boolean initial) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        // Load Definitions
        for (int i = 0; i < AST.getChildCount(); i++) {
            CommonTree ch = (CommonTree) AST.getChild(i);
            if (ch.getText().equals("DEFINITIONS"))
                nameEngine.readDefinitions(ch);
        }
        Debug.debugNameEngine(nameEngine.toString());

        // Defaults
        Debug.debugParameters(parameterSpecs.toString());

        if (initial)
            count = 0;

        nameEngine.assign("$count", count);

        nameEngine.loadDefaults(parameterSpecs, initial);
        Debug.debugNameEngine(nameEngine.toString());

        // Actual values
        Debug.debugParameters(actualVars.toString());
        // Read actual parameters into the name engine, thowing DomainViolationException if appropriate.
        nameEngine.loadActualParameters(actualVars, parameterSpecs);
        Debug.debugNameEngine(nameEngine.toString());

        // Check that parameters have values (throwing UnassignedException if appropriate).
        nameEngine.checkAssignments(parameterSpecs);

        // Now do the real work
        PrimaryIrStream irStream = ASTTraverser.bitspec_irstream(passNo, considerRepeatMin, this, topBitspecIrsteam);
        irStream.assignBitSpecs();
        count++;
        nameEngine.assign("$count", count);
         // = 8
        Debug.debugASTParser("finished parsing AST without exceptions.");
        // = 16
        Debug.debugNameEngine(nameEngine.toString());
        // 4096
        Debug.debugIrStreams("ProcessAST: " + irStream);

        virgin = false;
        return irStream;
    }

    public IrSequence render(HashMap<String, Long>actualVars, int pass, boolean considerRepeatMins, boolean initialize) throws IncompatibleArgumentException, UnassignedException, DomainViolationException, InvalidRepeatException {
        PrimaryIrStream irStream = process(actualVars, pass, considerRepeatMins, initialize);
        return new IrSequence(irStream);
    }

    public IrSequence render(HashMap<String, Long>actualVars, Pass pass, boolean considerRepeatMins, boolean initialize) throws IncompatibleArgumentException, UnassignedException, DomainViolationException, InvalidRepeatException {
        PrimaryIrStream irStream = process(actualVars, pass.toInt(), considerRepeatMins, initialize);
        return new IrSequence(irStream);
    }

    public IrSignal renderIrSignal(HashMap<String, Long>actualVars, int pass, boolean considerRepeatMins) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        virgin = true;
        IrSequence intro  = (pass == Pass.intro.toInt()  || pass == IrpUtils.all) ? render(actualVars, Pass.intro,  considerRepeatMins,  true) : null; //TODO: what is correct?
        IrSequence repeat = (pass == Pass.repeat.toInt() || pass == IrpUtils.all) ? render(actualVars, Pass.repeat, false, false) : null;
        IrSequence ending = (pass == Pass.ending.toInt() || pass == IrpUtils.all) ? render(actualVars, Pass.ending, false, false) : null;
        return new IrSignal(getFrequency(), getDutyCycle(), intro, repeat, ending);
    }

    public IrSignal renderIrSignal(HashMap<String, Long>actualVars, int pass) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return renderIrSignal(actualVars, pass, true);
    }

    public IrSignal renderIrSignal(HashMap<String, Long>actualVars) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return renderIrSignal(actualVars, (int) IrpUtils.all);
    }

    public IrSignal renderIrSignal(HashMap<String, Long>actualVars, boolean considerRepeatMins) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return renderIrSignal(actualVars, (int) IrpUtils.all, considerRepeatMins);
    }

    private static void assignIfValid(HashMap<String, Long> actualVars, String name, long value) {
        if (value != IrpUtils.invalid)
            actualVars.put(name, value);
    }
    public IrSignal renderIrSignal(long device, long subdevice, long function) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return renderIrSignal(device, subdevice, function, -1L);
    }

    public IrSignal renderIrSignal(long device, long subdevice, long function, long toggle) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        HashMap<String, Long> actualVars = new HashMap<>(3);
        assignIfValid(actualVars, "D", device);
        assignIfValid(actualVars, "S", subdevice);
        assignIfValid(actualVars, "F", function);
        assignIfValid(actualVars, "T", toggle);
        return renderIrSignal(actualVars);
    }

    public IrSignal renderIrSignal(int device, int subdevice, int function) throws DomainViolationException, UnassignedException, IncompatibleArgumentException, InvalidRepeatException {
        return renderIrSignal((long) device, (long) subdevice, (long) function);
    }

    public IrSequence tryRender(HashMap<String, Long> ivs, int pass, boolean considerRepeatMins, boolean initialize) {
        boolean success = false;
        IrSequence irSequence = null;

        try {
            irSequence = render(ivs, pass, considerRepeatMins, initialize);
        } catch (IrpMasterException ex) {
            System.err.println(ex.getMessage());
        }
        return irSequence;
    }

    public IrSequence tryRender(HashMap<String, Long> ivs, int pass, boolean considerRepeatMins) {
        return tryRender(ivs, pass, considerRepeatMins, this.virgin);
    }

     /**
     * Returns a parameter HashMap<String, Long> suitable for using as argument to renderIRSignal by evaluating the arguments.
     * @param additionalParams String of assignments like a=12 b=34 c=56
     * @return HashMap<String, Long> for using as argument to renderIrSignal
     * /
    public static HashMap<String, Long> parseParams(String additionalParams) {
        HashMap<String, Long> params = new HashMap<>();
        String[] arr = additionalParams.split("[,=\\s;]+");
        //for (int i = 0; i < arr.length; i++)
        //    System.out.println(arr[i]);
        for (int i = 0; i < arr.length/2; i++)
            params.put(arr[2*i], IrpUtils.parseLong(arr[2*i+1], false));

        return params;
    }

    /**
     * Returns a parameter HashMap<String, Long> suitable for using as argument to renderIrSignal by evaluating the arguments.
     * The four first parameters overwrite the parameters in the additional parameters, if in conflict.
     * @param D device number. Use -1 for not assigned.
     * @param S subdevice number. Use -1 for not assigned.
     * @param F function number (obc, command number). Use -1 for not assigned.
     * @param T toggle. Use -1 for not assigned.
     * @param additionalParams String of assignments like a=12 b=34 c=56
     * @return HashMap<String, Long> for using as argument to renderIrSignal
     * /
    public static HashMap<String, Long> parseParams(int D, int S, int F, int T, String additionalParams) {
        HashMap<String, Long> params = parseParams(additionalParams);
        assignIfValid(params, "D", (long) D);
        assignIfValid(params, "S", (long) S);
        assignIfValid(params, "F", (long) F);
        assignIfValid(params, "T", (long) T);
        return params;
    }

    /**
     * "Smart" method for decoding parameters. If the first argument contains the character "=",
     * the arguments are assume to be assignments using the "=". Otherwise,
     *
     * <ul>
     * <li>one argument is supposed to be F
     * <li>two argumente are supposed to be D and F
     * <li>three arguments are supposed to be D, S, and F
     * <li>four arguments are supposed to be D, S, F, and T.
     * </ul>
     *
     * @param args String array of parameters
     * @param skip Number of elements in the args to skip
     * @return parameter HashMap<String, Long> suitable for rendering signals
     * @throws IncompatibleArgumentException
     * /
    public static HashMap<String, Long> parseParams(String[] args, int skip) throws IncompatibleArgumentException {
        return args[skip].contains("=")
                ? parseNamedProtocolArgs(args, skip)
                : parsePositionalProtocolArgs(args, skip);
    }

    private static HashMap<String, Long> parseNamedProtocolArgs(String[] args, int skip) throws IncompatibleArgumentException {
        HashMap<String, Long> params = new HashMap<>();
            for (int i = skip; i < args.length; i++) {
                String[] str = args[i].split("=");
                if (str.length != 2)
                    throw new IncompatibleArgumentException("`" + args[i] + "' is not a parameter assignment");
                String name = str[0].trim();
                long value = Long.parseLong(str[1]);
                params.put(name, value);
            }
            return params;
    }*/

    /**
     * @return the nameEngine
     */
//    public NameEngine getNameEngine() {
//        return nameEngine;
//    }
}
