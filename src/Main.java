import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner fileNamesScanner = new Scanner(System.in);
        System.out.println("Enter file path:");
        String filePath = fileNamesScanner.nextLine();
        File input = new File(filePath);
        CompilerParser compilerParser = new CompilerParser(input);

        try {
            compilerParser.parse();
            System.out.println("Parse Successful");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }


    }
}
