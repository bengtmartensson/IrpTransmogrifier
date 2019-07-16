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

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.analyze.AbstractDecoder;
import org.harctoolbox.analyze.Analyzer;
import org.harctoolbox.analyze.Burst;
import org.harctoolbox.analyze.Cleaner;
import org.harctoolbox.analyze.NoDecoderMatchException;
import org.harctoolbox.analyze.RepeatFinder;
import org.harctoolbox.ircore.IctImporter;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThingsLineParser;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.ircore.XmlUtils;
import org.harctoolbox.lirc.LircCommand;
import org.harctoolbox.lirc.LircConfigFile;
import org.harctoolbox.lirc.LircIrp;
import org.harctoolbox.lirc.LircRemote;
import org.w3c.dom.Document;

/**
 * This class contains a command line main routine, allowing command line access to most things in the package.
 *
 * Basically, there should not be "too much" business logic here; we construct element and call its
 * member functions, defined elsewhere.
 */
public final class IrpTransmogrifier {

    // No need to make these settable, at least not presently
    public static final String DEFAULT_CONFIG_FILE = "/IrpProtocols.xml"; // in jar-file
    public static final String DEFAULT_CHARSET = "UTF-8"; // Just for runMain
    private static final String PROGRAMNAME = Version.appName;

    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());

    static String execute(String commandLine) {
        return execute(commandLine.split("\\s+"));
    }

    static String execute(String[] args) {
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            try (PrintStream outStream = new PrintStream(outBytes, false, DEFAULT_CHARSET)) {
                IrpTransmogrifier instance = new IrpTransmogrifier(outStream);
                ProgramExitStatus status = instance.run(args);
                if (!status.isSuccess())
                    return null;

                outStream.flush();
            }
            return new String(outBytes.toByteArray(), DEFAULT_CHARSET).trim();
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    // Allow for parallel execition of several instances -- main is static.
    /**
     *
     * @param args
     * @param out
     */
    public static void main(String[] args, PrintStream out) {
        IrpTransmogrifier instance = new IrpTransmogrifier(out);
        ProgramExitStatus status = instance.run(args);
        out.close();
        status.die();
    }

    public static void main(String[] args) {
        main(args, System.out);
    }

    public static void main(String cmdLine, PrintStream printStream) {
        main(cmdLine.split("\\s+"), printStream);
    }

    private static Map<String, String> assembleParameterMap(List<String> paramStrings) throws UsageException {
        HashMap<String, String> result = new HashMap<>(paramStrings.size());
        for (String s : paramStrings) {
            String[] kvp = s.split(":");
            if (kvp.length != 2)
                throw new UsageException("Wrong syntax for parameter:value");

            result.put(kvp[0], kvp[1]);
        }
        return result;
    }

    private static Double frequencyAverage(Collection<ModulatedIrSequence> seqs) {
        if (seqs.isEmpty())
            return null;

        double sum = 0;
        for (ModulatedIrSequence seq : seqs) {
            Double freq = seq.getFrequency();
            if (freq == null)
                return null;
            sum += freq;
        }
        return sum / seqs.size();
    }

    private PrintStream out;
    private IrpDatabase irpDatabase;
    private JCommander argumentParser;
    private CommandLineArgs commandLineArgs;
    private CommandHelp commandHelp;
    private CommandVersion commandVersion;
    private CommandList commandList;
    private CommandRender commandRender;
    private CommandDecode commandDecode;
    private CommandAnalyze commandAnalyze;
    private CommandCode commandCode;
    private CommandBitField commandBitField;
    private CommandExpression commandExpression;
    private CommandLirc commandLirc;
    private CommandConvertConfig commandConvertConfig;
    private String[] originalArguments;

    public IrpTransmogrifier() {
        this(System.out);
    }

    public IrpTransmogrifier(PrintStream out) {
        this.out = out;
    }

    public ProgramExitStatus run(String cmdLine) {
        return run(cmdLine.split("\\s+"));
    }

    /**
     *
     * @param args program args
     * @return
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public ProgramExitStatus run(String[] args) {
        this.originalArguments = args.clone();
        commandLineArgs = new CommandLineArgs();
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

        commandConvertConfig = new CommandConvertConfig();
        argumentParser.addCommand(commandConvertConfig);

        try {
            argumentParser.parse(args);
        } catch (ParameterException | NumberFormatException ex) {
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getMessage());
        }

        try {
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

            if (commandLineArgs.seed != null)
                ParameterSpec.initRandom(commandLineArgs.seed);

            if (commandLineArgs.output != null)
                out = IrCoreUtils.getPrintSteam(commandLineArgs.output);
            //IrpTransmogrifier instance = new IrpTransmogrifier(ps);

            RepeatFinder.setDefaultMinRepeatLastGap(commandLineArgs.minRepeatGap); // Parallelization problem

            // Since we have help and version as subcommands, --help and --version
            // are a little off. Keep them for compatibility, and
            // map --help and --version to the subcommands
            String command = commandLineArgs.helpRequested ? "help"
                    : commandLineArgs.versionRequested ? "version"
                    : argumentParser.getParsedCommand();

            if (command == null)
                return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, "Usage: " + PROGRAMNAME + " [options] <command> [command_options]");
            else // For findbugs...
                switch (command) {
                    case "analyze":
                        analyze();
                        break;
                    case "bitfield":
                        bitfield();
                        break;
                    case "code":
                        code();
                        break;
                    case "decode":
                        decode();
                        break;
                    case "expression":
                        expression();
                        break;
                    case "help":
                        help();
                        break;
                    case "lirc":
                        lirc();
                        break;
                    case "list":
                        list();
                        break;
                    case "render":
                        render();
                        break;
                    case "version":
                        version();
                        break;
                    case "convertconfig":
                        convertConfig();
                        break;
                    default:
                        return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, "Unknown command: " + command);
                }
        } catch (UsageException | NameUnassignedException | UnknownProtocolException| FileNotFoundException | DomainViolationException ex) {
            // Exceptions likely from silly user input, just print the exception
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (OddSequenceLengthException ex) {
            return new ProgramExitStatus(IrpUtils.EXIT_SEMANTIC_USAGE_ERROR,
                    ex.getLocalizedMessage() + ". Consider using --trailinggap.");
        } catch (ParseCancellationException | InvalidArgumentException ex) {
            // When we get here,
            // Antlr has already written a somewhat sensible error message on
            // stderr; that is good enough for now.
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (UnsupportedOperationException | IOException | IllegalArgumentException | SecurityException | InvalidNameException | IrpInvalidArgumentException | UnsupportedRepeatException ex) {
            //if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
            // Likely a programming error or fatal error in the data base. Barf.
            ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_FATAL_PROGRAM_FAILURE, ex.getLocalizedMessage());
        } catch (IrpParseException ex) {
            // TODO: Improve error message
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, "Parse error in \"" + ex.getText() + "\": " + ex.getLocalizedMessage());
        } catch (NoDecoderMatchException ex) {
            return new ProgramExitStatus(IrpUtils.EXIT_SEMANTIC_USAGE_ERROR,
                    "No decoder matched \"" + commandAnalyze.decoder +
                    "\". Use \"--decoder list\" to list the available decoders.");
        }
        return new ProgramExitStatus();
    }

    private String usageString(String command) {
        StringBuilder stringBuilder = new StringBuilder(10000);
        if (command == null)
            argumentParser.usage(stringBuilder);
        else
            argumentParser.usage(command, stringBuilder);
        return stringBuilder.toString().trim();
    }

    private void help() {
        boolean finished = commandHelp.process(this);
        if (finished)
            return;

        if (commandHelp.shortForm) {
            shortUsage();
            return;
        }

        if (commandHelp.commonOptions) {
            commonOptions();
            return;
        }

        String cmd = argumentParser.getParsedCommand();
        if (commandHelp.commands != null)
            commandHelp.commands.forEach((command) -> {
                try {
                    out.println(usageString(command));
                } catch (ParameterException ex) {
                    out.println("No such command: " + command);
                }
            });
        else if (cmd == null || cmd.equals("help"))
            out.println(usageString(null));
        else
            out.println(usageString(cmd));
    }

    /**
     * Print just the common options.
     * JCommander does not support this case,
     * so this implementation is pretty gross.
     */
    private void commonOptions() {
        CommandLineArgs cla = new CommandLineArgs();
        JCommander parser = new JCommander(cla);
        StringBuilder str = new StringBuilder(2500);
        parser.usage(str);
        str.replace(0, 41, "Common options:\n"); // barf!
        out.println(str.toString().trim()); // str ends with line feed.
    }

    private void shortUsage() {
        out.println("Usage: " + PROGRAMNAME + " [options] <command> [command_options]");
        out.println("Commands:");

        List<String> commands = new ArrayList<>(argumentParser.getCommands().keySet());
        Collections.sort(commands);
        commands.forEach((cmd) -> {
            out.println("   " + padString(cmd) + argumentParser.getCommandDescription(cmd));
        });

        out.println();
        out.println("Use");
        out.println("    \"" + PROGRAMNAME + " help\" for the full syntax,");
        out.println("    \"" + PROGRAMNAME + " help <command>\" for a particular command.");
        out.println("    \"" + PROGRAMNAME + " help --common\" for the common options.");
    }

    private String padString(String name) {
        StringBuilder stringBuilder = new StringBuilder(name);
        while (stringBuilder.length() < 16)
            stringBuilder.append(" ");
        return stringBuilder.toString();
    }

    private void list() throws IOException, UsageException, IrpParseException {
        boolean finished = commandList.process(this);
        if (finished)
            return;

        setupDatabase();
        irpDatabase.expand();
        if (!commandLineArgs.quiet) {
            commandList.protocols.stream().filter((protocol) -> (irpDatabase.isAlias(protocol))).forEachOrdered((protocol) -> {
                out.println(protocol + " -> " + irpDatabase.expandAlias(protocol));
            });
        }
        List<String> list = irpDatabase.evaluateProtocols(commandList.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (list.isEmpty())
            throw new UsageException("No protocol matched.");

        for (String name : list) {
            String protocolName = irpDatabase.expandAlias(name);
            NamedProtocol protocol;
            try {
                protocol = irpDatabase.getNamedProtocol(protocolName);
                logger.log(Level.FINE, "Protocol {0} parsed", protocolName);
            } catch (UnknownProtocolException ex) {
                logger.log(Level.WARNING, "{0}", ex.getMessage());
                continue;
            } catch (InvalidNameException | NameUnassignedException | IrpInvalidArgumentException | UnsupportedRepeatException ex) {
                logger.log(Level.WARNING, "Unparsable protocol {0}", protocolName);
                continue;
            }

            if (!commandLineArgs.quiet || commandList.name)
                // Use one line for the first, relatively short items
                listProperty("name", irpDatabase.getName(protocolName));

            if (commandList.cName)
                listProperty("cName", irpDatabase.getCName(protocolName));

            if (commandList.irp)
                listProperty("irp", irpDatabase.getIrp(protocolName));

            if (commandList.normalForm)
                try {
                    // already checked it once...
                    listProperty("normal form", irpDatabase.getNormalFormIrp(protocolName, commandList.radix));
                } catch (NameUnassignedException | UnknownProtocolException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                    throw new ThisCannotHappenException(ex);
                }

            if (commandList.documentation)
                listProperty("documentation", irpDatabase.getDocumentation(protocolName));

            if (commandList.stringTree)
                listProperty("stringTree", protocol.toStringTree());

            if (commandList.is)
                listProperty("irpString", protocol.toIrpString(commandList.radix));

            if (commandList.gui)
                IrpUtils.showTreeViewer(protocol.toTreeViewer(), "Parse tree for " + protocolName);

            if (commandList.weight)
                listProperty("Weight", protocol.weight());

            if (commandList.minDiff)
                listProperty("minDiff", protocol.minDurationDiff());

            if (commandList.classify)
                listProperty("classification", protocol.classificationString());

            if (commandList.warnings)
                listProperty("warnings", protocol.warningsString());
        }
    }

    private void listProperty(String propertyName, String propertyValue) {
        if (!commandLineArgs.quiet && propertyName != null)
            out.print(propertyName + "=");
        out.println(propertyValue);
    }

    private void listProperty(String propertyName, double value) {
        listProperty(propertyName, Math.round(value));
    }

    private void listProperty(String propertyName, int value) {
        listProperty(propertyName, Integer.toString(value));
    }

    private void listProperty(String propertyName, long value) {
        listProperty(propertyName, Long.toString(value));
    }

    private void version() throws UsageException, IOException, IrpParseException {
        if (commandVersion.shortForm || commandLineArgs.quiet)
            out.println(Version.version);
        else {
            out.println(Version.versionString);
            setupDatabase();
            irpDatabase.expand();
            out.println("Database: " + (commandLineArgs.configFile != null ? commandLineArgs.configFile : "")
                    + " version: " + irpDatabase.getConfigFileVersion());

            out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
            out.println();
            out.println(Version.licenseString);
        }
    }

    private void convertConfig() throws IOException, UsageException, IrpParseException {
        boolean finished = commandConvertConfig.process(this);
        if (finished)
            return;

        setupDatabase(); // checks exactly one of -c, -i given
        if (commandConvertConfig.checkSorted)
            irpDatabase.checkSorted();

        if (commandLineArgs.iniFile != null)
            XmlUtils.printDOM(out, irpDatabase.toDocument(), commandLineArgs.encoding, "{" + IrpDatabase.IRP_PROTOCOL_NS + "}irp");
        else
            irpDatabase.printAsIni(out);

        if (commandLineArgs.output != null)
            logger.log(Level.INFO, "Wrote {0}", commandLineArgs.output);
    }

    private void code() throws UsageException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        boolean finished = commandCode.process(this);
        if (finished)
            return;

        if (commandCode.directory != null && commandLineArgs.output != null)
            throw new UsageException("The --output and the --directory options are mutually exclusive.");

        setupDatabase();
        irpDatabase.expand();
        List<String> protocolNames = irpDatabase.evaluateProtocols(commandCode.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolNames.isEmpty())
            throw new UsageException("No protocols matched (forgot --regexp?)");

        if (protocolNames.size() > 1 && commandCode.directory == null)
            logger.warning("Several protocol will be concatenated in one file. Consider using --directory.");

        STCodeGenerator.setStDir(commandCode.stDir);
        for (String target : commandCode.target)
            // Hardcoded selection of technologies for different targets
            if (target.equals("?"))
                listTargets(out);
            else if (target.equalsIgnoreCase("xml"))
                createXmlProtocols(protocolNames, commandLineArgs.encoding);
            else if (target.equalsIgnoreCase("dump"))
                code(protocolNames, new DumpCodeGenerator());
            else {
                if (!new File(commandCode.stDir).isDirectory())
                    throw new IOException("Cannot find stdir = " + new File(commandCode.stDir).getCanonicalPath());
                code(protocolNames, target);
            }
    }

    private void code(Collection<String> protocolNames, String pattern) throws UsageException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        File[] targets = IrCoreUtils.filesInDirMatchingRegExp(new File(commandCode.stDir), pattern + STCodeGenerator.ST_GROUP_FILEEXTENSION);
        if (targets.length > 1 && commandCode.directory == null)
            logger.warning("Several targets will be concatenated in one file. Consider using --directory.");
       for (File target : targets) {
            CodeGenerator codeGenerator;
            try {
                codeGenerator = new STCodeGenerator(target);
            } catch (FileNotFoundException ex) {
                throw new UsageException("Target " + target.getName() + " not available.  Available targets: " + String.join(" ", listTargets()));
            }
            code(protocolNames, codeGenerator);
        }
    }

    private void code(Collection<String> protocolNames, CodeGenerator codeGenerator) throws UsageException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        Map<String, String> parameters = assembleParameterMap(commandCode.parameters);
        if (commandCode.directory != null)
            codeGenerator.generate(protocolNames, irpDatabase, new File(commandCode.directory), commandCode.inspect, parameters,
                    commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance,
                    getClass().getSimpleName(), Version.version, String.join(" ", originalArguments));
        else
            codeGenerator.generate(protocolNames, irpDatabase, out, commandCode.inspect, parameters,
                    commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance,
                    getClass().getSimpleName(), Version.version, String.join(" ", this.originalArguments));

    }

    private List<String> listTargets() throws IOException {
        List<String> targets = STCodeGenerator.listTargets();
        targets.add("xml");
        targets.add("dump");
        targets.sort(String.CASE_INSENSITIVE_ORDER);
        return targets;
    }

    private void listTargets(PrintStream stream) throws IOException {
        stream.println(String.join(" ", listTargets()));
    }

    private void createXmlProtocols(List<String> protocolNames, String encoding) {
        Document document = irpDatabase.toDocument(protocolNames);
        XmlUtils.printDOM(out, document, encoding, "Irp Documentation");
    }

    private void render(NamedProtocol protocol) throws OddSequenceLengthException, DomainViolationException, IrpInvalidArgumentException, NameUnassignedException, UsageException, InvalidNameException {
        NameEngine nameEngine = !commandRender.nameEngine.isEmpty() ? commandRender.nameEngine
                : commandRender.random ? new NameEngine(protocol.randomParameters())
                        : new NameEngine();
        if (commandRender.random)
            logger.log(Level.INFO, nameEngine.toString());

        if (commandRender.printParameters)
            out.println(nameEngine.toString());

        if (!commandRender.pronto && !commandRender.raw && !commandRender.rawWithoutSigns && !commandRender.printParameters)
            logger.warning("No output requested. Use either --raw, --raw-without-signs, --pronto, or --printparameters to get output.");
        IrSignal irSignal = protocol.toIrSignal(nameEngine);

        if (commandRender.count != null) {
            if (commandRender.numberRepeats != null)
                throw new UsageException("Can only specify one of --number-repeats and --count.");
            renderPrint(irSignal.toModulatedIrSequence(commandRender.count));
        } else if (commandRender.numberRepeats != null)
            renderPrint(irSignal.toModulatedIrSequence(true, commandRender.numberRepeats, true));
        else
            renderPrint(irSignal);
    }

    private void renderPrint(IrSignal irSignal) {
        if (commandRender.raw)
            out.println(irSignal.toString(true));
        if (commandRender.rawWithoutSigns)
            out.println(irSignal.toString(false));
        if (commandRender.pronto)
            out.println(Pronto.toString(irSignal));
    }

    private void renderPrint(ModulatedIrSequence irSequence) {
        if (commandRender.raw)
            out.println(irSequence.toString(true));
        if (commandRender.rawWithoutSigns)
            out.println(irSequence.toString(false));
        if (commandRender.pronto)
            out.println(Pronto.toString(new IrSignal(irSequence)));
    }

    private void render() throws UsageException, IOException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        boolean finished = commandRender.process(this);
        if (finished)
            return;

        if (commandLineArgs.irp == null && (commandRender.random != commandRender.nameEngine.isEmpty()))
            throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

        if (commandLineArgs.irp != null) {
            if (!commandRender.protocols.isEmpty())
                throw new UsageException("Cannot not use --irp together with named protocols");
        }
        setupDatabase();
        irpDatabase.expand();
        List<String> list = irpDatabase.evaluateProtocols(commandRender.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (list.isEmpty())
            throw new UsageException("No protocol matched.");
        for (String proto : list) {
            //logger.info(proto);
            NamedProtocol protocol = irpDatabase.getNamedProtocolExpandAlias(proto);
            render(protocol);
        }

    }

    private void analyze() throws IrpInvalidArgumentException, UsageException, InvalidArgumentException, IOException, NoDecoderMatchException {
        boolean finished = commandAnalyze.process(this);
        if (finished)
            return;

        if (commandAnalyze.allDecodes && commandAnalyze.decoder != null)
            throw new UsageException("Cannot use both --alldecodes and --decode.");
        if (commandAnalyze.allDecodes && commandAnalyze.girr)
            throw new UsageException("Cannot use both --alldecodes and --girr.");
        if (commandAnalyze.bitUsage && (commandAnalyze.allDecodes || commandAnalyze.eliminateVars))
            throw new UsageException("Bit usage report not possible together with --all or --eliminate-vars");
        if (commandAnalyze.parameterTable && commandAnalyze.eliminateVars)
            throw new UsageException("Parameter table is meaninless together with --eliminate-vars");

        if (IrCoreUtils.numberTrue(commandAnalyze.input != null, commandAnalyze.namedInput != null, commandAnalyze.args != null) != 1)
            throw new UsageException("Must use exactly one of --input, --namedinput, and non-empty arguments");

        if (commandAnalyze.input != null) {
            ThingsLineParser<ModulatedIrSequence> irSignalParser = new ThingsLineParser<>(
                    (List<String> line) -> { return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(commandAnalyze.frequency, commandAnalyze.trailingGap); }
            );
            List<ModulatedIrSequence> modSeqs = irSignalParser.readThings(commandAnalyze.input, commandLineArgs.encoding, false);
            analyze(modSeqs, frequencyAverage(modSeqs));
        } else if (commandAnalyze.namedInput != null) {
            try {
                Map<String, ModulatedIrSequence> modSequences = IctImporter.parse(commandAnalyze.namedInput, commandLineArgs.encoding);
                analyze(modSequences);
            } catch (ParseException ex) {
                logger.log(Level.INFO, "Parsing of {0} as ict failed", commandAnalyze.namedInput);
                ThingsLineParser<ModulatedIrSequence> thingsLineParser = new ThingsLineParser<>(
                        (List<String> line) -> { return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(commandAnalyze.frequency, commandAnalyze.trailingGap); }
                );
                Map<String, ModulatedIrSequence> signals = thingsLineParser.readNamedThings(commandAnalyze.namedInput, commandLineArgs.encoding);
                if (signals.isEmpty())
                    throw new InvalidArgumentException("No parseable sequences found.");
                analyze(signals);
            }
        } else {
            MultiParser parser = MultiParser.newIrCoreParser(commandAnalyze.args);
            if (commandAnalyze.introRepeatEnding) {
                IrSignal irSignal = (commandAnalyze.chop != null)
                        ? parser.toIrSignalChop(commandAnalyze.frequency, commandAnalyze.chop)
                        : parser.toIrSignal(commandAnalyze.frequency, commandAnalyze.trailingGap);
                analyze(irSignal);
            } else if (commandAnalyze.chop != null) {
                List<IrSequence> list = parser.toListChop(commandAnalyze.chop, commandAnalyze.trailingGap);
                analyze(list);
            } else {
                List<IrSequence> list = parser.toList(commandAnalyze.trailingGap);
                if (list.size() > 1)
                    analyze(list);
                else {
                    IrSignal irSignal = parser.toIrSignal(commandAnalyze.frequency, commandAnalyze.trailingGap);
                    if (irSignal != null)
                        analyze(irSignal);
                    else
                        throw new UsageException("Invalid signal, neither valid as Pronto nor as raw.");
                }
            }
        }
    }

    private void analyze(IrSignal irSignal) throws NoDecoderMatchException, InvalidArgumentException {
        Analyzer analyzer;
        Double freq = possiblyOverrideWithAnalyzeFrequency(irSignal.getFrequency());
        if (commandAnalyze.repeatFinder || commandAnalyze.dumpRepeatfinder) {
            IrSequence irSequence = irSignal.toModulatedIrSequence();
            analyzer = new Analyzer(irSequence, freq, true, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        } else
            analyzer = new Analyzer(irSignal.setFrequency(freq), commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer, null);
    }

    private void analyze(Map<String, ModulatedIrSequence> modulatedIrSequences) throws NoDecoderMatchException, InvalidArgumentException {
        Map<String, IrSequence> irSequences = new LinkedHashMap<>(modulatedIrSequences.size());
        irSequences.putAll(modulatedIrSequences);
        Double frequency = possiblyOverrideWithAnalyzeFrequency(frequencyAverage(modulatedIrSequences.values()));
        analyze(irSequences, frequency);
    }

    private void analyze(Map<String, IrSequence> irSequences, Double frequency) throws NoDecoderMatchException, InvalidArgumentException {
        if (irSequences.isEmpty())
            throw new InvalidArgumentException("No parseable sequences found.");
        Analyzer analyzer = new Analyzer(irSequences.values(), possiblyOverrideWithAnalyzeFrequency(frequency), commandAnalyze.repeatFinder || commandAnalyze.dumpRepeatfinder, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer, irSequences.keySet().toArray(new String[irSequences.size()]));
    }

    private void analyze(List<? extends IrSequence> irSequences) throws NoDecoderMatchException, InvalidArgumentException {
        analyze(irSequences, commandAnalyze.frequency);
    }

    private void analyze(List<? extends IrSequence> irSequences, Double frequency) throws NoDecoderMatchException, InvalidArgumentException {
        if (irSequences.isEmpty())
            throw new InvalidArgumentException("No parseable sequences found.");
        Analyzer analyzer = new Analyzer(irSequences, possiblyOverrideWithAnalyzeFrequency(frequency), commandAnalyze.repeatFinder || commandAnalyze.dumpRepeatfinder, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer, null);
    }

    private void analyze(Analyzer analyzer, String[] names) throws NoDecoderMatchException {
        Burst.Preferences burstPrefs = new Burst.Preferences(commandAnalyze.maxRoundingError, commandAnalyze.maxUnits, commandAnalyze.maxMicroSeconds);
        Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(analyzer.getFrequency(), commandAnalyze.timeBase,
                commandAnalyze.lsb ? BitDirection.lsb : BitDirection.msb,
                commandAnalyze.extent, commandAnalyze.parameterWidths, commandAnalyze.maxParameterWidth, commandAnalyze.invert,
                burstPrefs);

        if (commandAnalyze.statistics) {
            analyzer.printStatistics(out, params);
            out.println();
        }

        if (commandAnalyze.clean) {
            for (int i = 0; i < analyzer.getNoSequences(); i++) {
                if (analyzer.getNoSequences() > 1)
                    out.print("#" + i + ":\t");
                out.println(analyzer.cleanedIrSequence(i).toString(true));
                if (commandAnalyze.statistics)
                    out.println(analyzer.toTimingsString(i));
            }
        }
        if (commandAnalyze.dumpRepeatfinder) {
            for (int i = 0; i < analyzer.getNoSequences(); i++) {
                if (analyzer.getNoSequences() > 1)
                    out.print("#" + i + ":\t");
                out.println(analyzer.repeatReducedIrSignal(i).toString(true));
                out.println("RepeatFinderData: " + analyzer.repeatFinderData(i).toString());

            }
        }

        if (commandAnalyze.allDecodes) {
            List<List<Protocol>> protocols = analyzer.searchAllProtocols(params, commandAnalyze.decoder, commandLineArgs.regexp);
            int noSignal = 0;
            for (List<Protocol> protocolList : protocols) {
                if (protocols.size() > 1)
                    out.print((names != null ? names[noSignal] : "#" + noSignal) + ":\t");
                if (commandAnalyze.statistics)
                    out.println(analyzer.toTimingsString(noSignal));
                protocolList.forEach((protocol) -> {
                    printAnalyzedProtocol(protocol, commandAnalyze.radix, params.isPreferPeriods(), true, true);
                });
                noSignal++;
            }
        } else {
            List<Protocol> protocols = analyzer.searchBestProtocol(params, commandAnalyze.decoder, commandLineArgs.regexp);

            if (commandAnalyze.girr) {
                System.err.println("NOTE: --girr supresses all other output!");
                Document doc = ProtocolListDomFactory.protocolListToDom(analyzer, protocols, names, commandAnalyze.radix);
                XmlUtils.printDOM(out, doc, commandLineArgs.encoding, "");
                return;
            }

            int maxNameLength = IrCoreUtils.maxLength(names);
            for (int i = 0; i < protocols.size(); i++) {
                if (protocols.size() > 1)
                    out.print(names != null
                            ? (names[i] + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - names[i].length() + 1)))
                            : ("#" + i + "\t"));
                if (commandAnalyze.statistics)
                    out.println(analyzer.toTimingsString(i));
                printAnalyzedProtocol(protocols.get(i), commandAnalyze.radix, params.isPreferPeriods(), commandAnalyze.statistics, commandAnalyze.timings);
            }

            if (commandAnalyze.bitUsage) {
                out.println();
                out.println("Bit usage analysis:");
                Map<String, BitCounter> bitStatistics = BitCounter.scrutinizeProtocols(protocols);
                bitStatistics.entrySet().forEach((kvp) -> {
                    out.println(kvp.getKey() + "\t" + kvp.getValue().toString() + (commandAnalyze.lsb ? " (note: lsb-first)" : ""));
                });
                //#if duplicates
                try {
                    DuplicateFinder duplicateFinder = new DuplicateFinder(protocols, bitStatistics);
                    out.println("Duplicates analysis:");
                    Map<String, DuplicateFinder.DuplicateCollection> duplicates = duplicateFinder.getDuplicates();
                    duplicates.entrySet().forEach((kvp) -> {
                        out.println(kvp.getKey() + "\t" + kvp.getValue().toString()
                                + "\t" + kvp.getValue().getRecommendedParameterWidthsAsString()
                                + (commandAnalyze.lsb ? " (note: lsb-first)" : ""));
                    });
                } catch (NameUnassignedException ex) {
                    logger.warning("Duplicates analysis not possible due to different variables in the protocols.");
                }
                //#endif duplicates
            }

            if (commandAnalyze.parameterTable) {
                out.println();
                out.println("Parameter table:");
                for (int i = 0; i < protocols.size(); i++) {
                    if (protocols.size() > 1)
                        out.print(names != null
                                ? (names[i] + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - names[i].length() + 1)))
                                :  ("#" + i + "\t"));
                    Protocol protocol = protocols.get(i);
                    NameEngine definitions = protocol.getDefinitions();
                    for (Map.Entry<String, Expression> definition : definitions) {
                        String name = definition.getKey();
                        int length = protocol.guessParameterLength(name);
                        Number num = definition.getValue().toNumber();
                        out.print("\t" + num.formatIntegerWithLeadingZeros(commandAnalyze.radix, length));
                    }
                    out.println();
                }
            }
        }
    }

    private void decode() throws IrpInvalidArgumentException, IOException, UsageException, InvalidArgumentException, IrpParseException {
        boolean finished = commandDecode.process(this);
        if (finished)
            return;

        if (IrCoreUtils.numberTrue(commandDecode.input != null, commandDecode.namedInput != null, commandDecode.args != null) != 1)
            throw new UsageException("Must use exactly one of --input, --namedinput, and non-empty arguments");

        setupDatabase();
        irpDatabase.expand();
        List<String> protocolNamePatterns = commandDecode.protocol == null ? null : Arrays.asList(commandDecode.protocol.split(","));
        List<String> protocolsNames = irpDatabase.evaluateProtocols(protocolNamePatterns, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolsNames.isEmpty())
            throw new UsageException("No protocol given or matched.");

        Decoder decoder = new Decoder(irpDatabase, protocolsNames);
        if (commandDecode.input != null) {
            ThingsLineParser<IrSignal> irSignalParser = new ThingsLineParser<>((List<String> line) -> {
                    return (MultiParser.newIrCoreParser(line)).toIrSignal(commandDecode.frequency, commandDecode.trailingGap);
            });
            List<IrSignal> signals = irSignalParser.readThings(commandDecode.input, commandLineArgs.encoding, false);
            for (IrSignal irSignal : signals)
                decode(decoder, irSignal.setFrequency(commandDecode.frequency), null, 0);
        } else if (commandDecode.namedInput != null) {
            try {
                Map<String, ModulatedIrSequence> modSequences = IctImporter.parse(commandDecode.namedInput, commandLineArgs.encoding);
                int maxNameLength = IrCoreUtils.maxLength(modSequences.keySet());
                for (Map.Entry<String, ModulatedIrSequence> kvp : modSequences.entrySet()) {
                    ModulatedIrSequence modSeq = kvp.getValue().setFrequency(commandDecode.frequency);
                    decode(decoder, new IrSignal(modSeq), kvp.getKey(), maxNameLength);
                }
            } catch (ParseException ex) {
                ThingsLineParser<IrSignal> irSignalParser = new ThingsLineParser<>((List<String> line) -> {
                    return (MultiParser.newIrCoreParser(line)).toIrSignal(commandDecode.frequency, commandDecode.trailingGap);
                });
                Map<String, IrSignal> signals = irSignalParser.readNamedThings(commandDecode.namedInput, commandLineArgs.encoding);
                int maxNameLength = IrCoreUtils.maxLength(signals.keySet());
                for (Map.Entry<String, IrSignal> kvp : signals.entrySet())
                    decode(decoder, kvp.getValue().setFrequency(commandDecode.frequency), kvp.getKey(), maxNameLength);
            }
        } else {
            MultiParser prontoRawParser = MultiParser.newIrCoreParser(commandDecode.args);
            IrSignal irSignal = prontoRawParser.toIrSignal(commandDecode.frequency, commandDecode.trailingGap);
            if (irSignal == null)
                throw new UsageException("Could not parse as IrSignal: " + String.join(" ", commandDecode.args));
            decode(decoder, irSignal.setFrequency(commandDecode.frequency), null, 0);
        }
    }

    private void decode(Decoder decoder, IrSignal irSignal, String name, int maxNameLength) throws UsageException, InvalidArgumentException {
        Objects.requireNonNull(irSignal, "irSignal must be non-null");
        if (! commandDecode.strict && (irSignal.introOnly() || irSignal.repeatOnly())) {
            ModulatedIrSequence sequence = irSignal.toModulatedIrSequence();
            if (commandDecode.repeatFinder) {
                RepeatFinder repeatFinder = new RepeatFinder(sequence, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);

                IrSignal fixedIrSignal = repeatFinder.toIrSignalClean(sequence);
                if (commandDecode.dumpRepeatfinder) {
                    out.println("RepeatReduced: " + irSignal);
                    out.println("RepeatData: " + repeatFinder.getRepeatFinderData());
                }
                decodeIrSignal(decoder, fixedIrSignal, name, maxNameLength);
            } else {
                decodeIrSequence(decoder, sequence, name, maxNameLength);
            }
        } else {
            decodeIrSignal(decoder, irSignal, name, maxNameLength);
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void decodeIrSequence(Decoder decoder, ModulatedIrSequence irSequence, String name, int maxNameLength) throws UsageException, InvalidArgumentException {
        if (commandDecode.cleaner) {
            irSequence = Cleaner.clean(irSequence, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            logger.log(Level.INFO, "Cleansed signal: {0}", irSequence.toString(true));
        }

        Decoder.DecoderParameters decoderParams = newDecoderParameters();
        Decoder.DecodeTree decodes = decoder.decode(irSequence, decoderParams);
        printDecodes(decodes, name, maxNameLength);
    }

    private Decoder.DecoderParameters newDecoderParameters() {
        return new Decoder.DecoderParameters(commandDecode.strict, commandDecode.noPreferOver,
                ! commandDecode.keepDefaultedParameters, commandDecode.recursive, commandLineArgs.frequencyTolerance,
                commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.minLeadout);
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void decodeIrSignal(Decoder decoder, IrSignal irSignal, String name, int maxNameLength) throws UsageException, InvalidArgumentException {
        if (commandDecode.cleaner) {
            irSignal = Cleaner.clean(irSignal, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            logger.log(Level.INFO, "Cleansed signal: {0}", irSignal.toString(true));
        }
        Decoder.DecoderParameters params = newDecoderParameters();
        Map<String, Decoder.Decode> decodes = decoder.decode(irSignal, params);
        printDecodes(decodes, name, maxNameLength);
    }

    private void printDecodes(Map<String, Decoder.Decode> decodes, String name, int maxNameLength) {
        if (name != null)
            out.print(name + ":" + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - name.length() + 1)));

        if (decodes == null || decodes.isEmpty()) {
            out.println();
            return;
        }

        decodes.values().forEach((kvp) -> {
            out.println("\t" + kvp.toString(commandDecode.radix, commandLineArgs.tsvOptimize ? "\t" : " "));
        });
    }

    private void printDecodes(Decoder.DecodeTree decodes, String name, int maxNameLength) {
        if (name != null)
            out.print(name + ":" + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - name.length() + 1)));

        if (decodes == null || decodes.isEmpty())
            out.println();
        else {
            boolean first = true;
            for (Decoder.TrunkDecodeTree decode : decodes) {
                printDecodes(decode, first ? 0 : maxNameLength + 2);
                first = false;
            }
        }
    }

    private void printDecodes(Decoder.TrunkDecodeTree decode, int indent) {
        if (commandLineArgs.tsvOptimize)
            out.println((indent > 0 ? "\t" : "") + decode.toString(commandDecode.radix, "\t"));
        else
            out.println(IrCoreUtils.spaces(indent) + decode.toString(commandDecode.radix, " "));
    }

    private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods, boolean printWeight, boolean printTimings) {
        if (protocol == null) {
            out.println();
            return;
        }

        Protocol actualProtocol = commandAnalyze.eliminateVars ? protocol.substituteConstantVariables() : protocol;
        out.println(actualProtocol.toIrpString(radix, usePeriods, commandLineArgs.tsvOptimize));
        if (printWeight)
            out.println("weight = " + protocol.weight() + "\t" + protocol.getDecoderName());
        if (printTimings) {
            try {
                IrSignal irSignal = protocol.toIrSignal(new NameEngine());
                int introDuration = (int) irSignal.getIntroSequence().getTotalDuration();
                int repeatDuration = (int) irSignal.getRepeatSequence().getTotalDuration();
                int endingDuration = (int) irSignal.getEndingSequence().getTotalDuration();
                out.println("timings = (" + introDuration + ", " + repeatDuration + ", " + endingDuration + ").");
            } catch (DomainViolationException | NameUnassignedException | IrpInvalidArgumentException | InvalidNameException ex) {
                throw new ThisCannotHappenException(ex);
            }
        }
    }

    private void expression() throws FileNotFoundException, NameUnassignedException, IrpParseException {
        boolean finished = commandExpression.process(this);
        if (finished)
            return;

        NameEngine nameEngine = commandExpression.nameEngine;
        String text = String.join(" ", commandExpression.expressions).trim();
        Expression expression = Expression.newExpressionEOF(text);
        long result = expression.toLong(nameEngine);
        out.println(Long.toString(result, commandExpression.radix));
        if (commandExpression.stringTree)
            out.println(expression.toStringTree());

        if (commandExpression.xml != null) {
            XmlUtils.printDOM(commandExpression.xml, expression.toDocument(), commandLineArgs.encoding, null);
            logger.log(Level.INFO, "Wrote {0}", commandExpression.xml);
        }

        if (commandExpression.gui)
            IrpUtils.showTreeViewer(expression.toTreeViewer(), text + "=" + result);
    }

    private void bitfield() throws FileNotFoundException, UsageException, NameUnassignedException {
        boolean finished = commandBitField.process(this);
        if (finished)
            return;

        NameEngine nameEngine = commandBitField.nameEngine;
        String text = String.join("", commandBitField.bitField).trim();
        BitField bitfield = BitField.newBitField(text);
        long result = bitfield.toLong(nameEngine);
        listProperty("integer value", result);
        if (bitfield instanceof FiniteBitField) {
            FiniteBitField fbf = (FiniteBitField) bitfield;
            listProperty("bitfield", fbf.toBinaryString(nameEngine, commandBitField.lsb));
        }

        if (commandBitField.xml != null) {
            XmlUtils.printDOM(IrCoreUtils.getPrintSteam(commandBitField.xml), bitfield.toDocument(), commandLineArgs.encoding, null);
            logger.log(Level.INFO, "Wrote {0}", commandBitField.xml);
        }
//        if (commandBitField.gui)
//            IrpUtils.showTreeViewer(bitfield.toTreeViewer(), text + "=" + result);
    }

    private void lirc() throws IOException {
        boolean finished = commandLirc.process(this);
        if (finished)
            return;

        List<LircRemote> list;
        if (commandLirc.files.isEmpty())
            list = LircConfigFile.readRemotes(new InputStreamReader(System.in, commandLineArgs.encoding));
        else {
            list = new ArrayList<>(commandLirc.files.size());
            for (String f : commandLirc.files) {
                list.addAll(LircConfigFile.readRemotes(f, commandLineArgs.encoding));
            }
        }

        for (LircRemote rem : list) {
            out.print(rem.getName() + ":\t");
            try {
                out.println(LircIrp.toProtocol(rem).toIrpString(commandLirc.radix, false));
            } catch (LircIrp.RawRemoteException ex) {
                out.println("raw remote");
            } catch (LircIrp.LircCodeRemoteException ex) {
                out.println("lirc code remote, does not contain relevant information.");
            } catch (NonUniqueBitCodeException ex) {
                out.println("Non-unique bitcodes");
            }
            if (commandLirc.commands) {
                for (LircCommand cmd : rem.getCommands()) {
                    out.print(cmd.getName() + ":\t");
                    cmd.getCodes().forEach((x) -> {
                        out.print(IrCoreUtils.radixPrefix(commandLirc.radix) + Long.toUnsignedString(x, commandLirc.radix) + " ");
                    });
                    out.println();
                }
            }
        }
    }

    private void setupDatabase() throws IOException, UsageException {
        if (IrCoreUtils.numberTrue(commandLineArgs.configFile != null, commandLineArgs.irp != null, commandLineArgs.iniFile != null) > 1)
            throw new UsageException("At most one of inifile, configfile, and irp can be specified");

        irpDatabase = commandLineArgs.iniFile != null ? IrpDatabase.readIni(commandLineArgs.iniFile)
                : commandLineArgs.irp != null         ? IrpDatabase.parseIrp("user_protocol", commandLineArgs.irp, "Protocol entered on the command line")
                : new IrpDatabase(commandLineArgs.configFile);
    }

    private Double possiblyOverrideWithAnalyzeFrequency(Double frequency) {
        return commandAnalyze.frequency != null ? commandAnalyze.frequency : frequency;
    }

    public static class LevelParser implements IStringConverter<Level> { // MUST be public

        @Override
        public Level convert(String value) {
            try {
                return Level.parse(value.toUpperCase(Locale.US));
            } catch (IllegalArgumentException ex) {
                throw new ParameterException(ex);
            }
        }
    }

    public static class FrequencyParser implements IStringConverter<Double> { // MUST be public

        @Override
        public Double convert(String value) {
            return value.toLowerCase(Locale.US).endsWith("k")
                    ? IrCoreUtils.khz2Hz(Double.parseDouble(value.substring(0, value.length() - 1)))
                    : Double.parseDouble(value);
        }
    }

    public static class LessThanOne implements IParameterValidator { // MUST be public

        @Override
        public void validate(String name, String value) throws ParameterException {
            try {
            double d = Double.parseDouble(value);
            if (d < 0 || d >= 1)
                throw new ParameterException("Parameter " + name + " must be  be between 0 and 1 (found " + value +")");
            } catch (NumberFormatException ex) {
                throw new ParameterException("Parameter " + name + " must be a double (found " + value +")");
            }
        }
    }

    public static class NameEngineParser implements IStringConverter<NameEngine> { // MUST be public

        @Override
        public NameEngine convert(String value) {
            try {
                return NameEngine.parseLoose(value);
            } catch (ParseCancellationException ex) {
                throw new ParameterException("Parse error as name engine: \"" + value + "\"");
            } catch (IllegalArgumentException ex) {
                throw new ParameterException(ex);
            }
        }
    }

    // The reaining classes are ordered alphabetically
    private final static class CommandLineArgs {

        // JCommander does not know about our defaults being null, so handle this explicitly-
        @Parameter(names = {"-a", "--absolutetolerance"},
                description = "Absolute tolerance in microseconds, used when comparing durations. Default: " + IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE + ".")
        private Double absoluteTolerance = null;

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format. Default is the one in the jar file.")
        private String configFile = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used in generated output.")
        private String encoding = "UTF-8";

        @Parameter(names = {"-f", "--frequencytolerance"}, converter = FrequencyParser.class,
                description = "Frequency tolerance in Hz. Negative disables frequency check. Default: " + IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE + ".")
        private Double frequencyTolerance = null;

        @Parameter(names = {"-g", "--minrepeatgap"}, description = "Minumum gap at end of repetition")
        private double minRepeatGap = IrCoreUtils.DEFAULT_MIN_REPEAT_LAST_GAP;

        @Parameter(names = {"-h", "--help", "-?"}, help = true, description = "Display help message. Deprecated; use the command \"help\" instead.")
        private boolean helpRequested = false;

        @Parameter(names = {      "--ini", "--inifile"},
                description = "Pathname of IRP database file in ini format. "
                + "If not specified, an XML config file (using --configfile) will be used instead.")
        private String iniFile = null;//"src/main/config/IrpProtocols.ini";

        @Parameter(names = { "-i", "--irp" }, description = "Explicit IRP string to use as protocol definition.")
        private String irp = null;

        @Parameter(names = {"--logclasses"}, description = "List of (fully qualified) classes and their log levels.")
        private String logclasses = "";

        @Parameter(names = {"-L", "--logfile"}, description = "Log file. If empty, log to stderr.")
        private String logfile = null;

        @Parameter(names = {"-F", "--logformat"}, description = "Log format, as in class java.util.logging.SimpleFormatter.")
        private String logformat = "[%2$s] %4$s: %5$s%n";

        @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
                description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
        private Level logLevel = Level.WARNING;

        @Parameter(names = { "--min-leadout"},
                description = "Threshold for leadout when decoding. Default: " + IrCoreUtils.DEFAULT_MINIMUM_LEADOUT + ".")
        private Double minLeadout = null;

        @Parameter(names = { "-o", "--output" }, description = "Name of output file. Default: stdout.")
        private String output = null;

        @Parameter(names = { "-q", "--quiet" }, description = "Quitest possible operation, typically to be used from scripts.")
        private boolean quiet = false;

        @Parameter(names = {"-r", "--relativetolerance"}, validateWith = LessThanOne.class,
                description = "Relative tolerance as a number < 1. Default: " + IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE + ".")
        private Double relativeTolerance = null;

        @Parameter(names = { "--regexp" }, description = "Interpret protocol/decoder argument as regular expressions.")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the protocols alphabetically on output.")
        private boolean sort = false;

        @Parameter(names = {"--seed"},
                description = "Set seed for the pseudo random number generation. If not specified, will be random, different between program invocations.")
        private Long seed = null;

        @Parameter(names = {"-t", "--tsv", "--csv"}, description = "Use tabs in output to optimize for the import in spreadsheet programs as cvs.")
        private boolean tsvOptimize = false;

        @Parameter(names = {"-u", "--url-decode"}, description = "URL-decode protocol names, (understanding %20 for example).")
        private boolean urlDecode = false;

        @Parameter(names = {"-v", "--version"}, description = "Report version. Deprecated; use the command \"version\" instead.")
        private boolean versionRequested = false;

        @Parameter(names = {"-x", "--xmllog"}, description = "Write the log in XML format.")
        private boolean xmlLog = false;
    }

    @Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal: tries to find an IRP form with parameters")
    private static class CommandAnalyze extends MyCommand {

        @Parameter(names = { "-a", "--all" }, description = "List all decoder outcomes, instead of only the one with lowest weight.")
        private boolean allDecodes = false;

        @Parameter(names = { "-b", "--bit-usage" }, description = "Create bit usage report. (Not with --all)")
        private boolean bitUsage = false;

        @Parameter(names = { "-c", "--chop" }, description = "Chop input sequence into several using threshold (in milliseconds) given as argument.")
        private Integer chop = null;

        @Parameter(names = { "-C", "--clean" }, description = "Output the cleaned sequence(s).")
        private boolean clean = false;

        @Parameter(names = { "-d", "--decoder" }, description = "Use only the decoders matching argument (regular expression, or prefix). "
                + "Use the argument \"list\" to list the available decoders.")
        private String decoder = null;

        @Parameter(names = { "-e", "--extent" }, description = "Output the last gap as an extent.")
        private boolean extent = false;

        @Parameter(names = {       "--eliminate-vars" }, description = "Eliminate variables in output form")
        private boolean eliminateVars = false;

        @Parameter(names = { "-f", "--frequency"}, converter = FrequencyParser.class, description = "Modulation frequency of raw signal.")
        private Double frequency = null;

        @Parameter(names = { "-g", "--girr"}, description = "Generate Girr file.")
        private boolean girr = false;

        @Parameter(names = { "-i", "--input"}, description = "File/URL from which to take inputs, one sequence per line.")
        private String input = null;

        @Parameter(names = { "-I", "--invert"}, description = "Invert the order in bitspec.")
        private boolean invert = false;

        @Parameter(names = { "--ire", "--intro-repeat-ending"}, description = "Consider the argument as begin, repeat, and ending sequence.")
        private boolean introRepeatEnding = false;

        @Parameter(names = { "-l", "--lsb" }, description = "Force lsb-first bitorder for the parameters.")
        private boolean lsb = false;

        @Parameter(names = { "-m", "--maxunits" }, description = "Maximal multiplier of time unit in durations.")
        private double maxUnits = Burst.Preferences.DEFAULT_MAX_UNITS;

        @Parameter(names = { "-n", "--namedinput"}, description = "File/URL from which to take inputs, one line name, data one line.")
        private String namedInput = null;

        @Parameter(names = { "-p", "--parametertable" }, description = "Create parameter table.")
        private boolean parameterTable = false;

        @Parameter(names = { "-u", "--maxmicroseconds" }, description = "Maximal duration to be expressed as micro seconds.")
        private double maxMicroSeconds = Burst.Preferences.DEFAULT_MAX_MICROSECONDS;

        @Parameter(names = {      "--maxroundingerror" }, description = "Maximal rounding errors for expressing as multiple of time unit.")
        private double maxRoundingError = Burst.Preferences.DEFAULT_MAX_ROUNDING_ERROR;

        @Parameter(names = { "-M", "--maxparameterwidth" }, description = "Maximal parameter width.")
        private int maxParameterWidth = 63;

        @Parameter(names = { "-w", "--parameterwidths" }, description = "Comma separated list of parameter widths.")
        private List<Integer> parameterWidths = new ArrayList<>(4);

        @Parameter(names = { "-r", "--repeatfinder" }, description = "Invoke the repeatfinder.")
        private boolean repeatFinder = false;

        @Parameter(names = { "-R", "--dump-repeatfinder" }, description = "Print the result of the repeatfinder.")
        private boolean dumpRepeatfinder = false;

        @Parameter(names = {"--radix" }, description = "Radix used for printing of output parameters.")
        private int radix = 16;

        @Parameter(names = {"-s", "--statistics" }, description = "Print some statistics.")
        private boolean statistics = false;

        @Parameter(names = {"-t", "--timebase"}, description = "Force time unit , in microseconds (no suffix), or in periods (with suffix \"p\").")
        private String timeBase = null;

        @Parameter(names = {      "--timings"}, description = "Print the total timings of the compute IRP form.")
        private boolean timings = false;

        @Parameter(names = {"-T", "--trailinggap"}, description = "Dummy trailing gap (in micro seconds) added to sequences of odd length.")
        private Double trailingGap = null;

        @Parameter(description = "durations in microseconds, or pronto hex.", required = false)
        private List<String> args = null;

        @Override
        public String description() {
            return "The \"analyze\" command takes as input one or several sequences or signals, and computes an IRP form that corresponds to the given input "
                    + "(within the specified tolerances). "
                    + "The input can be given either as Pronto Hex or in raw form, optionally with signs (ignored). "
                    //+ "(In the latter case, it may be necessary to use \"--\" to denote the end of the options.) "
                    + "Several raw format input sequences can be given by enclosing the individual sequences in brackets (\"[]\"). "
                    + "However, if using the --intro-repeat-ending option, the sequences are instead interpreted as intro-, repeat-, "
                    + "and (optionally) ending sequences of an IR signal. "
                    + "\n\n"
                    + "For raw sequences, an explicit modulation frequency can be given with the --frequency option. "
                    + "Otherwise the default frequency, " + (int) ModulatedIrSequence.DEFAULT_FREQUENCY + "Hz, will be assumed. "
                    + "\n\n"
                    + "Using the option --input, instead the content of a file can be taken as input, containing sequences to be analyzed, "
                    + "one per line, blank lines ignored. "
                    + "Using the option --namedinput, the sequences may have names, immediately preceeding the signal. "
                    + "\n\n"
                    + "Input sequences can be pre-processed using the options --chop, --clean, and --repeatfinder. "
                    + "\n\n"
                    + "The input sequence(s) are matched using different \"decoders\". "
                    + "Normally the \"best\" decoder match is output. "
                    + "With the --all option, all decoder matches are output. "
                    + "Using the --decode option, the used decoders can be further limited. "
                    + "The presently available decoders are: "
                    + String.join(", ", AbstractDecoder.decoderNames())
                    + ".\n\n"
                    + "The options --statistics and --dump-repeatfinder (the latter forces the repeatfinder to be on) can be used to print extra information. "
                    + "The common options --absolutetolerance, --relativetolerance, --minrepeatgap determine how the repeat finder breaks the input data. "
                    + "The options --extent, --invert, --lsb, --maxmicroseconds, --maxparameterwidth, --maxroundingerror, --maxunits, --parameterwidths, "
                    + "--radix, and --timebase determine how the computed IRP is displayed."
                    ;
        }

        @Override
        public boolean process(IrpTransmogrifier instance) {
            boolean result = super.process(instance);
            if (result)
                return true;

            if (decoder != null && (decoder.equals("list") || decoder.equals("help") || decoder.equals("?"))) {
                IrCoreUtils.trivialFormatter(instance.out,
                        "Available decoders: " + String.join(", ", AbstractDecoder.decoderNames()), 65);
                return true;
            }
            return false;
        }
    }

    @Parameters(commandNames = { "bitfield" }, commandDescription = "Evaluate bitfield given as argument.")
    private static class CommandBitField extends MyCommand {

        @Parameter(names = { "-n", "--nameengine" }, description = "Define a name engine for resolving the bitfield.", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-l", "--lsb" }, description = "Output bitstream with least significant bit first.")
        private boolean lsb = false;

        @Parameter(names = { "--xml"}, description = "Generate XML and write to file given as argument.")
        private String xml = null;

        @Parameter(description = "bitfield", required = true)
        private List<String> bitField;

        @Override
        public String description() {
            return
                    "The \"bitfield\" command computes the value and the binary form corresponding to the bitfield given as input. "
                    + "Using the --nameengine argument, the bitfield can also refer to names. "
                    + "\n\n"
                    + "As an alternatively, the \"expression\" command may be used."
                    ;
        }
    }

    @Parameters(commandNames = {"code"}, commandDescription = "Generate code for the given target(s)")
    private static class CommandCode extends MyCommand {

        @Parameter(names = { "-d", "--directory" }, description = "Directory in whicht the generate output files will be written, if not using the --output option.")
        private String directory = null;

        @Parameter(names = {       "--inspect" }, description = "Fire up stringtemplate inspector on generated code (if sensible)")
        private boolean inspect = false;

        @Parameter(names = { "-p", "--parameter" }, description = "Specify target dependent parameters to the code generators.")
        private List<String> parameters = new ArrayList<>(4);

        @Parameter(names = { "-s", "--stdirectory" }, description = "Directory containing st (string template) files for code generation.")
        private String stDir = System.getenv("STDIR") != null ? System.getenv("STDIR") : "st";

        @Parameter(names = { "-t", "--target" }, required = true, description = "Target(s) for code generation. Use ? for a list.")
        private List<String> target = new ArrayList<>(4);

        @Parameter(description = "protocols")
        private List<String> protocols;
    }

    @Parameters(commandNames = {"decode"}, commandDescription = "Decode IR signal given as argument")
    private static class CommandDecode extends MyCommand {
        @Parameter(names = { "-a", "--all", "--no-prefer-over"}, description = "Output all decodes; ignore prefer-over.")
        private boolean noPreferOver = false;

//        @Parameter(names = { "-c", "--chop"}, description = "Chop input sequence into several using threshold (in milliseconds) given as argument.")
//        private Integer chop = null;

        @Parameter(names = { "-c", "--clean"}, description = "Invoke cleaner on signal") // ignored with --repeat-finder
        private boolean cleaner = false;

        @Parameter(names = { "-f", "--frequency"}, converter = FrequencyParser.class, description = "Set modulation frequency.")
        private Double frequency = null;

        @Parameter(names = { "-i", "--input"}, description = "File/URL from which to take inputs, one per line.")
        private String input = null;

        // NOTE: Removing defaulted parameter is the default from the command line. In the API, the parameter is called
        // removeDefaulted and has the opposite semantics.
        @Parameter(names = { "-k", "--keep-defaulted"}, description = "In output, do not remove parameters that are equal to their defaults.")
        private boolean keepDefaultedParameters = false;

        @Parameter(names = { "-n", "--namedinput"}, description = "File/URL from which to take inputs, one line name, data one line.")
        private String namedInput = null;

        @Parameter(names = { "-p", "--protocol"}, description = "Comma separated list of protocols to try match (default all).")
        private String protocol = null;

        @Parameter(names = { "-r", "--repeatfinder"}, description = "Invoke repeat finder on input sequence")
        private boolean repeatFinder = false;

        @Parameter(names = { "-R", "--dump-repeatfinder" }, description = "Print the result of the repeatfinder.")
        private boolean dumpRepeatfinder = false;

        @Parameter(names = {"--radix" }, description = "Radix used for printing of output parameters.")
        private int radix = 10;

        @Parameter(names = {"--recursive" }, description = "Apply decoder recursively, (for long signals).")
        private boolean recursive = false;

        @Parameter(names = { "-s", "--strict"}, description = "Require intro- and repeat sequences to match exactly.")
        private boolean strict = false;

        @Parameter(names = {"-T", "--trailinggap"}, description = "Trailing gap (in micro seconds) added to sequences of odd length.")
        private Double trailingGap = null;

        @Parameter(description = "durations in micro seconds, alternatively pronto hex", required = false)
        private List<String> args;

        @Override
        public String description() {
            return "The \"decode\" command takes as input one or several sequences or signals, "
                    + "and output one or many protocol/parameter combinations that corresponds to the given input "
                    + "(within the specified tolerances). "
                    + "The input can be given either as Pronto Hex or in raw form, optionally with signs (ignored). "
                    + "Several raw format input sequences can be given by enclosing the individual sequences in brackets (\"[]\"). "
                    + "\n\n"
                    + "For raw sequences, an explicit modulation frequency can be given with the --frequency option. "
                    + "Otherwise the default frequency, " + (int) ModulatedIrSequence.DEFAULT_FREQUENCY + "Hz, will be assumed. "
                    + "\n\n"
                    + "Using the option --input, instead the content of a file can be taken as input, containing sequences to be analyzed, "
                    + "one per line, blank lines ignored. "
                    + "Using the option --namedinput, the sequences may have names, immediately preceeding the signal. "
                    + "\n\n"
                    + "Input sequences can be pre-processed using the options --clean, and --repeatfinder. "
                    + "\n\n"
                    + "The common options --absolutetolerance --relativetolerance, --minrepeatgap determine how the repeat finder breaks the input data. "
                    ;
        }
    }

    @Parameters(commandNames = { "expression" }, commandDescription = "Evaluate expression given as argument.")
    private static class CommandExpression extends MyCommand {

        @Parameter(names = { "-n", "--nameengine" }, description = "Define a name engine to use for evaluating.", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-r", "--radix"}, description = "Radix for outputting result.")
        private int radix = 10;

        @Parameter(names = { "--stringtree" }, description = "Output stringtree.")
        private boolean stringTree = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram.")
        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "Generate XML and write to file argument.")
        private String xml = null;

        @Parameter(description = "expression", required = true)
        private List<String> expressions;

        @Override
        public String description() {
            return "This command evaluates its argument as an expression. "
                    + "Using the --nameengine argument, the expression may also contain names. "
                    + "The --gui options presents a graphical representation of the parse tree.";
        }
    }

    @Parameters(commandNames = {"help"}, commandDescription = "Describe the syntax of program and commands.")
    private static class CommandHelp extends MyCommand {

        @Parameter(names = { "-c", "--common", "--options"}, description = "Describe the common options only.")
        private boolean commonOptions = false;

        @Parameter(names = { "-s", "--short" }, description = "Produce a short usage message.")
        private boolean shortForm = false;

        @Parameter(description = "commands")
        private List<String> commands = null;

        @Override
        public String description() {
            return "This command list the syntax for the command(s) given as argument, default all. "
                    + "Also see the option \"--describe\"."
                    ;
        }
    }

    @Parameters(commandNames = { "lirc" }, commandDescription = "Convert Lirc configuration files to IRP form.")
    private static class CommandLirc extends MyCommand {

        @Parameter(names = { "-c", "--commands" }, description = "Also list the commands if the remotes.")
        private boolean commands = false;

        @Parameter(names = { "-r", "--radix"}, hidden = true, description = "Radix for outputting result, default 16.") // Too much...?
        private int radix = 16;

        @Parameter(description = "Lirc config files/directories/URLs; empty for <stdin>.", required = false)
        private List<String> files = new ArrayList<>(8);

        @Override
        public String description() {
            return "This command reads a Lirc configuration, from a file, directory, or an URL, "
                    + "and computes a correponding IRP form. "
                    + "No attempt is made to clean up, for example by rounding times or "
                    + "finding a largest common divider.";
        }
    }

    @Parameters(commandNames = {"list"}, commandDescription = "List protocols and their properites")
    private static class CommandList extends MyCommand {

        // not yet implemented
        //@Parameter(names = { "-b", "--browse" }, description = "Open the protoocol data base file in the browser")
        //private boolean browse = false;

        @Parameter(names = { "-c", "--classify"}, description = "Classify the protocol(s).")
        private boolean classify = false;

        @Parameter(names = { "--cname"}, description = "List C name of the protocol(s).")
        private boolean cName = false;

        @Parameter(names = { "--documentation"}, description = "Print (possible longer) documentation.")
        private boolean documentation = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram.")
        private boolean gui = false;

        @Parameter(names = { "-i", "--irp"}, description = "List IRP form.")
        private boolean irp = false;

        // not really useful, therefore hidden
        @Parameter(names = { "--istring"}, hidden = true, description = "test toIrpString.")
        private boolean is = false;

        @Parameter(names = { "-m", "--mindiff"}, description = "Display minimal difference between contained durations.")
        private boolean minDiff = false;

        @Parameter(names = { "-n", "--normalform"}, description = "List the normal form.")
        private boolean normalForm = false;

        @Parameter(names = { "--name"}, description = "List protocol name, also if --quiet is given.")
        private boolean name = false;

        @Parameter(names = { "-r", "--radix" }, description = "Radix of parameter output.")
        private int radix = 16;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree.")
        private boolean stringTree = false;

        @Parameter(names = { "-w", "--weight" }, description = "Compute weight of the protocols.")
        private boolean weight = false;

        @Parameter(names = { "--warnings" }, description = "Issue warnings for some problematic IRP constructs.")
        private boolean warnings = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols = new ArrayList<>(8);

        @Override
        public String description() {
            return "This command list miscellaneous properties of the protocol(s) given as arguments.";
        }
    }

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal from parameters")
    private static class CommandRender extends MyCommand {

        @Parameter(names = { "-#", "--count" }, description = "Generate am IR sequence with count number of transmissions")
        private Integer count = null;

        //@Parameter(names = { "-i", "--irp" }, description = "Explicit IRP string to use as protocol definition.")
        //private String irp = null;

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--pronto", "--ccf", "--hex" }, description = "Generate Pronto hex.")
        private boolean pronto = false;

        @Parameter(names = { "-P", "--printparameters", "--parameters" }, description = "Print used parameters values")
        private boolean printParameters = false;

        @Parameter(names = { "-r", "--signed-raw" }, description = "Generate raw form.")
        private boolean raw = false;

        @Parameter(names = { "-R", "--raw-without-signs" }, description = "Generate raw form without signs.")
        private boolean rawWithoutSigns = false;

        @Parameter(names = { "--random" }, description = "Generate random, valid, parameters")
        private boolean random = false;

        @Parameter(names = { "--number-repeats" }, description = "Generate an IR sequence containing the given number of repeats")
        private Integer numberRepeats = null;

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        private List<String> protocols = new ArrayList<>(0);

        @Override
        public String description() {
            return "This command is used to compute an IR signal from one or more protocols "
                    + "(\"render\" it). The protocol can be given either by name(s) "
                    + "(or regular expression if using the --regexp option), or, using the "
                    + "--irp options, given explicitly as an IRP form. "
                    + "The parameters can be either given directly with the -n option,"
                    + "or the --random option can be used to generate random, but valid parameters"
                    + "With the --count or --number-repeats option, instead an IR sequence is computed,"
                    + "containing the desired number of repeats.";
        }
    }

    @Parameters(commandNames = {"version"}, commandDescription = "Report version")
    private static class CommandVersion extends MyCommand {

        @Parameter(names = { "-s", "--short" }, description = "Issue only the version number of the program proper")
        private boolean shortForm = false;

        @Override
        public String description() {
            return "This command returns the version. and licensing information for the program.";
        }
    }

    @Parameters(commandNames = {"convertconfig"}, commandDescription = "Convert an IrpProtocols.ini-file to an IrpProtocols.xml, or vice versa.")
    @SuppressWarnings("ClassMayBeInterface")
    private static class CommandConvertConfig extends MyCommand {

        @Parameter(names = { "-c", "--check" }, description = "Check that the protocols in the input file are alphabetically ordered.")
        private boolean checkSorted = false;

        @Override
        public String description() {
            return "This command converts between the xml form and the ini form on IrpProtocols.";
        }
    }

    private static abstract class MyCommand {
        @Parameter(names = { "-h", "-?", "--help" }, help = true, description = "Print help for this command.")
        @SuppressWarnings("FieldMayBeFinal")
        private boolean help = false;

        @Parameter(names = { "--describe" }, help = true, description = "Print a possibly longer documentation for the present command.")
        @SuppressWarnings("FieldMayBeFinal")
        private boolean description = false;

        /**
         * Returns a possibly longer documentation of the command.
         * @return Documentation string;
         */
        // Please override!
        public String description() {
            return "Documentation for this command has not yet been written.\nUse --help for the syntax of the command.";
        }

        public boolean process(IrpTransmogrifier instance) {
            if (help) {
                instance.out.println(instance.usageString(this.getClass().getSimpleName().substring(7).toLowerCase(Locale.US)));
                return true;
            }
            if (description) {
                IrCoreUtils.trivialFormatter(instance.out, description(), 65);
                return true;
            }
            return false;
        }
    }

    private static class UsageException extends Exception {

        UsageException(String message) {
            super(message);
        }
    }

    public static class ProgramExitStatus {

        private static void doExit(int exitCode) {
            System.exit(exitCode);
        }

        static void die(int exitStatus, String message) {
            new ProgramExitStatus(exitStatus, message).die();
        }

        private final int exitStatus;
        private final String message;

        ProgramExitStatus(int exitStatus, String message) {
            this.exitStatus = exitStatus;
            this.message = message;
        }

        ProgramExitStatus() {
            this(IrpUtils.EXIT_SUCCESS, null);
        }

        /**
         * @return the exitStatus
         */
        int getExitStatus() {
            return exitStatus;
        }

        /**
         * @return the message
         */
        String getMessage() {
            return message;
        }

        boolean isSuccess() {
            return exitStatus == IrpUtils.EXIT_SUCCESS;
        }

        void die() {
            PrintStream stream = exitStatus == IrpUtils.EXIT_SUCCESS ? System.out : System.err;
            if (message != null && !message.isEmpty())
                stream.println(message);
            if (exitStatus == IrpUtils.EXIT_USAGE_ERROR) {
                stream.println();
                stream.println("Use \"" + PROGRAMNAME + " help\" or \"" + PROGRAMNAME + " help --short\"\nfor command syntax.");
            }
            doExit(exitStatus);
        }
    }
}
