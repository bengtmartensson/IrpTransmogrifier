/*
Copyright (C) 2017, 2018, 2019, 2020 Bengt Martensson.

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

package org.harctoolbox.irp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.analyze.NoDecoderMatchException;
import org.harctoolbox.cmdline.CmdLineProgram;
import org.harctoolbox.cmdline.CommandAnalyze;
import org.harctoolbox.cmdline.CommandBitField;
import org.harctoolbox.cmdline.CommandCode;
import org.harctoolbox.cmdline.CommandCommonOptions;
import org.harctoolbox.cmdline.CommandDecode;
import org.harctoolbox.cmdline.CommandDemodulate;
import org.harctoolbox.cmdline.CommandExpression;
import org.harctoolbox.cmdline.CommandLirc;
import org.harctoolbox.cmdline.CommandList;
import org.harctoolbox.cmdline.CommandRender;
import org.harctoolbox.cmdline.CommandVersion;
import org.harctoolbox.cmdline.ProgramExitStatus;
import org.harctoolbox.cmdline.UsageException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.ircore.OddSequenceLengthException;

/**
 * This class contains a command line main routine, allowing command line access to most things in the package.
 *
 * Basically, there should not be "too much" business logic here; we construct element and call its
 * member functions, defined elsewhere.
 */
public final class IrpTransmogrifier extends CmdLineProgram {

    /**
     * Configuration file to use if none specified. Taken from main jar file.
     */
    public static final String DEFAULT_CONFIG_FILE = "/IrpProtocols.xml"; // in jar-file
    private static final Logger logger = Logger.getLogger(IrpTransmogrifier.class.getName());

    /**
     *
     * @param args
     * @param out
     */
    private static void main(String[] args, PrintStream out) {
        IrpTransmogrifier instance = new IrpTransmogrifier(out);
        ProgramExitStatus status = instance.run(args);
        status.die();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        main(args, System.out);
    }

    private IrpDatabase irpDatabase = null;
    private final CommandCommonOptions commandLineArgs;
    private final CommandVersion commandVersion = new CommandVersion();
    private final CommandList commandList = new CommandList();
    private final CommandRender commandRender = new CommandRender();
    private final CommandDecode commandDecode = new CommandDecode();
    private final CommandDemodulate commandDemodulate = new CommandDemodulate();
    private final CommandAnalyze commandAnalyze = new CommandAnalyze();
    private final CommandCode commandCode = new CommandCode();
    private final CommandBitField commandBitField = new CommandBitField();
    private final CommandExpression commandExpression = new CommandExpression();
    private final CommandLirc commandLirc = new CommandLirc();

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public IrpTransmogrifier() {
        this(System.out);
    }

    public IrpTransmogrifier(PrintStream out) {
        super(out, new CommandCommonOptions(), Version.appName);
        setupCmds(commandVersion,
                commandList,
                commandRender,
                commandDecode,
                commandDemodulate,
                commandAnalyze,
                commandCode,
                commandBitField,
                commandExpression,
                commandLirc);
        commandLineArgs = (CommandCommonOptions) commandBasicOptions;
    }

    @Override
    public void extraSetup() {
        if (commandLineArgs.seed != null)
            ParameterSpec.initRandom(commandLineArgs.seed);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public ProgramExitStatus processCommand() {
        try {
            switch (command) {
                case "analyze":
                    commandAnalyze.analyze(out, commandLineArgs);
                    break;
                case "bitfield":
                    commandBitField.bitfield(out, commandLineArgs);
                    break;
                case "code":
                    irpDatabase = commandLineArgs.setupDatabase();
                    commandCode.code(out, commandLineArgs, irpDatabase, originalArguments);
                    break;
                case "decode":
                    irpDatabase = commandLineArgs.setupDatabase();
                    commandDecode.decode(out, commandLineArgs, irpDatabase);
                    break;
                case "demodulate":
                    commandDemodulate.demodulate(out, commandLineArgs);
                    break;
                case "expression":
                    commandExpression.expression(out, commandLineArgs);
                    break;
                case "help":
                    commandHelp.help(out, new CommandCommonOptions(), argumentParser, Version.documentationUrl);
                    break;
                case "lirc":
                    commandLirc.lirc(out, commandLineArgs.encoding);
                    break;
                case "list":
                    irpDatabase = commandLineArgs.setupDatabase();
                    commandList.list(out, commandLineArgs, irpDatabase);
                    break;
                case "render":
                    irpDatabase = commandLineArgs.setupDatabase();
                    commandRender.render(out, irpDatabase, commandLineArgs);
                    break;
                case "version":
                    irpDatabase = commandLineArgs.setupDatabase();
                    commandVersion.version(out, commandLineArgs, irpDatabase);
                    break;
                default:
                    return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_USAGE_ERROR, "Unknown command: " + command);
            }
        } catch (OddSequenceLengthException ex) {
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_SEMANTIC_USAGE_ERROR,
                    command.equals("render") ? "IrSequence does not end with a gap."
                    : ex.getLocalizedMessage() + ". Consider using --trailinggap.");
        } catch (IrpException | IrCoreException | UsageException | FileNotFoundException ex) {
            // Exceptions likely from silly user input, just print the exception
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (ParseCancellationException ex) {
            // When we get here,
            // Antlr has already written a somewhat sensible error message on
            // stderr; that is good enough for now.
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_USAGE_ERROR, ex.getLocalizedMessage());
        } catch (UnsupportedOperationException | IOException | IllegalArgumentException | SecurityException ex) {
            //if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
            // Likely a programming error or fatal error in the data base. Barf.
            ex.printStackTrace();
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_FATAL_PROGRAM_FAILURE, ex.getLocalizedMessage());
        } catch (IrpParseException ex) {
            // TODO: Improve error message
            if (commandLineArgs.logLevel.intValue() < Level.INFO.intValue())
                ex.printStackTrace();
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_USAGE_ERROR, "Parse error in \"" + ex.getText() + "\": " + ex.getLocalizedMessage());
        } catch (NoDecoderMatchException ex) {
            return new ProgramExitStatus(Version.appName, ProgramExitStatus.EXIT_SEMANTIC_USAGE_ERROR,
                    "No decoder matched \"" + ex.getMessage() +
                    "\". Use \"--decoder list\" to list the available decoders.");
        }
        return new ProgramExitStatus();
    }
}
