package emk4;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Client extends Application {

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/client.fxml")));
        stage.setTitle("Client's view");
        stage.getIcons().add(new Image("https://cdn4.iconfinder.com/data/icons/twitter-ui-set/128/Persone-256.png"));
        stage.setScene(new Scene(root));
        stage.show();
    }
}
