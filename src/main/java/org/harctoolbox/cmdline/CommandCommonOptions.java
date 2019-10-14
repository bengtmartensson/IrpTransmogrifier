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
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import org.harctoolbox.ircore.IrCoreUtils;

@SuppressWarnings("PublicField")

public class CommandCommonOptions {

    // JCommander does not know about our defaults being null, so handle this explicitly-
    @Parameter(names = {"-a", "--absolutetolerance"},
            description = "Absolute tolerance in microseconds, used when comparing durations. Default: " + IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE + ".")
    public Double absoluteTolerance = null;

    @Parameter(names = {"-b", "--blacklist"}, description = "List of protocols to be removed from the data base")
    public List<String> blackList = null;

    @Parameter(names = {"-c", "--configfile"}, listConverter = FileListParser.class,
            description = "Pathname of IRP database file in XML format. Default is the one in the jar file.")
    public List<File> configFiles = null;

    @Parameter(names = { "-C", "--commentStart"}, description = "Character(s) to be considered starting a line comment in input and namedInput files.")
    public String commentStart = null;

    // Some day there will possibly be a commentEnd?

    @Parameter(names = {"-e", "--encoding"}, description = "Encoding used in generated output.")
    public String encoding = "UTF-8";

    @Parameter(names = {"-f", "--frequencytolerance"}, converter = FrequencyParser.class,
            description = "Frequency tolerance in Hz. Negative disables frequency check. Default: " + IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE + ".")
    public Double frequencyTolerance = null;

    @Parameter(names = {"-g", "--minrepeatgap"}, description = "Minimum gap required to end a repetition.")
    public double minRepeatGap = IrCoreUtils.DEFAULT_MIN_REPEAT_LAST_GAP;

    @Parameter(names = {"-h", "--help", "-?"}, help = true, description = "Display help message. Deprecated; use the command \"help\" instead.")
    public boolean helpRequested = false;

    @Parameter(names = {"-i", "--irp"}, description = "Explicit IRP string to use as protocol definition.")
    public String irp = null;

    @Parameter(names = {"--logclasses"}, description = "List of (fully qualified) classes and their log levels.")
    public String logclasses = "";

    @Parameter(names = {"-L", "--logfile"}, description = "Log file. If empty, log to stderr.")
    public String logfile = null;

    @Parameter(names = {"-F", "--logformat"}, description = "Log format, as in class java.util.logging.SimpleFormatter.")
    public String logformat = "[%2$s] %4$s: %5$s%n";

    @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
            description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
    public Level logLevel = Level.WARNING;

    @Parameter(names = {"--min-leadout"},
            description = "Threshold for leadout when decoding. Default: " + IrCoreUtils.DEFAULT_MINIMUM_LEADOUT + ".")
    public Double minLeadout = null;

    @Parameter(names = {"-o", "--output"}, description = "Name of output file. Default: stdout.")
    public String output = null;

    @Parameter(names = {"-O", "--override"}, description = "Let given command line parameters override the protocol parameters in IrpProtoocols.xml")
    public boolean override = false;

    @Parameter(names = {"-q", "--quiet"}, description = "Quitest possible operation, typically to be used from scripts.")
    public boolean quiet = false;

    @Parameter(names = {"-r", "--relativetolerance"}, validateWith = LessThanOne.class,
            description = "Relative tolerance as a number < 1. Default: " + IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE + ".")
    public Double relativeTolerance = null;

    @Parameter(names = {"--regexp"}, description = "Interpret protocol/decoder argument as regular expressions.")
    public boolean regexp = false;

    @Parameter(names = {"-s", "--sort"}, description = "Sort the protocols alphabetically on output.")
    public boolean sort = false;

    @Parameter(names = {"--seed"},
            description = "Set seed for the pseudo random number generation. If not specified, will be random, different between program invocations.")
    public Long seed = null;

    @Parameter(names = {"-t", "--tsv", "--csv"}, description = "Use tabs in output to optimize for the import in spreadsheet programs as cvs.")
    public boolean tsvOptimize = false;

    @Parameter(names = {"-u", "--url-decode"}, description = "URL-decode protocol names, (understanding %20 for example).")
    public boolean urlDecode = false;

    @Parameter(names = {"-v", "--version"}, description = "Report version. Deprecated; use the command \"version\" instead.")
    public boolean versionRequested = false;

    @Parameter(names = {"-x", "--xmllog"}, description = "Write the log in XML format.")
    public boolean xmlLog = false;
}
