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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.harctoolbox.analyze.AbstractDecoder;
import org.harctoolbox.analyze.Analyzer;
import org.harctoolbox.analyze.Burst;
import org.harctoolbox.analyze.NoDecoderMatchException;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThingsLineParser;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitCounter;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.DomainViolationException;
import org.harctoolbox.irp.DuplicateFinder;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.Name;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.Number;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.ProtocolListDomFactory;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"analyze"}, commandDescription = "Analyze signal: tries to find an IRP form with parameters.")
public class CommandAnalyze extends AbstractCommand {

    /**
     * Path name, as found in the jar, of the XSLT style sheet taking raw girr to named input format.
     */
    public static final String RAWGIRR2NAMEINPUT = "/rawgirr2named.xsl";
    private static final Logger logger = Logger.getLogger(CommandAnalyze.class.getName());

    @Parameter(names = {"-a", "--all"}, description = "List all decoder outcomes, instead of only the one with lowest weight.")
    private boolean allDecodes = false;

    @Parameter(names = {"-b", "--bit-usage"}, description = "Create bit usage report. (Not with --all.)")
    private boolean bitUsage = false;

    @Parameter(names = {"-c", "--chop"}, description = "Chop input sequence into several using threshold (in milliseconds) given as argument.")
    private Integer chop = null;

    @Parameter(names = {"-C", "--clean"}, description = "Output the cleaned sequence(s).")
    private boolean clean = false;

    @Parameter(names = {"-d", "--decoder"}, description = "Use only the decoders matching argument (regular expression, or prefix). "
            + "Use the argument \"list\" to list the available decoders.")
    private String decoder = null;

    @Parameter(names = {"-e", "--extent"}, description = "Output the last gap as an extent.")
    private boolean extent = false;

    @Parameter(names = {"--eliminate-vars"}, description = "Eliminate variables in output form.")
    private boolean eliminateVars = false;

    @Parameter(names = {"--fatgirr"}, description = "Generate Girr file in fat format. Inhibits all other output.")
    private boolean fatgirr = false;

    @Parameter(names = {"-f", "--frequency"}, converter = FrequencyParser.class, description = "Modulation frequency of raw signal.")
    private Double frequency = null;

    @Parameter(names = {"-g", "--girroutput"}, description = "Generate Girr file. Inhibits all other output.")
    private boolean girr = false;

    @Parameter(names = {"-G", "--girrinput"}, description = "Read raw input in Girr format.")
    private String girrInput = null;

    @Parameter(names = {"-i", "--input"}, description = "File/URL from which to take inputs, one sequence per line.")
    private String input = null;

    @Parameter(names = {"-I", "--invert"}, description = "Invert the order in bitspec.")
    private boolean invert = false;

    @Parameter(names = {"--ire", "--intro-repeat-ending"}, description = "Consider the argument as begin, repeat, and ending sequence.")
    private boolean introRepeatEnding = false;

    @Parameter(names = {"-l", "--lsb"}, description = "Force lsb-first bitorder for the parameters.")
    private boolean lsb = false;

    @Parameter(names = {"-m", "--maxunits"}, description = "Maximal multiplier of time unit in durations.")
    private double maxUnits = Burst.Preferences.DEFAULT_MAX_UNITS;

    @Parameter(names = {"-n", "--namedinput"}, description = "File/URL from which to take inputs, one line name, data one line.")
    private String namedInput = null;

    @Parameter(names = {"-p", "--parametertable"}, description = "Create parameter table.")
    private boolean parameterTable = false;

    @Parameter(names = {"-P", "--parameterspecs"}, description = "Compute a dumb parameter specs in the IRP.")
    private boolean parameterSpecs = false;

    @Parameter(names = {"-u", "--maxmicroseconds"}, description = "Maximal duration to be expressed as micro seconds.")
    private double maxMicroSeconds = Burst.Preferences.DEFAULT_MAX_MICROSECONDS;

    @Parameter(names = {"--maxroundingerror"}, description = "Maximal rounding errors for expressing as multiple of time unit.")
    private double maxRoundingError = Burst.Preferences.DEFAULT_MAX_ROUNDING_ERROR;

    @Parameter(names = {"-M", "--maxparameterwidth"}, description = "Maximal parameter width.")
    private int maxParameterWidth = FiniteBitField.MAXWIDTH;

    @Parameter(names = {"-w", "--parameterwidths"}, description = "Comma separated list of either parameter widths or name:width pairs.")
    private List<String> parameterNamedWidths = new ArrayList<>(4);

    @Parameter(names = {"-r", "--repeatfinder"}, description = "Invoke the repeatfinder.")
    private boolean repeatFinder = false;

    @Parameter(names = {"-R", "--dump-repeatfinder"}, description = "Print the result of the repeatfinder.")
    private boolean dumpRepeatfinder = false;

    @Parameter(names = {"--radix"}, description = "Radix used for printing of output parameters.", validateWith = Radix.class)
    private int radix = 16;

    @Parameter(names = {"-s", "--statistics"}, description = "Print some statistics.")
    private boolean statistics = false;

    @Parameter(names = {"-t", "--timebase"}, description = "Force time unit , in microseconds (no suffix), or in periods (with suffix \"p\").")
    private String timeBase = null;

    @Parameter(names = {"--timings"}, description = "Print the total timings of the compute IRP form.")
    private boolean timings = false;

    @Parameter(names = {"-T", "--trailinggap"}, description = "Dummy trailing gap (in micro seconds) added to sequences of odd length.")
    private Double trailingGap = null;

    @Parameter(names = {"--validate"}, description = "Validate that the resulted protocol can be used for rendering and produces the same signal.")
    private boolean validate = false;

    @Parameter(names = {"--xslt"}, description = "File/URL name of XSLT transformation that will be applied to --input or --namedinput argument")
    private String xslt = null;

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
                + "--radix, and --timebase determine how the computed IRP is displayed.";
    }

    @Override
    public boolean process(CmdLineProgram instance) {
        boolean result = super.process(instance);
        if (result)
            return true;

        if (decoder != null && (decoder.equals("list") || decoder.equals("help") || decoder.equals("?"))) {
            IrCoreUtils.trivialFormatter(instance.getOutputStream(),
                    "Available decoders: " + String.join(", ", AbstractDecoder.decoderNames()), 65);
            return true;
        }
        return false;
    }

    private Double possiblyOverrideWithAnalyzeFrequency(Double frequency) {
        return this.frequency != null ? this.frequency : frequency;
    }

    public void analyze(PrintStream out, CommandCommonOptions commandLineArgs) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, IOException, SAXException, UnsupportedEncodingException, TransformerException {
        AnalyzeClass analyzeClass = new AnalyzeClass(out, commandLineArgs);
        analyzeClass.analyze();
    }

    private class AnalyzeClass {

        private final PrintStream out;
        private final CommandCommonOptions commandLineArgs;

        AnalyzeClass(PrintStream out, CommandCommonOptions commandLineArgs) {
            this.out = out;
            this.commandLineArgs = commandLineArgs;
        }

        private void analyze() throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, IOException, SAXException, UnsupportedEncodingException, TransformerException {
            CmdUtils.checkForOption("analyze", args);
            if (allDecodes && decoder != null)
                throw new UsageException("Cannot use both --alldecodes and --decode.");
            if (allDecodes && girr)
                throw new UsageException("Cannot use both --alldecodes and --girr.");
            if (bitUsage && (allDecodes || eliminateVars))
                throw new UsageException("Bit usage report not possible together with --all or --eliminate-vars");
            if (parameterTable && eliminateVars)
                throw new UsageException("Parameter table is meaninless together with --eliminate-vars");

            if (IrCoreUtils.numberTrue(input != null, namedInput != null, girrInput != null, args != null) != 1)
                throw new UsageException("Must use exactly one of --input, --namedinput, --girrinput and non-empty arguments");

            if (maxParameterWidth > FiniteBitField.MAXWIDTH) {
                logger.log(Level.WARNING, "The given value of --maxparameterwidth ({0}) is larger than {1}. This is using unspecified behavior, and the correct execution is not guaranteed.",
                        new Object[]{maxParameterWidth, FiniteBitField.MAXWIDTH});
                FiniteBitField.setAllowLargeBitfields(true);
            }

            if (input != null) {
                if (validate)
                    throw new UsageException("Cannot use --validate with --input.");
                ThingsLineParser<ModulatedIrSequence> irSignalParser = new ThingsLineParser<>(
                        (List<String> line) -> {
                            return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(frequency, trailingGap);
                        }, commandLineArgs.commentStart
                );
                List<ModulatedIrSequence> modSeqs = xslt == null
                        ? irSignalParser.readThings(input, commandLineArgs.inputEncoding, false)
                        : irSignalParser.readThings(input, xslt, commandLineArgs.inputEncoding, false);
                analyze(modSeqs, ModulatedIrSequence.frequencyAverage(modSeqs));
            } else if (namedInput != null) {
                if (validate)
                    throw new UsageException("Cannot use --validate with --namedinput.");
                ThingsLineParser<ModulatedIrSequence> thingsLineParser = new ThingsLineParser<>(
                        (List<String> line) -> {
                            return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(frequency, trailingGap);
                        }, commandLineArgs.commentStart
                );
                Map<String, ModulatedIrSequence> signals = xslt == null
                        ? thingsLineParser.readNamedThings(namedInput, commandLineArgs.inputEncoding)
                        : thingsLineParser.readNamedThings(namedInput, xslt, commandLineArgs.inputEncoding);
               if (signals.isEmpty())
                    throw new InvalidArgumentException("No parseable sequences found.");
                analyze(signals);
            } else if (girrInput != null) {
                if (validate)
                    throw new UsageException("Cannot use --validate with --girrinput.");

                ThingsLineParser<ModulatedIrSequence> thingsLineParser = new ThingsLineParser<>(
                        (List<String> line) -> {
                            return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(frequency, trailingGap);
                        }, commandLineArgs.commentStart
                );
                InputStream xsltStream = CommandAnalyze.class.getResourceAsStream(RAWGIRR2NAMEINPUT);
                Document xsltDoc = XmlUtils.openXmlStream(xsltStream, null, true, true);
                Map<String, ModulatedIrSequence> signals = thingsLineParser.readNamedThings(girrInput, xsltDoc, commandLineArgs.inputEncoding);
                if (signals.isEmpty()) {
                    throw new InvalidArgumentException("No parseable sequences found.");
                }
                analyze(signals);
            } else {
                MultiParser parser = MultiParser.newIrCoreParser(args);
                if (introRepeatEnding) {
                    IrSignal irSignal = (chop != null)
                            ? parser.toIrSignalChop(frequency, chop)
                            : parser.toIrSignal(frequency, trailingGap);
                    analyze(irSignal);
                } else if (chop != null) {
                    List<IrSequence> list = parser.toListChop(chop, trailingGap);
                    analyze(list);
                } else {
                    List<IrSequence> list = parser.toList(trailingGap);
                    if (list.size() > 1)
                        analyze(list);
                    else {
                        IrSignal irSignal = parser.toIrSignal(frequency, trailingGap);
                        if (irSignal != null)
                            analyze(irSignal);
                        else
                            throw new UsageException("Invalid signal, neither valid as Pronto nor as raw.");
                    }
                }
            }
        }

        private void analyze(IrSignal irSignal) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, UnsupportedEncodingException {
            Analyzer analyzer;
            Double freq = possiblyOverrideWithAnalyzeFrequency(irSignal.getFrequency());
            if (repeatFinder || dumpRepeatfinder) {
                IrSequence irSequence = irSignal.toModulatedIrSequence();
                analyzer = new Analyzer(irSequence, freq, true, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            } else {
                IrSignal fixedSignal = frequency != null ? new IrSignal(irSignal, frequency) : irSignal;
                analyzer = new Analyzer(fixedSignal, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            }
            analyze(analyzer, new ArrayList<>(0), irSignal);
        }

        private void analyze(Map<String, ModulatedIrSequence> modulatedIrSequences) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, UnsupportedEncodingException {
            Map<String, IrSequence> irSequences = new LinkedHashMap<>(modulatedIrSequences.size());
            irSequences.putAll(modulatedIrSequences);
            Double frequency = possiblyOverrideWithAnalyzeFrequency(ModulatedIrSequence.frequencyAverage(modulatedIrSequences.values()));
            analyze(irSequences, frequency);
        }

        private void analyze(Map<String, IrSequence> irSequences, Double frequency) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, UnsupportedEncodingException {
            if (irSequences.isEmpty())
                throw new InvalidArgumentException("No parseable sequences found.");
            Analyzer analyzer = new Analyzer(irSequences.values(), possiblyOverrideWithAnalyzeFrequency(frequency), repeatFinder || dumpRepeatfinder, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            analyze(analyzer, new ArrayList<>(irSequences.keySet()), null);
        }

        private void analyze(List<? extends IrSequence> irSequences) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, UnsupportedEncodingException {
            analyze(irSequences, frequency);
        }

        private void analyze(List<? extends IrSequence> irSequences, Double frequency) throws IrpException, IrCoreException, NoDecoderMatchException, UsageException, UnsupportedEncodingException {
            if (irSequences.isEmpty())
                throw new InvalidArgumentException("No parseable sequences found.");
            Analyzer analyzer = new Analyzer(irSequences, possiblyOverrideWithAnalyzeFrequency(frequency), repeatFinder || dumpRepeatfinder, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
            analyze(analyzer, new ArrayList<>(0), null);
        }

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        private void analyze(Analyzer analyzer, List<String> names, IrSignal inputSignal) throws IrpException, UsageException, IrCoreException, NoDecoderMatchException, UnsupportedEncodingException {
            List<Integer> parameterWidths = new ArrayList<>(parameterNamedWidths.size());
            List<String> parameterNames = new ArrayList<>(parameterNamedWidths.size());
            if (!parameterNamedWidths.isEmpty() && parameterNamedWidths.get(0).contains(":")) {
                for (String param : parameterNamedWidths) {
                    String[] arr = param.split(":");
                    if (arr.length != 2)
                        throw new UsageException("Must use name:width form for all or none of parameterWidth values.");
                    String name = arr[0];
                    if (!Name.validName(name))
                        throw new InvalidNameException(name);
                    parameterNames.add(name);
                    int width = Integer.parseInt(arr[1]);
                    parameterWidths.add(width);
                }
            } else {
                parameterNamedWidths.stream().map((param) -> Integer.valueOf(param)).forEachOrdered((width) -> {
                    parameterWidths.add(width);
                });
            }
            Burst.Preferences burstPrefs = new Burst.Preferences(maxRoundingError, maxUnits, maxMicroSeconds);
            Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(analyzer.getFrequency(), timeBase,
                    lsb ? BitDirection.lsb : BitDirection.msb,
                    extent, parameterWidths, maxParameterWidth, invert,
                    burstPrefs, parameterNames);

            if (statistics) {
                analyzer.printStatistics(out, params);
                out.println();
            }

            if (clean) {
                for (int i = 0; i < analyzer.getNoSequences(); i++) {
                    if (analyzer.getNoSequences() > 1)
                        out.print("#" + i + ":\t");
                    out.println(analyzer.cleanedIrSequence(i).toString(true));
                    if (statistics)
                        out.println(analyzer.toTimingsString(i));
                }
            }
            if (dumpRepeatfinder) {
                for (int i = 0; i < analyzer.getNoSequences(); i++) {
                    if (analyzer.getNoSequences() > 1)
                        out.print("#" + i + ":\t");
                    out.println(analyzer.repeatReducedIrSignal(i).toString(true));
                    out.println("RepeatFinderData: " + analyzer.repeatFinderData(i).toString());

                }
            }

            if (allDecodes) {
                List<List<Protocol>> protocols = analyzer.searchAllProtocols(params, decoder, commandLineArgs.regexp);
                int noSignal = 0;
                for (List<Protocol> protocolList : protocols) {
                    if (protocols.size() > 1)
                        out.print((names.isEmpty() ? "#" + noSignal : names.get(noSignal)) + ":\t");
                    if (statistics)
                        out.println(analyzer.toTimingsString(noSignal));
                    for (Protocol protocol : protocolList) {
                        if (parameterSpecs)
                            protocol.createParameterSpecsIfPossible();
                        printAnalyzedProtocol(protocol, radix, params.isPreferPeriods(), true, true);
                        if (validate) {
                            if (inputSignal != null)
                                validate(protocol, inputSignal);
                            else
                                out.println("Validation for current case not implemented.");
                        }
                    }
                    noSignal++;
                }
            } else {
                List<Protocol> protocols = analyzer.searchBestProtocol(params, decoder, commandLineArgs.regexp);
                if (parameterSpecs || girr || fatgirr)
                    protocols.forEach(protocol -> {
                        protocol.createParameterSpecsIfPossible();
                    });

                if (girr || fatgirr) {
                    Document doc = ProtocolListDomFactory.protocolListToDom(analyzer, protocols, names, radix, fatgirr);
                    XmlUtils.printDOM(out, doc, commandLineArgs.outputEncoding, "");
                } else {
                    int maxNameLength = IrCoreUtils.maxLength(names);
                    for (int i = 0; i < protocols.size(); i++) {
                        if (protocols.size() > 1)
                            out.print(names.isEmpty()
                                    ? ("#" + i + "\t")
                                    : (names.get(i) + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - names.get(i).length() + 1))));
                        if (statistics)
                            out.println(analyzer.toTimingsString(i));
                        printAnalyzedProtocol(protocols.get(i), radix, params.isPreferPeriods(), statistics, timings);
                        if (validate) {
                            if (inputSignal != null)
                                validate(protocols.get(i), inputSignal);
                            else
                                out.println("Validation for current case not implemented.");
                        }
                    }

                    if (bitUsage) {
                        out.println();
                        out.println("Bit usage analysis:");
                        Map<String, BitCounter> bitStatistics = BitCounter.scrutinizeProtocols(protocols);
                        bitStatistics.entrySet().forEach((kvp) -> {
                            out.println(kvp.getKey() + "\t" + kvp.getValue().toString() + (lsb ? " (note: lsb-first)" : "")
                                    + "\t" + kvp.getValue().toIntSequenceString());
                        });
                        //#if duplicates
                        try {
                            DuplicateFinder duplicateFinder = new DuplicateFinder(protocols, bitStatistics);
                            out.println("Duplicates analysis:");
                            Map<String, DuplicateFinder.DuplicateCollection> duplicates = duplicateFinder.getDuplicates();
                            duplicates.entrySet().forEach((kvp) -> {
                                out.println(kvp.getKey() + "\t" + kvp.getValue().toString()
                                        // wrong, and confusing...  + "\t" + kvp.getValue().getRecommendedParameterWidthsAsString()
                                        + (lsb ? " (note: lsb-first)" : ""));
                            });
                        } catch (NameUnassignedException ex) {
                            logger.warning("Duplicates analysis not possible due to different variables in the protocols.");
                        }
                        //#endif duplicates
                    }

                    if (parameterTable) {
                        out.println();
                        out.print("Parameter table:");
                        for (int i = 0; i < parameterNames.size(); i++)
                            out.print("\t" + parameterNames.get(i) + ":" + parameterWidths.get(i));
                        out.println();
                        for (int i = 0; i < protocols.size(); i++) {
                            if (protocols.size() > 1)
                                out.print(names != null
                                        ? (names.get(i) + (commandLineArgs.tsvOptimize ? "" : IrCoreUtils.spaces(maxNameLength - names.get(i).length() + 1)))
                                        : ("#" + i + "\t"));
                            Protocol protocol = protocols.get(i);
                            Iterable<String> parameters = parameterNames.isEmpty() ? protocol.getDefinitions().getNames() : parameterNames;
                            printParameters(parameters, protocol);
                        }
                    }
                }
            }
        }

        private void printParameters(Iterable<String> parameterNames, Protocol protocol) {
            for (String name : parameterNames) {
                try {
                    int length = protocol.guessParameterLength(name);
                    Number num = protocol.getDefinitions().get(name).toNumber();
                    out.print("\t" + num.formatIntegerWithLeadingZeros(radix, length));
                } catch (NameUnassignedException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            }
            out.println();
        }

        private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods, boolean printWeight, boolean printTimings) {
            if (protocol == null) {
                out.println();
                return;
            }

            Protocol actualProtocol = eliminateVars ? protocol.substituteConstantVariables() : protocol;
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
                } catch (DomainViolationException | NameUnassignedException | IrpInvalidArgumentException | InvalidNameException | OddSequenceLengthException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            }
        }

        private boolean validate(Protocol protocol, IrSignal inputSignal) throws IrpException, IrCoreException {
            IrSignal rendered = protocol.toIrSignal(new NameEngine());
            boolean success = rendered.approximatelyEquals(inputSignal);
            out.println(success
                    ? "Validation succeeded!"
                    : String.format("Validation failed: expected%n%s%ngot%n%s", inputSignal.toString(), rendered.toString()));
            return success;
        }
    }
}
