package emk4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

public class Publisher extends Application {


    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/publisher.fxml")));
        stage.setTitle("Publisher's view");
        stage.getIcons().add(new Image("https://cdn2.iconfinder.com/data/icons/circular-icon-set/256/Publisher.png"));
        stage.setScene(new Scene(root));
        stage.show();
    }
}
