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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class BitspecIrstream extends IrStreamItem {
    private static final Logger logger = Logger.getLogger(BitspecIrstream.class.getName());

    private BitSpec bitSpec;
    private IrStream irStream;
    private final IrpParser.Bitspec_irstreamContext parseTree;

    public BitspecIrstream(IrpParser.ProtocolContext ctx) throws IrpSyntaxException, InvalidRepeatException {
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

    public BitspecIrstream(IrpParser.Bitspec_irstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(new BitSpec(ctx.bitspec()), new IrStream(ctx.irstream()), ctx);
    }

    public BitspecIrstream(String str) throws IrpSyntaxException, InvalidRepeatException {
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
    public Element toElement(Document document) throws IrpSyntaxException {
        Element root = document.createElement("bitspec_irstream");
        root.setAttribute("interleavingOk", Boolean.toString(interleavingOk(null, null)));
        root.appendChild(bitSpec.toElement(document));
        root.appendChild(irStream.toElement(document));
        return root;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate(4args)", this);
        EvaluatedIrStream evaluatedIrStream = irStream.evaluate(state, pass, nameEngine, generalSpec);
        evaluatedIrStream.reduce(bitSpec);
        IrpUtils.exiting(logger, "evaluate(4args)", evaluatedIrStream);
        return evaluatedIrStream;
    }

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            BitSpec bitSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate(5args)", this);
        EvaluatedIrStream inner = evaluate(state, pass, nameEngine, generalSpec);
        inner.reduce(bitSpec);
        logger.log(Level.FINEST, "inner = {0}", inner);
        EvaluatedIrStream outer = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        outer.add(inner);
        logger.log(Level.FINEST, "outer = {0}", outer);
        logger.exiting(BitspecIrstream.class.getName(), "evaluate(5args)");
        return outer;
    }

    @Override
    int numberOfBitSpecs() {
        return irStream.numberOfBitSpecs() + 1;
    }

    @Override
    int numberOfBits() {
        return irStream.numberOfBits();
    }

    @Override
    int numberOfBareDurations() {
        return irStream.numberOfBareDurations();
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
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> inheritedBitSpecs)
            throws NameConflictException {
        IrpUtils.entering(logger, "recognize " + pass, this);
        ArrayList<BitSpec> stack = new ArrayList<>(inheritedBitSpecs);
        stack.add(bitSpec);
        boolean status = irStream.recognize(recognizeData, pass, stack);
        IrpUtils.exiting(logger, "recognize " + pass, status ? "pass" : "fail"/*? recognizeData.toString() : ""*/);
        return status;
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

    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec) {
        return interleavingOk(nameEngine, generalSpec, DurationType.gap);
    }

//    private boolean interleavingOk() {
//        return interleavingOk(null, null, false);
//    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.endingDurationType(last, gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return irStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    boolean isStandardPWM(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isStandardPWM(nameEngine, generalSpec);
    }

    boolean isPWM4(NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitSpec.isPWM4(nameEngine, generalSpec);
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
}
