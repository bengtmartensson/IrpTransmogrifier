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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 */
public class BareIrStream extends IrStreamItem {

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

    private static List<IrStreamItem> parse(List<IrpParser.Irstream_itemContext> list) {
        List<IrStreamItem> irStreamItems = new ArrayList<>(list.size());
        for (IrpParser.Irstream_itemContext item : list) {
            IrStreamItem irStreamItem = newIrStreamItem(item);
            irStreamItems.add(irStreamItem);
        }
        return irStreamItems;
    }

    private List<IrStreamItem> irStreamItems = null;
    private IrpParser.Bare_irstreamContext parseTree = null;

    public BareIrStream(IrpParser.Bare_irstreamContext ctx) {
        this(parse(ctx.irstream_item()));
        parseTree = ctx;
        //this(toList(ctx, env), env);
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

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException, InvalidNameException {
        IrpUtils.entering(logger, "evaluate", this);
        IrSignal.Pass actualState = state;
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        for (IrStreamItem irStreamItem : irStreamItems) {
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);
            if (newState != null)
                actualState = newState;
            if (actualState.compareTo(pass) <= 0) {
                EvaluatedIrStream irStream = irStreamItem.evaluate(actualState, pass, nameEngine, generalSpec);
                if (irStream == null)
                    break;

                result.add(irStream);
            }
            IrSignal.Pass next = irStreamItem.stateWhenExiting(actualState);
            if (next != null)
                actualState = next;
        }
        result.setState(actualState);
        IrpUtils.entering(logger, "evaluate", result);
        return result;
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
    int numberOfBitSpecs() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBitSpecs()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("numberOfBareDurations", Integer.toString(numberOfBareDurations()));
        try {
            if (numberOfBits() >= 0)
                element.setAttribute("numberOfBits", Integer.toString(numberOfBits()));
        } catch (UnassignedException ex) {
            // numberOfBits has no meaninful value
        }
        for (IrStreamItem item : this.irStreamItems)
            element.appendChild(item.toElement(document));
        return element;
    }

    @Override
    int numberOfBareDurations() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBareDurations()).reduce(sum, Integer::sum);
        return sum;
    }


    @Override
    int numberOfBits() throws UnassignedException {
        int sum = 0;
        for (IrStreamItem item : irStreamItems) {
            sum += item.numberOfBits();
        }
        return sum;
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecStack) throws NameConflictException, InvalidNameException, IrpSemanticException {
        IrpUtils.entering(logger, "recognize " + pass, this);
        if (pass == IrSignal.Pass.intro && hasVariationWithIntroEqualsRepeat()) {
            IrpUtils.exiting(logger, "recognize " + pass, "pass (since variation with intro equals repeat)");
            return true;
        }

        for (int itemNr = 0; itemNr < irStreamItems.size(); itemNr++) {
            IrStreamItem irStreamItem = irStreamItems.get(itemNr);
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);
            if (newState != null)
                recognizeData.setState(newState);
            if (recognizeData.getState() != pass)
                continue;

            if (recognizeData.getState() == pass) {
                boolean success = false;
                try {
                    success = irStreamItem.recognize(recognizeData, pass, bitSpecStack);
                } catch (ArithmeticException | UnassignedException | IrpSyntaxException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                }
                if (!success) {
                    IrpUtils.exiting(logger, "recognize", "fail");
                    return false;
                }
            }
            IrSignal.Pass next = irStreamItem.stateWhenExiting(recognizeData.getState());
            if (next != null)
                recognizeData.setState(next);

            if (next == IrSignal.Pass.finish)
                break;
        }
        IrpUtils.exiting(logger, "recognize " + pass, "pass");
        return true;
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
/*
    public List<String> codeList(IrSignal.Pass state, IrSignal.Pass pass, CodeGenerator codeGenerator) {
        List<String> list = new ArrayList<>(irStreamItems.size());
        for (IrStreamItem item : irStreamItems) {
//            if (item instanceof IrStream && state == IrSignal.Pass.intro) {
//                IrStream irs = (IrStream) item;
//                if (irs.getRepeatMarker().isInfinite())
//                    state = IrSignal.Pass.repeat;
//            }
            IrSignal.Pass nextState = item.stateWhenEntering(pass);
            if (nextState != null)
                state = nextState;
            if (pass == null || pass == state) {
                String s = item.code(state, pass, codeGenerator);
                if (s != null && !s.isEmpty())
                    list.add(s);
            }

//            if (item instanceof IrStream && state == IrSignal.Pass.repeat) {
//                IrStream irs = (IrStream) item;
//                if (irs.getRepeatMarker().isInfinite())
//                    state = IrSignal.Pass.ending;
//            }
            nextState = item.stateWhenExiting(state);
            if (nextState != null)
                state = nextState;
        }
        return list;
    }

    @Override
    public String code(IrSignal.Pass state, IrSignal.Pass pass, CodeGenerator codeGenerator) {
        List<String> list = codeList(state, pass, codeGenerator);
        ItemCodeGenerator st = codeGenerator.newItemCodeGenerator("BareIrStream");
        st.addAttribute("body", list);
        return st.render();
    }*/

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
        //List<Map<String, Object>> list = new ArrayList<>(irStreamItems.size());
        for (IrStreamItem item : irStreamItems) {
//            if (item instanceof IrStream && state == IrSignal.Pass.intro) {
//                IrStream irs = (IrStream) item;
//                if (irs.getRepeatMarker().isInfinite())
//                    state = IrSignal.Pass.repeat;
//            }
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
                    //list.add(item.propertiesMapList(state, pass, generalSpec).get(0));

            }

//            if (item instanceof IrStream && state == IrSignal.Pass.repeat) {
//                IrStream irs = (IrStream) item;
//                if (irs.getRepeatMarker().isInfinite())
//                    state = IrSignal.Pass.ending;
//            }
            nextState = item.stateWhenExiting(state);
            if (nextState != null)
                state = nextState;
        }

//        ItemCodeGenerator st = codeGenerator.newItemCodeGenerator("BareIrStream");
//        st.addAttribute("body", list);
        Map<String, Object> result = new HashMap<>(2);
        result.put("kind", "BareIrStream"); // NOT this.getClass...
        result.put("items", list);
        return result;
    }
}
