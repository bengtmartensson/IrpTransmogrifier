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
import java.util.Set;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 */
public class IrStream extends IrpObject implements IrStreamItem,AggregateLister {

    private static final Logger logger = Logger.getLogger(IrStream.class.getName());

    private final RepeatMarker repeatMarker; // must not be null!
    private final BareIrStream bareIrStream;
    private final ParserRuleContext parseTree;

    public IrStream(String str) {
        this(new ParserDriver(str).getParser().irstream());
    }

    public IrStream(IrpParser.IrstreamContext ctx) {
        bareIrStream = new BareIrStream(ctx.bare_irstream());
        IrpParser.Repeat_markerContext ctxRepeatMarker = ctx.repeat_marker();
        repeatMarker = ctxRepeatMarker != null ? new RepeatMarker(ctxRepeatMarker) : new RepeatMarker();
        parseTree = ctx;
    }

    public IrStream(List<IrStreamItem> irStreamItems, RepeatMarker repeatMarker) {
        this(new BareIrStream(irStreamItems), repeatMarker);
    }

    public IrStream(BareIrStream bareIrStream, RepeatMarker repeatMarker) {
        this.bareIrStream = bareIrStream;
        this.repeatMarker = repeatMarker;
        parseTree = null;
    }

    public IrStream(BareIrStream bareIrStream) {
        this(bareIrStream, new RepeatMarker());
    }

    public IrStream(List<IrStreamItem> irStreamItems) {
        this(irStreamItems, new RepeatMarker());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IrStream))
            return false;

        IrStream other = (IrStream) obj;

        return bareIrStream.equals(other.bareIrStream) && repeatMarker.equals(other.repeatMarker);
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
        return "(" + bareIrStream.toIrpString() + ")" + repeatMarker.toIrpString();
    }

    public boolean isRepeatSequence() {
        return repeatMarker.isInfinite();
    }

    @Override
    public Element toElement(Document document) {
        Element element = bareIrStream.toElement(document, "IrStream");
        if (repeatMarker.getMin() != 1)
            element.setAttribute("repeatMin", Integer.toString(repeatMarker.getMin()));
        if (repeatMarker.getMax() != 1)
            element.setAttribute("repeatMax", repeatMarker.isInfinite() ? "infinite" : Integer.toString(repeatMarker.getMax()));
        element.setAttribute("isRepeat", Boolean.toString(isRepeatSequence()));
//        Integer n = numberOfBits();
//        if (n != null)
//                element.setAttribute("numberOfBits", Integer.toString(n));
//
//        n = numberOfBareDurations(false);
//        if (n != null)
//            element.setAttribute("numberOfBareDurations", Integer.toString(n));

        if (!repeatMarker.isTrivial())
            element.appendChild(repeatMarker.toElement(document));

        return element;
    }

    @Override
    public Integer numberOfBareDurations(boolean recursive) {
        if (!recursive && isInfiniteRepeat())
            return 0;

        return bareIrStream.numberOfBareDurations(recursive);
    }

    @Override
    public Integer numberOfBits() {
        return bareIrStream.numberOfBits();
    }

    @Override
    public Integer numberOfBitSpecs() {
        return bareIrStream.numberOfBitSpecs();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int noir = bareIrStream.numberOfInfiniteRepeats();
        return repeatMarker.isInfinite() ? noir + 1
                : repeatMarker.getMin() * noir;
    }

    private boolean evaluateTheRepeat(IrSignal.Pass pass) {
        return pass == IrSignal.Pass.repeat && isInfiniteRepeat();
    }

    private int numberRepetitions(IrSignal.Pass pass) {
        return evaluateTheRepeat(pass) ? 1 : getMinRepeats();
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        Pass pass = null;
        bareIrStream.decode(recognizeData, bitSpecs);
    }

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public List<IrStreamItem> extractPass(Pass pass, Pass state) {
        List<IrStreamItem> list = new ArrayList<>(8);
        int repetitions = numberRepetitions(pass);
        if (evaluateTheRepeat(pass))
            state = IrSignal.Pass.repeat;
        for (int i = 0; i < repetitions; i++)
            list.addAll(bareIrStream.extractPass(pass, state));
        return list;
    }

    @Override
    public void render(RenderData traverseData, List<BitSpec> bitSpecs) throws InvalidNameException, UnassignedException, IrpSemanticException, NameConflictException, IrpSignalParseException {
        bareIrStream.render(traverseData, bitSpecs);
    }

    @Override
    public Integer numberOfDurations(Pass pass) {
        return numberRepetitions(pass) * bareIrStream.numberOfDurations(pass);
    }

    public boolean hasVariation(boolean recursive) {
        return bareIrStream.hasVariation(recursive);
    }

    boolean isRPlus() {
        return repeatMarker.isInfinite() && repeatMarker.getMin() > 0 && ! hasVariation(true);
    }

    @Override
    public int weight() {
        return bareIrStream.weight() + repeatMarker.weight();
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> m = new HashMap<>(3);
        m.put("intro", propertiesMap(IrSignal.Pass.intro, generalSpec, nameEngine));
        m.put("repeat", propertiesMap(IrSignal.Pass.repeat, generalSpec, nameEngine));
        m.put("ending", propertiesMap(IrSignal.Pass.ending, generalSpec, nameEngine));
        return m;
    }

    // Top level only, not called recursively
    private Map<String, Object> propertiesMap(Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> m = new HashMap<>(4);
        m.put("kind", "FunktionBody");
        //Map<String, Object> body = null;
        //propertiesMapData = new PropertiesMapData(pass, generalSpec, nameEngine);

        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
        Map<String, Object> body = propertiesMap(state, pass, generalSpec, nameEngine);
        m.put("irStream", body);
        m.put("reset", hasExtent());
        m.put("numberOfDurations", numberOfDurations(pass));
        return m;
    }

    @Override
    public Map<String, Object> propertiesMap(Pass state, Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        int repetitions = numberRepetitions(pass);
        if (repetitions == 0)
            return new HashMap<>(0);

        Map<String, Object> m = new HashMap<>(2);
        Map<String, Object> body = bareIrStream.propertiesMap(state, pass, generalSpec, nameEngine);
        List<Map<String, Object>> items = (List<Map<String,Object>>) body.get("items");
        m.put("kind", body.get("kind"));
        ArrayList<Map<String, Object>> repeatedList = new ArrayList<>(repetitions*items.size());
        m.put("items", repeatedList);
        for (int r = 0; r < repetitions; r++)
            repeatedList.addAll(items);
        return m;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return bareIrStream.isEmpty(nameEngine);
    }

    @Override
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream.interleavingOk(generalSpec, nameEngine, last, gapFlashBitSpecs);
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream.interleavingOk(toCheck, generalSpec, nameEngine, last, gapFlashBitSpecs);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream.endingDurationType(last, gapFlashBitSpecs);
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasExtent() {
        return bareIrStream.hasExtent();
    }

    @Override
    public Set<String> assignmentVariables() {
        return bareIrStream.assignmentVariables();
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return repeatMarker.getMin() * bareIrStream.microSeconds(generalSpec, nameEngine);
    }

    boolean startsWithDuration() {
        return bareIrStream.startsWithDuration();
    }

    boolean hasVariationWithIntroEqualsRepeat() {
        return bareIrStream.hasVariationWithIntroEqualsRepeat();
    }
}
