// parser using Recursive Descent Parsing

// note that methods is sorted as project file
// expect write-stmt since it's same of name-value so both in same method "nameValue"
// and exit hasn't method since it's just a word
// value, integer-value and real-value also hasn't methods since they will return by the scanner

import java.io.File;
import java.io.FileNotFoundException;
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
    private String moduleName = "";

    // store procedure name to validate with end of procedure
    private String procedureName = "";

    public CompilerParser(File input) throws FileNotFoundException {
        // pass file in constructor to parse it
        scanner = new CompilerScanner(input);
    }


    public void parse() throws ParserException {
        // this is method of module-decl (main)
        // method that is public and will parse the file and all other methods is private
        // ParserException will be the exception that I will use to throw errors

        getNextToken(); // get first token

        // call methods of module-decl that are (module-heading, declarations, procedure-decl, block, name, and .)

        // validate "module name ;"
        moduleHeading();
        // validate "const-decl var-decl"
        declarations(true); // "true" since its module declarations
        // validate "procedure-heading declarations block name ;"
        procedureDecl();
        // validate "begin stmt-list end"
        block();
        // validate that name in end of module is same of module heading
        moduleName();

        // finally, validate has "."
        if (!currentToken.equals("."))
            throw new ParserException("File Must end with \".\", on line " + scanner.getTokenLine());

        // validate if "." is last token
        // here I added EOF to production rules to can get follow of module-decl
        getNextToken();
        if (!currentToken.equals("EOF"))
            throw new ParserException("File Must end on \".\" and nothing after, (on line " + scanner.getTokenLine() + ")");

    }

    private void moduleHeading() throws ParserException {
        // validate "module name ;"

        if (!currentToken.equals("module"))
            throw new ParserException("File Must start with \"module\", on line " + scanner.getTokenLine());
        getNextToken();

        name();
        // store module name to validate with end of program
        moduleName = currentToken;
        getNextToken();

        simiColon();
    }

    private void block() throws ParserException {
        // validate "begin stmt-list end"

        if (!currentToken.equals("begin"))
            throw new ParserException("you must make block and it must started with \"begin\", on line " + scanner.getTokenLine());

        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();

        if (!currentToken.equals("end"))
            throw new ParserException("you must end block here using \"end\", on line " + scanner.getTokenLine());
        getNextToken();
    }

    private void simiColon() throws ParserException {
        // validate ";"
        if (!currentToken.equals(";"))
            throw new ParserException("End of statement reached and must use \";\" in line " + scanner.getTokenLine() + " before \"" + currentToken + "\"");

        getNextToken();
    }

    private void declarations(boolean isModuleDeclarations) throws ParserException {
        // validate "const-decl var-decl"
        // and I added isModuleDeclarations boolean for:
        // if true so its declarations of module, otherwise its for procedure
        // this will help to know if follow of declarations is ("procedure" || "begin")

        // validate "const const-list | lambda"
        constDeclarations(isModuleDeclarations);


        // validate "var var-list | lambda"
        varDeclarations(isModuleDeclarations);

    }

    private void constDeclarations(boolean isModuleDeclarations) throws ParserException {
        // validate "const const-list | lambda"

        // if token is const so call const-list, otherwise its lambda
        if (currentToken.equals("const")) {
            getNextToken();
            // validate "( name = value ; )*"
            constList(isModuleDeclarations);
        }
    }

    private void constList(boolean isModuleDeclarations) throws ParserException {
        // validate "( name = value ; )*"


        // validate "name = value;"
        constItem();

        if (isModuleDeclarations) { // if this is module declaration so loop until find var or procedure

            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("procedure"))
                constItem(); // validate "name = value;"

        } else { // else, it's procedure declaration so loop until find var or begin

            // while token is a name validate const items
            while (!currentToken.equals("var") && !currentToken.equals("begin"))
                constItem(); // validate "name = value;"
        }
    }

    private void constItem() throws ParserException {
        // validate "name = value;"

        name();

        getNextToken();
        if (!currentToken.equals("="))
            throw new ParserException("When you are declaring const item," +
                    " you must use '=' between name and value, on line " + scanner.getTokenLine());

        getNextToken();
        // value is number so must start with digit
        if (!Character.isDigit(currentToken.charAt(0)))
            throw new ParserException("When you are declaring const item," +
                    " value must be integer or real, on line " + scanner.getTokenLine());

        getNextToken();
        simiColon();

    }

    private void varDeclarations(boolean isModuleDeclarations) throws ParserException {
        // validate "var var-list | lambda"

        // if token is var so call var-list, otherwise its lambda
        if (currentToken.equals("var")) {
            getNextToken();
            // validate "( var-item ; )*"
            varList(isModuleDeclarations);
        }
    }

    private void varList(boolean isModuleDeclarations) throws ParserException {
        // validate "( var-item ; )*"

        // validate name-list : data-type
        varItem();
        // validate ";"
        simiColon();

        if (isModuleDeclarations) { // if this is module declaration so loop until find procedure
            // while token is a name validate const items
            while (!currentToken.equals("procedure")) {
                // validate name-list : data-type
                varItem();
                // validate ";"
                simiColon();
            }
        } else { // otherwise, it's procedure declaration so loop until find begin
            while (!currentToken.equals("begin")) {
                // validate name-list : data-type
                varItem();
                // validate ";"
                simiColon();
            }
        }

    }

    private void varItem() throws ParserException {
        // validate name-list : data-type

        nameList();

        if (!currentToken.equals(":"))
            throw new ParserException("When you are declaring var item" +
                    " you must use ':' between names and value, on line " + scanner.getTokenLine());

        getNextToken();
        dataType();
        getNextToken();
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
        // validate "procedure-heading declarations block name ;"

        // validate "procedure name ;"
        procedureHeading();
        // validate declarations of procedure, false is for procedure declarations
        declarations(false);
        // validate block of procedure
        block();
        // validate name
        procedureName();
        // validate ";"
        simiColon();
    }

    private void procedureHeading() throws ParserException {
        // validate "procedure name ;"

        if (!currentToken.equals("procedure"))
            throw new ParserException("Procedure heading must start with \"procedure\", on line " + scanner.getTokenLine());

        getNextToken();
        name();

        procedureName = currentToken;

        getNextToken();
        simiColon();
    }

    private void procedureName() throws ParserException {
        // validate name that must be same as procedure-heading

        if (!currentToken.equals(procedureName))
            throw new ParserException("When you ending procedure," +
                    " you must use name that you entered in procedure-heading that is \"" + procedureName + "\""
                    + ", and on line " + scanner.getTokenLine() + " You are using " + currentToken);

        getNextToken();
    }

    private void stmtList() throws ParserException {
        // validate "statement ( ; statement )*"

        // validate "ass-stmt | read-stmt | write-stmt | if-stmt| while-stmt | repeat-stmt | exit-stmt | call-stmt | lambda"
        statement();

        // take new statement when have semicolon
        while (currentToken.equals(";")) {
            getNextToken();
            // validate "ass-stmt | read-stmt | write-stmt | if-stmt| while-stmt | repeat-stmt | exit-stmt | call-stmt | lambda"
            statement();
        }
    }

    private void statement() throws ParserException {
        // validate "ass-stmt | read-stmt | write-stmt | if-stmt| while-stmt | repeat-stmt | exit-stmt | call-stmt | lambda"

        if (isFirstOfReadStmt()) { // check if one of read stmts
            readStatement();
            getNextToken();
        } else if (isFirstOfWriteStmt()) { // check if one of write stmts
            writeStatement();
            getNextToken();
        } else if (currentToken.equals("if")) { // check if stmt is if stmt
            ifStatement();
            getNextToken();
        } else if (currentToken.equals("while")) { // check if stmt is while stmt
            whileStatement();
            getNextToken();
        } else if (currentToken.equals("loop")) { // check if stmt is repeat stmt
            RepeatStatement();
            getNextToken();
        } else if (currentToken.equals("exit")) { // check if stmt is exit stmt
            getNextToken();
        } else if (currentToken.equals("call")) { // check if stmt is call stmt
            call();
            getNextToken();
        } else if (Character.isLetter(currentToken.charAt(0)) && !reservedWords.contains(currentToken)) {
            // when first char is letter and word not one of above and not reserved word then it's assign stmt
            assignStatement();
        }

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
        // take new term when have + or -
        while (isAddOperation()) {
            getNextToken();
            term();
        }
    }

    private void term() throws ParserException {
        factor();
        // take new factor when has * | / | mod | div
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
        // validate name | value
        nameValue();
        getNextToken();

        // while token is a name validate const items
        while (currentToken.equals(",")) {
            getNextToken();
            nameValue();
            getNextToken();
        }

    }

    // this method is same with write-item
    private void nameValue() throws ParserException {
        if (Character.isDigit(currentToken.charAt(0))) // so its not valid value
            return;

        name();
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
            throw new ParserException("You must have \"end\" after if statement, on line " + scanner.getTokenLine());
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
        callProcedureName(); // validate if we call declared procedure
    }

    private void callProcedureName() {
        if (!currentToken.equals(procedureName))
            throw new ParserException("When you ending procedure," +
                    " you must use name that you entered in procedure-heading that is \"" + procedureName + "\""
                    + ", and on line " + scanner.getTokenLine() + " You are using " + currentToken);

    }

    private void condition() throws ParserException {
        nameValue();

        getNextToken();
        relationOperation();

        getNextToken();
        nameValue();

    }

    private void relationOperation() throws ParserException {
        if (!currentToken.equals("=") && !currentToken.equals("|=") && !currentToken.equals("<") &&
                !currentToken.equals("<=") && !currentToken.equals(">") && !currentToken.equals(">="))
            throw new ParserException("You must has valid operation here (=, |=, <, <=, >, >=) on line " + scanner.getTokenLine());

    }

    private void name() throws ParserException {
        // validate if token is valid name
        if (!Character.isLetter(currentToken.charAt(0)))
            throw new ParserException("Naming must start with char and you are using \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

        // validate if name is reserved word
        if (reservedWords.contains(currentToken))
            throw new ParserException("You are using a reserved word \"" + currentToken
                    + "\" as name on line " + scanner.getTokenLine());

    }


    private void moduleName() throws ParserException {
        // validate that name in end of module is same as module-heading
        if (!currentToken.equals(moduleName))
            throw new ParserException("When you ending module," +
                    " you must use name that you entered in module-heading that is \"" + moduleName + "\""
                    + ", on line " + scanner.getTokenLine() + " You are using end " + currentToken);

        getNextToken();
    }


    private void getNextToken() {
        currentToken = scanner.nextToken();
    }
}