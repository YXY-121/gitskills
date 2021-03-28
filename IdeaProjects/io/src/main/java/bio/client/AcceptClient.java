package bio.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class AcceptClient {
    private int DEFAULT_PORT=8888;
    private final String QUIT="quit";
    final String DEFAULT_SERVER_HOST="127.0.0.1";
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    public AcceptClient() throws IOException {
    }

    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }
    public String receive() throws IOException {
        String s=null;
        if(!socket.isInputShutdown()){//保证输入流没有关闭
            s = reader.readLine();
        }
        return s;
    }
    public boolean readyToQuit(String msg){
        return msg.equals(QUIT);

    }

    public void start(){
        try {
            //创建socket
            socket=new Socket(DEFAULT_SERVER_HOST,DEFAULT_PORT);
            //创建io
            reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //处理用户输入
            new Thread(new UserInputClient(this)).start();

            //读取服务器转发的信息
            String msg=null;
            while((msg=receive())!=null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
         close();
        }

    }
    public void close(){
        try {
            if(writer!=null){
           //     socket.close();
                writer.close();
                System.out.println("关闭socket");
              //  reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static  void main(String args[]){
        AcceptClient client= null;
        try {
            client = new AcceptClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.start();
    }

}
