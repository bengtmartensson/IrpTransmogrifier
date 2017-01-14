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

import java.util.Map;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;

public class RecognizeData implements Cloneable {

    private boolean success;
    private int position;
    private double hasConsumed;
    private IrSignal.Pass state;
    private ParameterCollector parameterCollector;
    private final IrSequence irSequence;
    private final GeneralSpec generalSpec;
    private int extentStart;
    private boolean interleaving;
    private final NameEngine definitions;
    private ParameterCollector needsChecking;
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private BitwiseParameter danglingBitFieldData;

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, IrSequence irSequence, boolean interleaving, Map<String, Long> nameMap,
            double absoulteTolerance, double relativeTolerance) {
        this(generalSpec, definitions, irSequence, 0, IrSignal.Pass.intro, new ParameterCollector(nameMap), interleaving, nameMap, absoulteTolerance, relativeTolerance);
    }

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, IrSequence irSequence, int position/*start, int length*/, IrSignal.Pass state,
            ParameterCollector parameterCollector, boolean interleaving, Map<String, Long> nameMap, double absoluteTolerance, double relativeTolerance) {
        success = true;
        danglingBitFieldData = new BitwiseParameter();
        this.generalSpec = generalSpec;
        this.definitions = definitions;
        this.position = position;
        this.hasConsumed = 0.0;
        //this.restIsFlash = false;
        this.irSequence = irSequence;
        this.state = state;
        this.parameterCollector = parameterCollector;
        this.extentStart = 0;
        ////this.lookAheadItem = null;
        this.interleaving = interleaving;
        //this.nameMap = nameMap;
        this.needsChecking = new ParameterCollector();
        this.absoluteTolerance = absoluteTolerance;
        this.relativeTolerance = relativeTolerance;
    }

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
     * @param state the state to set
     */
    public void setState(IrSignal.Pass state) {
        this.state = state;
    }

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

    void add(String name, BitwiseParameter parameter) throws NameConflictException, InvalidNameException {
        //Expression expression = definitions.containsKey(name) ? definitions.get(name) : null;
        if (definitions.containsKey(name)) {
            Expression expression;
            try {
                expression = definitions.get(name);
            } catch (UnassignedException ex) {
                throw new ThisCannotHappenException();
            }
            try {
                long expected = expression.toNumber(parameterCollector.toNameEngine());
                if (!parameter.isConsistent(expected))
                    throw new NameConflictException(name, parameter.getValue(), expected);
            } catch (UnassignedException ex) {
                // It has an expression, but is not presently checkable.
                // mark for later checking.
                needsChecking.add(name, parameter);
            }
        } else
            parameterCollector.add(name, parameter);
    }

    void add(String name, long value, long bitmask) throws NameConflictException, InvalidNameException {
        add(name, new BitwiseParameter(value, bitmask));
    }

    void add(String name, long value) throws NameConflictException, InvalidNameException {
        add(name, new BitwiseParameter(value));
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

    public boolean isOn() {
        return Duration.isOn(position);
    }

    public double get() {
        return  Math.abs(irSequence.get(position)) - getHasConsumed();
    }

    public void consume() {
        position++;
        hasConsumed = 0;
    }

    public void consume(double amount) {
        hasConsumed += amount;
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
     * @return the interleaving
     */
    public boolean isInterleaving() {
        return interleaving;
    }

    /**
     * @return the hasConsumed
     */
    public double getHasConsumed() {
        return hasConsumed;
    }

    /**
     * @param hasConsumed the hasConsumed to set
     */
    public void setHasConsumed(double hasConsumed) {
        this.hasConsumed = hasConsumed;
    }

    public boolean check(boolean on) {
        return isOn() == on
                && position < irSequence.getLength();
    }

    public double getExtentDuration() {
        int endPosition = position + 1;//IrCoreUtils.approximatelyEquals(hasConsumed, 0.0) ? position : position + 1;
        return irSequence.getDuration(extentStart, endPosition - extentStart);
    }

    public boolean allowChopping() {
        return ! interleaving;
    }

    void transferToNamesMap(Map<String, Long> nameEngine) {
        parameterCollector.transferToNamesMap(nameEngine);
    }

    void checkConsistency(Map<String, Long> nameMap) throws NameConflictException, UnassignedException {
        NameEngine nameEngine = new NameEngine(nameMap);
        nameEngine.add(definitions);
        needsChecking.checkConsistency(nameEngine, definitions);
        needsChecking = new ParameterCollector();
    }

    /**
     * @return the absoluteTolerance
     */
    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    /**
     * @return the relativeTolerance
     */
    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    NameEngine toNameEngine() throws InvalidNameException {
        NameEngine nameEngine = definitions.clone();
        nameEngine.add(parameterCollector.toNameEngine());
        return nameEngine;
    }

    /**
     * @return the danglingBitFieldData
     */
    BitwiseParameter getDanglingBitFieldData() {
        return danglingBitFieldData;
    }

    /**
     * @param data
     * @param bitmask
     */
    void setDanglingBitFieldData(long data, long bitmask) {
        danglingBitFieldData = new BitwiseParameter(data, bitmask);
    }

    void setDanglingBitFieldData() {
        danglingBitFieldData = new BitwiseParameter();
    }

    public boolean isFinished() {
        return position == irSequence.getLength();
    }
}
