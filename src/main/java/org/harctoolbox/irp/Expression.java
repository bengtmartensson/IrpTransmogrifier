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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 */
public class Expression extends PrimaryItem {

    //private static boolean debug;
    private IrpParser.ExpressionContext parseTree;
    private static final String expfunctionName = "powl";

    /**
     * Construct an Expression from the number supplied as argument.
     * @param value
     * @throws IrpSyntaxException
     */
    public Expression(long value) throws IrpSyntaxException {
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
    public String toString() {
        return toStringTree();
    }

    public String toStringTree(IrpParser parser) {
        return parseTree.toStringTree(parser);
    }

    public String toStringTree() {
        return parseTree.toStringTree();
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
                : throwNewRuntimeException();
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
                : operator.equals("<=") ? cBinary(left <= right)
                : operator.equals(">=") ? cBinary(left >= right)
                : operator.equals("<")  ? cBinary(left < right)
                : operator.equals(">")  ? cBinary(left > right)
                : operator.equals("==") ? cBinary(left == right)
                : operator.equals("!=") ? cBinary(left != right)
                : operator.equals("&")  ? left & right
                : operator.equals("|")  ? left | right
                : operator.equals("&&") ? (left != 0 ? right : 0L)
                : operator.equals("||") ? (left != 0 ? left : right)
                : throwNewRuntimeException();
    }

    private long toNumberTernary(IrpParser.ExpressionContext expressionContext, IrpParser.ExpressionContext trueExpContext,
            IrpParser.ExpressionContext falseExpContext, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long ctrl = new Expression(expressionContext).toNumber(nameEngine);
        return ctrl = new Expression(ctrl != 0L ? trueExpContext : falseExpContext).toNumber(nameEngine);
    }

    private long throwNewRuntimeException() {
        throw new RuntimeException("This cannot happen");
    }

    private long cBinary(long x) {
        return x != 0L ? 1L : 0L;
    }

    private long cBinary(boolean x) {
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
    public IrpParser.ExpressionContext getParseTree() {
        return parseTree;
    }

    Element toElement(Document document) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * show the given Tree Viewer
     *
     * @param tv
     * @return
     */
    public static int showTreeViewer(TreeViewer tv, String title) {
        JPanel panel = new JPanel();
        tv.setScale(2);
        panel.add(tv);

        return JOptionPane.showConfirmDialog(null, panel, title,
                JOptionPane.CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private static JCommander argumentParser;

//    private static void usage(int code) {
//        System.out.println("Usage:");
//        System.out.println("\tExpression [-d]? <expression> [<name>=<value>|{<name>=<expression>}]*");
//        System.exit(code);
//    }

    private final static class CommandLineArgs {
//        @Parameter(names = {"-d", "--debug"}, description = "Debug, not yet implemented")
//        private int debug = 0;

        @Parameter(names = {"--gui"}, description = "Display parse tree")
        private boolean gui = false;

        @Parameter(names = {"-h", "?", "--help"}, description = "List options")
        private boolean help = false;

        @Parameter(names = {"-p", "--parsetree"}, description = "Print parse tree")
        private boolean parseTree = false;

        @Parameter(names = {"-n", "--nameengine"}, description = "Name engine")
        private String names = null;

        @Parameter(description = "expression")
        private List<String> expression = new ArrayList<>();
    }

   /**
     * Evaluate the argument supplied on the command line, possibly with a name assignment.
     */
    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName("IrpDatabase");

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            argumentParser.usage();
            //usage(IrpUtils.exitUsageError);
        }

        if (commandLineArgs.help || commandLineArgs.expression.isEmpty()) {
            argumentParser.usage();
            System.exit(IrpUtils.exitSuccess);
        }

        try {
            String text = IrpUtils.join(commandLineArgs.expression, "");
            IrpParser parser = new ParserDriver(text).getParser();
            Expression expression = new Expression(parser.expression());
            //if (!parser.isMatchedEOF()) {
            //    System.err.println("Did not match all input");
            //    System.exit(IrpUtils.exitFatalProgramFailure);
            //}
            NameEngine nameEngine = new NameEngine(commandLineArgs.names);
            long result = expression.toNumber(nameEngine);
            System.out.println(result);
            if (commandLineArgs.parseTree)
                System.out.println(expression.getParseTree().toStringTree(parser));

            if (commandLineArgs.gui) {
                List<String> ruleNames = Arrays.asList(parser.getRuleNames());

                // http://stackoverflow.com/questions/34832518/antlr4-dotgenerator-example
                TreeViewer tv = new TreeViewer(ruleNames, expression.getParseTree());
                showTreeViewer(tv, text+"="+result);
            }
        } catch (ParseCancellationException | IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            System.err.println(ex);
        }
    }
}
