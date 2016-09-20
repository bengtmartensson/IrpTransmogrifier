# IrpTransmogrifier
Parser for IRP notation protocols, with rendering, code generation, recognition applications.

This is a new program, written from scratch, that is intended to replace IrpMaster, DecodeIR, and (most of) ExchangeIR,
and much more like potentially replacing "all" hand written decoders/renderers.
It consists of an API library that is also callable from the command line as a command line program.

## Background
The Irp parser [IrpMaster](http://www.harctoolbox.org/IrpMaster,html)
is based upon a [ANTLR3 grammar](http://www.antlr.org). The "new version"
ANTLR4 is really not a new version, but a completely different tool, fixing most of the quirks that irritated me
in IrpMaster (like the "ugliness" of embedding actions withing the grammar, no left recursion, and much more).
Unfortunately, this means that using version 4 instead of version 3 is not like updating a compiler or such,
but "necessitates" a complete rewrite of the actions.


## Use cases
The reason for this project is not (just) to migrate IrpMaster to ANTLR4 -- version 3 is still operational,
and no more broken than it was at the start. There are a number of nice use cases for a nice parser/tree traverser:

1. Rendering. This use case corresponds to IrpMaster. It is probably to be implemented as a traversing of the parse tree of an IRP protocol,
with numerical values assigned to the parameters.

_This has essentially been completed. Ambition level: only one infinite repeat allowed. With that restriction, should be
complete solution, down to specification holes._

2. Recognition. This use case corresponds to a dynamic version of DecodeIR: given a numerical IR signal,
 find one (or all) parameter/protocol combinations that could have generated the given signal.
 Probably trying a number of different pre-rendered IRP parse trees, possibly a two-stage strategy,
 using IRP ["Meta protocols"](http://www.harctoolbox.org/IrpMaster.html#Preprocessing+and+inheritance).

_Ambition level: should grok (almost) all of the existing protocols in IrpProtocols, but not necessarily be "complete".
Status: Most protocols work._

3. Code generation for rendering. For a particular protocol, generate target code (C, C++, Java, Python,...) that can render signals
   with the selected protocol. As opposed to the previous use cases, efficency (memory, execution time) is potentially
   an issue. This should be able to generate protocol renders for e.g. the Arduino libraries IrRemote, IrLib, and AGirs.
   At least in the first version, not all protocols describable by IRPs need to be supported.

_Ambition level: Should be able to generate the "important" protocols, not necessarily "complete". 
Status: An intermediate XML file is generated, on which XSLT stylesheets are operating. Presently, generating only Java code -- the simplest.
Most protocol sort-of works, but as a-priori given protocol, not like DecodeIR._

4. Code generation for recognition. Like above, but for recognizing received signals
 on embedded processors, using a certain protocol.

_Status: not started_

5. General (automated) code analysis. Not really connected to parsing IRP, but fits in the general framework.
 This corresponds to the Analyzer and the RepeatFinder in Graham Dixon's ExchangeIR.

_Status: (`org.harctoolbox.analyze.`)`Repeatfinder` and `Cleaner` completed (essentially adapted from recent
IrpMaster). Some work remaining (< 50%)._ 

## Protocol Data Base
The "ini"-file `IrpProtocols.ini` has been replaced by an XML file, per default called `IrpProtocols.xml`. The XML format
is defined by the schema [irp-protocols](http://www,harctoolbox.org/schemas/irp-protocols.xsd), and has the name space 
`http://www.harctoolbox.org/irp-protocols`. This format has many advantages in 
comparison with the previous, more primitive, format.

The program is capable of reading and translating the old format.

## Usage
Using from the command line, this is a command with subcommands

    Usage: IrpTransmogrifier [options] [command] [command options]
     Options:
    -c, --configfile
       Pathname of IRP database file in XML format
    -h, --help, -?
       Display help message
       Default: false
    -i, --ini, --inifile
       Pathname of IRP database file in ini format
    -l, --loglevel
       Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING
       }
       Default: INFO
    -o, --output
       Name of output file
    --seed
       Set seed for pseudo random number generation (default: random)
    -v, --version
       Report version
       Default: false
    Commands:
    analyze      Analyze signal
      Usage: analyze [options] durations, or pronto hex
        Options:
          -c, --clean
             Invoke the cleaner
             Default: false
          -f, --frequency
             Modulation frequency
             Default: 38000
          -r, --repeatfinder
             Invoke the repeatfinder
             Default: false

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
             List XML
             Default: false

    code      Generate code
      Usage: code [options] List of protocols (default all)
        Options:
          --decode
             Generate code for decoding, otherwise for rendering. Target
             dependent.
             Default: false
          --documentation
             List documentation
             Default: false
          -e, --encoding
             Encoding used for generating output
             Default: UTF-8
          -i, --irp
             List irp
             Default: false
          -r, --regex, --regexp
             Interpret arguments as regular expressions
             Default: false
          -s, --sort
             Sort the output
             Default: false
          --target
             Target for code generation (not yet evaluated)
          --xml
             List XML
             Default: false
          --xslt
             Pathname to XSLT

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
             List XML
             Default: false

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
          --irp
             List IRP
             Default: false
          --is
             test toIrpString
             Default: false
          -p, --parse
             Test parse the protocol(s)
             Default: false
          -r, --regex, --regexp
             Interpret arguments as regular expressions
             Default: false
          -s, --sort
             Sort the output
             Default: false
          --stringtree
             Produce stringtree
             Default: false

    recognize      Recognize signal
      Usage: recognize [options] durations, or pronto hex
        Options:
          -n, --nameengine
             Name Engine to generate test signal
             Default: {}
          -p, --protocol
             Protocol to decode against (default all)
          -r, --random
             Generate a random parameter signal to test
             Default: false
          --regex, --regexp
             Interpret arguments as regular expressions
             Default: false
          -s, --sort
             Sort the protocols
             Default: false
          -t, --test
             Generate a test signal and try to decode it
             Default: false

    render      Render signal
      Usage: render [options] protocol(s) or pattern (default all)
        Options:
          -n, --nameengine
             Name Engine to use
             Default: {}
          -p, --pronto
             Generate Pronto hex
             Default: false
          --random
             Generate random paraneters
             Default: false
          -r, --raw
             Generate raw form
             Default: false
          --regex, --regexp
             Generate random paraneters
             Default: false
          -s, --sort
             Sort the protocols?
             Default: false
          --test
             Compare with IrpMaster
             Default: false

    version      Report version
      Usage: version [options]

    writeconfig      Write a new config file in XML format
      Usage: writeconfig [options]


