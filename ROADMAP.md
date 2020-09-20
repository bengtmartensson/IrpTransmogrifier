# ROADMAP

This is an attempt to a high-level "roadmap" (or wishlist :-)) for IrpTransmogrifier.
The "low-level" issues are kept as [issues](https://github.com/bengtmartensson/IrpTransmogrifier/issues).

## Current status
Version 1.2.8 is quite stable and "good". Future changes are expected not to change the working of
the program, or the API, in an incompatible manner.

## 1. Decoding
There are presently two issues: [NEC2 signals are erroneously turned NEC, in loose mode](https://github.com/bengtmartensson/IrpTransmogrifier/issues/168)
and
[Decoding of long intra gaps too restrictive](https://github.com/bengtmartensson/IrpTransmogrifier/issues/111).
Both require fairly non-trivial changes, and not really show stoppers.

## 2. Bulk analyze functions
There are a [number of suggestion](https://github.com/bengtmartensson/IrpTransmogrifier/issues?q=is%3Aopen+is%3Aissue+label%3A%22Component%3A+Analyzer%22)
to improve the ability to handle analysis of several signals ("bulk analysis").

## 3. GUI
This is a [separate project](https://github.com/bengtmartensson/IrpTransmogrifier-GUI).

## 4. Target code generation
This is a [separate project](https://github.com/bengtmartensson/IrProtocolCodeGeneration).
