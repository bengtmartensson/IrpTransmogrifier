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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.CodeGenerator;
import org.harctoolbox.irp.DumpCodeGenerator;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.STCodeGenerator;
import org.harctoolbox.irp.UnknownProtocolException;
import org.harctoolbox.irp.UnsupportedRepeatException;
import org.harctoolbox.irp.Version;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"code"}, commandDescription = "Generate code for the given target(s)")
public class CommandCode extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandCode.class.getName());

    @Parameter(names = {"-d", "--directory"}, description = "Directory in whicht the generate output files will be written, if not using the --output option.")
    private String directory = null;

    @Parameter(names = {"--inspect"}, description = "Fire up stringtemplate inspector on generated code (if sensible)")
    private boolean inspect = false;

    @Parameter(names = {"-p", "--parameter"}, description = "Specify target dependent parameters to the code generators.")
    private List<String> parameters = new ArrayList<>(4);

    @Parameter(names = {"-s", "--stdirectory"}, description = "Directory containing st (string template) files for code generation.")
    private String stDir = System.getenv("STDIR") != null ? System.getenv("STDIR") : "st";

    @Parameter(names = {"-t", "--target"}, required = true, description = "Target(s) for code generation. Use ? for a list.")
    private List<String> target = new ArrayList<>(4);

    @Parameter(description = "protocols")
    private List<String> protocols;

    public void code(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase, String[] args) throws IrpInvalidArgumentException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, UsageException {
        CodeClass codeClass = new CodeClass(out, commandLineArgs, irpDatabase, args);
        codeClass.code();
    }

    private class CodeClass {

        private final PrintStream out;
        private final CommandCommonOptions commandLineArgs;
        private final IrpDatabase irpDatabase;
        private final String[] args;

        private CodeClass(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase, String[] args) {
            this.out = out;
            this.commandLineArgs = commandLineArgs;
            this.irpDatabase = irpDatabase;
            this.args = args;
        }

        private void code() throws IrpInvalidArgumentException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, UsageException {
            if (directory != null && commandLineArgs.output != null)
                throw new UsageException("The --output and the --directory options are mutually exclusive.");

            List<String> protocolNames = irpDatabase.evaluateProtocols(protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
            if (protocolNames.isEmpty())
                throw new UsageException("No protocols matched (forgot --regexp?)");

            if (protocolNames.size() > 1 && directory == null)
                logger.warning("Several protocol will be concatenated in one file. Consider using --directory.");

            STCodeGenerator.setStDir(stDir);
            for (String target : target)
                // Hardcoded selection of technologies for different targets
                if (target.equals("?"))
                    listTargets(out);
                else if (target.equalsIgnoreCase("xml"))
                    createXmlProtocols(protocolNames);
                else if (target.equalsIgnoreCase("dump"))
                    code(protocolNames, new DumpCodeGenerator());
                else {
                    if (!new File(stDir).isDirectory())
                        throw new IOException("Cannot find stdir = " + new File(stDir).getCanonicalPath());
                    code(protocolNames, target);
                }
        }

        private void code(Collection<String> protocolNames, String pattern) throws IrpInvalidArgumentException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, UsageException {
            File[] targets = IrCoreUtils.filesInDirMatchingRegExp(new File(stDir), pattern + STCodeGenerator.ST_GROUP_FILEEXTENSION);
            if (targets.length > 1 && directory == null)
                logger.warning("Several targets will be concatenated in one file. Consider using --directory.");
            for (File target : targets) {
                CodeGenerator codeGenerator;
                try {
                    codeGenerator = new STCodeGenerator(target);
                } catch (FileNotFoundException ex) {
                    throw new UsageException("Target " + target.getName() + " not available.  Available targets: " + String.join(" ", listTargets()));
                }
                code(protocolNames, codeGenerator);
            }
        }

        private void code(Collection<String> protocolNames, CodeGenerator codeGenerator) throws IrpInvalidArgumentException, IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, UsageException {
            Map<String, String> params = assembleParameterMap(parameters);
            if (directory != null)
                codeGenerator.generate(protocolNames, irpDatabase, new File(directory), inspect, params,
                        commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance,
                        getClass().getSimpleName(), Version.version, String.join(" ", args));
            else
                codeGenerator.generate(protocolNames, irpDatabase, out, inspect, params,
                        commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance,
                        getClass().getSimpleName(), Version.version, String.join(" ", args));

        }

        private List<String> listTargets() throws IOException {
            List<String> targets = STCodeGenerator.listTargets();
            targets.add("xml");
            targets.add("dump");
            targets.sort(String.CASE_INSENSITIVE_ORDER);
            return targets;
        }

        private void listTargets(PrintStream stream) throws IOException {
            stream.println(String.join(" ", listTargets()));
        }

        private void createXmlProtocols(List<String> protocolNames) {
            Document document = irpDatabase.toXml(protocolNames, commandLineArgs.absoluteTolerance, commandLineArgs.relativeTolerance, commandLineArgs.frequencyTolerance, commandLineArgs.override);
            XmlUtils.printDOM(out, document, commandLineArgs.outputEncoding, "Irp");
        }

        // TODO: nuke
        private Map<String, String> assembleParameterMap(List<String> paramStrings) throws UsageException {
            HashMap<String, String> result = new HashMap<>(paramStrings.size());
            for (String s : paramStrings) {
                String[] kvp = s.split(":");
                if (kvp.length != 2)
                    throw new UsageException("Wrong syntax for parameter:value");

                result.put(kvp[0], kvp[1]);
            }
            return result;
        }
    }
}
