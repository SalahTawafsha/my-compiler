import java.util.Collections;
import java.util.HashSet;

public class MyScanner {
    private final static HashSet<String> terminals;
    private final static HashSet<String> nonTerminals;
    private final static HashSet<String> reservedWords;

    static {
        terminals = initializeSet(".", "module", ";", "begin", "end", "", "const", "var",
                "(", ",", ")", "integer", "real", "char", "procedure", ":=", "+", "-", "*", "/", "mod",
                "div", "readint", "readreal ", "readchar", "readln", "writeint", "writereal",
                "writechar", "writeln", "if", "then", "end", "elseif", "else", "while", "do",
                "loop", "until", "exit", "call", "=", "|=", "<", "<=", ">", ">=",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

        nonTerminals = initializeSet("module-decl", "module-heading",
                "block", "declarations", "const-decl", "const-list", "var-decl", "var-list",
                "var-item", "name-list", "data-type", "procedure-decl", "procedure-heading",
                "stmt-list", "statement", "ass-stmt", "exp", "term", "factor", "add-oper",
                "mul-oper", "read-stmt", "write-stmt", "write-list", "write-item", "if-stmt",
                "elseif-part", "else-part", "while-stmt", "repeat-stmt", "exit-stmt", "call-stmt",
                "condition", "name-value", "relational-oper", "name", "value", "integer-value", "real-value");

        reservedWords = initializeSet("module", "begin", "end", "const",
                "var", "integer", "real", "char", "procedure", "mod", "div",
                "readint", "readreal", "readchar", "readln", "writeint",
                "writereal", "writechar", "writeln", "if", "then", "end",
                "elseif", "else", "while", "do", "loop", "until", "exit", "call");
    }

    private static HashSet<String> initializeSet(String... values) {
        HashSet<String> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
        return set;
    }

    public String nextToken() {
        return "";
    }
}