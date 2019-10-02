/*
Copyright (C) 2017, 2018 Bengt Martensson.

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Some useful static functions and constants.
 */
public final class IrCoreUtils {

    public final static long INVALID= -1L;
    public final static long ALL = -2L;
    public final static long SOME = -3L;

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Default absolute tolerance in micro seconds.
     */
    public static final double DEFAULT_ABSOLUTE_TOLERANCE = 100;

    /**
     * Default relative tolerance as a number between 0 and 1.
     */
    public static final double DEFAULT_RELATIVE_TOLERANCE = 0.3;

    /**
     * Default absolute tolerance for frequency comparison.
     */
    public static final double DEFAULT_FREQUENCY_TOLERANCE = 2000;

    /**
     * Default absolute tolerance for duty cycles.
     */
    public static final double DEFAULT_DUTYCYCLE_TOLERANCE = 0.3;

    /**
     * Default threshold value for lead-out in microseconds.
     */
    public static final double DEFAULT_MINIMUM_LEADOUT = 20000;

    /**
     * Default value for least value in a repeat in microseconds.
     */
    public static final double DEFAULT_MIN_REPEAT_LAST_GAP = 5000d;

    /**
     * "Dumb" Charset name
     */
    public static final String DEFAULT_CHARSET_NAME = "US-ASCII";

    /**
     * "Dumb" Charset
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

    private static double getDoubleWithSubstitute(Double userValue, double fallback) {
        return userValue != null ? userValue : fallback;
    }

    public static double getRelativeTolerance(Double userValue) {
        return getDoubleWithSubstitute(userValue, IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE);
    }

    public static double getAbsoluteTolerance(Double userValue) {
        return getDoubleWithSubstitute(userValue, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE);
    }

    public static double getFrequencyTolerance(Double userValue) {
        return getDoubleWithSubstitute(userValue, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE);
    }

    public static double getMinimumLeadout(Double userValue) {
        return getDoubleWithSubstitute(userValue, IrCoreUtils.DEFAULT_MINIMUM_LEADOUT);
    }

    public static double getMinRepeatLastGap(Double userValue) {
        return getDoubleWithSubstitute(userValue, IrCoreUtils.DEFAULT_MIN_REPEAT_LAST_GAP);
    }

    /**
     * Convert its argument from seconds to microseconds.
     * @param secs seconds
     * @return Argument converted to microseconds.
     */
    public static double seconds2microseconds(double secs) {
        return 1000000d * secs;
    }

    public static double milliseconds2microseconds(double ms) {
        return 1000d * ms;
    }

    public static double microseconds2milliseconds(double us) {
        return 0.001 * us;
    }

    public static double microseconds2seconds(double us) {
        return 0.000001 * us;
    }

    public static double khz2Hz(double khz) {
        return 1000d * khz;
    }

    public static double hz2khz(double frequency) {
        return 0.001 * frequency;
    }

    public static double us2Periods(double us, double frequency) {
        return microseconds2seconds(us) * frequency;
    }

    public static double percent2real(double percent) {
        return 0.01 * percent;
    }

    public static double real2percent(double x) {
        return 100d * x;
    }

    public static double l1Norm(Double[] sequence) {
        double sum = 0;
        for (Double d : sequence)
            sum += Math.abs(d);
        return sum;
    }

    public static double l1Norm(Iterable<Double> sequence) {
        double sum = 0;
        for (Double d : sequence)
            sum += Math.abs(d);

        return sum;
    }

    public static double l1Norm(double[] sequence) {
        return l1Norm(sequence, 0, sequence.length);
    }

    public static double l1Norm(double[] sequence, int beg, int length) {
        double sum = 0;
        for (int i = beg; i < beg + length; i++)
            sum += Math.abs(sequence[i]);
        return sum;
    }

    public static double l1Norm(List<Double> list, int beg) {
        double sum = 0;
        for (int i = beg; i < list.size(); i++)
            sum += Math.abs(list.get(i));
        return sum;
    }

    public static int l1Norm(int[] sequence, int beg, int length) {
        int sum = 0;
        for (int i = beg; i < beg + length; i++)
            sum += Math.abs(sequence[i]);
        return sum;
    }

    public static int l1Norm(int[] sequence) {
        return l1Norm(sequence, 0, sequence.length);
    }

    private static String chars(int length, byte value) {
        if (length <= 0)
            return "";

        byte[] buf = new byte[length];
        for (int i = 0; i < length; i++)
            buf[i] = value;
        return new String(buf, DEFAULT_CHARSET);
    }

    /**
     * Returns a string consisting of length spaces.
     * @param length
     * @return String of the requested length.
     */
    public static String spaces(int length) {
        return chars(length, (byte) 0x20);
    }

    /**
     * Returns a string consisting of length number of tabs.
     * @param length
     * @return
     */
    public static String tabs(int length) {
        return chars(length, (byte) 0x09);
    }

    public static long ones(Number numOnes) {
        long n = numOnes.longValue();
        if (n < 0 || n > Long.SIZE)
            throw new IllegalArgumentException("Argument must be non-negative and <= " + Long.SIZE);
        long result = 0L;
        for (int i = 0; i < n; i++)
            result = (result << 1) | 1L;
        return result;
    }

    public static String toCName(String name) {
        String newName = name.replaceAll("[^0-9A-Za-z_]", "");
        return newName.matches("\\d.*") ? ("X" + newName) : newName;
    }

    public static int numberTrue(Boolean... args) {
        int result = 0;
        for (boolean b : args) {
            if (b)
                result++;
        }
        return result;
    }

    /**
     * Either opens a file (optionally for appending (if beginning with +)) or returns stdout.
     *
     * @param filename Either - for stdout, or a file name, or null. If starting with +, the file is opened in append mode, after removing the +-character.
     * @return Open PrintStream
     * @throws FileNotFoundException if FileOutputStream does
     */
    public static PrintStream getPrintSteam(String filename) throws FileNotFoundException {
        if (filename == null)
            return null;

        String realFilename = filename.startsWith("+") ? filename.substring(1) : filename;
        try {
            return filename.equals("-")
                    ? System.out
                    : new PrintStream(new FileOutputStream(realFilename, filename.startsWith("+")), false, DEFAULT_CHARSET_NAME);
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
    }

    /**
     * Either opens an input file or returns stdin.
     *
     * @param filename
     * @return Open InputStream
     * @throws FileNotFoundException
     */
    public static InputStream getInputSteam(String filename) throws FileNotFoundException {
        return filename.equals("-") ? System.in : new FileInputStream(filename);
    }

    /**
     * Prints the String in the second argument nicely on the PrintStream in the first argument,
     * nicely chopping after lineLength positions. Respects linefeeds, tabs, etc.
     * @param out PrintStream to print on.
     * @param string String to print
     * @param lineLength Break after this position.
     */
    public static void trivialFormatter(PrintStream out, String string, int lineLength) {
        int pos = 0;
        boolean justBrokeLine = false;
        String[] data = string.split("((?<=\\s)|(?=\\s))"); // splits on whitspace, while keeping it
        for (String str : data) {
            if (pos <= 0 && str.matches(" ")) {
            } else if (pos == 0 && str.matches("\\v") && justBrokeLine) {
            } else
                out.print(str);
            justBrokeLine = false;
            pos = str.matches("\\v") ? 0 : pos + str.length();
            if (pos > lineLength) {
                out.println();
                pos = 0;
                justBrokeLine = true;
            }
        }
        if (pos > 0)
            out.println();
    }

    public static String formatIntegerWithLeadingZeros(long x, int radix, int length) {
        return radix == 2 ? formatIntegerBase2WithLeadingZeros(x, length)
                : radix == 8 ? formatIntegerBase8WithLeadingZeros(x, length)
                : radix == 16 ? formatIntegerBase16WithLeadingZeros(x, length)
                : formatIntegerBaseSomeWithLeadingZeros(x, radix, length);
    }

    private static String formatIntegerBase2WithLeadingZeros(long x, int length) {
        return pad(Long.toBinaryString(x), length, 1);
    }

    private static String formatIntegerBase8WithLeadingZeros(long x, int length) {
        return pad(Long.toOctalString(x), length, 3);
    }

    private static String formatIntegerBase16WithLeadingZeros(long x, int length) {
        return pad(Long.toHexString(x), length, 4);
    }

    private static String formatIntegerBaseSomeWithLeadingZeros(long x, int radix, int length) {
        return pad(Long.toString(x, radix), length, Math.log(radix)/Math.log(2.0));
    }

    private static String pad(String rawString, int length, double noBits) {
        StringBuilder str = new StringBuilder(rawString);
        int effectiveLength = (int) Math.ceil(length/noBits);
        while (str.length() < effectiveLength)
            str.insert(0, '0');
        return str.toString();
    }

    /**
     * The power function for long arguments.
     *
     * @param x long
     * @param y long, non-negative
     * @return x raised to the y'th power
     *
     * @throws ArithmeticException
     */
    public static long power(long x, long y) {
        if (y < 0)
            throw new ArithmeticException("power to a negative integer is not sensible here.");
        long r = 1;
        for (long i = 0; i < y; i++)
            r *= x;
        return r;
    }

    /**
     * Computes ceil(log2(x))
     * @param x
     * @return
     */
    public static long log2(long x) {
        if (x <= 0)
            throw new IllegalArgumentException("argument must be positive");
        long pow = 1;
        for (long n = 0; ; n++) {
            if (pow >= x)
                return n;
            pow *= 2;
        }
    }

    public static String radixPrefix(int radix) {
        return radix == 2 ? "0b"
                : radix == 8 ? "0"
                : radix == 16 ? "0x"
                : "";
    }

    /**
     * Parses integers of base 2 (prefix "0b"  or "%", 8 (leading 0), 10, or 16 (prefix "0x).
     * If argument special is true, allows intervals 123..456 or 123:456 by ignoring upper part.
     * and translates `*' to the constant "all" = (-2) and `#' to "some" (= -3).
     *
     * @param str String to be parsed
     * @param special If the special stuff should be interpreted ('*', '+', intervals).
     * @return long integer.
     */
    public static long parseLong(String str, boolean special) /*throws NumberFormatException*/ {
        if (special && (str.startsWith("#") || str.contains(",")))
            return SOME;

        String s = special ? str.replaceAll("[:.\\+<#].*$", "").trim() : str;
        if (special && (s.equals("*") || s.equals("'*'")))
            return ALL; // Just to help Windows' victims, who cannot otherwise pass a *.
        //s.equals("#") ? some :
        return s.startsWith("0x") ? Long.parseLong(s.substring(2), 16) :
               s.startsWith("0b") ? Long.parseLong(s.substring(2), 2) :
               s.startsWith("%") ? Long.parseLong(s.substring(1), 2) :
               s.equals("0") ? 0L :
               s.startsWith("0") ? Long.parseLong(s.substring(1), 8) :
               Long.parseLong(s);
    }

    /**
     * Parses integers of base 2 (prefix "0b"  or "%", 8 (leading 0), 10, or 16 (prefix "0x).
     *
     * @param str String to be parsed
     * @return long integer.
     */
    public static long parseLong(String str) {
        return parseLong(str,false);
    }

    public static long parseUpper(String str) {
        String[] s = str.split("\\.\\.");
        if (s.length == 1)
            s = str.split(":");

        return (s.length == 2) ? parseLong(s[1], false) : INVALID;
    }

    /**
     * Reverses the bits, living in a width-bit wide world.
     *
     * @param x data
     * @param width width in bits
     * @return bitreversed
     */

    public static long reverse(long x, int width) {
        long y = Long.reverse(x);
        if (width > 0)
            y >>>= Long.SIZE - width;
        return y;
    }

    public static BigInteger reverse(BigInteger x, int width) {
        try {
            if (width < Long.SIZE)
                return BigInteger.valueOf(reverse(x.longValueExact(), width));
        } catch (ArithmeticException ex) {
        }

        // A very inefficient implementation, but executed quite seldomly
        StringBuilder str = new StringBuilder(x.toString(2));
        if (str.length() > width)
            str.delete(0, str.length() - width);
        else
            while (str.length() < width)
                str.insert(0, '0');
        str.reverse();
        return new BigInteger(str.toString(), 2);
    }

    /**
     * Tests for approximate equality.
     *
     * @param x first argument
     * @param y second argument
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return true if either absolute or relative requirement is satisfied.
     */
    public static boolean approximatelyEquals(Double x, Double y, double absoluteTolerance, double relativeTolerance) {
        if (x == null && y == null)
            return true;
        if (x == null || y == null)
            return false;

        double absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        if (absoluteOk)
            return true;

        double max = Math.max(Math.abs(x), Math.abs(y));
        if (max < 1)
            return false;

        double relDiff = absDiff / max;
        return relDiff <= relativeTolerance;
    }

    public static boolean approximatelyEquals(Double x, Double y) {
        return approximatelyEquals(x, y, DEFAULT_ABSOLUTE_TOLERANCE, DEFAULT_RELATIVE_TOLERANCE);
    }

    /**
     * Tests for approximate equality.
     *
     * @param x first argument
     * @param y second argument
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return true if either absolute or relative requirement is satisfied.
     */
    public static boolean approximatelyEquals(int x, int y, int absoluteTolerance, double relativeTolerance) {
        int absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        if (absoluteOk)
            return true;
        int max = Math.max(Math.abs(x), Math.abs(y));
        boolean relativeOk = max > 0 && absDiff / (double) max <= relativeTolerance;
        return relativeOk;
    }

    public static long maskTo(long data, int width) {
        return data & ones(width);
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }

    public static String javaifyString(String s) {
        return s.replaceAll("\n\r?", " ").replaceAll("\\s\\s+", " ").replace("\"", "\\\"");
    }

    public static boolean hasDuplicatedElements(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            for (int j = i + 1; j < list.size(); j++)
                if (obj.equals(list.get(j)))
                    return true;
        }
        return false;
    }

    public static int approximateGreatestCommonDivider(List<Integer> args, double relTolerance) {
        if (args.isEmpty())
            throw new IllegalArgumentException();

        return approximateGreatestCommonDivider(null, args, relTolerance);
    }

    private static int approximateGreatestCommonDivider(Integer num, List<Integer> args, double relTolerance) {
        if (args.isEmpty())
            return num;
        int newNum = num == null ? args.get(0) : approximateGreatestCommonDivider(num, args.get(0), relTolerance);
        ArrayList<Integer> list = new ArrayList<>(args);
        list.remove(0);
        return approximateGreatestCommonDivider(newNum, list, relTolerance);
    }

    public static int approximateGreatestCommonDivider(int first, int second, double relTolerance) {
        return first > second ? approximateGCD(first, second, relTolerance)
                : approximateGCD(second, first, relTolerance);
    }

    private static int approximateGCD(int first, int second, double relTolerance) {
        int quot = (first + second/2)/second;
        int rest = Math.abs(first - quot * second);
        return rest/((double) second) <= relTolerance ? second : approximateGCD(second, rest, relTolerance);
    }

    public static int maxLength(Iterable<String> strings) {
        int max = 0;
        for (String s : strings) {
            int len = s.length();
            if (len > max)
                max = len;
        }
        return max;
    }

    public static int maxLength(String[] strings) {
        return strings != null ? maxLength(Arrays.asList(strings)) : 0;
    }

    public static void main(String[] args) {
        boolean hasOption = args[0].equals("-r");
        double relTolerance = hasOption ? Double.parseDouble(args[1]) : 0.0;
        ArrayList<Integer> data = new ArrayList<>(args.length);
        for (int i = hasOption ? 2 : 0; i < args.length; i++)
            data.add(Integer.parseInt(args[i]));

        int gcd = approximateGreatestCommonDivider(data, relTolerance);
        System.out.println(gcd);
    }

    public static File[] filesInDirMatchingRegExp(File dir, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        File[] selected = dir.listFiles((File dir1, String name) -> {
            return pattern.matcher(name).matches();
        });
        return selected;
    }

    public static double minDiff(TreeSet<Double> numbers) {
        double lowestYet = Double.MAX_VALUE;
        double last = -Double.MAX_VALUE;
        for (Double d : numbers) {
            double diff = d - last;
            if (diff < lowestYet)
                lowestYet = diff;
            last = d;
        }
        return lowestYet;
    }

    public static String basename(String filename) {
        return (new File(filename)).getName().split("\\.")[0];
    }

    private static boolean hasExtension(String filename) {
        int lastSeparator = filename.lastIndexOf(File.separator);
        int lastPeriod = filename.lastIndexOf('.');
        return lastPeriod > lastSeparator;
    }

    public static String addExtensionIfNotPresent(String filename, String extension) {
        return filename + ((extension != null && !hasExtension(filename)) ? ('.' + extension) : "");
    }

    public static int hashForDouble(Double d) {
        return d == null ? 0 : d.hashCode();
    }

    /**
     * Support function for lexicographic compareTos
     * @param compare
     * @return
     */
    public static int lexicalCompare(int... compare) {
        for (int c : compare)
            if (c != 0)
                return c;
        return 0;
    }

    private IrCoreUtils() {
    }
}
