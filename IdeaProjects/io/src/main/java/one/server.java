package one;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class server {

    public static void main(String []args){
        final String DEFAULT_SERVER_HOST="127.0.0.1";
        final int DEFAULT_SERVER_PORT=8888;
        final String quit="quit";
        BufferedWriter writer=null;
        ServerSocket serverSocket=null;
        BufferedReader reader=null;

        //创建io流
        try {
            serverSocket=new ServerSocket(DEFAULT_SERVER_PORT);
            String msg=null;
            while (true) {
                Socket socket = serverSocket.accept();
                reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                System.out.println("客户端"+socket.getPort()+"已经连接");


                while( (msg=reader.readLine())!=null){

                    System.out.println("客户端"+socket.getPort()+"发送"+msg);
                    writer.write("服务器回送："+msg+"\n");
                    writer.flush();
                    if(quit.equals(msg)){
                        System.out.println("客户端"+socket.getPort()+"断开连接");
                        break;
                    }
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                    System.out.println("服务器关闭" );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
