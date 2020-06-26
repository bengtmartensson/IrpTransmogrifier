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
import java.io.IOException;
import java.util.List;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.UnknownProtocolException;
import org.xml.sax.SAXException;

@SuppressWarnings("PublicField")
public class CommandIrpDatabaseOptions extends CommandBasicOptions {

    @Parameter(names = {"-b", "--blacklist"}, description = "List of protocols to be removed from the data base")
    public List<String> blackList = null;

    @Parameter(names = {"-c", "--configfiles"}, listConverter = FileListParser.class,
            description = "Pathname(s) of IRP database file(s) in XML format. Default is the one in the jar file.")
    public List<File> configFiles = null;

    @Parameter(names = {"-i", "--irp"}, description = "Explicit IRP string to use as protocol definition.")
    public String irp = null;

    @Parameter(names = {"--validate"}, description = "Validate IRP database files against the schema, abort if not valid.")
    public boolean validate = false;

   public IrpDatabase setupDatabase() throws UsageException, IrpParseException, IOException, UnknownProtocolException, SAXException {
        if (configFiles != null && irp != null)
            throw new UsageException("At most one of --configfile and --irp can be specified");

        IrpDatabase.setValidating(validate);

        IrpDatabase irpDatabase = irp != null ? IrpDatabase.parseIrp("user_protocol", irp, "Protocol entered on the command line")
                : configFiles != null ? new IrpDatabase(configFiles)
                : new IrpDatabase((String) null);
        irpDatabase.remove(blackList);
        return irpDatabase;
    }
}