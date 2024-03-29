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
        String[] reservedWords = {"module", "begin", "end", "const", "var", "integer", "real", "char", "procedure", "mod", "div", "readint", "readreal", "readchar", "readln", "writeint", "writereal", "writechar", "writeln", "if", "then", "end", "elseif", "else", "while", "do", "loop", "until", "exit", "call"};

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
        declarations();
        // validate "procedure-heading declarations block name ;"
        procedureDecl();
        // validate "begin stmt-list end"
        block();
        // validate that name in end of module is same of module heading
        moduleName();

        // finally, validate has "."
        if (!currentToken.equals(".")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "File must end with \".\" not with \"" + currentToken + "\".");
        }
        // validate if "." is last token
        // here check if there is any token after "." and throw error
        getNextToken();
        if (!currentToken.isEmpty()) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "File must not have any token after \".\" and you have \"" + currentToken + "\" after \".\"");
        }
    }

    private void moduleHeading() throws ParserException {
        // validate "module name ;"

        if (!currentToken.equals("module")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must start module with \"module\" not \"" + currentToken + "\".");
        }
        getNextToken();

        name();
        // store module name to validate with end of program
        moduleName = currentToken;
        getNextToken();

        simiColon();
    }

    private void block() throws ParserException {
        // validate "begin stmt-list end"

        if (!currentToken.equals("begin")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must start block here with \"begin\" not \"" + currentToken + "\".");
        }
        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();

        if (!currentToken.equals("end")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must end block here with \"end\" not \"" + currentToken + "\"");
        }
        getNextToken();
    }

    private void simiColon() throws ParserException {
        // validate ";"
        if (!currentToken.equals(";")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must end statement with \";\" before \"" + currentToken + "\"");
        }
        getNextToken();
    }

    private void declarations() throws ParserException {
        // validate "const-decl var-decl"

        // validate "const const-list | lambda"
        constDeclarations();


        // validate "var var-list | lambda"
        varDeclarations();

    }

    private void constDeclarations() throws ParserException {
        // validate "const const-list | lambda"

        // if token is const so call const-list, otherwise its lambda
        if (currentToken.equals("const")) {
            getNextToken();
            // validate "( name = value ; )*"
            constList();
        }
    }

    private void constList() throws ParserException {
        // validate "( name = value ; )*"


        // validate "name = value;"
        constItem();

//        if (isModuleDeclarations) { // if this is module declaration so loop until find var or procedure

        // while token is a name validate const items
        while (!currentToken.equals("var") && !currentToken.equals("procedure") && !currentToken.equals("begin"))
            constItem(); // validate "name = value;"

//        } else { // else, it's procedure declaration so loop until find var or begin
//
//            // while token is a name validate const items
//            while (!currentToken.equals("var") && !currentToken.equals("begin"))
//                constItem(); // validate "name = value;"
//        }
    }

    private void constItem() throws ParserException {
        // validate "name = value;"

        name();

        getNextToken();
        if (!currentToken.equals("=")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "When you are declaring const item, you must use '=' between name and value not \"" + currentToken + "\"");
        }
        getNextToken();
        // value is number so must start with digit
        if (!Character.isDigit(currentToken.charAt(0))) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "Value must be number and can't be \"" + currentToken + "\"");
        }
        getNextToken();
        simiColon();

    }

    private void varDeclarations() throws ParserException {
        // validate "var var-list | lambda"

        // if token is var so call var-list, otherwise its lambda
        if (currentToken.equals("var")) {
            getNextToken();
            // validate "( var-item ; )*"
            varList();
        }
    }

    private void varList() throws ParserException {
        // validate "( var-item ; )*"

        // validate name-list : data-type
        varItem();
        // validate ";"
        simiColon();

        // while token is not follow var-list validate const items
        while (!currentToken.equals("procedure") && !currentToken.equals("begin")) {
            // validate name-list : data-type
            varItem();
            // validate ";"
            simiColon();
        }

    }

    private void varItem() throws ParserException {
        // validate name-list : data-type

        // validate name ( , name )*
        nameList();

        if (!currentToken.equals(":")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must use ':' after name-list not \"" + currentToken + "\"");
        }
        getNextToken();
        dataType();
    }

    private void nameList() throws ParserException {
        // validate name ( , name )*

        name();
        getNextToken();

        while (currentToken.equals(",")) {
            getNextToken();
            name();
            getNextToken();
        }
    }

    private void dataType() throws ParserException {
        if (!currentToken.equals("integer") && !currentToken.equals("real") && !currentToken.equals("char")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must use one of data types {integer, real, char} not \"" + currentToken + "\"");
        }
        getNextToken();
    }

    private void procedureDecl() throws ParserException {
        // validate "procedure-heading declarations block name ;"

        // validate "procedure name ;"
        procedureHeading();
        // validate declarations of procedure, false is for procedure declarations
        declarations();
        // validate block of procedure
        block();
        // validate name
        procedureName();
        // validate ";"
        simiColon();
    }

    private void procedureHeading() throws ParserException {
        // validate "procedure name ;"

        if (!currentToken.equals("procedure")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must start procedure here using \"procedure\" not \"" + currentToken + "\"");
        }
        getNextToken();
        name();

        procedureName = currentToken;

        getNextToken();
        simiColon();
    }

    private void procedureName() throws ParserException {
        // validate name that must be same as procedure-heading

        if (!currentToken.equals(procedureName)) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "When you are ending procedure, you must use name that you entered in procedure-heading that is \"" + procedureName + "\"" + ", and you are using \"" + currentToken + "\".");
        }
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

        if (isFirstOfReadStmt()) // check if one of read stmts and call read-stmt method
            readStatement();
        else if (isFirstOfWriteStmt())  // check if one of write stmts and call write-stmt method
            writeStatement();
        else if (currentToken.equals("if"))  // check if stmt is if stmt and call if-stmt method
            ifStatement();
        else if (currentToken.equals("while"))  // check if stmt is while stmt and call while-stmt method
            whileStatement();
        else if (currentToken.equals("loop")) // check if stmt is repeat stmt and call repeat-stmt method
            RepeatStatement();
        else if (currentToken.equals("exit"))  // check if stmt is exit stmt and just get next token because nothing after exit
            getNextToken();
        else if (currentToken.equals("call"))  // check if stmt is call stmt and call call-stmt method
            callStatement();
        else if (Character.isLetter(currentToken.charAt(0)) && !reservedWords.contains(currentToken))
            // when first char is letter and word not one of above and not reserved word then it's assign stmt
            assignStatement();


    }

    private void assignStatement() throws ParserException {
        // validate "name := exp"
        name();
        getNextToken();
        if (!currentToken.equals(":=")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must use \":=\" for assignment statement not \"" + currentToken + "\"");
        }        getNextToken();

        exp(); // validate "term ( add-oper term )*"
    }

    private void exp() throws ParserException {
        // validate "term ( add-oper term )*"

        term(); // validate "factor ( mul-oper factor )*"

        // take new term when have + or -
        while (isAddOperation()) {
            getNextToken();
            term(); // validate "factor ( mul-oper factor )*"
        }
    }

    private void term() throws ParserException {
        // validate "factor ( mul-oper factor )*"

        factor(); // validate "( exp ) | name | value"

        // take new factor when has * | / | mod | div
        while (isMultiplyOperation()) {
            getNextToken();
            factor(); // validate "( exp ) | name | value"
        }

    }

    private void factor() throws ParserException {
        // validate "( exp ) | value | name"

        if (currentToken.equals("(")) {
            getNextToken();
            exp();

            if (!currentToken.equals(")")) {
                scanner.close();
                throw new ParserException(scanner.getLineOfToken(), "You must use \")\" before \"" + currentToken + "\" to close opened bracket");
            }
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
        return currentToken.equals("readint") || currentToken.equals("readreal") || currentToken.equals("readchar") || currentToken.equals("readln");
    }

    private void readStatement() throws ParserException {
        // validate "readint ( name-list ) | readreal ( name-list ) | readchar ( name-list ) | readln"

        // when it readln nothing after so get next token and return
        if (currentToken.equals("readln")) {
            getNextToken();
            return;
        }

        getNextToken();
        if (!currentToken.equals("(")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "This is read statement and you must have \"(\" before \"" + currentToken + "\"");
        }
        getNextToken();
        // validate name ( , name )*
        nameList();

        if (!currentToken.equals(")")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "This is read statement and you must have \")\" after names, before \"" + currentToken + "\"");
        }
        getNextToken();
    }

    private boolean isFirstOfWriteStmt() {
        return currentToken.equals("writeint") || currentToken.equals("writereal") || currentToken.equals("writechar") || currentToken.equals("writeln");
    }

    private void writeStatement() throws ParserException {
        // validate "writeint ( write-list ) | writereal ( write-list ) | writechar ( write-list ) | writeln"

        // when it writeln nothing after so get next token and return
        if (currentToken.equals("writeln")) {
            getNextToken();
            return;
        }


        getNextToken();
        if (!currentToken.equals("(")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "This is write statement and you must have \"(\" before \"" + currentToken + "\"");
        }
        getNextToken();
        // validate "write-item ( , write-item )*"
        writeList();

        if (!currentToken.equals(")")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "This is write statement and you must have \")\" after write items, before \"" + currentToken + "\"");
        }
        getNextToken();
    }

    private void writeList() throws ParserException {
        // validate "write-item ( , write-item )*"

        // validate name | value
        nameValue();

        // while token is a name validate const items
        while (currentToken.equals(",")) {
            getNextToken();
            // validate name | value
            nameValue();
        }

    }

    // this method is same with write-item
    private void nameValue() throws ParserException {
        // validate name | value
        if (Character.isDigit(currentToken.charAt(0))) { // so its not valid value
            getNextToken();
            return;
        }

        name();
        getNextToken();
    }

    private void ifStatement() throws ParserException {
        // validate "if condition then stmt-list elseif-part else-part end"

        getNextToken(); // skip "if" word

        // validate "name-value relational-operation name-value"
        condition();

        if (!currentToken.equals("then")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"then\" not \"" + currentToken + "\" after condition of if.");
        }
        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();

        while (currentToken.equals("elseif")) elseIfPart(); // validate "( elseif condition then stmt-list )*"


        if (currentToken.equals("else")) elsePart(); // validate "else stmt-list | lambda"

        if (!currentToken.equals("end")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"end\" not \"" + currentToken + "\" to end if statement.");
        }
        getNextToken();
    }

    private void elseIfPart() throws ParserException {
        // validate "( elseif condition then stmt-list )*"

        getNextToken(); // skip "elseif" word

        // validate "name-value relational-operation name-value"
        condition();

        if (!currentToken.equals("then")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"then\" not \"" + currentToken + "\" after condition of elseif.");
        }
        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();

    }

    private void elsePart() throws ParserException {
        // validate "else stmt-list"

        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();
    }

    private void whileStatement() throws ParserException {
        // validate "while condition do stmt-list end"

        getNextToken(); // skip "while" word

        // validate "name-value relational-operation name-value"
        condition();

        if (!currentToken.equals("do")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"do\" not \"" + currentToken + "\" after condition of while.");
        }
        getNextToken();
        // validate "statement ( ; statement )*"
        stmtList();

        if (!currentToken.equals("end")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"end\" not \"" + currentToken + "\" to end while statement.");
        }
        getNextToken();
    }

    private void RepeatStatement() throws ParserException {
        // validate "loop stmt-list until condition"

        getNextToken(); // skip "repeat" word

        // validate "statement ( ; statement )*"
        stmtList();

        if (!currentToken.equals("until")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must have \"until\" not \"" + currentToken + "\" after stmt-list of repeat.");
        }
        getNextToken();
        // validate "name-value relational-operation name-value"
        condition();
    }

    private void callStatement() throws ParserException {
        // validate "call name"
        getNextToken();
        callProcedureName(); // validate if we call declared procedure

        getNextToken();
    }

    private void callProcedureName() {
        if (!currentToken.equals(procedureName)) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must use name that you entered in procedure-heading that is \"" + procedureName + "\" and you are using " + currentToken + ".");
        }
    }

    private void condition() throws ParserException {
        // validate "name-value relational-operation name-value"

        nameValue();
        relationOperation(); // validate "= | |= | < | <= | > | >="
        nameValue();
    }

    private void relationOperation() throws ParserException {
        if (!currentToken.equals("=") && !currentToken.equals("|=") && !currentToken.equals("<") && !currentToken.equals("<=") && !currentToken.equals(">") && !currentToken.equals(">=")) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "You must use one of relation operations {=, |=, <, <=, >, >=} not \"" + currentToken + "\".");
        }
        getNextToken();
    }

    private void name() throws ParserException {
        // validate if token is valid name
        if (!Character.isLetter(currentToken.charAt(0))) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "Name must start with letter and you are using \"" + currentToken + "\".");
        }
        // validate if name is reserved word
        if (reservedWords.contains(currentToken)) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "Name can't be reserved word, you are using \"" + currentToken + "\".");
        }
    }


    private void moduleName() throws ParserException {
        // validate that name in end of module is same as module-heading
        if (!currentToken.equals(moduleName)) {
            scanner.close();
            throw new ParserException(scanner.getLineOfToken(), "When you are ending module, you must use name that you entered in module-heading that is \"" + moduleName + "\"" + ", and you are using " + currentToken);
        }
        getNextToken();
    }


    private void getNextToken() {
        currentToken = scanner.nextToken();
    }
}