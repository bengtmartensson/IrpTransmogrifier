/*
Copyright (C) 2011,2012, 2015 Bengt Martensson.

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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 */
public class Expression extends PrimaryItem /* ??? */ {

    //private static boolean debug;
    private IrpParser.ExpressionContext parseTree;

    /**
     * Construct an Expression from the number supplied as argument.
     * @param value
     * @throws IrpSyntaxException
     */
    public Expression(long value) throws IrpSyntaxException { // FIXME: insanely inefficient
        this(new ParserDriver(Long.toString(value)));
    }

    /**
     * Construct an Expression by parsing the argument.
     * @param str
     * @throws IrpSyntaxException
     */
    public Expression(String str) throws IrpSyntaxException {
        this(new ParserDriver(str));
    }

    Expression(ParserDriver parserDriver) throws IrpSyntaxException {
        this(parserDriver.getParser().expression());
    }

    public Expression(IrpParser.Para_expressionContext ctx) {
        this(ctx.expression());
    }

//    Expression(IrpParser.Expression_asitemContext ctx) {
//        this(ctx.para_expression());
//    }

    public Expression(IrpParser.ExpressionContext ctx) {
        this.parseTree = ctx;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.parseTree);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Expression))
            return false;

        Expression other = (Expression) obj;

        return toIrpString().equals(other.toIrpString());
//        for (int i = 0; i < parseTree.children.size(); i++) {
//            ParseTree mychild = parseTree.children.get(i);
//            ParseTree theirChild = other.parseTree.children.get(i);
//
//            if (!parseTree.children.get(i).equals(other.parseTree.children.get(i)))
//                return false;
//        }
//
//        return true;
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    public String toStringTree(IrpParser parser) {
        return parseTree.toStringTree(parser);
    }

    public String toStringTree() {
        return parseTree.toStringTree();
    }

    @Override
    public long invert(long rhs) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {

        int noChilden = parseTree.getChildCount();
        long solution;
        String operator;

        switch (noChilden) {
            case 1:
                return rhs;
            case 2:
                operator = parseTree.getChild(0).getText();
                solution = operator.equals("~") ? ~rhs
                        : operator.equals("-") ? -rhs
                        : throwNewRuntimeException("Not implemented");
                break;
            case 3:
                // Solve the equation
                // exp1 op exp2 = rhs
                // for op: "+", "-", "*", and "/"
                operator = parseTree.getChild(1).getText();
                Expression operand = new Expression(parseTree.expression(1));
                long exp2 = operand.toNumber();
                solution
                        = operator.equals("+") ? rhs - exp2
                        : operator.equals("-") ? rhs + exp2
                        : operator.equals("*") ? rhs / exp2
                        : operator.equals("/") ? rhs * exp2
                        : throwNewRuntimeException("Not implemented");
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }

        Expression exp1 = new Expression(parseTree.expression(0)); // contains exactly one name
        return exp1.invert(solution);
    }

    @Override
    public boolean isUnary() {
        return parseTree.getChildCount() == 1;
    }

    public int numberNames() {
        if (parseTree.primary_item() != null)
            return newPrimaryItem(parseTree.primary_item()).toName() != null ? 1 : 0;

        int number = 0;
        for (IrpParser.ExpressionContext expression : parseTree.expression()) {
            number += new Expression(expression).numberNames();
        }

        return number;
    }

    @Override
    public Name toName() {
        if (parseTree.primary_item() != null)
            return newPrimaryItem(parseTree.primary_item()).toName();

        int number = numberNames();
        return (number == 1 && parseTree.expression(0) != null)
                ? new Expression(parseTree.expression(0)).toName()
                : null;
//
//            parseTree.primary_item()
//        if (parseTree.children.size() == 3) {
//            ParseTree x = parseTree.children.get(0);
//            IrpParser.ExpressionContext exp = (IrpParser.ExpressionContext) x;
//            ParseTree y = parseTree.children.get(1);
//            ParseTree z = parseTree.children.get(2);
//
//        //PrimaryItem pi = newPrimaryItem(parseTree.primary_item());
//        }
//        //return pi.toName();
//        return null;
    }

    public long toNumber() throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(parseTree, new NameEngine());
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(parseTree, nameEngine);
    }

//    @Override
//    public String toInfixCode() throws IrpSyntaxException {
//        return toInfixCode(parseTree);
//    }

    private long toNumber(IrpParser.ExpressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        int noChilden = ctx.getChildCount();
        return noChilden == 1
                ? toNumberPrimary(ctx.getChild(0), nameEngine)
                : noChilden == 2 ? toNumberUnary(ctx.getChild(0).getText(), (IrpParser.ExpressionContext)ctx.getChild(1), nameEngine)
                : noChilden == 3 ? toNumberBinary((IrpParser.ExpressionContext) ctx.getChild(0), ctx.getChild(1).getText(),
                        (IrpParser.ExpressionContext) ctx.getChild(2), nameEngine)
                : noChilden == 5 ? toNumberTernary((IrpParser.ExpressionContext) ctx.getChild(0), (IrpParser.ExpressionContext) ctx.getChild(2),
                        (IrpParser.ExpressionContext) ctx.getChild(4), nameEngine)
                : throwNewRuntimeException();
    }

    private long toNumberPrimary(ParseTree child, NameEngine nameEngine) throws IrpSyntaxException, IncompatibleArgumentException, UnassignedException {
        return child instanceof IrpParser.Primary_itemContext
                ? newPrimaryItem((IrpParser.Primary_itemContext) child).toNumber(nameEngine)
                : child instanceof IrpParser.BitfieldContext ? BitField.newBitField((IrpParser.BitfieldContext) child).toNumber(nameEngine)
                : throwNewRuntimeException();
    }

    private long toNumberUnary(String operator, IrpParser.ExpressionContext expressionContext, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long operand = new Expression(expressionContext).toNumber(nameEngine);
        return operator.equals("!") ? (operand == 0L ? 1L : 0L)
                : operator.equals("#") ? Long.bitCount(operand)
                : operator.equals("-") ? - operand
                : operator.equals("~") ? ~ operand
                : throwNewRuntimeException("Unknown operator: " + operator);
    }

    private long toNumberBinary(IrpParser.ExpressionContext expressionContext, String operator,
            IrpParser.ExpressionContext expressionContext0, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long left = new Expression(expressionContext).toNumber(nameEngine);
        long right = new Expression(expressionContext0).toNumber(nameEngine);

        return    operator.equals("**") ? IrpUtils.power(left, right)
                : operator.equals("*")  ? left * right
                : operator.equals("/")  ? left / right
                : operator.equals("%")  ? left % right
                : operator.equals("+")  ? left + right
                : operator.equals("-")  ? left - right
                : operator.equals("<<") ? left << right
                : operator.equals(">>") ? left >> right
                : operator.equals("<=") ? cBoolean(left <= right)
                : operator.equals(">=") ? cBoolean(left >= right)
                : operator.equals("<")  ? cBoolean(left < right)
                : operator.equals(">")  ? cBoolean(left > right)
                : operator.equals("==") ? cBoolean(left == right)
                : operator.equals("!=") ? cBoolean(left != right)
                : operator.equals("&")  ? left & right
                : operator.equals("^")  ? left ^ right
                : operator.equals("|")  ? left | right
                : operator.equals("&&") ? (left != 0 ? right : 0L)
                : operator.equals("||") ? (left != 0 ? left : right)
                : throwNewRuntimeException("Unknown operator: " + operator);
    }

    private long toNumberTernary(IrpParser.ExpressionContext expressionContext, IrpParser.ExpressionContext trueExpContext,
            IrpParser.ExpressionContext falseExpContext, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long ctrl = new Expression(expressionContext).toNumber(nameEngine);
        return new Expression(ctrl != 0L ? trueExpContext : falseExpContext).toNumber(nameEngine);
    }

    private long throwNewRuntimeException() {
        throw new RuntimeException("This cannot happen");
    }

    private long throwNewRuntimeException(String msg) {
        throw new RuntimeException(msg);
    }

//    private long cBinary(long x) {
//        return x != 0L ? 1L : 0L;
//    }

    private long cBoolean(boolean x) {
        return x ? 1L : 0L;
    }


//    private String toInfixCode(IrpParser.Bare_expressionContext ctx) throws IrpSyntaxException {
//        return toInfixCode(ctx.inclusive_or_expression());
//    }

//    private long toNumber(IrpParser.Inclusive_or_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = 0L;
//        for (IrpParser.Exclusive_or_expressionContext expr : ctx.exclusive_or_expression())
//            result |= toNumber(expr, nameEngine);
//
//        return result;
//    }

//    private String toInfixCode(IrpParser.Inclusive_or_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Exclusive_or_expressionContext expr : ctx.exclusive_or_expression()) {
//            if (sb.length() > 0)
//                sb.append("|");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

//    private long toNumber(IrpParser.Exclusive_or_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = 0L;
//        for (IrpParser.And_expressionContext expr : ctx.and_expression())
//            result ^= toNumber(expr, nameEngine);
//
//        return result;
//    }

//    private String toInfixCode(IrpParser.Exclusive_or_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.And_expressionContext expr : ctx.and_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

//    private long toNumber(IrpParser.And_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = -1L;
//        for (IrpParser.Shift_expressionContext expr : ctx.shift_expression()) {
//            result &= toNumber(expr, nameEngine);
//        }
//        return result;
//    }

//    private String toInfixCode(IrpParser.And_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Shift_expressionContext expr : ctx.shift_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

//    private long toNumber(IrpParser.Shift_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = toNumber(ctx.additive_expression(0), nameEngine);
//        for (int i = 1; i < ctx.children.size(); i++) {
//            ParseTree x = ctx.children.get(i);
//            if (x instanceof IrpParser.Additive_expressionContext) {
//                long op = toNumber((IrpParser.Additive_expressionContext) x, nameEngine);
//                result = (ctx.children.get(i - 1).getText().charAt(0) == '<')
//                        ? result << op : result >> op;
//            }
//        }
//        return result;
//    }

//    private String toInfixCode(IrpParser.Shift_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Additive_expressionContext expr : ctx.additive_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

//    private long toNumber(IrpParser.Additive_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = 0L;
//        for (int i = 0; i < ctx.children.size(); i++) {
//            ParseTree x = ctx.children.get(i);
//            if (x instanceof IrpParser.Multiplicative_expressionContext) {
//                long op = toNumber((IrpParser.Multiplicative_expressionContext) x, nameEngine);
//                if (i == 0 || ctx.children.get(i - 1).getText().charAt(0) == '+')
//                    result += op;
//                else
//                    result -= op;
//            }
//        }
//        return result;
//    }

//    private String toInfixCode(IrpParser.Additive_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (ParseTree child : ctx.children) {
//            if (child instanceof IrpParser.Multiplicative_expressionContext)
//                sb.append(toInfixCode((IrpParser.Multiplicative_expressionContext) child));
//            else
//                sb.append(child.getText());
//        }
//        return sb.toString();
//    }

//    private long toNumber(IrpParser.Multiplicative_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        long result = 1L;
//        for (int i = 0; i < ctx.children.size(); i++) {
//            ParseTree x = ctx.children.get(i);
//            if (x instanceof IrpParser.Exponential_expressionContext) {
//                long op = toNumber((IrpParser.Exponential_expressionContext) x, nameEngine);
//                if (i == 0 || ctx.children.get(i - 1).getText().charAt(0) == '*')
//                    result *= op;
//                else
//                    result /= op;
//            }
//        }
//        return result;
//    }

//    private String toInfixCode(IrpParser.Multiplicative_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (ParseTree child : ctx.children) {
//            if (child instanceof IrpParser.Exponential_expressionContext)
//                sb.append(toInfixCode((IrpParser.Exponential_expressionContext) child));
//            else
//                sb.append(child.getText());
//        }
//        return sb.toString();
//    }

    // Note: exponentiation is right-associative, so we evaluate the arguments in descending  order
//    private long toNumber(IrpParser.Exponential_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        List<IrpParser.Unary_expressionContext> children = ctx.unary_expression();
//        //long result = toNumber(children.get(children.size() - 1), nameEngine);
//        UnaryExpression unaryExpession = new UnaryExpression(children.get(children.size()-1));
//        long result = unaryExpession.toNumber(nameEngine);
//        for (int i = children.size() - 2; i >= 0; i--) {
//            unaryExpession = new UnaryExpression(children.get(i));
//            result = IrpUtils.power(unaryExpession.toNumber(nameEngine), result);
//        }
//        return result;
//    }

//    private String toInfixCode(IrpParser.Exponential_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        List<IrpParser.Unary_expressionContext> children = ctx.unary_expression();
//        //long result = toNumber(children.get(children.size() - 1), nameEngine);
//        for (int i = children.size() - 1; i >= 0; i++) {
//            IrpParser.Unary_expressionContext child = children.get(i);
//            if (i < children.size() - 1) {
//                sb.append(")");
//                sb.insert(0, ",");
//            }
//            sb.insert(0, (new UnaryExpression(child)).toInfixCode());
//            if (i < children.size() - 1)
//                sb.insert(0, expfunctionName + "(");
//        }
//        return sb.toString();
//    }

    /**
     * @return the parseTree
     */
    IrpParser.ExpressionContext getParseTree() {
        return parseTree;
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("expression");
        Element op;
        switch (parseTree.children.size()) {
            case 1:
                ParseTree child = parseTree.children.get(0);

                try {
                    if (child instanceof IrpParser.Primary_itemContext)
                        element.appendChild(newPrimaryItem((IrpParser.Primary_itemContext) child).toElement(document));
                    else if (child instanceof IrpParser.BitfieldContext)
                        element.appendChild(BitField.newBitField((IrpParser.BitfieldContext) child).toElement(document));
                    else
                        ;
                } catch (IrpSyntaxException ex) {
                    Logger.getLogger(Expression.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case 2:
                op = document.createElement("unary_operator");
                op.setAttribute("type", parseTree.children.get(0).getText());
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                break;
            case 3:
                op = document.createElement("binary_operator");
                op.setAttribute("type", parseTree.children.get(1).getText());
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(1)).toElement(document));
                break;
            case 5:
                op = document.createElement("ternary_operator");
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(1)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(2)).toElement(document));
                break;
            default:
                element.setTextContent("Unknown case in Expression.toElement");
        }
        return element;
    }

    @Override
    public String toIrpString() {
        return toIrpString(10);
    }

    @Override
    public String toIrpString(int radix) {
        if (parseTree == null)
            return null;

        switch (parseTree.children.size()) {
            case 1:
                ParseTree child = parseTree.children.get(0);

                try {
                    if (child instanceof IrpParser.Primary_itemContext)
                        return newPrimaryItem((IrpParser.Primary_itemContext) child).toIrpString(radix);
                    else if (child instanceof IrpParser.BitfieldContext)
                        return BitField.newBitField((IrpParser.BitfieldContext) child).toIrpString();
                    else
                        return null;
                } catch (IrpSyntaxException ex) {
                    Logger.getLogger(Expression.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case 2:
                return "(" + parseTree.children.get(0).getText() + new Expression(parseTree.expression(0)).toIrpString() + ")";

            case 3:
                return "("
                        + new Expression(parseTree.expression(0)).toIrpString(radix)
                        + parseTree.children.get(1).getText()
                        + new Expression(parseTree.expression(1)).toIrpString(radix)
                        + ")";
            case 5:
                return "("
                        + new Expression(parseTree.expression(0)).toIrpString(radix) + "?"
                        + new Expression(parseTree.expression(1)).toIrpString(radix) + ":"
                        + new Expression(parseTree.expression(2)).toIrpString(radix)
                        + ")";
            default:
                throw new ThisCannotHappenException("Unknown case in Expression.toElement");
        }
        return null;
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = parseTree.children.stream().filter((child) -> (child instanceof IrpObject)).map((child) -> ((IrpObject) child).weight()).reduce(weight, Integer::sum);
        return weight;
    }
}
