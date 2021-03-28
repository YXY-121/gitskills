package bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Handler implements  Runnable{
    private server server;
    private Socket socket;
    public Handler(server server,Socket socket){
        this.server=server;
        this.socket=socket;
    }

    @Override
    public void run() {
        try {
            //存储新上线用户
            server.addClient(socket);
            String msg=null;
            BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while((msg=reader.readLine())!=null){
                System.out.println("客户端"+socket.getPort()+"发送信息:"+msg);
                //转发信息给别的用户
                server.forward(socket,"客户端"+socket.getPort()+"发送"+msg+"\n");
                boolean b = server.readyToQUit(msg);
                if(b){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.delClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
