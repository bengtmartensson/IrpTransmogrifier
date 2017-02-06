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
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

public class RenderData extends Traverser {

    private final List<EvaluatedIrStream> evaluatedIrStreamList;

    public RenderData(NameEngine nameEngine, GeneralSpec generalSpec) {
        super(IrSignal.Pass.intro, nameEngine, generalSpec);
        evaluatedIrStreamList = new ArrayList<>(2);
        push();
    }

    private EvaluatedIrStream currentEvaluatedIrStream() {
        return evaluatedIrStreamList.get(evaluatedIrStreamList.size() - 1);
    }

    IrSequence toIrSequence() throws UnassignedException, IrpSemanticException {
        return currentEvaluatedIrStream().toIrSequence();
    }

    void add(Duration duration) {
        currentEvaluatedIrStream().add(duration);
    }

    void add(BitStream bitStream) {
        currentEvaluatedIrStream().add(bitStream);
    }

    void add(EvaluatedIrStream evalStream) {
        currentEvaluatedIrStream().add(evalStream);
    }

    public final void push() {
        EvaluatedIrStream evalIrStream = evaluatedIrStreamList.isEmpty()
                ? new EvaluatedIrStream(getNameEngine(), getGeneralSpec(), IrSignal.Pass.intro)
                : new EvaluatedIrStream(currentEvaluatedIrStream());
        evaluatedIrStreamList.add(evalIrStream);
    }

    public void pop() {
        if (evaluatedIrStreamList.size() > 1) {
            EvaluatedIrStream evalIrStream = currentEvaluatedIrStream();
            evaluatedIrStreamList.remove(evaluatedIrStreamList.size() - 1);
            currentEvaluatedIrStream().add(evalIrStream);
        }
    }

    void reduce(BitSpec bitStream) throws UnassignedException, InvalidNameException, IrpSemanticException, NameConflictException, IrpSignalParseException {
        currentEvaluatedIrStream().reduce(bitStream);
    }

    @Override
    public void preprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        item.prerender(this, pass, bitSpecs);
    }

    @Override
    public void postprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        item.render(this, pass, bitSpecs);
    }

    EvaluatedIrStream getEvaluatedIrStream() {
        return currentEvaluatedIrStream();
    }
}
