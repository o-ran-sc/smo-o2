/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Ericsson
 *  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
/*
 The contents of this file were inspired by https://github.com/onap/cps/blob/master/cps-path-parser/src/main/antlr4/org/onap/cps/cpspath/parser/antlr4/CpsPath.g4, which falls under the Apache 2.0 license:
 ** The original copyright is as follows
 **  ============LICENSE_START=======================================================
 **  Copyright (C) 2021-2023 Nordix Foundation
 **  Modifications Copyright (C) 2023 TechMahindra Ltd
 **  ================================================================================
 **  Licensed under the Apache License, Version 2.0 (the "License");
 **  you may not use this file except in compliance with the License.
 **  You may obtain a copy of the License at
 **
 **        http://www.apache.org/licenses/LICENSE-2.0
 **
 **  Unless required by applicable law or agreed to in writing, software
 **  distributed under the License is distributed on an "AS IS" BASIS,
 **  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **  See the License for the specific language governing permissions and
 **  limitations under the License.
 **
 **  SPDX-License-Identifier: Apache-2.0
 **  ============LICENSE_END=========================================================
 */

/*
 * Parser Rules
 * Some of the parser rules below are inspired by
 * https://github.com/antlr/grammars-v4/blob/master/xpath/xpath31/XPath31Parser.g4
 */

grammar tiesPath ;

tiesPath : ( prefix | incorrectPrefix ) multipleLeafConditions? textFunctionCondition? containsFunctionCondition? ancestorAxis? invalidPostFix? containerName? fieldLeaf?;

ancestorAxis : SLASH KW_ANCESTOR COLONCOLON ancestorPath ;

ancestorPath : yangElement ( SLASH yangElement)* ;

textFunctionCondition : SLASH leafName OB KW_TEXT_FUNCTION EQ StringLiteral CB ;

containsFunctionCondition : OB KW_CONTAINS_FUNCTION OP AT leafName COMMA StringLiteral CP CB ;

parent : ( SLASH yangElement)* ;

prefix : parent SLASH containerName ;

incorrectPrefix : SLASH SLASH+ ;

yangElement : containerName listElementRef? ;

containerName : QName ;

listElementRef :  OB leafCondition ( booleanOperators leafCondition)* CB ;

multipleLeafConditions : OB leafCondition ( booleanOperators leafCondition)* CB ;

leafCondition : AT leafName comparativeOperators ( IntegerLiteral | StringLiteral) ;

leafName : QName ;

booleanOperators : ( KW_AND | KW_OR ) ;

comparativeOperators : ( EQ | GT | LT | GE | LE ) ;

invalidPostFix : (AT | CB | COLONCOLON | comparativeOperators ).+ ;

fieldLeaf : OP leafName ( COMMA leafName)* CP;

/*
 * Lexer Rules
 * Most of the lexer rules below are inspired by
 * https://github.com/antlr/grammars-v4/blob/master/xpath/xpath31/XPath31Lexer.g4
 */

AT : '@' ;
CB : ']' ;
COLONCOLON : '::' ;
EQ : '=' ;
OB : '[' ;
SLASH : '/' ;
COMMA : ',' ;
OP : '(' ;
CP : ')' ;
GT : '>' ;
LT : '<' ;
GE : '>=' ;
LE : '<=' ;

// KEYWORDS

KW_ANCESTOR : 'ancestor' ;
KW_AND : 'and' ;
KW_TEXT_FUNCTION: 'text()' ;
KW_OR : 'or' ;
KW_CONTAINS_FUNCTION: 'contains' ;

IntegerLiteral : FragDigits ;
// Add below type definitions for leafvalue comparision in https://jira.onap.org/browse/CPS-440
DecimalLiteral : ('.' FragDigits) | (FragDigits '.' [0-9]*) ;
DoubleLiteral : (('.' FragDigits) | (FragDigits ('.' [0-9]*)?)) [eE] [+-]? FragDigits ;
StringLiteral : '"' (~["] | FragEscapeQuot)* '"' | '\'' (~['] | FragEscapeApos)* '\'' ;
fragment FragEscapeQuot : '""' ;
fragment FragEscapeApos : '\'\'';
fragment FragDigits : [0-9]+ ;

QName  : FragQName ;
NCName : FragmentNCName ;
fragment FragQName : FragPrefixedName | FragUnprefixedName ;
fragment FragPrefixedName : FragPrefix ':' FragLocalPart ;
fragment FragUnprefixedName : FragLocalPart ;
fragment FragPrefix : FragmentNCName ;
fragment FragLocalPart : FragmentNCName ;
fragment FragNCNameStartChar
  :  'A'..'Z'
  |  '_'
  | 'a'..'z'
  | '\u00C0'..'\u00D6'
  | '\u00D8'..'\u00F6'
  | '\u00F8'..'\u02FF'
  | '\u0370'..'\u037D'
  | '\u037F'..'\u1FFF'
  | '\u200C'..'\u200D'
  | '\u2070'..'\u218F'
  | '\u2C00'..'\u2FEF'
  | '\u3001'..'\uD7FF'
  | '\uF900'..'\uFDCF'
  | '\uFDF0'..'\uFFFD'
  | '\u{10000}'..'\u{EFFFF}'
  ;
fragment FragNCNameChar
  :  FragNCNameStartChar | '-' | '.' | '0'..'9'
  |  '\u00B7' | '\u0300'..'\u036F'
  |  '\u203F'..'\u2040'
  ;
fragment FragmentNCName : FragNCNameStartChar FragNCNameChar* ;

// https://www.w3.org/TR/REC-xml/#NT-Char

fragment FragChar : '\u0009' | '\u000a' | '\u000d'
  | '\u0020'..'\ud7ff'
  | '\ue000'..'\ufffd'
  | '\u{10000}'..'\u{10ffff}'
  ;

// Skip all Whitespace
Whitespace : ('\u000d' | '\u000a' | '\u0020' | '\u0009')+ -> skip ;

// handle characters which failed to match any other token (otherwise Antlr will ignore them)
ErrorCharacter : . ;
