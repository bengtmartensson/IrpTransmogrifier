/*
Copyright (C) 2019, 2020 Bengt Martensson.

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
import com.beust.jcommander.ParameterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import org.harctoolbox.ircore.IrCoreUtils;

public abstract class CmdLineProgram {

    protected PrintStream out;
    protected final JCommander argumentParser;
    protected final CommandHelp commandHelp;
    protected String[] originalArguments;
    protected final CommandBasicOptions commandBasicOptions;
    protected String command;
    private final String progName;

    protected CmdLineProgram(PrintStream out, CommandBasicOptions commandLineArgs, String progName) {
        this.out = out;
        this.progName = progName;
        this.commandBasicOptions = commandLineArgs;
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(progName);
        argumentParser.setAllowAbbreviatedOptions(true);

        // The ordering in the following lines is the order the commands
        // will be listed in the help. Keep this order in a logical order.
        // In the rest of the file, these are ordered alphabetically.
        commandHelp = new CommandHelp();
        argumentParser.addCommand(commandHelp);
    }

    protected CmdLineProgram(CommandBasicOptions commandLineArgs, String progName) {
        this(System.out, commandLineArgs, progName);
    }

    public final PrintStream getOutputStream() {
        return out;
    }

    public void usage(String command) {
        CommandHelp.usage(out, command, argumentParser);
    }

    public ProgramExitStatus run(String[] args) {
        try {
            try {
                parseArgs(args);
                setupLoggers();
            } catch (UnsupportedEncodingException | UsageException | FileNotFoundException | ParameterException ex) {
                // Exceptions likely from silly user input, just print the exception
                return new ProgramExitStatus(progName, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
            } catch (IOException ex) {
                return new ProgramExitStatus(progName, ProgramExitStatus.EXIT_FATAL_PROGRAM_FAILURE, ex.getLocalizedMessage());
            }

            extraSetup();
            ProgramExitStatus status = processHelpAndDescription();
            if (status != null)
                return status;

            return processCommand();
        } finally {
            out.close();
        }
    }

    protected void setupCmds(AbstractCommand... cmds) {
        for (AbstractCommand cmd : cmds)
            argumentParser.addCommand(cmd);
    }

    protected void parseArgs(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        originalArguments = args.clone();
        argumentParser.parse(args);


        if (commandBasicOptions.output != null)
            out = IrCoreUtils.getPrintStream(commandBasicOptions.output, commandBasicOptions.encoding);


        // Since we have help and version as subcommands, --help and --version
        // are a little off. Keep them for compatibility, and
        // map --help and --version to the subcommands
        command = commandBasicOptions.helpRequested ? "help"
                : commandBasicOptions.versionRequested ? "version"
                : argumentParser.getParsedCommand();
    }

    protected ProgramExitStatus processHelpAndDescription() {
        if (command == null)
            return new ProgramExitStatus(progName, ProgramExitStatus.EXIT_USAGE_ERROR, "Command missing.");

        JCommander jCommander = argumentParser.getCommands().get(command);
        AbstractCommand cmd = (AbstractCommand) jCommander.getObjects().get(0);
        return cmd.process(this) ? new ProgramExitStatus() : null;
    }

    protected void setupLoggers() throws UsageException, IOException {
        if (commandBasicOptions.logformat != null) {
            System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", commandBasicOptions.logformat);
        }
        Logger topLevelLogger = Logger.getLogger("");
        Formatter formatter = commandBasicOptions.xmlLog ? new XMLFormatter() : new SimpleFormatter();
        Handler[] handlers = topLevelLogger.getHandlers();
        for (Handler handler : handlers) {
            topLevelLogger.removeHandler(handler);
        }

        String[] logclasses = commandBasicOptions.logclasses.split("\\|");
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Logger> loggers = new ArrayList<>(logclasses.length);
        for (String logclass : logclasses) {
            String[] classLevel = logclass.trim().split(":");
            if (classLevel.length < 2) {
                continue;
            }

            Logger log = Logger.getLogger(classLevel[0].trim());
            loggers.add(log); // stop them from being garbage collected
            Level level;
            try {
                level = Level.parse(classLevel[1].trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException ex) {
                throw new UsageException(ex + ". Valid levels are: OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL.");
            }
            log.setLevel(level);
            log.setUseParentHandlers(false);
            Handler handler = commandBasicOptions.logfile != null ? new FileHandler(commandBasicOptions.logfile) : new ConsoleHandler();
            handler.setLevel(level);
            handler.setFormatter(formatter);
            log.addHandler(handler);
        }

        Handler handler = commandBasicOptions.logfile != null ? new FileHandler(commandBasicOptions.logfile) : new ConsoleHandler();
        handler.setFormatter(formatter);
        topLevelLogger.addHandler(handler);

        handler.setLevel(commandBasicOptions.logLevel);
        topLevelLogger.setLevel(commandBasicOptions.logLevel);
    }

    public void extraSetup() {
    }

    public abstract ProgramExitStatus processCommand();
}
