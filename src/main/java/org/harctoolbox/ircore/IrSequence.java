/*
Copyright (C) 2011, 2012, 2014, 2016, 2017 Bengt Martensson.

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
package org.harctoolbox.ircore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

/**
 * This class models an IR Sequence, which is a sequence of pulse pairs.
 *
 * <p>It consists of a sequence of positive real numbers, each representing a gap
 * (for odd indices) or flash (for even indices).
 * The duration of each flash or gap is the value of the entry in micro seconds."
 *
 * <p>The length of the IrSequence (as reported by getLength()) is therefore always even.
 * To get the "length in bursts", divide by 2.
 *
 * <p>The class is (almost) immutable: it can only be constructed and then read from.
 * Also note that there is no modulation frequency herein.
 *
 * @see ModulatedIrSequence
 *
 */
public class IrSequence implements Cloneable {

    private static final double EPSILON = 0.001;
    public static final double DUMMYGAPDURATION = 50000d; // should not translate to 0000 in Pronto
    private static Random random = null;

    /**
     * Initialize the random number generator of the class (used for addNoise(...)
     * in a predictable way, using the seed provided.
     * @param seed
     */
    public static void initRandom(long seed) {
        initRandom();
        random.setSeed(seed);
    }

    /**
     * Initialize the random number generator of the class (used for addNoise(...)
     * in a non-predictable way.
     */
    public static void initRandom() {
        random = new Random();
    }

    /**
     * Concatenates the elements in the argument.
     * @param sequences
     * @return new IrSequence
     */
    public static IrSequence concatenate(Collection<IrSequence> sequences) {
        int totalLength = 0;
        totalLength = sequences.stream().map((seq) -> seq.data.length).reduce(totalLength, Integer::sum);
        double[] data = new double[totalLength];
        int pos = 0;
        for (IrSequence seq : sequences) {
            System.arraycopy(seq.data, 0, data, pos, seq.data.length);
            pos += seq.data.length;
        }
        try {
            return new IrSequence(data);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

    public static IrSequence concatenate(IrSequence... sequences) {
        return concatenate(Arrays.asList(sequences));
    }

    private static boolean isFlash(int i) {
        return i % 2 == 0;
    }

    private static void checkOddLength(int length) throws OddSequenceLengthException {
        if (length % 2 != 0)
            throw new OddSequenceLengthException(length);
    }

    private static void checkOddLength(double[] data) throws OddSequenceLengthException {
        checkOddLength(data.length);
    }

    private static double[] fixOddLength(double[] data, Double dummyGapDuration) {
        if (data.length % 2 != 0 && dummyGapDuration != null) {
            double[] newData = new double[data.length + 1];
            System.arraycopy(data, 0, newData, 0, data.length);
            newData[data.length] = dummyGapDuration;
            return newData;
        } else {
            return data;
        }
    }

    public static List<Double> toInterleavingList(Collection<? extends Number> list) {
        if (list == null || list.isEmpty())
            return new ArrayList<>(0);

        List<Double> result = new ArrayList<>(list.size());
        Number previous = -1;
        for (Number num : list) {
            double value = num.doubleValue();
            if (result.isEmpty() && value <= 0)   // Nuke leading gaps
                continue;

            if (value == 0d)
                continue;

            if (equalSign(value, previous))
                result.set(result.size() - 1, result.get(result.size() - 1) + value);
            else
                result.add(value);
            previous = value;
        }
        return result;
    }

    private static boolean equalSign(Number x, Number y) {
        return x.doubleValue() < 0 == y.doubleValue() < 0;
    }

    public static int[] toInts(Iterable<IrSequence> list) {
        int length = 0;
        for (IrSequence seq : list)
            length += seq.getLength();

        int[] result = new int[length];
        int index = 0;
        for (IrSequence seq : list) {
            int[] array = seq.toInts();
            System.arraycopy(array, 0, result, index, array.length);
            index += array.length;
        }
        return result;
    }

    private static double[] toDoubles(Collection<? extends Number> collection) {
        double[] result = new double[collection.size()];
        int i = 0;
        for (Number num : collection) {
            result[i] = num.doubleValue();
            i++;
        }
        return result;
    }

    private static double[] stringsToDoubles(String[] collection) {
        double[] result = new double[collection.length];
        int i = 0;
        for (String str : collection) {
            result[i] = Double.parseDouble(str);
            i++;
        }
        return result;
    }

    public static double[] toDoublesPreservingSigns(String str) {
        if (str == null || str.trim().isEmpty())
            return new double[0];

        String[] strings = str.trim().split("[\\s,;]+");
        if (strings.length == 1)
            // Instead, try to break at "+" and "-" chars, preserving these
            strings = str.trim().split("(?=\\+)|(?=-)");
        return stringsToDoubles(strings);
    }

    private static double[] toDoubles(String string) {
        if (string == null || string.trim().isEmpty())
            return new double[0];
        String str = stripDecoratedString(string);
        String[] strings = str.trim().split("\\s+");
        return stringsToDoubles(strings);
    }

    /**
     * Removes all "decorations" like signs, commas and semicolons.
     * In particular, it is assumed that the numbers really are interleaving
     * flash and gaps.
     * @param in
     * @return
     */
    public static String stripDecoratedString(String in) {
        return in.replaceAll("[+\\-,;\\s]+", " ").trim();
    }

    public static String normalize(String in, Double dummyGap, boolean useSigns, String separator) {
        IrSequence irSequence = new IrSequence(in, dummyGap);
        return irSequence.toString(useSigns, separator, "", "");
    }

    /**
     * Duration data, all positive. Even indices are considered flashes, even ones gaps.
     * By definition they are interleaving.
     */
    private double[] data;

    /**
     * Constructs an empty IrSequence,
     */
    public IrSequence() {
        data = new double[0];
    }

    /**
     * Constructs an IrSequence from the parameter data.
     * The input data must be semantically compatible: the data at even indicies
     * are denoting flash; the ones at odd indicies gaps.
     * Signs on the input data are not evaluated, but disposed.
     * There must be an even number of entries.
     * @param inData Array of input durations.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public IrSequence(double[] inData) throws OddSequenceLengthException {
        checkOddLength(inData);
        setup(inData);
    }

    public IrSequence(double[] inData, Double dummyGapDuration) {
        setup(fixOddLength(inData, dummyGapDuration));
    }

    /**
     * Constructs an IrSequence from the parameter data.
     * @param inData
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public IrSequence(int[] inData) throws OddSequenceLengthException {
        checkOddLength(inData.length);
        setup(inData);
    }

    /**
     * Constructs an IrSequence from the parameter data.
     * @param collection Collection of numbers representing durations.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    public IrSequence(Collection<? extends Number> collection) throws OddSequenceLengthException {
        this(toDoubles(collection));
    }

    public IrSequence(String[] strings) throws OddSequenceLengthException {
        this(stringsToDoubles(strings));
    }

    public IrSequence(String string) throws OddSequenceLengthException {
        this(toDoubles(string));
    }

    /**
     * Constructs an IrSequence from the parameter data. It is assumed that
     * flashes and gaps are interleaved. Signs, commas, and semicolons are
     * ignored.
     *
     * @param string String of durations, possibly "decorated".
     * @param dummyGapDuration if the string consists of an odd number of
     * numbers, a gap of this length is silently adding to the sequence.
     */
    public IrSequence(String string, Double dummyGapDuration) {
        this(toDoubles(string), dummyGapDuration);
    }

    /**
     * Constructs an IrSequence from the parameter data.
     * @param idata Data
     * @param offset First index to be used
     * @param length Length of used subset of the idata array.
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public IrSequence(int[] idata, int offset, int length) throws InvalidArgumentException {
        if (offset >= idata.length && length != 0)
            throw new InvalidArgumentException("IrSequence: offset beyond end.");
        if (offset + length > idata.length)
            throw new InvalidArgumentException("IrSequence: length too large.");

        data = new double[length];
        for (int i = 0; i < length; i++)
            data[i] = Math.abs(idata[i+offset]);
    }

    /**
     * Constructs an IrSequence from the parameter data, by cloning.
     * @param src Original
     */
    public IrSequence(IrSequence src) {
        data = src.data.clone();
    }

    /**
     *
     * @param src
     * @param start
     * @param length
     * @throws InvalidArgumentException
     */
    public IrSequence(IrSequence src, int start, int length) throws InvalidArgumentException {
        if (start % 2 != 0 || length % 2 != 0)
            throw new OddSequenceLengthException("Start and length must be even");
        if (start + length > src.data.length)
            throw new InvalidArgumentException("Selection extends beyond end.");
        data = new double[length];
        System.arraycopy(src.data, start, data, 0, length);
    }

    private void setup(double[] inData) {
        this.data = new double[inData.length];
        int i = 0;
        for (double d : inData) {
            data[i] = Math.abs(d);
            i++;
        }
    }

    private void setup(int[] inData) {
        this.data = new double[inData.length];
        int i = 0;
        for (int d : inData) {
            data[i] = Math.abs(d);
            i++;
        }
    }

    /**
     * Returns the i'th value, a duration in micro seconds.
     * If i is even, it is a flash (light on), if i is odd, a gap (light off).
     * @param i index
     * @return duration in microseconds,
     */
    public final double get(int i) {
        return data[i];
    }

    /**
     * Returns an array of integers of durations.
     * This is a copy of the original data, so it might be manipulated without affecting the original instance.
     * @return integer array of durations in micro seconds.
     */
    public final int[] toInts() {
        int[] array = new int[data.length];
        for (int i = 0; i < data.length; i++)
            array[i] = (int) Math.round(data[i]);

        return array;
    }

    /**
     * Returns an array of doubles of durations.
     * This is a copy of the original data, so it might be manipulated without affecting the original instance.
     * @return double array of durations in micro seconds.
     */
    public final double[] toDoubles() {
        return data.clone();
    }

    /**
     * Returns an IrSequence consisting of this sequence, with repetitions
     * copies of the first argument appended.
     * @param tail IrSequence to be appended.
     * @param repetitions Number of copies to append.
     * @return new IrSequence
     */
    public IrSequence append(IrSequence tail, int repetitions) {
        double[] newData = new double[data.length + repetitions*tail.data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        for (int r = 0; r < repetitions; r++)
            System.arraycopy(tail.data, 0, newData, data.length + r*tail.data.length, tail.data.length);
        try {
            return new IrSequence(newData);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

    /**
     * Returns an IrSequence consisting of this sequence, with one
     * copy of the argument appended.
     * @param tail IrSequence to be appended.
     * @return new IrSequence
     */
    public IrSequence append(IrSequence tail) {
        return append(tail, 1);
    }

    /**
     * Appends a delay to the end of the IrSequence. Original is left untouched.
     * @param delay microseconds of silence to be appended to the IrSequence.
     * @return Copy of object with additional delay at end.
     * @throws InvalidArgumentException
     */
    public IrSequence append(double delay) throws InvalidArgumentException {
        if (data.length == 0)
            throw new InvalidArgumentException("IrSequence is empty");
        IrSequence irSequence = new IrSequence(this);
        irSequence.data[data.length-1] += delay;
        return irSequence;
    }

    /**
     * Creates and returns a copy of this object.
     * @return A copy of this IrSequence.
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public IrSequence clone() {
        IrSequence result;
        try {
            result = (IrSequence) super.clone();
            result.data = this.data.clone();
        } catch (CloneNotSupportedException ex) {
            throw new ThisCannotHappenException(ex);
        }
        return result;
    }

    /**
     * Returns a new IrSequence consisting of the length durations.
     * @param start Index of first duration
     * @param length Length of new sequence
     * @return IrSequence, a subsequence of the current
     * @throws InvalidArgumentException if length or start are not even.
     */
    public IrSequence subSequence(int start, int length) throws InvalidArgumentException {
        try {
            return new IrSequence(this, start, length);
        } catch (OddSequenceLengthException ex) {
            throw new ThisCannotHappenException();
        }
    }

    /**
     * Returns a new IrSequence consisting of the first length durations.
     * Equivalent to subSequence with first argument 0.
     * @param length Length of new sequence
     * @return IrSequence
     * @throws InvalidArgumentException if length not even.
     */
    public IrSequence truncate(int length) throws InvalidArgumentException {
        return subSequence(0, length);
    }

    /**
     * Chops a IrSequence in parts. Every gap of length &ge; threshold cause a cut.
     * @param threshold minimal gap in microseconds to cause a cut.
     * @return List of IrSequences
     */
    public List<IrSequence> chop(double threshold) {
        List<IrSequence> arrayList = new ArrayList<>(16);
        int beg = 0;
        for (int i = 1; i < data.length; i += 2) {
            if (data[i] >= threshold || i == data.length - 1) {
                double[] arr = new double[i - beg + 1];
                System.arraycopy(data, beg, arr, 0, i - beg + 1);
                try {
                    arrayList.add(new IrSequence(arr));
                } catch (OddSequenceLengthException ex) {
                    throw new ThisCannotHappenException();
                }
                beg = i + 1;
            }
        }
        return arrayList;
    }

    /**
     * Adds an amount to all flashes.
     *
     * @param amount Amount to add in microseconds.
     * @return New instance
     */
    public IrSequence addToFlashes(double amount) {
        IrSequence clone = clone();

        for (int i = 0; i < data.length; i += 2)
            clone.data[i] += amount;

        return clone;
    }

    /**
     * Adds an amount to all flashes, and subtract it from all gaps. For
     * generating test data for decoders etc.
     *
     * @param amount Amount (positive or negative) to add in microseconds.
     * @return new instance
     */
    public IrSequence addToGaps(double amount) {
        IrSequence clone = clone();

        for (int i = 1; i < data.length; i += 2)
            clone.data[i] += amount;

        return clone;
    }

    /**
     * Adds an amount to all flashes, and subtract it from all gaps. For
     * generating test data for decoders etc.
     *
     * @param amount Amount (positive or negative) to add in microseconds.
     * @return new instance
     */
    public IrSequence flashExcess(double amount) {
        return addToFlashes(amount).addToGaps(-amount);
    }

    /**
     * Adds a random number in the interval [-max, max) to each flash, and
     * subtract it from the next gap. For generating test data for decoders etc.
     * NOTE: initRandom must have been called before this call.
     *
     * @param max max amount to add/subtract, in microseconds.
     * @return new instance
     */
    public IrSequence addNoise(double max) {
        IrSequence clone = clone();

        for (int i = 0; i < data.length; i += 2) {
            double t = max * (2 * random.nextDouble() - 1);
            clone.data[i] += t;
            clone.data[i + 1] -= t;
        }
        return clone;
    }

    /**
     * Compares two IrSequences for equality.
     * @param irSequence to be compared against this.
     * @return equality
     */
    public boolean approximatelyEquals(IrSequence irSequence) {
        return approximatelyEquals(irSequence, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE, IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE);
    }

    /**
     * Compares two IrSequences for (approximate) equality.
     *
     * @param irSequence to be compared against this.
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @return equality within tolerance.
     */
    public boolean approximatelyEquals(IrSequence irSequence, double absoluteTolerance, double relativeTolerance) {
        if (irSequence == null || (data.length != irSequence.data.length))
            return false;

        for (int i = 0; i < data.length; i++)
            if (!IrCoreUtils.approximatelyEquals(data[i], irSequence.data[i], absoluteTolerance, relativeTolerance))
                return false;

        return true;
    }

    /**
     * Compares two segments of the current IrSequences for (approximate) equality.
     *
     * @param beginning start of first subsequence
     * @param compareStart start of second subsequence
     * @param length length to be compared
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @param lastLimit
     * @return if the subsequences are approximately equal.
     */
    public boolean approximatelyEquals(int beginning, int compareStart, int length, double absoluteTolerance, double relativeTolerance, double lastLimit) {
        boolean specialTreatment = compareStart + length == data.length && lastLimit > 0;
        for (int i = 0; i < (specialTreatment ? length - 1 : length); i++) {
            if (!IrCoreUtils.approximatelyEquals(data[beginning+i], data[compareStart+i], absoluteTolerance, relativeTolerance))
                return false;
        }

        if (specialTreatment) {
            if (!(
                    IrCoreUtils.approximatelyEquals(data[beginning+length-1], data[compareStart+length-1], absoluteTolerance, relativeTolerance)
                    || (data[beginning+length-1] >= lastLimit && data[compareStart+length-1] >= lastLimit)))
                return false;
        }
        return true;
    }

    /**
     * Compares two segments of the current IrSequences for (approximate) equality.
     *
     * @param beginning start of first subsequence
     * @param compareStart start of second subsequence
     * @param length length to be compared
     * @param absoluteTolerance tolerance threshold in microseconds.
     * @param relativeTolerance relative threshold, between 0 and 1.
     * @return if the subsequences are approximately equal.
     */
    public boolean approximatelyEquals(int beginning, int compareStart, int length, double absoluteTolerance, double relativeTolerance) {
        return IrSequence.this.approximatelyEquals(beginning, compareStart, length, absoluteTolerance, relativeTolerance, 0f);
    }

    /**
     * Returns the number of gaps and flashes. Always even.
     * Divide by 2 to get number of bursts.
     * @return number of gaps/flashes.
     */
    public final int getLength() {
        return data.length;
    }

    /**
     * Return last entry, or <code>null</code> if the data is empty.
     * @return last entry, or <code>null</code> if the data is empty.
     */
    public final Double getLastGap() {
        return data.length > 0 ? data[data.length - 1] : null;
    }

    /**
     *
     * @return emptyness of the sequence.
     */
    public final boolean isEmpty() {
        return data.length == 0;
    }

    /**
     * Returns true if and only if the sequence contains durations of zero length.
     * @return existence of zero durations.
     */
    public final boolean containsZeros() {
        for (double t : data)
            if (t < EPSILON)
                return true;
        return false;
    }

    /**
     * Replace all zero durations. Changes the signal in-place.
     * @param replacement Duration in micro seconds to replace zero durations with.
     * @return if the signal was changed.
     */
    public final boolean replaceZeros(double replacement) {
        boolean wasChanged = false;
        for (int i = 0; i < data.length; i++)
            if (data[i] < EPSILON) {
                data[i] = replacement;
                wasChanged = true;
            }
        return wasChanged;
    }

    /**
     * Computes the total duration of the IR sequence modeled.
     *
     * @return Length of the IR sequence in microseconds.
     */
    public final double getTotalDuration() {
        return getTotalDuration(0, data.length);
    }

    /**
     * Computes the total duration of a subsequence of the IR sequence modeled.
     *
     * @param begin start of subsequence.
     * @param length length of subsequence.
     * @return Length of the IR sequence in microseconds.
     */
    public double getTotalDuration(int begin, int length) {
        return IrCoreUtils.l1Norm(data, begin, length);
    }

    /**
     * Generates a pretty string representing the object.
     * @return nice string.
     */
    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean alternatingSigns) {
        return toString(alternatingSigns, ",", "[", "]");
    }

    /**
     * Generates a pretty string representing the object. If argument true, generate string with alternating signs.
     * @param alternatingSigns if true, generate alternating signs.
     * @param separator
     * @param prefix
     * @param suffix
     * @return nice string.
     */
    public String toString(boolean alternatingSigns, String separator, String prefix, String suffix) {
        StringJoiner stringJoiner = new StringJoiner(separator, prefix, suffix);
        for (int i = 0; i < data.length; i++) {
            String sign = alternatingSigns ? (isFlash(i) ? "+" : "-") : "";
            stringJoiner.add(sign + Long.toString(Math.round(data[i])));
        }
        return stringJoiner.toString();
    }
}
