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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThisCannotHappenException;


public class CleanerParser extends MultiParser {

    private final static Logger logger = Logger.getLogger(CleanerParser.class.getName());
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private Cleaner cleaner = null;

    public CleanerParser(String source, Double absoluteTolerance, Double relativeTolerance) {
        super(source);
        this.absoluteTolerance = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
        this.relativeTolerance = IrCoreUtils.getRelativeTolerance(relativeTolerance);
    }

    public CleanerParser(Iterable<? extends CharSequence> args, Double absoluteTolerance, Double relativeTolerance) {
        super(args);
        this.absoluteTolerance = IrCoreUtils.getAbsoluteTolerance(absoluteTolerance);
        this.relativeTolerance = IrCoreUtils.getRelativeTolerance(relativeTolerance);
    }

    public Cleaner getCleaner() {
        return cleaner;
    }

    @Override
    public IrSequence toIrSequence(Double dummyGap) throws OddSequenceLengthException {
        IrSequence irSequence = super.toIrSequence(dummyGap);
        cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        return cleaner.toIrSequence();
    }

    @Override
    public List<IrSequence> toList(Double dummmyGap) throws OddSequenceLengthException {
        List<IrSequence> list = super.toList(dummmyGap);
        return clean(list);
    }

    @Override
    public List<IrSequence> toListChop(double threshold, Double dummyGap) throws OddSequenceLengthException {
        List<IrSequence> list = super.toListChop(threshold, dummyGap);
        return clean(list);
    }

    private List<IrSequence> clean(List<IrSequence> list) {
        IrSequence total = IrSequence.concatenate(list);
        cleaner = new Cleaner(total, absoluteTolerance, relativeTolerance);
        IrSequence cleanedTotal = cleaner.toIrSequence();
        List<IrSequence> result = new ArrayList<>(list.size());
        int pos = 0;
        for (IrSequence seq : list) {
            int length = seq.getLength();
            try {
                IrSequence segment = cleanedTotal.subSequence(pos, length);
                result.add(segment);
                pos += length;
            } catch (InvalidArgumentException ex) {
                throw new ThisCannotHappenException();
            }
        }
        return result;
    }

    @Override
    public ModulatedIrSequence toModulatedIrSequence(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        ModulatedIrSequence irSequence = super.toModulatedIrSequence(fallbackFrequency, dummyGap);
        cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        return new ModulatedIrSequence(cleaner.toIrSequence(), irSequence.getFrequency());
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
        IrSignal irSignal = super.toIrSignal(fallbackFrequency, dummyGap);
        return clean(irSignal);
    }

    @Override
    public IrSignal toIrSignalChop(Double fallbackFrequency, double threshold) throws OddSequenceLengthException, InvalidArgumentException {
        IrSignal irSignal = super.toIrSignalChop(fallbackFrequency, threshold);
        return clean(irSignal);
    }

    private IrSignal clean(IrSignal irSignal) throws InvalidArgumentException {
        IrSequence irSequence = irSignal.toModulatedIrSequence();
        cleaner = new Cleaner(irSequence, absoluteTolerance, relativeTolerance);
        IrSequence cleansed = cleaner.toIrSequence();
        return new IrSignal(cleansed, irSignal.getIntroLength(), irSignal.getRepeatLength(), irSignal.getFrequency(), irSignal.getDutyCycle());
    }
}
