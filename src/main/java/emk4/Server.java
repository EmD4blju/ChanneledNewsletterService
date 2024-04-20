package emk4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final List<String> topics;

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
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    socketChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap("Hello client!")));
                    continue;
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        new Server("localhost", 8383);
    }

}
