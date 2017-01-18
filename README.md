# IrpTransmogrifier
Parser for IRP notation protocols, with rendering, code generation, recognition applications.

This is a new program, written from scratch, that is intended to replace IrpMaster, DecodeIR, and (most of) ExchangeIR,
and much more like potentially replacing "all" hand written decoders/renderers.
It consists of an API library that is also callable from the command line as a command line program.
A GUI is possible, but should not cripple the command line interface.

## Background
The Irp parser [IrpMaster](http://www.harctoolbox.org/IrpMaster,html)
is based upon a [ANTLR3 grammar](http://www.antlr.org). The "new version"
ANTLR4 is really not a new version, but a completely different tool, fixing most of the quirks that irritated me
in IrpMaster (like the "ugliness" of embedding actions within the grammar, no left recursion, and much more).
Unfortunately, this means that using version 4 instead of version 3 is not like updating a compiler or such,
but "necessitates" a complete rewrite of the actions.

But that is of course not all: Both DecodeIR by John Fine and the Analyzer by Graham Dixon have shown to be (at least IMHO)
essentially impossible to maintain and extend. Although DecodeIR and IrpMaster (through the data base IrpProtocols.ini)
agree on most protocols, this consists of two different, discoupled sources of protocol information. Finally, to be able to 
generate code from the IRP form is a natural wish.


## Use cases
The reason for this project is not (just) to migrate IrpMaster to ANTLR4 -- version 3 is still operational,
and no more broken than it was at the start. There are a number of interesting use cases for a nice parser/tree traverser:

### Rendering
This use case corresponds to IrpMaster. It is probably to be implemented as a traversing of the parse tree of an IRP protocol,
with numerical values assigned to the parameters.

_This has essentially been completed. Ambition level: only one infinite repeat allowed. With that restriction, should be
complete solution, down to specification holes. Remains: test framework._

### Recognition
This use case corresponds to a dynamic version of DecodeIR: given a numerical IR signal,
 find one (or all) parameter/protocol combinations that could have generated the given signal.
 Probably trying a number of different pre-rendered IRP parse trees, possibly a two-stage strategy,
 using IRP ["Meta protocols"](http://www.harctoolbox.org/IrpMaster.html#Preprocessing+and+inheritance).
See [this issue](https://github.com/bengtmartensson/IrpTransmogrifier/issues/7).

_Ambition level: should grok almost all of the existing protocols in IrpProtocols, but not necessarily be "complete".
Status: Three protocols declared un-doable (zenith (bitfield width as parameter), entone, fujitsu_aircon (would require non-trivial equation solving)).
All other protocols recognizable. 
Remains: Speeding up by using different techniques, parameter tuning, testing, test framework_

### Code generation for rendering and/or decoding
For a particular protocol, generate target code (C, C++, Java, Python,...) that can render or decode signals
   with the selected protocol. As opposed to the previous use cases, efficency (memory, execution time) (for the generated code) is potentially
   an issue. This should be able to generate protocol renders for e.g. the Arduino libraries IrRemote, IrLib, and AGirs.
   At least in the first version, not all protocols describable by IRPs need to be supported. Not implemented in the first phase: Protocols with hierarchical bitspecs 
(rc6*, replay, arctech, entone), protocols with bitspec lenght as parameter (zenith, nec1-shirrif). Also default are not implemented, e.g. NEC1 has to be
called with 3 parameters.

Two mechanisms are available: XSLT and [Stringtemplate](http://www.stringtemplate.org/).
The program invokes either one (or many) XSLT2 transformations  on an XML representation of the protocol,
or invokes Stringtemplate. In both cases, the program just invokes the stylesheet or template, without caring what it does; if it generates a renderer or decoder.
The user is instead governs this by invoking the style sheets or templates (s)he want using the --target option to the `code` sub subcommand.
(For this reason, there is no `--renderer` or `--decoder` option to the `code` sub command.)

Targets:
* [Lircd.conf](http://lirc.org/html/lircd.conf.html) generation from IrScrutinizer. This is based on an XSLT-transformation (lirc.xsd) and generates
 [an XSLT (version 1) file that can work with IrScrutinizer](https://github.com/bengtmartensson/harctoolboxbundle/blob/master/IrScrutinizer/src/main/config/exportformats.d/lirc.xml)
Handling of definitions as well as expressions as bitfields not implemented, as well as a few other things (search for "omitted" in the above file),
otherwise works. "90% complete", see [this issue](https://github.com/bengtmartensson/IrpTransmogrifier/issues/6).
* Java. Essentially for testing. This is essentially working for rendering, including a test rig (see the [test project](https://github.com/bengtmartensson/JavaIrpProtocolTest).
* C++ ([Infrared4Arduino](https://github.com/bengtmartensson/Infrared4Arduino)).
* [IRremote](https://github.com/z3t0/Arduino-IRremote)
* Linux kernel modules in [linux/drivers/media/rc](https://github.com/torvalds/linux/tree/master/drivers/media/rc) (decoding only).

_Ambition level: Should be able to generate the "important" protocols, not necessarily "complete"._

### General code analysis
Not really connected to parsing IRP, but fits in the general framework.
This corresponds to the Analyzer and the RepeatFinder in
[Graham Dixon's ExchangeIR](http://www.hifi-remote.com/forums/dload.php?action=file&file_id=8460)
([Java translation](https://sourceforge.net/p/controlremote/code/HEAD/tree/trunk/exchangeir/)).

_Status: (`org.harctoolbox.analyze.`)`Repeatfinder` and `Cleaner` completed (essentially adapted from recent
IrpMaster). Decoders: BiphaseDecoder, Pwm4Decoder, Pwm4Decoder, TrivialDecoder written, remains Pwm16 (XMP). Remains: testing and tuning._ 

## Protocol Data Base
The "ini"-file `IrpProtocols.ini` of IrpMaster (and thus IrScrutinizer) has been replaced by an XML file,
per default called `IrpProtocols.xml`. The XML format
is defined by the schema [irp-protocols](http://www.harctoolbox.org/schemas/irp-protocols.xsd), and has the name space 
`http://www.harctoolbox.org/irp-protocols`. This format has many advantages in 
comparison with the previous, more primitive, format, for example, it can contain embedded XHTLM fragments.
It also can contain different parameters that can be used by different programs, for example, tolerance parameters
for decoding.

The program is capable of reading and translating the old format.

## Usage
Using from the command line, this is a command with subcommands

    Usage: IrpTransmogrifier [options] [command] [command options]
      Options:
	-a, --absolutetolerance
	   Absolute tolerance in microseconds
	-c, --configfile
	   Pathname of IRP database file in XML format
	-e, --encoding
	   Encoding used for generating output
	   Default: UTF-8
	-f, --frequencytolerance
	   Frequency tolerance in Hz. Negative disables frequency check
	-h, --help, -?
	   Display help message (deprecated; use command help instead)
	   Default: false
	-i, --ini, --inifile
	   Pathname of IRP database file in ini format
	--logclasses
	   List of (fully qualified) classes and their log levels.
	   Default: <empty string>
	-L, --logfile
	   Log file. If empty, log to stderr.
	-F, --logformat
	   Log format, see class SimpleFormatter.
	   Default: %4$s(%2$s): %5$s%n
	-l, --loglevel
	   Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING
	   }
	   Default: INFO
	-o, --output
	   Name of output file (default stdout)
	--regexp
	   Interpret protocol/decoder argument as regular expressions
	   Default: false
	-r, --relativetolerance
	   Relative tolerance as a number < 1 (NOT: percent)
	--seed
	   Set seed for pseudo random number generation (default: random)
	-s, --sort
	   Sort the protocols alphabetically
	   Default: false
	-v, --version
	   Report version (deprecated; use command version instead)
	   Default: false
	-x, --xmllog
	   Log in XML format.
	   Default: false
      Commands:
	analyze      Analyze signal
	  Usage: analyze [options] durations in microseconds, or pronto hex
	    Options:
	      --decoder
		 Use only the decoders matching argument (regular expression).
		 Mainly for debugging.
	      -e, --extent
		 Output last gap as extent
		 Default: false
	      -f, --frequency
		 Modulation frequency
	      -i, --invert
		 Invert order in bitspec
		 Default: false
	      -l, --lsb
		 Force lsb-first bitorder for the analyzer
		 Default: false
	      -u, --maxmicroseconds
		 Maximal duration to be expressed as micro seconds
		 Default: 10000.0
	      --maxroundingerror
		 Maximal rounding errors for expressing as multiple of time unit
		 Default: 0.3
	      -m, --maxunits
		 Maximal multiplier of time unit in durations
		 Default: 30.0
	      -w, --parameterwidths
		 Comma separated list of parameter widths
		 Default: []
	      --radix
		 Radix of parameter output
		 Default: 16
	      -r, --repeatfinder
		 Invoke the repeatfinder
		 Default: false
	      -s, --statistics
		 Print some statistics
		 Default: false
	      -t, --timebase
		 Force timebase, in microseconds, or in periods (with ending "p")

	bitfield      Evaluate bitfield
	  Usage: bitfield [options] bitfield
	    Options:
	      --gui, --display
		 Display parse diagram
		 Default: false
	      -l, --lsb
		 Least significant bit first
		 Default: false
	      -n, --nameengine
		 Name Engine to use
		 Default: {}
	      --xml
		 Generate XML and write to file argument

	code      Generate code
	  Usage: code [options] protocol
	    Options:
	      -d, --directory
		 Directory to generate output files, if not using the --output
		 option.
	      --inspect
		 Fire up stringtemplate inspector on generated code (if sensible)
		 Default: false
	    * -t, --target
		 Target for code generation

	decode      Decode given IR signal
	  Usage: decode [options] durations in micro seconds, or pronto hex
	    Options:
	      -f, --frequency
		 Modulation frequency
	      -k, --keep-defaulted
		 Keep parameters equal to their defaults
		 Default: false
	      -a, --all, --no-prefer-over
		 Output all decodes; ignore prefer-over
		 Default: false
	      -p, --protocol
		 Comma separated list of protocols to try decode (default all)

	expression      Evaluate expression
	  Usage: expression [options] expression
	    Options:
	      --gui, --display
		 Display parse diagram
		 Default: false
	      -n, --nameengine
		 Name Engine to use
		 Default: {}
	      --stringtree
		 Produce stringtree
		 Default: false
	      --xml
		 Generate XML and write to file argument

	help      Report usage
	  Usage: help [options]

	list      List the protocols known
	  Usage: list [options] List of protocols (default all)
	    Options:
	      -c, --classify
		 Classify the protocols
		 Default: false
	      --gui, --display
		 Display parse diagram
		 Default: false
	      --documentation
		 List documentation
		 Default: false
	      -i, --irp
		 List IRP
		 Default: false
	      --is
		 test toIrpString
		 Default: false
	      --stringtree
		 Produce stringtree
		 Default: false
	      -w, --weight
		 Compute weight
		 Default: false

	render      Render signal
	  Usage: render [options] protocol(s) or pattern (default all)
	    Options:
	      -i, --irp
		 IRP string to use as protocol definition
	      -n, --nameengine
		 Name Engine to use
		 Default: {}
	      -p, --pronto
		 Generate Pronto hex
		 Default: false
	      --random
		 Generate random, but valid, parameters
		 Default: false
	      -r, --raw
		 Generate raw form
		 Default: false
	      --test
		 Compare with IrpMaster
		 Default: false

	version      Report version
	  Usage: version [options]

	writeconfig      Write a new config file in XML format, using the --inifile argument
	  Usage: writeconfig [options]
