# IrpTransmogrifier
Parser for IRP notation protocols, with rendering, code generation, recognition applications

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

2. Recognition. This use case corresponds to a dynamic version of DecodeIR: given a numerical IR signal,
 find one (or all) parameter/protocol combinations that could have generated the given signal.
 Probably trying a number of different pre-rendered IRP parse trees, possibly a two-stage strategy,
 using IRP ["Meta protocols"](http://www.harctoolbox.org/IrpMaster.html#Preprocessing+and+inheritance).

3. Code generation for rendering. For a particular protocol, generate target code (C, C++, Java, Python,...) that can render signals
   with the selected protocol. As opposed to the previous use cases, efficency (memory, execution time) is potentially
   an issue. This should be able to generate protocol renders for e.g. the Arduino libraries IrRemote, IrLib, and AGirs.
   At least in the first version, not all protocols describable by IRPs need to be supported.

4. Code generation for recognition. Like above, but for recognizing received signals
 on embedded processors, using a certain protocol.

5. General (automated) code analysis. Not really connected to parsing IRP, but fits in the general framework.
 This corresponds to "Analyze" in Graham Dixon's ExchangeIR. 
