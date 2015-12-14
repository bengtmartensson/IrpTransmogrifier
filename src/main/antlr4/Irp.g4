/*
Copyright (C) 2015 Bengt Martensson.

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

options {
}

tokens {
   PROTOCOL,
   GENERALSPEC,
   FREQUENCY,
   BITDIRECTION,
   DUTYCYCLE,
   UNIT,
   BITSPEC_IRSTREAM,
   BITSPEC,
   IRSTREAM,
   BARE_IRSTREAM,
   BITFIELD,
   INFINITE_BITFIELD,
   COMPLEMENT,
   REVERSE,
   FLASH,
   GAP,
   EXTENT,
   REPEAT_MARKER,
   VARIATION,
   POWER,
   UMINUS,
   BITCOUNT,
   FLOAT,
   ASSIGNMENT,
   DEFINITIONS,
   DEFINITION,
   PARAMETER_SPECS,
   PARAMETER_SPEC,
   PARAMETER_SPEC_MEMORY
}

@header {
package org.harctoolbox.irp;
}

// TODO
// Due to the lexer, there can be no "name"s called k, u, m, p, lsb, or msb.
// I am not sure if it is an issue, but it is ugly.

// 1.7
protocol:
        generalspec bitspec_irstream definitions* parameter_specs? // Difference: * instead of ?, parameterspec
;

// 2.2, simplified
// This is simpler than Graham in the sense that some silly input is not rejected.
generalspec:
	'{'  generalspec_list  '}'
           ;

generalspec_list:
        /* Empty */
    | generalspec_item (',' generalspec_item )*
    ;

generalspec_item:
      frequency_item    # frequency
    | unit_item         # unit
    | order_item        # byteorder
    | dutycycle_item    # dutycycle
	;

frequency_item:
      number_with_decimals 'k'
;

dutycycle_item:
                  number_with_decimals '%'
;

unit_item:
             number_with_decimals ('u')?  # unitInMicroseconds
           | number_with_decimals 'p'     # unitInPeriods
;

order_item:
        LSB         # orderLSB
        | MSB       # orderMSB
	;

//3.2
duration:
        flash_duration | gap_duration
    ;

flash_duration:
                  name_or_number ('m' | 'u' | 'p')?
	;

gap_duration:
                '-' name_or_number ('m' | 'u' | 'p')?
	;

name_or_number:
                  name | number_with_decimals // Diff: Graham allowed number (integers) only
	;

// 4.2
extent:
      '^'  name_or_number ('m' | 'u' | 'p')?
;

//  5.2
bitfield:
        t='~'? data=primary_item ':'  (m='-'? length=primary_item (':' chop=primary_item)?
                                    |  ':'  chop=primary_item
                                      )
	;

primary_item:
        name
        | DOLLAR_ID
	| number
	| expression
	;

// 6.2
irstream:
        '(' bare_irstream ')' repeat_marker?
    ;

bare_irstream:
        /* Empty */
        | irstream_item (','  irstream_item)*
	;

irstream_item:
        variation
    | bitfield // must come before duration!
    | assignment
    | extent
    | duration
    | irstream
    | bitspec_irstream
    ;

// 7.4
bitspec:
    '<'  bare_irstream ('|'  bare_irstream)* '>'
    ;

// 8.2
repeat_marker:
	 '*'
        | '+'
        | INT '+'?
;

bitspec_irstream:
	bitspec irstream
;

// 9.2
expression:
        '(' bare_expression ')'
	;

// Following rules were rewritten to avoid left recursion
bare_expression:
        inclusive_or_expression
	;

inclusive_or_expression	:
        exclusive_or_expression ('|' exclusive_or_expression)*
	;

exclusive_or_expression:
	and_expression ('^' and_expression)*
	;

and_expression:
    additive_expression ('&'  additive_expression)*
	;

additive_expression:
                       multiplicative_expression (('+' | '-')  multiplicative_expression)*
	;

multiplicative_expression:
                             exponential_expression ( ('*' | '/' | '%')  exponential_expression)*
	;

exponential_expression:
        unary_expression ('**' unary_expression)*
	;

unary_expression:
        bitfield            # bitfield_expression
        | primary_item      # primary_item_expression
        | '-' bitfield      # minus_bitfield_expresson
        | '-' primary_item  # minus_primary_item_expression
        | '#' bitfield      # count_bitfield_expression
        | '#' primary_item  # count_primary_item_expression
                ;

// 10.2
definitions:
	'{' definitions_list '}'
	;

definitions_list:
	/* Empty */
        | definitions_list (',' definition)
	;

definition:
	name '=' bare_expression
	;

// 11.2
assignment:
    name '=' bare_expression
	;

// 12.2
variation:
        intro=alternative repetition=alternative ending=alternative?
    ;

alternative:
        '[' bare_irstream ']'
	;

// 13.2
number:
        INT
;

number_with_decimals:
        INT             # integerAsFloat
      | float_number    # float
    ;

name:
    ID
    | 'k'
    | 'u'
    | 'p'
    | 'm'
    | 'lsb'
    | 'msb'
    ;

parameter_specs:
    '[' parameter_spec (',' parameter_spec )* ']'
    | '['  ']'
    ;

parameter_spec:
	   name      ':' INT '.' '.' INT ('=' bare_expression)? # memorylessParameterSpec
	|  name  '@' ':' INT '.' '.' INT  '=' bare_expression   # memoryfullParameterSpec
    ;

float_number:
           '.' INT         # dotInt
	|   INT '.' INT    # intDotInt
	;


LSB:
        'lsb'
    ;

MSB:
        'msb'
    ;

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
PERCENT: '%';
EXP: '**';

// Diff: Allow C syntax identifiers; Graham disallowed lower case and underscores.
ID:
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
		//('A'..'Z' ('A'..'Z'|'0'..'9')*  // Graham's version
;

DOLLAR_ID:
             '$' ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT:
	( '0' .. '9')+
;

// Diff: Not present by Graham.
COMMENT:
           ('//' ~('\n'|'\r')* '\r'? '\n'
    |   '/*' .*? '*/') -> skip
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
