package emk4;

import com.google.gson.Gson;
import emk4.JSON.TopicNetInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class Server {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final List<TopicNetInfo> topics;


    public Server(String ipAddress, int port) throws IOException {
        topics = new ArrayList<>();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(ipAddress, port));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Listening for request...");
        while(true){
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
            while(selectionKeyIterator.hasNext()){
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();
                if(selectionKey.isAcceptable()){
                    System.out.println("Client has connected to the server");
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    continue;
                }
//                else if(selectionKey.isWritable()){
//                    System.out.println("Writing to client...");
//                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//                    socketChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap("Hello client!!!")));
//                    socketChannel.close();
//                    socketChannel.socket().close();
//                }
                if(selectionKey.isReadable()){
                    System.out.println("Reading from client...");
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    handleRequest(socketChannel);
                    continue;
                }
            }
        }
    }
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    private void handleRequest(SocketChannel socketChannel){
        try {
            byteBuffer.clear();
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
            StringBuilder request = new StringBuilder();
            CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
            while (charBuffer.hasRemaining()) {
                char character = charBuffer.get();
                if (character == '\r' || character == '\n') return;
                request.append(character);
            }
            System.out.println("From client: " + request);

            String[] requestData = request.toString().split(" ");

            if(requestData[0].equals("addTopic")){
                TopicNetInfo topicNetInfo = new Gson().fromJson(
                        requestData[1], TopicNetInfo.class
                );
                topics.add(topicNetInfo);
                topics.forEach(System.out::println);
            }

        }catch (IOException exception){
            exception.printStackTrace();
            try{
                socketChannel.close();
                socketChannel.socket().close();
            }catch (IOException ignored){}
        }
    }


    public static void main(String[] args) throws IOException {
        new Server("localhost", 8383);
    }

}
