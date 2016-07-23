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

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class Variation extends IrStreamItem {

    private BareIrStream intro;
    private BareIrStream repeat;
    private BareIrStream ending;

    public Variation(String str) throws IrpSyntaxException, InvalidRepeatException {
        this((new ParserDriver(str)).getParser().variation());
    }

    public Variation(IrpParser.VariationContext variation) throws IrpSyntaxException, InvalidRepeatException {
        intro = new BareIrStream(variation.alternative(0).bare_irstream());
        repeat = new BareIrStream(variation.alternative(1).bare_irstream());
        ending = variation.alternative().size() > 2 ? new BareIrStream(variation.alternative(2).bare_irstream()) : null;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return intro.isEmpty(nameEngine) && repeat.isEmpty(nameEngine) && (ending == null || ending.isEmpty(nameEngine));
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            BitSpec bitSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return pass == Pass.intro ? intro.evaluate(state, pass, nameEngine, generalSpec, bitSpec, elapsed)
                : pass == Pass.repeat ? repeat.evaluate(state, pass, nameEngine, generalSpec, bitSpec, elapsed)
                : ending != null ? ending.evaluate(state, pass, nameEngine, generalSpec, bitSpec, elapsed)
                : new EvaluatedIrStream(nameEngine, generalSpec, bitSpec, pass);
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
        StringBuilder str = new StringBuilder();
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
}
