package emk4.GUI;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;

public class PublisherController implements Initializable {
    @FXML
    private ComboBox<String> commandBox = new ComboBox<>();
    @FXML
    private Button submitButton = new Button();
    @FXML
    private TextArea commandPrompt = new TextArea();

    private SocketChannel socketChannel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", 8383));
            System.out.println("Connecting to the server ...");
            while (!socketChannel.finishConnect()) {
            }
            System.out.println("Connected to the server :-)");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        commandBox.getItems().addAll(FXCollections.observableArrayList(
                "addTopic",
                "rmTopic",
                "addArticle",
                "exit"
        ));
    }
    public void onSubmit(ActionEvent actionEvent) {
           
    }
}
