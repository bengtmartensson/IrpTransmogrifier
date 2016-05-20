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
// Extension: * instead of ?, parameterspec
protocol:
        generalspec bitspec_irstream definitions* parameter_specs?
;

// 2.2, simplified
// Difference: This a simplified version; implementing exclusions is not really
// mainstream... Some silly input is not rejected.
// class GeneralSpec
generalspec:
	'{' generalspec_list '}'
;

generalspec_list:
        /* Empty */
        | generalspec_item (',' generalspec_item )*
;

generalspec_item:
          frequency_item
        | unit_item
        | order_item
        | dutycycle_item // Not present by Graham
;

frequency_item:
        number_with_decimals 'k'
;

dutycycle_item:
        number_with_decimals '%'
;

unit_item:
          number_with_decimals 'u'?
        | number_with_decimals 'p'
;

// enum BitDirection
order_item:
          'lsb'
        | 'msb'
;

// 3.2
// abstract class Duration implements Numerical
duration:
          flash_duration
        | gap_duration
;

// class Flash extends Duration
flash_duration:
        name_or_number ('m' | 'u' | 'p')?
;

// class Gap extends Duration
gap_duration:
        '-' name_or_number ('m' | 'u' | 'p')?
;

// class NameOrNumber implements Floatable
name_or_number:
// Extension: Graham allowed number (integers) only
        name | number_with_decimals
;

// 4.2
// class extent (extends Duration)
extent:
        '^' name_or_number ('m' | 'u' | 'p')?
;

//  5.2
// abstact class BitField extends IrStreamItem implements Numerical
// class FiniteBitField extends BitField
// class InfiniteBitField extends BitField
bitfield:
          '~'? primary_item ':' '-'? primary_item (':' primary_item)? # finite_bitfield
        | '~'? primary_item ':'                    ':' primary_item   # infinite_bitfield
;

// abstract class PrimaryItem implements Numerical
primary_item:
          name
        | DOLLAR_ID
	| number
	| para_expression
;

// 6.2
// class IrStream extends BareIrStream
irstream:
        '(' bare_irstream ')' repeat_marker?
;

// class BareIrStream extends IrStreamItem
bare_irstream:
        /* Empty */
        | irstream_item (','  irstream_item)*
;

// abstract class IrStreamItem
irstream_item:
          variation
        | bitfield  // must come before duration!
        | assignment
        | extent
        | duration
        | irstream
        | bitspec_irstream
;

// 7.4
// class BitSpec extends IrStreamItem
bitspec:
        '<'  bare_irstream ('|'  bare_irstream)* '>'
;

// 8.2
// class RepeatMarker
repeat_marker:
	 '*'
        | '+'
        | INT '+'?
;

// class BitspecIrstream
bitspec_irstream:
	bitspec irstream
;

// 9.2
// class Expression implements Numerical, InfixCode
para_expression:            // was previously called expression
        '(' expression ')'
;

expression:                 // was previously called bare_expression
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
;

// class numberWithDecimals extends Floatable
number_with_decimals:
        INT
      | float_number
;

// Due to the lexer, have to take special precautions to allow name-s
// to be called k, u, m, p, lsb, or msb.
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
	   name      ':' INT '.' '.' INT ('=' expression)? # memorylessParameterSpec
	|  name  '@' ':' INT '.' '.' INT  '=' expression   # memoryfullParameterSpec
;

// class FloatNumber implements Floatable
float_number:
           '.' INT
	|   INT '.' INT
;

// Extension: Here allow C syntax identifiers;
// Graham allowed only one letter capitals.
ID:
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
;

DOLLAR_ID:
        '$' ID
;

INT:
	( '0' .. '9')+
;

// Extension: Not present by Graham.
COMMENT:
          ('//' ~('\n'|'\r')* '\r'? '\n'
        | '/*' .*? '*/') -> skip
;

WS:
        ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) -> skip
;

NEWLINE:
        '\r'? '\n'
;
