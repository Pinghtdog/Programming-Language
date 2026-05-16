import java.util.List;

public abstract class Stmt {

    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }
    }

    public static class Declare extends Stmt {
        public final Token dataType;
        public final List<Token> names;
        public final List<Expr> initializers;

        public Declare(Token dataType, List<Token> names, List<Expr> initializers) {
            this.dataType = dataType;
            this.names = names;
            this.initializers = initializers;
        }
    }

    public static class Assign extends Stmt {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    // group of statements in { }
    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    // IF / ELSE IF / ELSE
    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }
}