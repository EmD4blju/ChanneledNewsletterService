package emk4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

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
        CharBuffer charBuffer = null;
        while (true){
            byteBuffer.clear();
            int readBytes = socketChannel.read(byteBuffer);
            if(readBytes == 0) continue;
            else if(readBytes == -1) break;
            else{
                byteBuffer.flip();
                charBuffer = Charset.defaultCharset().decode(byteBuffer);
                String response = charBuffer.toString();
                System.out.println(response);
                charBuffer.clear();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Publisher("localhost", 8383);
    }
}
