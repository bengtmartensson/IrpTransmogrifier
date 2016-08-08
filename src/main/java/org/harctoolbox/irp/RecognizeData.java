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

import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

public class RecognizeData implements Cloneable {

    private static int ALL = -1;

    /**
     * @return the ALL
     */
    public static int getALL() {
        return ALL;
    }

    /**
     * @param aALL the ALL to set
     */
    public static void setALL(int aALL) {
        ALL = aALL;
    }

    //private int start;
    //private int length;
    private boolean success;
    private int position;
    private double rest; // microseconds
    private boolean restIsFlash;
    private IrSignal.Pass state;
    //private NameEngine nameEngine;
    private ParameterCollector parameterCollector;
    private final IrSequence irSequence;
    private final GeneralSpec generalSpec;
    private int extentStart;
    private IrStreamItem lookAheadItem;

//    public RecognizeData(NameEngine nameEngine) {
//        this(0, ALL, null, nameEngine);
//    }

    public RecognizeData(GeneralSpec generalSpec, IrSequence irSequence) {
        this(generalSpec, irSequence, 0, IrSignal.Pass.intro, new ParameterCollector());
    }

    public RecognizeData(GeneralSpec generalSpec, IrSequence irSequence, int position/*start, int length*/, IrSignal.Pass state, ParameterCollector parameterCollector) {
        //this.start = start;
        //this.length = length;
        success = true;
        this.generalSpec = generalSpec;
        this.position = position;
        this.rest = 0.0;
        this.restIsFlash = false;
        this.irSequence = irSequence;
        this.state = state;
        this.parameterCollector = parameterCollector;
        this.extentStart = 0;
        this.lookAheadItem = null;
    }

    RecognizeData(GeneralSpec generalSpec, IrSequence irSequence, ParameterCollector nameEngine) {
        this(generalSpec, irSequence, 0, IrSignal.Pass.intro, nameEngine);
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
        result.setParameterCollector(getParameterCollector().clone());
        return result;
    }

//    /**
//     * @return the start
//     */
//    public int getStart() {
//        return start;
//    }
//
//    /**
//     * @return the length
//     */
//    public int getLength() {
//        return length;
//    }
//
//    /**
//     * @return the data
//     */
//    public NameEngine getNameEngine() {
//        return nameEngine;
//    }

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

//    /**
//     * @param start the start to set
//     */
//    public void setStart(int start) {
//        this.start = start;
//    }
//
//    /**
//     * @param length the length to set
//     */
//    public void setLength(int length) {
//        this.length = length;
//    }

    /**
     * @param state the state to set
     */
    public void setState(IrSignal.Pass state) {
        this.state = state;
    }

//    void setNameEngine(NameEngine nameEngine) {
//        this.nameEngine = nameEngine;
//    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the parameterCollector
     */
    public ParameterCollector getParameterCollector() {
        return parameterCollector;
    }

    /**
     * @param parameterCollector the parameterCollector to set
     */
    public void setParameterCollector(ParameterCollector parameterCollector) {
        this.parameterCollector = parameterCollector;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the generalSpec
     */
    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    void incrementPosition(int i) {
        position += i;
    }

    public boolean hasRest() {
        return ! IrCoreUtils.approximatelyEquals(rest, 0.0);
    }

    /**
     * @return the rest
     */
    public double getRest() {
        return rest;
    }

    /**
     * @param rest the rest to set
     * @param isFlash
     */
    public void setRest(double rest, boolean isFlash) {
        this.rest = rest;
        this.restIsFlash = isFlash;
    }

    /**
     * @param rest the rest to set
     */
    public void setRestFlash(double rest) {
        setRest(rest, true);
    }

    /**
     * @param rest the rest to set
     */
    public void setRestGap(double rest) {
        setRest(rest, false);
    }

    /**
     * @return the restIsFlash
     */
    public boolean isRestIsFlash() {
        return restIsFlash;
    }

    public boolean hasRestGap() {
        return hasRest() && ! restIsFlash;
    }

    public boolean hasRestFlash() {
        return hasRest() && restIsFlash;
    }

    public void clearRest() {
        rest = 0.0f;
    }

    /**
     * @return the extentStart
     */
    public int getExtentStart() {
        return extentStart;
    }

    /**
     */
    public void markExtentStart() {
        extentStart = position + 1;
    }

    /**
     * @return the lookAheadItem
     */
    public IrStreamItem getLookAheadItem() {
        return lookAheadItem;
    }

    /**
     * @param lookAheadItem the lookAheadItem to set
     */
    public void setLookAheadItem(IrStreamItem lookAheadItem) {
        this.lookAheadItem = lookAheadItem;
    }

}
