/*
Copyright (C) 2019 Bengt Martensson.

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

public class Equation {

    private EquationSolving leftHandSide;
    private BitwiseParameter rightHandSide;
    //private long bitmask;
    private RecognizeData recognizeData;

    public Equation(EquationSolving leftHandSide, BitwiseParameter rightHandSide, RecognizeData recognizeData) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
        //this.bitmask = bitmask;
        this.recognizeData = recognizeData;
    }

    public Equation(EquationSolving leftHandSide, PrimaryItem rightHandSide, NameEngine nameEngine) throws NameUnassignedException {
        this(leftHandSide, rightHandSide.toLong(nameEngine), new RecognizeData(nameEngine));
    }

    public Equation(EquationSolving leftHandSide, Long rightHandSide, RecognizeData recognizeData) {
        this(leftHandSide, new BitwiseParameter(rightHandSide), recognizeData);
    }

    public Equation(EquationSolving leftHandSide, Long rightHandSide, long width, RecognizeData recognizeData) {
        this(leftHandSide, new BitwiseParameter(rightHandSide, IrCoreUtils.ones(width)), recognizeData);
    }

    public Equation(EquationSolving leftHandSide, Long rightHandSide) {
        this(leftHandSide, rightHandSide, new RecognizeData());
    }

    public Equation(String eq, long rhs) {
        this(Expression.newExpression(eq), rhs);
    }

    public Equation(String eq, String rhs, String string) throws InvalidNameException, NameUnassignedException {
        this(Expression.newExpression(eq), Expression.newExpression(rhs), new NameEngine(string));
    }

    public Equation(String eq, String rhs) throws NameUnassignedException {
        this(Expression.newExpression(eq), Expression.newExpression(rhs), NameEngine.EMPTY);
    }

    public boolean solve() throws NameUnassignedException {
        while (isOk() && ! isFinished())
            solveStep();

        return isOk();
    }

    private boolean isOk() {
        return leftHandSide != null && rightHandSide != null;
    }

    private boolean isFinished() {
        return leftHandSide instanceof Name/* && rightHandSide instanceof NumberExpression*/;
    }

    private void solveStep() throws NameUnassignedException {
        rightHandSide = leftHandSide.invert(rightHandSide, recognizeData);
        leftHandSide = leftHandSide.leftHandSide();
    }

    @Override
    public String toString() {
        return leftHandSide.toString() + "=" + rightHandSide.toString();
    }

    public Name getName() {
        return isOk() && isFinished() ? (Name) leftHandSide.leftHandSide() : null;
    }

    public BitwiseParameter getValue() {
        return isOk() && isFinished() ? rightHandSide : null;
    }

    public Long getBitmask() {
        return rightHandSide.getBitmask();
    }

    public boolean expandLhsSolve() throws NameUnassignedException {
        String name = getName().toString();
        Expression expression = recognizeData.getNameEngine().getPossiblyNull(name);
        if (expression != null) {
            leftHandSide = expression;
            return solve();
        }
        return false;
    }
}
