import java.util.List;

public class Main {
    public static void main(String[] args) {
        String sourceCode = "let a = 10;\n" +
                            "let b = 25;\n" +
                            "print \"The answer is:\";\n" +
                            "print a * b;\n";
        
        System.out.println("Source code:\n" + sourceCode);
        System.out.println("-------------------------------------");

        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements); 
    }
}
