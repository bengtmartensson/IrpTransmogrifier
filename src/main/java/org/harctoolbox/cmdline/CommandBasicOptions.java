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
import org.harctoolbox.ircore.IrCoreUtils;

@SuppressWarnings("PublicField")
public class CommandBasicOptions extends CommandLogOptions {

    @Parameter(names = {"-e", "--encoding", "--iencoding"}, description = "Encoding used to read input.")
    public String inputEncoding = IrCoreUtils.UTF8_NAME;

    @Parameter(names = {"-o", "--output"}, description = "Name of output file. Default: stdout.")
    public String output = null;

    @Parameter(names = {      "--oencoding"}, description = "Encoding used in generated output.")
    public String outputEncoding = IrCoreUtils.UTF8_NAME;

    @Parameter(names = {"-q", "--quiet"}, description = "Quitest possible operation, typically to be used from scripts.")
    public boolean quiet = false;

    @Parameter(names = {      "--version"}, description = "Report version. Deprecated; use the command \"version\" instead.")
    public boolean versionRequested = false;
}
