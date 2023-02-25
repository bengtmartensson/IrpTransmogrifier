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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements BitSpecs, as described in Chapter 7.
 *
 */
public final class BitSpec extends IrpObject implements AggregateLister {

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

    private static List<BareIrStream> parse(List<IrpParser.Bare_irstreamContext> list) {
        List<BareIrStream> result = new ArrayList<>(list.size());
        list.stream().forEach((bareIrStreamCtx) -> {
            result.add(new BareIrStream(bareIrStreamCtx));
        });

        return result;
    }

    // Number of bits encoded
    private int chunkSize;

    private List<BareIrStream> bitCodes;

    public BitSpec(String str) {
        this(new ParserDriver(str).getParser().bitspec());
    }

    public BitSpec(IrpParser.BitspecContext ctx) {
        super(ctx);
        bitCodes = parse(ctx.bare_irstream());
        chunkSize = computeNoBits(bitCodes.size());
    }

    public BitSpec(List<BareIrStream> list) throws NonUniqueBitCodeException {
        this();
        if (IrCoreUtils.hasDuplicatedElements(list))
            throw new NonUniqueBitCodeException();
        chunkSize = computeNoBits(list.size());
        bitCodes = list;
    }

    public BitSpec() {
        super(null);
        chunkSize = 0;
        bitCodes = new ArrayList<>(0);
    }

    BitSpec substituteConstantVariables(Map<String, Long> constantVariables) {
        try {
            List<BareIrStream> list = new ArrayList<>(bitCodes.size());
            bitCodes.forEach((bitCode) -> {
                list.add(bitCode.substituteConstantVariables(constantVariables));
            });
            return new BitSpec(list);
        } catch (NonUniqueBitCodeException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    public Integer numberOfDurations() {
        int result = 0;
        for (BareIrStream bitCode : bitCodes) {
            Integer curr = bitCode.numberOfDurations();
            if (curr == null)
                return null;
            if (curr > result)
                result = curr;
        }
        return result;
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

    public BareIrStream get(int index) {
        if (index >= bitCodes.size())
            throw new ThisCannotHappenException("Cannot encode " + index + " with current bitspec.");
        return bitCodes.get(index);
    }

    @Override
    public String toIrpString(int radix) {
        StringBuilder s = new StringBuilder(bitCodes.size()*10);
        s.append("<");
        List<String> list = new ArrayList<>(bitCodes.size() * 20);
        bitCodes.stream().forEach((bitCode) -> {
            list.add(bitCode.toIrpString(radix));
        });

        return s.append(String.join("|", list)).append(">").toString();
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public boolean isEmpty() {
        return bitCodes.isEmpty();
    }

    public Integer numberOfBitspecDurations() {
        Integer numberDurations = null;
        for (BareIrStream bitCode : bitCodes) {
            int n = bitCode.numberOfBareDurations(true);
            if (numberDurations == null)
                numberDurations = n;
            else if (numberDurations == n) {
                // ok
            } else {
                return null;
            }
        }
        return numberDurations;
    }

    public boolean isPWM() {
        return isPWM(2);
    }

    public boolean isPWM(int length) {
        return bitCodes.size() == length && isTwoLengthFlashGap();
    }

    private boolean isTwoLengthFlashGap() {
        return bitCodes.stream().allMatch((bitCode) -> isTwoLengthFlashGap(bitCode));
    }

    private boolean isTwoLengthFlashGap(BareIrStream bitCode) {
        List<Duration> durations = bitCode.getDurations();
        return durations.size() == 2 && (durations.get(0) instanceof Flash) && (durations.get(1) instanceof Gap);
    }

    boolean isSonyType(GeneralSpec generalSpec, NameEngine nameEngine) {
        return isPWM()
                && ! IrCoreUtils.approximatelyEquals(bitCodes.get(0).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine),
                        bitCodes.get(1).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
    }

    /**
     * Checks if the BitSpec is of type &lt;a,-a|-a,a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isStandardBiPhase(GeneralSpec generalSpec, NameEngine nameEngine) {
        if (bitCodes.size() != 2)
            return false;

        Double a = null;
        for (BareIrStream bitCode : bitCodes) {
            try {
                EvaluatedIrStream on = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine);
                if (on.getLength() != 2)
                    return false;
                if (a == null)
                    a = on.get(0);
                if (! (IrCoreUtils.approximatelyEquals(a, on.get(0), 1, 0) && IrCoreUtils.approximatelyEquals(-a, on.get(1), 1, 0)))
                    return false;
                a = -a;
            } catch (NumberFormatException | NameUnassignedException | IrpInvalidArgumentException ex) {
                return false;
            }
        }
        return true;
    }

   /**
     * Checks if the BitSpec is of type &lt;a|-a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @param inverted If true then a &gt; 0, i.e., starts with a flash.
     * @return
     */
    public boolean isTrivial(GeneralSpec generalSpec, NameEngine nameEngine, boolean inverted) {
       if (bitCodes.size() != 2)
            return false;
        try {
            EvaluatedIrStream off = bitCodes.get(0).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine);
            EvaluatedIrStream on = bitCodes.get(1).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine);
            if (on.getLength() != 1 || off.getLength() != 1)
                return false;

            boolean sign = off.get(0) > 0;
            return IrCoreUtils.approximatelyEquals(on.get(0), -off.get(0)) && (sign == inverted);
        } catch (IrpInvalidArgumentException | NameUnassignedException ex) {
            return false;
        }
    }

    /**
     * Checks if the BitSpec is of type &lt;a|-a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isTrivial(GeneralSpec generalSpec, NameEngine nameEngine) {
        return isTrivial(generalSpec, nameEngine, true) || isTrivial(generalSpec, nameEngine, false);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("size", Integer.toString(bitCodes.size()));
        element.setAttribute("chunkSize", Integer.toString(chunkSize));
        element.setAttribute("bitMask", Long.toString(IrCoreUtils.ones(chunkSize)));
        element.setAttribute("pwm2", Boolean.toString(isPWM(2)));
        element.setAttribute("standardBiPhase", Boolean.toString(isStandardBiPhase(new GeneralSpec(), new NameEngine())));
        Integer nod = numberOfDurations();
        if (nod != null)
            element.setAttribute("numberOfDurations", Integer.toString(nod));
        bitCodes.forEach((bitCode) -> {
            element.appendChild(bitCode.toElement(document));
        });
        return element;
    }

    boolean interleaveOk(DurationType last, boolean gapFlashBitSpecs) {
        if (!gapFlashBitSpecs && (isPWM(2) || isPWM(4)))
            return true;

        return bitCodes.stream().noneMatch((bareIrStream) -> (bareIrStream.getIrStreamItems().size() < 2 || !bareIrStream.interleavingOk(last, gapFlashBitSpecs)));
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

    Set<String> assignmentVariables() {
        Set<String> list = new HashSet<>(1);
        bitCodes.stream().forEach((bitCode) -> {
            list.addAll(bitCode.assignmentVariables());
        });
        return list;
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(17);
        if (chunkSize > 1)
            map.put("chunkSize", chunkSize);
        map.put("bitMask", IrCoreUtils.ones(chunkSize));
        map.put("size", bitCodes.size());
        if (isPWM(2)) {
            map.put("pwm2", true);
            map.put("zeroGap",   bitCodes.get(0).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("zeroFlash", bitCodes.get(0).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
            map.put("oneGap",    bitCodes.get(1).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("oneFlash",  bitCodes.get(1).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
            map.put("flashesDiffer", !IrCoreUtils.approximatelyEquals(
                    bitCodes.get(0).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine),
                    bitCodes.get(0).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine)));
        }
        if (isPWM(4)) {
            map.put("pwm4", true);
            map.put("zeroGap",   bitCodes.get(0).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("zeroFlash", bitCodes.get(0).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
            map.put("oneGap",    bitCodes.get(1).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("oneFlash",  bitCodes.get(1).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
            map.put("twoGap",    bitCodes.get(2).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("twoFlash",  bitCodes.get(2).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
            map.put("threeGap",  bitCodes.get(3).getIrStreamItems().get(1).microSeconds(generalSpec, nameEngine));
            map.put("threeFlash",bitCodes.get(3).getIrStreamItems().get(0).microSeconds(generalSpec, nameEngine));
        }
        if (isStandardBiPhase(new GeneralSpec(), new NameEngine())) {
            map.put("standardBiPhase", true);
            map.put("biPhaseHalfPeriod", averageDuration(generalSpec, nameEngine));
            map.put("biPhaseInverted", bitCodes.get(0).getIrStreamItems().get(0) instanceof Flash);
        }
        map.put("lsbFirst", generalSpec.getBitDirection() == BitDirection.lsb);
        if (numberOfDurations() != null)
            map.put("numberOfDurations", Integer.toString(numberOfDurations()));
        List<Map<String, Object>> list = new ArrayList<>(bitCodes.size());
        bitCodes.stream().map((bitCode) -> bitCode.propertiesMap(generalSpec, nameEngine)).forEach(list::add);
        map.put("list", list);
        return map;
    }

    double averageDuration(GeneralSpec generalSpec, NameEngine nameEngine) {
        double sum = 0;
        sum = bitCodes.stream().map((bitCode) -> bitCode.averageDuration(generalSpec, nameEngine)).reduce(sum, (accumulator, _item) -> accumulator + _item);
        return sum / bitCodes.size();
    }

    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        TreeSet<Double> result = new TreeSet<>();
        bitCodes.forEach((BareIrStream bitCode) -> {
            result.addAll(bitCode.allDurationsInMicros(generalSpec, nameEngine));
        });
        if (result.size() == 1) {
            if (isStandardBiPhase(generalSpec, nameEngine))
                result.add(2*result.first());
            else if (isTrivial(generalSpec, nameEngine))
                result.add(0d);
        }

        return result;
    }

    public boolean constant(NameEngine nameEngine) {
        return bitCodes.stream().noneMatch((bitCode) -> (!bitCode.constant(nameEngine)));
    }

    @Override
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
        for (BareIrStream bitCode: bitCodes)
            bitCode.createParameterSpecs(parameterSpecs);
    }

    public static class IncompatibleBitSpecException extends RuntimeException {

        IncompatibleBitSpecException() {
            super();
        }

        IncompatibleBitSpecException(BitSpec bitSpec) {
            super("Incompatible BitSpec: " + bitSpec);
        }

        public IncompatibleBitSpecException(Throwable ex) {
            super(ex);
        }
    }
}
