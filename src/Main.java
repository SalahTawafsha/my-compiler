import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class Main  {
//    private static final Alert error = new Alert(Alert.AlertType.ERROR);

//    @Override
//    public void start(Stage primaryStage) {
//        Button selectFile = new Button("Click to import MODULA-2 File");
//        selectFile.setPadding(new Insets(10));
//        selectFile.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, new CornerRadii(50), null)));
//        selectFile.setBorder(
//                new Border(
//                        new BorderStroke(Color.BLACK,
//                                new BorderStrokeStyle(StrokeType.OUTSIDE, null, null, 1, 1, null),
//                                new CornerRadii(50), null)));
//
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setInitialDirectory(new File("."));
//        fileChooser.setTitle("Select MODULA-2 File");
//
//        selectFile.setOnAction(e -> {
//            File input = fileChooser.showOpenDialog(primaryStage);
//            if (input != null) {
//                CompilerParser compilerParser = new CompilerParser(input);
//                try {
//                     compilerParser.parse();
//                    Alert success = new Alert(Alert.AlertType.INFORMATION);
//                    success.setTitle("Parsing completed");
//                    success.setContentText("Successful parsing");
//                    success.show();
//                } catch (ParserConfigurationException ex) {
//                    error.setContentText(ex.getMessage());
//                    error.show();
//                }
//
//            } else {
//                error.setTitle("Select File please");
//                error.setContentText("You MUST select file !!");
//                error.show();
//            }
//        });
//
//        BorderPane pane = new BorderPane(selectFile);
//        Scene scene = new Scene(pane, 500, 500);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Parsing application");
//        primaryStage.getIcons().add(new Image("https://img.icons8.com/?size=256&id=4523&format=png"));
//        primaryStage.show();
//    }

    public static void main(String[] args) {
        File input = new File("program.txt");
        CompilerParser compilerParser = new CompilerParser(input);
        try {
            compilerParser.parse();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }


    }
}
