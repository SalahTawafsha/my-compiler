import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;

public class CompilerParser {
    private final CompilerScanner scanner;
    private String currentToken = "";
    private final static HashSet<String> reservedWords = new HashSet<>();

    static {
        String[] reservedWords = {"module", "begin", "end", "const",
                "var", "integer", "real", "char", "procedure", "mod", "div",
                "readint", "readreal", "readchar", "readln", "writeint",
                "writereal", "writechar", "writeln", "if", "then", "end",
                "elseif", "else", "while", "do", "loop", "until", "exit", "call"};

        Collections.addAll(CompilerParser.reservedWords, reservedWords);
    }

    private String moduleName;
    private String procedureName;


    public CompilerParser(File input) {
        scanner = new CompilerScanner(input);
    }

    public void parse() throws ParserConfigurationException {
        moduleHeading();
        declarations(true);
        procedureDecl();

        block();
        moduleName();

        if (!currentToken.equals("."))
            throw new ParserConfigurationException("File Must end with \".\", on line " + scanner.getTokenLine());

        getNextToken();
        if (!currentToken.equals("EOF"))
            throw new ParserConfigurationException("File Must end on \".\" and nothing after, (on line " + scanner.getTokenLine() + ")");

    }

    private void moduleHeading() throws ParserConfigurationException {
        getNextToken();

        if (!currentToken.equals("module"))
            throw new ParserConfigurationException("File Must start with \"module\", on line " + scanner.getTokenLine());

        getNextToken();
        validateName();

        moduleName = currentToken;

        getNextToken();
        validateSimiColon();
    }

    private void validateSimiColon() throws ParserConfigurationException {
        if (!currentToken.equals(";"))
            throw new ParserConfigurationException("\";\" is expected in end of line " + scanner.getTokenLine());
    }

    private void validateName() throws ParserConfigurationException {
        // validate if token is available name
        if (!Character.isLetter(currentToken.charAt(0)))
            throw new ParserConfigurationException("Naming must start with char and you are using \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

        // validate if name is reserved word
        if (reservedWords.contains(currentToken))
            throw new ParserConfigurationException("You are using a reserved word \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

    }

    // if isModuleDeclarations is true so its declarations of module, otherwise its for procedure
    private void declarations(boolean isModuleDeclarations) throws ParserConfigurationException {
        getNextToken();

        constDeclarations(isModuleDeclarations);
        varDeclarations(isModuleDeclarations);

    }


    private void constDeclarations(boolean isModuleDeclarations) throws ParserConfigurationException {
        if (currentToken.equals("const")) {
            getNextToken();
            constList(isModuleDeclarations);
        }

        // since const-decl can be lambda so, we must check if token is follow(const-decl) that is var
        // and since var-decl can be lambda so, we must check if token is follow(var-decl) that is procedure
        // this if statement will check if no one of them and report error if no one
        if (isModuleDeclarations && !currentToken.equals("var") && !currentToken.equals("procedure"))
            throw new ParserConfigurationException("You must have procedure declaration that started with \"procedure\", (on line " + scanner.getTokenLine() + ")");

        if (!isModuleDeclarations && !currentToken.equals("var") && !currentToken.equals("begin"))
            throw new ParserConfigurationException("You must have block declaration that started with \"begin\", (on line " + scanner.getTokenLine() + ")");


    }

    private void constList(boolean isModuleDeclarations) throws ParserConfigurationException {
        validateConstItem();// validate name = value

        if (isModuleDeclarations) {
            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("procedure")) {
                validateConstItem();
            }
        } else {
            while (!currentToken.equals("var") && !currentToken.equals("begin")) {
                validateConstItem();
            }
        }
    }

    private void validateConstItem() throws ParserConfigurationException {
        validateName();

        getNextToken();
        if (!currentToken.equals("="))
            throw new ParserConfigurationException("When you are declaring const item," +
                    " you must use '=' between name and value, on line " + scanner.getTokenLine());

        getNextToken();
        if (!Character.isDigit(currentToken.charAt(0))) // so its not valid value
            throw new ParserConfigurationException("When you are declaring const item," +
                    " value must be integer or real, on line " + scanner.getTokenLine());

        getNextToken();
        validateSimiColon();

        getNextToken();


    }

    private void varDeclarations(boolean isModuleDeclarations) throws ParserConfigurationException {
        if (currentToken.equals("var")) {
            getNextToken();
            varList(isModuleDeclarations);
        }

        // since var-decl can be lambda so, we must check if token is follow(var-decl) that is procedure
        // this if statement will check that and report error if not
        if (isModuleDeclarations && !currentToken.equals("procedure"))
            throw new ParserConfigurationException("You must have procedure declaration that started with \"procedure\", (on line " + scanner.getTokenLine() + ")");

        if (!isModuleDeclarations && !currentToken.equals("begin"))
            throw new ParserConfigurationException("You must have block declaration that started with \"begin\", (on line " + scanner.getTokenLine() + ")");

    }

    private void varList(boolean isModuleDeclarations) throws ParserConfigurationException {
        validateVarItem();// validate name-list : value
        getNextToken();
        validateSimiColon();
        getNextToken();

        if (isModuleDeclarations) {
            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("procedure")) {
                validateVarItem();
                getNextToken();
                validateSimiColon();
                getNextToken();
            }
        } else {
            while (!currentToken.equals("var") && !currentToken.equals("begin")) {
                validateVarItem();
                getNextToken();
                validateSimiColon();
                getNextToken();
            }
        }

    }

    private void validateVarItem() throws ParserConfigurationException {
        validateNameList();

        if (currentToken.charAt(0) != ':')
            throw new ParserConfigurationException("When you are declaring var item" +
                    " you must use ':' between names and value, on line " + scanner.getTokenLine());

        getNextToken();
        validateDataType();
    }

    private void validateDataType() throws ParserConfigurationException {
        if (!currentToken.equals("integer") && !currentToken.equals("real") && !currentToken.equals("char"))
            throw new ParserConfigurationException("You must select data type that is (integer or real or char)," +
                    " on line " + scanner.getTokenLine());

    }

    private void validateNameList() throws ParserConfigurationException {
        validateName();
        getNextToken();

        while (currentToken.charAt(0) == ',') {
            getNextToken();
            validateName();
            getNextToken();
        }
    }

    private void procedureDecl() throws ParserConfigurationException {
        procedureHeading();
        declarations(false);
        block();
        procedureName();
        validateSimiColon();
    }

    private void procedureHeading() throws ParserConfigurationException {
        if (!currentToken.equals("procedure"))
            throw new ParserConfigurationException("Procedure heading must start with \"procedure\", on line " + scanner.getTokenLine());

        getNextToken();
        validateName();

        procedureName = currentToken;

        getNextToken();
        validateSimiColon();

    }

    private void block() throws ParserConfigurationException {
        if (!currentToken.equals("begin"))
            throw new ParserConfigurationException("you must make block and it must started with \"begin\", on line " + scanner.getTokenLine());

        stmtList();

        getNextToken();
        if (!currentToken.equals("begin"))
            throw new ParserConfigurationException("you must end block here using \"end\", on line " + scanner.getTokenLine());

    }

    private void stmtList() {
        // ToDo: implement stmtList
    }

    private void procedureName() throws ParserConfigurationException {
        if (!currentToken.equals(procedureName))
            throw new ParserConfigurationException("When you ending procedure," +
                    " you must use name that you entered in procedure-heading that is \"" + procedureName + "\""
                    + ", on line " + currentToken + " You are using end " + currentToken);

    }

    private void moduleName() throws ParserConfigurationException {
        if (!currentToken.equals(moduleName))
            throw new ParserConfigurationException("When you ending program," +
                    " you must use name that you entered in module-heading that is \"" + moduleName + "\""
                    + ", on line " + currentToken + " You are using end " + currentToken);

    }


    public void getNextToken() {
        currentToken = scanner.nextToken();
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
