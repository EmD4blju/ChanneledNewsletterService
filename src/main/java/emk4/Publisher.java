package emk4;

import com.google.gson.Gson;
import emk4.JSON.TopicNetInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;

public class Publisher extends Application {

//    private final SocketChannel socketChannel;

//    public Publisher(String ipAddress, int port) throws IOException {


//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        CharBuffer charBuffer;


//        while(true) {
//            Scanner scanner = new Scanner(System.in);
//            String command = scanner.nextLine();
//            String[] commandInfo = command.split(" ");
//
//            if(commandInfo.length == 2) {
//                String topicNetInfoJSON = new Gson().toJson(new TopicNetInfo(
//                        commandInfo[1]
//                ));
//                charBuffer = CharBuffer.wrap(commandInfo[0] + " " + topicNetInfoJSON);
//                socketChannel.write(Charset.defaultCharset().encode(charBuffer));
//            }else if(commandInfo.length == 1){
//                charBuffer = CharBuffer.wrap(commandInfo[0]);
//                socketChannel.write(Charset.defaultCharset().encode(charBuffer));
//                return;
//            }else if(commandInfo.length == 3){
//                String topicNetInfoJSON = new Gson().toJson(new TopicNetInfo(
//                        commandInfo[1]
//                ));
//                charBuffer = CharBuffer.wrap(commandInfo[0] + " " + topicNetInfoJSON + " " + commandInfo[2]);
//                socketChannel.write(Charset.defaultCharset().encode(charBuffer));
//            }
//        }
//            byteBuffer.clear();
//            int readBytes = socketChannel.read(byteBuffer);
//            byteBuffer.flip();
//            charBuffer = Charset.defaultCharset().decode(byteBuffer);
//            String response = charBuffer.toString();
//            System.out.println("Response: " + response);
//            charBuffer.clear();
//    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/publisher.fxml")));
        stage.setTitle("Publisher's view");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
