package pl.net.brach;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RozniceKursowe extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RozniceKursowe.class.getResource("MainWindow.fxml"));
        Parent root = (Parent) fxmlLoader.load();

        Scene scene = new Scene(root);

        //scene.getStylesheets().add("pl/net/brach/style.css");

        stage.setTitle("BRACHSoft - Różnice kursowe v.1.08");
        stage.getIcons().add(new Image("pl/net/brach/brachs.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
