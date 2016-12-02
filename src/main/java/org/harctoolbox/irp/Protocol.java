/*
Copyright (C) 2016 Bengt Martensson.

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.gui.TreeViewer;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;
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

    private GeneralSpec generalSpec;
    private ParameterSpecs parameterSpecs;
    private BitspecIrstream bitspecIrstream;
    private IrpParser.ProtocolContext parseTree;
    private ParserDriver parseDriver;
    private NameEngine definitions;
    private NameEngine memoryVariables;
    private IrpParser parser = null;

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

    /**
     *
     * @param nameEngine
     * @return
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws IrpSemanticException
     * @throws ArithmeticException
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     * @throws UnassignedException
     * @throws org.harctoolbox.irp.DomainViolationException
     */
    public IrSignal toIrSignal(NameEngine nameEngine) throws InvalidNameException, UnassignedException, DomainViolationException, IrpSemanticException, OddSequenceLenghtException {
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
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public ModulatedIrSequence toModulatedIrSequence(NameEngine nameEngine, Pass pass) throws UnassignedException, InvalidNameException, IrpSemanticException, OddSequenceLenghtException {
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
    private IrSequence toIrSequence(NameEngine nameEngine, Pass pass) throws UnassignedException, InvalidNameException, IrpSemanticException, OddSequenceLenghtException {
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

    public String toStringTree() {
        return parseDriver != null ? parseTree.toStringTree(parseDriver.getParser()) : null;
    }

    long getMemoryVariable(String name) throws UnassignedException {
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

    public boolean hasExtent() {
        return bitspecIrstream.hasExtent();
    }

    public BitspecIrstream getBitspecIrstream() {
        return bitspecIrstream;
    }

    @Override
    public Element toElement(Document document) {
        return toElement(document, false);
    }

    public Element toElement(Document document, boolean split) {
        Element root = super.toElement(document);
        Element renderer = document.createElement(Protocol.class.getSimpleName());
        root.appendChild(renderer);
        XmlUtils.addBooleanAttributeIfTrue(renderer, "toggle", hasMemoryVariable("T"));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "standardPwm", isStandardPWM());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "pwm4", isPWM4());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "pwm16", isPWM16());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "biphase", isBiphase());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "trivial", isTrivial(false));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "invTrivial", isTrivial(true));
        XmlUtils.addBooleanAttributeIfTrue(renderer, "interleavingOk", interleavingOk());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "startsWithDuration", startsWithDuration());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "hasVariation", hasVariation());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "rplus", isRPlus());
        Element generalSpecElement = generalSpec.toElement(document);
        renderer.appendChild(generalSpecElement);
        Element bitspecIrstreamElement = bitspecIrstream.toElement(document, split);
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

    public Map<String, Long> randomParameters() {
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
        RecognizeData recognizeData = new RecognizeData(generalSpec, definitions, irSequence, interleavingOk(), names, absoluteTolerance, relativeTolerance);
        boolean status = recognize(recognizeData, pass);
        if (!status)
            return false;

        try {
            recognizeData.transferToNamesMap(names);
            recognizeData.checkConsistency(names);
        } catch (NameConflictException ex) {
            return false;
        } catch (UnassignedException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return recognizeData.isSuccess();
    }

    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass) {
        IrpUtils.entering(logger, "recognize " + pass, this);
        boolean success = false;
        try {
            success = bitspecIrstream.recognize(recognizeData, pass, new ArrayList<>(0));
        } catch (NameConflictException | InvalidNameException | IrpSemanticException | ArithmeticException ex) {
            logger.log(Level.INFO, ex.getMessage());
        }
        IrpUtils.exiting(logger, "recognize " + pass, success ? "pass" : "fail");
        return success;
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
        str.append((int) getFrequency());
        str.append("\t").append(hasMemoryVariable("T") ? "toggle\t" : "\t");
        str.append(isStandardPWM() ? "PWM" : "");
        str.append(isPWM4() ? "PWM4" : "");
        str.append(isPWM16() ? "PWM16" : "");
        str.append(isBiphase() ? "Biphase" : "");
        str.append(isTrivial(false) ? "Trivial" : "");
        str.append(isTrivial(true) ? "invTrivial" : "");
        str.append("\t").append(interleavingOk() ? "interleaving\t" : "\t");
        str.append(startsWithDuration() ? "SWD\t" : "\t");
        str.append(hasVariation() ? "variation\t" : "\t");
        str.append(isRPlus() ? "R+" : "");
        return str.toString();
    }
}
