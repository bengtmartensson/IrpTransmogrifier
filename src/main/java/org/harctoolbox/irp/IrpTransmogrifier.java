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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());
    private static JCommander argumentParser;
    private static CommandLineArgs commandLineArgs = new CommandLineArgs();
    private static PrintStream out;

    private static void usage() {
        StringBuilder str = new StringBuilder(1000);
        argumentParser.usage(str);

        out.println(str);
    }

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder(1000);
        argumentParser.usage(str);

        (exitcode == IrpUtils.exitSuccess ? out : System.err).println(str);
        System.exit(exitcode);
    }

    private static void list(IrpDatabase irpDatabase, CommandList commandList) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, UnassignedException {
        List<String> list = commandList.protocols == null ? new ArrayList<>(irpDatabase.getNames())
                : commandList.regexp ? irpDatabase.getMatchingNames(commandList.protocols)
                : commandList.protocols;
        if (commandList.sort)
            Collections.sort(list);

        for (String proto : list) {
            if (!irpDatabase.isKnown(proto))
                throw new UnknownProtocolException(proto);

            if (commandList.irp)
                out.println(irpDatabase.getIrp(proto));
            if (commandList.documentation)
                out.println(irpDatabase.getDocumentation(proto));
            if (commandList.stringTree) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.println(protocol.toStringTree());
            }
            if (commandList.is) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.println(protocol.toIrpString());
            }
            if (commandList.gui) {
                IrpParser parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                //parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                Protocol protocol = new Protocol(parser.protocol());
                showTreeViewer(parser, protocol.getParseTree(), "Parse tree for " + proto);
            }
            if (commandList.parse)
                try {
                    Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                    out.println(protocol);
                    out.println("Parsing succeeded");
                } catch (IrpSyntaxException ex) {
                    logger.log(Level.WARNING, "Unparsable protocol {0}", proto);
                }
            if (commandList.classify) {
                out.print(proto);
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                out.print("\t");
                out.print((int) protocol.getFrequency());
                out.print("\t");
                out.print(protocol.hasMemoryVariable("T") ? "toggle\t" : "\t");
                out.print(protocol.isStandardPWM() ? "PWM" : "");
                out.print(protocol.isPWM4() ? "PWM4" : "");
                out.print(protocol.isBiphase() ? "Biphase" : "");
                out.print(protocol.isTrivial(false) ? "Trivial" : "");
                out.print(protocol.isTrivial(true) ? "invTrivial" : "");
                out.print("\t");
                out.print(protocol.interleavingOk() ? "interleaving\t" : "\t");
                out.print(protocol.startsWithDuration() ? "SWD\t" : "\t");
                out.print(protocol.isRPlus() ? "R+" : "");

                out.println();
            }
        }
    }

    private static void help() {
        usage();
    }

    private static void version(IrpDatabase db, String filename) {
        out.println(Version.versionString);
        if (db != null)
            out.println("Database: " + filename + " version: " + db.getConfigFileVersion());

        out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
        out.println();
        out.println(Version.licenseString);
    }

    private static void writeConfig(IrpDatabase database, CommandWriteConfig commandWriteConfig) {
        XmlUtils.printDOM(out, database.toDocument(), "UTF-8", "{" + IrpDatabase.irpProtocolNS + "}irp");
    }

    private static void code(IrpDatabase irpDatabase, CommandCode commandCode) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, FileNotFoundException, IOException, SAXException, UnassignedException {
        for (String proto : commandCode.protocols) {
            NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
            if (commandCode.irp)
                out.println(protocol.getIrp());
            if (commandCode.documentation)
                out.println(protocol.getDocumentation());
            if (commandCode.xml) {
                Document doc = protocol.toDocument();
                XmlUtils.printDOM(out, doc, commandCode.encoding, "irp documentation");
            }
            if (commandCode.xslt != null) {
                Document doc = protocol.toDocument();
                CodeGenerator codeGenerator = new CodeGenerator(doc);
                Document stylesheet = XmlUtils.openXmlFile(new File(commandCode.xslt));
                codeGenerator.printDOM(out, stylesheet, commandCode.encoding);
           }
        }
    }

    private static void render(IrpDatabase irpDatabase, CommandRender commandRenderer) throws IrpSyntaxException, UnknownProtocolException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnassignedException, DomainViolationException, UsageException, FileNotFoundException, org.harctoolbox.IrpMaster.IrpMasterException, CloneNotSupportedException {
        if (commandRenderer.random != commandRenderer.nameEngine.isEmpty())
            throw new UsageException("Must give exactly one of --nameengine and --random");

        for (String prt : commandRenderer.protocols) {

            List<String> list;
            if (commandRenderer.regex)
                list = irpDatabase.getMatchingNames(prt);
            else {
                list = new ArrayList<>(10);
                list.add(prt);
            }
            //if (commandList.sort)
            Collections.sort(list);

            for (String proto : list) {
                logger.info(proto);
                NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
                NameEngine nameEngine = !commandRenderer.nameEngine.isEmpty() ? commandRenderer.nameEngine
                        : commandRenderer.random ? protocol.randomParameters()
                                : new NameEngine();
                if (commandRenderer.random)
                    logger.log(Level.INFO, nameEngine.toString());

                IrSignal irSignal;
                irSignal = protocol.toIrSignal(nameEngine.clone());
                if (commandRenderer.pronto)
                    System.out.println(Pronto.toPrintString(irSignal));

                if (commandRenderer.test) {
                    IrSignal irpMasterSignal = IrpMasterUtils.renderIrSignal(proto, nameEngine);
                    if (!irSignal.approximatelyEquals(irpMasterSignal)) {
                        out.println(Pronto.toPrintString(irpMasterSignal));
                        out.println("Error in " + proto);
                        //System.exit(1);
                    }
                }
            }
        }
    }

    private static void analyze(CommandAnalyze commandAnalyze) throws OddSequenceLenghtException {
        int[] data = new int[commandAnalyze.args.size()];
        for (int i = 0; i < commandAnalyze.args.size(); i++)
            data[i] = Integer.parseInt(commandAnalyze.args.get(i));

        IrSequence irSequence = new ModulatedIrSequence(data, commandAnalyze.frequency);

        if (commandAnalyze.cleaner) {
            Cleaner cleaner = new Cleaner(irSequence);
            irSequence = cleaner.toIrSequence();
        }

        if (commandAnalyze.repeatFinder) {
            RepeatFinder repeatFinder = new RepeatFinder(irSequence);
            RepeatFinder.RepeatFinderData repeatFinderData = repeatFinder.getRepeatFinderData();
            out.println(repeatFinderData);
            IrSignal irSignal = repeatFinder.toIrSignal(irSequence, commandAnalyze.frequency);
            out.println(irSignal);
        }

        Analyzer analyzer = new Analyzer(irSequence);
        for (int i = 0; i < analyzer.getTimings().size(); i++) {
            int timing = analyzer.getTimings().get(i);
            out.println((char) ('A' + i) + ": " + timing + "    \t" + analyzer.getCleanedHistogram().get(timing));
        }
        out.println(analyzer.toTimingsString());

    }

    private static void recognize(IrpDatabase irpDatabase, CommandRecognize commandRecognize) throws IncompatibleArgumentException, IrpSyntaxException, IrpSemanticException, ArithmeticException, InvalidRepeatException, UnknownProtocolException, UnassignedException, UsageException, DomainViolationException {
        if (commandRecognize.protocol == null)
            throw new UnsupportedOperationException("Not implemented yet");

        if (commandRecognize.test != (commandRecognize.args == null))
            throw new UsageException("Must either use --test or have parameters, but not both.");

        if (commandRecognize.random == (!commandRecognize.nameEngine.isEmpty()))
            throw new UsageException("Must either use --random or --nameengine, but not both.");

        NamedProtocol protocol = irpDatabase.getNamedProtocol(commandRecognize.protocol);
        NameEngine testNameEngine = null;
        IrSignal irSignal;
        NameEngine nameEngine;
        if (commandRecognize.test) {
            testNameEngine = commandRecognize.random ? protocol.randomParameters() : commandRecognize.nameEngine;
            irSignal = protocol.toIrSignal(testNameEngine);
            out.println(testNameEngine);
        } else {
            irSignal = Pronto.parse(commandRecognize.args);
        }

        nameEngine = protocol.recognize(irSignal);

        if (commandRecognize.test) {
            if (nameEngine != null && nameEngine.numbericallyEquals(testNameEngine))
                out.println("hit");
            else
                out.println("miss");

        } else if (nameEngine != null)
            out.println(nameEngine);
        else
            out.println("no decode");
    }

    private static void expression(CommandExpression commandExpression) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        NameEngine nameEngine = commandExpression.nameEngine;
        for (String text : commandExpression.expressions) {

            IrpParser parser = new ParserDriver(text).getParser();
            Expression expression = new Expression(parser.expression());
//            if (!parser.isMatchedEOF()) {
//                System.err.println("WARNING: Did not match all input");
//                //System.exit(IrpUtils.exitFatalProgramFailure);
//            }

            long result = expression.toNumber(nameEngine);
            out.println(result);
            if (commandExpression.stringTree)
                out.println(expression.getParseTree().toStringTree(parser));
            if (commandExpression.xml) {
                Document doc = XmlUtils.newDocument();
                Element root = expression.toElement(doc);
                doc.appendChild(root);
                XmlUtils.printDOM(doc);
            }

            if (commandExpression.gui)
                IrpTransmogrifier.showTreeViewer(parser, expression.getParseTree(), text+"="+result);
        }
    }

    private static void bitfield(CommandBitfield commandBitField) throws IrpSyntaxException, UsageException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, UnassignedException {
        NameEngine nameEngine = commandBitField.nameEngine;

//        String generalSpecString = commandExpression.generalspec;
//        if (commandExpression.msb) {
//            if (commandExpression.generalspec != null)
//                throw new UsageException("The options --generalspec and --msb are exclusive");
//            generalSpecString = "{msb}";
//        }
//        GeneralSpec generalSpec = new GeneralSpec(generalSpecString);

        for (String text : commandBitField.bitfields) {

            IrpParser parser = new ParserDriver(text).getParser();
            BitField bitfield = BitField.newBitField(parser.bitfield());
//            if (!parser.isMatchedEOF()) {
//                System.err.println("WARNING: Did not match all input");
//                //System.exit(IrpUtils.exitFatalProgramFailure);
//            }

            long result = bitfield.toNumber(nameEngine);
            out.print(result);
            if (bitfield instanceof FiniteBitField) {
                FiniteBitField fbf = (FiniteBitField) bitfield;
                out.print("\t" + fbf.toBinaryString(nameEngine, commandBitField.lsb));
            }
            out.println();

//            if (commandExpression.stringTree)
//                out.println(bitfield.getParseTree().toStringTree(parser));
            if (commandBitField.xml) {
                Document doc = XmlUtils.newDocument();
                Element root = bitfield.toElement(doc);
                doc.appendChild(root);
                XmlUtils.printDOM(doc);
            }
//
//            if (commandExpression.gui)
//                IrpTransmogrifier.showTreeViewer(parser, bitfield.getParseTree(), text+"="+result);
            if (commandBitField.gui)
                IrpTransmogrifier.showTreeViewer(parser, bitfield.getParseTree(),
                        text + "=" + (bitfield instanceof FiniteBitField
                                ? ((FiniteBitField) bitfield).toBinaryString(nameEngine, commandBitField.lsb)
                                : bitfield.toString(nameEngine)));
        }
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
     *
     * @param args
     */
    public static void main(String[] args) {
        commandLineArgs = new CommandLineArgs();
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

        try {
            out = IrpUtils.getPrintSteam(commandLineArgs.output == null ? "-" : commandLineArgs.output);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            System.exit(IrpUtils.exitIoError);
        }

        if (commandLineArgs.helpRequested) {
            help();
            System.exit(IrpUtils.exitSuccess);
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
            String configFilename = commandLineArgs.configFile  != null
                    ? commandLineArgs.configFile
                    : commandLineArgs.iniFile  != null
                    ? commandLineArgs.iniFile : defaultConfigFile;
            IrpDatabase irpDatabase = commandLineArgs.iniFile  != null
                    ? new IrpDatabase(IrpDatabase.readIni(commandLineArgs.iniFile))
                    : new IrpDatabase(configFilename);
            if (!argumentParser.getParsedCommand().equals("writeconfig"))
                irpDatabase.expand(); // FIXME

            if (commandLineArgs.versionRequested) {
                version(irpDatabase, configFilename);
                System.exit(IrpUtils.exitSuccess);
            }

            String command = argumentParser.getParsedCommand();
            if (command == null) {
                usage(IrpUtils.exitUsageError);
            }
            switch (command) {
                case "analyze":
                    analyze(commandAnalyze);
                    break;
                case "bitfield":
                    bitfield(commandBitfield);
                    break;
                case "code":
                    code(irpDatabase, commandCode);
                    break;
                case "expression":
                    expression(commandExpression);
                    break;
                case "help":
                    help();
                    break;
                case "list":
                    list(irpDatabase, commandList);
                    break;
                case "recognize":
                    recognize(irpDatabase, commandRecognize);
                    break;
                case "render":
                    render(irpDatabase, commandRenderer);
                    break;
                case "version":
                    version(irpDatabase, configFilename);
                    break;
                case "writeconfig":
                    writeConfig(irpDatabase, commandWriteConfig);
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    System.exit(IrpUtils.exitSemanticUsageError);
            }
        } catch (UnsupportedOperationException | ParseCancellationException | CloneNotSupportedException | IOException | IncompatibleArgumentException | SAXException | IrpSyntaxException | IrpSemanticException | ArithmeticException | InvalidRepeatException | UnknownProtocolException | UnassignedException | DomainViolationException | UsageException | IrpMasterException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
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

    public static class NameEngineParser implements IStringConverter<NameEngine> { // MUST be public

        @Override
        public NameEngine convert(String value) {
            try {
                return NameEngine.parseLoose(value);
            } catch (IllegalArgumentException | IrpSyntaxException ex) {
                throw new ParameterException(ex);
            }
        }
    }

    private final static class CommandLineArgs {

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format")
        private String configFile = null;

        @Parameter(names = {"-h", "--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-i", "--ini", "--inifile"}, description = "Pathname of IRP database file in ini format")
        private String iniFile = null;//"src/main/config/IrpProtocols.ini";

        @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
                description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
        private Level logLevel = Level.INFO;

        @Parameter(names = { "-o", "--output" }, description = "Name of output file")
        private String output = null;

        @Parameter(names = {"--seed"}, description = "Set seed for pseudo random number generation (default: random)")
        private Long seed = null;

        @Parameter(names = {"-v", "--version"}, description = "Report version")
        private boolean versionRequested = false;
    }

    @Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal")
    private static class CommandAnalyze {

        @Parameter(names = { "-f", "--frequency"}, description = "Modulation frequency")
        private int frequency = (int) ModulatedIrSequence.defaultFrequency;

        @Parameter(names = { "-r", "--repeatfinder" }, description = "Invoke the repeatfinder")
        private boolean repeatFinder = false;

        @Parameter(names = { "-c", "--clean" }, description = "Invoke the cleaner")
        private boolean cleaner = false;

        @Parameter(description = "durations, or pronto hex", required = true)
        private List<String> args;
    }

    @Parameters(commandNames = { "bitfield" }, commandDescription = "Evaluate bitfield")
    private static class CommandBitfield {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

//        @Parameter(names = { "-g", "--generalspec" }, description = "Generalspec to use")
//        private String generalspec = null;

        @Parameter(names = { "-l", "--lsb" }, description = "Least significant bit first")
        private boolean lsb = false;

//        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
//        private boolean stringTree = false;
//
        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

        @Parameter(description = "bitfield", required = true)
        private List<String> bitfields;
    }

    @Parameters(commandNames = {"code"}, commandDescription = "Generate code")
    private static class CommandCode {

        //@Parameter(names = { "--render" }, description = "Generate code for rendering, otherwise for decoding")
        //private boolean render = false;

        @Parameter(names = { "--xslt" }, description = "Pathname to XSLT")
        private String xslt = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used for generating output")
        private String encoding = "UTF-8";

        @Parameter(names = { "--target" }, description = "Target for code generation")
        private String target;

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = {"-i", "--irp"}, description = "List irp")
        private boolean irp = false;

        @Parameter(description = "List of protocols (default all)")
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

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

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

        @Parameter(names = { "--irp"}, description = "List IRP")
        private boolean irp = false;

        @Parameter(names = { "--is"}, description = "test toIrpString")
        private boolean is = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = {"-p", "--parse"}, description = "Test parse the protocol(s)")
        private boolean parse = false;

        @Parameter(names = {"-r", "--regex", "--regexp"}, description = "Interpret arguments as regular expressions")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the output")
        private boolean sort = false;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols;
    }

    @Parameters(commandNames = {"recognize"}, commandDescription = "Recognize signal")
    private static class CommandRecognize {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to generate test signal", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--protocol"}, description = "Protocol to decode against (default all)")
        private String protocol = null;

        @Parameter(names = { "-r", "--random"}, description = "Generate a random parameter signal to test")
        private boolean random = false;

        @Parameter(names = { "--regex", "--regexp"}, description = "Interpret arguments as regular expressions")
        private boolean regexp = false;

        @Parameter(names = { "-t", "--test"}, description = "Generate a test signal and try to decode it")
        private boolean test = false;

        @Parameter(description = "durations, or pronto hex")
        private List<String> args;
    }

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal")
    private static class CommandRender {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        private NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--pronto" }, description = "Generate Pronto hex")
        private boolean pronto = false;

        @Parameter(names = { "-r", "--raw" }, description = "Generate raw form")
        private boolean raw = false;

        @Parameter(names = { "--random" }, description = "Generate random paraneters")
        private boolean random = false;

        @Parameter(names = { "--regex", "--regexp" }, description = "Generate random paraneters")
        private boolean regex = false;

        @Parameter(names = { "-s", "--sort" }, description = "Sort the protocols?")
        private boolean sort = false;

        @Parameter(names = { "--test" }, description = "Compare with IrpMaster")
        private boolean test = false;

        //@Parameter(names = { "--irpmaster" }, description = "Config for IrpMaster")
        //private String irpMasterConfig = "/usr/local/share/irscrutinizer/IrpProtocols.ini";

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        private List<String> protocols;
    }

    @Parameters(commandNames = {"version"}, commandDescription = "Report version")
    private static class CommandVersion {
    }

    @Parameters(commandNames = {"writeconfig"}, commandDescription = "Write a new config file in XML format")
    private static class CommandWriteConfig {
    }

    private static class UsageException extends Exception {

        UsageException(String message) {
            super(message);
        }
    }
}
