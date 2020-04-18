/*
Copyright (C) 2020 Bengt Martensson.

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
import java.util.logging.Level;

@SuppressWarnings("PublicField")
public class CommandLogOptions extends AbstractCommand {
    static final String[] loggingOptions = new String[] { "logclasses",  "logfile", "logformat", "logLevel", "xmlLog" };

    @Parameter(names = {"--logclasses"}, description = "List of (fully qualified) classes and their log levels, in the form class1:level1|class2:level2|...")
    public String logclasses = "";

    @Parameter(names = {"-L", "--logfile"}, description = "Log file. If empty, log to stderr.")
    public String logfile = null;

    @Parameter(names = {"-F", "--logformat"}, description = "Log format, as in class java.util.logging.SimpleFormatter.")
    public String logformat = "[%2$s] %4$s: %5$s%n";

    @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
            description = "Log level { OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL }")
    public Level logLevel = Level.WARNING;

    @Parameter(names = {"-x", "--xmllog"}, description = "Write the log in XML format.")
    public boolean xmlLog = false;
}
