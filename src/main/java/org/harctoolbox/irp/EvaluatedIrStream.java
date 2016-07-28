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
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

/**
 *
 */
class EvaluatedIrStream {

    private final static Logger logger = Logger.getLogger(EvaluatedIrStream.class.getName());

    private final List<Evaluatable> elements;
    //private BitField bitField;
    private final BitSpec bitSpec;
    private final GeneralSpec generalSpec;
    private final IrSignal.Pass pass;
    private final NameEngine nameEngine;
    private double elapsed;
    private IrSignal.Pass state;

    EvaluatedIrStream(NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec, IrSignal.Pass pass) {
        this.nameEngine = nameEngine;
        this.generalSpec = generalSpec;
        this.pass = pass;
        this.bitSpec = bitSpec;
        elements = new ArrayList<>();
        elapsed = 0f;
        state = null;
    }

    IrSequence toIrSequence()
            throws IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, IrpSyntaxException {
        //evaluateBitStream();
        //canonicalize();
        IrpUtils.entering(logger, "toIrSequence", this);
        List<Double>times = new ArrayList<>();
        for (Evaluatable element : elements) {
            if (!(element instanceof Duration))
                throw new IrpSemanticException("IrSequence cannot be (completely) evaluated");
            times.add(((Duration) element).evaluateWithSign(nameEngine, generalSpec, 0));
        }
        IrSequence result = new IrSequence(times);
        IrpUtils.exiting(logger, "tiIrSequence", result);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        elements.stream().forEach((element) -> {
            if (str.length() > 0)
                str.append(";");
            str.append(element.toString());
        });
        return "(" + str.append(")").toString();
    }

    void add(EvaluatedIrStream evaluatedIrStream) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        if (evaluatedIrStream == null || evaluatedIrStream.isEmpty())
            return;

        int lastIndex = elements.size() - 1;
        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream && evaluatedIrStream.elements.get(0) instanceof BitStream) {
            //assert (evaluatedIrStream.elements.size() == 1);
            squeezeBitStreams((BitStream) evaluatedIrStream.elements.get(0));
            if (bitSpec != null)
                reduce(lastIndex);
            evaluatedIrStream.removeHead();
            add(evaluatedIrStream);
        } else {
            //evaluateBitStream();
            elements.addAll(evaluatedIrStream.elements);
            if (bitSpec != null)
                reduce();
        }
        updateElapsed();
        updateState();
        //}
        //} else {
        //    elements.addAll(evaluatedIrStream.elements);
        //}
        //updateElapsed();
        //updateState();
    }

    private void reduce() throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        int index = 0;
        while (index < elements.size()) {
            if (elements.get(index) instanceof BitStream) {
                index += reduce(index);
//            add(bitFieldDurations);
            } else {
                index++;
            }
        }
    }

    private int reduce(int index) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        BitStream bitStream = (BitStream) elements.get(index);
        elements.remove(index);
        EvaluatedIrStream bitFieldDurations = bitStream.evaluate(pass, pass, nameEngine, generalSpec, bitSpec, elapsed);
        int length = bitFieldDurations.elements.size();
        elements.addAll(index, bitFieldDurations.elements);
        return length;
    }

    void add(Evaluatable evaluatable) throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException  {
        //evaluateBitStream();
        elements.add(evaluatable);
        updateElapsed();
        updateState();
    }

    private void updateState() {
        for (Evaluatable element : elements)
            if (element.stateWhenExiting() != null)
                state = element.stateWhenExiting();
    }

//    private void evaluateBitStream() throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
//        int lastIndex = elements.size() - 1;
//        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream) {
//            BitStream bitStream = (BitStream) elements.get(lastIndex);
//            elements.remove(lastIndex);
//            EvaluatedIrStream bitFieldDurations = bitStream.evaluate(pass, pass, nameEngine, generalSpec, bitSpec, elapsed);
//            add(bitFieldDurations);
//        }
//    }

    private void removeHead() {
        elements.remove(0);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int getLenght() {
        return elements.size();
    }

    public double get(int i) throws IrpSemanticException, IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        Evaluatable object = elements.get(i);
        if (!(object instanceof Duration))
            throw new IrpSemanticException("Not numeric");
        return ((Duration) object).evaluateWithSign(nameEngine, generalSpec, 0);
    }

    private void squeezeBitStreams(BitStream bitStream) {
        IrpUtils.entering(logger, "squeezeBitStreams", this.toString() + "+" + bitStream);
        int lastIndex = elements.size() - 1;
        BitStream old = (BitStream) elements.get(lastIndex);
        //elements.remove(lastIndex);
        old.add(bitStream, nameEngine, generalSpec);
        //elements.add(newBitField);
        IrpUtils.exiting(logger, "squeezeBitStreams", this);
    }

    private void updateElapsed() throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        elapsed = 0f;
        for (Evaluatable element : elements) {
            if (element instanceof Floatable)
                elapsed += ((Floatable) element).toFloat(nameEngine, generalSpec);
            else {
                elapsed = -1f;
                break;
            }
        }
    }

    double getElapsed() {
        return elapsed;
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
