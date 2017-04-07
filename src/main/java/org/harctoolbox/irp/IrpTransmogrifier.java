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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.analyze.Analyzer;
import org.harctoolbox.analyze.Burst;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.lirc.LircCommand;
import org.harctoolbox.lirc.LircConfigFile;
import org.harctoolbox.lirc.LircIrp;
import org.harctoolbox.lirc.LircRemote;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
    private static final String SEPARATOR = "\t";
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

    private PrintStream out;
    private IrpDatabase irpDatabase;
    private CommandLineArgs commandLineArgs;
    private JCommander argumentParser;
    private CommandHelp commandHelp;
    private CommandVersion commandVersion;
    private CommandList commandList;
    private CommandRender commandRenderer;
    private CommandDecode commandDecode;
    private CommandAnalyze commandAnalyze;
    private CommandCode commandCode;
    private CommandBitField commandBitField;
    private CommandExpression commandExpression;
    private CommandLirc commandLirc;
    private CommandConvertConfig commandConvertConfig;

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
    public ProgramExitStatus run(String[] args) {
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

        commandRenderer = new CommandRender();
        argumentParser.addCommand(commandRenderer);

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
        } catch (ParameterException ex) {
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        }

        try {
            Logger topLevelLogger = Logger.getLogger("");
            Formatter formatter = commandLineArgs.xmlLog ? new XMLFormatter() : new SimpleFormatter();
            Handler[] handlers = topLevelLogger.getHandlers();
            for (Handler handler : handlers)
                topLevelLogger.removeHandler(handler);

            System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", commandLineArgs.logformat);

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
                out = IrpUtils.getPrintSteam(commandLineArgs.output);
            //IrpTransmogrifier instance = new IrpTransmogrifier(ps);

            // Since we have help and version as subcommands, --help and --version
            // are a little off. Keep them for compatibility, and
            // map --help and --version to the subcommands
            String command = commandLineArgs.helpRequested ? "help"
                    : commandLineArgs.versionRequested ? "version"
                    : argumentParser.getParsedCommand();

            if (command == null)
                return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, "No command given.");
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
        } catch (UsageException ex) {
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (ParseCancellationException ex) {
            // When we get here,
            // Antlr has already written a somewhat sensible error message on
            // stderr; that is good enough for now.
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (IOException | IllegalArgumentException | SecurityException | InvalidArgumentException | DomainViolationException | InvalidNameException | IrpInvalidArgumentException | NameUnassignedException | UnknownProtocolException | UnsupportedRepeatException | SAXException ex) {
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_FATAL_PROGRAM_FAILURE, ex.getLocalizedMessage());
        } catch (IrpParseException ex) {
            // TODO: Improve error message
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(IrpUtils.EXIT_USAGE_ERROR, "Parse error in \"" + ex.getText() + "\"");
        }
        return new ProgramExitStatus();
    }

    private String usageString(String command) {
        StringBuilder stringBuilder = new StringBuilder(10000);
        if (command == null)
            argumentParser.usage(stringBuilder);
        else
            argumentParser.usage(command, stringBuilder);
        return stringBuilder.toString();
    }

    private void help() {
        boolean finished = commandHelp.process(this);
        if (finished)
            return;

        if (commandHelp.shortForm) {
            shortUsage();
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

    private void shortUsage() {
        out.println("Usage: " + PROGRAMNAME + " [options] [command] [command_options]");
        out.println("Commands:");

        List<String> commands = new ArrayList<>(argumentParser.getCommands().keySet());
        Collections.sort(commands);
        commands.forEach((cmd) -> {
            out.println("   " + padString(cmd) + argumentParser.getCommandDescription(cmd));
        });

        out.println();
        out.println("Use \"" + PROGRAMNAME + " help [command]\" for the full syntax.");
    }

    private String padString(String name) {
        StringBuilder stringBuilder = new StringBuilder(name);
        while (stringBuilder.length() < 16)
            stringBuilder.append(" ");
        return stringBuilder.toString();
    }

    private void list() throws IOException, SAXException, UsageException {
        boolean finished = commandList.process(this);
        if (finished)
            return;

        setupDatabase();
        List<String> list = irpDatabase.evaluateProtocols(commandList.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);

        for (String protocolName : list) {
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

            // Use one line for the first, relatively short items
            out.print(irpDatabase.getName(protocolName));

            if (commandList.cName)
                out.print(SEPARATOR + IrpUtils.toCIdentifier(irpDatabase.getName(protocolName)));

            if (commandList.irp)
                out.print(SEPARATOR + irpDatabase.getIrp(protocolName));

            if (commandList.normalForm)
                try {
                    // already checked it once...
                    out.print(SEPARATOR + irpDatabase.getNormalFormIrp(protocolName, commandList.radix));
                } catch (NameUnassignedException | UnknownProtocolException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                    throw new ThisCannotHappenException(ex);
                }

            if (commandList.documentation)
                out.print(SEPARATOR + irpDatabase.getDocumentation(protocolName));

            if (commandList.stringTree)
                out.print(SEPARATOR + protocol.toStringTree());

            if (commandList.is)
                out.print(SEPARATOR + protocol.toIrpString(commandList.radix));

            if (commandList.gui)
                IrpUtils.showTreeViewer(protocol.toTreeViewer(), "Parse tree for " + protocolName);

            if (commandList.weight)
                out.print(SEPARATOR + "Weight: " + protocol.weight());

            out.println();

            // From here on, use full lines
            if (commandList.classify) {
                out.println(protocol.classificationString());
            }

            if (commandList.warnings)
                out.println(protocol.warningsString()); // already ends with LINESEPARATOR
        }
    }

    private void version() throws UsageException, IOException, SAXException {
        if (commandVersion.shortForm)
            out.println(Version.version);
        else {
            out.println(Version.versionString);
            setupDatabase();
            out.println("Database: " + commandLineArgs.configFile + " version: " + irpDatabase.getConfigFileVersion());

            out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
            out.println();
            out.println(Version.licenseString);
        }
    }

    private void convertConfig() throws IOException, SAXException, UsageException {
        setupDatabase(false); // checks exactly one of -c, -i given
        if (commandLineArgs.iniFile != null)
            XmlUtils.printDOM(out, irpDatabase.toDocument(), commandLineArgs.encoding, "{" + IrpDatabase.IRP_PROTOCOL_NS + "}irp");
        else
            irpDatabase.printAsIni(out);

        if (commandLineArgs.output != null)
            logger.log(Level.INFO, "Wrote {0}", commandLineArgs.output);
    }

    private void code() throws UsageException, IOException, SAXException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        boolean finished = commandCode.process(this);
        if (finished)
            return;

        if (commandCode.directory != null && commandLineArgs.output != null)
            throw new UsageException("The --output and the --directory options are mutually exclusive.");
//        if (commandCode.protocols == null)
//            throw new UsageException("At least one protocol needs to be given.");

        setupDatabase();
        List<String> protocolNames = irpDatabase.evaluateProtocols(commandCode.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolNames.isEmpty())
            throw new UsageException("No protocols matched (forgot --regexp?)");

        //String[] targets = commandCode.target.split(MULTIPLEARGSSEPARATOR);
        for (String target : commandCode.target)
            // Hardcoded selection of technologies for different targets
            if (target.equalsIgnoreCase("xml"))
                createXmlProtocols(protocolNames, commandLineArgs.encoding);
            else
                code(protocolNames, target);
    }

    private void code(Collection<String> protocolNames, String target) throws IOException, UsageException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        if (!new File(commandCode.stDir).isDirectory())
            throw new IOException("Cannot find stdir = " + new File(commandCode.stDir).getCanonicalPath());

        STCodeGenerator.setStDir(commandCode.stDir);
        if (target.equals("?")) {
            listTargets(out);
            return;
        }

        CodeGenerator codeGenerator;
        if (target.equals("dump"))
            codeGenerator = new DumpCodeGenerator();
        else {
            try {
                codeGenerator = new STCodeGenerator(target);
            } catch (FileNotFoundException ex) {
                 throw new UsageException("Target " + target + " not available.  Available targets: " + String.join(" ", listTargets()));
            }
        }
        Map<String, String> parameters = assembleParameterMap(commandCode.parameters);
        if (commandCode.directory != null)
            codeGenerator.generate(protocolNames, irpDatabase, new File(commandCode.directory), commandCode.inspect, parameters,
                    commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance);
        else
            codeGenerator.generate(protocolNames, irpDatabase, out, commandCode.inspect, parameters,
                    commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance);

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

    private void render(NamedProtocol protocol, CommandRender commandRenderer) throws OddSequenceLengthException, DomainViolationException, IrpInvalidArgumentException, NameUnassignedException {
        NameEngine nameEngine = !commandRenderer.nameEngine.isEmpty() ? commandRenderer.nameEngine
                : commandRenderer.random ? new NameEngine(protocol.randomParameters())
                        : new NameEngine();
        if (commandRenderer.random)
            logger.log(Level.INFO, nameEngine.toString());

        if (!commandRenderer.pronto && !commandRenderer.raw && !commandRenderer.rawWithoutSigns)
            logger.warning("No output requested, use either --raw, --raw-without-signs or --pronto go get output.");
        IrSignal irSignal = protocol.toIrSignal(nameEngine);
        if (commandRenderer.raw)
            out.println(irSignal.toString(true));
        if (commandRenderer.rawWithoutSigns)
            out.println(irSignal.toString(false));
        if (commandRenderer.pronto)
            out.println(irSignal.ccfString());
    }

    private void render() throws UsageException, IOException, SAXException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        boolean finished = commandRenderer.process(this);
        if (finished)
            return;

        if (commandRenderer.irp == null && (commandRenderer.random != commandRenderer.nameEngine.isEmpty()))
            throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

        if (commandRenderer.irp != null) {
            if (!commandRenderer.protocols.isEmpty())
                throw new UsageException("Cannot not use --irp together with named protocols");
            try {
                NamedProtocol protocol = new NamedProtocol("irp", commandRenderer.irp, "");
                render(protocol, commandRenderer);
            } catch (ParseCancellationException ex) {
                throw new IrpParseException(commandRenderer.irp, ex);
            }
        } else {
            setupDatabase();
            List<String> list = irpDatabase.evaluateProtocols(commandRenderer.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
            for (String proto : list) {
                //logger.info(proto);
                NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
                render(protocol, commandRenderer);
            }
        }
    }

    private void analyze() throws IrpInvalidArgumentException, UsageException, InvalidArgumentException {
        boolean finished = commandAnalyze.process(this);
        if (finished)
            return;

        // FIXME: parallelization blocker
        Burst.setMaxUnits(commandAnalyze.maxUnits);
        Burst.setMaxUs(commandAnalyze.maxMicroSeconds);
        Burst.setMaxRoundingError(commandAnalyze.maxRoundingError);
        try {
            analyzePronto();
        } catch (Pronto.NonProntoFormatException ex) {
            logger.log(Level.FINE, "Parsing as Pronto Hex failed, trying as raw.");
            try {
                analyzeRaw();
            } catch (NumberFormatException e) {
                throw new UsageException("Invalid signal, neither valid as Pronto nor as raw.");
            }
        }
    }

    private void analyzePronto() throws InvalidArgumentException, InvalidArgumentException, Pronto.NonProntoFormatException {
        IrSignal irSignal = Pronto.parse(commandAnalyze.args);
        if (commandAnalyze.introRepeatEnding)
            logger.warning("--intro.repeat-ending ignored when using a Pronto Hex signal.");
        if (commandAnalyze.chop != null)
            logger.warning("--chop ignored when using a Pronto Hex signal.");
        analyze(irSignal);
    }

    private void analyzeRaw() throws IrpInvalidArgumentException, UsageException, OddSequenceLengthException, InvalidArgumentException {
        if (commandAnalyze.introRepeatEnding)
            analyzeIntroRepeatEnding();
        else
            analyzeSequence();
    }

    private void analyzeIntroRepeatEnding() throws UsageException, OddSequenceLengthException, InvalidArgumentException {
        IrSignal irSignal;
        if (commandAnalyze.chop != null) {
            List<IrSequence> sequences = IrSequence.parse(String.join(" ", commandAnalyze.args));
            if (sequences.size() > 1)
                throw new UsageException("Cannot use --chop together with several IR seqeunces");
            sequences = sequences.get(0).chop(commandAnalyze.chop.doubleValue());
            switch (sequences.size()) {
                case 2:
                    irSignal = new IrSignal(sequences.get(0), sequences.get(1), null, commandAnalyze.frequency);
                    break;
                case 3:
                    irSignal = new IrSignal(sequences.get(0), sequences.get(1), sequences.get(2), commandAnalyze.frequency);
                    break;
                default:
                    throw new UsageException("Wrong number of parts after chop = " + sequences.size());
            }
        } else
            irSignal = IrSignal.parseRaw(commandAnalyze.args, commandAnalyze.frequency, false);
        analyze(irSignal);
    }

    private void analyzeSequence() throws UsageException, OddSequenceLengthException {
        List<IrSequence> sequences = IrSequence.parse(String.join(" ", commandAnalyze.args));
        if (commandAnalyze.chop != null) {
            if (sequences.size() > 1)
                throw new UsageException("Cannot use --chop together with several IR seqeunces");
            sequences = sequences.get(0).chop(commandAnalyze.chop.doubleValue());
        }
        analyze(sequences);
    }

    private void analyze(IrSignal irSignal) {
        Analyzer analyzer = new Analyzer(irSignal, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer);
    }

    private void analyze(List<IrSequence> irSequences) {
        Analyzer analyzer = new Analyzer(irSequences, commandAnalyze.frequency, commandAnalyze.repeatFinder || commandAnalyze.dumpRepeatfind, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer);
    }


    private void analyze(Analyzer analyzer) {
        Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(analyzer.getFrequency(), commandAnalyze.timeBase,
                commandAnalyze.lsb ? BitDirection.lsb : BitDirection.msb,
                commandAnalyze.extent, commandAnalyze.parameterWidths, commandAnalyze.invert);

        if (commandAnalyze.statistics) {
            analyzer.printStatistics(out, params);
            out.println();
        }

        if (commandAnalyze.clean) {
            for (int i = 0; i < analyzer.getNoSequences(); i++) {
                if (analyzer.getNoSequences() > 1)
                    out.print("Seq. #" + i + ": ");
                out.println(analyzer.cleanedIrSequence(i).toString(true));
                if (commandAnalyze.statistics)
                    out.println(analyzer.toTimingsString(i));
            }
        }
        if (commandAnalyze.dumpRepeatfind) {
            for (int i = 0; i < analyzer.getNoSequences(); i++) {
                if (analyzer.getNoSequences() > 1)
                    out.print("Seq. #" + i + ": ");
                out.println(analyzer.repeatReducedIrSignal(i).toString(true));
            }
        }

        List<Protocol> protocols = analyzer.searchProtocol(params, commandAnalyze.decoder, commandLineArgs.regexp);
        for (int i = 0; i < protocols.size(); i++) {
            if (protocols.size() > 1)
                out.print("Seq. #" + i + ": ");
            if (commandAnalyze.statistics)
                out.println(analyzer.toTimingsString(i));
            printAnalyzedProtocol(protocols.get(i), commandAnalyze.radix, params.isPreferPeriods());
        }
    }

    private void decode() throws IrpInvalidArgumentException, IOException, SAXException, UsageException, InvalidArgumentException {
        boolean finished = commandDecode.process(this);
        if (finished)
            return;

        setupDatabase();
        List<String> protocolNamePatterns = commandDecode.protocol == null ? null : Arrays.asList(commandDecode.protocol.split(","));
        List<String> protocolsNames = irpDatabase.evaluateProtocols(protocolNamePatterns, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolsNames.isEmpty())
            throw new UsageException("No protocol given or matched.");

        Decoder decoder = new Decoder(irpDatabase, protocolsNames);
        IrSignal irSignal = IrSignal.parse(commandDecode.args, commandDecode.frequency, false);
        Map<String, Decoder.Decode> decodes = decoder.decode(irSignal, commandDecode.noPreferOver,
                commandDecode.keepDefaultedParameters, commandLineArgs.frequencyTolerance,
                commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.minLeadout);
        decodes.entrySet().forEach((kvp) -> {
            out.println(kvp.getKey() + ": " + kvp.getValue().toString());
        });
    }

    private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods) {
        if (protocol != null)
            out.println(protocol.toIrpString(radix, usePeriods) + SEPARATOR + "weight = " + protocol.weight());
    }

    private void expression() throws FileNotFoundException, NameUnassignedException, IrpParseException {
        boolean finished = commandExpression.process(this);
        if (finished)
            return;

        NameEngine nameEngine = commandExpression.nameEngine;
        String text = String.join(" ", commandExpression.expressions).trim();
        Expression expression = Expression.newExpressionEOF(text);
        long result = expression.toNumber(nameEngine);
        out.println(result);
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
        long result = bitfield.toNumber(nameEngine);
        out.print(result);
        if (bitfield instanceof FiniteBitField) {
            FiniteBitField fbf = (FiniteBitField) bitfield;
            out.print(SEPARATOR + fbf.toBinaryString(nameEngine, commandBitField.lsb));
        }
        out.println();

        if (commandBitField.xml != null) {
            XmlUtils.printDOM(IrpUtils.getPrintSteam(commandBitField.xml), bitfield.toDocument(), commandLineArgs.encoding, null);
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
                out.println("lirc code remote");
            }
            if (commandLirc.commands) {
                for (LircCommand cmd : rem.getCommands()) {
                    out.print(cmd.getName() + ":\t");
                    cmd.getCodes().forEach((x) -> {
                        out.print(Long.toString(x, commandLirc.radix) + " ");
                    });
                    out.println();
                }
            }
        }
    }

    private void setupDatabase() throws IOException, SAXException, UsageException {
        setupDatabase(true);
    }

    private void setupDatabase(boolean expand) throws IOException, SAXException, UsageException {
        if (commandLineArgs.iniFile != null) {
            if (commandLineArgs.configFile != null)
                throw new UsageException("configfile and inifile cannot both be specified");
            irpDatabase = IrpDatabase.readIni(commandLineArgs.iniFile);
        } else {
            irpDatabase = commandLineArgs.configFile == null
                    ? new IrpDatabase(getClass().getResourceAsStream(DEFAULT_CONFIG_FILE))
                    : new IrpDatabase(commandLineArgs.configFile);
        }
        if (expand) {
            irpDatabase.expand();
        }
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
            } catch (IllegalArgumentException | InvalidNameException ex) {
                throw new ParameterException(ex);
            }
        }
    }

    // The reaining classes are ordered alphabetically
    private final static class CommandLineArgs {

        @Parameter(names = {"-a", "--absolutetolerance"}, description = "Absolute tolerance in microseconds, used when comparing durations.")
        private Double absoluteTolerance = null;

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format. Default is the one in the jar file.")
        private String configFile = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used in generated output.")
        private String encoding = "UTF-8";

        @Parameter(names = {"-f", "--frequencytolerance"}, converter = FrequencyParser.class,
                description = "Frequency tolerance in Hz. Negative disables frequency check.")
        private Double frequencyTolerance = null;

        @Parameter(names = {"-h", "--help", "-?"}, help = true, description = "Display help message (deprecated; use the command \"help\" instead).")
        private boolean helpRequested = false;

        @Parameter(names = {"-i", "--ini", "--inifile"}, description = "Pathname of IRP database file in ini format.")
        private String iniFile = null;//"src/main/config/IrpProtocols.ini";

        @Parameter(names = {"--logclasses"}, description = "List of (fully qualified) classes and their log levels.")
        private String logclasses = "";

        @Parameter(names = {"-L", "--logfile"}, description = "Log file. If empty, log to stderr.")
        private String logfile = null;

        @Parameter(names = {"-F", "--logformat"}, description = "Log format, as in class java.util.logging.SimpleFormatter.")
        private String logformat = "%4$s(%2$s): %5$s%n";

        @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
                description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
        private Level logLevel = Level.INFO;

        @Parameter(names = { "--min-leadout"}, description = "Threshold for leadout when decoding.")
        private Double minLeadout = null;

        @Parameter(names = { "-o", "--output" }, description = "Name of output file (default: stdout).")
        private String output = null;

        @Parameter(names = {"-r", "--relativetolerance"}, validateWith = LessThanOne.class,
                description = "Relative tolerance as a number < 1")
        private Double relativeTolerance = null;

        @Parameter(names = { "--regexp" }, description = "Interpret protocol/decoder argument as regular expressions.")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the protocols alphabetically on output.")
        private boolean sort = false;

        @Parameter(names = {"--seed"}, description = "Set seed for pseudo random number generation (default: random).")
        private Long seed = null;

        @Parameter(names = {"-u", "--url-decode"}, description = "URL-decode protocol names, (understanding %20 for example).")
        private boolean urlDecode = false;

        @Parameter(names = {"-v", "--version"}, description = "Report version (deprecated; use command version instead).")
        private boolean versionRequested = false;

        @Parameter(names = {"-x", "--xmllog"}, description = "Log in XML format.")
        private boolean xmlLog = false;
    }

    @Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal: tries to find an IRP form with parameters")
    private static class CommandAnalyze extends MyCommand {

        @Parameter(names = { "-c", "--chop" }, description = "Chop input sequence into several using threshold given as argument.")
        private Integer chop = null;

        @Parameter(names = { "-C", "--clean" }, description = "Output the cleaned sequence(s).")
        private boolean clean = false;

        @Parameter(names = { "-e", "--extent" }, description = "Output the last gap as an extent.")
        private boolean extent = false;

        @Parameter(names = { "-f", "--frequency"}, converter = FrequencyParser.class, description = "Modulation frequency of raw signal.")
        private Double frequency = null;

        @Parameter(names = { "-i", "--invert"}, description = "Invert the order in bitspec.")
        private boolean invert = false;

        @Parameter(names = { "--ire", "--intro-repeat-ending"}, description = "Consider the argument as begin, repeat, and ending sequence.")
        private boolean introRepeatEnding = false;

        @Parameter(names = { "-l", "--lsb" }, description = "Force lsb-first bitorder for the parameters.")
        private boolean lsb = false;

        @Parameter(names = { "-m", "--maxunits" }, description = "Maximal multiplier of time unit in durations.")
        private double maxUnits = 30f;

        @Parameter(names = { "-u", "--maxmicroseconds" }, description = "Maximal duration to be expressed as micro seconds.")
        private double maxMicroSeconds = 10000f;

        @Parameter(names = {      "--maxroundingerror" }, description = "Maximal rounding errors for expressing as multiple of time unit.")
        private double maxRoundingError = 0.3;

        // too complicated for most users...
        @Parameter(names = {      "--decoder" }, hidden = true, description = "Use only the decoders matching argument (regular expression). Mainly for debugging.")
        private String decoder = null;

        @Parameter(names = { "-w", "--parameterwidths" }, variableArity = true, description = "Comma separated list of parameter widths.")
        private List<Integer> parameterWidths = new ArrayList<>(4);

        @Parameter(names = { "-r", "--repeatfinder" }, description = "Invoke the repeatfinder.")
        private boolean repeatFinder = false;

        @Parameter(names = { "-R", "--dump-repeatfind" }, description = "Print the result of the repeatfinder.")
        private boolean dumpRepeatfind = false;

        @Parameter(names = {"--radix" }, description = "Radix used for printing of output parameters.")
        private int radix = 10;

        @Parameter(names = {"-s", "--statistics" }, description = "Print some statistics.")
        private boolean statistics = false;

        @Parameter(names = {"-t", "--timebase"}, description = "Force time unit , in microseconds (no suffix), or in periods (with suffix \"p\").")
        private String timeBase = null;

        @Parameter(description = "durations in microseconds, or pronto hex.", required = true)
        private List<String> args;
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

        @Parameter(names = { "-f", "--frequency"}, converter = FrequencyParser.class, description = "Set modulation frequency.")
        private Double frequency = null;

        @Parameter(names = { "-k", "--keep-defaulted"}, description = "In output, do not remove parameters that are equal to their defaults.")
        private boolean keepDefaultedParameters = false;

        @Parameter(names = { "-p", "--protocol"}, description = "Comma separated list of protocols to try match (default all).")
        private String protocol = null;

        @Parameter(description = "durations in micro seconds, alternatively pronto hex", required = true)
        private List<String> args;
    }

    @Parameters(commandNames = { "expression" }, commandDescription = "Evaluate expression given as argument.")
    private static class CommandExpression extends MyCommand {

        @Parameter(names = { "-n", "--nameengine" }, description = "Define a name engine to use for evaluating.", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

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

    @Parameters(commandNames = { "lirc" }, commandDescription = "Convert Lirc configuration files to IRP form.")
    private static class CommandLirc extends MyCommand {

        @Parameter(names = { "-c", "--commands" }, description = "List the commands in the remotes.")
        private boolean commands = false;

        @Parameter(names = { "-r", "--radix"}, description = "Radix for outputting result, deefault 16.") // Too much...?
        private int radix = 16;

        @Parameter(description = "Lirc config files/directories/URLs); empty for <stdin>.", required = false)
        private List<String> files = new ArrayList<>(8);
    }

    @Parameters(commandNames = {"help"}, commandDescription = "Describe the syntax of program and commands.")
    private static class CommandHelp extends MyCommand {

        @Parameter(names = { "-s", "--short" }, description = "Produce a short usage message.")
        private boolean shortForm = false;

        @Parameter(description = "commands")
        private List<String> commands = null;
    }

    @Parameters(commandNames = {"list"}, commandDescription = "List protocols and their properites")
    private static class CommandList extends MyCommand {

        @Parameter(names = { "-c", "--classify"}, description = "Classify the protocols.")
        private boolean classify = false;

        @Parameter(names = { "--cname"}, description = "List C name of the protocols.")
        private boolean cName = false;

        @Parameter(names = { "--documentation"}, description = "List documentation.")
        private boolean documentation = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram.")
        private boolean gui = false;

        @Parameter(names = { "-i", "--irp"}, description = "List IRP form.")
        private boolean irp = false;

        // not really useful, therefore hidden
        @Parameter(names = { "--istring"}, hidden = true, description = "test toIrpString.")
        private boolean is = false;

        @Parameter(names = { "-n", "--normal", "--normalform"}, description = "List normal form.")
        private boolean normalForm = false;

        // Only sensible together with --irpstring, consequentely hidded
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
    }

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal from parameters")
    private static class CommandRender extends MyCommand {

        @Parameter(names = { "-i", "--irp" }, description = "Explicit IRP string to use as protocol definition.")
        private String irp = null;

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--pronto", "--ccf", "--hex" }, description = "Generate Pronto hex.")
        private boolean pronto = false;

        @Parameter(names = { "-r", "--raw" }, description = "Generate raw form.")
        private boolean raw = false;

        @Parameter(names = { "-R", "--raw-without-signs" }, description = "Generate raw form without signs.")
        private boolean rawWithoutSigns = false;

        @Parameter(names = { "--random" }, description = "Generate random, valid, parameters")
        private boolean random = false;

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        private List<String> protocols = new ArrayList<>(0);

        @Override
        public String description() {
            return "This command is used to compute an IR signal from a parametric description (\"render\" it).";
        }
    }

    @Parameters(commandNames = {"version"}, commandDescription = "Report version")
    private static class CommandVersion {

        @Parameter(names = { "-s", "--short" }, description = "Issue only the version number of the program proper")
        private boolean shortForm = false;
    }

    @Parameters(commandNames = {"convertconfig"}, commandDescription = "Convert an IrpProtocols.ini-file to an IrpProtocols.xml, or vice versa.")
    @SuppressWarnings("ClassMayBeInterface")
    private static class CommandConvertConfig {
    }

    private static abstract class MyCommand {
        @Parameter(names = { "-h", "-?", "--help" }, help = true, description = "Print help for this command.")
        @SuppressWarnings("FieldMayBeFinal")
        private boolean help = false;

        @Parameter(names = { "--description" }, help = true, description = "Print a possibly longer documentation for the command.")
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
                instance.out.println(description());
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
