public enum TokenType {
    // character tokens
    PLUS, MINUS, STAR, SLASH, LPAREN, RPAREN, EQUAL, SEMICOLON,

    // for code block
    LBRACE, RBRACE,

    // Logic and Comparisons
    BANG, BANG_EQUAL,
    EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    NUMBER, IDENTIFIER, STRING,

    // Keywords
    IF, ELSE, WHILE, FOR, TRUE, FALSE, LET, PRINT, EOF
}