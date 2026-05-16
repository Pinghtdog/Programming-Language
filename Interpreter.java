import java.util.List;

public class Interpreter {

    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        if (statements == null)
            return;
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            Object value = evaluate(((Stmt.Print) stmt).expression);
            System.out.println(value);
        } else if (stmt instanceof Stmt.Declare) {
            Stmt.Declare decl = (Stmt.Declare) stmt;
            for (int i = 0; i < decl.names.size(); i++) {
                Token name = decl.names.get(i);
                Expr initializer = decl.initializers.get(i);

                Object value = null;
                if (initializer != null) {
                    value = evaluate(initializer);
                }
                environment.define(name.lexeme, value);
            }
        } else if (stmt instanceof Stmt.Assign) {
            Stmt.Assign assign = (Stmt.Assign) stmt;
            Object value = evaluate(assign.value);
            environment.assign(assign.name, value);
        } else if (stmt instanceof Stmt.Block) {
            Stmt.Block block = (Stmt.Block) stmt;
            for (Stmt statement : block.statements) {
                execute(statement);
            }
        } else if (stmt instanceof Stmt.If) {
            Stmt.If ifStmt = (Stmt.If) stmt;
            Object condition = evaluate(ifStmt.condition);

            if (isTruthy(condition)) {
                execute(ifStmt.thenBranch);
            } else if (ifStmt.elseBranch != null) {
                execute(ifStmt.elseBranch);
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
        if (expr instanceof Expr.Grouping) {
            return evaluate(((Expr.Grouping) expr).expression);
        }

        // --- UNARY (Negative, Positive, NOT) ---
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

        // --- LOGICAL (AND, OR) ---
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

        // --- BINARY (Math & Comparisons) ---
        if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            Object left = evaluate(binary.left);
            Object right = evaluate(binary.right);

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

    // --- HELPER ---
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof String)
            return object.equals("TRUE");
        return false;
    }
}