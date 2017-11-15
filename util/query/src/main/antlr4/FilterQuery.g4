grammar FilterQuery;

query
  : orExpression
  ;

orExpression
  : andExpression ( OR andExpression )*
  ;

andExpression
  : notExpression ( AND notExpression )*
  ;

notExpression
  : NOT baseExpression
  | baseExpression
  ;

baseExpression
  : LPAR query RPAR
  | comparisonExpression
  | inExpression
  ;

comparisonExpression
  : ATTRIBUTE_NAME operator value
  ;

inExpression
  : ATTRIBUTE_NAME IN LPAR value (COMMA value )* RPAR
  ;

operator
   : LESS
   | LESS_EQUAL
   | GREATER
   | GREATER_EQUAL
   | EQUALS
   | CONTAINS
   | STARTS_WITH
   | ENDS_WITH
   ;

value
  : NUMBER
  | bool
  | STRING
  ;

bool
  : TRUE
  | FALSE
  ;

LPAR: '(';
RPAR: ')';
COMMA: ',';

OR: 'or';
AND: 'and';
NOT: 'not';
IN: 'in';

LESS: 'lt';
LESS_EQUAL: 'le';
GREATER: 'gt';
GREATER_EQUAL: 'ge';
EQUALS: 'equals';
CONTAINS: 'contains';
STARTS_WITH: 'startswith';
ENDS_WITH: 'endswith';

ATTRIBUTE_NAME: [a-zA-Z_][a-zA-Z0-9_]*;

NUMBER: (([-+])? ([0-9])+ | ([-+])? ([0-9])* '.' ([0-9])+) ;

fragment
STRING_CONTENT: (~[\n\r\f\\\"])*;

STRING : '"' STRING_CONTENT '"';

TRUE: 'true';
FALSE: 'false';

SPACE: [ \t]+ -> skip ;