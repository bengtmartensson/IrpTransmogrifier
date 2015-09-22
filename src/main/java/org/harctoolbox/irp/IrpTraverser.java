/*
 Copyright (C) 2015 Bengt Martensson.

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

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ModulatedIrSequence;

/**
 *
 */
public class IrpTraverser extends org.harctoolbox.irp.IrpBaseVisitor<Object> {

    private double frequency = ModulatedIrSequence.defaultFrequency;
    private double unit = 1.0; // TODO
    private double unitInPeriods = -1.0;
    private double dutyCycle = -1.0;
    private BitDirection bitDirection = BitDirection.lsb;

    private ParseTreeProperty<Object> values = new ParseTreeProperty<>();
    private GeneralSpec generalSpec = null;
    private ParameterSpecs parameterSpecs = null;
    private IrpParser.Bitspec_irstreamContext topBitspecIrstream = null;

    public void setValue(ParseTree node, Object value) {
        values.put(node, value);
    }

    public Object getValue(ParseTree node) {
        return values.get(node);
    }

    /**
     * @return the generalSpec
     */
    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    /**
     * @return the topBitspecIrstream
     */
    public ParseTree getTopBitspecIrstream() {
        return topBitspecIrstream;
    }

    /**
     * @return the topBitspecIrstream
     */
    public ParameterSpecs getParameterSpecs() {
        return parameterSpecs;
    }

//    @Override
//    public Object visitProtocol(IrpParser.ProtocolContext ctx) {
//        //return visit(ctx.generalspec());
//        return visitChildren(ctx);
//    }

    //@Override
    //public Object visitGeneralspec(IrpParser.GeneralspecContext ctx) {
    //    System.out.println("visitGeneralspec");
    //    return visitChildren(ctx);
    //}
//
//    public Object visitGeneralspec_list(IrpParser.GeneralspecContext ctx) {
//        System.out.println("visitGeneralspec_list");
//        return visitChildren(ctx);
//    }
//
//    @Override
//    public Object visitDutycycle_item(IrpParser.Dutycycle_itemContext ctx) {
//        System.out.println("sdsdf");
//        return visitChildren(ctx);
//    }
//
//    //@Override
//    public Object visitFrequency(IrpParser.Frequency_itemContext ctx) {
//        System.out.println("freq");
//        return visitChildren(ctx);
//    }
//
//    //@Override
//    public Object visitFloat_number(IrpParser.Float_numberContext ctx) {
//        System.out.println("fpnumber");
//        return visitChildren(ctx);
//    }
//

    /**
     * {@inheritDoc}
     *
     * <p>
     * Objecthe default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitProtocol(IrpParser.ProtocolContext ctx) {
        topBitspecIrstream = ctx.bitspec_irstream();
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitGeneralspec(IrpParser.GeneralspecContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitGeneralspec_list(IrpParser.Generalspec_listContext ctx) {
        visitChildren(ctx);
        if (unitInPeriods != -1)
            unit = IrCoreUtils.seconds2microseconds(unitInPeriods/frequency);
        generalSpec = new GeneralSpec(bitDirection, unit/*, unitInPeriods*/, frequency, dutyCycle);
        return generalSpec;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
//    @Override
//    public Object visitGeneralspec_item(IrpParser.Generalspec_itemContext ctx) {
//        return visitChildren(ctx);
//    }
    @Override
    public Object visitFrequency(IrpParser.FrequencyContext ctx) {
        frequency = IrCoreUtils.khz2Hz((Double) visit(ctx.frequency_item()));
        return frequency;//visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitUnit(IrpParser.UnitContext ctx) {
        visit(ctx.unit_item());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitByteorder(IrpParser.ByteorderContext ctx) {
        bitDirection = (BitDirection) visit(ctx.order_item());
        return bitDirection;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitDutycycle(IrpParser.DutycycleContext ctx) {
        dutyCycle = ((Double)visit(ctx.dutycycle_item()))/100.0;
        return dutyCycle;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Double visitFrequency_item(IrpParser.Frequency_itemContext ctx) {
        return (Double) visit(ctx.number_with_decimals());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Double visitDutycycle_item(IrpParser.Dutycycle_itemContext ctx) {
        return (Double) visit(ctx.number_with_decimals());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Double visitUnitInMicroseconds(IrpParser.UnitInMicrosecondsContext ctx) {
        unit = (Double) visit(ctx.number_with_decimals());
        return unit;
    }

    @Override
    public Object visitUnitInPeriods(IrpParser.UnitInPeriodsContext ctx) {
        unitInPeriods = (Double) visit(ctx.number_with_decimals());
        return unitInPeriods;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitOrderMSB(IrpParser.OrderMSBContext ctx) {
        bitDirection = BitDirection.msb;
        return bitDirection;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitOrderLSB(IrpParser.OrderLSBContext ctx) {
        bitDirection = BitDirection.lsb;
        return bitDirection;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitDuration(IrpParser.DurationContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitFlash_duration(IrpParser.Flash_durationContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitGap_duration(IrpParser.Gap_durationContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitName_or_number(IrpParser.Name_or_numberContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitExtent(IrpParser.ExtentContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitBitfield(IrpParser.BitfieldContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitPrimary_item(IrpParser.Primary_itemContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitIrstream(IrpParser.IrstreamContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitBare_irstream(IrpParser.Bare_irstreamContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitIrstream_item(IrpParser.Irstream_itemContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitBitspec(IrpParser.BitspecContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitRepeat_marker(IrpParser.Repeat_markerContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitBitspec_irstream(IrpParser.Bitspec_irstreamContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitExpression(IrpParser.ExpressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitBare_expression(IrpParser.Bare_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitInclusive_or_expression(IrpParser.Inclusive_or_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitExclusive_or_expression(IrpParser.Exclusive_or_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitAnd_expression(IrpParser.And_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitAdditive_expression(IrpParser.Additive_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitMultiplicative_expression(IrpParser.Multiplicative_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitExponential_expression(IrpParser.Exponential_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitUnary_expression(IrpParser.Unary_expressionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitDefinitions(IrpParser.DefinitionsContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitDefinitions_list(IrpParser.Definitions_listContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitDefinition(IrpParser.DefinitionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitAssignment(IrpParser.AssignmentContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitVariation(IrpParser.VariationContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitAlternative(IrpParser.AlternativeContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitNumber(IrpParser.NumberContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
//    @Override
//    public Object visitNumber_with_decimals(IrpParser.Number_with_decimalsContext ctx) {
//        return visitChildren(ctx);
//    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Double visitIntegerAsFloat(IrpParser.IntegerAsFloatContext ctx) {
        return (double) parseINT(ctx.INT());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Double visitFloat(IrpParser.FloatContext ctx) {
        return (Double) visit(ctx.float_number());
        //return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitName(IrpParser.NameContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitParameter_specs(IrpParser.Parameter_specsContext ctx) {
        parameterSpecs = new ParameterSpecs(ctx);
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    //@Override
    //public Object visitParameter_spec(IrpParser.Parameter_specContext ctx) {
    //    return visitChildren(ctx);
    //}

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    //@Override
    //public Object visitFloat_number(IrpParser.Float_numberContext ctx) {
    //    return visitChildren(ctx);
    //}

    @Override
    public Object visitDotInt(IrpParser.DotIntContext ctx) {
        Double x = parseFloat(ctx.INT());
        setValue(ctx, x);
        return x;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Object visitIntDotInt(IrpParser.IntDotIntContext ctx) {
        Double x = parseFloat(ctx.INT(0), ctx.INT(1));
        setValue(ctx, x);
        return x;
        //Double x = String s0 = ctx.INT(0);// .getText();
        //String s1 = ctx.INT(1).getText();
        //return visitChildren(ctx);
    }

    private long parseINT(TerminalNode n) {
        return Long.parseLong(n.getText());
    }

    private double parseFloat(TerminalNode integ, TerminalNode matissa) {
        return Double.parseDouble(integ.getText() + "." + matissa.getText());
    }

    private double parseFloat(TerminalNode matissa) {
        return Double.parseDouble("0." + matissa.getText());
    }
}
