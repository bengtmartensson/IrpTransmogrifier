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
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements BitSpecs, as described in Chapter 7.
 *
 */
public class BitSpec extends IrpObject {
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
//    private static int noInstances = 0;
//
//    public static void reset() {
//        noInstances = 0;
//    }
//
//    public static int getNoInstances() {
//        return noInstances;
//    }

    // Number of bits encoded
    private int chunkSize;

    private List<BareIrStream> bitCodes;

    public BitSpec(String str) throws IrpSyntaxException, InvalidRepeatException {
        this(new ParserDriver(str).getParser().bitspec());
    }

    public BitSpec(IrpParser.BitspecContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(ctx.bare_irstream());
    }

    private BitSpec(List<IrpParser.Bare_irstreamContext> list) throws IrpSyntaxException, InvalidRepeatException {
//        noInstances++;
        chunkSize = computeNoBits(list.size());
        bitCodes = new ArrayList<>(list.size());
        for (IrpParser.Bare_irstreamContext bareIrStreamCtx : list) {
            BareIrStream bareIrStream = new BareIrStream(bareIrStreamCtx);
            bitCodes.add(bareIrStream);
        }
    }

    BitSpec() {
        chunkSize = 0;
        bitCodes = new ArrayList<>(2);
    }

    public int size() {
        return bitCodes.size();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int sum = 0;
        sum = bitCodes.stream().map((code) -> code.numberOfInfiniteRepeats()).reduce(sum, Integer::sum);
        return sum;
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

    public BareIrStream get(int index) throws IncompatibleArgumentException {
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
        if (bitCodes.isEmpty())
            return "<null>";

        StringBuilder s = new StringBuilder(bitCodes.size()*10);
        s.append("<").append(bitCodes.get(0));
        for (int i = 1; i < bitCodes.size(); i++) {
            //s += (i > 0 ? "; " : "") + "bitCodes[" + i + "]=" + bitCodes[i];
            s.append("|").append(bitCodes.get(i));
        }
        return s.append(">").toString();
    }

    @Override
    public String toIrpString() {
        StringBuilder s = new StringBuilder(bitCodes.size()*10);
        s.append("<");
        List<String> list = new ArrayList<>(bitCodes.size() * 20);
        bitCodes.stream().forEach((bitCode) -> {
            list.add(bitCode.toIrpString());
        });

        return s.append(String.join("|", list)).append(">").toString();
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public boolean isEmpty() {
        return bitCodes.isEmpty();
    }

    public int numberOfBitspecDurations() {
        int numberDurations = -1;
        for (BareIrStream bitCode : bitCodes) {
            int n = bitCode.numberOfBareDurations();
            if (numberDurations == -1)
                numberDurations = n;
            else if (numberDurations == n) {
                // ok
            } else {
                return -1;
            }
        }
        return numberDurations;
    }

    /**
     * Checks if the BitSpec is of type &lt;a,-b|c,-d&gt;, for a,b,c,d &gt; 0.
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isStandardPWM(NameEngine nameEngine, GeneralSpec generalSpec) {
        if (bitCodes.size() != 2)
            return false;
        for (BareIrStream bitCode : bitCodes) {
            try {
                // toIrSequence throws exception if not positive, negative
                IrSequence irSequence = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec).toIrSequence();
                if (irSequence.getLength() != 2)
                    return false;
            } catch (IrpException | IncompatibleArgumentException | ArithmeticException ex) {
                return false;
            }
        }
        return true;
    }

   /**
     * Checks if the BitSpec is of type &lt;a,-b|c,-d|e,-f|g,-h&gt;, for a,b,c,d,e,f,g,h &gt; 0.
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isPWM4(NameEngine nameEngine, GeneralSpec generalSpec) {
        if (bitCodes.size() != 4)
            return false;
        for (BareIrStream bitCode : bitCodes) {
            try {
                // toIrSequence throws exception if not positive, negative
                IrSequence irSequence = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec).toIrSequence();
                if (irSequence.getLength() != 2)
                    return false;
            } catch (IrpException | IncompatibleArgumentException | ArithmeticException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the BitSpec is of type &lt;a,-a|-a,a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isStandardBiPhase(NameEngine nameEngine, GeneralSpec generalSpec) {
        if (bitCodes.size() != 2)
            return false;

        Double a = null;
        for (BareIrStream bitCode : bitCodes) {
            try {
                EvaluatedIrStream on = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
                if (on.getLenght() != 2)
                    return false;
                if (a == null)
                    a = on.get(0);
                if (! (IrCoreUtils.approximatelyEquals(a, on.get(0), 1, 0) && IrCoreUtils.approximatelyEquals(-a, on.get(1), 1, 0)))
                    return false;
                a = -a;
            } catch (IrpException | IncompatibleArgumentException | ArithmeticException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the BitSpec is of type &lt;a|-a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @param inverted
     * @return
     */
    public boolean isTrivial(NameEngine nameEngine, GeneralSpec generalSpec, boolean inverted) {
        if (bitCodes.size() != 2)
            return false;
        try {
            EvaluatedIrStream off = bitCodes.get(0).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
            EvaluatedIrStream on = bitCodes.get(1).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
            if (on.getLenght() != 1 || off.getLenght() != 1)
                return false;

            boolean sign = off.get(0) > 0;
            return IrCoreUtils.approximatelyEquals(on.get(0), -off.get(0)) && (sign == inverted);
        } catch (IrpException | IncompatibleArgumentException | ArithmeticException ex) {
            return false;
        }
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("bitspec");
        element.setAttribute("size", Integer.toString(bitCodes.size()));
        element.setAttribute("chunksize", Integer.toString(chunkSize));
        element.setAttribute("standard_pwm", Boolean.toString(isStandardPWM(new NameEngine(), new GeneralSpec())));
        element.setAttribute("standard_biphase", Boolean.toString(isStandardBiPhase(new NameEngine(), new GeneralSpec())));
        element.setAttribute("numberBareDurations", Integer.toString(numberOfBitspecDurations()));
        for (BareIrStream bitCode : bitCodes)
            element.appendChild(bitCode.toElement(document));
        return element;
    }

//    public List<IrStreamItem> evaluate(BitSpec bitSpec) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    boolean interleaveOk(NameEngine nameEngine, GeneralSpec generalSpec) {
        if (isStandardPWM(nameEngine, generalSpec)
                || isPWM4(nameEngine, generalSpec))
            return true;

        for (BareIrStream bareIrStream : bitCodes)
            if (bareIrStream.irStreamItems.size() < 2 || !bareIrStream.interleavingOk(nameEngine, generalSpec))
                return false;

        return true;
    }
}
