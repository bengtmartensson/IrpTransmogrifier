# README
This directory contains the [string template](http://www.stringtemplate.org/) files used for code generation.
[Documentation  found here.](https://github.com/antlr/stringtemplate4/blob/master/doc/index.md)

Users are encouraged to copy this directory and make modifications to their version.
Use the --stdirectory to IrpTransmogrifier to use the copy.

## Naming convention
### User accessible files

`<targettype>-<functiontype>-<filetype>.stg`, for example infrared4arduino-renderer-header.stg.

targettype: java, infrared4arduino, irremote,...

functiontype: renderer, decoder, meta,...

filetype: (empty), header, code, test,...

### Non-user accessible files
TBD. Presently no rules.