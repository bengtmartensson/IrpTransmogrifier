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
}

@header {
package org.harctoolbox.irp;
}

// 1.7
// class Protocol
protocol:
        generalspec bitspec_irstream definitions* parameter_specs? // Difference: * instead of ?, parameterspec
;

// 2.2, simplified
// This is simpler than Graham in the sense that some silly input is not rejected.
// class GeneralSpec
generalspec:
	'{' generalspec_list '}'
;

generalspec_list:
        /* Empty */
        | generalspec_item (',' generalspec_item )*
;

generalspec_item:
          frequency_item    # frequency
        | unit_item         # unit
        | order_item        # byteorder
        | dutycycle_item    # dutycycle  // Not present by Graham
;

frequency_item:
        number_with_decimals 'k'
;

dutycycle_item:
        number_with_decimals '%'
;

unit_item:
          number_with_decimals 'u'?  # unitInMicroseconds
        | number_with_decimals 'p'   # unitInPeriods
;

// enum BitDirection
order_item:
          LSB       # orderLSB
        | MSB       # orderMSB
;

//3.2
// class Duration implements Numerical
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
// class extent (extends duration)
extent:
        '^' name_or_number ('m' | 'u' | 'p')?
;

//  5.2
// class bitfield (extends IrStreamItem implements Numerical)
bitfield:
          '~'? primary_item ':' '-'? primary_item (':' primary_item)? # finite_bitfield
        | '~'? primary_item ':'                    ':' primary_item   # infinite_bitfield
;

// class PrimaryItem implements Numerical, InfixCode
primary_item:
          name          # name_asitem
        | DOLLAR_ID     # DOLLAR_ID_asitem
	| number        # number_asitem
	| expression    # expression_asitem
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
          variation         # variation_asitem
        | bitfield          # bitfield_asitem // must come before duration!
        | assignment        # assignment_asitem
        | extent            # extend_asitem
        | duration          # duration_asitem
        | irstream          # irstream_asitem
        | bitspec_irstream  # bitspec_irstream_asitem
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
        shift_expression ('&' shift_expression)*
;

shift_expression:       // Added
        additive_expression (('<' '<' | '>' '>')  additive_expression)*
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

// class UnaryExpression implements Numeric, InfixCode
unary_expression:
        bitfield            # bitfield_expression
        | primary_item      # primary_item_expression
        | '-' bitfield      # minus_bitfield_expresson
        | '-' primary_item  # minus_primary_item_expression
        | '#' bitfield      # count_bitfield_expression
        | '#' primary_item  # count_primary_item_expression
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
	name '=' bare_expression
;

// 11.2
assignment:
        name '=' bare_expression
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

number_with_decimals:
        INT             # integerAsFloat
      | float_number    # float
;

// Due to the lexer, have to take special precaitions to allow name-s
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
	   name      ':' INT '.' '.' INT ('=' bare_expression)? # memorylessParameterSpec
	|  name  '@' ':' INT '.' '.' INT  '=' bare_expression   # memoryfullParameterSpec
;

// class FloatNumber implements Floatable
float_number:
           '.' INT         # dotInt
	|   INT '.' INT    # intDotInt
;

LSB:        'lsb';
MSB:        'msb';
ADD:        '+';
SUB:        '-';
MUL:        '*';
DIV:        '/';
PERCENT:    '%';
EXP:        '**';

// Diff: Allow C syntax identifiers; Graham disallowed lower case and underscores.
ID:
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
;

DOLLAR_ID:
        '$' ID
;

INT:
	( '0' .. '9')+
;

// Diff: Not present by Graham.
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
