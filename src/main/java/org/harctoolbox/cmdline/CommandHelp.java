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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.irp.Version;

@SuppressWarnings("FieldMayBeFinal")
@Parameters(commandNames = {"help"}, commandDescription = "Describe the syntax of program and commands.")
public class CommandHelp extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandHelp.class.getName());

    private static String padString(String name, int length) {
        StringBuilder stringBuilder = new StringBuilder(name);
        while (stringBuilder.length() < length)
            stringBuilder.append(" ");
        return stringBuilder.toString();
    }

    public static String usageString(String command, JCommander argumentParser) {
        StringBuilder stringBuilder = new StringBuilder(10000);
        if (command == null)
            argumentParser.usage(stringBuilder);
        else
            argumentParser.usage(command, stringBuilder);
        return stringBuilder.toString().trim();
    }

    @Parameter(names = {"-c", "--common", "--options"}, description = "Describe the common options only.")
    private boolean commonOptions = false;

    @Parameter(names = {"-s", "--short"}, description = "Produce a short usage message.")
    private boolean shortForm = false;

    @Parameter(description = "commands")
    private List<String> commands = null;

    public void help(PrintStream out, JCommander argumentParser) {
        Help instance = new Help(out, argumentParser);
        instance.help();
    }

    @Override
    public String description() {
        return "This command list the syntax for the command(s) given as argument, default all. "
                + "Also see the option \"--describe\".";
    }

    private class Help {

        private final PrintStream out;
        private final JCommander argumentParser;

        private Help(PrintStream out, JCommander argumentParser) {
            this.out = out;
            this.argumentParser = argumentParser;
        }

        private void help() {
            if (shortForm) {
                shortUsage(out, argumentParser);
                return;
            }

            if (commonOptions) {
                commonOptions(out);
                return;
            }

            String cmd = argumentParser.getParsedCommand();
            if (commands != null)
                commands.forEach((command) -> {
                    try {
                        out.println(usageString(command, argumentParser));
                    } catch (ParameterException ex) {
                        out.println("No such command: " + command);
                    }
                });
            else if (cmd == null || cmd.equals("help")) {
                out.println(usageString(null, argumentParser));
                printDocumentationUrl();
            } else
                out.println(usageString(cmd, argumentParser));
        }

        /**
         * Print just the common options. JCommander does not support this case,
         * so this implementation is pretty gross.
         */
        private void commonOptions(PrintStream out) {
            CommandCommonOptions cla = new CommandCommonOptions();
            JCommander parser = new JCommander(cla);
            StringBuilder str = new StringBuilder(2500);
            parser.usage(str);
            str.replace(0, 41, "Common options:\n"); // barf!
            out.println(str.toString().trim()); // str ends with line feed.
        }

        private void shortUsage(PrintStream out, JCommander argumentParser) {
            String PROGRAMNAME = argumentParser.getProgramName();
            out.println("Usage: " + PROGRAMNAME + " [options] [command] [command options]");
            out.println("Commands:");

            List<String> cmds = new ArrayList<>(argumentParser.getCommands().keySet());
            Collections.sort(cmds);
            cmds.forEach((cmd) -> {
                out.println("   " + padString(cmd, 16) + argumentParser.getCommandDescription(cmd));
            });

            out.println();
            out.println("Use");
            out.println("    \"" + PROGRAMNAME + " help\" for the full syntax,");
            out.println("    \"" + PROGRAMNAME + " help <command>\" for a particular command,");
            out.println("    \"" + PROGRAMNAME + " <command> --describe\" for a description,");
            out.println("    \"" + PROGRAMNAME + " help --common\" for the common options.");

            printDocumentationUrl();
        }

        private void printDocumentationUrl() {
            out.println();
            out.println("For documentation, see " + Version.documentationUrl);
        }
    }
}
