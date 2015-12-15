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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class encapsulates the IrpParser.
 */
public class ParserDriver {

    private final IrpLexer lexer;
    private final CommonTokenStream tokens;
    private IrpParser parser;

    public ParserDriver(String irpString) {
        lexer = new IrpLexer(new ANTLRInputStream(irpString));
        tokens = new CommonTokenStream(lexer);
        parser = new IrpParser(tokens);
        parser.setErrorHandler(new ErrorStrategy());
    }

    public IrpParser getParser() {
        return parser;
    }

    public IrpParser.ProtocolContext protocol() throws IrpSyntaxException {
        try {
            return parser.protocol();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.GeneralspecContext generalspec() throws IrpSyntaxException {
        try {
            return parser.generalspec();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.Parameter_specsContext parameterSpecs() throws IrpSyntaxException {
        try {
            return parser.parameter_specs();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.ExpressionContext expression() throws IrpSyntaxException {
        try {
            return parser.expression();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public static double visit(IrpParser.Number_with_decimalsContext ctx) {
        return (ctx instanceof IrpParser.IntegerAsFloatContext)
                ? parseINT(((IrpParser.IntegerAsFloatContext) ctx).INT())
                : visit(((IrpParser.FloatContext) ctx).float_number());
    }

    public static double visit(IrpParser.Float_numberContext ctx) {
        return (ctx instanceof IrpParser.DotIntContext)
                ? visit((IrpParser.DotIntContext) ctx)
                : visit((IrpParser.IntDotIntContext) ctx);
    }

    public static double visit(IrpParser.DotIntContext ctx) {
        return parseFloat(ctx.INT());
    }

    public static double visit(IrpParser.IntDotIntContext ctx) {
        return parseFloat(ctx.INT(0), ctx.INT(1));
    }

    private static long parseINT(TerminalNode n) {
        return Long.parseLong(n.getText());
    }

    private static double parseFloat(TerminalNode integ, TerminalNode matissa) {
        return Double.parseDouble(integ.getText() + "." + matissa.getText());
    }

    private static double parseFloat(TerminalNode matissa) {
        return Double.parseDouble("0." + matissa.getText());
    }
}
