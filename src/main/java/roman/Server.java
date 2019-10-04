package roman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws IOException{
        int port = 6379;

        ServerSocket serverSocket = new ServerSocket(port);//ServerScoket监听端口

        System.out.println("服务器等待连接..."+serverSocket.getLocalSocketAddress());

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        while(true){

            Socket client = serverSocket.accept();

            System.out.println("有客户端连接到服务器..."+client.getRemoteSocketAddress());

            executorService.execute(new MutliThread(client));
        }
    }
}