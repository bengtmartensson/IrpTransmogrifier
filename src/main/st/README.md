# Code generation
## Usage
    irptransmogrifier code [--target target]+ [--parameter name:value]* [options] protocol+

generates code for the target `target` for the protocols given. The `--target`
option is mandatory, and may be given several times. For a list of all options, invoke
the program with argument `--help`.

## XML export
If `xml` is used for target, the program will instead generate an XML file internally in the program, not using StringTemplate.
This can be used by XML transforming tools (like XSLT) to generate code. Currently, the Lirc
exporter for IrScrutinizer is generated that way. IrpTransmorgrifier does not contain an XML transformation
engine, so an external programs has to be invoked. For this, XSLT2 and
[Saxon home edition](http://www.saxonica.com/download/opensource.xml) is recommended.

Presently, no schema or DTD for the XML format exists.

## This directory
This directory contains the [StringTemplate4](http://www.stringtemplate.org/) files used for code generation.
[Documentation  found here.](https://github.com/antlr/stringtemplate4/blob/master/doc/index.md)

Users are encouraged to copy this directory and make modifications to their version.
Use the `--stdirectory` to IrpTransmogrifier to use that copy.

## Naming convention for user accessible files

`<targettype>-<functiontype>-<filetype>.stg`, for example `infrared4arduino-renderer-header.stg`.

targettype: java, infrared4arduino, irremote,...

functiontype: renderer, decoder, meta,...

filetype: (empty), header, code, test,...

## Import and inheritance
[Reference documentation](https://github.com/antlr/stringtemplate4/blob/master/doc/inheritance.md).

Current inheritences (all non-leaves are abstract "classes"):

     generic -+--infix -+- clike -+- java ---+- java-*-*
              |                   |
              +- dumb             +- c ------+- cplusplus -+- infrared4arduino-*-*-*
                                                           |
                                                           +- irremote -+- irremote-*-*


## Implementing new code generation targets

Implementing a new target is comparatively easy -- just to write ST files to generated the desired code
from the information provided by the program. Knowledge of Java or the inner workings of IrpTransmogrifier
are not required. That information is of the form of hierarchical directories of values.
To have a look at it, "generate code for the target `dummy`", preferably running it through a formatter like `indent`.
More explicitly, use a command like

    irptransmogrifier code -t dump nec1 | indent

(The final error message from indent should be ignored -- it expects C code, which is not what it gets).

Look at the existing targets, and try to get the idea...

The information, both syntax and semantic, is likely to change. For this reason,
a detailed description is not planned.

### Parametrization
A code generation target can be controlled by one or more parameters. These are given by the user using the `--parameter` option.
This may be given multiple times. There parameters are uninterpreted included in the `metaData` dictionary. (They may even overwrite
other parameters -- intended or not!)

### Debugging
It the option `--inspect` is given, [a StringTemplate inspection window](https://github.com/antlr/stringtemplate4/blob/master/doc/inspector.md)
is opened, allowing the user to interactively examine the generated code.

### Formatting
It is OK to ignore the formatting of the generated code, and instead delegate this task to an external program,
like [Gnu indent](https://www.gnu.org/software/indent/manual/indent.html), available on most Linux- and Unix system, as well as Cygwin.
In this case, please write a comment in the generated code
stating this fact, preferably together with recommended parameters.

## Final remarks
Code generation with StringTemplate is a very powerful concept, not restricted just to generate program code for rendering and decoding
in programming languages like C and the C family. For example, test cases, Makefiles, textual information (HTML, text, csv, (La-)TeX etc)
are all possible.