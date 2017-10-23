# Generating Protocol Files for IRremote

[IRremote](https://github.com/z3t0/Arduino-IRremote) is a well known Arduino library for the generation,
sending, receiving, and decoding of IR signals. Although it has some problems,
is widely spread and popular.

IrpTransmogrifier can generate IRremote code for (most of) the protocols it knows.
Presently, only sending is implemented, although decoding is planned.
At the moment of this writing, code can
be generated for most of the protocols
(with the exception mainly of the protocols containing hierarchical bitspecs, most notably RC6).

IRremote was not conceived to be extended in this way. Therefore, the integration of generated protocols is comparably complicated.
Suggestions for simplifications and other improvements are welcome.

I perform my work in a Unix-like environment (Linux). The commands herein take heavy advantage of that environment,
in particular the shell. Windows users can install [Cygwin](https://www.cygwin.com/), which will allow the scripts here to be executed.
Porting to (e.g) the native Windows command line is probably possibly, but I simple do not care enough.

## How to use
There are essentially two different methods of generating new sending protocols:

First make sure that IrpTransmogrifier is installed.

### Manual method
1. Either just generate the code files with a command like

        irptransmogrifier code --target  -t irremote-renderer-code nec1 *** protocol > ir_protocol.cpp

(see the documentation for IrpTransmogrifier and/or the script `generate-irremote-protocol.sh`)
and manually "bake in" the thus generated code like described
[in IRremote](https://raw.githubusercontent.com/z3t0/Arduino-IRremote/master/ir_Template.cpp).
The file [utils.cpp](https://github.com/bengtmartensson/Arduino-IRremote/blob/codegen/src/utils.cpp)
will be needed however (possibly "baked in").


### Automatic method
2. Or apply a few simple changes to the IRremote sources;
either by checking out branch `codegen` of [my fork of IRremote](https://github.com/bengtmartensson/Arduino-IRremote/tree/codegen),
or by applying [a patch](https://github.com/bengtmartensson/IrpTransmogrifier/tree/master/src/main/doc/0001-Changes-for-accomplishing-for-auto-generated-protoco.patch).
to the [`beta` branch of IRremote](https://github.com/z3t0/Arduino-IRremote/tree/beta). After this, the script `generate-irremote-protocol.sh`
(located in the `tools` directory or IrpTransmogrifier) can be used to generate all the necessary files.  However, be sure to adjust the pathname of the IRremote souces (`IRREMOTE_DIR` and/or `IRREMOTE_SRC`) in the script first. The script `generate-irremote-prots.sh`
generates all the presently supported protocols. Note: these can/will overwrite some existing files of the type `src/ir_*.cpp`.
(presently, the following files will be replaced: TODO.
Also, the symbol `SEND_MITSUBISHI` will be doubly defined, and there will be two conflicting signatures for 
`IRsend::sendSharp(unsigned int, unsigned int)`.
This must be fixed in `IRremote.h` if the generated version of these protocols are to be included.

## Automated testing
It is also possible to generate test files for testing the generated files in a Software-In-the-Loop (SIL) test using
[this framework](https://github.com/bengtmartensson/IRremote-SIL). IrpTransmogrifier contains code generation targets for such test files,
as well as suitable Makefiles. For this, check out the project, and make sure that it is working. Then adjust the
pathname to the files in the script `generate-irremote-protocol.sh`, and that file should generate and perform the tests
-- of course assuming a suitable environment (make, gcc, etc).
