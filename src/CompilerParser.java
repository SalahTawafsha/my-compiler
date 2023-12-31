// parser using Recursive Descent Parsing

// note that methods is sorted as project file
// expect write-stmt since it's same of name so both in same function "name"

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

public class CompilerParser {
    private final CompilerScanner scanner;
    private String currentToken = ""; // token that current in checking

    // create hashset that has reserved words and implement it
    private final static HashSet<String> reservedWords = new HashSet<>();

    static {
        String[] reservedWords = {"module", "begin", "end", "const",
                "var", "integer", "real", "char", "procedure", "mod", "div",
                "readint", "readreal", "readchar", "readln", "writeint",
                "writereal", "writechar", "writeln", "if", "then", "end",
                "elseif", "else", "while", "do", "loop", "until", "exit", "call"};

        Collections.addAll(CompilerParser.reservedWords, reservedWords);
    }

    // store module name to validate with end of module
    private String moduleName;

    // store procedure name to validate with end of procedure
    private String procedureName;

    public CompilerParser(File input) {
        // pass file in constructor to parse it
        scanner = new CompilerScanner(input);
    }


    public void parse() throws ParserException {
        // method that is public and will parse the file and all other methods is private
        // ParserException will be the exception that I will use to throw errors

        // call methods of module-decl that are (module-heading, declarations, procedure-decl, block, name, and .)
        moduleHeading();
        declarations(true);
        procedureDecl();
        block();
        moduleName();
        getNextToken();

        if (!currentToken.equals("."))
            throw new ParserException("File Must end with \".\", on line " + scanner.getTokenLine());

        getNextToken();
        if (!currentToken.equals("EOF"))
            throw new ParserException("File Must end on \".\" and nothing after, (on line " + scanner.getTokenLine() + ")");

    }

    private void moduleHeading() throws ParserException {
        // validate "module name ;"
        getNextToken();

        if (!currentToken.equals("module"))
            throw new ParserException("File Must start with \"module\", on line " + scanner.getTokenLine());

        getNextToken();
        name();

        moduleName = currentToken; // store module name to validate with end of program

        getNextToken();
        simiColon();
    }

    private void block() throws ParserException {
        if (!currentToken.equals("begin"))
            throw new ParserException("you must make block and it must started with \"begin\", on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

        if (!currentToken.equals("end"))
            throw new ParserException("you must end block here using \"end\", on line " + scanner.getTokenLine());
        getNextToken();
    }

    private void simiColon() throws ParserException {
        if (!currentToken.equals(";"))
            throw new ParserException("\";\" is expected in end of line " + scanner.getTokenLine());
    }

    private void declarations(boolean isModuleDeclarations) throws ParserException {
        // method will check const and var declarations
        // if isModuleDeclarations is true so its declarations of module, otherwise its for procedure
        getNextToken();

        constDeclarations(isModuleDeclarations);
        varDeclarations(isModuleDeclarations);

    }

    private void constDeclarations(boolean isModuleDeclarations) throws ParserException {
        if (currentToken.equals("const")) {
            getNextToken();
            constList(isModuleDeclarations);
        }

        // this when validation is for module declarations
        // since const-decl can be lambda so, we must check if token is follow(const-decl) that is "var"
        // and since var-decl can be lambda so, we must check if token is follow(var-decl) that is "procedure"
        // this if statement will check if no one of them and report error if no one
        if (isModuleDeclarations && !currentToken.equals("var") && !currentToken.equals("procedure"))
            throw new ParserException("You must have procedure declaration that started with \"procedure\", (on line " + scanner.getTokenLine() + ")");

        // this when validation is for procedure declarations
        // since const-decl can be lambda so, we must check if token is follow(const-decl) that is "var"
        // and since var-decl can be lambda so, we must check if token is follow(var-decl) that is "begin"
        // this if statement will check if no one of them and report error if no one
        if (!isModuleDeclarations && !currentToken.equals("var") && !currentToken.equals("begin"))
            throw new ParserException("You must have block declaration that started with \"begin\", (on line " + scanner.getTokenLine() + ")");


    }

    private void constList(boolean isModuleDeclarations) throws ParserException {
        constItem();// validate "name = value"

        if (isModuleDeclarations) {
            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("procedure")) {
                constItem();
            }
        } else {
            while (!currentToken.equals("var") && !currentToken.equals("begin")) {
                constItem();
            }
        }
    }

    private void constItem() throws ParserException {
        // validate "name = value"
        name();

        getNextToken();
        if (!currentToken.equals("="))
            throw new ParserException("When you are declaring const item," +
                    " you must use '=' between name and value, on line " + scanner.getTokenLine());

        getNextToken();
        if (!Character.isDigit(currentToken.charAt(0))) // so its not valid value
            throw new ParserException("When you are declaring const item," +
                    " value must be integer or real, on line " + scanner.getTokenLine());

        getNextToken();
        simiColon();

        getNextToken();


    }

    private void varDeclarations(boolean isModuleDeclarations) throws ParserException {
        if (currentToken.equals("var")) {
            getNextToken();
            varList(isModuleDeclarations);
        }

        // this when validation is for module declarations
        // since var-decl can be lambda so, we must check if token is follow(var-decl) that is "procedure"
        // this if statement will check that and report error if not
        if (isModuleDeclarations && !currentToken.equals("procedure"))
            throw new ParserException("You must have procedure declaration that started with \"procedure\", (on line " + scanner.getTokenLine() + ")");

        // this when validation is for procedure declarations
        // since var-decl can be lambda so, we must check if token is follow(var-decl) that is "begin"
        // this if statement will check that and report error if not
        if (!isModuleDeclarations && !currentToken.equals("begin"))
            throw new ParserException("You must have block declaration that started with \"begin\", (on line " + scanner.getTokenLine() + ")");

    }

    private void varList(boolean isModuleDeclarations) throws ParserException {
        varItem();// validate name-list : value
        getNextToken();
        simiColon();
        getNextToken();

        if (isModuleDeclarations) {
            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("procedure")) {
                varItem();
                getNextToken();
                simiColon();
                getNextToken();
            }
        } else {
            while (!currentToken.equals("var") && !currentToken.equals("begin")) {
                varItem();
                getNextToken();
                simiColon();
                getNextToken();
            }
        }

    }

    private void varItem() throws ParserException {
        nameList();

        if (currentToken.charAt(0) != ':')
            throw new ParserException("When you are declaring var item" +
                    " you must use ':' between names and value, on line " + scanner.getTokenLine());

        getNextToken();
        dataType();
    }

    private void nameList() throws ParserException {
        name();
        getNextToken();

        while (currentToken.equals(",")) {
            getNextToken();
            name();
            getNextToken();
        }
    }

    private void dataType() throws ParserException {
        if (!currentToken.equals("integer") && !currentToken.equals("real") && !currentToken.equals("char"))
            throw new ParserException("You must select data type that is (integer or real or char)," +
                    " on line " + scanner.getTokenLine());

    }

    private void procedureDecl() throws ParserException {
        procedureHeading();
        declarations(false);
        block();
        procedureName();
        getNextToken();
        simiColon();
        getNextToken();
    }

    private void procedureHeading() throws ParserException {
        if (!currentToken.equals("procedure"))
            throw new ParserException("Procedure heading must start with \"procedure\", on line " + scanner.getTokenLine());

        getNextToken();
        name();

        procedureName = currentToken;

        getNextToken();
        simiColon();

    }

    private void procedureName() throws ParserException {
        if (!currentToken.equals(procedureName))
            throw new ParserException("When you ending procedure," +
                    " you must use name that you entered in procedure-heading that is \"" + procedureName + "\""
                    + ", and on line " + scanner.getTokenLine() + " You are using " + currentToken);

    }

    private void stmtList() throws ParserException {
        statement();
        while (currentToken.equals(";")) {
            getNextToken();
            statement();
        }
    }

    private void statement() throws ParserException {
        if (isFirstOfReadStmt()) {
            readStatement();
            getNextToken();
        } else if (isFirstOfWriteStmt()) {
            writeStatement();
            getNextToken();
        } else if (isFirstOfIfStmt()) {
            ifStatement();
            getNextToken();
        } else if (isFirstOfWhileStmt()) {
            whileStatement();
            getNextToken();
        } else if (isFirstOfRepeatStmt()) {
            RepeatStatement();
            getNextToken();
        } else if (currentToken.equals("exit")) {
            getNextToken();
        } else if (currentToken.equals("call")) {
            call();
            getNextToken();
        } else if (Character.isLetter(currentToken.charAt(0)) && !reservedWords.contains(currentToken)) {
            assignStatement();
            getNextToken();
        }

        if (!currentToken.equals(";") && !currentToken.equals("elseif") && !currentToken.equals("else") && !currentToken.equals("end"))
            throw new ParserException("this is not valid statement, on line " + scanner.getTokenLine());

    }

    private void assignStatement() throws ParserException {
        name();
        getNextToken();
        if (!currentToken.equals(":="))
            throw new ParserException("You must use := in assignment statement, on line " + scanner.getTokenLine());


        getNextToken();
        exp();
    }

    private void exp() throws ParserException {
        term();
        getNextToken();

        while (isAddOperation()) {
            getNextToken();
            term();
        }
    }

    private void term() throws ParserException {
        factor();
        getNextToken();

        while (isMultiplyOperation()) {
            getNextToken();
            factor();
        }

    }

    private void factor() throws ParserException {
        if (currentToken.equals("(")) {
            getNextToken();
            exp();

            if (!currentToken.equals(")"))
                throw new ParserException("You must close bracket in this statement, on line " + scanner.getTokenLine());

            getNextToken();
        } else if (Character.isDigit(currentToken.charAt(0))) {
            getNextToken();
        } else {
            name();
            getNextToken();
        }
    }

    private boolean isAddOperation() {
        return currentToken.equals("+") || currentToken.equals("-");
    }

    private boolean isMultiplyOperation() {
        return currentToken.equals("*") || currentToken.equals("/") || currentToken.equals("mod") || currentToken.equals("div");
    }

    private boolean isFirstOfReadStmt() {
        return currentToken.equals("readint") || currentToken.equals("readreal") || currentToken.equals("readchar") ||
                currentToken.equals("readln");
    }

    private void readStatement() throws ParserException {
        if (currentToken.equals("readln"))
            return;

        getNextToken();
        if (!currentToken.equals("("))
            throw new ParserException("This is read statement and you must add '(' before names, on line " + scanner.getTokenLine());

        getNextToken();
        nameList();

        if (!currentToken.equals(")"))
            throw new ParserException("This is read statement and you must add ')' after names, on line " + scanner.getTokenLine());

    }

    private boolean isFirstOfWriteStmt() {
        return currentToken.equals("writeint") || currentToken.equals("writereal") || currentToken.equals("writechar") ||
                currentToken.equals("writeln");
    }

    private void writeStatement() throws ParserException {
        if (currentToken.equals("writeln"))
            return;

        getNextToken();
        if (!currentToken.equals("("))
            throw new ParserException("This is read statement and you must add '(' before names, on line " + scanner.getTokenLine());

        getNextToken();
        writeList();

        if (!currentToken.equals(")"))
            throw new ParserException("This is read statement and you must add ')' after names, on line " + scanner.getTokenLine());


    }

    private void writeList() throws ParserException {
        writeItem();// validate name = value
        getNextToken();

        // while token is a name validate const items
        while (currentToken.equals(",")) {
            writeItem();
            getNextToken();
        }

    }

    private void writeItem() throws ParserException {
        if (Character.isDigit(currentToken.charAt(0))) // so its not valid value
            return;

        name();
    }

    private boolean isFirstOfIfStmt() {
        return currentToken.equals("if");
    }

    private void ifStatement() throws ParserException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("then"))
            throw new ParserException("You must have \"then\" after condition of if, on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

        while (currentToken.equals("elseif"))
            elseIfPart();

        if (currentToken.equals("else"))
            elsePart();

        if (!currentToken.equals("end"))
            throw new ParserException("You must have one of elseif, else or end after statements, on line " + scanner.getTokenLine());
    }

    private void elseIfPart() throws ParserException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("then"))
            throw new ParserException("You must have \"then\" after condition of else if, on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

    }

    private void elsePart() throws ParserException {
        getNextToken();
        stmtList();
    }

    private boolean isFirstOfWhileStmt() {
        return currentToken.equals("while");
    }

    private void whileStatement() throws ParserException {
        getNextToken();
        condition();

        getNextToken();
        if (!currentToken.equals("do"))
            throw new ParserException("You must have \"do\" after condition of while, on line " + scanner.getTokenLine());

        getNextToken();
        stmtList();

        if (!currentToken.equals("end"))
            throw new ParserException("You must have \"end\" of while after statements, on line " + scanner.getTokenLine());

    }

    private boolean isFirstOfRepeatStmt() {
        return currentToken.equals("loop");
    }

    private void RepeatStatement() throws ParserException {
        getNextToken();
        stmtList();

        if (!currentToken.equals("until"))
            throw new ParserException("You must have \"until\" after statements of repeat statement, on line " + scanner.getTokenLine());

        getNextToken();
        condition();

    }

    private void call() throws ParserException {
        getNextToken();
        name();
    }

    private void condition() throws ParserException {
        writeItem();

        getNextToken();
        relationOperation();

        getNextToken();
        writeItem();

    }

    private void name() throws ParserException {
        // validate if token is available name
        if (!Character.isLetter(currentToken.charAt(0)))
            throw new ParserException("Naming must start with char and you are using \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

        // validate if name is reserved word
        if (reservedWords.contains(currentToken))
            throw new ParserException("You are using a reserved word \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

    }

    private void relationOperation() throws ParserException {
        if (!currentToken.equals("=") && !currentToken.equals("|=") && !currentToken.equals("<") &&
                !currentToken.equals("<=") && !currentToken.equals(">") && !currentToken.equals(">="))
            throw new ParserException("You must has valid operation here (=, |=, <, <=, >, >=) on line " + scanner.getTokenLine());

    }


    private void moduleName() throws ParserException {
        if (!currentToken.equals(moduleName))
            throw new ParserException("When you ending module," +
                    " you must use name that you entered in module-heading that is \"" + moduleName + "\""
                    + ", on line " + scanner.getTokenLine() + " You are using end " + currentToken);

    }


    private void getNextToken() {
        currentToken = scanner.nextToken();
    }
}