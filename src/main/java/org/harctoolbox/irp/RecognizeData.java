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

import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

public class RecognizeData implements Cloneable {

    private static final int ALL = -1;

    private int start;
    private int length;
    private IrSignal.Pass state;
    private NameEngine nameEngine;
    private final IrSequence irSequence;

//    public RecognizeData(NameEngine nameEngine) {
//        this(0, ALL, null, nameEngine);
//    }

    public RecognizeData(IrSequence irSequence) {
        this(irSequence, 0, 0, IrSignal.Pass.intro, new NameEngine());
    }

    public RecognizeData(IrSequence irSequence, int start, int length, IrSignal.Pass state, NameEngine nameEngine) {
        this.start = start;
        this.length = length;
        this.state = state;
        this.nameEngine = nameEngine;
        this.irSequence = irSequence;
    }

    RecognizeData(IrSequence irSequence, NameEngine nameEngine) {
        this(irSequence, 0, 0, IrSignal.Pass.intro, nameEngine);
    }

//    public RecognizeData(IrSequence irSequence, int position) {
//        this(irSequence, position, 0, IrSignal.Pass.intro, new NameEngine());
//    }
//
//    public RecognizeData(IrSequence irSequence, NameEngine nameEngine) {
//        this(irSequence, 0, 0, IrSignal.Pass.intro, nameEngine);
//    }

    /**
     * Returns a shallow copy, except for the NameEngine, which is copied with NameEngine.clone().
     * @return
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public RecognizeData clone() {
        RecognizeData result;
        try {
            result = (RecognizeData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.nameEngine = nameEngine.clone();
        return result;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the data
     */
    public NameEngine getNameEngine() {
        return nameEngine;
    }

    /**
     * @return the state
     */
    public IrSignal.Pass getState() {
        return state;
    }

    /**
     * @return the irSequence
     */
    public IrSequence getIrSequence() {
        return irSequence;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @param state the state to set
     */
    public void setState(IrSignal.Pass state) {
        this.state = state;
    }

    void setNameEngine(NameEngine nameEngine) {
        this.nameEngine = nameEngine;
    }
}
