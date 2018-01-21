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

import org.harctoolbox.ircore.ThisCannotHappenException;

public final class RecognizeOneParameterData extends AbstractRecognizeData {

    private int position;
    private final long oneParameter;

//    public RecognizeOneParameter(GeneralSpec generalSpec, NameEngine definitions, IrSequence irSequence, boolean interleaving, ParameterCollector nameMap,
//            double absoulteTolerance, double relativeTolerance, double minimumLeadout) {
//        this(generalSpec, definitions, irSequence, 0, nameMap, interleaving, absoulteTolerance, relativeTolerance, minimumLeadout);
//    }

    public RecognizeOneParameterData(GeneralSpec generalSpec, NameEngine definitions, long oneParameter, int numberBits, boolean interleaving) {
       this(generalSpec, definitions, oneParameter, numberBits, new ParameterCollector(), interleaving);
    }

    public RecognizeOneParameterData(GeneralSpec generalSpec, NameEngine definitions, long oneParameter, int numberBits,
            ParameterCollector parameterCollector, boolean interleaving) {
        super(generalSpec, definitions, parameterCollector, interleaving);
        //danglingBitFieldData = new BitwiseParameter();
        this.oneParameter = oneParameter;
        this.position = numberBits;
        //this.parameterCollector = parameterCollector;
        //this.interleaving = interleaving;
        //this.needsChecking = new ParameterCollector();
    }

//    /**
//     * Returns a shallow copy, except for the NameEngine, which is copied with NameEngine.clone().
//     * @return
//     */
//    @Override
//    @SuppressWarnings("CloneDeclaresCloneNotSupported")
//    public RecognizeData clone() {
//        RecognizeData result;
//        try {
//            result = (RecognizeData) super.clone();
//        } catch (CloneNotSupportedException ex) {
//            throw new InternalError(ex);
//        }
//        result.setParameterCollector(getParameterCollector().clone());
//        result.nameEngine = this.nameEngine.clone();
//        return result;
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

    void add(String name, BitwiseParameter parameter) throws ParameterInconsistencyException {
        if (getNameEngine().containsKey(name)) {
            Expression expression;
            try {
                expression = getNameEngine().get(name);
            } catch (NameUnassignedException ex) {
                throw new ThisCannotHappenException();
            }
            try {
                long expected = expression.toNumber(parameterCollector.toNameEngine());
                parameter.checkConsistency(name, expected);
            } catch (NameUnassignedException ex) {
                // It has an expression, but is not presently checkable.
                // mark for later checking.
                needsChecking.add(name, parameter);
            }
        } else
            parameterCollector.add(name, parameter);
    }

    void add(String name, long value, long bitmask) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value, bitmask));
    }

    void add(String name, long value) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value));
    }

    void incrementPosition(int i) {
        position += i;
    }

//    public boolean isOn() {
//        return Duration.isOn(position);
//    }
//
//    public double get() {
//        return  Math.abs(irSequence.get(position)) - getHasConsumed();
//    }
//
//    public void consume() {
//        position++;
//        hasConsumed = 0;
//    }
//
//    public void consume(double amount) {
//        hasConsumed += amount;
//    }



//    public boolean check(boolean on) {
//        return isOn() == on
//                && position < irSequence.getLength();
//    }


//    public boolean allowChopping() {
//        return ! interleaving;
//    }



//    @Override
//    public boolean isFinished() {
//        return remaining() == 0;
//    }
//
//    void finish() {
//        if (hasConsumed > 0) {
//            position++;
//            hasConsumed = 0;
//        }
//    }
}
