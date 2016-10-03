/*
Copyright (C) 2011, 2016 Bengt Martensson.

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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 * @author Bengt Martensson
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
    //    private BareIrStream(List<IrpParser.Irstream_itemContext> list) throws IrpSyntaxException, InvalidRepeatException {
//        irStreamItems = new ArrayList<>(list.size());
//        for (IrpParser.Irstream_itemContext item : list) {
//            IrStreamItem irStreamItem = newIrStreamItem(item);
//            irStreamItems.add(irStreamItem);
//        }
//    }

    private static List<IrStreamItem> parse(List<IrpParser.Irstream_itemContext> list) throws IrpSyntaxException, InvalidRepeatException {
        List<IrStreamItem> irStreamItems = new ArrayList<>(list.size());
        for (IrpParser.Irstream_itemContext item : list) {
            IrStreamItem irStreamItem = newIrStreamItem(item);
            irStreamItems.add(irStreamItem);
        }
        return irStreamItems;
    }

    protected List<IrStreamItem> irStreamItems = null;
    //protected BitSpec bitSpec;
    //private int noAlternatives = 0;
    private IrpParser.Bare_irstreamContext parseTree = null;
    /*
    private static ArrayList<PrimaryIrStreamItem> toPrimaryIrStreamItems(Protocol environment, ArrayList<IrStreamItem> irstreamItems) {
    ArrayList<PrimaryIrStreamItem> primaryItems = new ArrayList<PrimaryIrStreamItem>();
    for (IrStreamItem item : irstreamItems) {
    BitStream bitStream = null;
    String type = item.getClass().getSimpleName();
    if (type.equals("Bitfield")) {
    if (bitStream == null)
    bitStream = new BitStream(environment);

    bitStream.add((BitField)item, environment.getBitDirection());
    } else if (type.equals("Duration") || type.equals("Extent") || type.equals("IRStream")) {
    if (bitStream != null) {
    primaryItems.add(bitStream);
    bitStream = null;
    }
    primaryItems.add((PrimaryIrStreamItem)item);
    } else {
    throw new RuntimeException("This-cannot-happen-item found: " + type);
    //assert false;
    }
    if (bitStream != null) {
    primaryItems.add(bitStream);
    bitStream = null;
    }
    }
    return primaryItems;
    }*/

    public BareIrStream(IrpParser.Bare_irstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
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

//@Override
//    boolean stringOk(String s) {
//        return s.startsWith("(");
//    }

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

//    @Override
//    public int numberOfAlternatives() {
//        return noAlternatives;
//    }

    @Override
    public int numberOfInfiniteRepeats() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfInfiniteRepeats()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this);
        IrSignal.Pass actualState = state;
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        for (IrStreamItem irStreamItem : irStreamItems) {
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);
            if (/*pass == IrSignal.Pass.repeat &&*/ newState != null)
                actualState = newState;
            if (actualState.compareTo(pass) <= 0) {
                //double elapsed = result.getElapsed();
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

/*
    private static List<IrStreamItem> toList(IrpParser.Bare_irstreamContext ctx, Protocol env) {
        List<IrStreamItem> array = new ArrayList<>();
        for (IrpParser.Irstream_itemContext item : ctx.irstream_item())
            array.add(IrStreamItem.parse(item));
        return array;
    }

    /*public BareIrStream(Protocol env) {
        this(env, null, null, 0);
    }*/

//    public BareIrStream(Protocol env, List<IrStreamItem>items, BitSpec bitSpec, int noAlternatives) {
//        super(env);
//        this.irStreamItems = items;
//        this.noAlternatives = noAlternatives;
//        //this.bitSpec = bitSpec;
//    }

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

    /*private static ArrayList<Double> normalize(ArrayList<Double> list, boolean nukeLeadingZeros) {
        if (list == null || list.isEmpty())
            return list;

        // Nuke leading gaps
        while (nukeLeadingZeros && list.size() > 1 && list.get(0) <= 0)
            list.remove(0);

        for (int i = 0; i < list.size(); i++) {
            while (i + 1 < list.size() && equalSign(list.get(i), list.get(i+1))) {
                double val = list.get(i) + list.get(i+1);
                list.remove(i);
                list.remove(i);
                list.add(i, val);
            }
        }
        return list;
    }*/

    @Override
    int numberOfBitSpecs() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBitSpecs()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("bare_irstream");
        element.setAttribute("numberOfBareDurations", Integer.toString(numberOfBareDurations()));
        element.setAttribute("numberOfBits", Integer.toString(numberOfBits()));
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
    int numberOfBits() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBits()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    ParserRuleContext getParseTree() {
        return parseTree;
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecStack)
            throws NameConflictException {
        IrpUtils.entering(logger, "recognize " + pass, this);
        ////IrStreamItem callersLookAheadItem = recognizeData.getLookAheadItem();
        //IrSignal.Pass state = recognizeData.getState();
        //int position = recognizeData.getStart();
        //NameEngine nameEngine = recognizeData.getNameEngine().clone();
        if (pass == IrSignal.Pass.intro && hasVariationWithIntroEqualsRepeat()) {
            IrpUtils.exiting(logger, "recognize " + pass, "pass (since variation with intro equals repeat)");
            return true;
        }

        for (int itemNr = 0; itemNr < irStreamItems.size(); itemNr++) {
            IrStreamItem irStreamItem = irStreamItems.get(itemNr);
            ////IrStreamItem lookAheadItem = itemNr < irStreamItems.size() - 1 ? irStreamItems.get(itemNr + 1)
            ////        : (itemNr == irStreamItems.size() - 1) ? callersLookAheadItem
            ////        : null;
            ////recognizeData.setLookAheadItem(lookAheadItem);
            IrSignal.Pass newState = irStreamItem.stateWhenEntering(pass);
            if (/*pass == IrSignal.Pass.repeat &&*/ newState != null)
                recognizeData.setState(newState);
            if (recognizeData.getState() != pass)
                continue;

            if (recognizeData.getState() == pass) {
                //double elapsed = result.getElapsed();
                //RecognizeData inData = new RecognizeData(recognizeData.getIrSequence(), position, 0, state, nameEngine.clone());
                //RecognizeData data;
                boolean success = false;
                try {
                    success = irStreamItem.recognize(recognizeData, pass, bitSpecStack);
                } catch (ArithmeticException | IncompatibleArgumentException | UnassignedException | IrpSyntaxException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                }
                if (!success) {
                    IrpUtils.exiting(logger, "recognize", "fail");
                    return false;
                }

                //nameEngine.addBarfByConflicts(data.getNameEngine());
                //position += data.getLength();
            }
            IrSignal.Pass next = irStreamItem.stateWhenExiting(recognizeData.getState());
            if (next != null)
                recognizeData.setState(next);

            if (next == IrSignal.Pass.finish)
                break;
        }
        //result.setState(actualState);
        //RecognizeData recognizeData = recognizeData.clone();
        //recognizeData.setLength(position - recognizeData.getStart());
        //recognizeData.setNameEngine(nameEngine);
        //new RecognizeData(initialData.getIrSequence(), initialData.getStart(), position - initialData.getStart(), state, nameEngine);
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
}
