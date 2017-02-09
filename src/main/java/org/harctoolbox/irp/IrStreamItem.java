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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrSignal;

/**
 * This class is an abstract superclass of the things that make up an IRStream (see "Directly known subclasses").
 *
 * This should rather be an interface.
 */
public interface IrStreamItem extends XmlExport {

    public static IrStreamItem newIrStreamItem(String str) {
        return newIrStreamItem((new ParserDriver(str)).getParser().irstream_item());
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

    public boolean isEmpty(NameEngine nameEngine) throws UnassignedException, IrpSemanticException;

    /**
     *
     * @param nameEngine nameengine, or null
     * @param generalSpec generalspec, or null
     * @param last
     * @param gapFlashBitSpecs
     * @return
     */
    public abstract boolean interleavingOk(GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs);

    public abstract boolean interleavingOk(DurationType toCheck, GeneralSpec generalSpec, NameEngine nameEngine, DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs);

    public abstract Integer numberOfBits();

    public abstract Integer numberOfBareDurations(boolean recursive);

    /**
     * Upper limit of the number of (interleaving) durations in the rendered signal.
     * @param pass
     * @return
     */
    public abstract Integer numberOfDurations(IrSignal.Pass pass);

    public abstract Integer numberOfBitSpecs();

    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass);

    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass);

    abstract ParserRuleContext getParseTree();

    public abstract void recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, InvalidNameException, UnassignedException, IrpSemanticException;

    void prerender(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs);

    public abstract void render(RenderData renderData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException, IrpSemanticException, NameConflictException, IrpSignalParseException;

    public abstract void render(RenderData renderData, List<BitSpec> bitSpecs) throws UnassignedException, InvalidNameException, IrpSemanticException, NameConflictException, IrpSignalParseException;

    //public abstract void render(RenderData renderData, List<BitSpec> bitSpecs) throws InvalidNameException, UnassignedException;

    public abstract void traverse(Traverser recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, IrpSemanticException, InvalidNameException, UnassignedException, NameConflictException;

    //public abstract void propertiesMap(PropertiesMapData propertiesMapData, GeneralSpec generalSpec);

    public abstract boolean hasExtent();

    public abstract Set<String> assignmentVariables();

    public abstract Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine);

    public Map<String, Object> propertiesMap(int noProperites);
//    {
//        return IrpUtils.propertiesMap(noProperites, this);
//    }

    public Double microSeconds(GeneralSpec generalSpec, NameEngine nameEngine);

    public String toIrpString();

    public int numberOfInfiniteRepeats();

    public int weight();

    public List<IrStreamItem> extractPass(IrSignal.Pass pass, IrSignal.Pass state);
}
