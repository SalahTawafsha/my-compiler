// simple Exception class to use in error report
public class ParserException extends RuntimeException {

    public ParserException(int lineNumber, String message) {
        super("Error at line " + lineNumber + ": " + message);
    }

}
