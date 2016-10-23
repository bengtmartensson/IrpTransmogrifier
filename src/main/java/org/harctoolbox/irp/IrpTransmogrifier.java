/*
Copyright (C) 2016 Bengt Martensson.

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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.transform.TransformerException;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.analyze.*;
import org.harctoolbox.ircore.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class contains a command line main routine, allowing command line access to most things in the package.
 */
public class IrpTransmogrifier {
    public static final String defaultConfigFile = "src/main/config/IrpProtocols.xml";
    public static final String xsltDir = "src/main/xslt";
    private static final String charSet = "UTF-8"; // Just for runMain
    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());
    private static JCommander argumentParser;
    private static PrintStream printStream = null;
    private static final String separator = "\t";

    private static void usage() {
        StringBuilder str = new StringBuilder(1000);
        argumentParser.usage(str);

        System.out.println(str);
    }

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder(1000);
        argumentParser.usage(str);

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode);
    }

    private static void doExit(int exitCode) {
        System.exit(exitCode);
    }

    private static int numberTrue(Boolean... bool) {
        int result = 0;
        for (boolean b : bool) {
            if (b)
                result++;
        }
        return result;
    }

    /**
     * show the given Tree Viewer
     *
     * @param tv
     * @param title
     */
    public static void showTreeViewer(TreeViewer tv, String title) {
        JPanel panel = new JPanel();
        //tv.setScale(2);
        panel.add(tv);

        JOptionPane.showMessageDialog(null, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showTreeViewer(IrpParser parser, ParserRuleContext parserRuleContext, String title) {
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());

        // http://stackoverflow.com/questions/34832518/antlr4-dotgenerator-example
        TreeViewer tv = new TreeViewer(ruleNames, parserRuleContext);
        showTreeViewer(tv, title);
    }

    /**
     * Runs main on the split-ted argument, and returns the result as a string.
     * @param input
     * @return Result as String.
     */
    public static String runMain(String input) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(byteArrayOutputStream, false, charSet);
            //IrpTransmogrifier instance = new IrpTransmogrifier(printStream);
            main(input.split(" "));
            printStream.flush();
            return new String(byteArrayOutputStream.toByteArray(), charSet);
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);
        argumentParser.setAllowAbbreviatedOptions(true);

        CommandAnalyze commandAnalyze = new CommandAnalyze();
        argumentParser.addCommand(commandAnalyze);

        CommandBitfield commandBitfield = new CommandBitfield();
        argumentParser.addCommand(commandBitfield);

        CommandCode commandCode = new CommandCode();
        argumentParser.addCommand(commandCode);

        CommandExpression commandExpression = new CommandExpression();
        argumentParser.addCommand(commandExpression);

        CommandHelp commandHelp = new CommandHelp();
        argumentParser.addCommand(commandHelp);

        CommandList commandList = new CommandList();
        argumentParser.addCommand(commandList);

        CommandRecognize commandRecognize = new CommandRecognize();
        argumentParser.addCommand(commandRecognize);

        CommandRender commandRenderer = new CommandRender();
        argumentParser.addCommand(commandRenderer);

        CommandVersion commandVersion = new CommandVersion();
        argumentParser.addCommand(commandVersion);

        CommandWriteConfig commandWriteConfig = new CommandWriteConfig();
        argumentParser.addCommand(commandWriteConfig);

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            usage(IrpUtils.exitUsageError);
        }

        Logger topLevelLogger = Logger.getLogger("");
        System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%4$s(%2$s): %5$s%n");
        //SimpleFormatter formatter = new SimpleFormatter();
        //formatter.
        //ConsoleHandler handler = new ConsoleHandler();
        //handler.setLevel(commandLineArgs.logLevel);
        //topLevelLogger.addHandler(handler);
        topLevelLogger.getHandlers()[0].setLevel(commandLineArgs.logLevel);
        topLevelLogger.setLevel(commandLineArgs.logLevel);
        //logger.removeHandler(logger.getHandlers()[0]);
        //logger.setLevel(commandLineArgs.logLevel/*Level.ALL*/);
        //Logger.getLogger(Protocol.class.getName()).setLevel(Level.INFO);

        if (commandLineArgs.seed != null)
            ParameterSpec.initRandom(commandLineArgs.seed);

        try {
            PrintStream ps = printStream != null ? printStream
                    : commandLineArgs.output == null ? System.out
                    : IrpUtils.getPrintSteam(commandLineArgs.output);
            IrpTransmogrifier instance = new IrpTransmogrifier(ps);

            // map --help and --version to the subcommands
            String command = commandLineArgs.helpRequested ? "help"
                    : commandLineArgs.versionRequested ? "version"
                    : argumentParser.getParsedCommand();

            if (command == null)
                usage(IrpUtils.exitUsageError);

            assert(command != null); // for FindBugs

            switch (command) {
                case "analyze":
                    instance.analyze(commandAnalyze, commandLineArgs);
                    break;
                case "bitfield":
                    instance.bitfield(commandBitfield, commandLineArgs);
                    break;
                case "code":
                    instance.code(commandCode, commandLineArgs);
                    break;
                case "expression":
                    instance.expression(commandExpression, commandLineArgs);
                    break;
                case "help":
                    instance.help();
                    break;
                case "list":
                    instance.list(commandList, commandLineArgs);
                    break;
                case "recognize":
                    instance.recognize(commandRecognize, commandLineArgs);
                    break;
                case "render":
                    instance.render(commandRenderer, commandLineArgs);
                    break;
                case "version":
                    instance.version(commandLineArgs.configFile, commandLineArgs);
                    break;
                case "writeconfig":
                    instance.writeConfig(commandWriteConfig, commandLineArgs);
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    System.exit(IrpUtils.exitSemanticUsageError);
            }
        } catch (TransformerException | UnsupportedOperationException | ParseCancellationException | IOException | IncompatibleArgumentException | SAXException | IrpSyntaxException | IrpSemanticException | ArithmeticException | InvalidRepeatException | UnknownProtocolException | UnassignedException | DomainViolationException | UsageException | IrpMasterException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
        }
    }

    private PrintStream out = null;
    private IrpDatabase irpDatabase;
    private String configFilename;

    public IrpTransmogrifier(PrintStream out) {
        this.out = out;
    }


    private void help() {
        usage();
    }

    private List<String> evaluateProtocols(List<String> in, boolean sort, boolean regexp) {
        List<String> list = (in == null || in.isEmpty()) ? new ArrayList<>(irpDatabase.getNames())
                : regexp ? irpDatabase.getMatchingNames(in)
                        : in;
        if (sort)
            Collections.sort(list);
        return list;
    }

    private List<String> evaluateProtocols(String in, boolean sort, boolean regexp) {
        if (in == null)
            return new ArrayList<>(0);

        List<String> list = new ArrayList<>(1);
        list.add(in);
        return evaluateProtocols(list, sort, regexp);
    }

    private void list(CommandList commandList, CommandLineArgs commandLineArgs) throws UnknownProtocolException, IrpSemanticException, IrpSyntaxException, InvalidRepeatException, ArithmeticException, IncompatibleArgumentException, UnassignedException, IOException, SAXException, TransformerException {
        setupDatabase(commandLineArgs);
        List<String> list = evaluateProtocols(commandList.protocols, commandLineArgs.sort, commandLineArgs.regexp);

        for (String proto : list) {
            if (!irpDatabase.isKnown(proto))
                throw new UnknownProtocolException(proto);

            out.print(proto);

            if (commandList.irp)
                out.print(separator + irpDatabase.getIrp(proto));

            if (commandList.documentation)
                out.print(separator + irpDatabase.getDocumentation(proto));

            if (commandList.stringTree) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.print(separator + protocol.toStringTree());
            }

            if (commandList.is) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.print(separator + protocol.toIrpString());
            }

            if (commandList.gui) {
                IrpParser parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                //parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                Protocol protocol = new Protocol(parser.protocol());
                showTreeViewer(parser, protocol.getParseTree(), "Parse tree for " + proto);
            }

            if (commandList.weight) {
                try {
                    Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                    int weight = protocol.weight();
                    out.print(separator + "Weight: " + weight);
                } catch (IrpSyntaxException ex) {
                    logger.log(Level.WARNING, "Unparsable protocol {0}", proto);
                }
            }

            if (commandList.parse)
                try {
                    Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                    out.print(separator + protocol);
                    out.println(separator + "Parsing succeeded");
                } catch (IrpSyntaxException ex) {
                    logger.log(Level.WARNING, "Unparsable protocol {0}", proto);
                }

            if (commandList.classify) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.print("\t");
                out.print((int) protocol.getFrequency());
                out.print("\t");
                out.print(protocol.hasMemoryVariable("T") ? "toggle\t" : "\t");
                out.print(protocol.isStandardPWM() ? "PWM" : "");
                out.print(protocol.isPWM4() ? "PWM4" : "");
                out.print(protocol.isPWM16() ? "PWM16" : "");
                out.print(protocol.isBiphase() ? "Biphase" : "");
                out.print(protocol.isTrivial(false) ? "Trivial" : "");
                out.print(protocol.isTrivial(true) ? "invTrivial" : "");
                out.print("\t");
                out.print(protocol.interleavingOk() ? "interleaving\t" : "\t");
                out.print(protocol.startsWithDuration() ? "SWD\t" : "\t");
                out.print(protocol.hasVariation() ? "variation\t" : "\t");
                out.print(protocol.isRPlus() ? "R+" : "");
            }

            out.println();
        }

        if (commandLineArgs.xmlFile != null)
            createXmlProtocols(list, commandLineArgs);
    }

    private void createXmlProtocols(List<String> list, CommandLineArgs commandLineArgs) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException, FileNotFoundException, TransformerException {

        Document document = XmlUtils.newDocument();

        Element root = document.createElement("NamedProtocols");
        document.appendChild(root);

        for (String protocolName : list) {
            NamedProtocol protocol = irpDatabase.getNamedProtocol(protocolName);
            Element element = protocol.toElement(document);
            root.appendChild(element);
        }
        PrintStream xmlStream = IrpUtils.getPrintSteam(commandLineArgs.xmlFile);
        XmlUtils.printDOM(xmlStream, document, commandLineArgs.encoding, "Irp Documentation");
    }

    private void version(String filename, CommandLineArgs commandLineArgs) {
        out.println(Version.versionString);
        try {
            setupDatabase(commandLineArgs);
            if (irpDatabase != null)
                out.println("Database: " + filename + " version: " + irpDatabase.getConfigFileVersion());
        } catch (IncompatibleArgumentException | IOException | SAXException ex) {
            logger.log(Level.WARNING, "Could not setup IRP data base: {0}", ex.getMessage());
        }

        out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
        out.println();
        out.println(Version.licenseString);
    }

    private void writeConfig(CommandWriteConfig commandWriteConfig, CommandLineArgs commandLineArgs) throws TransformerException, IncompatibleArgumentException, IOException, SAXException {
        setupDatabase(false, commandLineArgs);
        XmlUtils.printDOM(out, irpDatabase.toDocument(), commandLineArgs.encoding, "{" + IrpDatabase.irpProtocolNS + "}irp");
    }

    private void code(CommandCode commandCode, CommandLineArgs commandLineArgs) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException, IOException, SAXException, TransformerException, UsageException {
        setupDatabase(commandLineArgs);
        List<String> list = evaluateProtocols(commandCode.protocols, commandLineArgs.sort, commandLineArgs.regexp);

        List<NamedProtocol> protocols = new ArrayList<>(list.size());
        for (String protocolName : list)
            protocols.add(irpDatabase.getNamedProtocol(protocolName));

        Document document = NamedProtocol.toDocument(protocols);

        if (commandLineArgs.xmlFile != null) {
            PrintStream xmlStream = IrpUtils.getPrintSteam(commandLineArgs.xmlFile);
            XmlUtils.printDOM(xmlStream, document, commandLineArgs.encoding, "Irp Documentation");
        }

        CodeGenerator codeGenerator = new CodeGenerator(document, commandCode.intermediates);
        codeGenerator.transform(new File(xsltDir, "intro-repeat-ending.xsl"), ".ire");
        //codeGenerator.transform(new File(xsltDir, "code.xsl"), ".code");
        Document stylesheet = XmlUtils.openXmlFile(new File(xsltDir, commandCode.target + ".xsl"));
        codeGenerator.printDOM(out, stylesheet, commandLineArgs.encoding);
        //codeGenerator.printDOM(out, commandLineArgs.encoding);
    }

    private void render(NamedProtocol protocol, CommandRender commandRenderer) throws IrpSyntaxException, IncompatibleArgumentException, IrpSemanticException, ArithmeticException, UnassignedException, DomainViolationException, IrpMasterException {
        NameEngine nameEngine = !commandRenderer.nameEngine.isEmpty() ? commandRenderer.nameEngine
                : commandRenderer.random ? new NameEngine(protocol.randomParameters())
                        : new NameEngine();
        if (commandRenderer.random)
            logger.log(Level.INFO, nameEngine.toString());

        IrSignal irSignal = protocol.toIrSignal(nameEngine.clone());
        if (commandRenderer.raw)
            out.println(irSignal.toPrintString(true));
        if (commandRenderer.pronto)
            out.println(irSignal.ccfString());

        if (commandRenderer.test) {
            String protocolName = protocol.getName();
                IrSignal irpMasterSignal = IrpMasterUtils.renderIrSignal(protocolName, nameEngine);
                if (!irSignal.approximatelyEquals(irpMasterSignal)) {
                    out.println(Pronto.toPrintString(irpMasterSignal));
                    out.println("Error in " + protocolName);
                }
            }
    }

    private void render(CommandRender commandRenderer, CommandLineArgs commandLineArgs) throws UsageException, IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException, DomainViolationException, IrpMasterException, IOException, SAXException {
        setupDatabase(commandLineArgs);
        if (commandRenderer.irp == null && (commandRenderer.random != commandRenderer.nameEngine.isEmpty()))
            throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

        if (commandRenderer.irp != null) {
            if (!commandRenderer.protocols.isEmpty())
                throw new UsageException("Cannot not use --irp together with named protocols");
            if (commandRenderer.test)
                throw new UsageException("Cannot not use --irp together with --test");

            NamedProtocol protocol = new NamedProtocol("irp", commandRenderer.irp, "");
            render(protocol, commandRenderer);
        } else {
            List<String> list = evaluateProtocols(commandRenderer.protocols, commandLineArgs.sort, commandLineArgs.regexp);
            for (String proto : list) {
                logger.info(proto);
                NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
                render(protocol, commandRenderer);
            }
        }
    }

    private void analyze(CommandAnalyze commandAnalyze, CommandLineArgs commandLineArgs) throws OddSequenceLenghtException, IncompatibleArgumentException, UsageException {
        IrSignal inputIrSignal = IrSignal.parse(commandAnalyze.args, commandAnalyze.frequency, false);
        IrSequence irSequence = inputIrSignal.toModulatedIrSequence(1);
        double frequency = inputIrSignal.getFrequency();

        if (commandAnalyze.cleaner) {
            Cleaner cleaner = new Cleaner(irSequence);
            irSequence = cleaner.toIrSequence();
        }

        if (commandAnalyze.repeatFinder) {
            if (inputIrSignal.getRepeatLength() != 0 || inputIrSignal.getEndingLength() != 0)
                throw new UsageException("Cannot use --repeatfinder with a signal with repeat- or ending sequence.");

            RepeatFinder repeatFinder = new RepeatFinder(irSequence);
            RepeatFinder.RepeatFinderData repeatFinderData = repeatFinder.getRepeatFinderData();
            out.println(repeatFinderData);
            IrSignal repeatFinderIrSignal = repeatFinder.toIrSignal(irSequence, frequency);
            out.println(repeatFinderIrSignal);
        }

        Analyzer analyzer = new Analyzer(irSequence, commandAnalyze.repeatFinder,
                commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);

        if (commandAnalyze.statistics) {
            out.println("Spaces:");
            analyzer.getDistinctGaps().stream().forEach((d) -> {
                out.println(analyzer.getName(d) + ": " + d + "    \t" + analyzer.getNumberGaps(d));
            });

            out.println("Marks:");
            analyzer.getDistinctFlashes().stream().forEach((d) -> {
                out.println(analyzer.getName(d) + ": " + d + "    \t" + analyzer.getNumberFlashes(d));
            });

            out.println("Pairs:");
            analyzer.getPairs().stream().forEach((pair) -> {
                out.println(analyzer.getName(pair) + ":\t" + analyzer.getNumberPairs(pair));
            });
            out.println(analyzer.toTimingsString());
        }

        Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(frequency, commandAnalyze.timeBase,
                commandAnalyze.lsb ? BitDirection.lsb : BitDirection.msb,
                commandAnalyze.extent, commandAnalyze.parameterWidths, commandAnalyze.invert);

        Protocol protocol = analyzer.searchProtocol(params);
        printAnalyzedProtocol(protocol, commandAnalyze.radix, params.isPreferPeriods());
    }

    private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods) {
        out.println(protocol.toIrpString(radix, usePeriods) + " \tweight = " + protocol.weight());
    }

    private void recognize(CommandRecognize commandRecognize, CommandLineArgs commandLineArgs) throws UsageException, IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException, DomainViolationException, IOException, SAXException {
        setupDatabase(commandLineArgs);
        List<String> list = evaluateProtocols(commandRecognize.protocol, commandLineArgs.sort, commandLineArgs.regexp);
        if (list.isEmpty())
            throw new UsageException("No protocol given or matched.");

        for (String protocolName : list) {
            if (numberTrue(commandRecognize.random, !commandRecognize.nameEngine.isEmpty(), !commandRecognize.args.isEmpty()) != 1)
                throw new UsageException("Must either use --random or --nameengine, or have arguments.");

            NamedProtocol protocol = irpDatabase.getNamedProtocol(protocolName);
            NameEngine testNameEngine = new NameEngine();
            IrSignal irSignal;
            if (commandRecognize.args.isEmpty()) {
                testNameEngine = commandRecognize.random ? new NameEngine(protocol.randomParameters()) : commandRecognize.nameEngine;
                irSignal = protocol.toIrSignal(testNameEngine.clone());
            } else {
                irSignal = IrSignal.parse(commandRecognize.args, commandRecognize.frequency, false);
            }

            Map<String, Long> nameEngine = protocol.recognize(irSignal, commandRecognize.args.isEmpty() || commandRecognize.keepDefaultedParameters,
                    true, commandLineArgs.frequencyTolerance, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);

            if (commandRecognize.args.isEmpty()) {
                out.print(protocolName + "\t");
                out.print(testNameEngine + "\t");
                out.println((nameEngine != null && testNameEngine.numericallyEquals(nameEngine)) ? "success" : "fail");
            } else if (nameEngine != null)
                out.println(nameEngine);
            else
                out.println("no decode");
        }
    }

    private void expression(CommandExpression commandExpression, CommandLineArgs commandLineArgs) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException, TransformerException, FileNotFoundException {
        NameEngine nameEngine = commandExpression.nameEngine;
        String text = String.join(" ", commandExpression.expressions).trim();

        IrpParser parser = new ParserDriver(text).getParser();
        Expression expression = new Expression(parser.expression());
        int last = expression.getParseTree().getStop().getStopIndex();
        if (last != text.length() - 1)
            logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", text.substring(0, last + 1));

        long result = expression.toNumber(nameEngine);
        out.println(result);
        if (commandExpression.stringTree)
            out.println(expression.getParseTree().toStringTree(parser));
        if (commandLineArgs.xmlFile != null) {
            Document doc = XmlUtils.newDocument();
            Element root = expression.toElement(doc);
            doc.appendChild(root);
            PrintStream xmlStream = IrpUtils.getPrintSteam(commandLineArgs.xmlFile);
            XmlUtils.printDOM(xmlStream, doc, commandLineArgs.encoding, null);
        }

        if (commandExpression.gui)
            IrpTransmogrifier.showTreeViewer(parser, expression.getParseTree(), text + "=" + result);
    }

    private void bitfield(CommandBitfield commandBitField, CommandLineArgs commandLineArgs) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException, TransformerException, FileNotFoundException {
        NameEngine nameEngine = commandBitField.nameEngine;
        for (String text : commandBitField.bitfields) {

            IrpParser parser = new ParserDriver(text).getParser();
            BitField bitfield = BitField.newBitField(parser.bitfield());
            int last = bitfield.getParseTree().getStop().getStopIndex();
            if (last != text.length() - 1)
                logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", text.substring(0, last + 1));

            long result = bitfield.toNumber(nameEngine);
            out.print(result);
            if (bitfield instanceof FiniteBitField) {
                FiniteBitField fbf = (FiniteBitField) bitfield;
                out.print("\t" + fbf.toBinaryString(nameEngine, commandBitField.lsb));
            }
            out.println();

            if (commandLineArgs.xmlFile != null) {
                Document doc = XmlUtils.newDocument();
                Element root = bitfield.toElement(doc);
                doc.appendChild(root);
                PrintStream xmlStream = IrpUtils.getPrintSteam(commandLineArgs.xmlFile);
                XmlUtils.printDOM(xmlStream, doc, commandLineArgs.encoding, null);
            }
            if (commandBitField.gui)
                IrpTransmogrifier.showTreeViewer(parser, bitfield.getParseTree(),
                        text + "=" + (bitfield instanceof FiniteBitField
                                ? ((FiniteBitField) bitfield).toBinaryString(nameEngine, commandBitField.lsb)
                                : bitfield.toString(nameEngine)));
        }
    }


    private void setupDatabase(CommandLineArgs commandLineArgs) throws IncompatibleArgumentException, IOException, SAXException {
        setupDatabase(true, commandLineArgs);
    }

    private void setupDatabase(boolean expand, CommandLineArgs commandLineArgs) throws IncompatibleArgumentException, IOException, SAXException {
        configFilename = commandLineArgs.configFile != null
                ? commandLineArgs.configFile
                : commandLineArgs.iniFile != null
                        ? commandLineArgs.iniFile : defaultConfigFile;
        irpDatabase = commandLineArgs.iniFile != null
                ? new IrpDatabase(IrpDatabase.readIni(commandLineArgs.iniFile))
                : new IrpDatabase(configFilename);
        if (expand)
            irpDatabase.expand();
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

    private final static class CommandLineArgs {

        @Parameter(names = {"-a", "--absolutetolerance"}, description = "Absolute tolerance in microseconds")
        private int absoluteTolerance = 50;

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format")
        private String configFile = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used for generating output")
        private String encoding = "UTF-8";

        @Parameter(names = {"-f", "--frequencytolerance"}, description = "Absolute tolerance in microseconds")
        private double frequencyTolerance = 1000;

        @Parameter(names = {"-h", "--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-i", "--ini", "--inifile"}, description = "Pathname of IRP database file in ini format")
        private String iniFile = null;//"src/main/config/IrpProtocols.ini";

        @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
                description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
        private Level logLevel = Level.INFO;

        @Parameter(names = { "-o", "--output" }, description = "Name of output file (default stdout)")
        private String output = null;

        @Parameter(names = {"-r", "--relativetolerance"}, description = "Relative tolerance as a number < 1 (NOT: percent)")
        private double relativeTolerance = 0.04;

        @Parameter(names = { "--regex", "--regexp"}, description = "Interpret protocol arguments as regular expressions")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the protocols alphabetically")
        private boolean sort = false;

        @Parameter(names = {"--seed"}, description = "Set seed for pseudo random number generation (default: random)")
        private Long seed = null;

        @Parameter(names = {"-v", "--version"}, description = "Report version")
        private boolean versionRequested = false;

        @Parameter(names = { "--xmlFile"}, description = "Generate XML and write to file argument")
        private String xmlFile = null;
    }

    @Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal")
    private static class CommandAnalyze {

        @Parameter(names = { "-c", "--clean" }, description = "Invoke the cleaner")
        private boolean cleaner = false;

        @Parameter(names = { "-e", "--extent" }, description = "Output last gap as extent")
        private boolean extent = false;

        @Parameter(names = { "-f", "--frequency"}, description = "Modulation frequency")
        private double frequency = ModulatedIrSequence.defaultFrequency;

        @Parameter(names = { "-i", "--invert"}, description = "Invert order in bitspec")
        private boolean invert = false;

        @Parameter(names = { "-l", "--lsb" }, description = "Force lsb-first bitorder for the analyzer")
        private boolean lsb = false;

        @Parameter(names = { "-w", "--parameterwidths" }, variableArity = true, description = "Comma separated list of parameter widths")
        private List<Integer> parameterWidths = new ArrayList<>(4);

        @Parameter(names = { "-r", "--repeatfinder" }, description = "Invoke the repeatfinder")
        private boolean repeatFinder = false;

        @Parameter(names = {"--radix" }, description = "Radix of parameter output")
        private int radix = 16;

        @Parameter(names = {"-s", "--statistics"}, description = "Print some statistics on the analyzed signal")
        private boolean statistics = false;

        @Parameter(names = {"-t", "--timebase"}, description = "Force timebase, in microseconds, or in periods (with ending \"p\")")
        private String timeBase = null;

        @Parameter(description = "durations in microseconds, or pronto hex", required = true)
        private List<String> args;
    }

    @Parameters(commandNames = { "bitfield" }, commandDescription = "Evaluate bitfield")
    private static class CommandBitfield {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-l", "--lsb" }, description = "Least significant bit first")
        private boolean lsb = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(description = "bitfield", required = true)
        private List<String> bitfields;
    }

    @Parameters(commandNames = {"code"}, commandDescription = "Generate code")
    private static class CommandCode {

//        @Parameter(names = { "--decode" }, description = "Generate code for decoding, otherwise for rendering. Target dependent.")
//        private boolean decode = false;

//        @Parameter(names = { "--documentation"}, description = "List documentation")
//        private boolean documentation = false;
//
//        @Parameter(names = {"-i", "--irp"}, description = "List irp")
//        private boolean irp = false;

        @Parameter(names = { "-i", "--intermediates" }, description = "Dump intermediate results to files")
        private boolean intermediates = false;

        @Parameter(names = { "-t", "--target" }, required = true, description = "Target for code generation")
        private String target = null;

//        @Parameter(names = { "--xsl", "--xslt" }, description = "Pathname to XSLT")
//        private String xslt = null;

        @Parameter(description = "Protocol")
        private List<String> protocols;
    }

    @Parameters(commandNames = { "expression" }, commandDescription = "Evaluate expression")
    private static class CommandExpression {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(description = "expression")
        private List<String> expressions;
    }

    @Parameters(commandNames = {"help"}, commandDescription = "Report usage")
    private static class CommandHelp {
    }

    @Parameters(commandNames = {"list"}, commandDescription = "List the protocols known")
    private static class CommandList {

        @Parameter(names = { "-c", "--classify"}, description = "Classify the protocols")
        private boolean classify = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "-i", "--irp"}, description = "List IRP")
        private boolean irp = false;

        @Parameter(names = { "--is"}, description = "test toIrpString")
        private boolean is = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = {"-p", "--parse"}, description = "Test parse the protocol(s)")
        private boolean parse = false;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(names = { "-w", "--weight" }, description = "Compute weight")
        private boolean weight = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols = new ArrayList<>(8);
    }

    @Parameters(commandNames = {"recognize"}, commandDescription = "Recognize signal")
    private static class CommandRecognize {

        @Parameter(names = { "-f", "--frequency"}, description = "Modulation frequency (ignored for pronto hex signals)")
        private double frequency = ModulatedIrSequence.defaultFrequency;

        @Parameter(names = { "-k", "--keep-defaulted"}, description = "Normally parameters equal to their default are removed; this option keeps them")
        private boolean keepDefaultedParameters = false;

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to generate test signal", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--protocol"}, description = "Protocol to decode against (default all)")
        private String protocol = null;

        @Parameter(names = { "-r", "--random"}, description = "Generate a random parameter signal to test")
        private boolean random = false;

        @Parameter(description = "durations in micro seconds, or pronto hex")
        private List<String> args = new ArrayList<>(16);
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

        @Parameter(names = { "--random" }, description = "Generate random paraneters")
        private boolean random = false;

        @Parameter(names = { "--test" }, description = "Compare with IrpMaster")
        private boolean test = false;

        //@Parameter(names = { "--irpmaster" }, description = "Config for IrpMaster")
        //private String irpMasterConfig = "/usr/local/share/irscrutinizer/IrpProtocols.ini";

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        private List<String> protocols = new ArrayList<>(0);
    }

    @Parameters(commandNames = {"version"}, commandDescription = "Report version")
    private static class CommandVersion {
    }

    @Parameters(commandNames = {"writeconfig"}, commandDescription = "Write a new config file in XML format, using the --inifile argument")
    private static class CommandWriteConfig {
    }

    private static class UsageException extends Exception {

        UsageException(String message) {
            super(message);
        }
    }
}
