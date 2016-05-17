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
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

/**
 *
 */
class EvaluatedIrStream {
    private List<Object> elements;
    //private BitField bitField;
    private BitSpec bitSpec;
    private GeneralSpec generalSpec;
    private IrSignal.Pass pass;
    private NameEngine nameEngine;
    private double elapsed;

    EvaluatedIrStream(NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec, IrSignal.Pass pass) {
        this.nameEngine = nameEngine;
        this.generalSpec = generalSpec;
        this.pass = pass;
        this.bitSpec = bitSpec;
        elements = new ArrayList<>();
        elapsed = 0f;
    }

    IrSequence toIrSequence()
            throws IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, IrpSyntaxException {
        evaluateBitStream();
        //canonicalize();
        List<Double>times = new ArrayList<>();
        for (Object object : elements) {
            if (!(object instanceof Double))
                throw new IrpSemanticException("IrSequence cannot be (completely) evaluated");
            times.add((Double) object);
        }
        return new IrSequence(times);
    }

    void add(EvaluatedIrStream evaluatedIrStream) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        if (evaluatedIrStream.isEmpty())
            return;

        int lastIndex = elements.size() - 1;
        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream) {
            if (evaluatedIrStream.elements.get(0) instanceof BitStream) {
                assert (evaluatedIrStream.elements.size() == 1);
                squeezeBitStreams((BitStream) evaluatedIrStream.elements.get(0));
            } else {
                evaluateBitStream();
                elements.addAll(evaluatedIrStream.elements);
            }
        } else {
            elements.addAll(evaluatedIrStream.elements);
        }
        updateElapsed();
    }

    void add(double duration) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        evaluateBitStream();
        elements.add(duration);
        updateElapsed();
    }

    private void evaluateBitStream() throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        int lastIndex = elements.size() - 1;
        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream) {
            BitStream bitStream = (BitStream) elements.get(lastIndex);
            elements.remove(lastIndex);
            EvaluatedIrStream bitFieldDurations = bitStream.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
            add(bitFieldDurations);
        }
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int getLenght() {
        return elements.size();
    }

    public double get(int i) throws IrpSemanticException {
        Object object = elements.get(i);
        if (!(object instanceof Double))
            throw new IrpSemanticException("Not numeric");
        return (Double) object;
    }

    private void squeezeBitStreams(BitStream bitStream) {
        int lastIndex = elements.size() - 1;
        BitStream old = (BitStream) elements.get(lastIndex);
        //elements.remove(lastIndex);
        old.add(bitStream, nameEngine, generalSpec);
        //elements.add(newBitField);
    }

    private void updateElapsed() {
        elapsed = 0f;
        for (Object object : elements) {
            if (object instanceof Double)
                elapsed += Math.abs((Double)object);
            else {
                elapsed = -1f;
                break;
            }
        }
    }

    double getElapsed() {
        return elapsed;
    }
}
