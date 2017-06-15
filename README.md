# IrpTransmogrifier [![Build Status](https://travis-ci.org/bengtmartensson/IrpTransmogrifier.svg?branch=master)](https://travis-ci.org/bengtmartensson/IrpTransmogrifier)

Parser for IRP notation protocols, with rendering, code generation, recognition applications.

This is a new program, written from scratch, that is intended to replace IrpMaster, DecodeIR, and (most of) ExchangeIR,
and much more like potentially replacing "all" hand written decoders/renderers.
It consists of an API library that is also callable from the command line as a command line program.
A GUI is possible, as well as the integration in GUI programs.

## Acknowledgement
I would like to acknowledge the influence of the [JP1 forum](http://hifi-remote.com/forums/index.php), both the programs
(in particular of course [DecodeIR](http://www.hifi-remote.com/wiki/index.php?title=DecodeIR), and the discussions
(in particular with Dave Reed ("3FG")). This work surely would not exist without the JP1 forum.

## Background
The Irp parser [IrpMaster](http://www.harctoolbox.org/IrpMaster,html)
is based upon a [ANTLR3 grammar](http://www.antlr.org). The "new version"
ANTLR4 is really not a new version, but a completely different tool, fixing most of the quirks that were irritating
in IrpMaster (like the "ugliness" of embedding actions within the grammar, no left recursion, and much more).
Unfortunately, this means that using version 4 instead of version 3 is not like updating a compiler or such,
but "necessitates" a complete rewrite of the actions.

But that is of course not all: Both DecodeIR by John Fine and the Analyzer by Graham Dixon have shown to be
essentially impossible to maintain and extend. Although DecodeIR and IrpMaster (through the data base IrpProtocols.ini)
agree on most protocols, this consists of two different, dis-coupled sources of protocol information. Finally, to be able to 
generate code from the IRP form is a natural wish.


## Use cases
The reason for this project is not (just) to migrate IrpMaster to ANTLR4 -- version 3 is still operational,
and no more broken than it was at the start. There are a number of interesting use cases for a nice parser/tree traverser:

### Rendering
This use case corresponds to IrpMaster. It is probably to be implemented as a traversing of the parse tree of an IRP protocol,
with numerical values assigned to the parameters.

The present implementation allows only one infinite repeat allowed. (A realistic protocol, or use case, requiring more than
one infinite repeat is not known to me.) Also, individual bitfields are restricted to 63 bits of length or less.
(This is inherited from the use of Java's long type. It [may be removed in the future](https://github.com/bengtmartensson/IrpTransmogrifier/issues/38).)
With the exception of these restriction, the implementations should be complete, down to specification holes.

### Recognition
This use case corresponds to a dynamic version of DecodeIR: given a numerical IR signal,
find the parameter/protocol combinations that could have generated the given signal.
This is implemented by trying to parse the given signal with respect to the candidate protocols.
It is thus very systematic, but [comparatively slow](https://github.com/bengtmartensson/IrpTransmogrifier/issues/44).

It is not claimed that all protocols in the protocol data base are recognizable.
Non-recognizable protocols are to be marked by setting the `decodable` parameter to `false`.
To be recognizable, the IRP protocol should preferably adhere to some additional rules:

* The "+" form of repetitions is discouraged in favor of the "*" form. 
* The width and shift of a Bitfield must be constant
* The decoder is capable of _simple_ equation solving (e.g. `Arctech`), but not of complicated equation solving (e.g. `Fujitsu_Aircon`).

Presently all but two protocols (`zenith`, `nec1-shirrif`, (bitfield width as parameter), `fujitsu_aircon` (would require non-trivial equation solving))
are recognizable.  It is not guaranteed that new protocols automatically will be recognizable.

#### Loose matches, Guessing
Many captured signals are not quite correct according to their protocol. However, the firmware in a receiving device is often "forgiving",
and accepts slightly flawed signals. It is thus desirable for a program of this type to find a near match, "guess", when an real match fails.
The program currently does not implement this, however, it is [planned](https://github.com/bengtmartensson/IrpTransmogrifier/issues/42).

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
The "ini"-file `IrpProtocols.ini` of IrpMaster (and thus IrScrutinizer) has been replaced by an XML file,
per default called `IrpProtocols.xml`. The XML format
is defined by the schema [irp-protocols](http://www.harctoolbox.org/schemas/irp-protocols.xsd), and has the name space 
`http://www.harctoolbox.org/irp-protocols`. This format has many advantages in 
comparison with the simpler previous format, for example, it can contain embedded XHTLM fragments.
It also can contain different parameters that can be used by different programs, for example, tolerance parameters
for decoding.
Arbitrary string-valued parameters are permitted. It is up to an interpreting program to determine the semantic.

There is also an XSLT stylesheet, which technically translates the XML to HTML, allowing for a user
friendly reading of IrpProtocols.xml in the browser.

The program is capable of reading and translating to and from the old format, sub-command `convertconfig`.

## Installation
Unpack the binary distribution in a new, empty directory. Start the program by invoking the wrapper
(`irptransmobrifier.bat` on Windows, `irptransmogrifier.sh` on Unix-like systems like Linux and MacOS.)
from the command line. Do not double click the wrappers, since this program runs only from the command line.

## Usage
Using from the command line, this is a command with subcommands. Before the sub command,
common options can be given. After the command, command-specific options can be specified.
Commands and option names can be abbreviated, as long as the abbreviation is unique.
They are mached case sensitively.

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

Using the option `--input`, instead the content of a file can be taken
as input, containing sequences to be analyzed, one per line, blank
lines ignored. Using the option `--namedinput`, the sequences may have
names, immediately preceeding the signal. 

Input sequences can be pre-processed using the options `--chop`, `--clean`,
and `--repeatfinder`. 

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

## Subcommand convertconfig
This command converts between the xml form and the ini form on IrpProtocols.
decode --desc

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
    Usage: IrpTransmogrifier [options] [command] [command options]
      Options:
	-a, --absolutetolerance
	  Absolute tolerance in microseconds, used when comparing durations.
	-c, --configfile
	  Pathname of IRP database file in XML format. Default is the one in the 
	  jar file.
	-e, --encoding
	  Encoding used in generated output.
	  Default: UTF-8
	-f, --frequencytolerance
	  Frequency tolerance in Hz. Negative disables frequency check.
	-h, --help, -?
	  Display help message (deprecated; use the command "help" instead).
	-i, --ini, --inifile
	  Pathname of IRP database file in ini format.
	--logclasses
	  List of (fully qualified) classes and their log levels.
	  Default: <empty string>
	-L, --logfile
	  Log file. If empty, log to stderr.
	-F, --logformat
	  Log format, as in class java.util.logging.SimpleFormatter.
	  Default: %4$s(%2$s): %5$s%n
	-l, --loglevel
	  Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING 
	  } 
	  Default: WARNING
	--min-leadout
	  Threshold for leadout when decoding.
	-g, --minrepeatgap
	  Minumum gap at end of repetition
	  Default: 5000.0
	-o, --output
	  Name of output file (default: stdout).
	--regexp
	  Interpret protocol/decoder argument as regular expressions.
	  Default: false
	-r, --relativetolerance
	  Relative tolerance as a number < 1
	--seed
	  Set seed for pseudo random number generation (default: random).
	-s, --sort
	  Sort the protocols alphabetically on output.
	  Default: false
	-u, --url-decode
	  URL-decode protocol names, (understanding %20 for example).
	  Default: false
	-v, --version
	  Report version (deprecated; use command version instead).
	  Default: false
	-x, --xmllog
	  Log in XML format.
	  Default: false
      Commands:
	help      Describe the syntax of program and commands.
	  Usage: help [options] commands
	    Options:
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.
	      -s, --short
		Produce a short usage message.
		Default: false

	version      Report version
	  Usage: version [options]
	    Options:
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.
	      -s, --short
		Issue only the version number of the program proper
		Default: false

	list      List protocols and their properites
	  Usage: list [options] List of protocols (default all)
	    Options:
	      -c, --classify
		Classify the protocol(s).
		Default: false
	      --cname
		List C name of the protocol(s).
		Default: false
	      --description
		Print a possibly longer documentation for the present command.
	      --gui, --display
		Display parse diagram.
		Default: false
	      --documentation
		Print (possible longer) documentation.
		Default: false
	      -h, -?, --help
		Print help for this command.
	      -i, --irp
		List IRP form.
		Default: false
	      -n, --normal, --normalform
		List the normal form.
		Default: false
	      -r, --radix
		Radix of parameter output.
		Default: 16
	      --stringtree
		Produce stringtree.
		Default: false
	      --warnings
		Issue warnings for some problematic IRP constructs.
		Default: false
	      -w, --weight
		Compute weight of the protocols.
		Default: false

	render      Render signal from parameters
	  Usage: render [options] protocol(s) or pattern (default all)
	    Options:
	      -#, --count
		Generate am IR sequence with count number of transmissions
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.
	      -i, --irp
		Explicit IRP string to use as protocol definition.
	      -n, --nameengine
		Name Engine to use
		Default: {}
	      --number-repeats
		Generate an IR sequence containing the given number of repeats
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
	      --description
		Print a possibly longer documentation for the present command.
	      -R, --dump-repeatfinder
		Print the result of the repeatfinder.
		Default: false
	      -f, --frequency
		Set modulation frequency.
	      -h, -?, --help
		Print help for this command.
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
	      -r, --repeat-finder
		Invoke repeat finder on input sequence
		Default: false
	      -s, --strict
		Require intro- and repeat sequences to match exactly.
		Default: false

	analyze      Analyze signal: tries to find an IRP form with parameters
	  Usage: analyze [options] durations in microseconds, or pronto hex.
	    Options:
	      -a, --all
		List all decoder outcomes, instead of only the one with lowest 
		weight. 
		Default: false
	      -c, --chop
		Chop input sequence into several using threshold given as 
		argument. 
	      -C, --clean
		Output the cleaned sequence(s).
		Default: false
	      --description
		Print a possibly longer documentation for the present command.
	      -R, --dump-repeatfinder
		Print the result of the repeatfinder.
		Default: false
	      -e, --extent
		Output the last gap as an extent.
		Default: false
	      -f, --frequency
		Modulation frequency of raw signal.
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
		Maximal parameter width (must be < 64).
		Default: 16
	      --maxroundingerror
		Maximal rounding errors for expressing as multiple of time unit.
		Default: 0.3
	      -m, --maxunits
		Maximal multiplier of time unit in durations.
		Default: 30.0
	      -n, --namedinput
		File/URL from which to take inputs, one line name, data one line.
	      -w, --parameterwidths
		Comma separated list of parameter widths.
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

	code      Generate code for the given target(s)
	  Usage: code [options] protocols
	    Options:
	      --description
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
		Default: /local/share/irptransmogrifier/st
	    * -t, --target
		Target(s) for code generation. Use ? for a list.
		Default: []

	bitfield      Evaluate bitfield given as argument.
	  Usage: bitfield [options] bitfield
	    Options:
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.
	      -l, --lsb
		Output bitstream with least significant bit first.
		Default: false
	      -n, --nameengine
		Define a name engine for resolving the bitfield.
		Default: {}
	      --xml
		Generate XML and write to file given as argument.

	expression      Evaluate expression given as argument.
	  Usage: expression [options] expression
	    Options:
	      --description
		Print a possibly longer documentation for the present command.
	      --gui, --display
		Display parse diagram.
		Default: false
	      -h, -?, --help
		Print help for this command.
	      -n, --nameengine
		Define a name engine to use for evaluating.
		Default: {}
	      --stringtree
		Output stringtree.
		Default: false
	      --xml
		Generate XML and write to file argument.

	lirc      Convert Lirc configuration files to IRP form.
	  Usage: lirc [options] Lirc config files/directories/URLs); empty for 
		<stdin>. 
	    Options:
	      -c, --commands
		Also list the commands if the remotes.
		Default: false
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.

	convertconfig      Convert an IrpProtocols.ini-file to an 
		IrpProtocols.xml, or vice versa.
	  Usage: convertconfig [options]
	    Options:
	      -c, --check
		Check that the protocols in the input file are alphabetically 
		ordered. 
		Default: false
	      --description
		Print a possibly longer documentation for the present command.
	      -h, -?, --help
		Print help for this command.
