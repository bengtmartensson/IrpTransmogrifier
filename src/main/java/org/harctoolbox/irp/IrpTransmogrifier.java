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
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.lirc.LircConfigFile;
import org.harctoolbox.lirc.LircCommand;
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
public class IrpTransmogrifier {
    // TODO: make these user settable, reading environment vars, etc.
    private static final String defaultConfigFile = "src/main/config/IrpProtocols.xml";

    // No need to make these settable
    private static final String charSet = "UTF-8"; // Just for runMain
    private static final String SEPARATOR = "\t";
    private static final String PROGRAMNAME = Version.appName;

    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());
    private static JCommander argumentParser;

    private static void die(int exitcode, String message) {
        PrintStream stream = exitcode == IrpUtils.exitSuccess ? System.out : System.err;
        stream.println(message);
        if (exitcode == IrpUtils.exitUsageError) {
            stream.println();
            stream.println("Use \"" + PROGRAMNAME + " help\" for command syntax.");
        }
        doExit(exitcode);
    }

    private static void doExit(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * Runs main on the input, and returns the result as a string. Intended for testing etc.
     * @param input
     * @return Result as String.
     */
    public static String runMain(String[] input) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream, false, charSet);
            main(input, printStream);
            printStream.flush();
            return new String(byteArrayOutputStream.toByteArray(), charSet);
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
    }

    /**
     * Runs main on the split-ted argument, and returns the result as a string. Intended for testing etc.
     * @param input
     * @return Result as String.
     */
    public static String runMain(String input) {
        return runMain(input.split("\\s+"));
    }

    /**
     *
     * @param args program args
     */
    public static void main(String[] args) {
        main(args, null);
    }

    /**
     *
     * @param args program args
     * @param printStream overrides normal print output
     */
    @SuppressWarnings("null")
    private static void main(String[] args, PrintStream printStream) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(PROGRAMNAME);
        argumentParser.setAllowAbbreviatedOptions(true);

        // The ordering in the following lines is the order the commands
        // will be listed in the help. Keep this order in a logical order.
        // In the rest of the file, these are ordered alphabetically.
        CommandHelp commandHelp = new CommandHelp();
        argumentParser.addCommand(commandHelp);

        CommandVersion commandVersion = new CommandVersion();
        argumentParser.addCommand(commandVersion);

        CommandList commandList = new CommandList();
        argumentParser.addCommand(commandList);

        CommandRender commandRenderer = new CommandRender();
        argumentParser.addCommand(commandRenderer);

        CommandDecode commandDecode = new CommandDecode();
        argumentParser.addCommand(commandDecode);

        CommandAnalyze commandAnalyze = new CommandAnalyze();
        argumentParser.addCommand(commandAnalyze);

        CommandCode commandCode = new CommandCode();
        argumentParser.addCommand(commandCode);

        CommandBitField commandBitField = new CommandBitField();
        argumentParser.addCommand(commandBitField);

        CommandExpression commandExpression = new CommandExpression();
        argumentParser.addCommand(commandExpression);

        CommandLirc commandLirc = new CommandLirc();
        argumentParser.addCommand(commandLirc);

        CommandConvertConfig commandConvertConfig = new CommandConvertConfig();
        argumentParser.addCommand(commandConvertConfig);

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            die(IrpUtils.exitUsageError, ex.getLocalizedMessage());
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

            PrintStream ps = printStream != null ? printStream
                    : commandLineArgs.output == null ? System.out
                    : IrpUtils.getPrintSteam(commandLineArgs.output);
            IrpTransmogrifier instance = new IrpTransmogrifier(ps);

            // Since we have help and version as subcommands, --help and --version
            // are a little off. Keep them for compatibility, and
            // map --help and --version to the subcommands
            String command = commandLineArgs.helpRequested ? "help"
                    : commandLineArgs.versionRequested ? "version"
                    : argumentParser.getParsedCommand();

            if (command == null)
                die(IrpUtils.exitUsageError, "No command given");
            else // For findbugs...
                switch (command) {
                    case "analyze":
                        instance.analyze(commandAnalyze, commandLineArgs);
                        break;
                    case "bitfield":
                        instance.bitfield(commandBitField, commandLineArgs);
                        break;
                    case "code":
                        instance.code(commandCode, commandLineArgs);
                        break;
                    case "decode":
                        instance.decode(commandDecode, commandLineArgs);
                        break;
                    case "expression":
                        instance.expression(commandExpression, commandLineArgs);
                        break;
                    case "help":
                        instance.help(commandHelp);
                        break;
                    case "lirc":
                        instance.lirc(commandLirc, commandLineArgs);
                        break;
                    case "list":
                        instance.list(commandList, commandLineArgs);
                        break;
                    case "render":
                        instance.render(commandRenderer, commandLineArgs);
                        break;
                    case "version":
                        instance.version(commandLineArgs, commandVersion);
                        break;
                    case "convertconfig":
                        instance.convertConfig(commandConvertConfig, commandLineArgs);
                        break;
                    default:
                        System.err.println("Unknown command: " + command);
                        System.exit(IrpUtils.exitSemanticUsageError);
                }
        } catch (IrpException | InvalidArgumentException | UnsupportedOperationException | OddSequenceLengthException
                | ParseCancellationException | SAXException | IOException | UsageException | NumberFormatException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
        }
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

    private PrintStream out = null;
    private IrpDatabase irpDatabase;
    private String configFilename;

    private IrpTransmogrifier(PrintStream out) {
        this.out = out;
    }

    private String usageString(String command) {
        StringBuilder stringBuilder = new StringBuilder(10000);
        if (command == null)
            argumentParser.usage(stringBuilder);
        else
            argumentParser.usage(command, stringBuilder);
        return stringBuilder.toString();
    }

    private void help(CommandHelp commandHelp) {
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

    private void list(CommandList commandList, CommandLineArgs commandLineArgs) throws IOException, SAXException, IrpException, UsageException {
        setupDatabase(commandLineArgs);
        List<String> list = irpDatabase.evaluateProtocols(commandList.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);

        for (String protocolName : list) {
            Protocol protocol;
            try {
                protocol = irpDatabase.getProtocol(protocolName);
                logger.log(Level.FINE, "Protocol {0} parsed", protocolName);
            } catch (UnknownProtocolException ex) {
                logger.log(Level.WARNING, "{0}", ex.getMessage());
                continue;
            } catch (IrpException ex) {
                logger.log(Level.WARNING, "Unparsable protocol {0}", protocolName);
                continue;
            }

            out.print(irpDatabase.getName(protocolName));

            if (commandList.cName)
                out.print(SEPARATOR + IrpUtils.toCIdentifier(irpDatabase.getName(protocolName)));

            if (commandList.irp)
                out.print(SEPARATOR + irpDatabase.getIrp(protocolName));

            if (commandList.normalForm)
                out.print(SEPARATOR + irpDatabase.getNormalFormIrp(protocolName));

            if (commandList.documentation)
                out.print(SEPARATOR + irpDatabase.getDocumentation(protocolName));

            if (commandList.stringTree)
                out.print(SEPARATOR + protocol.toStringTree());

            if (commandList.is)
                out.print(SEPARATOR + protocol.toIrpString());

            if (commandList.gui)
                IrpUtils.showTreeViewer(protocol.toTreeViewer(), "Parse tree for " + protocolName);

            if (commandList.weight)
                out.print(SEPARATOR + "Weight: " + protocol.weight());

            if (commandList.classify)
                out.print(SEPARATOR + protocol.classificationString());

            out.println();
        }
    }

    private void version(CommandLineArgs commandLineArgs, CommandVersion commandVersion) throws UsageException, IOException, SAXException, IrpException {
        if (commandVersion.shortForm)
            out.println(Version.version);
        else {
            out.println(Version.versionString);
            setupDatabase(commandLineArgs);
            out.println("Database: " + commandLineArgs.configFile + " version: " + irpDatabase.getConfigFileVersion());

            out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
            out.println();
            out.println(Version.licenseString);
        }
    }

    private void convertConfig(CommandConvertConfig commandConvertConfig, CommandLineArgs commandLineArgs) throws IOException, SAXException, IrpException, UsageException {
        setupDatabase(false, commandLineArgs); // checks exactly one of -c, -i given
        if (commandLineArgs.iniFile != null)
            XmlUtils.printDOM(out, irpDatabase.toDocument(), commandLineArgs.encoding, "{" + IrpDatabase.irpProtocolNS + "}irp");
        else
            irpDatabase.printAsIni(out);

        if (commandLineArgs.output != null)
            logger.log(Level.INFO, "Wrote {0}", commandLineArgs.output);
    }

    private void code(CommandCode commandCode, CommandLineArgs commandLineArgs) throws UsageException, IOException, IrpException, SAXException {
        if (commandCode.directory != null && commandLineArgs.output != null)
            throw new UsageException("The --output and the --directory options are mutually exclusive.");
//        if (commandCode.protocols == null)
//            throw new UsageException("At least one protocol needs to be given.");

        setupDatabase(commandLineArgs);
        List<String> protocolNames = irpDatabase.evaluateProtocols(commandCode.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolNames.isEmpty())
            throw new UsageException("No protocols matched (forgot --regexp?)");

        //String[] targets = commandCode.target.split(MULTIPLEARGSSEPARATOR);
        for (String target : commandCode.target)
            // Hardcoded selection of technologies for different targets
            if (target.equalsIgnoreCase("xml"))
                createXmlProtocols(protocolNames, commandLineArgs.encoding);
            else
                code(protocolNames, target, commandCode, commandLineArgs);
    }

    private void code(Collection<String> protocolNames, String target, CommandCode commandCode, CommandLineArgs commandLineArgs) throws IOException, IrpException, UsageException {
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

    private void render(NamedProtocol protocol, CommandRender commandRenderer) throws IrpException, OddSequenceLengthException {
        NameEngine nameEngine = !commandRenderer.nameEngine.isEmpty() ? commandRenderer.nameEngine
                : commandRenderer.random ? new NameEngine(protocol.randomParameters())
                        : new NameEngine();
        if (commandRenderer.random)
            logger.log(Level.INFO, nameEngine.toString());

        if (!commandRenderer.pronto && !commandRenderer.raw && !commandRenderer.rawWithoutSigns)
            logger.warning("No output requested, use either --raw, --raw-without-signs or --pronto go get output.");
        IrSignal irSignal = protocol.toIrSignal(nameEngine);
        if (commandRenderer.raw)
            out.println(irSignal.toPrintString(true));
        if (commandRenderer.rawWithoutSigns)
            out.println(irSignal.toPrintString(false, true, " "));
        if (commandRenderer.pronto)
            out.println(irSignal.ccfString());
    }

    private void render(CommandRender commandRenderer, CommandLineArgs commandLineArgs) throws UsageException, IOException, SAXException, IrpException, OddSequenceLengthException {
        if (commandRenderer.irp == null && (commandRenderer.random != commandRenderer.nameEngine.isEmpty()))
            throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

        setupDatabase(commandLineArgs);

        if (commandRenderer.irp != null) {
            if (!commandRenderer.protocols.isEmpty())
                throw new UsageException("Cannot not use --irp together with named protocols");
            NamedProtocol protocol = new NamedProtocol("irp", commandRenderer.irp, "");
            render(protocol, commandRenderer);
        } else {
            List<String> list = irpDatabase.evaluateProtocols(commandRenderer.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
            for (String proto : list) {
                //logger.info(proto);
                NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
                render(protocol, commandRenderer);
            }
        }
    }

    private void analyze(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws InvalidArgumentException, UsageException {
        Burst.setMaxUnits(commandAnalyze.maxUnits);
        Burst.setMaxUs(commandAnalyze.maxMicroSeconds);
        Burst.setMaxRoundingError(commandAnalyze.maxRoundingError);
        try {
            analyzePronto(commandAnalyze, commandLineArgs);
        } catch (InvalidArgumentException ex) {
            analyzeRaw(commandAnalyze, commandLineArgs);
        }
    }

    private void analyzePronto(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws InvalidArgumentException {
        IrSignal irSignal = Pronto.parse(commandAnalyze.args);
        if (commandAnalyze.introRepeatEnding)
            logger.warning("--intro.repeat-ending ignored when using a Pronto Hex signal.");
        if (commandAnalyze.chop != null)
            logger.warning("--chop ignored when using a Pronto Hex signal.");
        analyze(irSignal, commandAnalyze, commandLineArgs);
    }

    private void analyzeRaw(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws InvalidArgumentException, UsageException {
        if (commandAnalyze.introRepeatEnding)
            analyzeIntroRepeatEnding(commandAnalyze, commandLineArgs);
        else
            analyzeSequence(commandAnalyze, commandLineArgs);
    }

    private void analyzeIntroRepeatEnding(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws UsageException, InvalidArgumentException {
        IrSignal irSignal;
        if (commandAnalyze.chop != null) {
            List<IrSequence> sequences = IrSequence.parse(String.join(" ", commandAnalyze.args), false);
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
        analyze(irSignal, commandAnalyze, commandLineArgs);
    }

    private void analyzeSequence(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws UsageException {
        List<IrSequence> sequences = IrSequence.parse(String.join(" ", commandAnalyze.args), false);
        if (commandAnalyze.chop != null) {
            if (sequences.size() > 1)
                throw new UsageException("Cannot use --chop together with several IR seqeunces");
            sequences = sequences.get(0).chop(commandAnalyze.chop.doubleValue());
        }
        analyze(sequences, commandAnalyze, commandLineArgs);
    }

    private void analyze(IrSignal irSignal, CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) {
        Analyzer analyzer = new Analyzer(irSignal, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer, commandAnalyze, commandLineArgs);
    }

    private void analyze(List<IrSequence> irSequences, CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) {
        Analyzer analyzer = new Analyzer(irSequences, commandAnalyze.frequency, commandAnalyze.repeatFinder, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        analyze(analyzer, commandAnalyze, commandLineArgs);
    }


    private void analyze(Analyzer analyzer, CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) {
        Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(analyzer.getFrequency(), commandAnalyze.timeBase,
                commandAnalyze.lsb ? BitDirection.lsb : BitDirection.msb,
                commandAnalyze.extent, commandAnalyze.parameterWidths, commandAnalyze.invert);

        if (commandAnalyze.statistics) {
            analyzer.printStatistics(out, params);
            out.println();
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

    private void decode(CommandDecode commandDecode, CommandLineArgs commandLineArgs) throws InvalidArgumentException, IOException, SAXException, IrpException, UsageException {
        setupDatabase(commandLineArgs);
        List<String> protocolNamePatterns = commandDecode.protocol == null ? null : Arrays.asList(commandDecode.protocol.split(","));
        List<String> protocolsNames = irpDatabase.evaluateProtocols(protocolNamePatterns, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (protocolsNames.isEmpty())
            throw new UsageException("No protocol given or matched.");

        Decoder decoder = new Decoder(irpDatabase, protocolsNames, commandDecode.keepDefaultedParameters,
                commandLineArgs.frequencyTolerance, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
        IrSignal irSignal = IrSignal.parse(commandDecode.args, commandDecode.frequency, false);
        Map<String, Decoder.Decode> decodes = decoder.decode(irSignal, commandDecode.noPreferOver);
        decodes.entrySet().forEach((kvp) -> {
            out.println(kvp.getKey() + ": " + kvp.getValue().toString());
        });
    }

    private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods) {
        if (protocol != null)
            out.println(protocol.toIrpString(radix, usePeriods) + SEPARATOR + "weight = " + protocol.weight());
    }

    private void expression(CommandExpression commandExpression, CommandLineArgs commandLineArgs) throws FileNotFoundException, UnassignedException {
        NameEngine nameEngine = commandExpression.nameEngine;
        String text = String.join(" ", commandExpression.expressions).trim();
        Expression expression = new Expression(text);
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

    private void bitfield(CommandBitField commandBitField, CommandLineArgs commandLineArgs) throws FileNotFoundException, UnassignedException {
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
        if (commandBitField.gui)
            IrpUtils.showTreeViewer(bitfield.toTreeViewer(), text + "=" + result);
    }

    private void lirc(CommandLirc commandLirc, CommandLineArgs commandLineArgs) throws IOException {
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

    private void setupDatabase(CommandLineArgs commandLineArgs) throws IOException, SAXException, IrpException, UsageException {
        setupDatabase(true, commandLineArgs);
    }

    private void setupDatabase(boolean expand, CommandLineArgs commandLineArgs) throws IOException, SAXException, IrpException, UsageException {
        if (commandLineArgs.iniFile != null) {
            if (commandLineArgs.configFile != null)
                throw new UsageException("configfile and inifile cannot both be specified");
            irpDatabase = IrpDatabase.readIni(commandLineArgs.iniFile);
        } else {
            configFilename = commandLineArgs.configFile != null ? commandLineArgs.configFile : defaultConfigFile;
            irpDatabase = new IrpDatabase(configFilename);
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
            } catch (IllegalArgumentException | IrpSyntaxException ex) {
                throw new ParameterException(ex);
            }
        }
    }


    // The reaining classes are ordered alphabetically
    private final static class CommandLineArgs {

        @Parameter(names = {"-a", "--absolutetolerance"}, description = "Absolute tolerance in microseconds")
        private Double absoluteTolerance = null;

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format")
        private String configFile = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used for generating output")
        private String encoding = "UTF-8";

        @Parameter(names = {"-f", "--frequencytolerance"}, description = "Frequency tolerance in Hz. Negative disables frequency check")
        private Double frequencyTolerance = null;

        @Parameter(names = {"-h", "--help", "-?"}, help = true, description = "Display help message (deprecated; use command help instead)")
        private boolean helpRequested = false;

        @Parameter(names = {"-i", "--ini", "--inifile"}, description = "Pathname of IRP database file in ini format")
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

        @Parameter(names = { "-o", "--output" }, description = "Name of output file (default stdout)")
        private String output = null;

        @Parameter(names = {"-r", "--relativetolerance"}, validateWith = LessThanOne.class,
                description = "Relative tolerance as a number < 1")
        private Double relativeTolerance = null;

        @Parameter(names = { "--regexp" }, description = "Interpret protocol/decoder argument as regular expressions")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the protocols alphabetically")
        private boolean sort = false;

        @Parameter(names = {"--seed"}, description = "Set seed for pseudo random number generation (default: random)")
        private Long seed = null;

        @Parameter(names = {"-u", "--url-decode"}, description = "URL-decode protocol names, (understanding %20 for example)")
        private boolean urlDecode = false;

        @Parameter(names = {"-v", "--version"}, description = "Report version (deprecated; use command version instead)")
        private boolean versionRequested = false;

        @Parameter(names = {"-x", "--xmllog"}, description = "Log in XML format.")
        private boolean xmlLog = false;
    }

    @Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal: tries to find an IRP form with parameters")
    private static class CommandAnalyze {

        @Parameter(names = { "-c", "--chop" }, description = "Chop input sequence into several using threshold given as argument")
        private Integer chop = null;

        @Parameter(names = { "-e", "--extent" }, description = "Output last gap as extent")
        private boolean extent = false;

        @Parameter(names = { "-f", "--frequency"}, description = "Modulation frequency of raw signal")
        private Double frequency = null;

        @Parameter(names = { "-i", "--invert"}, description = "Invert order in bitspec")
        private boolean invert = false;

        @Parameter(names = { "--ire", "--intro-repeat-ending"}, description = "Consider the argument as begin, repeat, and ending sequence")
        private boolean introRepeatEnding = false;

        @Parameter(names = { "-l", "--lsb" }, description = "Force lsb-first bitorder for the parameters")
        private boolean lsb = false;

        @Parameter(names = { "-m", "--maxunits" }, description = "Maximal multiplier of time unit in durations")
        private double maxUnits = 30f;

        @Parameter(names = { "-u", "--maxmicroseconds" }, description = "Maximal duration to be expressed as micro seconds")
        private double maxMicroSeconds = 10000f;

        @Parameter(names = {      "--maxroundingerror" }, description = "Maximal rounding errors for expressing as multiple of time unit")
        private double maxRoundingError = 0.3;

        @Parameter(names = {      "--decoder" }, description = "Use only the decoders matching argument (regular expression). Mainly for debugging.")
        private String decoder = null;

        @Parameter(names = { "-w", "--parameterwidths" }, variableArity = true, description = "Comma separated list of parameter widths")
        private List<Integer> parameterWidths = new ArrayList<>(4);

        @Parameter(names = { "-r", "--repeatfinder" }, description = "Invoke the repeatfinder")
        private boolean repeatFinder = false;

        @Parameter(names = {"--radix" }, description = "Radix of parameter output")
        private int radix = 16;

        @Parameter(names = {"-s", "--statistics" }, description = "Print some statistics")
        private boolean statistics = false;

        @Parameter(names = {"-t", "--timebase"}, description = "Force timebase, in microseconds, or in periods (with ending \"p\")")
        private String timeBase = null;

        @Parameter(description = "durations in microseconds, or pronto hex", required = true)
        private List<String> args;
    }

    @Parameters(commandNames = { "bitfield" }, commandDescription = "Evaluate bitfield given as argument")
    private static class CommandBitField {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-l", "--lsb" }, description = "Least significant bit first")
        private boolean lsb = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "Generate XML and write to file argument")
        private String xml = null;

        @Parameter(description = "bitfield", required = true)
        private List<String> bitField;
    }

    @Parameters(commandNames = {"code"}, commandDescription = "Generate code for the given target(s)")
    private static class CommandCode {

        @Parameter(names = { "-d", "--directory" }, description = "Directory to generate output files, if not using the --output option.")
        private String directory = null;

        @Parameter(names = {       "--inspect" }, description = "Fire up stringtemplate inspector on generated code (if sensible)")
        private boolean inspect = false;

        @Parameter(names = { "-p", "--parameter" }, description = "Specify target dependent parameters to the code generators")
        private List<String> parameters = new ArrayList<>(4);

        @Parameter(names = { "-s", "--stdirectory" }, description = "Directory containing st files for code generation")
        private String stDir = System.getenv("STDIR") != null ? System.getenv("STDIR") : "st"; // FIXME

        @Parameter(names = { "-t", "--target" }, required = true, description = "Target(s) for code generation. Use ? for a list.")
        private List<String> target = new ArrayList<>(4);

        @Parameter(description = "protocol")
        private List<String> protocols;
    }

    @Parameters(commandNames = {"decode"}, commandDescription = "Decode IR signal given as argument")
    private static class CommandDecode {
        // TODO: presently no sensible way to input raw sequences/signals, issue #14
        @Parameter(names = { "-a", "--all", "--no-prefer-over"}, description = "Output all decodes; ignore prefer-over")
        private boolean noPreferOver = false;

        @Parameter(names = { "-f", "--frequency"}, description = "Modulation frequency")
        private Double frequency = null;

        @Parameter(names = { "-k", "--keep-defaulted"}, description = "Keep parameters equal to their defaults")
        private boolean keepDefaultedParameters = false;

        @Parameter(names = { "-p", "--protocol"}, description = "Comma separated list of protocols to try match (default all)")
        private String protocol = null;

        @Parameter(description = "durations in micro seconds, or pronto hex", required = true)
        private List<String> args;
    }

    @Parameters(commandNames = { "expression" }, commandDescription = "Evaluate expression given as argument")
    private static class CommandExpression {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "Generate XML and write to file argument")
        private String xml = null;

        @Parameter(description = "expression", required = true)
        private List<String> expressions;
    }

    @Parameters(commandNames = { "lirc" }, commandDescription = "Convert Lirc configuration files to IRP form")
    private static class CommandLirc {

        @Parameter(names = { "-c", "--commands" }, description = "List the commands in the remotes")
        private boolean commands = false;

        @Parameter(names = { "-r", "--radix"}, description = "Radix for outputting result") // Too much...?
        private int radix = 16;

        @Parameter(description = "Lirc config files (or directories)", required = false)
        private List<String> files = new ArrayList<>(0);
    }

    @Parameters(commandNames = {"help"}, commandDescription = "Describe the syntax of program and commands")
    private static class CommandHelp {

        @Parameter(names = { "-s", "--short" }, description = "Produce a short usage message")
        private boolean shortForm = false;

        @Parameter(description = "commands")
        private List<String> commands = null;
    }

    @Parameters(commandNames = {"list"}, commandDescription = "List protocols and their properites")
    private static class CommandList {

        @Parameter(names = { "-c", "--classify"}, description = "Classify the protocols")
        private boolean classify = false;

        @Parameter(names = { "--cname"}, description = "List C name of the protocols")
        private boolean cName = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "-i", "--irp"}, description = "List IRP")
        private boolean irp = false;

        @Parameter(names = { "--is"}, description = "test toIrpString")
        private boolean is = false;

        @Parameter(names = { "-n", "--normal", "--normalform"}, description = "List normal form")
        private boolean normalForm = false;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(names = { "-w", "--weight" }, description = "Compute weight")
        private boolean weight = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols = new ArrayList<>(8);
    }

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal")
    private static class CommandRender {

        @Parameter(names = { "-i", "--irp" }, description = "IRP string to use as protocol definition")
        private String irp = null;

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--pronto" }, description = "Generate Pronto hex")
        private boolean pronto = false;

        @Parameter(names = { "-r", "--raw" }, description = "Generate raw form")
        private boolean raw = false;

        @Parameter(names = { "-R", "--raw-without-signs" }, description = "Generate raw form without signs")
        private boolean rawWithoutSigns = false;

        @Parameter(names = { "--random" }, description = "Generate random, but valid, parameters")
        private boolean random = false;

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        private List<String> protocols = new ArrayList<>(0);
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

    private static class UsageException extends Exception {

        UsageException(String message) {
            super(message);
        }
    }
}
