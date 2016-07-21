/*
Copyright (C) 2016 Bengt Martensson.

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

grammar Irp;

// 1.7
// class Protocol
// Extension: * instead of ?, parameter_specs
protocol:
    generalspec bitspec_irstream definitions* parameter_specs?
;

// 2.2, simplified
// Difference: This a simplified version; implementing exclusions is not really
// mainstream... Some silly input is not rejected.
// My semantics: read left-to-right, later entries overwrite.
// class GeneralSpec
generalspec:
    '{' generalspec_list '}'
;

generalspec_list:
    /* Empty */
    | generalspec_item (',' generalspec_item )*
;

// extension: dutycycle_item
generalspec_item:
    frequency_item
    | unit_item
    | order_item
    | dutycycle_item
;

frequency_item:
    number_with_decimals 'k'
;

dutycycle_item:
    number_with_decimals '%'
;

unit_item:
    number_with_decimals ('u' | 'p')?
;

// enum BitDirection
order_item:
    'lsb' | 'msb'
;

// 3.2
// abstract class Duration implements Numerical
// Note: spec did not consider extent as a duration
duration:
    flash
    | gap
    | extent
;

// class Flash extends Duration
// called flash_duration in spec
flash:
    name_or_number ('m' | 'u' | 'p')?
;

// class Gap extends Duration
// called gap_duration in spec
gap:
    '-' name_or_number ('m' | 'u' | 'p')?
;

// class NameOrNumber implements Floatable
// Extension: Spec allowed number (integers) only
name_or_number:
    name
    | number_with_decimals
;

// 4.2
// class extent (extends Duration)
// Semantics: An extent is a gap, with all preceeding durations in the
// containing bare_irstream subtracted. More than one extent in one
// bare_irstream is thus allowed.
extent:
    '^' name_or_number ('m' | 'u' | 'p')?
;

//  5.2
// abstact class BitField extends IrStreamItem implements Numerical
// class FiniteBitField extends BitField
// class InfiniteBitField extends BitField
bitfield:
    '~'? primary_item ':' '-'? primary_item (':' primary_item)? # finite_bitfield
    | '~'? primary_item '::' primary_item                    # infinite_bitfield
;

// abstract class PrimaryItem implements Numerical
primary_item:
    name
//    | DOLLAR_ID
    | number
    | para_expression
;

// 6.2
// class IrStream extends BareIrStream
irstream:
    '(' bare_irstream ')' repeat_marker?
;

// class BareIrStream extends IrStreamItem
// extension: the ?: form
bare_irstream:
    /* Empty */
    | expression '?' bare_irstream ':' bare_irstream
    | irstream_item (','  irstream_item)*
;

// abstract class IrStreamItem
// Note: extent was implicit within duration
irstream_item:
    variation
    | bitfield  // must come before duration!
    | assignment
    | duration
    | irstream
    | bitspec_irstream
;

// 7.4
// class BitSpec extends IrStreamItem
bitspec:
    '<'  bare_irstream ('|' bare_irstream)* '>'
;

// 8.2
// class RepeatMarker
repeat_marker:
    '*'
    | '+'
    | number '+'?
;

// class BitspecIrstream
bitspec_irstream:
    bitspec irstream
;

// 9.2
// class Expression implements Numerical, InfixCode
// para_expression is called expression in spec
para_expression:
    '(' expression ')'
;

// called bare_expression in spec
expression:
                    primary_item
    |               bitfield
    |               '!'             expression
    |               '-'             expression
    |               '#'             expression
    | <assoc=right> expression '**'                 expression
    |               expression ('*' | '/' | '%')    expression
    |               expression ('+' | '-')          expression
    |               expression ('<<' | '>>')        expression
    |               expression ('<=' | '>=' | '>' | '<') expression
    |               expression ('==' | '!=')        expression
    |               expression '&'                  expression
    |               expression '^'                  expression
    |               expression '|'                  expression
    |               expression '&&'                 expression
    |               expression '||'                 expression
    | <assoc=right> expression '?'                  expression ':' expression
;

// 10.2
// (class NameEngine)
definitions:
    '{' definitions_list '}'
;

definitions_list:
    /* Empty */
    | definition (',' definition)*
;

definition:
    name '=' expression
;

// 11.2
assignment:
    name '=' expression
;

// 12.2
variation:
    alternative alternative alternative?
;

alternative:
    '[' bare_irstream ']'
;

// 13.2
// class Number implements Numerical,InfixCode
number:
      INT
    | HEXINT
    | BININT
    | 'UINT8_MAX'
    | 'UINT16_MAX'
    | 'UINT24_MAX'
    | 'UINT32_MAX'
    | 'UINT48_MAX'
    | 'UINT64_MAX'
;

// class numberWithDecimals extends Floatable
number_with_decimals:
    number
    | float_number
;

// Due to the lexer, have to take special precautions to allow name-s
// to be called k, u, m, p, lsb, or msb. See Parr p.209-211.
// class Name implements Numerical,InfixCode
name:
    ID
    | 'k'
    | 'u'
    | 'p'
    | 'm'
    | 'lsb'
    | 'msb'
;

// class ParameterSpecs
parameter_specs:
    '[' parameter_spec (',' parameter_spec )* ']'
    | '['  ']'
;

// class ParameterSpec
parameter_spec:
      name     ':' number '..' number ('=' expression)?
    | name '@' ':' number '..' number  '=' expression
;

// class FloatNumber implements Floatable
float_number:
    '.' INT
    | INT '.' INT
;

// Extension: Here allow C syntax identifiers;
// Graham allowed only one letter capitals.
ID:
    ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
;

//DOLLAR_ID:
//    '$' ID
//;

INT:
    ( '0' .. '9')+
;

HEXINT:
    '0x' ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
;

BININT:
    '0b' ( '0' | '1' )+
;

// Extension: Not present by Graham.
COMMENT: // non-greedy
    '/*' .*? '*/'                       -> skip
;

LINECOMMENT:
    '//' ~('\n'|'\r')* '\r'? '\n'       -> skip
;

WS:
    [ \t\r\n\u000C]+                    -> skip
;
