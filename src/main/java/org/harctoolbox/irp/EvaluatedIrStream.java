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
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 *
 */
final class EvaluatedIrStream {

    private final static Logger logger = Logger.getLogger(EvaluatedIrStream.class.getName());

    /**
     * Factory method that generates an IrSequence also for non-interleaved, signed data,
     * possibly starting with gaps.
     * @param durations
     * @param fixOdd It true, appends {@link DUMMYGAPDURATION} to sequences ending with a flash.
     * @return
     * @throws OddSequenceLengthException
     */
    private static IrSequence mkIrSequence(List<Double> durations) {
        List<Double> interleaved = IrSequence.toInterleavingList(durations);
        try {
            //checkFixOddLength(interleaved, fixOdd);
            return new IrSequence(interleaved);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

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

    EvaluatedIrStream(EvaluatedIrStream evaluatedIrStream) {
        this(evaluatedIrStream.nameEngine, evaluatedIrStream.generalSpec, evaluatedIrStream.pass);
        state = evaluatedIrStream.state;
    }


    IrSequence toIrSequence() throws NameUnassignedException, IrpInvalidArgumentException {
        List<Double>times = new ArrayList<>(elements.size()*10);
        double elapsed = 0.0;
        for (Evaluatable element : elements) {
            if (!(element instanceof Duration))
                throw new ThisCannotHappenException("EvaluatedIrSequence cannot be (completely) evaluated");
            Duration duration = (Duration) element;
            double time = duration.evaluateWithSign(generalSpec, nameEngine, elapsed);
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
        IrSequence result = mkIrSequence(times);
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        elements.stream().forEach((element) -> {
            joiner.add(element.toString());
        });
        return joiner.toString() + " " + nameEngine.toString();
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

    void add(BitStream bitStream) {
        int lastIndex = elements.size() - 1;
        if (lastIndex >= 0 && elements.get(lastIndex) instanceof BitStream)
            squeezeBitStreams(bitStream);
        else
            elements.add(bitStream);
    }

    public void reduce(BitSpec bitSpec) throws NameUnassignedException {
        int index = 0;
        while (index < elements.size()) {
            if (elements.get(index) instanceof BitStream)
                index += reduce(bitSpec, index);
            else
                index++;
        }
    }

    private int reduce(BitSpec bitSpec, int index) throws NameUnassignedException {
        BitStream bitStream = (BitStream) elements.get(index);
        elements.remove(index);
        EvaluatedIrStream bitFieldDurations = bitStream.evaluate(pass, pass, generalSpec, nameEngine, bitSpec);
        int length = bitFieldDurations.elements.size();
        elements.addAll(index, bitFieldDurations.elements);
        return length;
    }

    void add(Duration evaluatable) {
        elements.add(evaluatable);
    }

    private void removeHead() {
        elements.remove(0);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int getLength() {
        return elements.size();
    }

    public double get(int i) throws IrpInvalidArgumentException, NameUnassignedException {
        Evaluatable object = elements.get(i);
        if (!(object instanceof Duration))
            throw new ThisCannotHappenException("Not numeric");
        return ((Duration) object).evaluateWithSign(generalSpec, nameEngine, 0);
    }

    private void squeezeBitStreams(BitStream bitStream) {
        int lastIndex = elements.size() - 1;
        BitStream old = (BitStream) elements.get(lastIndex);
        old.add(bitStream, generalSpec, nameEngine);
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

    boolean isFlash(int i) {
        return elements.get(i) instanceof Flash;
    }

    boolean isGap(int i) {
        return elements.get(i) instanceof Gap;
    }
}
