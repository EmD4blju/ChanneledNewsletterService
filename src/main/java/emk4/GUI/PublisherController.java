package emk4.GUI;

import com.google.gson.Gson;
import emk4.JSON.Request;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.UUID;

public class PublisherController implements Initializable {
    @FXML
    private ComboBox<String> commandBox = new ComboBox<>();
    @FXML
    private TextArea commandPrompt = new TextArea();
    private SocketChannel socketChannel;
    private UUID adminID;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            adminID = UUID.randomUUID();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", 8383));
            System.out.println("Connecting to the server ...");
            while (!socketChannel.finishConnect()) {
            }
            System.out.println("Connected to the server :-)");
            new Thread(new ResponseReader(socketChannel)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        commandBox.getItems().addAll(FXCollections.observableArrayList(
                "addTopic",
                "rmTopic",
                "addArticle"
        ));
    }

    public void onSubmit(ActionEvent ignoredActionEvent) throws IOException{
        String commandType = commandBox.getValue();
        String commandParameters = commandPrompt.getText();
        CharBuffer charBuffer;
        String requestJSON = new Gson().toJson(
                buildRequest(commandType, commandParameters)
        );
        charBuffer = CharBuffer.wrap(requestJSON);
        socketChannel.write(Charset.defaultCharset().encode(charBuffer));
    }

    private static class ResponseReader implements Runnable{
        private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        private final SocketChannel socketChannel;
        public ResponseReader(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }
        @Override
        public void run() {
            StringBuffer response = new StringBuffer();
            try {
                while (true) {
                    byteBuffer.clear();
                    int readBytes = socketChannel.read(byteBuffer);
                    if(readBytes == 0) Thread.sleep(1000);
                    else if(readBytes == -1){
                        socketChannel.close();
                        break;
                    }else{
                        byteBuffer.flip();
                        CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
                        response.append(charBuffer);
                        System.out.println(response);
                        break;
                    }
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private Request buildRequest(String commandType, String commandParameters){
        switch (commandType) {
            case "addTopic" -> {
                return new Request(
                        Request.Role.ADMIN,
                        Request.Command.ADD_TOPIC,
                        commandParameters,
                        adminID
                );
            }
            case "rmTopic" -> {
                return new Request(
                        Request.Role.ADMIN,
                        Request.Command.REMOVE_TOPIC,
                        commandParameters,
                        adminID
                );
            }
            case "addArticle" -> {
                String[] commandData = commandParameters.split(" ");
                String topicName = commandData[0];
                String newsHeader = commandData[1];
                return new Request(
                        Request.Role.ADMIN,
                        Request.Command.ADD_NEWS,
                        topicName,
                        newsHeader,
                        adminID
                );
            }
            default -> throw new IllegalArgumentException("Unsupported command");
        }
    }

    public void onCommandChange(ActionEvent actionEvent) {
        ComboBox<String> comboBox = (ComboBox<String>) actionEvent.getSource();
        switch (comboBox.getValue()){
            case "addTopic", "rmTopic" -> commandPrompt.setPromptText("<Topic>");
            case "addArticle" -> commandPrompt.setPromptText("<Topic> <Article>");
        }
    }
}
