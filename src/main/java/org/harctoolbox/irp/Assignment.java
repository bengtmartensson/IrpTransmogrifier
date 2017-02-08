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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class models assignments as defined in Chapter 11.
 */
public class Assignment extends IrpObject implements IrStreamItem, Numerical {
    public static long parse(String str, NameEngine nameEngine) throws UnassignedException {
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
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Assignment))
            return false;

        Assignment other = (Assignment) obj;
        return name.equals(other.name) && value.equals(other.value);
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return true;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException {
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
    public void render(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException {
        NameEngine nameEngine = renderData.getNameEngine();
        long val = value.toNumber(nameEngine);
        nameEngine.define(name, val);
    }

    @Override
    public void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        //recognizeData.preprocess(this, pass, bitSpecs);
        recognizeData.postprocess(this, pass, bitSpecs);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.appendChild(name.toElement(document));
        element.appendChild(value.toElement(document));
        return element;
    }

    @Override
    public Integer numberOfBareDurations(boolean recursive) {
        return 0;
    }

    @Override
    public Integer numberOfDurations(IrSignal.Pass pass) {
        return 0;
    }

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws InvalidNameException, UnassignedException {
        if (recognizeData.getState() == pass)
            recognizeData.getParameterCollector().setExpected(name.toString(), value.toNumber(recognizeData.toNameEngine()));
    }

    @Override
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return true;
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return true;
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.none;
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return DurationType.none;
    }

    @Override
    public int weight() {
        return name.weight() + value.weight();
    }

    @Override
    public boolean hasExtent() {
        return false;
    }

    @Override
    public Set<String> assignmentVariables() {
        Set<String> list = new HashSet<>(1);
        list.add(name.toString());
        return list;
    }

    @Override
    public Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(2);
        map.put("name", name.propertiesMap(false, generalSpec, nameEngine));
        map.put("expression", value.propertiesMap(true, generalSpec, nameEngine));
        return map;
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return 0.0;
    }
}
