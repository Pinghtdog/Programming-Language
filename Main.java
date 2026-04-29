import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filePath = "test.txt";

        if (args.length > 0) {
            filePath = args[0];
        }

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String sourceCode = new String(bytes);

            System.out.println("Running file: " + filePath);
            System.out.println("-------------------------------------");

            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);

        } catch (IOException e) {
            System.err.println("Error: Could not read file '" + filePath + "'");
            System.err.println("Make sure the file exists in the same folder.");
        }
    }
}