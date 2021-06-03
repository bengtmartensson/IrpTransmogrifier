# IrpTransmogrifier
[![Java CI with Maven + Upload](https://github.com/bengtmartensson/IrpTransmogrifier/actions/workflows/maven.yml/badge.svg)](https://github.com/bengtmartensson/IrpTransmogrifier/actions/workflows/maven.yml)

Parser for IRP notation protocols, with rendering, code generation, recognition applications.

This is a new program, written from scratch, that is intended to replace IrpMaster, DecodeIR, and (most of) ExchangeIR,
and much more like potentially replacing "all" hand written decoders/renderers.
The project consists of an API library that is also callable from the command line as a command line program.
A GUI is possible, as well as the integration in GUI programs, like IrScrutinizer.

## Acknowledgement
I would like to acknowledge the influence of the [JP1 forum](http://hifi-remote.com/forums/index.php), both the programs
(in particular of course [DecodeIR](http://www.hifi-remote.com/wiki/index.php?title=DecodeIR), and the discussions
(in particular with Dave Reed ("3FG") and Graham Dixon ("mathdon")).
This work surely would not exist without the JP1 forum.

## Background
This program can be considered as a successor of [IrpMaster](http://www.harctoolbox.org/IrpMaster,html).
The Irp parser therein is based upon a [ANTLR3 grammar](http://www.antlr.org). The "new version"
ANTLR4 is really not a new version, but a completely different tool, fixing most of the quirks that were irritating
in IrpMaster (like the "ugliness" of embedding actions within the grammar, no left recursion, and much more).
Unfortunately, this means that using version 4 instead of version 3 is not like updating a compiler or such,
but "necessitates" a complete rewrite of the actions.

But that is of course not all: Both DecodeIR by John Fine and the Analyzer by Graham Dixon have shown to be
essentially impossible to maintain and extend. Although DecodeIR and IrpMaster (through the data base `IrpProtocols.ini`)
agree on most (but not all) protocols, this consists of two different, dis-coupled sources of protocol information. Finally, to be able to
generate code from the IRP form is a natural wish.

## Use cases
The reason for this project is not (just) to migrate IrpMaster to ANTLR4 -- version 3 is still operational,
and no more broken than it was at the start. There are a number of interesting use cases for a nice parser/tree traverser:

### Rendering
This use case corresponds to IrpMaster. It is probably to be implemented as a traversing of the parse tree of an IRP protocol,
with numerical values assigned to the parameters.

In the present implementation, only one infinite repeat is allowed.
(A realistic protocol, or use case, requiring more than
one infinite repeat is not known to me.) Also, individual bitfields are restricted to 63 bits of length or less.
(This is inherited from the use of Java's long type. It [may be removed in the future](https://github.com/bengtmartensson/IrpTransmogrifier/issues/38).)
With the exception of these restriction, the implementations should be complete, down to specification holes.
There are also a few extensions to the IRP notation as described in the [official documentation](http://www.hifi-remote.com/wiki/index.php/IRP_Notation).

In IrScrutinizer, the word "generate" is used instead of "render". These words can be considered as synonyms (here).

### Recognition
This use case corresponds to a dynamic version of DecodeIR: given a numerical IR signal,
find the parameter/protocol combination(s) that could have generated the given signal.
This is implemented by trying to parse the given signal with respect to the candidate protocols.
It is thus very systematic, but [comparatively slow](https://github.com/bengtmartensson/IrpTransmogrifier/issues/44).

It is not claimed that all protocols in the protocol data base are recognizable.
Non-recognizable protocols are to be marked by setting the `decodable` parameter to `false`.
To be recognizable, the IRP protocol should preferably adhere to some additional rules:

* The "+" form of repetitions is discouraged in favor of the "*" form.
* The width and shift of a Bitfield must be constant.
* The decoder is capable of _simple_ equation solving (e.g. `Arctech`), but not of complicated equation solving.

Presently all but two protocols (`zenith`, `nec1-shirriff`, (bitfield width as parameter), `fujitsu_aircon` (would require non-trivial equation solving))
are recognizable.  It is not guaranteed that new protocols automatically will be recognizable.

#### Loose matches, Guessing
Many captured signals are not quite correct according to their protocol. However, the firmware in a receiving device is often "forgiving",
and accepts slightly flawed signals. It is thus desirable for a program of this type to find a near match, "guess", when an real match fails.
This is known as loose mode, opposite of "strict" mode. For practical reasons, the loose mode is the default in the command line usage.
The strict mode is enabled using the decode option `--strict`.

### Code generation for rendering and/or decoding
For a particular protocol, generate target code (C, C++, Java, Python,...) that can render or decode signals
with the selected protocol. As opposed to the previous use cases, efficency (memory, execution time) (for the generated code) is potentially
an issue. This should be able to generate protocol renders for e.g. the Arduino libraries IrRemote, IrLib, and AGirs.
At least in the first version, not all protocols describable by IRPs need to be supported. Not implemented in the first phase: Protocols with hierarchical bitspecs
(rc6*, replay, arctech, entone), protocols with bitspec lenght as parameter (zenith, nec1-shirrif). Also default are not implemented, e.g. NEC1 has to be
called with 3 parameters.

Two mechanisms are available: XML and [Stringtemplate](http://www.stringtemplate.org/).
The program does not come with an XSLT engine, so this has to be invoked independently on the XML export.
The program just invokes the template, without caring what it does; if it generates a renderer or decoder.
The user is instead governs this by invoking the style sheets or templates (s)he want using the `--target` (`-t`) option to the `code` sub subcommand.
(For this reason, there is no `--renderer` or `--decoder` option to the `code` sub command.)
It is also possible to pass
target-specific parameters to the code generators using the `--parameter` (`-p`) argument.

Targets:
* [Lircd.conf](http://lirc.org/html/lircd.conf.html) generation from IrScrutinizer. This is based on an XSLT-transformation (`lirc.xsd`) and generates
 [an XSLT (version 1) file that can work with IrScrutinizer](https://github.com/bengtmartensson/harctoolboxbundle/blob/master/IrScrutinizer/src/main/config/exportformats.d/lirc.xml).
Handling of definitions as well as expressions as bitfields not implemented, as well as a few other things (search for "omitted" in the above file),
otherwise works. "90% complete", see [this issue](https://github.com/bengtmartensson/IrpTransmogrifier/issues/6).
To create: see (or execute) the shell script `tools/generate-lirc.sh`. In short, this generates the xml export, and then invokes
xslt transformations on that xml file.
* Java. Essentially for testing. This is essentially working both for rendering and decoding, including a generated test rig
(see the [test project](https://github.com/bengtmartensson/JavaIrpProtocolTest)). Targets: `java-decoder java-decoder-test java-renderer java-renderer-test`.
Not quite finished, see [this issue](https://github.com/bengtmartensson/IrpTransmogrifier/issues/25).

Some possible future targets:
* [Infrared4Arduino](https://github.com/bengtmartensson/Infrared4Arduino).
* [IRremote](https://github.com/z3t0/Arduino-IRremote)
* Linux kernel modules in [linux/drivers/media/rc](https://github.com/torvalds/linux/tree/master/drivers/media/rc) (decoding only).

### General code analysis
Not really connected to parsing IRP, but fits in the general framework.
This has been inspired by to the Analyzer and the RepeatFinder in
[Graham Dixon's ExchangeIR](http://www.hifi-remote.com/forums/dload.php?action=file&file_id=8460)
([Java translation](https://sourceforge.net/p/controlremote/code/HEAD/tree/trunk/exchangeir/)).

## Protocol Data Base
The "ini"-file `IrpProtocols.ini` of IrpMaster has been replaced by an XML file,
per default called `IrpProtocols.xml`. The XML format
is defined by the W3C schema [irp-protocols](http://www.harctoolbox.org/schemas/irp-protocols.xsd), and has the name space
`http://www.harctoolbox.org/irp-protocols`. This format has many advantages in
comparison with the simpler previous format, for example, it can contain embedded XHTLM fragments.
It also can contain different parameters that can be used by different programs, for example, tolerance parameters
for decoding.
Arbitrary string-valued parameters are permitted. It is up to an interpreting program to determine the semantic.

There is also an XSLT stylesheet, which technically translates the XML to HTML, allowing for a user
friendly reading of IrpProtocols.xml in the browser.

## API documentation
Up-to-date API documentation, generated by Javadoc, is found [here](https://bengtmartensson.github.io/IrpTransmogrifier/).

## Integration in Maven projects
This project can be integrated into other projects using Maven. For this, include the lines
```
        <dependency>
            <groupId>org.harctoolbox</groupId>
            <artifactId>IrpTransmogrifier</artifactId>
            <version>1.2.3</version>  <!-- or another supported version -->
        </dependency>
```
in the `pom.xml` of the importing project.

## Installation
Unpack the binary distribution in a new, empty directory. Start the program by invoking the wrapper
(`irptransmogrifier.bat` on Windows, `irptransmogrifier.sh` on Unix-like systems like Linux and MacOS.)
from the command line.
Modify and/or relocate the wrapper(s) if desired or necessary.
Do not double click the wrappers, since this program runs only from the command line.

Also, do not use the wrapper `irptransmogrifier` in the top top directory of the source tree.
This is intended for development only, not by users.

## Building from sources
The project uses [Maven](https://maven.apache.org/) as build system. Any modern IDE should be able
to open/import and build it (as Maven project). Of course, Maven can also be run from the command line,
like

    mvn install

## Dependencies
The program depends on [ANTLR4](https://www.antlr.org) ([license](https://github.com/antlr/antlr4/blob/master/LICENSE.txt)),
[Stringtemplate](https://www.stringtemplate.org/), ([license](https://github.com/antlr/stringtemplate4/blob/master/LICENSE.txt)),
as well as the command line decoder [JCommander](http://jcommander.org/). (licensed under the [Apache 2 license](https://github.com/cbeust/jcommander/blob/master/license.txt)).
When using Maven for building, these are automatically downloaded and installed.

## Usage
Using from the command line, this is a command with sub commands. Before the sub command,
common options can be given. After the command, command-specific options can be specified.
Commands and option names can be abbreviated, as long as the abbreviation is unique.
They are matched case sensitively, and can be abbreviated as long as the abbreviation is unambiguous.

The command

    irptransmogrifier help --short
lists the subcommands.
A command like

    irptransmogrifier analyze --help
gives the usage for the subcommand `analyze`,
while a command like

    irptransmogrifier analyze --help
gives a possibly somewhat longer description for the subcommand `analyze`.


The subcommands are briefly described next.

### Subcommand analyze
The "analyze" command takes as input one or several sequences or signals,
and computes an IRP form that corresponds to the given input (within
the specified tolerances). The input can be given either as Pronto
Hex or in raw form, optionally with signs (ignored). Several raw
format input sequences can be given by enclosing the individual sequences
in brackets ("[]"). However, if using the `--intro-repeat-ending` option,
the sequences are instead interpreted as intro-, repeat-, and (optionally)
ending sequences of an IR signal.

For raw sequences, an explicit modulation frequency can be given with
the `--frequency` option. Otherwise the default frequency, 38000Hz,
will be assumed.
If this option is given together with a Pronto type signal (which contains
a modulation frequency), it is ignored.

Using the option `--input`, instead the content of a file can be taken
as input, containing sequences to be analyzed, one per line, blank
lines ignored. Using the option `--namedinput`, the sequences may have
names, immediately preceeding the sequence.
In both cases, the data is taken as IrSequences.
IrSignals, with intro-, repeat-, and ending, are coerced into IrSequences.

In the Harctoolbox world, IR sequences start with a flash (mark) and ends with a
non-zero gap (space). In some other "worlds", the last gap is omitted. These signal
are in general rejected. The option `--trailinggap <duration>` adds a dummy duration
to the end of each IR sequence lacking a final gap.

Input sequences can be pre-processed using the options `--chop`, `--clean`,
and `--repeatfinder`.

The cleaner works according to this idea: The collected durations found in the sequence(s)
are bundled into "bins" (disjoint intervals), according to `absolutetolerance` and `relativetolerance`.
Every duration belonging to a bin is "close" (determined by those parameters) to the bin
middle. All the durations within the bin are then replaced by the average of its
members. It is thus not guaranteed that the distance between a duration and its relacement
will be consistent with `absolutetolerance` and `relativetolerance`.

The input sequence(s) are matched using different "decoders". Normally
the "best" decoder match is output. With the `--all` option, all decoder
matches are output.

The options `--statistics` and `--dump-repeatfinder` (the latter forces
the repeatfinder to be on) can be used to print extra information.
The common options `--absolutetolerance`, `--relativetolerance`, `--minrepeatgap`
determine how the repeat finder breaks the input data. The options
`--extent`, `--invert`, `--lsb`, `--maxmicroseconds`, `--maxparameterwidth`,
`--maxroundingerror`, `--maxunits`, `--parameterwidths`, `--radix`, and `--timebase`
determine how the computed IRP is displayed.

## Subcommand bitfield
The "bitfield" command computes the value and the binary form corresponding
to the bitfield given as input. Using the `--nameengine` argument,
the bitfield can also refer to names.

As an alternatively, the "expression" command may be used.

## Subcommand code
Used for generating code for different targets.

## Subcommand decode
The "decode" command takes as input one or several sequences or signals,
and output one or many protocol/parameter combinations that corresponds
to the given input (within the specified tolerances). The input can
be given either as Pronto Hex or in raw form, optionally with signs
(ignored). Several raw format input sequences can be given by enclosing
the individual sequences in brackets ("[]").

For raw sequences, an explicit modulation frequency can be given with
the `--frequency` option. Otherwise the default frequency, 38000Hz,
will be assumed.

Using the option `--input`, instead the content of a file can be taken
as input, containing sequences to be analyzed, one per line, blank
lines ignored. Using the option `--namedinput`, the sequences may have
names, immediately preceeding the signal.

In the Harctoolbox world, IR sequences start with a flash (mark) and ends with a
non-zero gap (space). In some other "worlds", the last gap is omitted. These signal
are in general rejected. The option `--trailinggap <duration>` adds a dummy duration
to the end of each IR sequence lacking a final gap.

Input sequences can be pre-processed using the options `--clean`, and
`--repeatfinder`.

The common options `--absolutetolerance`, `--relativetolerance`, `--minrepeatgap`
determine how the repeat finder breaks the input data.

## Subcommand help

This command list the syntax for the command(s) given as argument,
default all. Also see the option `--describe`.

## Subcommand lirc
This command reads a Lirc configuration, from a file, directory, or
an URL, and computes a correponding IRP form.

## Subcommand list
This command list miscellaneous properties of the protocol(s) given
as arguments.

## Subcommand render
This command is used to compute an IR signal from one or more protocols
("render" it). The protocol can be given either by name(s) (or regular
expression if using the `--regexp` option), or, using the `--irp` options,
given explicitly as an IRP form. The parameters can be either given
directly with the -n option,or the `--random` option can be used to
generate random, but valid parameters. With the `--count` or `--number-repeats`
option, instead an IR sequence is computed,containing the desired
number of repeats.

## Subcommand version
Reports version number and license.

## Usage message
```
Usage: IrpTransmogrifier [options] [command] [command options]
  Options:
    -a, --absolutetolerance
      Absolute tolerance in microseconds, used when comparing durations.
      Default: 100.0.
    -b, --blacklist
      List of protocols to be removed from the data base
    -C, --commentStart
      Character(s) to be considered starting a line comment in input and
      namedInput files.
    -c, --configfiles
      Pathname(s) of IRP database file(s) in XML format. Default is the one in
      the jar file. Can be given several times.
    --describe
      Print a possibly longer documentation for the present command.
    -f, --frequencytolerance
      Frequency tolerance in Hz. Negative disables frequency check. Default:
      2000.0.
    -h, -?, --help
      Print help for this command.
    -e, --encoding, --iencoding
      Encoding used to read input.
      Default: UTF-8
    -i, --irp
      Explicit IRP string to use as protocol definition.
    --logclasses
      List of (fully qualified) classes and their log levels, in the form
      class1:level1|class2:level2|...
      Default: <empty string>
    -L, --logfile
      Log file. If empty, log to stderr.
    -F, --logformat
      Log format, as in class java.util.logging.SimpleFormatter.
      Default: [%2$s] %4$s: %5$s%n
    -l, --loglevel
      Log level { OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
      }
      Default: WARNING
    --min-leadout
      Threshold for leadout when decoding. Default: 20000.0.
    -g, --minrepeatgap
      Minimum gap required to end a repetition.
      Default: 5000.0
    --oencoding
      Encoding used in generated output.
      Default: UTF-8
    -o, --output
      Name of output file. Default: stdout.
    -O, --override
      Let given command line parameters override the protocol parameters in
      IrpProtoocols.xml
      Default: false
    -q, --quiet
      Quitest possible operation, typically to be used from scripts.
      Default: false
    --regexp
      Interpret protocol/decoder argument as regular expressions.
      Default: false
    -r, --relativetolerance
      Relative tolerance as a number < 1. Default: 0.3.
    --seed
      Set seed for the pseudo random number generation. If not specified, will
      be random, different between program invocations.
    -s, --sort
      Sort the protocols alphabetically on output.
      Default: false
    -t, --tsv, --csv
      Use tabs in output to optimize for the import in spreadsheet programs as
      cvs.
      Default: false
    -u, --url-decode
      URL-decode protocol names, (understanding %20 for example).
      Default: false
    --validate
      Validate IRP database files against the schema, abort if not valid.
      Default: false
    --version
      Report version. Deprecated; use the command "version" instead.
      Default: false
    -x, --xmllog
      Write the log in XML format.
      Default: false
  Commands:
    help      Describe the syntax of program and commands.
      Usage: help [options] commands
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.
          -l, --logging
            Describe the logging related options only.
            Default: false
          -c, --common, --options
            Describe the common options only.
            Default: false
          -s, --short
            Produce a short usage message.
            Default: false

    version      Report version and license.
      Usage: version [options]
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.
          -s, --short
            Issue only the version number of the program proper.
            Default: false

    list      List protocols and their properites.
      Usage: list [options] List of protocols (default all)
        Options:
          -a, --all
            Implies (almost) all of the "list xxx"-options.
            Default: false
          --check-sorted
            Check if the protocol are alphabetically.
            Default: false
          -c, --classify
            Classify the protocol(s).
            Default: false
          --describe
            Print a possibly longer documentation for the present command.
          --gui, --display
            Display parse diagram.
            Default: false
          --documentation
            Print (possible longer) documentation, as a dumb rendering of the
            HTML documenation.
            Default: false
          -d, --dump
            Print the IRP data base as DOC tree stringified, including initial
            XML comments.
            Default: false
          -h, -?, --help
            Print help for this command.
          --html
            Print (possible longer) documentation as HTML.
            Default: false
          -i, --irp
            List IRP form, as given in the database (unparsed, i.e. preserving
            comments and whitespace, not taking --radix into account).
            Default: false
          -m, --mindiff
            Compute minimal difference between contained durations.
            Default: false
          --name
            List protocol name, also if --quiet is given.
            Default: false
          -n, --normalform
            List the normal form.
            Default: false
          --prefer-overs
            List all protocol's prefer-overs, recursively.
            Default: false
          -r, --radix
            Radix of parameter output.
            Default: 10
          --stringtree
            Produce stringtree.
            Default: false
          --warnings
            Issue warnings for some problematic IRP constructs.
            Default: false
          -w, --weight
            Compute weight of the protocols.
            Default: false
          -x, --xml
            Like dump, but without XML comments.
            Default: false

    render      Render signal from parameters
      Usage: render [options] protocol(s) or pattern (default all)
        Options:
          -#, --count
            Generate am IR sequence with count number of transmissions
          -d, --decode
            Send the rendered signal to the decoder (for
            debugging/development).
            Default: false
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.
          -m, --modulate
            Generate modulated form (EXPERIMENTAL)
            Default: false
          -n, --nameengine, --parameters
            Name Engine to use
            Default: <empty string>
          --number-repeats
            Generate an IR sequence containing the given number of repeats
          -P, --printparameters
            Print actual parameters values, for example by --random
            Default: false
          -p, --pronto, --ccf, --hex
            Generate Pronto hex.
            Default: false
          --random
            Generate random, valid, parameters
            Default: false
          -R, --raw-without-signs
            Generate raw form without signs.
            Default: false
          -r, --signed-raw
            Generate raw form.
            Default: false

    decode      Decode IR signal given as argument
      Usage: decode [options] durations in micro seconds, alternatively pronto
            hex
        Options:
          -c, --clean
            Invoke cleaner on signal
            Default: false
          --describe
            Print a possibly longer documentation for the present command.
          -R, --dump-repeatfinder
            Print the result of the repeatfinder.
            Default: false
          -f, --frequency
            Set modulation frequency.
          -g, --girr
            Generate output in Girr format (only)
            Default: false
          -h, -?, --help
            Print help for this command.
          -l, --ignoreleadinggarbage
            Accept decodes starting with undecodable pairs.
            Default: false
          -i, --input
            File/URL from which to take inputs, one per line.
          -k, --keep-defaulted
            In output, do not remove parameters that are equal to their
            defaults.
            Default: false
          -n, --namedinput
            File/URL from which to take inputs, one line name, data one line.
          -a, --all, --no-prefer-over
            Output all decodes; ignore prefer-over.
            Default: false
          -p, --protocol
            Comma separated list of protocols to try match (default all).
          --radix
            Radix used for printing of output parameters.
            Default: 10
          --recursive
            Apply decoder recursively, (for long signals).
            Default: false
          -r, --repeatfinder
            Invoke repeat finder on input sequence
            Default: false
          -s, --strict
            Require intro- and repeat sequences to match exactly.
            Default: false
          -T, --trailinggap
            Trailing gap (in micro seconds) added to sequences of odd length.
          --xslt
            File/URL name of XSLT transformation that will be applied to
            --input or --namedinput argument

    demodulate      Demodulate IrSequence given as argument (EXPERIMENTAL).
      Usage: demodulate [options] durations in micro seconds, alternatively
            pronto hex
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.
          -t, --threshold
            Threshold used for demodulating, in micro seconds.
            Default: 35.0

    analyze      Analyze signal: tries to find an IRP form with parameters.
      Usage: analyze [options] durations in microseconds, or pronto hex.
        Options:
          -a, --all
            List all decoder outcomes, instead of only the one with lowest
            weight.
            Default: false
          -b, --bit-usage
            Create bit usage report. (Not with --all)
            Default: false
          -c, --chop
            Chop input sequence into several using threshold (in milliseconds)
            given as argument.
          -C, --clean
            Output the cleaned sequence(s).
            Default: false
          -d, --decoder
            Use only the decoders matching argument (regular expression, or
            prefix). Use the argument "list" to list the available decoders.
          --describe
            Print a possibly longer documentation for the present command.
          -R, --dump-repeatfinder
            Print the result of the repeatfinder.
            Default: false
          --eliminate-vars
            Eliminate variables in output form
            Default: false
          -e, --extent
            Output the last gap as an extent.
            Default: false
          --fatgirr
            Generate Girr file in fat format (EXPERIMENTAL).
            Default: false
          -f, --frequency
            Modulation frequency of raw signal.
          -g, --girr
            Generate Girr file (EXPERIMENTAL).
            Default: false
          -h, -?, --help
            Print help for this command.
          -i, --input
            File/URL from which to take inputs, one sequence per line.
          --ire, --intro-repeat-ending
            Consider the argument as begin, repeat, and ending sequence.
            Default: false
          -I, --invert
            Invert the order in bitspec.
            Default: false
          -l, --lsb
            Force lsb-first bitorder for the parameters.
            Default: false
          -u, --maxmicroseconds
            Maximal duration to be expressed as micro seconds.
            Default: 10000.0
          -M, --maxparameterwidth
            Maximal parameter width.
            Default: 63
          --maxroundingerror
            Maximal rounding errors for expressing as multiple of time unit.
            Default: 0.3
          -m, --maxunits
            Maximal multiplier of time unit in durations.
            Default: 30.0
          -n, --namedinput
            File/URL from which to take inputs, one line name, data one line.
          -p, --parametertable
            Create parameter table.
            Default: false
          -w, --parameterwidths
            Comma separated list of either parameter widths or name:width
            pairs.
            Default: []
          --radix
            Radix used for printing of output parameters.
            Default: 16
          -r, --repeatfinder
            Invoke the repeatfinder.
            Default: false
          -s, --statistics
            Print some statistics.
            Default: false
          -t, --timebase
            Force time unit , in microseconds (no suffix), or in periods (with
            suffix "p").
          --timings
            Print the total timings of the compute IRP form.
            Default: false
          -T, --trailinggap
            Dummy trailing gap (in micro seconds) added to sequences of odd
            length.
          --validate
            Validate that the resulted protocol can be used for rendering and
            produces the same signal.
            Default: false
          --xslt
            File/URL name of XSLT transformation that will be applied to
            --input or --namedinput argument

    code      Generate code for the given target(s)
      Usage: code [options] protocols
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          -d, --directory
            Directory in whicht the generate output files will be written, if
            not using the --output option.
          -h, -?, --help
            Print help for this command.
          --inspect
            Fire up stringtemplate inspector on generated code (if sensible)
            Default: false
          -p, --parameter
            Specify target dependent parameters to the code generators.
            Default: []
          -s, --stdirectory
            Directory containing st (string template) files for code
            generation.
            Default: st
        * -t, --target
            Target(s) for code generation. Use ? for a list.
            Default: []

    bitfield      Evaluate bitfield given as argument.
      Usage: bitfield [options] bitfield
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.
          -l, --lsb
            Output bitstream with least significant bit first.
            Default: false
          -n, --nameengine, --parameters
            Define a name engine for resolving the bitfield.
            Default: <empty string>
          --xml
            Generate XML and write to file given as argument.

    expression      Evaluate expression given as argument.
      Usage: expression [options] expression
        Options:
          --describe
            Print a possibly longer documentation for the present command.
          --gui, --display
            Display parse diagram.
            Default: false
          -h, -?, --help
            Print help for this command.
          -n, --nameengine, --parameters
            Define a name engine to use for evaluating.
            Default: <empty string>
          -r, --radix
            Radix for outputting result.
            Default: 10
          --stringtree
            Output stringtree.
            Default: false
          --xml
            Generate XML and write to file argument.

    lirc      Convert Lirc configuration files to IRP form.
      Usage: lirc [options] Lirc config files/directories/URLs; empty for
            <stdin>.
        Options:
          -c, --commands
            Also list the commands if the remotes.
            Default: false
          --describe
            Print a possibly longer documentation for the present command.
          -h, -?, --help
            Print help for this command.



For documentation, see http://www.harctoolbox.org/IrpTransmogrifier.html
```
