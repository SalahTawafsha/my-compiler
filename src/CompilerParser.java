import java.io.File;

public class CompilerParser {
    private final CompilerScanner scanner;
    private String currentToken = "";

    public CompilerParser(File input) {
        scanner = new CompilerScanner(input);
    }

    public String parse() {
        currentToken = scanner.nextToken();
        return "success";
    }
}
