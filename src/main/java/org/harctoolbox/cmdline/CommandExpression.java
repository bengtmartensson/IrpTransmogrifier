/*
Copyright (C) 2019 Bengt Martensson.

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

package org.harctoolbox.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.Expression;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.xml.XmlUtils;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"expression"}, commandDescription = "Evaluate expression given as argument.")
public class CommandExpression extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandExpression.class.getName());

    @Parameter(names = {"-n", "--nameengine", "--parameters"}, description = "Define a name engine to use for evaluating.", converter = NameEngineParser.class)
    private NameEngine nameEngine = new NameEngine();

    @Parameter(names = {"-r", "--radix"}, description = "Radix for outputting result.", validateWith = Radix.class)
    private int radix = 10;

    @Parameter(names = {"--stringtree"}, description = "Output stringtree.")
    private boolean stringTree = false;

    @Parameter(names = {"--gui", "--display"}, description = "Display parse diagram.")
    private boolean gui = false;

    @Parameter(names = {"--xml"}, description = "Generate XML and write to file argument.")
    private String xml = null;

    @Parameter(description = "expression", required = true)
    private List<String> expressions;

    @Override
    public String description() {
        return "This command evaluates its argument as an expression. "
                + "Using the --nameengine argument, the expression may also contain names. "
                + "The --gui options presents a graphical representation of the parse tree.";
    }

    public void expression(PrintStream out, CommandCommonOptions commandLineArgs) throws FileNotFoundException, NameUnassignedException, IrpParseException, UnsupportedEncodingException {
        //NameEngine nameEngine = nameEngine;
        String text = String.join(" ", expressions).trim();
        Expression expression = Expression.newExpressionEOF(text);
        long result = expression.toLong(nameEngine);

        out.println(IrCoreUtils.radixPrefix(radix) + Long.toString(result, radix));
        if (stringTree)
            out.println(expression.toStringTree());

        if (xml != null) {
            XmlUtils.printDOM(xml, expression.toDocument(), commandLineArgs.encoding, null);
            logger.log(Level.INFO, "Wrote {0}", xml);
        }

        if (gui)
            IrpUtils.showTreeViewer(expression.toTreeViewer(), text + "=" + result);
    }
}
