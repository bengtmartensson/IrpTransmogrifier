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

    static boolean interleavingOk(BareIrStream bareIrStream, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    static boolean interleavingOk(DurationType toCheck, BareIrStream bareIrStream, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(generalSpec, nameEngine, last, gapFlashBitspecs);
    }

    private static List<IrStreamItem> parse(List<IrpParser.Irstream_itemContext> list) {
        List<IrStreamItem> irStreamItems = new ArrayList<>(list.size());
        list.stream().map((item) -> IrStreamItem.newIrStreamItem(item)).forEachOrdered((irStreamItem) -> {
            irStreamItems.add(irStreamItem);
        });
        return irStreamItems;
    }

    private List<IrStreamItem> irStreamItems;

    public BareIrStream(IrpParser.Bare_irstreamContext ctx) {
        super(ctx);
        irStreamItems = parse(ctx.irstream_item());
    }

    public BareIrStream() {
        this(new ArrayList<>(0));
    }

    public BareIrStream(List<IrStreamItem> list) {
        super(null);
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

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException {
        RenderData renderData = new RenderData(generalSpec, nameEngine);
        evaluate(renderData, new ArrayList<>(0));
        return renderData.getEvaluatedIrStream();
    }

    public boolean startsWithFlash() {
        for (IrStreamItem irStreamItem : irStreamItems) {
            if (irStreamItem instanceof Flash)
                return true;
            if (irStreamItem instanceof Gap)
                return false;
            if (irStreamItem instanceof BitField)
                return false;
            if (irStreamItem instanceof BitspecIrstream)
                return ((BitspecIrstream) irStreamItem).startsWithFlash();
            if (irStreamItem instanceof BareIrStream)
                return ((BareIrStream) irStreamItem).startsWithFlash();
            if (irStreamItem instanceof IrStream)
                return ((IrStream) irStreamItem).startsWithFlash();
            if (irStreamItem instanceof Variation)
                return ((Variation) irStreamItem).startsWithFlash();
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
    public String toIrpString(int radix) {
        StringBuilder str = new StringBuilder(irStreamItems.size()*20);
        List<String> list = new ArrayList<>(irStreamItems.size());
        irStreamItems.stream().forEach((item) -> {
            list.add(item.toIrpString(radix));
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
        return fillElement(document, element);
    }

    public Element toElement(Document document, String tagName) {
        Element element = document.createElement(tagName);
        return fillElement(document, element);
    }

    private Element fillElement(Document document, Element element) {
        Integer nobd = numberOfBareDurations(true);
        if (nobd != null)
           element.setAttribute("numberOfBareDurations", Integer.toString(nobd));
        Integer nob = numberOfBits();
        if (nob != null)
            element.setAttribute("numberOfBits", Integer.toString(nob));
        Integer nod = numberOfDurations();
        if (nod != null)
            element.setAttribute("numberOfDurations", Integer.toString(nod));
        element.setAttribute("numberOfBitSpecs", Integer.toString(numberOfBitSpecs()));
        irStreamItems.forEach((item) -> {
            element.appendChild(item.toElement(document));
        });
        return element;
    }

    @Override
    public Integer numberOfBareDurations(boolean recursive) {
        int sum = 0;
        for (IrStreamItem item : irStreamItems) {
            Integer nobd = item.numberOfBareDurations(recursive);
            if (nobd == null)
                return null;
            sum += nobd;
        }
        return sum;
    }


    @Override
    public Integer numberOfBits() {
        int sum = 0;
        for (IrStreamItem item : irStreamItems) {
            Integer nob = item.numberOfBits();
            if (nob == null)
                return null;
            sum += nob;
        }
        return sum;
    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack) throws SignalRecognitionException {
        IrSignal.Pass pass = null;
        for (IrStreamItem irStreamItem : irStreamItems)
            irStreamItem.decode(recognizeData, bitSpecStack);
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) throws NameUnassignedException {
        for (IrStreamItem irStreamItem : irStreamItems)
            irStreamItem.evaluate(renderData, bitSpecStack);
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) throws NameUnassignedException {
        for (IrStreamItem irStreamItem : irStreamItems)
            irStreamItem.render(renderData, bitSpecs);
    }

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public List<IrStreamItem> extractPass(IrSignal.Pass pass, IrSignal.Pass state) {
        List<IrStreamItem> list = new ArrayList<>(irStreamItems.size());
        if (pass == IrSignal.Pass.intro && hasVariationWithIntroEqualsRepeat()) {
            IrpUtils.exiting(logger, "traverse " + pass, "pass (since variation with intro equals repeat)");
            return list;
        }

        for (IrStreamItem irStreamItem : irStreamItems) {
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);

            if (newState != null)
                state = newState;

            if (state == pass || pass == null)
                list.addAll(irStreamItem.extractPass(pass, state));

            IrSignal.Pass next = irStreamItem.stateWhenExiting(state);
            if (next != null)
                state = next;

            if (next == IrSignal.Pass.finish)
                break;
        }
        return list;
    }

    @Override
    public boolean nonConstantBitFieldLength() {
        return irStreamItems.stream().anyMatch((irStreamItem) -> (irStreamItem.nonConstantBitFieldLength()));
    }

    @Override
    public Integer numberOfDurations() {
        int sum = 0;
        for (IrStreamItem irStreamItem : irStreamItems) {
            Integer numberDurations = irStreamItem.numberOfDurations();
            if (numberDurations == null)
                return null;
            else
                sum += numberDurations;
        }
        return sum;
    }

    /**
     * Fallback version of numberOfDurations().
     * @param bitspecLength
     * @return
     */
    Integer numberOfDurations(int bitspecLength) {
        Integer nod = numberOfDurations();
        if (nod != null)
            return nod;

        Integer nobd = numberOfBareDurations(true);
        Integer nob = numberOfBits();
        return nobd != null && nob != null ? nobd + bitspecLength*nob : null;
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
    public boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(generalSpec, nameEngine, current, gapFlashBitSpecs))
                return false;
            current = item.endingDurationType(last, gapFlashBitSpecs);
        }
        return true;
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(toCheck, generalSpec, nameEngine, current, gapFlashBitSpecs))
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

    // Top level only, not called recursively
    public Map<String, Object> topLevelPropertiesMap(GeneralSpec generalSpec, NameEngine nameEngine, int bitSpecLength) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("kind", "FunctionBody");
        Map<String, Object> body = propertiesMap(generalSpec, nameEngine);
        map.put("irStream", body);
        map.put("reset", hasExtent());
        map.put("numberOfDurations", numberOfDurations(bitSpecLength));
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        List<Map<String, Object>> list = new ArrayList<>(irStreamItems.size());
        irStreamItems.stream().map((item) -> item.propertiesMap(generalSpec, nameEngine)).filter((m) -> (!m.isEmpty())).forEachOrdered((m) -> {
            if (m.containsKey("items"))
                list.addAll((List<Map<String, Object>>) m.get("items"));
            else
                list.add(m);
        });

        Map<String, Object> result = new HashMap<>(2);
        result.put("kind", getClass().getSimpleName());
        result.put("items", list);
        return result;
    }

    double averageDuration(GeneralSpec generalSpec, NameEngine nameEngine) {
        double sum = 0;
        sum = irStreamItems.stream().map((item) -> item.microSeconds(generalSpec, nameEngine)).reduce(sum, (accumulator, _item) -> accumulator + _item);
        return sum / irStreamItems.size();
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return null;
    }
}
