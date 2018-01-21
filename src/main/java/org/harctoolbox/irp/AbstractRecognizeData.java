/*
Copyright (C) 2018 Bengt Martensson.

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

abstract class AbstractRecognizeData extends Traverser implements Cloneable {

    protected ParameterCollector parameterCollector;
    protected final boolean interleaving;
    protected ParameterCollector needsChecking;
    protected BitwiseParameter danglingBitFieldData;

    AbstractRecognizeData(GeneralSpec generalSpec, NameEngine nameEngine, ParameterCollector parameterCollector, boolean interleaving) {
        super(generalSpec, nameEngine);
        this.parameterCollector = parameterCollector;
        this.interleaving = interleaving;
        needsChecking = new ParameterCollector();
        danglingBitFieldData = new BitwiseParameter();
    }

    /**
     * Returns a shallow copy, except for the NameEngine, which is copied with NameEngine.clone().
     * @return
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public AbstractRecognizeData clone() {
        AbstractRecognizeData result;
        try {
            result = (AbstractRecognizeData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.setParameterCollector(getParameterCollector().clone());
        result.nameEngine = this.nameEngine.clone();
        return result;
    }

    /**
     * @return the interleaving
     */
    public boolean isInterleaving() {
        return interleaving;
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

    void checkConsistency() throws NameUnassignedException, ParameterInconsistencyException {
        NameEngine nameEngine = this.toNameEngine();
        needsChecking.checkConsistency(nameEngine, getNameEngine());
        needsChecking = new ParameterCollector();
    }

    NameEngine toNameEngine() {
        NameEngine nameEngine = getNameEngine().clone();
        nameEngine.add(parameterCollector.toNameEngine());
        return nameEngine;
    }
}
