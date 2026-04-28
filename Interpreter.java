import java.util.List;
        
public class Interpreter {
    
    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            Object value = evaluate(((Stmt.Print) stmt).expression);
            System.out.println(value); 
        } 
        else if (stmt instanceof Stmt.Expression) {
            evaluate(((Stmt.Expression) stmt).expression);
        }
        else if (stmt instanceof Stmt.Var) {
            Object value = null;
            if (((Stmt.Var) stmt).initializer != null) {
                value = evaluate(((Stmt.Var) stmt).initializer);
            }
            environment.define(((Stmt.Var) stmt).name.lexeme, value);
        }
    }

    public Object evaluate(Expr expr) {
        if (expr == null) return null;

        if (expr instanceof Expr.Literal) return ((Expr.Literal) expr).value;
        
        if (expr instanceof Expr.Grouping) return evaluate(((Expr.Grouping) expr).expression);
        
        if (expr instanceof Expr.Variable) {
            return environment.get(((Expr.Variable) expr).name);
        }

        if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;
            Object right = evaluate(unary.right);
            switch (unary.operator.type) {
                case BANG: return !isTruthy(right);
                case MINUS: return -(double) right;
            }
        }

        if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            Object left = evaluate(binary.left);
            Object right = evaluate(binary.right);

            switch (binary.operator.type) {
                case MINUS: return (double) left - (double) right;
                case SLASH: return (double) left / (double) right;
                case STAR:  return (double) left * (double) right;
                case PLUS:
                    if (left instanceof Double && right instanceof Double) return (double) left + (double) right;
                    if (left instanceof String && right instanceof String) return (String) left + (String) right;
                    throw new RuntimeException("Operands must be two numbers or two strings.");
                case GREATER:       return (double) left > (double) right;
                case GREATER_EQUAL: return (double) left >= (double) right;
                case LESS:          return (double) left < (double) right;
                case LESS_EQUAL:    return (double) left <= (double) right;
                case BANG_EQUAL:    return !isEqual(left, right);
                case EQUAL_EQUAL:   return isEqual(left, right);
            }
        }

        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }
}
