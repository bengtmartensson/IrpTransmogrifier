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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class BitspecIrstream extends IrpObject implements IrStreamItem {
    private static final Logger logger = Logger.getLogger(BitspecIrstream.class.getName());

    private BitSpec bitSpec;
    private IrStream irStream;

    public BitspecIrstream(IrpParser.ProtocolContext ctx) {
        this(ctx.bitspec_irstream());
    }

    public BitspecIrstream(BitSpec bitSpec, IrStream irStream, IrpParser.Bitspec_irstreamContext ctx) {
        super(ctx);
        this.bitSpec = bitSpec;
        this.irStream = irStream;
    }

    public BitspecIrstream(BitSpec bitSpec, IrStream irStream) {
        this(bitSpec, irStream, null);
    }

    public BitspecIrstream(IrpParser.Bitspec_irstreamContext ctx) {
        this(new BitSpec(ctx.bitspec()), new IrStream(ctx.irstream()), ctx);
    }

    public BitspecIrstream(String str) {
        this(new ParserDriver(str));
    }

    private BitspecIrstream(ParserDriver parserDriver) {
        this(parserDriver.getParser().bitspec_irstream());
    }

    public BitspecIrstream() {
        this(new BitSpec(), new IrStream());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BitspecIrstream))
            return false;

        BitspecIrstream other = (BitspecIrstream) obj;
        return bitSpec.equals(other.bitSpec)
                && irStream.equals(other.irStream);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.bitSpec);
        hash = 97 * hash + Objects.hashCode(this.irStream);
        return hash;
    }

    @Override
    public String toIrpString(int radix) {
        return toIrpString(radix, "");
    }

    public String toIrpString(int radix, String separator) {
        return bitSpec.toIrpString(radix) + separator + irStream.toIrpString(radix, separator);
    }

    @Override
    public Element toElement(Document document) {
        Element root = super.toElement(document);
        root.setAttribute("interleavingOk", Boolean.toString(interleavingOk(null, null)));
        if (numberOfDurations() != null)
            root.setAttribute("numberOfDuration", Integer.toString(numberOfDurations()));
        root.appendChild(bitSpec.toElement(document));
        root.appendChild(irStream.toElement(document));
        /*
            Element intro = document.createElement("Intro");
            root.appendChild(intro);
            intro.appendChild(irStream.toElement(document, Pass.intro));
            Element repeat = document.createElement("Repeat");
            root.appendChild(repeat);
            repeat.appendChild(irStream.toElement(document, Pass.repeat));
            Element ending = document.createElement("Ending");
            root.appendChild(ending);
            ending.appendChild(irStream.toElement(document, Pass.ending));
            */
        return root;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Integer numberOfBitSpecs() {
        return irStream.numberOfBitSpecs() + 1;
    }

    @Override
    public Integer numberOfBits() {
        return irStream.numberOfBits();
    }

    @Override
    public Integer numberOfBareDurations() {
        return irStream.numberOfBareDurations();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitSpec.numberOfInfiniteRepeats() + irStream.numberOfInfiniteRepeats();
    }

//    @Override
//    public ParserRuleContext getParseTree() {
//        return parseTree;
//    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> inheritedBitSpecs, boolean isLast) throws SignalRecognitionException {
        logger.log(recognizeData.logRecordEnterWithIrStream(this));
        List<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        int currentLevel = recognizeData.getLevel();
        recognizeData.setLevel(currentLevel + 1);
        irStream.decode(recognizeData, stack, isLast);
        recognizeData.setLevel(currentLevel);
        logger.log(recognizeData.logRecordExit(this));
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> inheritedBitSpecs) throws NameUnassignedException {
        List<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        renderData.push();
        irStream.render(renderData, stack);
        logger.log(Level.FINE, "renderdata (unreduced): {0}", renderData.getEvaluatedIrStream().toString());
        renderData.reduce(bitSpec);
        logger.log(Level.FINE, "renderdata (reduced): {0}", renderData.getEvaluatedIrStream().toString());
        renderData.pop();
    }

    @Override
    public void updateStateWhenEntering(IrSignal.Pass pass, IrStream.PassExtractorState state) {
        irStream.updateStateWhenEntering(pass, state);
    }

    @Override
    public void updateStateWhenExiting(IrSignal.Pass pass, IrStream.PassExtractorState state) {
        irStream.updateStateWhenExiting(pass, state);
    }

    @Override
    public BareIrStream extractPass(Pass pass, IrStream.PassExtractorState state) {
        BareIrStream extractList = irStream.extractPass(pass, state);
        IrStream reducedIrStream = new IrStream(extractList);
        List<IrStreamItem> result = new ArrayList<>(1);
        result.add(new BitspecIrstream(bitSpec, reducedIrStream));
        return new BareIrStream(result);
    }

    public BareIrStream extractPass(IrSignal.Pass pass) {
        return irStream.extractPass(pass);
    }

    @Override
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecsUnused) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(DurationType.gap, true);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(DurationType.flash, false);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(last, gapFlashBitspecs);
    }

    public boolean interleavingOk(DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(last, gapFlashBitspecs);
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(DurationType toCheck, DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, last, gapFlashBitspecs);
    }

    public boolean interleavingOk() {
        return interleavingOk(DurationType.gap);
    }

//    public boolean interleavingOk(DurationType toCheck) {
//        return interleavingOk(toCheck, DurationType.gap);
//    }

    public boolean interleavingFlashOk() {
        return interleavingOk(DurationType.flash, DurationType.gap);
    }

    public boolean interleavingGapOk() {
        return interleavingOk(DurationType.gap, DurationType.gap);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.endingDurationType(last, gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    boolean isPWM2() {
        return bitSpec.isPWM(2);
    }

    boolean isPWM4() {
        return bitSpec.isPWM(4);
    }

    boolean isPWM16() {
        return bitSpec.isPWM(16);
    }

    boolean isBiphase(GeneralSpec generalSpec, NameEngine nameEngine) {
        return bitSpec.isStandardBiPhase(generalSpec, nameEngine);
    }

    boolean isTrivial(GeneralSpec generalSpec, NameEngine definitions, boolean inverted) {
        return bitSpec.isTrivial(generalSpec, definitions, inverted);
    }

    boolean isRPlus() {
        return irStream.isRPlus();
    }

    boolean isSonyType(GeneralSpec generalSpec, NameEngine nameEngine) {
        return interleavingFlashOk() && bitSpec.isSonyType(generalSpec, nameEngine);
    }

    public DurationType startingDurationType(boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(DurationType.gap, gapFlashBitSpecs);
    }

    boolean hasVariation(Pass pass) {
        return irStream.hasVariation(pass);
    }

    boolean startsWithFlash() {
        return irStream.startsWithFlash();
    }

    @Override
    public int weight() {
        return bitSpec.weight() + irStream.weight();
    }

    @Override
    public boolean hasExtent() {
        return bitSpec.hasExtent() || irStream.hasExtent();
    }

    public BitSpec getBitSpec() {
        return bitSpec;
    }

    public IrStream getIrStream() {
        return irStream;
    }

    @Override
    public Set<String> assignmentVariables() {
        Set<String> list = bitSpec.assignmentVariables();
        list.addAll(irStream.assignmentVariables());
        return list;
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        throw new UnsupportedOperationException("Hierarchical BitSpecs not implemented yet.");
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return null;
    }

    @Override
    public Integer numberOfDurations() {
        Integer bsnod = bitSpec.numberOfDurations();
        return bsnod != null ? irStream.numberOfDurations(bsnod) : null;
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nonConstantBitFieldLength() {
        return irStream.nonConstantBitFieldLength();
    }

    @Override
    public Integer guessParameterLength(String name) {
        return irStream.guessParameterLength(name);
    }

    @Override
    public BitspecIrstream substituteConstantVariables(Map<String, Long> constantVariables) {
        return new BitspecIrstream(bitSpec.substituteConstantVariables(constantVariables), irStream.substituteConstantVariables(constantVariables));
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        TreeSet<Double> result = new TreeSet<>();
        Set<Double> bitSpecDurations = bitSpec.allDurationsInMicros(generalSpec, nameEngine);
        result.addAll(bitSpecDurations);
        Set<Double> irStreamDurations = irStream.allDurationsInMicros(generalSpec, nameEngine);
        result.addAll(irStreamDurations);
        return result;
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return bitSpec.constant(nameEngine) && irStream.constant(nameEngine);
    }

    @Override
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
        bitSpec.createParameterSpecs(parameterSpecs);
        irStream.createParameterSpecs(parameterSpecs);
    }
}