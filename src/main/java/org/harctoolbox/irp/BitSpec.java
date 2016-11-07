/*
Copyright (C) 2016 Bengt Martensson.

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
    // Treat n = 0 and n = 1 differently, since coding on a zero or one-letter alphaber is ... special.
    private static int computeNoBits(int n) {
        if (n == 0)
            return 0;
        if (n == 1)
            return 1;
        int x = n-1;
        int m;
        for (m = 0; x != 0; m++)
            x >>= 1;
        return m;
    }

    private static List<BareIrStream> parse(List<IrpParser.Bare_irstreamContext> list) throws IrpSyntaxException, InvalidRepeatException {
        List<BareIrStream> result = new ArrayList<>(list.size());
        for (IrpParser.Bare_irstreamContext bareIrStreamCtx : list)
            result.add(new BareIrStream(bareIrStreamCtx));

        return result;
    }

    // Number of bits encoded
    private int chunkSize;

    private List<BareIrStream> bitCodes;

    public BitSpec(String str) throws IrpSyntaxException, InvalidRepeatException {
        this(new ParserDriver(str).getParser().bitspec());
    }

    public BitSpec(IrpParser.BitspecContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(parse(ctx.bare_irstream()));
    }


    public BitSpec(List<BareIrStream> list) throws IrpSyntaxException, InvalidRepeatException {
        chunkSize = computeNoBits(list.size());
        bitCodes = list;
    }

    public BitSpec() {
        chunkSize = 0;
        bitCodes = new ArrayList<>(2);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSpec))
            return false;

        BitSpec other = (BitSpec) obj;
        if (chunkSize != other.getChunkSize())
            return false;

        if (bitCodes.size() != other.bitCodes.size())
            return false;

        for (int i = 0; i < bitCodes.size(); i++)
            if (!bitCodes.get(i).equals(other.bitCodes.get(i)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.chunkSize;
        hash = 53 * hash + Objects.hashCode(this.bitCodes);
        return hash;
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

    public BareIrStream get(int index) throws IncompatibleArgumentException {
        if (index >= bitCodes.size())
            throw new IncompatibleArgumentException("Cannot encode " + index + " with current bitspec.");
        return bitCodes.get(index);
    }

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
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isPWM(NameEngine nameEngine, GeneralSpec generalSpec) {
        return isPWM(2, nameEngine, generalSpec);
    }

    /**
     * @param length
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isPWM(int length, NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitCodes.size() == length && isTwoLengthGapFlash(nameEngine, generalSpec);
    }

    private boolean isTwoLengthGapFlash(NameEngine nameEngine, GeneralSpec generalSpec) {
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
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("size", Integer.toString(bitCodes.size()));
        element.setAttribute("chunkSize", Integer.toString(chunkSize));
        element.setAttribute("bitMask", Integer.toString(IrCoreUtils.ones(chunkSize)));
        element.setAttribute("standardPwm", Boolean.toString(isPWM(2, new NameEngine(), new GeneralSpec())));
        element.setAttribute("standardBiPhase", Boolean.toString(isStandardBiPhase(new NameEngine(), new GeneralSpec())));
        //element.setAttribute("numberBareDurations", Integer.toString(numberOfBitspecDurations()));
        for (BareIrStream bitCode : bitCodes)
            element.appendChild(bitCode.toElement(document));
        return element;
    }

    boolean interleaveOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        if (!gapFlashBitSpecs && (isPWM(2, nameEngine, generalSpec)
                || isPWM(4, nameEngine, generalSpec)))
            return true;

        return bitCodes.stream().noneMatch((bareIrStream) -> (bareIrStream.getIrStreamItems().size() < 2 || !bareIrStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitSpecs)));
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = bitCodes.stream().map((bitCode) -> bitCode.weight()).reduce(weight, Integer::sum);
        return weight;
    }

    public boolean hasExtent() {
        return bitCodes.stream().anyMatch((bitCode) -> (bitCode.hasExtent()));
    }

    public String code(CodeGenerator codeGenerator) {
        ItemCodeGenerator st = codeGenerator.newItemCodeGenerator(this);
        st.addAttribute("arg1", code(false, codeGenerator));
        st.addAttribute("arg2", code(true, codeGenerator));
/*
s1 = code(false, generalSpec, pass, codeGenerator);
        codeGenerator.addLine(code(true, generalSpec, pass, codeGenerator));

        String normalBeginTemplateName  = generalSpec.getBitDirection() == BitDirection.lsb ? "BitSpecLsbBegin" : "BitSpecMsbBegin";
        String reverseBeginTemplateName = generalSpec.getBitDirection() == BitDirection.msb ? "BitSpecLsbBegin" : "BitSpecMsbBegin";
        String normalEndTemplateName    = generalSpec.getBitDirection() == BitDirection.lsb ? "BitSpecLsbEnd"   : "BitSpecMsbEnd";
        String reverseEndTemplateName   = generalSpec.getBitDirection() == BitDirection.msb ? "BitSpecLsbEnd"   : "BitSpecMsbEnd";

        codeGenerator.newItemCodeGenerator("BitSpecBegin");

        ItemCodeGenerator normalBeginTemplate  = codeGenerator.newItemCodeGenerator(normalBeginTemplateName);
        ItemCodeGenerator reverseBeginTemplate = codeGenerator.newItemCodeGenerator(reverseBeginTemplateName);
        ItemCodeGenerator normalEndTemplate    = codeGenerator.newItemCodeGenerator(normalEndTemplateName);
        ItemCodeGenerator reverseEndTemplate   = codeGenerator.newItemCodeGenerator(reverseEndTemplateName);
*/
        return st.render();
    }

    private String code(boolean reverse, CodeGenerator codeGenerator) {
        List<String> list = new ArrayList<>(bitCodes.size());
        for (int i = 0; i < bitCodes.size(); i++) {
            BareIrStream bitCode = bitCodes.get(i);
            ItemCodeGenerator bitSpecCaseTemplate = codeGenerator.newItemCodeGenerator("BitSpecCase");
            bitSpecCaseTemplate.addAttribute("number", i);
            bitSpecCaseTemplate.addAttribute("code", bitCode.code(null, null, codeGenerator));
            list.add(bitSpecCaseTemplate.render());
        }

        String normalStr  = reverse == (codeGenerator.getGeneralSpec().getBitDirection() == BitDirection.msb) ? "Lsb" : "Msb";
        ItemCodeGenerator st = codeGenerator.newItemCodeGenerator("BitSpec" + normalStr);
        st.addAttribute("chunkSize", chunkSize);
        st.addAttribute("bitmask", IrCoreUtils.ones(chunkSize));
        st.addAttribute("body", list);

        ItemCodeGenerator bitSpecTemplate  = codeGenerator.newItemCodeGenerator(reverse ? "ReverseBitSpec" : "NormalBitSpec");
        bitSpecTemplate.addAttribute("body", st.render());
        return bitSpecTemplate.render();
    }
}
