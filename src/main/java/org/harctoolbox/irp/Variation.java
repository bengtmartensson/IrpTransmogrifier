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

import java.util.List;
import java.util.Objects;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class Variation extends IrStreamItem {
    private static int numberOfInfiniteRepeats(BareIrStream bareIrStream) {
        return bareIrStream == null ? 0 : bareIrStream.numberOfInfiniteRepeats();
    }

    private BareIrStream intro;
    private BareIrStream repeat;
    private BareIrStream ending;
    private final IrpParser.VariationContext parseTree;

    public Variation(String str) throws IrpSyntaxException, InvalidRepeatException {
        this((new ParserDriver(str)).getParser().variation());
    }

    public Variation(IrpParser.VariationContext variation) throws IrpSyntaxException, InvalidRepeatException {
        parseTree = variation;
        intro = new BareIrStream(variation.alternative(0).bare_irstream());
        repeat = new BareIrStream(variation.alternative(1).bare_irstream());
        ending = variation.alternative().size() > 2 ? new BareIrStream(variation.alternative(2).bare_irstream()) : null;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return intro.isEmpty(nameEngine) && repeat.isEmpty(nameEngine) && (ending == null || ending.isEmpty(nameEngine));
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        BareIrStream actual = select(pass);
        return actual != null ? actual.evaluate(state, pass, nameEngine, generalSpec) : null;
//        return pass == Pass.intro ? intro.evaluate(state, pass, nameEngine, generalSpec)
//                : pass == Pass.repeat ? repeat.evaluate(state, pass, nameEngine, generalSpec)
//                : ending != null ? ending.evaluate(state, pass, nameEngine, generalSpec)
//                : new EvaluatedIrStream(nameEngine, generalSpec, pass);
    }

    private BareIrStream select(IrSignal.Pass pass) {
        return pass == Pass.intro ? intro
                : pass == Pass.repeat ? repeat
                : ending;
    }

    @Override
    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass) {
        return pass;
    }

    @Override
    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass) {
        return pass;
    }


    @Override
    public int numberOfInfiniteRepeats() {
        return Math.max(numberOfInfiniteRepeats(intro), Math.max(numberOfInfiniteRepeats(repeat), numberOfInfiniteRepeats(ending)));
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("variation");
        element.appendChild(intro.toElement(document));
        element.appendChild(repeat.toElement(document));
        element.appendChild(ending.toElement(document));
        return element;
    }

    @Override
    int numberOfBareDurations() {
        return -999999999;
    }

    @Override
    int numberOfBits() {
        return -999999999;
    }

    @Override
    public String toIrpString() {
        StringBuilder str = new StringBuilder(50);
        str.append("[").append(intro.toIrpString()).append("]");
        str.append("[").append(repeat.toIrpString()).append("]");
        if (ending != null && !ending.isEmpty(null))
            str.append("[").append(ending.toIrpString()).append("]");
        return str.toString();
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, Pass pass, List<BitSpec> bitSpecs) throws NameConflictException {
        return select(pass).recognize(recognizeData, pass, bitSpecs);
    }

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.interleavingOk(intro, nameEngine, generalSpec, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(intro, nameEngine, generalSpec, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(intro, nameEngine, generalSpec, last, gapFlashBitSpecs);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.endingDurationType(intro, last, gapFlashBitSpecs)
                .combine(BareIrStream.endingDurationType(repeat, last, gapFlashBitSpecs))
                .combine(BareIrStream.endingDurationType(ending, last, gapFlashBitSpecs));
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.startingDurationType(intro, last, gapFlashBitSpecs)
                .combine(BareIrStream.startingDurationType(repeat, last, gapFlashBitSpecs))
                .combine(BareIrStream.startingDurationType(ending, last, gapFlashBitSpecs));
    }

    @Override
    public int weight() {
        return intro.weight() + repeat.weight() + ending.weight();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Variation))
            return false;

        Variation other = (Variation) obj;
        return intro.equals(other.intro)
                && repeat.equals(other.repeat)
                && ending.equals(other.ending);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.intro);
        hash = 61 * hash + Objects.hashCode(this.repeat);
        hash = 61 * hash + Objects.hashCode(this.ending);
        return hash;
    }
}
