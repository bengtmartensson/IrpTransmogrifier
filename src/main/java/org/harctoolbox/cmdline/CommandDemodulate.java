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
import java.io.PrintStream;
import java.util.List;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.OddSequenceLengthException;

@Parameters(commandNames = {"demodulate"}, commandDescription = "Demodulate IrSequence given as argument (EXPERIMENTAL).")
public class CommandDemodulate extends AbstractCommand {

    //private static final Logger logger = Logger.getLogger(CommandDemodulate.class.getName());

    @Parameter(names = {"-t", "--threshold"}, description = "Threshold used for demodulating, in micro seconds.", converter = NameEngineParser.class)
    @SuppressWarnings("FieldMayBeFinal")
    private double threshold = ModulatedIrSequence.DEFAULT_DEMODULATE_THRESHOLD;

    @Parameter(description = "durations in micro seconds, alternatively pronto hex", required = true)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<String> args;

    @Override
    public String description() {
        return "This command demodulates its argument IrSequence, emulating the use of a demodulating IR receiver. "
                + "This means that all gaps less than or equal to the threshold are squeezed into the preceeding flash. "
                + "Typically the threshold is taken around the period of the expected modulation frequency.";
    }

    public void demodulate(PrintStream out, CommandCommonOptions commandLineArgs) throws OddSequenceLengthException, InvalidArgumentException {
        MultiParser prontoRawParser = MultiParser.newIrCoreParser(args);
        IrSequence irSequence = prontoRawParser.toIrSequence(commandLineArgs.minLeadout);
        ModulatedIrSequence demodulated = ModulatedIrSequence.demodulate(irSequence, threshold);
        out.println(demodulated.toString(true));
    }
}
