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

public class BitspecIrstream extends IrStreamItem {
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
        return toElement(document, false);
    }

    public Element toElement(Document document, boolean split) {
        Element root = super.toElement(document);
        root.setAttribute("interleavingOk", Boolean.toString(interleavingOk(null, null)));
        root.appendChild(bitSpec.toElement(document));
        if (split) {
            Element intro = document.createElement("Intro");
            root.appendChild(intro);
            intro.appendChild(irStream.toElement(document, Pass.intro));
            Element repeat = document.createElement("Repeat");
            root.appendChild(repeat);
            repeat.appendChild(irStream.toElement(document, Pass.repeat));
            Element ending = document.createElement("Ending");
            root.appendChild(ending);
            ending.appendChild(irStream.toElement(document, Pass.ending));
        } else
            root.appendChild(irStream.toElement(document));
        return root;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    int numberOfBitSpecs() {
        return irStream.numberOfBitSpecs() + 1;
    }

    @Override
    int numberOfBits() throws UnassignedException {
        return irStream.numberOfBits();
    }

    @Override
    int numberOfBareDurations(boolean recursive) {
        return irStream.numberOfBareDurations(recursive);
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitSpec.numberOfInfiniteRepeats() + irStream.numberOfInfiniteRepeats();
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> inheritedBitSpecs) {
        IrpUtils.entering(logger, "recognize " + pass, this);
        ArrayList<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        irStream.recognize(recognizeData, pass, stack);
        IrpUtils.exiting(logger, "recognize " + pass);
    }

    @Override
    public void prerender(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) {
        renderData.push();
    }

    @Override
    public void render(RenderData renderData, Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException, IrpSemanticException, NameConflictException, IrpSignalParseException {
        BitSpec lastBitSpec = bitSpecs.get(bitSpecs.size() - 1);
        renderData.reduce(lastBitSpec);
        renderData.pop();
    }

    @Override
    public void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> inheritedBitSpecs) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        IrpUtils.entering(logger, "traverse " + pass, this);
        ArrayList<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        recognizeData.preprocess(this, pass, inheritedBitSpecs);
        /*boolean status =*/

        irStream.traverse(recognizeData, pass, stack);
//        if (status)
//            try {
        /*status =*/
        recognizeData.postprocess(this, pass, stack);
//            } catch (NameConflictException | IrpSemanticException | InvalidNameException | UnassignedException ex) {
//                status = false;
//                Logger.getLogger(BitspecIrstream.class.getName()).log(Level.SEVERE, null, ex);
//            }
        //IrpUtils.exiting(logger, "traverse " + pass, status ? "pass" : "fail"/*? recognizeData.toString() : ""*/);
        IrpUtils.exiting(logger, "traverse " + pass);
        //return status;
    }

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecsUnused) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.gap, true);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.flash, false);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last) {
        boolean flashGapBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.gap, false);
        boolean gapFlashBitspecs = bitSpec.interleaveOk(nameEngine, generalSpec, DurationType.flash, true);

        return (flashGapBitspecs || gapFlashBitspecs)
                && irStream.interleavingOk(toCheck, nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec) {
        return interleavingOk(nameEngine, generalSpec, DurationType.gap);
    }

    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec) {
        return interleavingOk(toCheck, nameEngine, generalSpec, DurationType.gap);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.endingDurationType(last, gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    boolean isPWM2(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isPWM(2, nameEngine, generalSpec);
    }

    boolean isPWM4(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isPWM(4, nameEngine, generalSpec);
    }

    boolean isPWM16(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isPWM(16, nameEngine, generalSpec);
    }

    boolean isBiphase(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isStandardBiPhase(nameEngine, generalSpec);
    }

    boolean isTrivial(NameEngine definitions, GeneralSpec generalSpec, boolean inverted) {
        return bitSpec.isTrivial(definitions, generalSpec, inverted);
    }

    boolean isRPlus() {
        return irStream.isRPlus();
    }

    boolean isSonyType(NameEngine nameEngine, GeneralSpec generalSpec) {
        return interleavingOk(DurationType.flash, nameEngine, generalSpec)
                && bitSpec.isSonyType(nameEngine, generalSpec);
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
}