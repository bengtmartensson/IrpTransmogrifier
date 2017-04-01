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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * This class encapsulates the IrpParser.
 */
public final class ParserDriver {

    private final IrpLexer lexer;
    private final CommonTokenStream tokens;
    private final IrpParser parser;

    public ParserDriver(String irpString) {
        lexer = new IrpLexer(new ANTLRInputStream(irpString));
        tokens = new CommonTokenStream(lexer);
        parser = new IrpParser(tokens);
        parser.setErrorHandler(new ErrorStrategy());
    }

    public String toStringTree() {
        IrpParser.ProtocolContext protocol = parser.protocol();
        return protocol != null ? protocol.toStringTree(parser) : null;
    }

    public IrpParser getParser() {
        return parser;
    }
}
