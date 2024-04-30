package emk4.GUI;

import com.google.gson.Gson;
import emk4.Client;
import emk4.JSON.Request;
import emk4.JSON.Response;
import emk4.QueueItem;
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

public class ClientController implements Initializable {
    @FXML
    private TextArea textArea = new TextArea();
    @FXML
    private ComboBox<String> newslettersBox = new ComboBox<>();
    private SocketChannel socketChannel;
    private UUID clientId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.clientId = UUID.randomUUID();
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(
                    "localhost",
                    8383
            ));
            System.out.println("Connecting to the server ...");
            while (!socketChannel.finishConnect()) {
            }
            System.out.println("Connected to the server :-)");
            socketChannel.write(
                    Charset.defaultCharset().encode(
                            CharBuffer.wrap(
                                    new Gson().toJson(
                                            new Request(
                                                    Request.Role.CLIENT,
                                                    Request.Command.REGISTER_CLIENT,
                                                    clientId
                                            )
                                    )
                            )
                    )
            );
            new Thread(new ResponseReader(socketChannel, this)).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }




    private static class ResponseReader implements Runnable{

        private final SocketChannel socketChannel;
        private final ByteBuffer byteBuffer;
        private final ClientController GUI;

        public ResponseReader(SocketChannel socketChannel, ClientController GUI) {
            this.socketChannel = socketChannel;
            this.GUI = GUI;
            byteBuffer = ByteBuffer.allocate(1024);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byteBuffer.clear();
                    int readBytes = socketChannel.read(byteBuffer);
                    if(readBytes == 0) Thread.sleep(1000);
                    else if(readBytes == -1){
                        socketChannel.close();
                        break;
                    }else{
                        StringBuilder responseJSON = new StringBuilder();
                        byteBuffer.flip();
                        CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
                        responseJSON.append(charBuffer);
                        Response response = new Gson().fromJson(responseJSON.toString(), Response.class);
                        System.out.println(response);
                        if(response.subscriberQueue != null){
                            if(!response.subscriberQueue.isEmpty()){
                                response.subscriberQueue.forEach(
                                        item -> {
                                            if(item.type == QueueItem.Type.TOPIC) {
                                                if(item.operation == QueueItem.Operation.ADDED)
                                                    GUI.newslettersBox.getItems().add(item.topicName);
                                                else if(item.operation == QueueItem.Operation.DELETED)
                                                    GUI.newslettersBox.getItems().remove(item.topicName);
                                            }else if(item.type == QueueItem.Type.NEWS){
                                                GUI.textArea.appendText("- [" +
                                                        item.topicName + "] " + item.newsName + "\n");
                                            }
                                        }
                                );
                            }
                        }
                    }
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onSubscribe(ActionEvent ignoredActionEvent) {
        Request request = new Request(
                Request.Role.CLIENT,
                Request.Command.SUBSCRIBE,
                newslettersBox.getValue(),
                clientId
        );
        String requestJSON = new Gson().toJson(request);
        try {
            socketChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(requestJSON)));
        } catch (IOException ignored) {
            System.out.println("Request failed");
        }
    }
    @FXML
    public void onUnsubscribe(ActionEvent ignoredActionEvent) {
        Request request = new Request(
                Request.Role.CLIENT,
                Request.Command.UNSUBSCRIBE,
                newslettersBox.getValue(),
                clientId
        );
        String requestJSON = new Gson().toJson(request);
        try{
            socketChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(requestJSON)));
        }catch (IOException ignored){
            System.out.println("Request failed");
        }
    }
}
