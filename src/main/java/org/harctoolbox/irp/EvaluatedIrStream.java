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
import java.util.StringJoiner;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 *
 */
class EvaluatedIrStream {

    private final static Logger logger = Logger.getLogger(EvaluatedIrStream.class.getName());

    private final List<Evaluatable> elements;
    private final GeneralSpec generalSpec;
    private final IrSignal.Pass pass;
    private final NameEngine nameEngine;
    private IrSignal.Pass state;

    EvaluatedIrStream(NameEngine nameEngine, GeneralSpec generalSpec, IrSignal.Pass pass) {
        this.nameEngine = nameEngine;
        this.generalSpec = generalSpec;
        this.pass = pass;
        elements = new ArrayList<>(10);
        state = null;
    }

    IrSequence toIrSequence() throws UnassignedException, IrpSemanticException, OddSequenceLenghtException {
        IrpUtils.entering(logger, "toIrSequence", this);
        List<Double>times = new ArrayList<>(elements.size()*10);
        double elapsed = 0.0;
        for (Evaluatable element : elements) {
            if (!(element instanceof Duration))
                throw new IrpSemanticException("IrSequence cannot be (completely) evaluated");
            Duration duration = (Duration) element;
            double time = duration.evaluateWithSign(nameEngine, generalSpec, elapsed);
            if (Math.abs(time) < 0.0001)
                logger.warning("Zero duration ignored");
            else {
            times.add(time);
            if (duration instanceof Extent)
                elapsed = 0.0;
            else
                elapsed += Math.abs(time);
            }
        }
        IrSequence result = new IrSequence(times);
        IrpUtils.exiting(logger, "toIrSequence", result);
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        elements.stream().forEach((element) -> {
            joiner.add(element.toString());
        });
        return joiner.toString();
    }

    void add(EvaluatedIrStream evaluatedIrStream) {
        if (evaluatedIrStream == null || evaluatedIrStream.isEmpty())
            return;

        int lastIndex = elements.size() - 1;
        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream && evaluatedIrStream.elements.get(0) instanceof BitStream) {
            squeezeBitStreams((BitStream) evaluatedIrStream.elements.get(0));
            evaluatedIrStream.removeHead();
            add(evaluatedIrStream);
        } else {
            elements.addAll(evaluatedIrStream.elements);
        }
    }

    public void reduce(BitSpec bitSpec) throws UnassignedException, InvalidNameException {
        int index = 0;
        while (index < elements.size()) {
            if (elements.get(index) instanceof BitStream)
                index += reduce(bitSpec, index);
            else
                index++;
        }
    }

    private int reduce(BitSpec bitSpec, int index) throws UnassignedException, InvalidNameException {
        BitStream bitStream = (BitStream) elements.get(index);
        elements.remove(index);
        EvaluatedIrStream bitFieldDurations = bitStream.evaluate(pass, pass, nameEngine, generalSpec, bitSpec);
        int length = bitFieldDurations.elements.size();
        elements.addAll(index, bitFieldDurations.elements);
        return length;
    }

    void add(Evaluatable evaluatable) {
        elements.add(evaluatable);
    }

    private void removeHead() {
        elements.remove(0);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int getLenght() {
        return elements.size();
    }

    public double get(int i) throws UnassignedException, IrpSemanticException {
        Evaluatable object = elements.get(i);
        if (!(object instanceof Duration))
            throw new ThisCannotHappenException("Not numeric");
        return ((Duration) object).evaluateWithSign(nameEngine, generalSpec, 0);
    }

    private void squeezeBitStreams(BitStream bitStream) {
        IrpUtils.entering(logger, "squeezeBitStreams", this.toString() + "+" + bitStream);
        int lastIndex = elements.size() - 1;
        BitStream old = (BitStream) elements.get(lastIndex);
        old.add(bitStream, nameEngine, generalSpec);
        IrpUtils.exiting(logger, "squeezeBitStreams", this);
    }

    /**
     * @return the state
     */
    public IrSignal.Pass getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(IrSignal.Pass state) {
        this.state = state;
    }
}
