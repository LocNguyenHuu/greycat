/**
 * Copyright 2017 The GreyCat Authors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar GreyCatModel;

fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

STRING :  '"' (ESC | ~["\\])* '"' | '\'' (ESC | ~["\\])* '\'' ;
IDENT : [a-zA-Z_][a-zA-Z_0-9]*;
NUMBER : [\-]?[0-9]+'.'?[0-9]*;
WS : ([ \t\r\n]+ | SL_COMMENT | ML_COMMENT) -> skip ; // skip spaces, tabs, newlines
SL_COMMENT :  '//' ~('\r' | '\n')* ;
ML_COMMENT : '/*' .*? '*/' -> skip;

modelDcl: (constDcl | classDcl | globalIndexDcl | customTypeDcl | importDcl)*;
importDcl: 'import' STRING;
constDcl: 'const' name=IDENT ':' typeDcl ('=' constValueDcl)?;
constValueDcl: (simpleValueDcl | taskValueDcl);
simpleValueDcl: (IDENT | STRING | NUMBER);
taskValueDcl: actionValueDcl ('.' actionValueDcl)*;
actionValueDcl: IDENT ('(' actionParam* ')')?;
actionParam: STRING | NUMBER | subTask;
subTask: '{' taskValueDcl '}';

classDcl: 'class' name=IDENT parentDcl? '{' (constDcl | attributeDcl | relationDcl | referenceDcl | localIndexDcl)* '}';
parentDcl: 'extends' IDENT;
attributeDcl: 'att' name=IDENT ':' typeDcl ('=' attributeValueDcl)?;

typeDcl: (builtInTypeDcl | customBuiltTypeDcl);
customBuiltTypeDcl: IDENT;
builtInTypeDcl: ('Bool' | 'Boolean' | 'String' | 'Long' | 'Int' | 'Integer' | 'Double' |
                'DoubleArray' | 'LongArray' | 'IntArray' | 'StringArray' |
                'LongToLongMap' | 'LongToLongArrayMap' | 'StringToIntMap'|
                'DMatrix' |'LMatrix' |'EGraph' |'ENode' | 'KDTree' | 'NDTree' |
                'IntToIntMap' | 'IntToStringMap' | 'Task' | 'TaskArray' | 'Node');

attributeValueDcl: (IDENT | STRING | NUMBER | complexAttributeValueDcl);
complexAttributeValueDcl: '(' complexValueDcl (',' complexValueDcl)* ')';
complexValueDcl: (IDENT | STRING | NUMBER | ntupleValueDlc);
ntupleValueDlc: '(' (IDENT | STRING | NUMBER) (',' (IDENT | STRING | NUMBER))* ')';

relationDcl: 'rel' name=IDENT ':' type=IDENT (oppositeDcl)?;
referenceDcl: 'ref' name=IDENT ':' type=IDENT (oppositeDcl)?;
oppositeDcl: 'oppositeOf' name=IDENT;

localIndexDcl: 'index' name=IDENT ':' type=IDENT 'using' indexAttributesDcl (oppositeDcl)?;
indexAttributesDcl: IDENT (',' IDENT)*;

globalIndexDcl: 'index' name=IDENT ':' type=IDENT 'using' indexAttributesDcl;

customTypeDcl: 'type' name=IDENT '{' (attributeDcl | constDcl)* '}';

