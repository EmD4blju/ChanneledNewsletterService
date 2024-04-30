package emk4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import emk4.JSON.Request;
import emk4.JSON.Response;

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
    private final Map<String, Set<UUID>> topicToSubscriberUUIDs;
    private final Map<UUID, List<QueueItem>> subscriberUUIDToNewsQueue;
    public Server(String ipAddress, int port) throws IOException {
        topicToSubscriberUUIDs = new HashMap<>();
        subscriberUUIDToNewsQueue = new HashMap<>();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(ipAddress, port));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
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
                    List<QueueItem> subscriberQueue = (List<QueueItem>) selectionKey.attachment();
                    if (subscriberQueue != null){
                        if(!subscriberQueue.isEmpty()){
                            System.out.println("Sending : " );
                            subscriberQueue.forEach(System.out::println);
                            System.out.println(new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(subscriberQueue)
                            );
                            sendNewsToSubscriber(socketChannel, new Gson().toJson(
                                    new Response(subscriberQueue)
                            ));
                            subscriberQueue.clear();
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

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    private void handleRequest(SocketChannel socketChannel, SelectionKey selectionKey){
        try {
            Request incomingRequest = new Gson().fromJson(getRequest(socketChannel), Request.class);
            try {
                if(incomingRequest.role.equals(Request.Role.ADMIN)) {
                    switch (incomingRequest.command) {
                        case ADD_TOPIC -> addTopic(incomingRequest, socketChannel);
                        case REMOVE_TOPIC -> removeTopic(incomingRequest, socketChannel);
                        case ADD_NEWS -> addNews(incomingRequest, socketChannel);
                    }
                }else if(incomingRequest.role.equals(Request.Role.CLIENT)){
                    switch ((incomingRequest.command)){
                        case SUBSCRIBE -> addSubscription(incomingRequest, socketChannel);
                        case UNSUBSCRIBE -> removeSubscription(incomingRequest, socketChannel);
                        case REGISTER_CLIENT -> registerClient(incomingRequest, selectionKey, socketChannel);
                    }
                }
                sendResponse("200 OK", incomingRequest.command, socketChannel);
            }catch (JsonSyntaxException exception){
                sendResponse("500 Something went wrong", incomingRequest.command, socketChannel);
                System.out.println("Command parameters not recognized: ");
                System.out.println(exception.getMessage());
            }catch(IOException ignored){}
        }catch (IOException exception){
            System.out.println("Client has disconnected");
            try{
                socketChannel.close();
                socketChannel.socket().close();
            }catch (IOException ignored){}
        }
    }

    private void registerClient(Request incomingRequest, SelectionKey selectionKey, SocketChannel socketChannel) throws IOException {
        List<QueueItem> subscriberQueue = new ArrayList<>();
        subscriberUUIDToNewsQueue.put(
                incomingRequest.senderId,
                subscriberQueue
        );
        selectionKey.attach(subscriberQueue);
    }

    private void removeSubscription(Request incomingRequest, SocketChannel socketChannel) throws IOException {
        if(!topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)
            || !topicToSubscriberUUIDs.get(incomingRequest.topicName).contains(incomingRequest.senderId)){
            sendResponse("404 Topic not found", incomingRequest.command, socketChannel);
            throw new IOException("404 Topic not found");
        }
        topicToSubscriberUUIDs.get(incomingRequest.topicName).remove(incomingRequest.senderId);
        subscriberUUIDToNewsQueue.remove(incomingRequest.senderId);
    }

    private void removeTopic(Request incomingRequest, SocketChannel socketChannel) throws IOException {
        if(!topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)){
            sendResponse("404 Topic not found", incomingRequest.command, socketChannel);
            throw new IOException("404 Topic not found");
        }
        topicToSubscriberUUIDs.get(incomingRequest.topicName)
                .forEach(subscriber -> subscriberUUIDToNewsQueue
                        .get(subscriber)
                        .add(new QueueItem(
                                incomingRequest.topicName,
                                QueueItem.Operation.DELETED,
                                QueueItem.Type.TOPIC
                        )));
        topicToSubscriberUUIDs.remove(incomingRequest.topicName);
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(topicToSubscriberUUIDs)
        );
        Set<UUID> subscribersUUIDS = subscriberUUIDToNewsQueue.keySet();
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(subscriberUUIDToNewsQueue)
        );
        for(UUID subscriberUUID : subscribersUUIDS){
            System.out.println("Adding: " + subscriberUUID.toString() + " to broadcast");
            subscriberUUIDToNewsQueue.get(subscriberUUID)
                    .add(
                            new QueueItem(
                                    incomingRequest.topicName,
                                    QueueItem.Operation.DELETED,
                                    QueueItem.Type.TOPIC
                            )
                    );
        }
    }

    private void addSubscription(Request incomingRequest, SocketChannel socketChannel) throws IOException {
        if(!topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)){
            sendResponse("404 Topic not found", incomingRequest.command, socketChannel);
            throw new IOException("404 Topic not found");
        }
        topicToSubscriberUUIDs.get(incomingRequest.topicName).add(incomingRequest.senderId);
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(topicToSubscriberUUIDs)
        );
    }


    private void addNews(Request incomingRequest, SocketChannel socketChannel) throws IOException {
        if(!topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)){
            sendResponse("404 Topic not found", incomingRequest.command, socketChannel);
            throw new IOException("404 Topic not found");
        }
        topicToSubscriberUUIDs.get(incomingRequest.topicName)
                .forEach(subscriber -> subscriberUUIDToNewsQueue
                        .get(subscriber)
                        .add(new QueueItem(
                                incomingRequest.topicName,
                                incomingRequest.newsHeader,
                                QueueItem.Operation.ADDED,
                                QueueItem.Type.NEWS
                        ))
                );
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(subscriberUUIDToNewsQueue)
        );
    }

    private void addTopic(Request incomingRequest, SocketChannel socketChannel) throws IOException {
        if(topicToSubscriberUUIDs.containsKey(incomingRequest.topicName)) {
            sendResponse("400 Topic already exists", incomingRequest.command, socketChannel);
            throw new IOException("404 Topic exists");
        }
        topicToSubscriberUUIDs.put(incomingRequest.topicName, new HashSet<>());
        Set<UUID> subscribersUUIDS = subscriberUUIDToNewsQueue.keySet();
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(subscriberUUIDToNewsQueue)
        );
        for(UUID subscriberUUID : subscribersUUIDS){
            System.out.println("Adding: " + subscriberUUID.toString() + " to broadcast");
            subscriberUUIDToNewsQueue.get(subscriberUUID)
                    .add(
                            new QueueItem(
                                    incomingRequest.topicName,
                                    QueueItem.Operation.ADDED,
                                    QueueItem.Type.TOPIC
                            )
                    );
        }
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(topicToSubscriberUUIDs)
        );
    }


    private String getRequest(SocketChannel socketChannel) throws IOException {
        byteBuffer.clear();
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        StringBuilder request = new StringBuilder();
        CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
        while (charBuffer.hasRemaining()) {
            char character = charBuffer.get();
            if (character == '\r' || character == '\n') break;
            request.append(character);
        }
        return request.toString();
    }

    public void sendResponse(String message, Request.Command commandType, SocketChannel socketChannel) throws IOException {
        socketChannel.write(Charset.defaultCharset().encode(
                CharBuffer.wrap(
                        new Gson().toJson(
                                new Response(message, commandType)
                        )
                )
        ));
    }

    public static void main(String[] args) throws IOException {
        new Server("localhost", 8383);
    }

}
