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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 */
public class BareIrStream extends IrpObject implements IrStreamItem {

    private static final Logger logger = Logger.getLogger(BareIrStream.class.getName());

    static DurationType startingDurationType(BareIrStream bareIrStream, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream == null ? DurationType.none : bareIrStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    static DurationType endingDurationType(BareIrStream bareIrStream, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream == null ? DurationType.none : bareIrStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    static boolean interleavingOk(BareIrStream bareIrStream, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    static boolean interleavingOk(DurationType toCheck, BareIrStream bareIrStream, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitspecs);
    }

    private static List<IrStreamItem> parse(List<IrpParser.Irstream_itemContext> list) {
        List<IrStreamItem> irStreamItems = new ArrayList<>(list.size());
        list.stream().map((item) -> IrStreamItem.newIrStreamItem(item)).forEachOrdered((irStreamItem) -> {
            irStreamItems.add(irStreamItem);
        });
        return irStreamItems;
    }

    private List<IrStreamItem> irStreamItems = null;
    private IrpParser.Bare_irstreamContext parseTree = null;

    public BareIrStream(IrpParser.Bare_irstreamContext ctx) {
        this(parse(ctx.irstream_item()));
        parseTree = ctx;
    }

    public BareIrStream() {
        irStreamItems = new ArrayList<>(0);
        parseTree = null;
    }

    public BareIrStream(List<IrStreamItem> list) {
        this.irStreamItems = list;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BareIrStream))
            return false;

        BareIrStream other = (BareIrStream) obj;
        if (irStreamItems.size() != other.irStreamItems.size())
            return false;

        for (int i = 0; i < irStreamItems.size(); i++)
            if (!irStreamItems.get(i).equals(other.irStreamItems.get(i)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.irStreamItems);
        return hash;
    }

    public void concatenate(BareIrStream bareIrStream) {
        irStreamItems.addAll(bareIrStream.irStreamItems);
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return isEmpty();
    }

    public boolean isEmpty() {
        return irStreamItems.isEmpty();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfInfiniteRepeats()).reduce(sum, Integer::sum);
        return sum;
    }

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        RenderData renderData = new RenderData(nameEngine, generalSpec);
        renderData.setState(state);
        traverse(renderData, pass, new ArrayList<>(0));
        return renderData.getEvaluatedIrStream();
    }

    public boolean startsWithDuration() {
        for (IrStreamItem irStreamItem : irStreamItems) {
            if (irStreamItem instanceof Duration)
                return true;
            if (irStreamItem instanceof BitField)
                return false;
            if (irStreamItem instanceof BitspecIrstream)
                return ((BitspecIrstream) irStreamItem).startsWithDuration();
            if (irStreamItem instanceof BareIrStream)
                return ((BareIrStream) irStreamItem).startsWithDuration();
        }
        return false; // give up
    }

    public boolean hasVariation(boolean recursive) {
        for (IrStreamItem irStreamItem : irStreamItems) {
            if (irStreamItem instanceof Variation)
                return true;
            if (recursive && irStreamItem instanceof BitspecIrstream)
                return ((BitspecIrstream) irStreamItem).hasVariation(recursive);
            if (recursive && irStreamItem instanceof BareIrStream)
                return ((BareIrStream) irStreamItem).hasVariation(recursive);
        }
        return false; // give up
    }

    public boolean hasVariationWithIntroEqualsRepeat() {
        for (IrStreamItem irStreamItem : irStreamItems) {
            if (irStreamItem instanceof Variation)
                return ((Variation) irStreamItem).introEqualsRepeat();
            if (irStreamItem instanceof BitspecIrstream)
                return ((BitspecIrstream) irStreamItem).hasVariationWithIntroEqualsRepeat();
            if (irStreamItem instanceof BareIrStream)
                return ((BareIrStream) irStreamItem).hasVariationWithIntroEqualsRepeat();
        }
        return false; // give up
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        StringBuilder str = new StringBuilder(irStreamItems.size()*20);
        List<String> list = new ArrayList<>(irStreamItems.size());
        irStreamItems.stream().forEach((item) -> {
            list.add(item.toIrpString());
        });
        return str.append(String.join(",", list)).toString();
    }

    @Override
    public Integer numberOfBitSpecs() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBitSpecs()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("numberOfBareDurations", Integer.toString(numberOfBareDurations(false)));
        if (numberOfBits() >= 0)
            element.setAttribute("numberOfBits", Integer.toString(numberOfBits())); // numberOfBits has no meaninful value
        this.irStreamItems.forEach((item) -> {
            element.appendChild(item.toElement(document));
        });
        return element;
    }

    @Override
    public Integer numberOfBareDurations(boolean recursive) {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBareDurations(recursive)).reduce(sum, Integer::sum);
        return sum;
    }


    @Override
    public Integer numberOfBits() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBits()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) {
    }

    @Override
    public void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecStack) throws IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException, IrpSignalParseException {
        IrpUtils.entering(logger, "traverse " + pass, this);
        if (pass == IrSignal.Pass.intro && hasVariationWithIntroEqualsRepeat()) {
            IrpUtils.exiting(logger, "traverse " + pass, "pass (since variation with intro equals repeat)");
            return;
        }

        recognizeData.preprocess(this, pass, bitSpecStack);
        for (int itemNr = 0; itemNr < irStreamItems.size(); itemNr++) {
            IrStreamItem irStreamItem = irStreamItems.get(itemNr);
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);
            if (newState != null)
                recognizeData.setState(newState);

            if (recognizeData.getState() == pass)
                irStreamItem.traverse(recognizeData, pass, bitSpecStack);

            IrSignal.Pass next = irStreamItem.stateWhenExiting(recognizeData.getState());
            if (next != null)
                recognizeData.setState(next);

            if (next == IrSignal.Pass.finish)
                break;
        }
        recognizeData.postprocess(this, pass, bitSpecStack);
        IrpUtils.exiting(logger, "traverse " + pass, "pass");
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems)
            current = item.endingDurationType(last, gapFlashBitSpecs);

        return current;
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return irStreamItems.isEmpty() ? DurationType.none : irStreamItems.get(0).startingDuratingType(last, gapFlashBitSpecs);
    }

    @Override
    public boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(nameEngine, generalSpec, current, gapFlashBitSpecs))
                return false;
            current = item.endingDurationType(last, gapFlashBitSpecs);
        }
        return true;
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(toCheck, nameEngine, generalSpec, current, gapFlashBitSpecs))
                return false;
            current = item.endingDurationType(last, gapFlashBitSpecs);
        }
        return true;
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = irStreamItems.stream().map((item) -> item.weight()).reduce(weight, Integer::sum);
        return weight;
    }

    @Override
    public boolean hasExtent() {
        return irStreamItems.stream().anyMatch((item) -> (item.hasExtent()));
    }

    /**
     * @return the irStreamItems
     */
    public List<IrStreamItem> getIrStreamItems() {
        return Collections.unmodifiableList(irStreamItems);
    }

    @Override
    public Set<String> assignmentVariables() {
        Set<String> set = new HashSet<>(4);
        irStreamItems.stream().forEach((item) -> {
            set.addAll(item.assignmentVariables());
        });
        return set;
    }

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) {
        List<Map<String, Object>> list = new ArrayList<>(irStreamItems.size());
        for (IrStreamItem item : irStreamItems) {
            IrSignal.Pass nextState = item.stateWhenEntering(pass);
            if (nextState != null)
                state = nextState;
            if (pass == null || pass == state) {
                Map<String, Object> m = item.propertiesMap(state, pass, generalSpec, nameEngine);
                if (!m.isEmpty()) {
                    if (m.containsKey("items"))
                        list.addAll((List<Map<String, Object>>)m.get("items"));
                    else
                        list.add(m);
                }
            }

            nextState = item.stateWhenExiting(state);
            if (nextState != null)
                state = nextState;
        }

        Map<String, Object> result = new HashMap<>(2);
        result.put("kind", "BareIrStream"); // NOT this.getClass...
        result.put("items", list);
        return result;
    }

    double averageDuration(NameEngine nameEngine, GeneralSpec generalSpec) throws IrpException {
        double sum = 0;
        sum = irStreamItems.stream().map((item) -> item.microSeconds(nameEngine, generalSpec)).reduce(sum, (accumulator, _item) -> accumulator + _item);
        return sum / irStreamItems.size();
    }

    @Override
    public void render(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException {
    }

    @Override
    public Double microSeconds(NameEngine nameEngine, GeneralSpec generalSpec) {
        return null;
    }
}
