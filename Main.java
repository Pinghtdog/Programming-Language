import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filePath = "script.txt";

        if (args.length > 0) {
            filePath = args[0];
        }

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String sourceCode = new String(bytes);

            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            // if (statements != null) {
            // System.out.println("Success");
            // }

            if (statements != null) {

                Interpreter interpreter = new Interpreter();
                interpreter.interpret(statements);
            }

        } catch (IOException e) {
            System.err.println("Error: Could not read file '" + filePath + "'");
        }
    }
}