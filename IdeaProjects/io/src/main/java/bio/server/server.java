package bio.server;

import java.awt.image.ImageProducer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    private ExecutorService executorService;
    private int DEFAULT_PORT=8888;
    private final String QUIT="quit";
    private ServerSocket serverSocket;
    //端口作为客户端id
    private Map<Integer, Writer> clients;//todo 为啥用writer
    public server(){
        clients=new HashMap<>();
        executorService= Executors.newFixedThreadPool(10);//超过10个客户端  那多出来的客户端就等待
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if(socket!=null){
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clients.put(socket.getPort(),writer);
            System.out.println("客户端"+socket.getPort()+"连接到服务器");
        }
    }

    public synchronized void delClient(Socket socket) throws IOException {
        if(socket!=null){
            if(clients.containsKey(socket.getPort())){
                clients.get(socket.getPort()).close();
                clients.remove(socket.getPort());
                System.out.println("客户端"+socket.getPort()+"断开连接");

            }
        }
    }
    public synchronized void forward(Socket socket,String msg) throws IOException {
        //知道是谁发的，不转发给自己
        int sender=socket.getPort();
        for(Integer id:clients.keySet()){
            if(id!=sender){
                Writer writer = clients.get(id);
                writer.write(msg);
                writer.flush();
            }
        }
    }
    public void start(){
        try {
            serverSocket=new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口...");

            while(true){
                //不断地accept 各个客户端（socket)
                Socket socket=serverSocket.accept();
                //创建ChatHandler线程 由handler来处理socket
                executorService.execute(new Handler(this,socket));

                //
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
         close();

        }
    }
    public boolean readyToQUit(String msg){
        return msg.equals("quit");
    }
    public synchronized void close(){
        if(serverSocket!=null){
            try {
                serverSocket.close();
                System.out.println("关闭serversocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static  void  main(String args[]){
            server server=new server();
            server.start();
    }
}
