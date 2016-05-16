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

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 * @author Bengt Martensson
 */
public class IrStream extends BareIrStream {
    private RepeatMarker repeatMarker;

    //private ArrayList<PrimaryIrStreamItem> toPrimaryIrStreamItems() {
    //    return toPrimaryIrStreamItems(environment, irStreamItems);
    //}

    /*private static ArrayList<PrimaryIrStreamItem> toPrimaryIrStreamItems(Protocol environment, ArrayList<IrStreamItem> irstreamItems) {
        ArrayList<PrimaryIrStreamItem> primaryItems = new ArrayList<PrimaryIrStreamItem>();
        for (IrStreamItem item : irstreamItems) {
            BitStream bitStream = null;
            String type = item.getClass().getSimpleName();
            if (type.equals("Bitfield")) {
                bitStream = new BitStream(environment);

                bitStream.add((BitField)item, environment.getBitDirection());
            } else if (type.equals("Duration") || type.equals("Extent") || type.equals("IRStream")) {
                primaryItems.add((PrimaryIrStreamItem)item);
            } else {
                throw new RuntimeException("This-cannot-happen-item found: " + type);
            }
            if (bitStream != null) {
                    primaryItems.add(bitStream);
                    bitStream = null;
            }
        }
        return primaryItems;
    }*/

    public RepeatMarker getRepeatMarker() {
        return repeatMarker;
    }

    @Override
    EvaluatedIrStream evaluate(NameEngine nameEngine, GeneralSpec generalSpec, BitSpec bitSpec, IrSignal.Pass pass, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        EvaluatedIrStream total = new EvaluatedIrStream(nameEngine, generalSpec, bitSpec, pass);

        if (pass == IrSignal.Pass.repeat && repeatMarker.isInfinite())
            return total;

        for (int i = 0; i < repeatMarker.getMin(); i++) {
            EvaluatedIrStream irSequence = super.evaluate(nameEngine, generalSpec, bitSpec, pass, elapsed);
            total.add(irSequence);
        }

        return total;
    }

    // I hate the missing default arguments in Java!!!
//    public IrStream(Protocol env) {
//        this(env, null);
//    }

//    public IrStream(Protocol env, ArrayList<IrStreamItem>items) {
//        this(env, items, null, null);
//    }
//
//    public IrStream(Protocol env, ArrayList<IrStreamItem>items, RepeatMarker repeatMarker) {
//        this(env, items, repeatMarker, null);
//    }

    //public IrStream(Protocol env, BareIrStream bareIrStream, RepeatMarker repeatMarker, BitSpec bitSpec) {
        //this(env, bareIrStream != null ? bareIrStream.irStreamItems : null, repeatMarker, bitSpec);
    //}

    //public IrStream(Protocol env, IrStream src, BitSpec bitSpec) {
    //    this(env, src, src != null ? src.repeatMarker : null, bitSpec);
    //}

//    public IrStream(Protocol env, ArrayList<IrStreamItem>items, RepeatMarker repeatMarker, BitSpec bitSpec) {
//        super(env, items, bitSpec, 0);
//        this.repeatMarker = repeatMarker != null ? repeatMarker : new RepeatMarker();
//    }

    public IrStream(IrpParser.IrstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        super(ctx.bare_irstream());
        repeatMarker = new RepeatMarker(ctx.repeat_marker()); // FIXME
    }

    @Override
    public String toString() {
        return super.toString() + (repeatMarker != null ? repeatMarker.toString() : "");
    }

    public Element toElement(Document document) {
        Element root = document.createElement("bitspec");
        return root;
    }
}
