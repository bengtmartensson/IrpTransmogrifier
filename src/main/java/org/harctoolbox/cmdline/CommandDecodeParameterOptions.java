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
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.Decoder;

@SuppressWarnings("PublicField")
public class CommandDecodeParameterOptions extends CommandIrpDatabaseOptions {

    @Parameter(names = {"-a", "--absolutetolerance"},
            description = "Absolute tolerance in microseconds, used when comparing durations. Default: " + IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE + ".")
    public Double absoluteTolerance = null;

    @Parameter(names = {"-f", "--frequencytolerance"}, converter = FrequencyParser.class,
            description = "Frequency tolerance in Hz. Negative disables frequency check. Default: " + IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE + ".")
    public Double frequencyTolerance = null;

    @Parameter(names = {"-g", "--minrepeatgap"}, description = "Minimum gap required to end a repetition.")
    public double minRepeatGap = IrCoreUtils.DEFAULT_MIN_REPEAT_LAST_GAP;

    @Parameter(names = {"--min-leadout"},
            description = "Threshold for leadout when decoding. Default: " + IrCoreUtils.DEFAULT_MINIMUM_LEADOUT + ".")
    public Double minLeadout = null;

    @Parameter(names = {"-O", "--override"}, description = "Let given command line parameters override the protocol parameters in IrpProtoocols.xml")
    public boolean override = false;

    @Parameter(names = {"-r", "--relativetolerance"}, validateWith = LessThanOne.class,
            description = "Relative tolerance as a number < 1. Default: " + IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE + ".")
    public Double relativeTolerance = null;

    public Decoder.DecoderParameters decoderParameters() {
        return new Decoder.DecoderParameters(false /*strict*/, false /*noPreferOver*/,
                true /*!keepDefaultedParameters*/, false/*recursive*/, frequencyTolerance,
                absoluteTolerance, relativeTolerance, minLeadout, override, false/*ignoreLeadingGarbage*/);
    }
}