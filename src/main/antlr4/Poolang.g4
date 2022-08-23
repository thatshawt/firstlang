grammar Poolang;

/*
 * Production Rules (non-terminal)
 */
start : ((func_decl | stmt)*) EOF?;

stmt: (if_stmt | assignment_stmt | funcCall_stmt | func_decl | return_stmt);

return_stmt: 'return' expression;

funcCall_stmt
    : funcCall_expr ';'                                 # NormalFuncCall
    | 'print' '(' stringLiteral ')' ';'                 # PrintStringCall
    | 'print' '(' arith_expr ')' ';'                    # PrintArithCall
    | 'print' '(' cond_expr ')' ';'                     # PrintCondCall
    | 'print' '(' identifier ':' primitive_types ')' ';'# PrintReferenceCall
    ;

assignment_stmt
    : bool_t identifier '=' cond_expr ';'           # BooleanAssign
    | int_t identifier '=' arith_expr ';'           # IntegerAssign
    | float_t identifier '=' arith_expr ';'         # FloatAssign
    | primitive_types identifier '=' expression ';' # UnsafeAssign //type not guaranteed
    ;

if_stmt
    : 'if' '(' cond_expr ')' ifblock=block              # If
    | 'if' '(' cond_expr ')' ifblock=block 'else' elseblock=block # Else
    | 'if' '(' cond_expr ')' ifblock=block ('elif' '(' cond_expr ')' block)+ #IfElif
    | 'if' '(' cond_expr ')' ifblock=block ('elif' '(' cond_expr ')' block)+ 'else' elseblock=block #IfElifElse
    ;

func_decl: primitive_types identifier
               '(' ( primitive_types identifier (',' primitive_types identifier)*)? ')' block;

expression: (arith_expr | cond_expr | funcCall_expr);

// (1 + sin(2)) / 2
arith_expr
   : integerLiteral                                         # Integer
   | floatLiteral                                           # Float
   | identifier ':' numeric_t                               # NumericReference
   | '(' inner=arith_expr ')'                               # Parentheses              //p spells pemdas
   | left=arith_expr operator=POW right=arith_expr          # Power                    //e
   | left=arith_expr operator=(MUL|DIV) right=arith_expr    # MultiplicationOrDivision //md
   | left=arith_expr operator=(ADD|SUB) right=arith_expr    # AdditionOrSubtraction    //as
   ;

cond_expr
    : booleanLiteral                                # TrueOrFalse
    | identifier ':' bool_t                                     # BooleanReference
    | left=arith_expr operation=numeric_relationals right=arith_expr # NumericRelational
    | left=cond_expr operation=conditional_relationals right=cond_expr # ConditionalRelational
    | operator=NOT right=cond_expr                  # Not
    | left=cond_expr operator=AND right=cond_expr   # And
    | left=cond_expr operator=OR right=cond_expr    # Or
    ;

funcCall_expr
    : identifier '(' ( expression (',' expression)*)? ')' ':' primitive_types;

block: '{' stmt* '}';

primitive_types
    : BOOL
    | INT
    | FLOAT
    ;

bool_t: BOOL;
int_t: INT;
float_t: FLOAT;
numeric_t: int_t | float_t;

identifier: ID;

integerLiteral: INTEGER;
floatLiteral: DECIMAL;
booleanLiteral: (TRUE | FALSE);
stringLiteral: STRING;

numeric_relationals: (GT | GTE | ST | STE | EQUALTO | NOT_EQUALTO);
conditional_relationals: (EQUALTO | NOT_EQUALTO | AND | OR);
//reference: TYPE ID;
//anumber: NUMBER;

/*
 * Tokens (terminal)
 */
POW: '^';
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';

GT: '>';
GTE: '>=';
ST: '<';
STE: '<=';

TRUE: 'true';
FALSE: 'false';

AND: '&&';
OR: '||';
NOT: '!';

EQUALTO: '==';
NOT_EQUALTO: '!=';

BOOL: 'bool';
INT: 'int';
FLOAT: 'float';

DECIMAL: '-'? DIGIT+ '.' DIGIT+;// -123.123
INTEGER: '-'? DIGIT+;

STRING:     '"' (~["\\\r\n] | EscapeSequence)* '"';
ID : [a-zA-Z_] [a-zA-Z]*;

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment DIGIT: [0-9];

//ANY: .;