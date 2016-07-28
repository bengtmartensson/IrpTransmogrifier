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
import java.util.logging.Logger;
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

    protected List<IrStreamItem> irStreamItems = null;
    //protected BitSpec bitSpec;
    //private int noAlternatives = 0;


//@Override
//    boolean stringOk(String s) {
//        return s.startsWith("(");
//    }

    public void concatenate(BareIrStream bareIrStream) {
        irStreamItems.addAll(bareIrStream.irStreamItems);
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return irStreamItems.isEmpty();
    }

    @Override
    public int numberOfAlternatives() {
        return noAlternatives;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int sum = 0;
        for (IrStreamItem item : irStreamItems)
            sum += item.numberOfInfiniteRepeats();
        return sum;
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            BitSpec bitSpec, double elapsed_)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this.toString() + "," + bitSpec);
        IrSignal.Pass actualState = state;
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, bitSpec, pass);
        for (IrStreamItem irStreamItem : irStreamItems) {
            IrSignal.Pass newState = irStreamItem.stateWhenEntering();
            if (pass == IrSignal.Pass.repeat && newState != null)
                actualState = newState;
            if (actualState.compareTo(pass) <= 0) {
                double elapsed = result.getElapsed();
                EvaluatedIrStream irStream = irStreamItem.evaluate(actualState, pass, nameEngine, generalSpec, bitSpec, elapsed);
                result.add(irStream);
            }
            IrSignal.Pass next = irStreamItem.stateWhenExiting();
            if (next != null)
                actualState = next;
        }
        IrpUtils.entering(logger, "evaluate", result);
        return result;
    }

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
        this(ctx.irstream_item());
        //this(toList(ctx, env), env);
    }

    public BareIrStream(List<IrpParser.Irstream_itemContext> list) throws IrpSyntaxException, InvalidRepeatException {
        irStreamItems = new ArrayList<>();
        for (IrpParser.Irstream_itemContext item : list) {
            IrStreamItem irStreamItem = newIrStreamItem(item);
            irStreamItems.add(irStreamItem);
        }
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
        StringBuilder str = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (IrStreamItem item : irStreamItems)
            list.add(item.toIrpString());
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
    boolean interleavingOk() {
        boolean ok = true;
        boolean lastWasGap = true;
        for (IrStreamItem item : this.irStreamItems) {
            if (item instanceof Gap || item instanceof Extent) {
                if (lastWasGap)
                    return false;
                else
                    lastWasGap = true;
            } else if (item instanceof Flash) {
                if (lastWasGap)
                    lastWasGap = false;
                else
                    return false;
            } else if (item instanceof BitField) {
                if (!lastWasGap)
                    return false;
            }
            if (!item.interleavingOk())
                return false;
        }
        return true;
    }

    @Override
    int numberOfBitSpecs() {
        int sum = 0;
        for (IrStreamItem item : irStreamItems)
            sum += item.numberOfBitSpecs();
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
        for (IrStreamItem item : irStreamItems)
            sum += item.numberOfBareDurations();
        return sum;
    }

    @Override
    int numberOfBits() {
        int sum = 0;
        for (IrStreamItem item : irStreamItems)
            sum += item.numberOfBits();
        return sum;
    }
}
