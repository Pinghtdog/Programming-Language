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

    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }
    }

    // group statements in { }
    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    // if elif else
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

    // repeat when (do while)
    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
    }

    // for loop
    public static class For extends Stmt {
        public final Stmt initialization;
        public final Expr condition;
        public final Stmt increment;
        public final Stmt body;

        public For(Stmt initialization, Expr condition, Stmt increment, Stmt body) {
            this.initialization = initialization;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }
    }

    // scanner
    public static class Scan extends Stmt {
        public final List<Token> names;

        public Scan(List<Token> names) {
            this.names = names;
        }
    }
}