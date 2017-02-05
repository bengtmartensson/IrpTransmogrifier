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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 */
public class IrStream extends BareIrStream implements AggregateLister {

    private static final Logger logger = Logger.getLogger(IrStream.class.getName());

    private RepeatMarker repeatMarker; // must not be null!

    public IrStream(String str) {
        this(new ParserDriver(str).getParser().irstream());
    }

    public IrStream(IrpParser.IrstreamContext ctx) {
        super(ctx.bare_irstream());
        IrpParser.Repeat_markerContext ctxRepeatMarker = ctx.repeat_marker();
        repeatMarker = ctxRepeatMarker != null ? new RepeatMarker(ctxRepeatMarker) : new RepeatMarker();
    }

    public IrStream(List<IrStreamItem> irStreamItems, RepeatMarker repeatMarker) {
        super(irStreamItems);
        this.repeatMarker = repeatMarker;
    }

    public IrStream(List<IrStreamItem> irStreamItems) {
        this(irStreamItems, new RepeatMarker());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IrStream))
            return false;

        IrStream other = (IrStream) obj;

        return super.equals(obj) && repeatMarker.equals(other.repeatMarker);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.repeatMarker);
        return hash;
    }

    public RepeatMarker getRepeatMarker() {
        return repeatMarker;
    }

    private int getMinRepeats() {
        return repeatMarker.getMin();
    }

    private boolean isInfiniteRepeat() {
        return repeatMarker.isInfinite();
    }

    @Override
    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass) {
        return hasVariation(false) ? pass
                : (pass == IrSignal.Pass.repeat || pass == IrSignal.Pass.ending) && isInfiniteRepeat() ? IrSignal.Pass.repeat
                : null;
    }

    @Override
    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass) {
        return isInfiniteRepeat() ? IrSignal.Pass.ending : null;
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        return "(" + super.toIrpString() + ")" + repeatMarker.toIrpString();
    }

    public boolean isRepeatSequence() {
        return repeatMarker.isInfinite();
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        if (repeatMarker.getMin() != 1)
            element.setAttribute("repeatMin", Integer.toString(repeatMarker.getMin()));
        if (repeatMarker.getMax() != 1)
            element.setAttribute("repeatMax", repeatMarker.isInfinite() ? "infinite" : Integer.toString(repeatMarker.getMax()));
        element.setAttribute("isRepeat", Boolean.toString(isRepeatSequence()));
        element.setAttribute("numberOfBitSpecs", Integer.toString(numberOfBitSpecs()));
        Integer n = numberOfBits();
        if (n != null)
                element.setAttribute("numberOfBits", Integer.toString(n));

        n = numberOfBareDurations(false);
        if (n != null)
            element.setAttribute("numberOfBareDurations", Integer.toString(n));

        if (!repeatMarker.isTrivial())
            element.appendChild(repeatMarker.toElement(document));

        return element;
    }

    public Element toElement(Document document, Pass pass) {
        Element element = super.toElement(document);
        return element;
    }

    @Override
    public Integer numberOfBareDurations(boolean recursive) {
        if (!recursive && isInfiniteRepeat())
            return 0;

        int sum = 0;
        sum = getIrStreamItems().stream().map((item) -> item.numberOfBareDurations(recursive)).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public Integer numberOfBits() {
        int sum = 0;
        sum = getIrStreamItems().stream().map((item) -> item.numberOfBits()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int noir = super.numberOfInfiniteRepeats();
        return repeatMarker.isInfinite() ? noir + 1
                : repeatMarker.getMin() * noir;
    }

    private boolean evaluateTheRepeat(IrSignal.Pass pass) {
        return pass == IrSignal.Pass.repeat && isInfiniteRepeat();
    }

    @Override
    public void traverse(Traverser traverseData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        IrpUtils.entering(logger, "traverse " + pass, this);
        traverseData.preprocess(this, pass, bitSpecs);
        if (evaluateTheRepeat(pass))
            traverseData.setState(IrSignal.Pass.repeat);
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        for (int i = 0; i < repetitions; i++)
            super.traverse(traverseData, pass, bitSpecs);
        traverseData.postprocess(this, pass, bitSpecs);
        IrpUtils.exiting(logger, "traverse " + pass);
    }

    boolean isRPlus() {
        return repeatMarker.isInfinite() && repeatMarker.getMin() > 0 && ! hasVariation(true);
    }

    @Override
    public int weight() {
        return super.weight() + repeatMarker.weight();
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> m = new HashMap<>(3);
        m.put("intro", propertiesMap(IrSignal.Pass.intro, generalSpec, nameEngine));
        m.put("repeat", propertiesMap(IrSignal.Pass.repeat, generalSpec, nameEngine));
        m.put("ending", propertiesMap(IrSignal.Pass.ending, generalSpec, nameEngine));
        return m;
    }

    private Map<String, Object> propertiesMap(Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> m = new HashMap<>(3);
        m.put("kind", "FunktionBody");

        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
        Map<String, Object> body = propertiesMap(state, pass, generalSpec, nameEngine);
        m.put("irStream", body);
        m.put("reset", hasExtent());
        return m;
    }

    @Override
    public Map<String, Object> propertiesMap(Pass state, Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        if (repetitions == 0)
            return new HashMap<>(0);

        Map<String, Object> m = new HashMap<>(2);
        Map<String, Object> body = super.propertiesMap(state, pass, generalSpec, nameEngine);
        List<Map<String, Object>> items = (List<Map<String,Object>>) body.get("items");
        m.put("kind", body.get("kind"));
        ArrayList<Map<String, Object>> repeatedList = new ArrayList<>(repetitions*items.size());
        m.put("items", repeatedList);
        for (int r = 0; r < repetitions; r++)
            repeatedList.addAll(items);
        return m;
    }
}
