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
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class ThreePartExpression extends Expression {

    public static Expression newExpression(IrpParser.ExpressionContext ctx) {
        return new ThreePartExpression(ctx, ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
    }

    public static Expression newExpression(ParseTree ctx, ParseTree first, ParseTree second, ParseTree third) {
        return new ThreePartExpression(ctx, first, second, third);
    }

    private final String operator;
    private final Expression op1;
    private final Expression op2;

    private ThreePartExpression(ParseTree ctx, ParseTree first, ParseTree second, ParseTree third) {
        super(ctx);
        operator = second.getText();
        op1 = Expression.newExpression((IrpParser.ExpressionContext) first);
        op2 = Expression.newExpression((IrpParser.ExpressionContext) third);
    }

    @Override
    public String toIrpString(int radix) {
        return "(" + op1.toIrpString(radix) + operator + op2.toIrpString(radix) + ")";
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(10);
        String type = operator.equals("**") ? "Exponentiate"
                : operator.equals("+") ? "Add"
                : operator.equals("-") ? "Subtract"
                : operator.equals("*") ? "Multiply"
                : operator.equals("/") ? "Divide"
                : operator.equals("%") ? "Modulo"
                : operator.equals("<<") ? "ShiftLeft"
                : operator.equals(">>") ? "ShiftRight"
                : operator.equals("<=") ? "LessOrEqual"
                : operator.equals(">=") ? "GreaterOrEqual"
                : operator.equals(">") ? "Greater"
                : operator.equals("<") ? "Less"
                : operator.equals("==") ? "Equals"
                : operator.equals("!=") ? "NotEquals"
                : operator.equals("&") ? "BitwiseAnd"
                : operator.equals("|") ? "BitwiseOr"
                : operator.equals("^") ? "BitwiseExclusiveOr"
                : operator.equals("&&") ? "And"
                : operator.equals("||") ? "Or"
                : null;
        map.put("kind", type);
        Map<String, Object> arg1 = op1.propertiesMap(true, generalSpec, nameEngine);
        map.put("arg1", arg1);
        map.put("scalar", true);
        Map<String, Object> arg2 = op2.propertiesMap(true, generalSpec, nameEngine);
        map.put("arg2", arg2);
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.operator);
        hash = 41 * hash + Objects.hashCode(this.op1);
        hash = 41 * hash + Objects.hashCode(this.op2);
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
        final ThreePartExpression other = (ThreePartExpression) obj;
        if (!Objects.equals(this.operator, other.operator))
            return false;
        if (!Objects.equals(this.op1, other.op1))
            return false;
        return Objects.equals(this.op2, other.op2);
    }

    @Override
    public int weight() {
        return 1 + op1.weight() + op2.weight();
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws NameUnassignedException {
        long left = op1.toNumber(nameEngine);
        long right = op2.toNumber(nameEngine);

        switch (operator) {

            case "**":
                return IrCoreUtils.power(left, right);
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "<<":
                return left << right;
            case ">>":
                return left >> right;
            case "<=":
                return cBoolean(left <= right);
            case ">=":
                return cBoolean(left >= right);
            case "<":
                return cBoolean(left < right);
            case ">":
                return cBoolean(left > right);
            case "==":
                return cBoolean(left == right);
            case "!=":
                return cBoolean(left != right);
            case "&":
                return left & right;
            case "^":
                return left ^ right;
            case "|":
                return left | right;
            case "&&":
                return (left != 0 ? right : 0L);
            case "||":
                return (left != 0 ? left : right);
            default:
                throw new ThisCannotHappenException("Unknown operator: " + operator);
        }
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        Element e = document.createElement("BinaryOperator");
        e.setAttribute("kind", operator);
        el.appendChild(e);
        e.appendChild(op1.toElement(document));
        e.appendChild(op2.toElement(document));
        return el;
    }

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) throws NameUnassignedException {
        switch (operator) {
            case "+":
                return rhs - op2.toNumber(nameEngine);
            case "-":
                return rhs + op2.toNumber(nameEngine);
            case "*":
                return (rhs / op2.toNumber(nameEngine)) & bitmask;
            case "/":
                return (rhs * op2.toNumber(nameEngine)) & bitmask;
            case "^":
                return (rhs ^ op2.toNumber(nameEngine)) & bitmask;
            default:
                return null;
        }
    }

    @Override
    public PrimaryItem leftHandSide() {
        return op1;
    }
}
