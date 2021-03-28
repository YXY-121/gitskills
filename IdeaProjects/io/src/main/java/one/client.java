package one;

import java.io.*;
import java.net.Socket;

public class client {
    public static void main(String []args) {
        final String quit="quit";
        final String DEFAULT_SERVER_HOST="127.0.0.1";
        final int DEFAULT_SERVER_PORT=8888;
        Socket socket=null;
        BufferedWriter writer=null;
        BufferedReader reader=null;

        try {
            socket=new Socket(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);

             reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            BufferedReader consoleReader=new BufferedReader(new InputStreamReader(System.in));
         while(true){
             String input=consoleReader.readLine();


             //发送信息给服务器
             writer.write(input+"\n");
             writer.flush();

             //读取服务器返回的消息
             String s = reader.readLine();
             System.out.println(s);
             //查看客户端有什么变化
             if(input.equals("quit")){
                 System.out.println("退出");
                 break;
             }

         }
            socket.close();





        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    writer.close();
                    socket.close();
                    System.out.println("关闭socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }



    }

}
