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

@SuppressWarnings("PublicField")
public class CommandCommonOptions extends CommandDecodeParameterOptions {

    @Parameter(names = { "-C", "--commentStart"}, description = "Character(s) to be considered starting a line comment in input and namedInput files.")
    public String commentStart = null;

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
}
