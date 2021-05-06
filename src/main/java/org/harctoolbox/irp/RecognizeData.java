/*
Copyright (C) 2017 Bengt Martensson.

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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

public final class RecognizeData extends Traverser implements Cloneable {

    private int position;
    private double hasConsumed;
    private ParameterCollector parameterCollector;
    private final IrSequence irSequence;
    private int extentStart;
    private final boolean interleaving;
    private ParameterCollector needsChecking;
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private BitwiseParameter danglingBitFieldData;
    private final double minimumLeadout;
    private int level;
    private final IrSignal.Pass pass;

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, ParameterSpecs parameterSpecs, IrSequence irSequence, int position,
            boolean interleaving, ParameterCollector parameterCollector, double absoluteTolerance, double relativeTolerance,
            double minimumLeadout, IrSignal.Pass pass) {
        super(generalSpec, definitions);
        danglingBitFieldData = new BitwiseParameter();
        this.position = position;
        this.hasConsumed = 0.0;
        this.irSequence = irSequence;
        this.parameterCollector = parameterCollector;
        this.extentStart = position;
        this.interleaving = interleaving;
        this.needsChecking = new ParameterCollector(parameterSpecs);
        this.absoluteTolerance = absoluteTolerance;
        this.relativeTolerance = relativeTolerance;
        this.minimumLeadout = minimumLeadout;
        this.pass = pass;
        this.level = 0;
    }

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, ParameterSpecs parameterSpecs,
            IrSequence irSequence, int beginPos, boolean interleavingOk,
            ParameterCollector names, Decoder.DecoderParameters params, IrSignal.Pass pass) {
        this(generalSpec, definitions, parameterSpecs, irSequence, beginPos, interleavingOk, names,
                params.getAbsoluteTolerance(), params.getRelativeTolerance(), params.getMinimumLeadout(), pass);
    }

    public RecognizeData() {
        this(new NameEngine());
    }

    public RecognizeData(NameEngine nameEngine) {
        this(new GeneralSpec(), nameEngine, new ParameterSpecs(), new IrSequence(), 0, false, new ParameterCollector(), new Decoder.DecoderParameters(), IrSignal.Pass.intro);
    }

    /**
     * Returns a shallow copy, except for the NameEngine, which is copied with NameEngine.clone().
     * @return
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public RecognizeData clone() {
        RecognizeData result;
        try {
            result = (RecognizeData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.setParameterCollector(getParameterCollector().clone());
        result.nameEngine = new NameEngine(this.nameEngine);
        return result;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the parameterCollector
     */
    public ParameterCollector getParameterCollector() {
        return parameterCollector;
    }

    /**
     * @param parameterCollector the parameterCollector to set
     */
    public void setParameterCollector(ParameterCollector parameterCollector) {
        this.parameterCollector = parameterCollector;
    }

    public void add(String name, BitwiseParameter parameter) throws ParameterInconsistencyException {
        Expression expression = getNameEngine().getPossiblyNull(name);
        if (expression == null) {
            parameterCollector.add(name, parameter);
        } else {
            BitwiseParameter expected = expression.toBitwiseParameter(this);
            boolean consistent = expected.isConsistent(parameter);
            if (!consistent)
                throw new ParameterInconsistencyException(name, expected, parameter);
            parameterCollector.add(name, parameter);

            // It has an expression, but is not presently checkable.
            // mark for later checking.
            Long bitmask = parameterCollector.getBitmask(name);
            if (bitmask == null || !parameter.isFinished(bitmask))
                needsChecking.add(name, parameter);
        }
    }


    public void add(Name name, BitwiseParameter value) throws ParameterInconsistencyException {
        add(name.toString(), value);
    }

    public void add(String name, long value) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value));
    }

    public boolean isOn() {
        return Duration.isOn(position);
    }

    public double get() {
        return position < irSequence.getLength()
                ? Math.abs(irSequence.get(position)) - getHasConsumed()
                : 0;
    }

    public void consume() {
        position++;
        hasConsumed = 0;
    }

    public void consume(double amount) {
        hasConsumed += amount;
    }

    /**
     */
    public void markExtentStart() {
        extentStart = position + 1;
    }

    /**
     * @return the hasConsumed
     */
    public double getHasConsumed() {
        return hasConsumed;
    }

    /**
     * @param hasConsumed the hasConsumed to set
     */
    public void setHasConsumed(double hasConsumed) {
        this.hasConsumed = hasConsumed;
    }

    public boolean leadoutOk(boolean isLast) throws SignalRecognitionException {
        return isLast && (get() >= minimumLeadout);
    }

    public boolean check(boolean on) {
        return isOn() == on && position < irSequence.getLength();
    }

    public double elapsed() {
//        if (position >= irSequence.getLength())
//            throw new ThisCannotHappenException("Internal error");
        return irSequence.getTotalDuration(extentStart, position - extentStart);
    }

    public double getExtentDuration() {
        int endPosition = Math.min(position + 1, irSequence.getLength());//IrCoreUtils.approximatelyEquals(hasConsumed, 0.0) ? position : position + 1;
        return irSequence.getTotalDuration(extentStart, endPosition - extentStart);
    }

    public boolean allowChopping() {
        return ! interleaving;
    }

    void checkConsistency() throws NameUnassignedException, ParameterInconsistencyException {
        needsChecking.checkConsistency(this);
        needsChecking = new ParameterCollector();
    }

    /**
     * @return the absoluteTolerance
     */
    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    /**
     * @return the relativeTolerance
     */
    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    /**
     * @return the danglingBitFieldData
     */
    BitwiseParameter getDanglingBitFieldData() {
        return danglingBitFieldData;
    }

    /**
     * @param data
     * @param bitmask
     */
    void setDanglingBitFieldData(long data, long bitmask) {
        danglingBitFieldData = new BitwiseParameter(data, bitmask);
    }

    void setDanglingBitFieldData() {
        danglingBitFieldData = new BitwiseParameter();
    }

    void finish() {
        if (hasConsumed > 0) {
            position++;
            hasConsumed = 0;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Level getLogLevel() {
        return level <= 2 ? Level.FINE
                : level == 3 ? Level.FINER
                : Level.FINEST;
    }

    public IrSignal.Pass getPass() {
        return pass;
    }

    LogRecord logRecordEnter(IrStreamItem item) {
        return logRecord(item, true);
    }

    LogRecord logRecordExit(IrStreamItem item) {
        return logRecord(item, false);
    }

    private LogRecord logRecord(IrStreamItem item, boolean enter) {
        LogRecord logRecord;
        if (item instanceof Numerical && !enter) {
            BitwiseParameter value = ((Numerical) item).toBitwiseParameter(this);
            logRecord = new LogRecord(getLogLevel(), "{0}Level {1}: \"{2}\", result: {3}");
            logRecord.setParameters(new Object[]{enter ? ">" : "<", level, item.toString(), value});
            return logRecord;
        }

        logRecord = new LogRecord(getLogLevel(), "{0}Level {1}: \"{2}\"");
        logRecord.setParameters(new Object[]{enter ? ">" : "<", level, item.toString()});

        return logRecord;
    }

    public LogRecord logRecordEnterWithIrStream(IrStreamItem item) {
        LogRecord logRecord= new LogRecord(getLogLevel(), "{0} {1}Level {2}: \"{3}\", IrSequence: {4}");
        logRecord.setParameters(new Object[]{this.getPass().toString(), ">", level, item.toString(), this.irSequence});

        return logRecord;
    }

    public void assignment(String nameString, long val) throws InvalidNameException {
        nameEngine.define(nameString, val);
    }

    public BitwiseParameter toBitwiseParameter(String name) {
        Expression expression = nameEngine.getPossiblyNull(name);
        return expression != null ? expression.toBitwiseParameter(this) : parameterCollector.get(name);
    }
}
