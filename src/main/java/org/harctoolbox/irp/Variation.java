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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Variation extends IrpObject implements IrStreamItem {
    private static int numberOfInfiniteRepeats(BareIrStream bareIrStream) {
        return bareIrStream == null ? 0 : bareIrStream.numberOfInfiniteRepeats();
    }

    private final BareIrStream intro;
    private final BareIrStream repeat;
    private final BareIrStream ending;

    public Variation(String str) {
        this(new ParserDriver(str));
    }

    public Variation(ParserDriver parserDriver) {
        this(parserDriver.getParser().variation());
    }

    public Variation(IrpParser.VariationContext variation) {
        super(variation);
        intro = new BareIrStream(variation.alternative(0).bare_irstream());
        repeat = new BareIrStream(variation.alternative(1).bare_irstream());
        ending = variation.alternative().size() > 2 ? new BareIrStream(variation.alternative(2).bare_irstream()) : new BareIrStream();
    }

    public Variation(BareIrStream intro, BareIrStream repeat, BareIrStream ending) {
        super(null);
        this.intro = intro;
        this.repeat = repeat;
        this.ending = ending;
    }

    @Override
    public IrStreamItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return new Variation(intro.substituteConstantVariables(constantVariables), repeat.substituteConstantVariables(constantVariables), ending.substituteConstantVariables(constantVariables));
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) {
        return intro.isEmpty(nameEngine) && repeat.isEmpty(nameEngine) && (ending == null || ending.isEmpty(nameEngine));
    }

    public BareIrStream select(IrSignal.Pass pass) {
        BareIrStream result;
        switch (pass) {
            case intro:
                result = intro;
                break;
            case repeat:
                result = repeat;
                break;
            case ending:
                result = ending;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return result;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return Math.max(numberOfInfiniteRepeats(intro), Math.max(numberOfInfiniteRepeats(repeat), numberOfInfiniteRepeats(ending)));
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.appendChild(intro.toElement(document));
        element.appendChild(repeat.toElement(document));
        element.appendChild(ending.toElement(document));
        return element;
    }

    @Override
    public Integer numberOfBareDurations() {
        return null;
    }

    @Override
    public Integer numberOfBits() {
        try {
            return intro.numberOfBits() + repeat.numberOfBits() + ending.numberOfBits();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public String toIrpString(int radix) {
        StringBuilder str = new StringBuilder(50);
        str.append("[").append(intro.toIrpString(radix)).append("]");
        str.append("[").append(repeat.toIrpString(radix)).append("]");
        if (ending != null && !ending.isEmpty(null))
            str.append("[").append(ending.toIrpString(radix)).append("]");
        return str.toString();
    }

//    @Override
//    public ParserRuleContext getParseTree() {
//        return parseTree;
//    }

    @Override
    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecs, boolean isLast) {
        throw new ThisCannotHappenException("decode cannot be called on a protocol with variation.");
    }

    @Override
    public BareIrStream extractPass(Pass pass, IrStream.PassExtractorState state) {
        return select(state.getState()).extractPass(pass, state);
    }

    @Override
    public void updateStateWhenEntering(Pass pass, IrStream.PassExtractorState state) {
        if (select(state.getState()).isEmpty())
            state.setState(Pass.finish);
    }

    @Override
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.interleavingOk(intro, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(intro, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(intro, last, gapFlashBitSpecs);
    }

    @Override
    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs) {
         return BareIrStream.interleavingOk(toCheck, intro, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(toCheck, intro, last, gapFlashBitSpecs)
                && BareIrStream.interleavingOk(toCheck, intro, last, gapFlashBitSpecs);
    }

    @Override
    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.endingDurationType(intro, last, gapFlashBitSpecs)
                .combine(BareIrStream.endingDurationType(repeat, last, gapFlashBitSpecs))
                .combine(BareIrStream.endingDurationType(ending, last, gapFlashBitSpecs));
    }

    @Override
    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs) {
        return BareIrStream.startingDurationType(intro, last, gapFlashBitSpecs)
                .combine(BareIrStream.startingDurationType(repeat, last, gapFlashBitSpecs))
                .combine(BareIrStream.startingDurationType(ending, last, gapFlashBitSpecs));
    }

    @Override
    public int weight() {
        return intro.weight() + repeat.weight() + ending.weight();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Variation))
            return false;

        Variation other = (Variation) obj;
        return intro.equals(other.intro)
                && repeat.equals(other.repeat)
                && ending.equals(other.ending);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.intro);
        hash = 61 * hash + Objects.hashCode(this.repeat);
        hash = 61 * hash + Objects.hashCode(this.ending);
        return hash;
    }

    @Override
    public boolean hasExtent() {
        return intro.hasExtent() || repeat.hasExtent() || ending.hasExtent();
    }

    public boolean hasPart(Pass part) {
        if (part == null)
            return true;
        return ! select(part).isEmpty();
    }

    @Override
    public Set<String> assignmentVariables() {
        Set<String> list = intro.assignmentVariables();
        list.addAll(repeat.assignmentVariables());
        list.addAll(ending.assignmentVariables());
        return list;
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine) {
        return null;
    }

    @Override
    public Integer numberOfDurations() {
        return null;
    }

    @Override
    public void render(RenderData renderData, List<BitSpec> bitSpecs) {
        throw new ThisCannotHappenException();
    }

    @Override
    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) {
        throw new ThisCannotHappenException();
    }

    public boolean startsWithFlash() {
        return (!intro.isEmpty() ? intro : repeat).startsWithFlash();
    }

    @Override
    public boolean nonConstantBitFieldLength() {
        return intro.nonConstantBitFieldLength() || repeat.nonConstantBitFieldLength() || ending.nonConstantBitFieldLength();
    }

    @Override
    public Integer guessParameterLength(String name) {
        Integer result = this.intro.guessParameterLength(name);
        if (result != null)
            return result;
        result = repeat.guessParameterLength(name);
        if (result != null)
            return result;
        return ending.guessParameterLength(name);
    }

    @Override
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine) {
        TreeSet<Double> result = new TreeSet<>();
        for (Pass pass : IrSignal.Pass.values()) {
            if (pass == Pass.finish)
                continue;
            TreeSet<Double> durations = select(pass).allDurationsInMicros(generalSpec, nameEngine);
            result.addAll(durations);
        }
        return result;
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return intro.constant(nameEngine) && repeat.constant(nameEngine) && ending.constant(nameEngine);
    }

    @Override
    public void createParameterSpecs(ParameterSpecs parameterSpecs) throws InvalidNameException {
        intro.createParameterSpecs(parameterSpecs);
        repeat.createParameterSpecs(parameterSpecs);
        ending.createParameterSpecs(parameterSpecs);
    }
}
