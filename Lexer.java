import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("SCRIPT", TokenType.SCRIPT);
        keywords.put("AREA", TokenType.AREA);
        keywords.put("START", TokenType.START);
        keywords.put("END", TokenType.END);
        keywords.put("DECLARE", TokenType.DECLARE);
        keywords.put("INT", TokenType.INT);
        keywords.put("CHAR", TokenType.CHAR);
        keywords.put("BOOL", TokenType.BOOL);
        keywords.put("FLOAT", TokenType.FLOAT);
        keywords.put("IF", TokenType.IF);
        keywords.put("ELSE", TokenType.ELSE);
        keywords.put("FOR", TokenType.FOR);
        keywords.put("REPEAT", TokenType.REPEAT);
        keywords.put("WHEN", TokenType.WHEN);
        keywords.put("PRINT", TokenType.PRINT);
        keywords.put("SCAN", TokenType.SCAN);
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("NOT", TokenType.NOT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;

            // --- UPDATED: Escape Codes for printing reserved symbols! ---
            case '[':
                if (isAtEnd()) {
                    System.err.println("Line " + line + ": Unterminated escape sequence.");
                    break;
                }

                // Grab the EXACT next character, even if it is a ']'
                String escapedText = String.valueOf(advance());

                // Now check if it is properly closed with a ']'
                if (!isAtEnd() && peek() == ']') {
                    advance(); // Consume the closing ']'
                    addToken(TokenType.STRING_LITERAL, escapedText);
                } else {
                    System.err.println("Line " + line + ": Invalid escape sequence. Expected closing ']'.");
                }
                break;

            case ']':
                addToken(TokenType.RBRACKET);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '&':
                addToken(TokenType.AMPERSAND);
                break;
            case '$':
                addToken(TokenType.DOLLAR);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '/':
                addToken(TokenType.SLASH);
                break;

            case '%':
                if (match('%')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.MODULO);
                }
                break;

            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                if (match('>')) {
                    addToken(TokenType.NOT_EQUAL);
                } else if (match('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    addToken(TokenType.LESS);
                }
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            case '\n':
                addToken(TokenType.NEWLINE);
                line++;
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\'':
                charLiteral();
                break;
            case '"':
                stringLiteral();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.err.println("Line " + line + ": Unexpected character '" + c + "'");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }

    private void number() {
        boolean isFloat = false;
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance();
            while (isDigit(peek()))
                advance();
        }

        String value = source.substring(start, current);
        if (isFloat) {
            addToken(TokenType.FLOAT_LITERAL, Double.parseDouble(value));
        } else {
            addToken(TokenType.INT_LITERAL, Integer.parseInt(value));
        }
    }

    private void stringLiteral() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Line " + line + ": Unterminated string.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);

        addToken(TokenType.STRING_LITERAL, value);
    }

    private void charLiteral() {
        if (peek() != '\'')
            advance();
        if (peek() == '\'') {
            advance();
            String value = source.substring(start + 1, current - 1);
            addToken(TokenType.CHAR_LITERAL, value.charAt(0));
        }
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}