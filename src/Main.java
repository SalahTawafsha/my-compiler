import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner fileNamesScanner = new Scanner(System.in);
        while (true) {  // loop to read all files without run each time

            System.out.println("Enter file path or \"exit\" to end program:");
            String filePath = fileNamesScanner.nextLine();

            if (filePath.equals("exit")) // loop will stop when enter exit as file path
                break;

            try {
                File input = new File(filePath); // create file
                // pass file to CompilerParser object
                CompilerParser compilerParser = new CompilerParser(input);
                // make parse that can throw ParserException
                compilerParser.parse();
                System.out.println("Parse Successful");
            } catch (FileNotFoundException e) { // check if file not found
                System.err.println("File NOT found");
                // check if there are Exception (Exception to get scanner and parser exceptions) and print it message
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }


    }
}
