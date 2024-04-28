package emk4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import emk4.JSON.ClientNetInfo;
import emk4.JSON.Request;
import emk4.JSON.Response;
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
    private final Map<String, Set<UUID>> topicToSubscriberUUIDs;
    private final Map<UUID, List<String>> subscriberUUIDToNewsQueue;
    public Server(String ipAddress, int port) throws IOException {
        topicToSubscriberUUIDs = new HashMap<>();
        subscriberUUIDToNewsQueue = new HashMap<>();
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
                    socketChannel.register(selector, SelectionKey.OP_READ  | SelectionKey.OP_WRITE);
                }else if(selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    handleRequest(socketChannel, selectionKey);
                }else if(selectionKey.isWritable()){
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    List<String> newsQueue = (List<String>) selectionKey.attachment();
                    if (newsQueue != null){
                        if(!newsQueue.isEmpty()){
                            System.out.println(new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(newsQueue)
                            );
                            sendNewsToSubscriber(socketChannel, new Gson().toJson(newsQueue));
                            newsQueue.clear();
                        }
                    }
                }
            }
        }
    }

    private void sendNewsToSubscriber(SocketChannel socketChannel, String newsQueueJSON) throws IOException {
        socketChannel.write(
                Charset.defaultCharset().encode(
                        CharBuffer.wrap(newsQueueJSON)
                )
        );
    }

    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    private void handleRequest(SocketChannel socketChannel, SelectionKey selectionKey){
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
            Request incomingRequest = new Gson().fromJson(request.toString(), Request.class);
            try {
                if(incomingRequest.role.equals(Request.Role.ADMIN)) {
                    switch (incomingRequest.command) {
                        case ADD_TOPIC -> {
                            if(topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)) {
                                sendResponse("Topic already exists", socketChannel);
                                return;
                            }
                            topicToSubscriberUUIDs.put(incomingRequest.topicName, new HashSet<>());
                            System.out.println(new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(topicToSubscriberUUIDs)
                            );
                        }
                        case ADD_NEWS -> {
                            topicToSubscriberUUIDs.get(incomingRequest.topicName)
                                    .forEach(subscriber -> subscriberUUIDToNewsQueue
                                            .get(subscriber)
                                            .add(incomingRequest.newsHeader)
                                    );
                            System.out.println(new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(subscriberUUIDToNewsQueue)
                            );
                        }
                    }
                }else if(incomingRequest.role.equals(Request.Role.CLIENT)){
                    switch ((incomingRequest.command)){
                        case SUBSCRIBE -> {
                            List<String> subscriberQueue = new ArrayList<>();
                            selectionKey.attach(subscriberQueue);
                            subscriberUUIDToNewsQueue.put(incomingRequest.senderId, subscriberQueue);
                            topicToSubscriberUUIDs.get(incomingRequest.topicName).add(incomingRequest.senderId);
                            System.out.println(new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(topicToSubscriberUUIDs)
                            );
                        }
                    }
                }
                sendResponse("200 OK", socketChannel);
            }catch (JsonSyntaxException exception){
                System.out.println("Command parameters not recognized: ");
                System.out.println(exception.getMessage());
            }
        }catch (IOException exception){
            System.out.println("Client has disconnected");
            try{
                socketChannel.close();
                socketChannel.socket().close();
            }catch (IOException ignored){}
        }
    }


    public void sendResponse(String message, SocketChannel socketChannel) throws IOException {
        socketChannel.write(Charset.defaultCharset().encode(
                CharBuffer.wrap(
                        new Gson().toJson(
                                new Response(message)
                        )
                )
        ));
    }

    public static void main(String[] args) throws IOException {
        new Server("localhost", 8383);
    }

}
