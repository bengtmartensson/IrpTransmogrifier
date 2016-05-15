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
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements BitSpecs, as described in Chapter 7.
 *
 */
public class BitSpec extends IrStreamItem {

    // Number of bits encoded
    private int chunkSize;

    private List<BareIrStream> bitCodes;

    // Computes the upper integer part of the 2-logarithm of the integer n.
    // Treat n = 1 differently, since coding on a one-letter alphaber is ... special.
    private static int computeNoBits(int n) {
        if (n == 1)
            return 1;
        int x = n-1;
        int m;
        for (m = 0; x != 0; m++)
            x >>= 1;
        return m;
    }
/*
    public BitSpec(List<BareIrStream> s, Protocol env) {
        super(env);
        bitCodes = s;
        chunkSize = computeNoBits(s.size());
    }

    //public BitSpec(Protocol env, List<PrimaryIrStream> list) {
    //    this(env, list.toArray(new PrimaryIrStream[list.size()]));
    //}

    //public BitSpec(IrpParser.BitspecContext ctx, Protocol env) {
    //    this(toList(ctx, env), env);
    //}
    */

    public BitSpec(IrpParser.BitspecContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(ctx.bare_irstream());
    }

    private BitSpec(List<IrpParser.Bare_irstreamContext> list) throws IrpSyntaxException, InvalidRepeatException {
        chunkSize = computeNoBits(list.size());
        bitCodes = new ArrayList<>();
        for (IrpParser.Bare_irstreamContext bareIrStreamCtx : list) {
            BareIrStream bareIrStream = new BareIrStream(bareIrStreamCtx);
            bitCodes.add(bareIrStream);
        }
    }

    /*
    private static List<PrimaryIrStream> toList(IrpParser.BitspecContext ctx, Protocol env) {
        List<PrimaryIrStream> array = new ArrayList<>();
        //for (int i = 0; i < ctx.getChildCount(); i++)
        //    array.add(new BareIrStream(ctx.getChild(i), env));
        return array;
    }

    /*
        ArrayList<PrimaryIrStream> list = new ArrayList<>();
        for (int i = 0; i < tree.getChildCount(); i++)
            list.add(bare_irstream((CommonTree)tree.getChild(i), level+1, forceOk, Pass.intro, repeatMarker));
        BitSpec b = new BitSpec(env, list);
        return b;
    }*/

    public BareIrStream getBitIrsteam(int index) throws IncompatibleArgumentException {
        if (index >= bitCodes.size())
            throw new IncompatibleArgumentException("Cannot encode " + index + " with current bitspec.");
        return bitCodes.get(index);
    }

    /*public void assignBitSpecs(BitSpec bitSpec) {
        for (IrStream pis : bitCodes) {
            pis.assignBitSpecs(bitSpec);
        }
    }*/

    @Override
    public String toString() {
        if (bitCodes == null || bitCodes.isEmpty())
            return "<null>";

        StringBuilder s = new StringBuilder();
        s.append("<").append(bitCodes.get(0));
        for (int i = 1; i < bitCodes.size(); i++) {
            //s += (i > 0 ? "; " : "") + "bitCodes[" + i + "]=" + bitCodes[i];
            s.append("|").append(bitCodes.get(i));
        }
        return s.append(">").toString();
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return bitCodes.isEmpty();
    }

    public Element toElement(Document document) {
        Element root = document.createElement("bitspec");
        root.setAttribute("size", Integer.toString(bitCodes.size()));
        root.setAttribute("chunksize", Integer.toString(chunkSize));
        return root;
    }

    @Override
    public List<IrStreamItem> evaluate(BitSpec bitSpec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
