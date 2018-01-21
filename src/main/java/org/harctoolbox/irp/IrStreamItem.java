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
import java.util.Set;
import java.util.TreeSet;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrSignal;

/**
 * This interface describes the things that make up an IRStream.
 */
public interface IrStreamItem extends XmlExport {

    public static IrStreamItem newIrStreamItem(String str) {
        return newIrStreamItem(new ParserDriver(str));
    }

    public static IrStreamItem newIrStreamItem(ParserDriver parserDriver) {
        return newIrStreamItem(parserDriver.getParser().irstream_item());
    }

    public static IrStreamItem newIrStreamItem(IrpParser.Irstream_itemContext ctx) {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.VariationContext) ? new Variation(((IrpParser.VariationContext) child))
                : (child instanceof IrpParser.Finite_bitfieldContext) ? FiniteBitField.newFiniteBitField((IrpParser.Finite_bitfieldContext) child)
                : (child instanceof IrpParser.AssignmentContext) ? new Assignment((IrpParser.AssignmentContext) child)
                //: (child instanceof IrpParser.ExtentContext) ? new Extent((IrpParser.ExtentContext) child)
                : (child instanceof IrpParser.DurationContext) ? Duration.newDuration((IrpParser.DurationContext) child)
                : (child instanceof IrpParser.IrstreamContext) ? new IrStream((IrpParser.IrstreamContext) child)
                : (child instanceof IrpParser.Bitspec_irstreamContext) ? new BitspecIrstream((IrpParser.Bitspec_irstreamContext) child)
                : null;
    }

    public boolean isEmpty(NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException;

    /**
     *
     * @param last
     * @param gapFlashBitSpecs
     * @return
     */
    public boolean interleavingOk(DurationType last, boolean gapFlashBitSpecs);

    public boolean interleavingOk(DurationType toCheck, DurationType last, boolean gapFlashBitSpecs);

    public DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs);

    public DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs);

    /**
     * Computes the number of encoded bits, which may differ from the number of payload bits,
     * in the case of redundancy.
     * @return
     */
    public Integer numberOfBits();

    public Integer numberOfBareDurations(boolean recursive);

    /**
     * Upper limit of the number of (interleaving) durations in the rendered signal.
     * Only intended to be used on intro/repeat/ending IrStreams.
     * @return number of durations (not necessarily interleaving), or null if not possible to determine.
     */
    public Integer numberOfDurations();

    public Integer numberOfBitSpecs();

    /**
     * Delivers a set of all the contained durations in microseconds.
     * Dumb, in that it does not consider consecutive flashes or consecutive gaps.
     * Also, durations that cannot be statically computed may be omitted,
     * as well as duration arising from extents.
     * @param generalSpec
     * @param nameEngine
     * @return
     */
    public TreeSet<Double> allDurationsInMicros(GeneralSpec generalSpec, NameEngine nameEngine);

    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass);

    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass);

    //ParserRuleContext getParseTree();

    public void render(RenderData renderData, List<BitSpec> bitSpecs) throws NameUnassignedException;

    public BitwiseParameter renderAsOneParameter(NameEngine nameEngine, BitDirection bitDirection, List<BitSpec> bitSpecs) throws NameUnassignedException;

    public boolean hasExtent();

    public Set<String> assignmentVariables();

    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine);

    public Map<String, Object> propertiesMap(int noProperites);

    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine);

    public String toIrpString(int radix);

    public int numberOfInfiniteRepeats();

    public int weight();

    public List<IrStreamItem> extractPass(IrSignal.Pass pass, IrSignal.Pass state);

    public void evaluate(RenderData renderData, List<BitSpec> bitSpecStack) throws NameUnassignedException;

    public void decode(RecognizeData recognizeData, List<BitSpec> bitSpecStack) throws SignalRecognitionException;

    public void decode(RecognizeOneParameterData recognizeOneParameterData, List<BitSpec> bitSpecStack);

    public boolean nonConstantBitFieldLength();

    /**
     * Tries to infer the bit-length of the parameter with name as the argument.
     * @param name Name of parameter sought.
     * @return length if success, null if failure.
     */
    public Integer guessParameterLength(String name);

    /**
     * Returns an IrStreamItem of the same type with all occurrences of the
     * variables in the dictionary replaced by their values in the dictionary.
     * Does not change the containing object. May return the object itself,
     * or share sub-objects with it.
     * @param constantVariables Map<String, Long> of variables to replace.
     * @return IrStreamItem of the same type.
     */
    public IrStreamItem substituteConstantVariables(Map<String, Long> constantVariables);
}
