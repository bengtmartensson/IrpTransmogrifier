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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.xml.XmlExport;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class IrpObject implements XmlExport, Serializable {

    private final static int DEFAULT_RADIX = 10;

    private final ParseTree parseTree;

    protected IrpObject(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public final ParseTree getParseTree() {
        return parseTree;
    }

    /**
     * Returns a computed IRP (-segment) string representation of current IrpObject.
     * Numerical parameters, but not durations etc, will be printed using the
     * radix in the argument.
     * @param radix Radix for parameters.
     * @return Formatted string.
     */
    public abstract String toIrpString(int radix);

    /**
     * Defaulted version of toIrpString().
     * @return
     */
    public final String toIrpString() {
        return toIrpString(DEFAULT_RADIX);
    }

    @Override
    public String toString() {
        return toIrpString(10);
    }

    public String toString(int radix) {
        return toIrpString(radix);
    }

    /**
     * Return a LISP-like representation of the current object.
     * Mainly for debugging.
     * @param parser
     * @return
     */
    public final String toStringTree(IrpParser parser) {
        return parseTree != null ? parseTree.toStringTree(parser) : null;
    }

    public final String toStringTree(ParserDriver parserDriver) {
        return toStringTree(parserDriver.getParser());
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    public int numberOfInfiniteRepeats() {
        return 0;
    }

    public final TreeViewer toTreeViewer(ParserDriver parserDriver) {
        return toTreeViewer(parserDriver.getParser());
    }

    public final TreeViewer toTreeViewer(IrpParser parser) {
        return toTreeViewer(Arrays.asList(parser.getRuleNames()));
    }

    public final TreeViewer toTreeViewer(List<String> ruleNames) {
        return new TreeViewer(ruleNames, parseTree);
    }

//    public final TreeViewer toTreeViewer() {
//        return null;
//        //List<String> ruleNames = Arrays.asList(parser.getRuleNames());
//        //return new TreeViewer(ruleNames, parseTree);
//    }

    /**
     * Returns a (somewhat arbitrary) measure of the complexity of the object. Can be used
     * for determining if a decode is "simpler" than another decode.
     * @return non-negative integer.
     */
    public abstract int weight();

//    @Override
//    public abstract int hashCode();

    @Override
    public Element toElement(Document document) {
        return document.createElement(getClass().getSimpleName());
    }

    @Override
    public Document toDocument() {
        Document document = XmlUtils.newDocument();
        Element element = toElement(document);
        document.appendChild(element);
        return document;
    }

    public Integer numberOfBits() {
        return 0;
    }

    public Integer numberOfBitSpecs() {
        return 0;
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    public void prerender(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) {
    }

    public Map<String, Object> propertiesMap(int noProperties) {
        return IrpUtils.propertiesMap(noProperties, this);
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    public void updateStateWhenEntering(IrSignal.Pass pass, IrStream.PassExtractorState state) {
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    public void updateStateWhenExiting(IrSignal.Pass pass, IrStream.PassExtractorState state) {
    }

    // Default implementation, often overridden
    @SuppressWarnings("NoopMethodInAbstractClass")
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
    }
}
