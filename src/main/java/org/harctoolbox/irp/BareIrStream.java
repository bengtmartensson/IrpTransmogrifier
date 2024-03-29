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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 */
public final class BareIrStream extends IrpObject implements IrStreamItem {

    private static final Logger logger = Logger.getLogger(BareIrStream.class.getName());

    static DurationType startingDurationType(BareIrStream bareIrStream, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream == null ? DurationType.none : bareIrStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    static DurationType endingDurationType(BareIrStream bareIrStream, DurationType last, boolean gapFlashBitSpecs) {
        return bareIrStream == null ? DurationType.none : bareIrStream.startingDuratingType(last, gapFlashBitSpecs);
    }

    static boolean interleavingOk(BareIrStream bareIrStream, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(last, gapFlashBitspecs);
    }

    static boolean interleavingOk(DurationType toCheck, BareIrStream bareIrStream, DurationType last, boolean gapFlashBitspecs) {
        return bareIrStream == null || bareIrStream.interleavingOk(last, gapFlashBitspecs);
    }

    private static List<IrStreamItem> parse(List<IrpParser.Irstream_itemContext> list) {
        List<IrStreamItem> irStreamItems = new ArrayList<>(list.size());
        list.stream().map((item) -> IrStreamItem.newIrStreamItem(item)).forEachOrdered((irStreamItem) -> {
            irStreamItems.add(irStreamItem);
        });
        return irStreamItems;
    }

    private static List<IrStreamItem> mkIrStreamItemList(IrStreamItem irStreamItem) {
        List<IrStreamItem> list = new ArrayList<>(1);
        list.add(irStreamItem);
        return list;
    }

    private List<IrStreamItem> irStreamItems;
    private Integer numberInfiniteRepeatsCached = null; // Assume immutability

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

    public BareIrStream(IrStreamItem irStreamItem) {
        this(mkIrStreamItemList(irStreamItem));
    }

    private BareIrStream(ParserDriver parserDriver) {
        this(parserDriver.getParser().bare_irstream());
    }

    public BareIrStream(String s) {
        this(new ParserDriver(s));
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
        if (numberInfiniteRepeatsCached == null) {
            numberInfiniteRepeatsCached = 0;
            irStreamItems.forEach(item -> {
                numberInfiniteRepeatsCached += item.numberOfInfiniteRepeats();
            });
        }

        return numberInfiniteRepeatsCached;
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

    public boolean hasVariation(Pass pass) {
        for (IrStreamItem irStreamItem : irStreamItems) {
            if (irStreamItem instanceof Variation)
                if (((Variation) irStreamItem).hasPart(pass))
                    return true;
            if (irStreamItem instanceof BitspecIrstream)
                if (((BitspecIrstream) irStreamItem).hasVariation(pass))
                    return true;
            if (irStreamItem instanceof BareIrStream)
                if (((BareIrStream) irStreamItem).hasVariation(pass))
                    return true;
            if (irStreamItem instanceof IrStream)
                if (((IrStream) irStreamItem).hasVariation(pass))
                    return true;
        }
        return false;
    }

    public boolean hasVariationNonRecursive() {
        return irStreamItems.stream().anyMatch(irStreamItem -> (irStreamItem instanceof Variation));
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
        Integer nobd = numberOfBareDurations();
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
    public Integer numberOfBareDurations() {
        int sum = 0;
        for (IrStreamItem item : irStreamItems) {
            Integer nobd = item.numberOfBareDurations();
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
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack, boolean isLast) throws SignalRecognitionException {
        logger.log(recognizeData.logRecordEnter(this));
        int currentLevel = recognizeData.getLevel();
        recognizeData.setLevel(currentLevel + 1);
        for (Iterator<IrStreamItem> it = irStreamItems.iterator(); it.hasNext();) {
            IrStreamItem irStreamItem = it.next();
            irStreamItem.decode(recognizeData, bitSpecStack, isLast && !it.hasNext());
        }
        recognizeData.setLevel(currentLevel);
        logger.log(recognizeData.logRecordExit(this));
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) throws NameUnassignedException {
        render(renderData, bitSpecStack);
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) throws NameUnassignedException {
        for (IrStreamItem irStreamItem : irStreamItems)
            irStreamItem.render(renderData, bitSpecs);
    }

    @Override
    public BareIrStream extractPass(IrSignal.Pass pass, IrStream.PassExtractorState state) {
        List<IrStreamItem> list = new ArrayList<>(irStreamItems.size());

        for (IrStreamItem irStreamItem : irStreamItems) {
            irStreamItem.updateStateWhenEntering(pass, state);

            if (state.getState() == pass)
                list.addAll(irStreamItem.extractPass(pass, state).getIrStreamItems());

            irStreamItem.updateStateWhenExiting(pass, state);

            if (state.getState() == IrSignal.Pass.finish)
                break;
        }
        return new BareIrStream(list);
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

    public List<Duration> getDurations() {
        List<Duration> result = new ArrayList<>(irStreamItems.size());
        irStreamItems.stream().filter((irStreamItem) -> (irStreamItem instanceof Duration)).forEachOrdered((irStreamItem) -> {
            result.add((Duration) irStreamItem);
        });

        return result;
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

        Integer nobd = numberOfBareDurations();
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
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(current, gapFlashBitSpecs))
                return false;
            current = item.endingDurationType(last, gapFlashBitSpecs);
        }
        return true;
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs) {
        DurationType current = last;
        for (IrStreamItem item : irStreamItems) {
            if (!item.interleavingOk(toCheck, current, gapFlashBitSpecs))
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
                list.addAll((Collection<? extends Map<String, Object>>) m.get("items"));
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

    @Override
    public Integer guessParameterLength(String name) {
        for (IrStreamItem item : irStreamItems) {
            Integer result = item.guessParameterLength(name);
            if (result != null)
                return result;
        }
        return null;
    }

    @Override
    public BareIrStream substituteConstantVariables(Map<String, Long> constantVariables) {
        List<IrStreamItem> newList = new ArrayList<>(irStreamItems.size());
        irStreamItems.forEach((item) -> {
            newList.add(item.substituteConstantVariables(constantVariables));
        });
        return new BareIrStream(newList);
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        TreeSet<Double> result = new TreeSet<>();
        irStreamItems.forEach((item) -> {
            result.addAll(item.allDurationsInMicros(generalSpec, nameEngine));
        });
        return result;
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return irStreamItems.stream().noneMatch((irStreamItem) -> (!irStreamItem.constant(nameEngine)));
    }

    @Override
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
        for (IrStreamItem irStreamItem : irStreamItems)
            irStreamItem.createParameterSpecs(parameterSpecs);
    }
}
