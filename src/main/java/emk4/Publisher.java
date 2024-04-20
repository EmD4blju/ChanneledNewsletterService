package emk4;

import com.google.gson.Gson;
import emk4.JSON.TopicNetInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Publisher {

    private final SocketChannel socketChannel;

    public Publisher(String ipAddress, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(ipAddress, port));
        System.out.println("Connecting to the server ...");
        while(!socketChannel.finishConnect()){}
        System.out.println("Connected to the server :-)");


        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        CharBuffer charBuffer;


        while(true) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            String topicName = scanner.nextLine();
            String topicNetInfoJSON = new Gson().toJson(new TopicNetInfo(
                    topicName
            ));
            charBuffer = CharBuffer.wrap(command + " " + topicNetInfoJSON);
            socketChannel.write(Charset.defaultCharset().encode(charBuffer));
        }
//            byteBuffer.clear();
//            int readBytes = socketChannel.read(byteBuffer);
//            byteBuffer.flip();
//            charBuffer = Charset.defaultCharset().decode(byteBuffer);
//            String response = charBuffer.toString();
//            System.out.println("Response: " + response);
//            charBuffer.clear();

    }


    public static void main(String[] args) throws IOException {
        new Publisher("localhost", 8383);
    }
}
