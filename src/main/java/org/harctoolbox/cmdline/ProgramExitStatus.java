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

import java.io.PrintStream;

public class ProgramExitStatus {

    public final static int EXIT_SUCCESS               = 0;
    public final static int EXIT_USAGE_ERROR           = 1;
    public final static int EXIT_SEMANTIC_USAGE_ERROR  = 2;
    public final static int EXIT_FATAL_PROGRAM_FAILURE = 3;
    public final static int EXIT_INTERNAL_FAILURE      = 4;
    public final static int EXIT_CONFIG_READ_ERROR     = 5;
    public final static int EXIT_CONFIG_WRITE_ERROR    = 6;
    public final static int EXIT_IO_ERROR              = 7;
    public final static int EXIT_XML_ERROR             = 8;
    public final static int EXIT_DYNAMICLINK_ERROR     = 9;
    public final static int EXIT_THIS_CANNOT_HAPPEN    = 10;
    public final static int EXIT_INTERRUPTED           = 11;

    private static void doExit(int exitCode) {
        System.exit(exitCode);
    }

    public static void die(String progName, int exitStatus, String message) {
        new ProgramExitStatus(progName, exitStatus, message).die();
    }

    private final int exitStatus;
    private final String message;
    private final String programName;

    public ProgramExitStatus(String programName, int exitStatus, String message) {
        this.programName = programName;
        this.exitStatus = exitStatus;
        this.message = message;
    }

    public ProgramExitStatus() {
        this(null, EXIT_SUCCESS, null);
    }

    /**
     * @return the exitStatus
     */
    public int getExitStatus() {
        return exitStatus;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return exitStatus == EXIT_SUCCESS;
    }

    public void die() {
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        PrintStream stream = exitStatus == EXIT_SUCCESS ? System.out : System.err;
        if (message != null && !message.isEmpty())
            stream.println(message);
        if (exitStatus == EXIT_USAGE_ERROR) {
            stream.println();
            stream.println(programName != null
                    ? ("Use \"" + programName + " help\" or \"" + programName + " help --short\"\nfor command syntax.")
                    : "Usage error.");
       }
        doExit(exitStatus);
    }
}
