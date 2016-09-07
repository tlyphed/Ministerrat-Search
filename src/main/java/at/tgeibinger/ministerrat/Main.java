package at.tgeibinger.ministerrat;

import at.tgeibinger.ministerrat.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setController(new MainController());
        Parent main = loader.load();
        primaryStage.setTitle("Ministerrat Suche");
        primaryStage.setScene(new Scene(main));
        primaryStage.show();
    }

    public static void main(String... args) throws Exception{
        try {
            launch(args);
        } catch (Exception e){
            System.err.println("ERROR: " + e.getLocalizedMessage());
        }
    }

}
