import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        try {
            while (match(TokenType.NEWLINE))
                ;

            consume(TokenType.SCRIPT, "Expect 'SCRIPT' at beginning of program.");
            consume(TokenType.AREA, "Expect 'AREA' after 'SCRIPT'.");
            consume(TokenType.NEWLINE, "Expect newline after 'SCRIPT AREA'.");

            while (match(TokenType.NEWLINE))
                ;

            consume(TokenType.START, "Expect 'START' before code.");
            consume(TokenType.SCRIPT, "Expect 'SCRIPT' after 'START'.");
            consume(TokenType.NEWLINE, "Expect newline after 'START SCRIPT'.");

            while (match(TokenType.NEWLINE))
                ;

            while (match(TokenType.DECLARE)) {
                statements.add(declaration());

                while (match(TokenType.NEWLINE))
                    ;
            }

            while (!check(TokenType.END) && !isAtEnd()) {
                if (match(TokenType.NEWLINE))
                    continue;
                statements.add(statement());
            }

            consume(TokenType.END, "Expect 'END' at the bottom of the program.");
            consume(TokenType.SCRIPT, "Expect 'SCRIPT' after 'END'.");

            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    private Stmt declaration() {
        Token dataType = consumeType("Expect variable type (INT, CHAR, BOOL, FLOAT).");

        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        do {
            Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
            names.add(name);

            Expr initializer = null;
            if (match(TokenType.EQUAL)) {
                initializer = expression();
            }
            initializers.add(initializer);

        } while (match(TokenType.COMMA));

        if (!isAtEnd() && !check(TokenType.END)) {
            consume(TokenType.NEWLINE, "Expect newline after declaration.");
        }

        return new Stmt.Declare(dataType, names, initializers);
    }

    private Token consumeType(String message) {
        if (match(TokenType.INT, TokenType.CHAR, TokenType.BOOL, TokenType.FLOAT)) {
            return previous();
        }
        throw error(peek(), message);
    }

    private Stmt statement() {
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.FOR))
            return forStatement();
        if (match(TokenType.REPEAT))
            return repeatStatement();
        if (match(TokenType.PRINT))
            return printStatement();
        if (match(TokenType.SCAN))
            return scanStatement();
        return expressionStatement();
    }

    private Stmt ifStatement() {

        consume(TokenType.LPAREN, "Expect '(' after 'IF'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after IF condition.");

        while (match(TokenType.NEWLINE))
            ;

        consume(TokenType.START, "Expect 'START' before 'IF' block.");
        consume(TokenType.IF, "Expect 'IF' after 'START'.");

        while (match(TokenType.NEWLINE))
            ;

        List<Stmt> thenBranch = new ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;
            thenBranch.add(statement());
        }

        consume(TokenType.END, "Expect 'END' after 'IF' block.");
        consume(TokenType.IF, "Expect 'IF' after 'END'.");

        while (match(TokenType.NEWLINE))
            ;

        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {

                elseBranch = ifStatement();
            } else {
                while (match(TokenType.NEWLINE))
                    ;
                consume(TokenType.START, "Expect 'START' before 'ELSE' block.");
                consume(TokenType.IF, "Expect 'IF' after 'START' in 'ELSE' block.");

                while (match(TokenType.NEWLINE))
                    ;

                List<Stmt> elseStmts = new ArrayList<>();
                while (!check(TokenType.END) && !isAtEnd()) {
                    if (match(TokenType.NEWLINE))
                        continue;
                    elseStmts.add(statement());
                }

                consume(TokenType.END, "Expect 'END' after 'ELSE' block.");
                consume(TokenType.IF, "Expect 'IF' after 'END'.");

                elseBranch = new Stmt.Block(elseStmts);
            }
        }

        return new Stmt.If(condition, new Stmt.Block(thenBranch), elseBranch);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (!isAtEnd() && !check(TokenType.END)) {
            consume(TokenType.NEWLINE, "Expect newline after statement.");
        }
        return new Stmt.Expression(expr);
    }

    private Stmt scanStatement() {
        consume(TokenType.COLON, "Expect ':' after SCAN.");

        List<Token> names = new ArrayList<>();
        do {
            names.add(consume(TokenType.IDENTIFIER, "Expect variable name to scan into."));
        } while (match(TokenType.COMMA));
        if (!isAtEnd() && !check(TokenType.END)) {
            consume(TokenType.NEWLINE, "Expect newline after statement.");
        }

        return new Stmt.Scan(names);
    }

    private Stmt printStatement() {
        consume(TokenType.COLON, "Expect ':' after PRINT.");
        Expr value = expression();

        while (match(TokenType.AMPERSAND)) {
            Token operator = previous();
            Expr right = expression();
            value = new Expr.Binary(value, operator, right);
        }

        if (!isAtEnd() && !check(TokenType.END)) {
            consume(TokenType.NEWLINE, "Expect newline after statement.");
        }

        return new Stmt.Print(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.DOLLAR)) {
            return new Expr.Literal("\n");
        }

        if (match(TokenType.STRING_LITERAL, TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.CHAR_LITERAL)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Stmt forStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'FOR'.");

        // init
        Token initName = consume(TokenType.IDENTIFIER, "Expect variable name.");
        consume(TokenType.EQUAL, "Expect '=' after variable.");

        Stmt initialization = new Stmt.Expression(new Expr.Assign(initName, expression()));

        consume(TokenType.COMMA, "Expect ',' after initialization.");

        // argument
        Expr condition = expression();
        consume(TokenType.COMMA, "Expect ',' after condition.");

        // increment
        Token incName = consume(TokenType.IDENTIFIER, "Expect variable name.");
        consume(TokenType.EQUAL, "Expect '=' after variable.");
        Stmt increment = new Stmt.Expression(new Expr.Assign(incName, expression()));

        consume(TokenType.RPAREN, "Expect ')' after FOR clauses.");
        while (match(TokenType.NEWLINE))
            ;

        // loop block
        consume(TokenType.START, "Expect 'START' before 'FOR' block.");
        consume(TokenType.FOR, "Expect 'FOR' after 'START'.");
        while (match(TokenType.NEWLINE))
            ;

        java.util.List<Stmt> body = new java.util.ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;
            body.add(statement());
        }

        consume(TokenType.END, "Expect 'END' after block.");
        consume(TokenType.FOR, "Expect 'FOR' after 'END'.");

        return new Stmt.For(initialization, condition, increment, new Stmt.Block(body));
    }

    private Stmt repeatStatement() {
        consume(TokenType.WHEN, "Expect 'WHEN' after 'REPEAT'.");
        consume(TokenType.LPAREN, "Expect '(' after 'WHEN'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after condition.");

        while (match(TokenType.NEWLINE))
            ;

        consume(TokenType.START, "Expect 'START' before 'REPEAT' block.");
        consume(TokenType.REPEAT, "Expect 'REPEAT' after 'START'.");
        while (match(TokenType.NEWLINE))
            ;

        java.util.List<Stmt> body = new java.util.ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;
            body.add(statement());
        }

        consume(TokenType.END, "Expect 'END' after block.");
        consume(TokenType.REPEAT, "Expect 'REPEAT' after 'END'.");

        return new Stmt.While(condition, new Stmt.Block(body));
    }

    // --- HELPER METHODS ---

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        System.err.println("Parse Error at line " + token.line + ": " + message);
        return new ParseError();
    }
}