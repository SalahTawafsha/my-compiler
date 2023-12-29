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

        while (currentToken.equals(",")) {
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

        getNextToken();
        stmtList();

        if (!currentToken.equals("end"))
            throw new ParserConfigurationException("you must end block here using \"end\", on line " + scanner.getTokenLine());

    }

    private void stmtList() throws ParserConfigurationException {
        validateStatement();

        while (currentToken.equals(";")) {
            getNextToken();
            validateStatement();
            getNextToken();
        }
    }

    private void validateStatement() throws ParserConfigurationException {

        if (isFirstOfReadStmt()) {
            readStatement();
            getNextToken();
        }

        if (isFirstOfWriteStmt()) {
            writeStatement();
            getNextToken();
        }
        if (isFirstOfIfStmt()) {
            ifStatement();
            getNextToken();
        }

        if (isFirstOfWhileStmt()) {
            whileStatement();
            getNextToken();
        }

        if (isFirstOfRepeatStmt()) {
            RepeatStatement();
            getNextToken();
        }

        if (currentToken.equals("exit")) {
            getNextToken();
        }

        if (currentToken.equals("call")) {
            getNextToken();
            validateName();
            getNextToken();
        }


        if (Character.isLetter(currentToken.charAt(0))) {
            getNextToken();
            assignStatement();
            getNextToken();
        }

        if (!currentToken.equals(";") && !currentToken.equals("elseif") && !currentToken.equals("else") && !currentToken.equals("end"))
            throw new ParserConfigurationException("this is not valid statement, on line " + scanner.getTokenLine());

    }

    private boolean isFirstOfRepeatStmt() {
        return currentToken.equals("loop");
    }

    private void RepeatStatement() throws ParserConfigurationException {
        getNextToken();
        stmtList();

        if (!currentToken.equals("until"))
            throw new ParserConfigurationException("You must have \"until\" after statements of repeat statement, on line " + scanner.getTokenLine());

        getNextToken();
        condition();

    }

    private void whileStatement() throws ParserConfigurationException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("do"))
            throw new ParserConfigurationException("You must have \"do\" after condition of while, on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

        if (!currentToken.equals("end"))
            throw new ParserConfigurationException("You must have \"end\" of while after statements, on line " + scanner.getTokenLine());

    }

    private boolean isFirstOfWhileStmt() {
        return currentToken.equals("while");
    }

    private boolean isFirstOfIfStmt() {
        return currentToken.equals("if");
    }

    private void ifStatement() throws ParserConfigurationException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("then"))
            throw new ParserConfigurationException("You must have \"then\" after condition of if, on line " + scanner.getTokenLine());

        stmtList();

        while (currentToken.equals("elseif"))
            elseIfPart();

        if (currentToken.equals("else"))
            elsePart();

        if (!currentToken.equals("end"))
            throw new ParserConfigurationException("You must have one of elseif, else or end after statements, on line " + scanner.getTokenLine());
    }


    private void elseIfPart() throws ParserConfigurationException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("then"))
            throw new ParserConfigurationException("You must have \"then\" after condition of else if, on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

    }

    private void elsePart() throws ParserConfigurationException {
        getNextToken();
        stmtList();
    }

    private void condition() throws ParserConfigurationException {
        validateWriteItem();

        getNextToken();
        validateRelationOperation();

        getNextToken();
        validateWriteItem();

    }

    private void validateRelationOperation() throws ParserConfigurationException {
        if (!currentToken.equals("=") && !currentToken.equals("|=") && !currentToken.equals("<") &&
                !currentToken.equals("<=") && !currentToken.equals(">") && !currentToken.equals(">="))
            throw new ParserConfigurationException("You must has valid operation here (=, |=, <, <=, >, >=) on line " + scanner.getTokenLine());

    }

    private boolean isFirstOfWriteStmt() {
        return currentToken.equals("writeint") || currentToken.equals("writereal") || currentToken.equals("writechar") ||
                currentToken.equals("writeln");
    }

    private void writeStatement() throws ParserConfigurationException {
        if (currentToken.equals("writeln"))
            return;

        getNextToken();
        if (!currentToken.equals("("))
            throw new ParserConfigurationException("This is read statement and you must add '(' before names, on line " + scanner.getTokenLine());

        getNextToken();
        validateWriteList();

        if (!currentToken.equals(")"))
            throw new ParserConfigurationException("This is read statement and you must add ')' after names, on line " + scanner.getTokenLine());


    }

    private void validateWriteList() throws ParserConfigurationException {
        validateWriteItem();// validate name = value
        getNextToken();

        // while token is a name validate const items
        while (currentToken.equals(",")) {
            validateWriteItem();
            getNextToken();
        }

    }

    private void validateWriteItem() throws ParserConfigurationException {
        if (Character.isDigit(currentToken.charAt(0))) // so its not valid value
            return;

        validateName();
    }


    private boolean isFirstOfReadStmt() {
        return currentToken.equals("readint") || currentToken.equals("readreal") || currentToken.equals("readchar") ||
                currentToken.equals("readln");
    }

    private void readStatement() throws ParserConfigurationException {
        if (currentToken.equals("readln"))
            return;

        getNextToken();
        if (!currentToken.equals("("))
            throw new ParserConfigurationException("This is read statement and you must add '(' before names, on line " + scanner.getTokenLine());

        getNextToken();
        validateNameList();

        if (!currentToken.equals(")"))
            throw new ParserConfigurationException("This is read statement and you must add ')' after names, on line " + scanner.getTokenLine());

    }

    private void assignStatement() {
        // ToDo: implement me please (' =
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
