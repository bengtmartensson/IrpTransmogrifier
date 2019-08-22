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
import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.NameEngine;

    @SuppressWarnings("PublicField")

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal from parameters")
    public class CommandRender extends AbstractCommand {

        @Parameter(names = { "-#", "--count" }, description = "Generate am IR sequence with count number of transmissions")
        public Integer count = null;

        //@Parameter(names = { "-i", "--irp" }, description = "Explicit IRP string to use as protocol definition.")
        //private String irp = null;

        @Parameter(names = { "-m", "--modulate" }, description = "Generate modulated form (EXPERIMENTAL)")
        public boolean modulate = false;

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use", converter = NameEngineParser.class)
        public NameEngine nameEngine = new NameEngine();

        @Parameter(names = { "-p", "--pronto", "--ccf", "--hex" }, description = "Generate Pronto hex.")

        public boolean pronto = false;

        @Parameter(names = { "-P", "--printparameters", "--parameters" }, description = "Print used parameters values")
        public boolean printParameters = false;

        @Parameter(names = { "-r", "--signed-raw" }, description = "Generate raw form.")
        public boolean raw = false;

        @Parameter(names = { "-R", "--raw-without-signs" }, description = "Generate raw form without signs.")
        public boolean rawWithoutSigns = false;

        @Parameter(names = { "--random" }, description = "Generate random, valid, parameters")
        public boolean random = false;

        @Parameter(names = { "--number-repeats" }, description = "Generate an IR sequence containing the given number of repeats")
        public Integer numberRepeats = null;

        @Parameter(description = "protocol(s) or pattern (default all)"/*, required = true*/)
        public List<String> protocols = new ArrayList<>(0);

        @Override
        public String description() {
            return "This command is used to compute an IR signal from one or more protocols "
                    + "(\"render\" it). The protocol can be given either by name(s) "
                    + "(or regular expression if using the --regexp option), or, using the "
                    + "--irp options, given explicitly as an IRP form. "
                    + "The parameters can be either given directly with the -n option,"
                    + "or the --random option can be used to generate random, but valid parameters"
                    + "With the --count or --number-repeats option, instead an IR sequence is computed,"
                    + "containing the desired number of repeats.\n\n"
                    + "The syntax of the name engine is as in the IRP specification, for example: --nameengine {D=12,F=34}. "
                    + "For convenience, the braces may be left out. Space around the equal sign \"=\" and "
                    + "around the comma \",\" is allowed, as long as the name engine is still only one argument in the sense of the shell -- "
                    + "it may need to be enclosed within single or double quotes."
                    ;
        }
    }