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

import com.beust.jcommander.DefaultUsageFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameterized;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.DefaultConsole;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Parameters(commandNames = {"help"}, commandDescription = "Describe the syntax of program and commands.")
public class CommandHelp extends AbstractCommand {

    //private static final Logger logger = Logger.getLogger(CommandHelp.class.getName());

    private static String padString(String name, int length) {
        StringBuilder stringBuilder = new StringBuilder(name);
        while (stringBuilder.length() < length)
            stringBuilder.append(" ");
        return stringBuilder.toString();
    }

    public static void usage(PrintStream printStream, String command, JCommander argumentParser) {
        argumentParser.setConsole(new DefaultConsole(printStream));
        if (command == null)
            argumentParser.usage();
        else {
            DefaultUsageFormatter formatter = new DefaultUsageFormatter(argumentParser);
            formatter.usage(command);
        }
    }

    @Parameter(names = {"-c", "--common", "--options"}, description = "Describe the common options only.")
    private boolean commonOptions = false;

    @Parameter(names = {"-l", "--logging" }, description = "Describe the logging related options only.")
    private boolean loggingOptions = false;

    @Parameter(names = {"-s", "--short"}, description = "Produce a short usage message.")
    private boolean shortForm = false;

    @Parameter(description = "commands")
    private List<String> commands = null;

    public void help(PrintStream out, AbstractCommand commonCommand, JCommander argumentParser, String url) {
        Help instance = new Help(out, argumentParser, commonCommand);
        instance.help(url);
    }

    @Override
    public String description() {
        return "This command list the syntax for the command(s) given as argument, default all. "
                + "Also see the option \"--describe\".";
    }

    private class Help {

        private final PrintStream out;
        private final JCommander argumentParser;
        private final AbstractCommand commonCommand;

        private Help(PrintStream out, JCommander argumentParser, AbstractCommand commonCommand) {
            this.out = out;
            this.argumentParser = argumentParser;
            this.commonCommand = commonCommand;
            argumentParser.setConsole(new DefaultConsole(out));
        }

        private void help(String url) {
            if (shortForm) {
                shortUsage(out, argumentParser, url);
                return;
            }

            if (commonOptions) {
                commonOptions(out);
                return;
            }

            if (loggingOptions) {
                loggingOptions(out);
                return;
            }

            String cmd = argumentParser.getParsedCommand();
            if (commands != null)
                commands.forEach((command) -> {
                    try {
                        usage(out, command, argumentParser);
                    } catch (ParameterException ex) {
                        out.println("No such command: " + command);
                    }
                });
            else if (cmd == null || cmd.equals("help")) {
                usage(out, null, argumentParser);
                printDocumentationUrl(url);
            } else
                usage(out, cmd, argumentParser);
        }

        /**
         * Print just the common options. JCommander does not support this case,
         * so this implementation is pretty gross.
         */
        private void commonOptions(PrintStream out) {
            //CommandCommonOptions commonOptions = new CommandCommonOptions();
            JCommander parser = new JCommander(commonCommand);
            options(out, parser, "Common options:\n");
        }

        private void options(PrintStream out, JCommander parser, String title) {
            parser.setConsole(new DefaultConsole(out));
            StringBuilder str = new StringBuilder(2500);
            DefaultUsageFormatter formatter = new DefaultUsageFormatter(parser);
            formatter.usage(str);
            str.replace(0, 41, title); // barf!
            out.println(str.toString().trim()); // str ends with line feed.
        }

        /**
         * Print just the common options. JCommander does not support this case,
         * so this implementation is pretty gross.
         */
        private void loggingOptions(PrintStream out) {
            List<String> loggingOpts = Arrays.asList(CommandLogOptions.loggingOptions);
            CommandCommonOptions cla = new CommandCommonOptions();
            JCommander parser = new JCommander(cla);
            Map<Parameterized, ParameterDescription> f = parser.getFields();
            List<Parameterized> list = new ArrayList<>(f.keySet());
            list.stream().filter((p) -> (!loggingOpts.contains(p.getName()))).forEachOrdered((p) -> {
                f.remove(p);
            });
            options(out, parser, "Logging options:\n");
        }

        private void shortUsage(PrintStream out, JCommander argumentParser, String documentationUrl) {
            String PROGRAMNAME = argumentParser.getProgramName();
            out.println("Usage: " + PROGRAMNAME + " [options] [command] [command options]");
            out.println("Commands:");

            DefaultUsageFormatter formatter = new DefaultUsageFormatter(argumentParser);
            List<String> cmds = new ArrayList<>(argumentParser.getCommands().keySet());
            Collections.sort(cmds);
            cmds.forEach((cmd) -> {
                out.println("   " + padString(cmd, 16) + formatter.getCommandDescription(cmd));
            });

            out.println();
            out.println("Use");
            out.println("    \"" + PROGRAMNAME + " help\" for the full syntax,");
            out.println("    \"" + PROGRAMNAME + " help <command>\" for a particular command,");
            out.println("    \"" + PROGRAMNAME + " <command> --describe\" for a description,");
            out.println("    \"" + PROGRAMNAME + " help --common\" for the common options.");
            out.println("    \"" + PROGRAMNAME + " help --logging\" for the logging related options.");

            printDocumentationUrl(documentationUrl);
        }

        private void printDocumentationUrl(String documentationUrl) {
            out.println();
            out.println("For documentation, see " + documentationUrl);
        }
    }
}
