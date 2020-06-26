/*
Copyright (C) 2020 Bengt Martensson.

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

package org.harctoolbox.xml;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.Version;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class allows command line access to reading, writing, validating, and xslt transformation of XML files.
 */
public final class XmlTransmogrifier {
    private static JCommander argumentParser;

    private static void usage(int exitcode) {
        argumentParser.usage();
        System.exit(exitcode);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        CommandLineArguments commandLineArgs = new CommandLineArguments();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);
        argumentParser.setAllowAbbreviatedOptions(true);


        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.EXIT_USAGE_ERROR);
        }

        if (commandLineArgs.helpRequested)
            usage(IrpUtils.EXIT_SUCCESS);

        PrintStream out = null;
        try {
            out = IrCoreUtils.getPrintStream(commandLineArgs.output, commandLineArgs.encoding);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.EXIT_USAGE_ERROR);
        }

        try {
            Document doc = XmlUtils.openXmlFile(commandLineArgs.argument, commandLineArgs.schema, true, true);
            if (!commandLineArgs.print) {
                System.out.println("Use the --print option if you want output.");
                System.exit(IrpUtils.EXIT_SUCCESS);
            }

            if (commandLineArgs.stylesheet == null) {
                XmlUtils.printDOM(out, doc, commandLineArgs.encoding, null);
            } else {
                Document stylesheet = XmlUtils.openXmlFile(commandLineArgs.stylesheet, null, true, true);
                XmlUtils.printDOM(out, doc, commandLineArgs.encoding, stylesheet, new HashMap<>(0), false);
            }
        } catch (SAXException | IOException | TransformerException ex) {
            ex.printStackTrace();
            System.exit(IrpUtils.EXIT_FATAL_PROGRAM_FAILURE);
        }
        System.exit(IrpUtils.EXIT_SUCCESS);
    }

    private XmlTransmogrifier() {
    }

    public static class CommandLineArguments {
        @Parameter(names = {"-e", "--encoding"}, description = "Output encoding")
        private String encoding = XmlUtils.DEFAULT_CHARSETNAME;

        @Parameter(names = {"-h", "-?", "--help"}, description = "Print help text")
        private boolean helpRequested = false;

        @Parameter(names = {"-o", "--output"}, description = "Output file; \"-\" for stdout")
        private String output = "-";

        @Parameter(names = {"-p", "--print"}, description = "Print result on the --output argument")
        private boolean print = false;

        @Parameter(names = {"-s", "--schema"}, description = "URL/Filename of schema for validation")
        private String schema = null;

        @Parameter(names = {"--xslt"}, description = "URL/filename of stylesheet")
        private String stylesheet = null;

        @Parameter(required = true, description = "URL/Filename or - for stdin")
        private String argument = "-";
    }
}
