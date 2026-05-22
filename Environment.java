import java.util.HashMap;
import java.util.Map;

public class Environment {

    final Environment enclosing;

    private final Map<String, Symbol> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    private static class Symbol {
        final TokenType type;
        Object value;

        Symbol(TokenType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    public void define(String name, TokenType type, Object value) {

        values.put(name, new Symbol(type, enforceType(name, type, value)));
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).value;
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeException("Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            Symbol symbol = values.get(name.lexeme);

            symbol.value = enforceType(name.lexeme, symbol.type, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable '" + name.lexeme + "'.");
    }

    private Object enforceType(String name, TokenType declaredType, Object value) {
        if (value == null)
            return null;

        if (declaredType == TokenType.INT) {
            if (value instanceof Integer)
                return value;
            if (value instanceof Double)
                return ((Double) value).intValue();
            throw new RuntimeException("Type Error: Cannot assign " + value.getClass().getSimpleName()
                    + " to INT variable '" + name + "'.");
        } else if (declaredType == TokenType.FLOAT) {
            if (value instanceof Double)
                return value;
            if (value instanceof Integer)
                return ((Integer) value).doubleValue();
            throw new RuntimeException("Type Error: Cannot assign " + value.getClass().getSimpleName()
                    + " to FLOAT variable '" + name + "'.");
        } else if (declaredType == TokenType.BOOL) {
            if (value instanceof String && (value.equals("TRUE") || value.equals("FALSE")))
                return value;
            throw new RuntimeException("Type Error: Cannot assign " + value.getClass().getSimpleName()
                    + " to BOOL variable '" + name + "'.");
        } else if (declaredType == TokenType.CHAR) {
            if (value instanceof Character)
                return value;
            if (value instanceof String && ((String) value).length() == 1)
                return ((String) value).charAt(0);
            throw new RuntimeException("Type Error: Cannot assign " + value.getClass().getSimpleName()
                    + " to CHAR variable '" + name + "'.");
        }
        return value;
    }
}
