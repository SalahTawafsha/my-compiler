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

/*
".",
"module",
";",
"begin",
"end",
"",
"const",
"var",
"(",
",",
")",
"integer",
"real",
"char",
"procedure",
":=",
"+",
"-",
"*",
"/",
"mod",
"div",
"readint",
"readreal ",
"readchar",
"readln",
"writeint",
"writereal",
"writechar",
"writeln",
"if",
"then",
"end",
"elseif",
"else",
"while",
"do",
"loop",
"until",
"exit",
"call",
"=",
"|=",
"<",
"<=",
">",
">=",
 */
