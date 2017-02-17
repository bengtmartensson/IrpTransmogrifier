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
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BitspecIrstream extends IrpObject implements IrStreamItem {
    private static final Logger logger = Logger.getLogger(BitspecIrstream.class.getName());

    private BitSpec bitSpec;
    private IrStream irStream;
    private final IrpParser.Bitspec_irstreamContext parseTree;

    public BitspecIrstream(IrpParser.ProtocolContext ctx) {
        this(ctx.bitspec_irstream());
    }

    public BitspecIrstream(BitSpec bitSpec, IrStream irStream, IrpParser.Bitspec_irstreamContext ctx) {
        parseTree = ctx;
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
        this((new ParserDriver(str)).getParser().bitspec_irstream());
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
    public String toIrpString() {
        return bitSpec.toIrpString() + irStream.toIrpString();
    }

    @Override
    public String toString() {
        return toIrpString();
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
    public Integer numberOfBareDurations(boolean recursive) {
        return irStream.numberOfBareDurations(recursive);
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitSpec.numberOfInfiniteRepeats() + irStream.numberOfInfiniteRepeats();
    }

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> inheritedBitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        ArrayList<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        IrSignal.Pass pass = null;
        irStream.decode(recognizeData, stack);
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> inheritedBitSpecs) throws InvalidNameException, UnassignedException, IrpSemanticException, NameConflictException, IrpSignalParseException {
        ArrayList<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        renderData.push();
        irStream.render(renderData, stack);
        renderData.reduce(bitSpec);
        renderData.pop();
    }

    @Override
    public List<IrStreamItem> extractPass(Pass pass, Pass state) {
        List<IrStreamItem> extractList = irStream.extractPass(pass, state);
        IrStream reducedIrStream = new IrStream(extractList);
        List<IrStreamItem> result = new ArrayList<>(1);
        result.add(new BitspecIrstream(bitSpec, reducedIrStream));
        return result;
    }

    public BareIrStream extractPass(IrSignal.Pass pass) {
        return new BareIrStream(irStream.extractPass(pass, IrSignal.Pass.intro));
    }

    @Override
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecsUnused) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.gap, true);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.flash, false);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(generalSpec, nameEngine, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine) {
        return interleavingOk(generalSpec, nameEngine, DurationType.gap);
    }

    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine) {
        return interleavingOk(toCheck, generalSpec, nameEngine, DurationType.gap);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.endingDurationType(last, gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    boolean isPWM2(GeneralSpec generalSpec, NameEngine nameEngine) {
        return bitSpec.isPWM(2, generalSpec, nameEngine);
    }

    boolean isPWM4(GeneralSpec generalSpec, NameEngine nameEngine) {
        return bitSpec.isPWM(4, generalSpec, nameEngine);
    }

    boolean isPWM16(GeneralSpec generalSpec, NameEngine nameEngine) {
        return bitSpec.isPWM(16, generalSpec, nameEngine);
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
        return interleavingOk(DurationType.flash, generalSpec, nameEngine)
                && bitSpec.isSonyType(generalSpec, nameEngine);
    }

    public DurationType startingDurationType(boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(DurationType.gap, gapFlashBitSpecs);
    }

    boolean hasVariation(boolean recursive) {
        return irStream.hasVariation(recursive);
    }

    boolean startsWithDuration() {
        return irStream.startsWithDuration();
    }

    @Override
    public int weight() {
        return bitSpec.weight() + irStream.weight();
    }

    boolean hasVariationWithIntroEqualsRepeat() {
        return irStream.hasVariationWithIntroEqualsRepeat();
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
    public Map<String, Object> propertiesMap(Pass state, Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        throw new UnsupportedOperationException("Hierarchical BitSpecs not implemented yet.");
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return null;
    }

    @Override
    public Integer numberOfDurations() {
        return irStream.numberOfDurations(bitSpec.numberOfDurations());
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}