package emk4.GUI;

import com.google.gson.Gson;
import emk4.JSON.ClientNetInfo;
import emk4.JSON.Request;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.Future;

public class ClientController implements Initializable {

    @FXML
    private Button updateBtn;
    @FXML
    private ComboBox newslettersBox;
    @FXML
    private Button subscribeBtn;
    @FXML
    private Button unsubscribeBtn;
    @FXML
    private ListView subscriptions;
    private SocketChannel socketChannel;
    private UUID clientId;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);



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
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onSubscribe(ActionEvent actionEvent) {
        Request request = new Request(
                Request.Role.CLIENT,
                Request.Command.SUBSCRIBE,
                "sports",
                clientId
        );
        String requestJSON = new Gson().toJson(request);
        try {
            socketChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(requestJSON)));
            socketChannel.configureBlocking(true);
            while(true) {
                byteBuffer.clear();
                socketChannel.read(byteBuffer);
                byteBuffer.flip();
                StringBuilder response = new StringBuilder();
                CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
                while (charBuffer.hasRemaining()) {
                    char character = charBuffer.get();
                    if (character == '\r' || character == '\n') return;
                    response.append(character);
                }
                System.out.println(response);
            }
        } catch (IOException ignored) {
            System.out.println("Request failed");
        }

    }
}
