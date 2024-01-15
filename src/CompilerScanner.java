import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CompilerScanner {
    // scanner that will read lines from the file
    private final Scanner scanner;

    // index of line string that I'm current checking it
    private int currIndex = -1;

    // current line that I got it from file and checking it
    private String currLine = "";

    // to use it when I want to show Illegal character Error
    private int lineOfToken = 0;

    // constructor that I can pass file of MODULA-2 to it
    // and make Scanner on the file to be able to get tokens from it
    public CompilerScanner(File input) throws FileNotFoundException {
        scanner = new Scanner(input);
    }

    // function that returns token line to report error
    public int getLineOfToken() {
        return lineOfToken;
    }

    // method that will return token by token when I call it
    // when reach to end of line get new line and if not hasNextLine return empty string (End Of File)
    // this method is only method that is public and can use by instance to get tokens until reach empty string (EOF)
    public String nextToken() {
        if (currIndex == currLine.length() - 1) { // end of line
            if (!scanner.hasNextLine()) { // close scanner and return empty string if no new line
                scanner.close();
                return "";
            }

            // do get new line until has line not empty and not blank OR reach to EOF
            do {
                currLine = scanner.nextLine();  // get new line
                lineOfToken++;               // increment line number
                currIndex = -1;                 // reset index
            } while ((currLine.isEmpty() || currLine.isBlank()) && scanner.hasNextLine());

            // if after previous loop line is empty so, we reach EOF
            if (currLine.isEmpty() || currLine.isBlank()) {
                scanner.close();
                return "";
            }

        }

        // get new chart and return getToken
        getNextChar();
        return getToken();
    }

    // this method will change index to new character and returns the new character
    // it returns Character not char to be able to return null
    private Character getNextChar() {
        if (currIndex == currLine.length() - 1) // return null if this is end of line
            return null;
        else // otherwise increment currIndex to get new char
            currIndex++;

        return currLine.charAt(currIndex);  // return new char
    }

    // return to prev index when I want to back to prev char after check it and I don't need this char
    // example: when I have < so I must check next char and it maybe not = then, I want to Rollback
    private void rollbackChar() {
        currIndex--;
    }

    // check first char using switch and return correct token
    // return token directly when token just one char like =
    // and return function of it when it can be more than one char like := or names and numbers
    private String getToken() throws IllegalArgumentException {
        switch (currLine.charAt(currIndex)) {
            case ' ':
            case '\t':
                return nextToken();
            case '.':
                return ".";
            case ';':
                return ";";
            case '(':
                return "(";
            case ',':
                return ",";
            case ')':
                return ")";
            case ':':
                return getColon();
            case '+':
                return "+";
            case '-':
                return "-";
            case '*':
                return "*";
            case '/':
                return "/";
            case '=':
                return "=";
            case '|':
                return getPipeEqual();
            case '<':
                return getLessThan();
            case '>':
                return getGreaterThan();
            default:
                // when reach default it must be letter or digit

                // if it is digit call getNumber() method
                if (Character.isDigit(currLine.charAt(currIndex)))
                    return getNumber();
                else if (Character.isLetter(currLine.charAt(currIndex))) // if it is letter call getName() method
                    return getName();
                else
                    throw new IllegalArgumentException("Illegal character Error: line " + lineOfToken + " has Illegal character '" + currLine.charAt(currIndex) + "'");
        }
    }

    private String getName() {
        Character currentChar = currLine.charAt(currIndex); // get first char
        StringBuilder stringBuilder = new StringBuilder();  // string builder to build name in it and return it

        // first char is letter by calling from switch so use do while to don't check first char
        // the do while will work until reach null that will return when reach to end of line
        // or until have char that is not digit or letter
        do {
            stringBuilder.append(currentChar);
            currentChar = getNextChar();
        } while (currentChar != null && Character.isLetterOrDigit(currentChar));

        // if not reach end of line, Rollback to prev char to be able to get next of it
        // because nextToken() function will make get new char
        if (currentChar != null) rollbackChar();

        // finally return name
        return stringBuilder.toString();
    }

    private String getNumber() {
        Character currentChar = currLine.charAt(currIndex); // get first digit
        StringBuilder stringBuilder = new StringBuilder(); // string builder to build name in it and return it
        boolean isReal = false; // to used as flag to has one .

        // first char is digit by calling from switch so use do while to don't check first char
        // the do while will work until reach null that will return when reach to end of line
        // or until have char that is not digit
        // or has already . and has new .
        do {
            if (currentChar == '.') // change flag when have .
                isReal = true;

            stringBuilder.append(currentChar);
            currentChar = getNextChar();
        } while (currentChar != null && (Character.isDigit(currentChar) || (!isReal && currentChar == '.')));

        // if not reach end of line, Rollback to prev char to be able to get next of it
        // because nextToken() function will make get new char
        if (currentChar != null) rollbackChar();

        // finally return number
        return stringBuilder.toString();
    }

    private String getGreaterThan() { // if next char is = then return >= otherwise make Rollback and return >
        Character nextChar = getNextChar();
        if (nextChar != null && nextChar == '=') return ">=";
        else {
            rollbackChar();
            return ">";
        }
    }

    private String getLessThan() {// if next char is = then return <= otherwise make Rollback and return <
        Character nextChar = getNextChar();
        if (nextChar != null && nextChar == '=') return "<=";
        else {
            rollbackChar();
            return "<";
        }
    }

    // if there are | so it must be followed with =, otherwise it Illegal character
    private String getPipeEqual() throws IllegalArgumentException {
        Character nextChar = getNextChar();
        if (nextChar != null && nextChar == '=') return "|=";
        else {
            throw new IllegalArgumentException("Illegal character Error: line " + lineOfToken + " has Illegal character '" + currLine.charAt(currIndex) + "'");
        }
    }

    private String getColon() { // if next char is = then return := otherwise make Rollback and return :
        Character nextChar = getNextChar();
        if (nextChar != null && nextChar == '=') return ":=";
        else {
            rollbackChar();
            return ":";
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        CompilerScanner compilerScanner = new CompilerScanner(new File("all.txt"));
        String token = compilerScanner.nextToken();
        while (!token.isEmpty()) {
            System.out.println(token);
            token = compilerScanner.nextToken();
        }
    }
}