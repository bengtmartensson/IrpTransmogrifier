/*
Copyright (C) 2017 Bengt Martensson.

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.gui.TreeViewer;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the Protocol, per Chapter 1.6--1.7.
 *
 * There are too many public functions in the API...
 *
 */
public class Protocol extends IrpObject implements AggregateLister {

    private final static Logger logger = Logger.getLogger(Protocol.class.getName());

    private GeneralSpec generalSpec;
    private ParameterSpecs parameterSpecs;
    private BitspecIrstream bitspecIrstream;
    private Variation normalFormVariation;
    private IrpParser.ProtocolContext parseTree;
    private ParserDriver parseDriver;
    private NameEngine definitions;
    private NameEngine memoryVariables;
    private IrpParser parser = null;
    private Boolean interleavingFlash = null;
    private Boolean interleavingGap = null;

    public Protocol(GeneralSpec generalSpec, BitspecIrstream bitspecIrstream, NameEngine definitions, ParameterSpecs parameterSpecs,
            IrpParser.ProtocolContext parseTree) {
        this.parseTree = parseTree;
        this.generalSpec = generalSpec;
        this.bitspecIrstream = bitspecIrstream;
        this.definitions = definitions;
        this.parameterSpecs = parameterSpecs != null ? parameterSpecs : new ParameterSpecs();
        computeNormalForm();
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
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.UnsupportedRepeatException
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public Protocol(String irpString) throws IrpSemanticException, InvalidNameException, UnassignedException {
        this(new ParserDriver(irpString));
    }

    public Protocol(ParserDriver parserDriver) throws IrpSemanticException, InvalidNameException, UnassignedException {
        this(parserDriver.getParser().protocol());
        this.parser = parserDriver.getParser();
        this.parseDriver = parserDriver;
    }

    public Protocol(IrpParser.ProtocolContext parseTree) throws IrpSemanticException, InvalidNameException, UnassignedException {
        this(new GeneralSpec(parseTree), new BitspecIrstream(parseTree), new NameEngine(), new ParameterSpecs(parseTree), parseTree);
        for (IrpParser.DefinitionsContext defs : parseTree.definitions())
            definitions.parseDefinitions(defs);

        parameterSpecs = new ParameterSpecs(parseTree);
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

    private void computeNormalForm() {
        BareIrStream intro  = bitspecIrstream.extractPass(IrSignal.Pass.intro);
        //introDurations = bitspecIrstream.numberOfDurations(Pass.intro);
        BareIrStream repeat = bitspecIrstream.extractPass(IrSignal.Pass.repeat);
        //repeatDurations = bitspecIrstream.numberOfDurations(IrSignal.Pass.repeat);
        BareIrStream ending = bitspecIrstream.extractPass(IrSignal.Pass.ending);
        //endingDurations = bitspecIrstream.numberOfDurations(IrSignal.Pass.ending);
        normalFormVariation = new Variation(intro, repeat, ending);
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

    private void checkSanity() throws UnsupportedRepeatException, IrpSemanticException {
        if (numberOfInfiniteRepeats() > 1) {
            throw new UnsupportedRepeatException("More than one infinite repeat found. The program does not handle this.");
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

    public Protocol normalFormProtocol() {
        List<IrStreamItem> list = new ArrayList<>(1);
        list.add(normalFormVariation);
        return mkProtocol(new BareIrStream(list));
    }

    public Protocol normalForm(IrSignal.Pass pass) {
        return mkProtocol(normalFormVariation.select(pass));
    }
    public BareIrStream normalBareIrStream(IrSignal.Pass pass) {
        return normalFormVariation.select(pass);
    }

    private Protocol mkProtocol(BareIrStream bareIrStream) {
        IrStream irStream = new IrStream(bareIrStream);
        BitspecIrstream normalBitspecIrstream = new BitspecIrstream(bitspecIrstream.getBitSpec(), irStream);
        return new Protocol(generalSpec, normalBitspecIrstream, definitions, parameterSpecs, parseTree);
    }

    public String normalFormIrpString() {
        Protocol normal = normalFormProtocol();
        return normal.toIrpString();
    }

    /**
     *
     * @param nameEngine; may be changed
     * @return
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws IrpSemanticException
     * @throws ArithmeticException
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @throws UnassignedException
     * @throws NameConflictException
     * @throws IrpSignalParseException
     * @throws DomainViolationException
     */
    public IrSignal toIrSignal(NameEngine nameEngine) throws InvalidNameException, UnassignedException, DomainViolationException, IrpSemanticException, OddSequenceLengthException, NameConflictException, IrpSignalParseException {
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

    private void fetchMemoryVariables(NameEngine nameEngine) throws InvalidNameException {
        for (Map.Entry<String, Expression> kvp : memoryVariables) {
            String name = kvp.getKey();
            if (!nameEngine.containsKey(name)) {
                nameEngine.define(name, kvp.getValue());
            }
        }
    }

    private void saveMemoryVariables(NameEngine nameEngine) throws InvalidNameException, UnassignedException {
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
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.irp.NameConflictException
     * @throws org.harctoolbox.irp.IrpSignalParseException
     */
    public ModulatedIrSequence toModulatedIrSequence(NameEngine nameEngine, Pass pass) throws UnassignedException, InvalidNameException, IrpSemanticException, OddSequenceLengthException, NameConflictException, IrpSignalParseException {
        return new ModulatedIrSequence(toIrSequence(nameEngine, pass), getFrequency(), getDutyCycle());
    }

    /**
     *
     * @param nameEngine Name engine, may be altered
     * @param pass
     * @return
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.irp.IrpSemanticException
     * @throws org.harctoolbox.irp.UnassignedException
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.DomainViolationException
     */
    private IrSequence toIrSequence(NameEngine nameEngine, Pass pass) throws UnassignedException, InvalidNameException, IrpSemanticException, OddSequenceLengthException, NameConflictException, IrpSignalParseException {
        IrpUtils.entering(logger, "toIrSequence", pass);
        RenderData renderData = new RenderData(generalSpec, nameEngine);
        Protocol reducedProtocol = normalForm(pass);
        reducedProtocol.bitspecIrstream.render(renderData, new ArrayList<>(0));
        IrSequence irSequence = renderData.toIrSequence();
        IrpUtils.exiting(logger, "toIrSequence", pass);
        return irSequence;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitspecIrstream.numberOfInfiniteRepeats();
    }

    public BitDirection getBitDirection() {
        return generalSpec.getBitDirection();
    }

    public Double getFrequency() {
        return generalSpec.getFrequency();
    }

    public double getUnit() {
        return generalSpec.getUnit();
    }

    public Double getDutyCycle() {
        return generalSpec.getDutyCycle();
    }

    public String toStringTree() {
        return parseDriver != null ? parseTree.toStringTree(parseDriver.getParser()) : null;
    }

    long getMemoryVariable(String name) throws UnassignedException {
        return memoryVariables.get(name).toNumber();
    }

    boolean hasMemoryVariable(String name) {
        return memoryVariables.containsKey(name);
    }

    public boolean isPWM2() {
        return bitspecIrstream.isPWM2(generalSpec, definitions);
    }

    public boolean isPWM4() {
        return bitspecIrstream.isPWM4(generalSpec, definitions);
    }

    boolean isPWM16() {
        return bitspecIrstream.isPWM16(generalSpec, definitions);
    }

    public boolean isBiphase() {
        return bitspecIrstream.isBiphase(generalSpec, definitions);
    }

    public boolean isTrivial(boolean inverted) {
        return bitspecIrstream.isTrivial(generalSpec, definitions, inverted);
    }

    public boolean interleavingOk() {
       return interleavingFlashOk() && interleavingGapOk();
    }

    public boolean interleavingFlashOk() {
        if (interleavingFlash == null)
            interleavingFlash = bitspecIrstream.interleavingOk(DurationType.flash, generalSpec, definitions);
        return interleavingFlash;
    }

    public boolean interleavingGapOk() {
        if (interleavingGap == null)
            interleavingGap = bitspecIrstream.interleavingOk(DurationType.gap, generalSpec, definitions);
        return interleavingGap;
    }

    /**
     * A protocol is Sonytype if it is PWM2 with different flashes, and has interleaving flashes.
     * @return
     */
    public boolean isSonyType() {
        return bitspecIrstream.isSonyType(generalSpec, definitions);
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

    public boolean hasExtent() {
        return bitspecIrstream.hasExtent();
    }

    public BitspecIrstream getBitspecIrstream() {
        return bitspecIrstream;
    }

    @Override
    public Element toElement(Document document) {
        Element root = super.toElement(document);
        Element renderer = document.createElement(Protocol.class.getSimpleName());
        root.appendChild(renderer);
        XmlUtils.addBooleanAttributeIfTrue(renderer, "toggle", hasMemoryVariable("T"));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "pwm2", isPWM2());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "pwm4", isPWM4());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "pwm16", isPWM16());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "biphase", isBiphase());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "trivial", isTrivial(false));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "invTrivial", isTrivial(true));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "interleavingOk", interleavingOk());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "interleavingFlashOk", interleavingFlashOk());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "interleavingGapOk", interleavingGapOk());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "sonyType", isSonyType());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "startsWithDuration", startsWithDuration());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "hasVariation", hasVariation());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "rplus", isRPlus());
        Element generalSpecElement = generalSpec.toElement(document);
        renderer.appendChild(generalSpecElement);
        Element bitspecIrstreamElement = bitspecIrstream.toElement(document);
        bitspecIrstreamElement.appendChild(normalFormElement(document));
        renderer.appendChild(bitspecIrstreamElement);
        Element definitionsElement = definitions.toElement(document);
        renderer.appendChild(definitionsElement);
        renderer.appendChild(parameterSpecs.toElement(document));
        return root;
    }

    public Element normalFormElement(Document document) {
        Element element = document.createElement("NormalForm");
        element.appendChild(mkElement(document, Pass.intro));
        element.appendChild(mkElement(document, Pass.repeat));
        element.appendChild(mkElement(document, Pass.ending));
        return element;
    }

    private Element mkElement(Document document, IrSignal.Pass pass) {
        BareIrStream stream = normalBareIrStream(pass);
        String tagName = IrCoreUtils.capitalize(pass.toString());
        Element element = stream.toElement(document, tagName);
        int bitspecLength = bitspecIrstream.getBitSpec().numberOfDurations();
        Integer noDurations = stream.numberOfDurations(bitspecLength);
        if (noDurations != null)
            element.setAttribute("numberOfDurations", Integer.toString(noDurations)); // overwriting is OK

        return element;
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

    public Map<String, Long> randomParameters() {
        return parameterSpecs.random();
    }

    public Map<String, Long> recognize(IrSignal irSignal) throws IrpSignalParseException, DomainViolationException, NameConflictException, UnassignedException, InvalidNameException, IrpSemanticException {
        return recognize(irSignal, true);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean keepDefaulted) throws IrpSignalParseException, DomainViolationException, NameConflictException, UnassignedException, InvalidNameException, IrpSemanticException {
        return recognize(irSignal, keepDefaulted, IrCoreUtils.defaultFrequencyTolerance, IrCoreUtils.defaultAbsoluteTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean keepDefaulted,
            double frequencyTolerance, double absoluteTolerance, double relativeTolerance)
            throws IrpSignalParseException, DomainViolationException, NameConflictException, UnassignedException, InvalidNameException, IrpSemanticException {
        IrpUtils.entering(logger, Level.FINE, "recognize", this);
        checkFrequency(irSignal.getFrequency(), frequencyTolerance);
        ParameterCollector names = new ParameterCollector();

        decode(names, irSignal.getIntroSequence(), IrSignal.Pass.intro, absoluteTolerance, relativeTolerance);
        decode(names, irSignal.getRepeatSequence(), IrSignal.Pass.repeat, absoluteTolerance, relativeTolerance);
        decode(names, irSignal.getEndingSequence(), IrSignal.Pass.ending, absoluteTolerance, relativeTolerance);

        Map<String, Long> result = names.collectedNames();
        parameterSpecs.reduceNamesMap(result, keepDefaulted);
        IrpUtils.entering(logger, Level.FINE, "recognize", result);
        return result;
    }

    private void checkFrequency(double frequency, double frequencyTolerance) throws IrpSignalParseException {
        boolean success = frequencyTolerance < 0 || IrCoreUtils.approximatelyEquals(getFrequency(), frequency, frequencyTolerance, 0.0);
        if (!success)
            throw new IrpSignalParseException("Frequency does not match");
    }

    private void decode(ParameterCollector names, IrSequence irSequence, IrSignal.Pass pass, double absoluteTolerance, double relativeTolerance) throws DomainViolationException, NameConflictException, UnassignedException, InvalidNameException, IrpSemanticException, IrpSignalParseException {
        RecognizeData recognizeData = new RecognizeData(generalSpec, definitions, irSequence, interleavingOk(), names, absoluteTolerance, relativeTolerance);
        Protocol reducedProtocol = normalForm(pass);
        //traverse(recognizeData, pass);
        reducedProtocol.decode(recognizeData);
        recognizeData.checkConsistency();
        checkDomain(names);
    }

    public void decode(RecognizeData recognizeData) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        bitspecIrstream.decode(recognizeData, new ArrayList<>(0));
        if (!recognizeData.isFinished())
            throw new IrpSignalParseException("IrSequence not fully matched");
    }

    @Override
    public int weight() {
        return generalSpec.weight() + bitspecIrstream.weight()
                + definitions.weight() + parameterSpecs.weight();
    }

    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    protected ParameterSpecs getParameterSpecs() {
        return parameterSpecs;
    }

    /**
     * @return the definitions
     */
    public NameEngine getDefinitions() {
        return definitions;
    }

    public TreeViewer toTreeViewer() {
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        return new TreeViewer(ruleNames, parseTree);
    }

    public String classificationString() {
        StringBuilder str = new StringBuilder(128);
        str.append((int) (getFrequency() != null ? getFrequency() : GeneralSpec.defaultFrequency));
        str.append("\t").append(hasMemoryVariable("T") ? "toggle\t" : "\t");
        str.append(isPWM2() ? "PWM2" : "");
        str.append(isPWM4() ? "PWM4" : "");
        str.append(isPWM16() ? "PWM16" : "");
        str.append(isBiphase() ? "Biphase" : "");
        str.append(isTrivial(false) ? "Trivial" : "");
        str.append(isTrivial(true) ? "invTrivial" : "");
        str.append("\t").append(interleavingOk() ? "interleaving\t" : "\t");
        str.append("\t").append(interleavingFlashOk() ? "flashint\t" : "\t");
        str.append("\t").append(interleavingGapOk() ? "gapint\t" : "\t");
        str.append("\t").append(isSonyType() ? "sony\t" : "\t");
        str.append(startsWithDuration() ? "SWD\t" : "\t");
        str.append(hasVariation() ? "variation\t" : "\t");
        str.append(isRPlus() ? "R+" : "");
        return str.toString();
    }

    private void checkDomain(ParameterCollector names) throws DomainViolationException {
        for (String kvp : names.getNames()) {
            ParameterSpec parameterSpec = parameterSpecs.getParameterSpec(kvp);
            if (parameterSpec != null)
                parameterSpec.checkDomain(names.getValue(kvp));
        }
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(3);
        addProperties(map, "generalSpec", getGeneralSpec());
        addProperties(map, "parameterSpecs", getParameterSpecs());
        Set<String> variables = getBitspecIrstream().assignmentVariables();
        Set<String> params = getParameterSpecs().getNames();
        variables.removeAll(params);
        map.put("assignmentVariables", variables);
        addProperties(map, "definitions", getDefinitions());
        addProperties(map, "bitSpec", getBitspecIrstream().getBitSpec());

        map.put("sonyType", isSonyType());
        map.put("interleavingOk", interleavingOk());
        map.put("interleavingFlashOk", interleavingFlashOk());
        map.put("interleavingGapOk", interleavingGapOk());

        addSequence(map, IrSignal.Pass.intro);
        addSequence(map, IrSignal.Pass.repeat);
        addSequence(map, IrSignal.Pass.ending);
        return map;
    }

    private void addSequence(Map<String, Object> map, Pass pass) {
        BareIrStream bareIrSequence = normalBareIrStream(pass);
        Map<String, Object> propMap = bareIrSequence.topLevelPropertiesMap(generalSpec, definitions, bitspecIrstream.getBitSpec().numberOfDurations());
        map.put(pass.toString(), propMap);
    }

    private void addProperties(Map<String, Object> map, String name, AggregateLister listener) {
        Map<String, Object> props = listener.propertiesMap(generalSpec, definitions);
        map.put(name, props);
    }
}
