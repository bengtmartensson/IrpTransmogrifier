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
        BareIrStream actual = pass == Pass.intro ? intro
                : pass == Pass.repeat ? repeat : ending;
        return actual != null ? actual.evaluate(state, pass, nameEngine, generalSpec) : null;
//        return pass == Pass.intro ? intro.evaluate(state, pass, nameEngine, generalSpec)
//                : pass == Pass.repeat ? repeat.evaluate(state, pass, nameEngine, generalSpec)
//                : ending != null ? ending.evaluate(state, pass, nameEngine, generalSpec)
//                : new EvaluatedIrStream(nameEngine, generalSpec, pass);
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
    boolean interleavingOk() {
        return intro.interleavingOk() && repeat.interleavingOk() && (ending == null || ending.interleavingOk());
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
    public RecognizeData recognize(RecognizeData recognizeData, Pass pass,
            GeneralSpec generalSpec, ArrayList<BitSpec> bitSpecs)
            throws NameConflictException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
