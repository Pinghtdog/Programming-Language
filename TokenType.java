public enum TokenType {
    // Structure Keywords
    SCRIPT, AREA, START, END,

    // Data Types
    DECLARE, INT, CHAR, BOOL, FLOAT,

    // Control Flow Keywords
    IF, ELSE, FOR, REPEAT, WHEN,

    // I/O Keywords
    PRINT, SCAN,

    // Logical Operators
    AND, OR, NOT,

    // Single-character tokens
    PLUS, MINUS, STAR, SLASH, MODULO,
    LPAREN, RPAREN, LBRACKET, RBRACKET,
    COMMA, COLON, AMPERSAND, DOLLAR,

    // One or two character tokens
    EQUAL, EQUAL_EQUAL, // = and ==
    GREATER, GREATER_EQUAL, // > and >=
    LESS, LESS_EQUAL, // < and <=
    NOT_EQUAL, // <>

    // Literals
    IDENTIFIER, INT_LITERAL, FLOAT_LITERAL, CHAR_LITERAL, STRING_LITERAL,

    // Special
    NEWLINE, EOF
}