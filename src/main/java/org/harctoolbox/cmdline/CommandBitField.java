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
import org.harctoolbox.ircore.XmlUtils;
import org.harctoolbox.irp.BitField;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"bitfield"}, commandDescription = "Evaluate bitfield given as argument.")
public class CommandBitField extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandBitField.class.getName());

    @Parameter(names = {"-n", "--nameengine", "--parameters"}, description = "Define a name engine for resolving the bitfield.", converter = NameEngineParser.class)
    private NameEngine nameEngine = new NameEngine();

    @Parameter(names = {"-l", "--lsb"}, description = "Output bitstream with least significant bit first.")
    private boolean lsb = false;

    @Parameter(names = {"--xml"}, description = "Generate XML and write to file given as argument.")
    private String xml = null;

    @Parameter(description = "bitfield", required = true)
    private List<String> bitField;

    @Override
    public String description() {
        return "The \"bitfield\" command computes the value and the binary form corresponding to the bitfield given as input. "
                + "Using the --nameengine argument, the bitfield can also refer to names. "
                + "\n\n"
                + "As an alternatively, the \"expression\" command may be used. "
                + "However, a bitfield has a length, which an expression, evaluating to an integer value, does not.";
    }

    public void bitfield(PrintStream out, CommandCommonOptions commandLineArgs) throws NameUnassignedException, FileNotFoundException, UnsupportedEncodingException {
        //NameEngine nameEngine = commandBitField.nameEngine;
        String text = String.join("", bitField).trim();
        BitField bitfield = BitField.newBitField(text);
        long result = bitfield.toLong(nameEngine);

        listProperty("integer value", result, out, commandLineArgs.quiet);
        if (bitfield instanceof FiniteBitField) {
            FiniteBitField fbf = (FiniteBitField) bitfield;
            listProperty("bitfield", fbf.toBinaryString(nameEngine, lsb), out, commandLineArgs.quiet);
        }

        if (xml != null) {
            XmlUtils.printDOM(IrCoreUtils.getPrintStream(xml, commandLineArgs.encoding), bitfield.toDocument(), commandLineArgs.encoding, null);
            logger.log(Level.INFO, "Wrote {0}", xml);
        }
    }

    private void listProperty(String propertyName, long value, PrintStream out, boolean quiet) {
        listProperty(propertyName, Long.toString(value), out, quiet);
    }

    private void listProperty(String propertyName, String propertyValue, PrintStream out, boolean quiet) {
        if (!quiet && propertyName != null)
            out.print(propertyName + "=");
        out.println(propertyValue);
    }
}
