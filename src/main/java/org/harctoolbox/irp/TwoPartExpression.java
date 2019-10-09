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

import java.util.Map;
import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class TwoPartExpression extends Expression {

    public static Expression newExpression(IrpParser.ExpressionContext ctx) {
        return newExpression(ctx, ctx.getChild(0).getText().charAt(0), ctx.expression(0));
    }

    public static Expression newExpression(ParseTree ctx, char operator, IrpParser.ExpressionContext expression) {
        return new TwoPartExpression(ctx, operator, expression);
    }

    public static Expression newExpression(ParseTree ctx, ParseTree first, ParseTree second) {
        return new TwoPartExpression(ctx, first, second);
    }

    private final char operator;
    private final Expression operand;

    private TwoPartExpression(ParseTree ctx, char operator, IrpParser.ExpressionContext operand) {
        this(ctx, operator, Expression.newExpression(operand));
    }

    private TwoPartExpression(ParseTree ctx, ParseTree first, ParseTree second) {
        this(ctx, first.getText().charAt(0), Expression.newExpression((IrpParser.ExpressionContext) second));
    }

    private TwoPartExpression(ParseTree ctx, char operator, Expression operand) {
        super(ctx);
        this.operator = operator;
        this.operand = operand;
    }

    private TwoPartExpression(char operator, PrimaryItem primaryItem) {
        this(null, operator, PrimaryItemExpression.newExpression(primaryItem));
    }

    @Override
    public PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return new TwoPartExpression(operator, operand.substituteConstantVariables(constantVariables));
    }

    @Override
    public String toIrpString(int radix) {
        return "(" + operator + operand.toIrpString(radix) + ")";
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(4);
        String kind = operator == '~' ? "BitInvert"
                : operator == '!' ? "Negate"
                : operator == '-' ? "UnaryMinus"
                : operator == '#' ? "BitCount"
                : null;
        map.put("kind", kind);
        Map<String, Object> arg = operand.propertiesMap(true, generalSpec, nameEngine);
        map.put("arg", arg);
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.operator;
        hash = 37 * hash + Objects.hashCode(this.operand);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TwoPartExpression other = (TwoPartExpression) obj;
        if (this.operator != other.operator)
            return false;
        return Objects.equals(this.operand, other.operand);
    }

    @Override
    public int weight() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long toLong(NameEngine nameEngine) throws NameUnassignedException {
        long op = operand.toLong(nameEngine);

        switch (operator) {
            case '!':
                return op == 0L ? 1L : 0L;
            case '#':
                return Long.bitCount(op);
            case '-':
                return -op;
            case '~':
                return ~op;
            default:
                throw new ThisCannotHappenException("Unknown operator: " + operator);
        }
    }

    @Override
    public BitwiseParameter toBitwiseParameter(RecognizeData recognizeData) {
        BitwiseParameter op = operand.toBitwiseParameter(recognizeData);
        if (op == null)
            return BitwiseParameter.NULL;

        switch (operator) {
            case '!':
                return op.negation();
            case '#':
                return op.bitCount();
            case '-':
                return op.minus();
            case '~':
                return op.bitInvert();
            default:
                throw new ThisCannotHappenException("Unknown operator: " + operator);
        }
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        Element e = document.createElement("UnaryOperator");
        e.setAttribute("kind", "" + operator);
        el.appendChild(e);
        e.appendChild(operand.toElement(document));
        return el;
    }

    @Override
    public BitwiseParameter invert(BitwiseParameter rhs, RecognizeData nameEngine) {
        switch (operator) {
            case '~':
                return rhs.bitInvert();
            case '-':
                return rhs.minus();
            default:
                return null;
        }
    }

    @Override
    public PrimaryItem leftHandSide() {
        return operand;
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return operand.constant(nameEngine);
    }
}
