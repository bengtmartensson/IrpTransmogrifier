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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * This class encapsulates the IrpParser.
 */
public final class ParserDriver {

    private final IrpLexer lexer;
    private final CommonTokenStream tokens;
    private final IrpParser parser;

    public ParserDriver(String irpString) {
        lexer = new IrpLexer(CharStreams.fromString(irpString));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new myErrorListener());
        tokens = new CommonTokenStream(lexer);
        parser = new IrpParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new myErrorListener());
        parser.setErrorHandler(new ErrorStrategy());
    }

    public String toStringTree() {
        IrpParser.ProtocolContext protocol = parser.protocol();
        return protocol != null ? protocol.toStringTree(parser) : null;
    }

    public IrpParser getParser() {
        return parser;
    }

    // The default error listener is the ConsoleErrorListener, which prints messages to System.err.
    // Instead, we encode the message in the exception, which is expected to cancel the parsing.

    private static class myErrorListener extends BaseErrorListener {

        myErrorListener() {
            super();
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg, e);
        }
    }
}
