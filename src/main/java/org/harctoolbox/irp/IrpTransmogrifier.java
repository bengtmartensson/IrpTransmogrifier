/*
Copyright (C) 2017, 2018, 2019 Bengt Martensson.

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
import com.beust.jcommander.ParameterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
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
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.analyze.NoDecoderMatchException;
import org.harctoolbox.cmdline.AbstractCommand;
import org.harctoolbox.cmdline.CmdLineProgram;
import org.harctoolbox.cmdline.CmdUtils;
import org.harctoolbox.cmdline.CommandAnalyze;
import org.harctoolbox.cmdline.CommandBitField;
import org.harctoolbox.cmdline.CommandCode;
import org.harctoolbox.cmdline.CommandCommonOptions;
import org.harctoolbox.cmdline.CommandDecode;
import org.harctoolbox.cmdline.CommandDemodulate;
import org.harctoolbox.cmdline.CommandExpression;
import org.harctoolbox.cmdline.CommandHelp;
import org.harctoolbox.cmdline.CommandLirc;
import org.harctoolbox.cmdline.CommandList;
import org.harctoolbox.cmdline.CommandRender;
import org.harctoolbox.cmdline.CommandVersion;
import org.harctoolbox.cmdline.ProgramExitStatus;
import org.harctoolbox.cmdline.UsageException;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * This class contains a command line main routine, allowing command line access to most things in the package.
 *
 * Basically, there should not be "too much" business logic here; we construct element and call its
 * member functions, defined elsewhere.
 */
public final class IrpTransmogrifier implements CmdLineProgram {

    // No need to make these settable, at least not presently
    public static final String DEFAULT_CONFIG_FILE = "/IrpProtocols.xml"; // in jar-file
    private static final String PROGRAMNAME = Version.appName;

    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());

    static String execute(String commandLine) {
        return execute(CmdUtils.shellSplit(commandLine));
    }

    static String execute(String[] args) {
        return CmdUtils.execute(IrpTransmogrifier.class, args);
    }

    /**
     *
     * @param args
     * @param out
     */
    private static void main(String[] args, PrintStream out) {
        IrpTransmogrifier instance = new IrpTransmogrifier(out);
        ProgramExitStatus status = instance.run(args);
        out.close();
        status.die();
    }

    public static void main(String[] args) {
        main(args, System.out);
    }

    private PrintStream out;
    private IrpDatabase irpDatabase;
    private JCommander argumentParser;
    private CommandCommonOptions commandLineArgs;
    private CommandHelp commandHelp;
    private CommandVersion commandVersion;
    private CommandList commandList;
    private CommandRender commandRender;
    private CommandDecode commandDecode;
    private CommandDemodulate commandDemodulate;
    private CommandAnalyze commandAnalyze;
    private CommandCode commandCode;
    private CommandBitField commandBitField;
    private CommandExpression commandExpression;
    private CommandLirc commandLirc;
    private String[] originalArguments; // Really necessary?

    public IrpTransmogrifier() {
        this(System.out);
    }

    public IrpTransmogrifier(PrintStream out) {
        this.out = out;
    }

    /**
     *
     * @param args program args
     * @return
     */
    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public ProgramExitStatus run(String[] args) {
        this.originalArguments = args.clone();
        setupArgParser();

        try {
            argumentParser.parse(args);
        } catch (ParameterException | NumberFormatException ex) {
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getMessage());
        }

        try {
            setupLoggers();

            if (commandLineArgs.seed != null)
                ParameterSpec.initRandom(commandLineArgs.seed);

            if (commandLineArgs.output != null)
                out = IrCoreUtils.getPrintSteam(commandLineArgs.output);

            // Since we have help and version as subcommands, --help and --version
            // are a little off. Keep them for compatibility, and
            // map --help and --version to the subcommands
            String command = commandLineArgs.helpRequested ? "help"
                    : commandLineArgs.versionRequested ? "version"
                    : argumentParser.getParsedCommand();

            if (command == null)
                return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, "Command missing.");

            boolean processed = processHelpAndDescription(command);
            if (processed)
                return new ProgramExitStatus();

            switch (command) {
                case "analyze":
                    commandAnalyze.analyze(out, commandLineArgs);
                    break;
                case "bitfield":
                    commandBitField.bitfield(out, commandLineArgs);
                    break;
                case "code":
                    setupDatabase(commandLineArgs.blackList);
                    commandCode.code(out, commandLineArgs, irpDatabase, originalArguments);
                    break;
                case "decode":
                    setupDatabase(commandLineArgs.blackList);
                    commandDecode.decode(out, commandLineArgs, irpDatabase);
                    break;
                case "demodulate":
                    commandDemodulate.demodulate(out);
                    break;
                case "expression":
                    commandExpression.expression(out, commandLineArgs);
                    break;
                case "help":
                    commandHelp.help(out, argumentParser);
                    break;
                case "lirc":
                    commandLirc.lirc(out, commandLineArgs.encoding);
                    break;
                case "list":
                    setupDatabase(commandLineArgs.blackList);
                    commandList.list(out, commandLineArgs, irpDatabase);
                    break;
                case "render":
                    setupDatabase(commandLineArgs.blackList);
                    commandRender.render(out, irpDatabase, commandLineArgs);
                    break;
                case "version":
                    setupDatabase(commandLineArgs.blackList);
                    commandVersion.version(out, commandLineArgs, irpDatabase);
                    break;
                default:
                    return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, "Unknown command: " + command);
            }
        } catch (NamedProtocol.ProtocolNotRenderableException | UsageException | NameUnassignedException | UnknownProtocolException| FileNotFoundException | DomainViolationException ex) {
            // Exceptions likely from silly user input, just print the exception
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (OddSequenceLengthException ex) {
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_SEMANTIC_USAGE_ERROR,
                    ex.getLocalizedMessage() + ". Consider using --trailinggap.");
        } catch (ParseCancellationException | InvalidArgumentException ex) {
            // When we get here,
            // Antlr has already written a somewhat sensible error message on
            // stderr; that is good enough for now.
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (UnsupportedOperationException | IOException | IllegalArgumentException | SecurityException | InvalidNameException | IrpInvalidArgumentException | UnsupportedRepeatException ex) {
            //if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
            // Likely a programming error or fatal error in the data base. Barf.
            ex.printStackTrace();
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_FATAL_PROGRAM_FAILURE, ex.getLocalizedMessage());
        } catch (IrpParseException ex) {
            // TODO: Improve error message
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_USAGE_ERROR, "Parse error in \"" + ex.getText() + "\": " + ex.getLocalizedMessage());
        } catch (NoDecoderMatchException ex) {
            return new ProgramExitStatus(PROGRAMNAME, ProgramExitStatus.EXIT_SEMANTIC_USAGE_ERROR,
                    "No decoder matched \"" + ex.getMessage() +
                    "\". Use \"--decoder list\" to list the available decoders.");
        }
        return new ProgramExitStatus();
    }

    @Override
    public String usageString(String command) {
        return CommandHelp.usageString(command, argumentParser);
    }

    private void setupDatabase(List<String> blackList) throws IOException, UsageException, IrpParseException, UnknownProtocolException {
        if (commandLineArgs.configFile != null && commandLineArgs.irp != null)
            throw new UsageException("At most one of configfile and irp can be specified");

        irpDatabase = commandLineArgs.irp != null ? IrpDatabase.parseIrp("user_protocol", commandLineArgs.irp, "Protocol entered on the command line")
                : new IrpDatabase(commandLineArgs.configFile);
        irpDatabase.expand();
        irpDatabase.remove(blackList);
    }

    @Override
    public PrintStream getOutputStream() {
        return out;
    }

    private void setupArgParser() {
        commandLineArgs = new CommandCommonOptions();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(PROGRAMNAME);
        argumentParser.setAllowAbbreviatedOptions(true);

        // The ordering in the following lines is the order the commands
        // will be listed in the help. Keep this order in a logical order.
        // In the rest of the file, these are ordered alphabetically.
        commandHelp = new CommandHelp();
        argumentParser.addCommand(commandHelp);

        commandVersion = new CommandVersion();
        argumentParser.addCommand(commandVersion);

        commandList = new CommandList();
        argumentParser.addCommand(commandList);

        commandRender = new CommandRender();
        argumentParser.addCommand(commandRender);

        commandDecode = new CommandDecode();
        argumentParser.addCommand(commandDecode);

        commandDemodulate = new CommandDemodulate();
        argumentParser.addCommand(commandDemodulate);

        commandAnalyze = new CommandAnalyze();
        argumentParser.addCommand(commandAnalyze);

        commandCode = new CommandCode();
        argumentParser.addCommand(commandCode);

        commandBitField = new CommandBitField();
        argumentParser.addCommand(commandBitField);

        commandExpression = new CommandExpression();
        argumentParser.addCommand(commandExpression);

        commandLirc = new CommandLirc();
        argumentParser.addCommand(commandLirc);
    }

    private void setupLoggers() throws IOException {
        if (commandLineArgs.logformat != null)
            System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", commandLineArgs.logformat);
        Logger topLevelLogger = Logger.getLogger("");
        Formatter formatter = commandLineArgs.xmlLog ? new XMLFormatter() : new SimpleFormatter();
        Handler[] handlers = topLevelLogger.getHandlers();
        for (Handler handler : handlers)
            topLevelLogger.removeHandler(handler);

        String[] logclasses = commandLineArgs.logclasses.split("\\|");
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Logger> loggers = new ArrayList<>(logclasses.length);
        for (String logclass : logclasses) {
            String[] classLevel = logclass.trim().split(":");
            if (classLevel.length < 2)
                continue;

            Logger log = Logger.getLogger(classLevel[0].trim());
            loggers.add(log); // stop them from being garbage collected
            Level level = Level.parse(classLevel[1].trim().toUpperCase(Locale.US));
            log.setLevel(level);
            log.setUseParentHandlers(false);
            Handler handler = commandLineArgs.logfile != null ? new FileHandler(commandLineArgs.logfile) : new ConsoleHandler();
            handler.setLevel(level);
            handler.setFormatter(formatter);
            log.addHandler(handler);
        }

        Handler handler = commandLineArgs.logfile != null ? new FileHandler(commandLineArgs.logfile) : new ConsoleHandler();
        handler.setFormatter(formatter);
        topLevelLogger.addHandler(handler);

        handler.setLevel(commandLineArgs.logLevel);
        topLevelLogger.setLevel(commandLineArgs.logLevel);
    }

    private boolean processHelpAndDescription(String commandName) {
        try {
            JCommander jCommander = argumentParser.getCommands().get(commandName);
            AbstractCommand command = (AbstractCommand) jCommander.getObjects().get(0);
            return command.process(this);
        } catch (SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(IrpTransmogrifier.class.getName()).log(Level.SEVERE, null, ex);
            throw new ThisCannotHappenException();
        }
    }
}
