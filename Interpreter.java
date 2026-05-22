import java.util.List;

public class Interpreter {

    private Environment environment = new Environment();
    private static final java.util.Scanner inputScanner = new java.util.Scanner(System.in);

    public void interpret(List<Stmt> statements) {
        if (statements == null)
            return;
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private void executeBlock(List<Stmt> statements, Environment innerEnvironment) {
        Environment previous = this.environment;
        try {
            this.environment = innerEnvironment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            Object value = evaluate(((Stmt.Print) stmt).expression);
            System.out.println(stringify(value));
        } else if (stmt instanceof Stmt.Declare) {
            Stmt.Declare decl = (Stmt.Declare) stmt;
            for (int i = 0; i < decl.names.size(); i++) {
                Token name = decl.names.get(i);
                Expr initializer = decl.initializers.get(i);

                Object value = null;
                if (initializer != null) {
                    value = evaluate(initializer);
                }
                environment.define(name.lexeme, decl.dataType.type, value);
            }
        }
        // --- THE MISSING LINK 1: Evaluates standard math and assignments! ---
        else if (stmt instanceof Stmt.Expression) {
            evaluate(((Stmt.Expression) stmt).expression);
        } else if (stmt instanceof Stmt.Block) {
            Stmt.Block block = (Stmt.Block) stmt;
            executeBlock(block.statements, new Environment(environment));
        } else if (stmt instanceof Stmt.If) {
            Stmt.If ifStmt = (Stmt.If) stmt;
            Object condition = evaluate(ifStmt.condition);

            if (isTruthy(condition)) {
                execute(ifStmt.thenBranch);
            } else if (ifStmt.elseBranch != null) {
                execute(ifStmt.elseBranch);
            }
        } else if (stmt instanceof Stmt.While) {
            Stmt.While whileStmt = (Stmt.While) stmt;
            while (isTruthy(evaluate(whileStmt.condition))) {
                execute(whileStmt.body);
            }
        } else if (stmt instanceof Stmt.For) {
            Stmt.For forStmt = (Stmt.For) stmt;

            execute(forStmt.initialization);

            while (isTruthy(evaluate(forStmt.condition))) {
                execute(forStmt.body);
                execute(forStmt.increment);
            }
        } else if (stmt instanceof Stmt.Scan) {
            Stmt.Scan scan = (Stmt.Scan) stmt;
            for (Token name : scan.names) {
                System.out.print("> Enter value for " + name.lexeme + ": ");
                String input = inputScanner.nextLine().trim();

                Object parsedValue = parseInput(input);
                environment.assign(name, parsedValue);
            }
        }
    }

    private Object evaluate(Expr expr) {
        if (expr instanceof Expr.Literal) {
            return ((Expr.Literal) expr).value;
        }
        if (expr instanceof Expr.Variable) {
            return environment.get(((Expr.Variable) expr).name);
        }

        if (expr instanceof Expr.Assign) {
            Expr.Assign assign = (Expr.Assign) expr;
            Object value = evaluate(assign.value);
            environment.assign(assign.name, value);
            return value;
        }

        if (expr instanceof Expr.Grouping) {
            return evaluate(((Expr.Grouping) expr).expression);
        }

        if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;
            Object right = evaluate(unary.right);

            if (unary.operator.type == TokenType.MINUS) {
                if (right instanceof Double)
                    return -(double) right;
                if (right instanceof Integer)
                    return -(int) right;
            } else if (unary.operator.type == TokenType.PLUS) {
                return right;
            } else if (unary.operator.type == TokenType.NOT) {
                return isTruthy(right) ? "FALSE" : "TRUE";
            }
        }

        if (expr instanceof Expr.Logical) {
            Expr.Logical logical = (Expr.Logical) expr;
            Object left = evaluate(logical.left);

            if (logical.operator.type == TokenType.OR) {
                if (isTruthy(left))
                    return "TRUE";
            } else if (logical.operator.type == TokenType.AND) {
                if (!isTruthy(left))
                    return "FALSE";
            }

            Object right = evaluate(logical.right);
            return isTruthy(right) ? "TRUE" : "FALSE";
        }

        if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            Object left = evaluate(binary.left);
            Object right = evaluate(binary.right);

            if (binary.operator.type == TokenType.AMPERSAND) {
                return stringify(left) + stringify(right);
            }

            if (left instanceof Integer && right instanceof Integer) {
                int l = (int) left;
                int r = (int) right;
                switch (binary.operator.type) {
                    case PLUS:
                        return l + r;
                    case MINUS:
                        return l - r;
                    case STAR:
                        return l * r;
                    case SLASH:
                        if (r == 0)
                            throw new RuntimeException("Division by zero.");
                        return l / r;
                    case MODULO:
                        if (r == 0)
                            throw new RuntimeException("Modulo by zero.");
                        return l % r;
                    case GREATER:
                        return l > r ? "TRUE" : "FALSE";
                    case GREATER_EQUAL:
                        return l >= r ? "TRUE" : "FALSE";
                    case LESS:
                        return l < r ? "TRUE" : "FALSE";
                    case LESS_EQUAL:
                        return l <= r ? "TRUE" : "FALSE";
                    case EQUAL_EQUAL:
                        return l == r ? "TRUE" : "FALSE";
                    case NOT_EQUAL:
                        return l != r ? "TRUE" : "FALSE";
                }
            } else if (left instanceof Double && right instanceof Double) {
                double l = (double) left;
                double r = (double) right;
                switch (binary.operator.type) {
                    case PLUS:
                        return l + r;
                    case MINUS:
                        return l - r;
                    case STAR:
                        return l * r;
                    case SLASH:
                        if (r == 0.0)
                            throw new RuntimeException("Division by zero.");
                        return l / r;
                    case MODULO:
                        if (r == 0.0)
                            throw new RuntimeException("Modulo by zero.");
                        return l % r;
                    case GREATER:
                        return l > r ? "TRUE" : "FALSE";
                    case GREATER_EQUAL:
                        return l >= r ? "TRUE" : "FALSE";
                    case LESS:
                        return l < r ? "TRUE" : "FALSE";
                    case LESS_EQUAL:
                        return l <= r ? "TRUE" : "FALSE";
                    case EQUAL_EQUAL:
                        return l == r ? "TRUE" : "FALSE";
                    case NOT_EQUAL:
                        return l != r ? "TRUE" : "FALSE";
                }
            } else {
                if (binary.operator.type == TokenType.EQUAL_EQUAL) {
                    return left.equals(right) ? "TRUE" : "FALSE";
                }
                if (binary.operator.type == TokenType.NOT_EQUAL) {
                    return !left.equals(right) ? "TRUE" : "FALSE";
                }
                throw new RuntimeException("Type mismatch: Cannot operate on " + left.getClass().getSimpleName()
                        + " and " + right.getClass().getSimpleName());
            }
        }

        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof String)
            return object.equals("TRUE");
        return false;
    }

    private String stringify(Object object) {
        if (object == null)
            return "null";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object parseInput(String input) {
        if (input.equals("TRUE") || input.equals("FALSE"))
            return input;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
        }
        if (input.length() == 1)
            return input.charAt(0);
        return input;
    }
}