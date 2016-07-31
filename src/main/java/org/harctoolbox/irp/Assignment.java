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
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class models assignments as defined in Chapter 11.
 */
public class Assignment extends IrStreamItem implements Numerical {
    public static long parse(String str, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        Assignment assignment = new Assignment(str);
        return assignment.toNumber(nameEngine);
    }
    private Name name;
    private Expression value;
    private IrpParser.AssignmentContext parseTree = null;

    public Assignment(String str) {
        this((new ParserDriver(str)).getParser().assignment());
    }

    public Assignment(IrpParser.AssignmentContext assignment) {
        this(assignment.name(), assignment.expression());
        parseTree = assignment;
    }

    public Assignment(IrpParser.NameContext name, IrpParser.ExpressionContext be) {
        this(new Name(name), new Expression(be));
    }

    public Assignment(Name name, Expression expression) {
        this.name = name;
        this.value = expression;
    }


    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return true;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return value.toNumber(nameEngine);
    }

    public String getName() {
        return name.toString();
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }

    @Override
    public String toIrpString() {
        return name.toIrpString() + "=" + value.toIrpString();
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        if (state == pass) {
            long val = value.toNumber(nameEngine);
            nameEngine.define(name, val);
        }

        return new EvaluatedIrStream(nameEngine, generalSpec, pass);
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("assignment");
        element.appendChild(name.toElement(document));
        element.appendChild(value.toElement(document));
        return element;
    }

    @Override
    boolean interleavingOk() {
        return true;
    }

    @Override
    int numberOfBits() {
        return 0;
    }

    @Override
    int numberOfBareDurations() {
        return 0;
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public RecognizeData recognize(RecognizeData inData, IrSignal.Pass pass, GeneralSpec generalSpec, ArrayList<BitSpec> bitSpecs)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        NameEngine nameEngine = inData.getNameEngine().clone();

        if (inData.getState() == pass)
            nameEngine.define(name, value.toNumber(nameEngine));

        return new RecognizeData(inData.getIrSequence(), inData.getStart(), 0, inData.getState(), nameEngine);
    }
}
