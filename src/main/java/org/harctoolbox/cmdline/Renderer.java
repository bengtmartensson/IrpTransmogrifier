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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
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

public class Renderer {

    private static final Logger logger = Logger.getLogger(Renderer.class.getName());

    public static void render(PrintStream printStream, IrpDatabase irpDatabase, CommandRender commandRender, CommandCommonOptions commonOptions) throws UsageException, IOException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
        Renderer renderer = new Renderer(printStream, irpDatabase, commandRender, commonOptions);
        renderer.render();
   }

    private final CommandRender commandRender;
    private final PrintStream out;
    private final CommandCommonOptions commandLineArgs;
    private final IrpDatabase irpDatabase;

    private Renderer(PrintStream printStream, IrpDatabase irpDatabase, CommandRender commandRender, CommandCommonOptions commonOptions) {
        this.irpDatabase = irpDatabase;
        this.commandLineArgs = commonOptions;
        this.out = printStream;
        this.commandRender = commandRender;
    }

    private void render() throws UsageException, IOException, OddSequenceLengthException, UnknownProtocolException, InvalidNameException, DomainViolationException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, IrpParseException {
//        boolean finished = commandRender.process(this);
//        if (finished)
//            return;

        if (commandLineArgs.irp == null && (commandRender.random != commandRender.nameEngine.isEmpty()))
            throw new UsageException("Must give exactly one of --nameengine and --random, unless using --irp");

        if (commandLineArgs.irp != null) {
            if (!commandRender.protocols.isEmpty())
                throw new UsageException("Cannot not use --irp together with named protocols");
        }
//        setupDatabase();
//        irpDatabase.expand();
        List<String> list = irpDatabase.evaluateProtocols(commandRender.protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (list.isEmpty())
            throw new UsageException("No protocol matched.");
        for (String proto : list) {
            //logger.info(proto);
            NamedProtocol protocol = irpDatabase.getNamedProtocolExpandAlias(proto);
            render(protocol);
        }
    }

    private void render(NamedProtocol protocol) throws OddSequenceLengthException, DomainViolationException, IrpInvalidArgumentException, NameUnassignedException, UsageException, InvalidNameException {
        NameEngine nameEngine = !commandRender.nameEngine.isEmpty() ? commandRender.nameEngine
                : commandRender.random ? new NameEngine(protocol.randomParameters())
                        : new NameEngine();
        if (commandRender.random)
            logger.log(Level.INFO, nameEngine.toString());

        if (commandRender.printParameters)
            out.println(nameEngine.toString());

        if (!commandRender.pronto && !commandRender.raw && !commandRender.rawWithoutSigns && !commandRender.modulate && !commandRender.printParameters)
            logger.warning("No output requested. Use either --raw, --raw-without-signs, --pronto, --modulate, or --printparameters to get output.");
        IrSignal irSignal = protocol.toIrSignal(nameEngine);

        if (commandRender.count != null) {
            if (commandRender.numberRepeats != null)
                throw new UsageException("Can only specify one of --number-repeats and --count.");
            renderPrint(irSignal.toModulatedIrSequence(commandRender.count));
        } else if (commandRender.numberRepeats != null)
            renderPrint(irSignal.toModulatedIrSequence(true, commandRender.numberRepeats, true));
        else {
            if (commandRender.modulate)
                throw new UsageException("--modulate is only supported together with --number-repeats or --count.");
            renderPrint(irSignal);
        }
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
        if (commandRender.modulate)
            out.println(irSequence.modulate().toString(true));
    }
}
