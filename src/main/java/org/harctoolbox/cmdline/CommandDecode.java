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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.harctoolbox.analyze.Cleaner;
import org.harctoolbox.analyze.RepeatFinder;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.ThingsLineParser;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.ElementaryDecode;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.ProtocolListDomFactory;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"decode"}, commandDescription = "Decode IR signal given as argument")
public class CommandDecode extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandDecode.class.getName());

    @Parameter(names = {"-a", "--all", "--no-prefer-over"}, description = "Output all decodes; ignore prefer-over.")
    private boolean noPreferOver = false;

//        @Parameter(names = { "-c", "--chop"}, description = "Chop input sequence into several using threshold (in milliseconds) given as argument.")
//        private Integer chop = null;
    @Parameter(names = {"-c", "--clean"}, description = "Invoke cleaner on signal") // ignored with --repeat-finder
    private boolean cleaner = false;

    @Parameter(names = { "--debugpattern"}, description = "Stop pattern for decoding", hidden = true)
    private String debugPattern = null;

    @Parameter(names = {"-f", "--frequency"}, converter = FrequencyParser.class, description = "Set modulation frequency.")
    private Double frequency = null;

    @Parameter(names = {"-g", "--girroutput"}, description = "Generate output in Girr format (only)")
    private boolean girr = false;

    @Parameter(names = {"-G", "--girrinput"}, description = "Read raw input in Girr format.")
    private String girrInput = null;

    @Parameter(names = {"-l", "--ignoreleadinggarbage"}, description = "Accept decodes starting with undecodable pairs.")
    private boolean ignoreLeadingGarbage = false;

    @Parameter(names = {"-i", "--input"}, description = "File/URL from which to take inputs, one per line.")
    private String input = null;

    // NOTE: Removing defaulted parameter is the default from the command line. In the API, the parameter is called
    // removeDefaulted and has the opposite semantics.
    @Parameter(names = {"-k", "--keep-defaulted"}, description = "In output, do not remove parameters that are equal to their defaults.")
    private boolean keepDefaultedParameters = false;

    @Parameter(names = {"-n", "--namedinput"}, description = "File/URL from which to take inputs, one line name, data one line.")
    private String namedInput = null;

    @Parameter(names = {"-p", "--protocol"}, description = "Comma separated list of protocols to try match (default all).")
    private String protocol = null;

    @Parameter(names = {"-r", "--repeatfinder"}, description = "Invoke repeat finder on input sequence")
    private boolean repeatFinder = false;

    @Parameter(names = {"-R", "--dump-repeatfinder"}, description = "Print the result of the repeatfinder.")
    private boolean dumpRepeatfinder = false;

    @Parameter(names = {"--radix"}, description = "Radix used for printing of output parameters.", validateWith = Radix.class)
    private int radix = 10;

    @Parameter(names = {"--recursive"}, description = "Apply decoder recursively, (for long signals).")
    private boolean recursive = false;

    @Parameter(names = {"-s", "--strict"}, description = "Require intro- and repeat sequences to match exactly.")
    private boolean strict = false;

    @Parameter(names = {"-T", "--trailinggap"}, description = "Trailing gap (in micro seconds) added to sequences of odd length.")
    private Double trailingGap = null;

    @Parameter(names = {"--xslt"}, description = "File/URL name of XSLT transformation that will be applied to --input or --namedinput argument")
    private String xslt = null;

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
                + "The common options --absolutetolerance --relativetolerance, --minrepeatgap determine how the repeat finder breaks the input data. ";
    }

    public void decode(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase) throws UsageException, IrpParseException, IOException, InvalidArgumentException, SAXException, TransformerException {
        DecodeClass decodeClass = new DecodeClass(out, commandLineArgs, irpDatabase);
        decodeClass.decode();
    }

    private class DecodeClass {

        private final PrintStream out;
        private final CommandCommonOptions commandLineArgs;
        private final IrpDatabase irpDatabase;
        private final Decoder.DecoderParameters decoderParams;
        private Decoder decoder;

        DecodeClass(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase) {
            this.out = out;
            this.commandLineArgs = commandLineArgs;
            this.irpDatabase = irpDatabase;
            this.decoderParams = newDecoderParameters();
        }

        private void decode() throws UsageException, IrpParseException, IOException, InvalidArgumentException, SAXException, TransformerException {
            CmdUtils.checkForOption("decode", args);

            if (IrCoreUtils.numberTrue(input != null, namedInput != null, girrInput != null, args != null) != 1)
                throw new UsageException("Must use exactly one of --input, --namedinput, --girrinput and non-empty arguments");
            if (ignoreLeadingGarbage && strict)
                throw new UsageException("--strict and --ignoreleadinggarbage may not be used together.");

            Decoder.setDebugProtocolRegExp(debugPattern);
            List<String> protocolNamePatterns = protocol == null ? null : Arrays.asList(protocol.split(","));
            List<String> protocolsNames = irpDatabase.evaluateProtocols(protocolNamePatterns, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
            if (protocolsNames.isEmpty())
                throw new UsageException("No protocol given or matched.");

            decoder = new Decoder(irpDatabase, protocolsNames);
            if (input != null) {
                ThingsLineParser<IrSignal> irSignalParser = new ThingsLineParser<>((List<String> line) -> {
                    return (MultiParser.newIrCoreParser(line)).toIrSignal(frequency, trailingGap);
                }, commandLineArgs.commentStart);
                List<IrSignal> signals = xslt == null
                        ? irSignalParser.readThings(input, commandLineArgs.inputEncoding, false)
                        : irSignalParser.readThings(input, xslt, commandLineArgs.inputEncoding, false);
                decode(signals);
            } else if (namedInput != null) {
                ThingsLineParser<IrSignal> irSignalParser = new ThingsLineParser<>((List<String> line) -> {
                    return (MultiParser.newIrCoreParser(line)).toIrSignal(frequency, trailingGap);
                }, commandLineArgs.commentStart);
                Map<String, IrSignal> signals = xslt == null
                        ? irSignalParser.readNamedThings(namedInput, commandLineArgs.inputEncoding)
                        : irSignalParser.readNamedThings(namedInput, xslt, commandLineArgs.inputEncoding);
                decode(signals);
            } else if (girrInput != null) {
                ThingsLineParser<IrSignal> irSignalParser = new ThingsLineParser<>((List<String> line) -> {
                    return (MultiParser.newIrCoreParser(line)).toIrSignal(frequency, trailingGap);
                }, commandLineArgs.commentStart);
                InputStream xsltStream = CommandAnalyze.class.getResourceAsStream(CommandAnalyze.RAWGIRR2NAMEINPUT);
                Document xsltDoc = XmlUtils.openXmlStream(xsltStream, null, true, true);
                Map<String, IrSignal> signals = irSignalParser.readNamedThings(girrInput, xsltDoc, commandLineArgs.inputEncoding);
                decode(signals);
            } else {
                MultiParser prontoRawParser = MultiParser.newIrCoreParser(args);
                IrSignal irSignal = prontoRawParser.toIrSignal(frequency, trailingGap);
                if (irSignal == null)
                    throw new UsageException("Could not parse as IrSignal: " + String.join(" ", args));
                decode(irSignal, null, 0);
            }
        }

        private void decode(List<IrSignal> signals) throws InvalidArgumentException, UnsupportedEncodingException {
            if (girr) {
                ProtocolListDomFactory factory = new ProtocolListDomFactory(radix);
                for (IrSignal irSignal : signals) {
                    Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes = mkDecodes(irSignal);
                    factory.add(decodes);
                }
                printAsGirr(factory);
            } else {
                for (IrSignal irSignal : signals)
                    decode(irSignal, null, 0);
            }
        }

        private void decode(Map<String, IrSignal> signals) throws InvalidArgumentException, UnsupportedEncodingException {
            if (girr) {
                ProtocolListDomFactory factory = new ProtocolListDomFactory(radix);
                for (Map.Entry<String, IrSignal> kvp : signals.entrySet()) {
                    Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes = mkDecodes(kvp.getValue());
                    factory.add(decodes, kvp.getKey());
                }
                printAsGirr(factory);
            } else {
                int maxNameLength = IrCoreUtils.maxLength(signals.keySet());
                for (Map.Entry<String, IrSignal> kvp : signals.entrySet())
                    decode(kvp.getValue(), kvp.getKey(), maxNameLength);
            }
        }

        private void decode(IrSignal irSig, String name, int maxNameLength) throws InvalidArgumentException, UnsupportedEncodingException {
            Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes = mkDecodes(irSig);
            if (girr) {
                ProtocolListDomFactory factory = new ProtocolListDomFactory(radix);
                factory.add(decodes);
                printAsGirr(factory);
            } else {
                printDecodes(decodes, name, maxNameLength);
            }
        }

        private Decoder.AbstractDecodesCollection<? extends ElementaryDecode> mkDecodes(IrSignal irSig) throws InvalidArgumentException {
            Objects.requireNonNull(irSig, "irSignal must be non-null");
            IrSignal irSignal = frequency != null ? new IrSignal(irSig, frequency) : irSig;
            Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes;

            if (cleaner) {
                irSignal = Cleaner.clean(irSignal, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance);
                logger.log(Level.INFO, "Cleansed signal: {0}", irSignal.toString(true));
            }

            if (repeatFinder) {
                ModulatedIrSequence sequence = irSignal.toModulatedIrSequence();
                RepeatFinder repeatFinder = new RepeatFinder(sequence, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.minRepeatGap);

                IrSignal fixedIrSignal = repeatFinder.toIrSignalClean(sequence);
                if (dumpRepeatfinder) {
                    out.println("RepeatReduced: " + fixedIrSignal);
                    out.println("RepeatData: " + repeatFinder.getRepeatFinderData());
                }
                decodes = decoder.decodeIrSignal(fixedIrSignal, decoderParams);
            } else
                decodes = decoder.decodeLoose(irSignal, decoderParams);

            return decodes;
        }

        private Decoder.DecoderParameters newDecoderParameters() {
            return new Decoder.DecoderParameters(strict, noPreferOver,
                    !keepDefaultedParameters, recursive, commandLineArgs.frequencyTolerance,
                    commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.minLeadout, commandLineArgs.override, ignoreLeadingGarbage);
        }

        private void printDecodes(Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes, String name, int maxNameLength) {
            if (name != null)
                out.print(name + ":" + (commandLineArgs.tsvOptimize ? "\t" : IrCoreUtils.spaces(maxNameLength - name.length() + 1)));
            decodes.println(out, radix, commandLineArgs.tsvOptimize ? "\t" : " ", commandLineArgs.quiet);
        }

        private void printAsGirr(ProtocolListDomFactory factory) throws UnsupportedEncodingException {
            Document doc = factory.toDocument();
            XmlUtils.printDOM(out, doc, commandLineArgs.outputEncoding, "");
        }
    }
}
