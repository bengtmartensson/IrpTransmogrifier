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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.DomainViolationException;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.NamedProtocol;
import org.harctoolbox.irp.UnknownProtocolException;
import org.harctoolbox.irp.UnsupportedRepeatException;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"render"}, commandDescription = "Render signal from parameters")
public class CommandRender extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandRender.class.getName());

    @Parameter(names = {"-#", "--count"}, description = "Generate am IR sequence with count number of transmissions")
    private Integer count = null;

    @Parameter(names = {"-d", "--decode"}, description = "Send the rendered signal to the decoder (for debugging/development).")
    private boolean decode = false;

    @Parameter(names = {"-m", "--modulate"}, description = "Generate modulated form (EXPERIMENTAL)")
    private boolean modulate = false;

    @Parameter(names = {"-n", "--nameengine"}, description = "Name Engine to use", converter = NameEngineParser.class)
    private NameEngine nameEngine = new NameEngine();

    @Parameter(names = {"-p", "--pronto", "--ccf", "--hex"}, description = "Generate Pronto hex.")

    private boolean pronto = false;

    @Parameter(names = {"-P", "--printparameters", "--parameters"}, description = "Print used parameters values")
    private boolean printParameters = false;

    @Parameter(names = {"-r", "--signed-raw"}, description = "Generate raw form.")
    private boolean raw = false;

    @Parameter(names = {"-R", "--raw-without-signs"}, description = "Generate raw form without signs.")
    private boolean rawWithoutSigns = false;

    @Parameter(names = {"--random"}, description = "Generate random, valid, parameters")
    private boolean random = false;

    @Parameter(names = {"--number-repeats"}, description = "Generate an IR sequence containing the given number of repeats")
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
                + "containing the desired number of repeats.\n\n"
                + "The syntax of the name engine is as in the IRP specification, for example: --nameengine {D=12,F=34}. "
                + "For convenience, the braces may be left out. Space around the equal sign \"=\" and "
                + "around the comma \",\" is allowed, as long as the name engine is still only one argument in the sense of the shell -- "
                + "it may need to be enclosed within single or double quotes.";
    }

    public void render(PrintStream printStream, IrpDatabase irpDatabase, CommandCommonOptions commonOptions) throws UsageException, IOException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException, NamedProtocol.ProtocolNotRenderableException {
        Renderer renderer = new Renderer(printStream, irpDatabase, commonOptions);
        renderer.render();
    }

    private class Renderer {

        private final PrintStream out;
        private final CommandCommonOptions commandLineArgs;
        private final IrpDatabase irpDatabase;

        private Renderer(PrintStream printStream, IrpDatabase irpDatabase, CommandCommonOptions commonOptions) {
            this.irpDatabase = irpDatabase;
            this.commandLineArgs = commonOptions;
            this.out = printStream;
        }

        private void render() throws UsageException, IOException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException, NamedProtocol.ProtocolNotRenderableException {
            if (commandLineArgs.irp == null && (random != nameEngine.isEmpty()))
                throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

            if (commandLineArgs.irp != null) {
                if (!protocols.isEmpty())
                    throw new UsageException("Cannot not use --irp together with named protocols");
            }
            List<String> list = irpDatabase.evaluateProtocols(protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
            if (list.isEmpty())
                throw new UsageException("No protocol matched.");
            for (String proto : list) {
                //logger.info(proto);
                NamedProtocol protocol = irpDatabase.getNamedProtocolExpandAlias(proto);
                render(protocol);
            }
        }

        private void render(NamedProtocol protocol) throws OddSequenceLengthException, DomainViolationException, IrpInvalidArgumentException, NameUnassignedException, UsageException, InvalidNameException, NamedProtocol.ProtocolNotRenderableException, IrpParseException {
            if (nameEngine.isEmpty() && random) {
                nameEngine = new NameEngine(protocol.randomParameters());
                logger.log(Level.INFO, nameEngine.toString());
            }

            if (printParameters)
                out.println(nameEngine.toString());

            if (!pronto && !raw && !rawWithoutSigns && !modulate && !printParameters)
                logger.warning("No output requested. Use either --raw, --raw-without-signs, --pronto, --modulate, or --printparameters to get output.");
            NameEngine newNameEngine = nameEngine.clone();
            IrSignal irSignal = protocol.render(newNameEngine); // modifies its argument

            if (count != null) {
                if (numberRepeats != null)
                    throw new UsageException("Can only specify one of --number-repeats and --count.");
                renderPrint(irSignal.toModulatedIrSequence(count));
            } else if (numberRepeats != null)
                renderPrint(irSignal.toModulatedIrSequence(true, numberRepeats, true));
            else {
                if (modulate)
                    throw new UsageException("--modulate is only supported together with --number-repeats or --count.");
                renderPrint(irSignal);
            }
            if (decode)
                decode(irSignal, protocol.getName());
        }

        private void renderPrint(IrSignal irSignal) {
            if (raw)
                out.println(irSignal.toString(true));
            if (rawWithoutSigns)
                out.println(irSignal.toString(false));
            if (pronto)
                out.println(Pronto.toString(irSignal));
        }

        private void renderPrint(ModulatedIrSequence irSequence) {
            if (raw)
                out.println(irSequence.toString(true));
            if (rawWithoutSigns)
                out.println(irSequence.toString(false));
            if (pronto)
                out.println(Pronto.toString(new IrSignal(irSequence)));
            if (modulate)
                out.println(irSequence.modulate().toString(true));
        }

        private void decode(IrSignal irSignal, String name) throws IrpParseException {
            Decoder decoder = new Decoder(irpDatabase);
            Decoder.DecoderParameters decoderParams = new Decoder.DecoderParameters();
            decoderParams.setAllDecodes(true);
            // Remove defaulted parameters both from decode ...
            decoderParams.setRemoveDefaultedParameters(true);
            Decoder.SimpleDecodesSet decodes = decoder.decodeIrSignal(irSignal, decoderParams);
            Decoder.Decode dec = decodes.get(name);
            if (dec == null) {
                System.err.println("Decode failed.");
                return;
            }
            // ... and the NameEngine used for rendering (to be uniform)
            Map<String, Long> map = nameEngine.toMap();
            dec.getNamedProtocol().removeDefaulteds(map);
            NameEngine reducedNameEngine = new NameEngine(map);
            if (reducedNameEngine.numericallyEquals(dec.getMap()))
                System.err.println("Decode succeeded!");
            else {
                System.err.println("Decode failed. Actual decodes:");
                decodes.forEach((decode) -> {
                    out.println(decode);
                });
            }
        }
    }
}
