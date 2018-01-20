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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.gui.TreeViewer;
import org.harctoolbox.analyze.AbstractDecoder;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.ThisCannotHappenException;
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

    private final static int SILLYNESSPENALTY = 3;

    private final static double LOWER_COMMON_FREQUENCY1 = 36000d;
    private final static double UPPER_COMMON_FREQUENCY1 = 40000d;
    private final static double LOWER_COMMON_FREQUENCY2 = 56000d;
    private final static double UPPER_COMMON_FREQUENCY2 = 58000d;

    private static String warn(String message) {
        return "Warning: " + message + "." + IrCoreUtils.LINESEPARATOR;
    }

    private static boolean commonFrequency(double f) {
        return IrCoreUtils.approximatelyEquals(f, 0d)
                || (f >= LOWER_COMMON_FREQUENCY1) && (f <= UPPER_COMMON_FREQUENCY1)
                || (f >= LOWER_COMMON_FREQUENCY2) && (f <= UPPER_COMMON_FREQUENCY2);
    }

    private GeneralSpec generalSpec;
    private ParameterSpecs parameterSpecs;
    private BitspecIrstream bitspecIrstream;
    private Variation normalFormVariation;
    private NameEngine initialDefinitions;
    private NameEngine definitions;
    private NameEngine memoryVariables;
    private Boolean interleavingFlash = null;
    private Boolean interleavingGap = null;
    private ParserDriver parserDriver = null;
    private Class<? extends AbstractDecoder> decoderClass;

    public Protocol(GeneralSpec generalSpec, BitspecIrstream bitspecIrstream, NameEngine definitions, ParameterSpecs parameterSpecs) {
        this(generalSpec, bitspecIrstream, definitions, parameterSpecs, null);
    }

    public Protocol(GeneralSpec generalSpec, BitspecIrstream bitspecIrstream, NameEngine definitions, ParameterSpecs parameterSpecs,
            IrpParser.ProtocolContext parseTree) {
        this(generalSpec, bitspecIrstream, definitions, parameterSpecs, parseTree, null);
    }

    public Protocol(GeneralSpec generalSpec, BitspecIrstream bitspecIrstream, NameEngine definitions, ParameterSpecs parameterSpecs,
            IrpParser.ProtocolContext parseTree, Class<? extends AbstractDecoder> decoderClass) {
        super(parseTree);
        this.generalSpec = generalSpec;
        this.bitspecIrstream = bitspecIrstream;
        this.initialDefinitions = definitions;
        this.decoderClass = decoderClass;
        initializeDefinitions();
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
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.NameUnassignedException
     * @throws org.harctoolbox.irp.UnsupportedRepeatException
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     */
    // TODO: should throw a "real" esxception if antlr parsing fails, now ParseCancellationException
    public Protocol(String irpString) throws UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException {
        this(new ParserDriver(irpString));
    }

    private Protocol(ParserDriver parserDriver) throws UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException {
        this(parserDriver.getParser().protocol());
        this.parserDriver = parserDriver;
    }

    public Protocol(IrpParser.ProtocolContext parseTree) throws UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException {
        this(new GeneralSpec(parseTree), new BitspecIrstream(parseTree), new NameEngine(), new ParameterSpecs(parseTree), parseTree);
        parseTree.definitions().forEach((defs) -> {
            initialDefinitions.parseDefinitions(defs);
        });
        initializeDefinitions();

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

    public String getDecoderName() {
        return decoderClass != null ? decoderClass.getSimpleName() : "";
    }

    private void initializeDefinitions() {
        definitions = initialDefinitions != null ? initialDefinitions.clone() : null;
    }

    public String toStringTree() {
        return toStringTree(parserDriver);
    }

    public TreeViewer toTreeViewer() {
        return toTreeViewer(parserDriver);
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
                && initialDefinitions.equals(other.initialDefinitions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.generalSpec);
        hash = 31 * hash + Objects.hashCode(this.parameterSpecs);
        hash = 31 * hash + Objects.hashCode(this.bitspecIrstream);
        hash = 31 * hash + Objects.hashCode(this.initialDefinitions);
        return hash;
    }

    /**
     * Returns a Protocol with all of the variables which are constant
     * literals in the Definitions replaced by their values.
     * Does not change the containing object, but the returned protocol
     * may share some objects with the original protocol.
     * @return Protocol.
     */
    public Protocol substituteConstantVariables() {
        Map<String, Long> constantVariables = definitions.getNumericLiterals();
        NameEngine newDefs = definitions.remove(constantVariables.keySet());
        BitspecIrstream newBitspecIrstream = bitspecIrstream.substituteConstantVariables(constantVariables);
        Protocol newProtocol = new Protocol(this.generalSpec, newBitspecIrstream, newDefs, parameterSpecs);
        return newProtocol;
    }

    private void checkSanity() throws UnsupportedRepeatException {
        if (numberOfInfiniteRepeats() > 1)
            throw new UnsupportedRepeatException();

        if (parameterSpecs.isEmpty()) {
            logger.log(Level.WARNING, "Parameter specs are missing from protocol. Runtime errors due to unassigned variables are possile. Also silent truncation of parameters can occur. Further messages on parameters will be suppressed.");
            parameterSpecs = new ParameterSpecs();
        }
        if (generalSpec == null) {
            // should have been caught during initial parsing
            throw new ThisCannotHappenException("GeneralSpec missing from protocol");
        }
    }

//    public IrpParser.ProtocolContext getParseTree() {
//        return parseTree;
//    }

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

    private boolean isEmpty(IrSignal.Pass pass) {
        return normalFormVariation.select(pass).isEmpty();
    }

    private Protocol mkProtocol(BareIrStream bareIrStream) {
        IrStream irStream = new IrStream(bareIrStream, RepeatMarker.newRepeatMarker('*'));
        BitspecIrstream normalBitspecIrstream = new BitspecIrstream(bitspecIrstream.getBitSpec(), irStream);
        return new Protocol(generalSpec, normalBitspecIrstream, definitions, parameterSpecs, null);
    }

    public String normalFormIrpString(int radix) {
        Protocol normal = normalFormProtocol();
        return normal.toIrpString(radix);
    }

    /**
     *
     * @param nameEngine; may be changed
     * @return
     * @throws ArithmeticException
     * @throws org.harctoolbox.irp.NameUnassignedException
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     * @throws DomainViolationException
     */
    public IrSignal toIrSignal(NameEngine nameEngine) throws DomainViolationException, NameUnassignedException, IrpInvalidArgumentException {
        IrpUtils.entering(logger, "toIrSignal");
        initializeDefinitions();
        parameterSpecs.check(nameEngine);
        fetchMemoryVariables(nameEngine);
        nameEngine.add(definitions);

        IrSequence intro = toIrSequence(nameEngine, Pass.intro);
        IrSequence repeat = toIrSequence(nameEngine, Pass.repeat);
        IrSequence ending = toIrSequence(nameEngine, Pass.ending);
        saveMemoryVariables(nameEngine);
        IrpUtils.entering(logger, "toIrSignal");
        return new IrSignal(intro, repeat, ending, getFrequency(), getDutyCycle());
    }

    /**
     * Renders the NameEngine as one parameter in the spirit of IRremote.
     * @param nameEngine
     * @return long
     * @throws DomainViolationException
     * @throws NameUnassignedException
     */
    public long renderAsOneParameter(NameEngine nameEngine) throws DomainViolationException, NameUnassignedException {
        initializeDefinitions();
        parameterSpecs.check(nameEngine);
        fetchMemoryVariables(nameEngine);
        nameEngine.add(definitions);

        BitwiseParameter bitwiseParameter = renderAsOneParameter(nameEngine, Pass.intro);
        bitwiseParameter.append(renderAsOneParameter(nameEngine, Pass.repeat));
        bitwiseParameter.append(renderAsOneParameter(nameEngine, Pass.ending));
        //saveMemoryVariables(nameEngine);
        return bitwiseParameter.getValue();
    }

    private BitwiseParameter renderAsOneParameter(NameEngine nameEngine, Pass pass) throws NameUnassignedException {
        Protocol reducedProtocol = normalForm(pass);
        return reducedProtocol.bitspecIrstream.renderAsOneParameter(nameEngine, getBitDirection(), new ArrayList<>(0));
    }


    private void fetchMemoryVariables(NameEngine nameEngine) {
        for (Map.Entry<String, Expression> kvp : memoryVariables) {
            String name = kvp.getKey();
            if (!nameEngine.containsKey(name)) {
                try {
                    nameEngine.define(name, kvp.getValue());
                } catch (InvalidNameException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            }
        }
    }

    private void saveMemoryVariables(NameEngine nameEngine) {
        for (Map.Entry<String, Expression> kvp : memoryVariables) {
            String name = kvp.getKey();
            try {
                memoryVariables.define(name, nameEngine.get(name));
            } catch (NameUnassignedException | InvalidNameException ex) {
                throw new ThisCannotHappenException(ex);
            }
        }
    }

    /**
     *
     * @param nameEngine, NameEngine, may be altered.
     * @param pass
     * @return
     * @throws org.harctoolbox.irp.NameUnassignedException
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     */
    public ModulatedIrSequence toModulatedIrSequence(NameEngine nameEngine, Pass pass) throws NameUnassignedException, IrpInvalidArgumentException {
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
    private IrSequence toIrSequence(NameEngine nameEngine, Pass pass) throws NameUnassignedException, IrpInvalidArgumentException {
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

    private double getFrequencyWithDefault() {
        return generalSpec.getFrequencyWitDefault();
    }

    public double getUnit() {
        return generalSpec.getUnit();
    }

    public Double getDutyCycle() {
        return generalSpec.getDutyCycle();
    }

    long getMemoryVariable(String name) throws NameUnassignedException {
        return memoryVariables.get(name).toNumber();
    }

    boolean hasMemoryVariable(String name) {
        return memoryVariables.containsKey(name);
    }

    public boolean isPWM2() {
        return bitspecIrstream.isPWM2();
    }

    public boolean isPWM4() {
        return bitspecIrstream.isPWM4();
    }

    boolean isPWM16() {
        return bitspecIrstream.isPWM16();
    }

    public boolean isBiphase() {
        return bitspecIrstream.isBiphase(generalSpec, initialDefinitions);
    }

    public boolean isTrivial(boolean inverted) {
        return bitspecIrstream.isTrivial(generalSpec, initialDefinitions, inverted);
    }

    public boolean isTrivial() {
        return isTrivial(true) || isTrivial(false);
    }

    public boolean interleavingOk() {
       return interleavingFlashOk() && interleavingGapOk();
    }

    public boolean interleavingFlashOk() {
        if (interleavingFlash == null)
            interleavingFlash = bitspecIrstream.interleavingFlashOk();
        return interleavingFlash;
    }

    public boolean interleavingGapOk() {
        if (interleavingGap == null)
            interleavingGap = bitspecIrstream.interleavingGapOk();
        return interleavingGap;
    }

    /**
     * A protocol is Sonytype if it is PWM2 with different flashes, and has interleaving flashes.
     * @return
     */
    public boolean isSonyType() {
        return bitspecIrstream.isSonyType(generalSpec, initialDefinitions.clone());
    }

    public boolean isRPlus() {
        return bitspecIrstream.isRPlus();
    }

    public boolean startsWithFlash() {
        return bitspecIrstream.startsWithFlash();
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
        XmlUtils.addBooleanAttributeIfTrue(renderer, "startsWithFlash", startsWithFlash());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "hasVariation", hasVariation());
        XmlUtils.addBooleanAttributeIfTrue(renderer, "rplus", isRPlus());
        XmlUtils.addDoubleAttributeAsInteger(renderer, "minDiff", minDurationDiff());
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
    public String toIrpString(int radix) {
        return toIrpString(radix, false);
    }

    public String toIrpString(int radix, boolean usePeriods) {
        return toIrpString(radix, usePeriods, "");
    }

    public String toIrpString(int radix, boolean usePeriods, String separator) {
        return
                generalSpec.toIrpString(usePeriods) + separator
                + bitspecIrstream.toIrpString(radix, separator) + separator
                + definitions.toIrpString(radix, separator) + separator
                + parameterSpecs.toIrpString(radix, separator);
    }

    public String toIrpString(int radix, boolean usePeriods, boolean tsvOptimized) {
        return toIrpString(radix, usePeriods, tsvOptimized ? "\t" : "");
    }

//    @Override
//    public String toString() {
//        return toIrpString();
//    }

    public Map<String, Long> randomParameters() {
        return parameterSpecs.random();
    }

    public Map<String, Long> randomParameters(Random random) {
        return parameterSpecs.random(random);
    }

    public Map<String, Long> recognize(IrSignal irSignal) throws SignalRecognitionException {
        return recognize(irSignal, true, false, true);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean strict, boolean loose, boolean keepDefaulted) throws SignalRecognitionException {
        return recognize(irSignal, strict, loose, keepDefaulted, IrCoreUtils.DEFAULTFREQUENCYTOLERANCE,
                IrCoreUtils.DEFAULTABSOLUTETOLERANCE, IrCoreUtils.DEFAULTRELATIVETOLERANCE, IrCoreUtils.DEFAULT_MINIMUM_LEADOUT);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean strict, boolean loose, boolean keepDefaulted,
            double frequencyTolerance, double absoluteTolerance, double relativeTolerance, double minimumLeadout)
            throws SignalRecognitionException {
        //IrpUtils.entering(logger, Level.FINE, "recognize", this);
        checkFrequency(irSignal.getFrequencyWithDefault(), frequencyTolerance);
        initializeDefinitions();
        ParameterCollector names = new ParameterCollector();

        IrSequence intro = irSignal.getIntroSequence();
        IrSequence repeat = irSignal.getRepeatSequence();
        IrSequence ending = irSignal.getEndingSequence();
        boolean justIntro = repeat.isEmpty() && ending.isEmpty();
        if (!strict && this.isEmpty(Pass.intro) && justIntro) {
            repeat = intro;
            intro = new IrSequence();
        }

        int rest = decode(names, intro, IrSignal.Pass.intro, absoluteTolerance, relativeTolerance, minimumLeadout);
        if (rest != 0) {
            if (!strict && repeat.isEmpty()) {
                try {
                    repeat = intro.subSequence(intro.getLength() - rest, rest);
                } catch (InvalidArgumentException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            } else
                throw new SignalRecognitionException("Intro sequence was not fully matched");
        }

        if (strict || ! repeat.isEmpty() || ! ending.isEmpty() || rest != 0) {
            rest = decode(names, repeat, IrSignal.Pass.repeat, absoluteTolerance, relativeTolerance, minimumLeadout);
            if (rest > 0) {
                if (strict || !justIntro)
                    throw new SignalRecognitionException("Repeat sequence was not fully matched");

                try {
                    ending = repeat.subSequence(repeat.getLength() - rest, rest);
                } catch (InvalidArgumentException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            }
            rest = decode(names, ending, IrSignal.Pass.ending, absoluteTolerance, relativeTolerance, minimumLeadout);
            if (rest > 0) {
                throw new SignalRecognitionException("Ending sequence was not fully matched");
            }
        }
        Map<String, Long> result = names.collectedNames();
        parameterSpecs.reduceNamesMap(result, keepDefaulted);
        //IrpUtils.exiting(logger, Level.FINE, "recognize", result);
        return result;
    }

    private void checkFrequency(Double frequency, double frequencyTolerance) throws SignalRecognitionException {
        boolean success = frequencyTolerance < 0
                || (frequency != null && IrCoreUtils.approximatelyEquals(getFrequencyWithDefault(), frequency, frequencyTolerance, 0.0));
        if (!success)
            throw new SignalRecognitionException("Frequency does not match");
    }

    private int decode(ParameterCollector names, IrSequence irSequence, IrSignal.Pass pass, double absoluteTolerance, double relativeTolerance, double minimumLeadout)
            throws SignalRecognitionException {
        RecognizeData recognizeData = new RecognizeData(generalSpec, definitions, irSequence, interleavingOk(), names,
                absoluteTolerance, relativeTolerance, minimumLeadout);
        Protocol reducedProtocol = normalForm(pass);
        //traverse(recognizeData, pass);
        reducedProtocol.decode(recognizeData);
        try {
            recognizeData.checkConsistency();
            checkDomain(names);
        } catch (DomainViolationException | NameUnassignedException ex) {
            throw new SignalRecognitionException(ex);
        }
        return recognizeData.remaining();
    }

    private void decode(RecognizeData recognizeData) throws SignalRecognitionException {
        bitspecIrstream.decode(recognizeData, new ArrayList<>(0));
        recognizeData.finish();
//        if (!recognizeData.isFinished())
//            throw new SignalRecognitionException("IrSequence not fully matched");
    }

    // Penalize silly protocols
    @Override
    public int weight() {
        int w = generalSpec.weight() + bitspecIrstream.weight()
                + initialDefinitions.weight() + parameterSpecs.weight();
        int penalty = isTrivial() ? SILLYNESSPENALTY : 1;
        return penalty * w;
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

    public String classificationString() {
        StringBuilder str = new StringBuilder(128);
        str.append((int) minDurationDiff());
        str.append("\t").append((int) (getFrequency() != null ? getFrequency() : GeneralSpec.DEFAULT_FREQUENCY));
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
        str.append(startsWithFlash() ? "SWD\t" : "\t");
        str.append(hasVariation() ? "variation\t" : "\t");
        str.append(isRPlus() ? "R+" : "");
        return str.toString();
    }

    /**
     * This is sort-of a version of classificationString, but for another audience.
     * @return
     */
    // TODO: check for equations we cannot solve.
    public String warningsString() {
        return warningFrequency()
                + warningStartsWithFlash()
                + warningTrivialBitspec()
                + warningRepeatPlus()
                + warningsInterleaving()
                + warningNonConstantLengthBitFields()
                + warningNoParameterSpecs()
                ;
    }

    public String warningFrequency() {
        Double frequency = getFrequency();
        return frequency == null ? warn("Frequency is missing, using default frequency = " + GeneralSpec.DEFAULT_FREQUENCY)
                : (! commonFrequency(frequency)) ? warn("Uncommon frequency = " + frequency.longValue())
                : "";
    }

    public String warningStartsWithFlash() {
        return !startsWithFlash() ? warn("Protocol does not start with a Duration/Flash") : "";
    }

    public String warningTrivialBitspec() {
        return isTrivial() ? warn("Protocol uses trivial bitspec") : "";
    }

    public String warningRepeatPlus() {
        return isRPlus() ? warn("Protocol uses infinite repeat with min > 0") : "";
    }

    public String warningsInterleaving() {
        return interleavingOk() ? ""
                : isBiphase() ? warn("Protocol not interleaving; is biphase")
                : isSonyType() ? warn("Protocol not interleaving, but is Sony-like")
                : warn("Protocol not interleaving");
    }

    public String warningNonConstantLengthBitFields() {
        return nonConstantBitFieldLength() ? warn("Protocol contains bitfields with non-constant lengths") : "";
    }

    public String warningNoParameterSpecs() {
        return getParameterSpecs().isEmpty() ? warn("ParameterSpecs missing from the protocol") : "";
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
        map.put("minDiff", minDurationDiff());

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

    public boolean nonConstantBitFieldLength() {
        return bitspecIrstream.nonConstantBitFieldLength();
    }

    public Integer guessParameterLength(String name) {
        return bitspecIrstream.guessParameterLength(name);
    }

    public TreeSet<Double> allDurationsInMicros() {
        return bitspecIrstream.allDurationsInMicros(generalSpec, definitions);
    }

    public double minDurationDiff() {
        return IrCoreUtils.minDiff(allDurationsInMicros());
    }

    public static class ProtocolNotDecodableException extends IrpException {

        ProtocolNotDecodableException(String name) {
            super("Protocol " + name + " not decodable");
        }
    }
}
