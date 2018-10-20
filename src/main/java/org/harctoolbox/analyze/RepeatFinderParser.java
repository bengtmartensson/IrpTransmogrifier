/*
Copyright (C) 2018 Bengt Martensson.

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

package org.harctoolbox.analyze;

import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignalParser;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;


public class RepeatFinderParser extends MultiParser {

    private final static Logger logger = Logger.getLogger(CleanerParser.class.getName());
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private final double minRepeatLastGap;
    private RepeatFinder repeatFinder = null;

    public RepeatFinderParser(List<IrSignalParser> parsers, String source) {
        this(parsers, source, null, null, null);
    }

    public RepeatFinderParser(List<IrSignalParser> parsers, String source, Double absoluteTolerance, Double relativeTolerance, Double minRepeatLastGap) {
        super(parsers, source);
        this.absoluteTolerance = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
        this.relativeTolerance = IrCoreUtils.getRelativeTolerance(relativeTolerance);
        this.minRepeatLastGap = IrCoreUtils.getMinRepeatLastGap(minRepeatLastGap);
    }

    public RepeatFinderParser(Iterable<? extends CharSequence> args) {
        this(args, null, null, null);
    }

    public RepeatFinderParser(Iterable<? extends CharSequence> args, Double absoluteTolerance, Double relativeTolerance, Double minRepeatLastGap) {
        super(args);
        this.absoluteTolerance = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
        this.relativeTolerance = IrCoreUtils.getRelativeTolerance(relativeTolerance);
        this.minRepeatLastGap = IrCoreUtils.getMinRepeatLastGap(minRepeatLastGap);
    }

    public RepeatFinder getRepeatFinder() {
        return repeatFinder;
    }

    /**
     * Tries to interpret the string argument as one of our known formats, and return an IrSignal.
     * Same as ProntoRawParser, but cleans up the IrSignal.
     *
     * @param fallbackFrequency Modulation frequency to use, if it cannot be inferred from the first parameter.
     * @param dummyGap
     * @return IrSignal, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        return toIrSignalAux(fallbackFrequency, dummyGap, false);
    }

    public IrSignal toIrSignalClean(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        return toIrSignalAux(fallbackFrequency, dummyGap, true);
    }

    public IrSignal toIrSignalClean() throws InvalidArgumentException {
        return toIrSignalAux(null, null, true);
    }

    private IrSignal toIrSignalAux(Double fallbackFrequency, Double dummyGap, boolean clean) throws InvalidArgumentException {
        IrSignal irSignal = super.toIrSignal(fallbackFrequency, dummyGap);
        if (irSignal != null && irSignal.getRepeatLength() > 0)
            return irSignal;

        ModulatedIrSequence modulatedIrSequence = irSignal != null
                ? irSignal.toModulatedIrSequence()
                : toModulatedIrSequence(fallbackFrequency, dummyGap);

        repeatFinder = new RepeatFinder(modulatedIrSequence, absoluteTolerance, relativeTolerance, minRepeatLastGap);
        return clean ? repeatFinder.toIrSignalClean(modulatedIrSequence) : repeatFinder.toIrSignal(modulatedIrSequence);
    }
}
