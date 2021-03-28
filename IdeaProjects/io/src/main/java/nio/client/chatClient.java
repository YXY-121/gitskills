package nio.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author: yxy
 * Date: 2021/3/28
 * Time: 18:33
 * 描述:
 */
public class chatClient {
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private String host;
    private int port;
    private SocketChannel client;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");
    public chatClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }
    public chatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    Closeable  closeable;
    public void start(){

        try {

            //获得channel
             client = SocketChannel.open();
            client.configureBlocking(false);
            //获得selector 监听服务器发来的东西
            selector = Selector.open();

            //监听连接状态
            client.register(selector, SelectionKey.OP_CONNECT);
            //向服务器发起连接
            client.connect(new InetSocketAddress(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT));

            while(true){
                //select()有反应 说明监听的connect连上了 或者连接上的东西动了- -？
                // if(selector.select()!=0){
                    selector.select();
                    //连上之后就处理
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for(SelectionKey key:selectionKeys){
                        handle(key);
                    }
                    selectionKeys.clear();
        //    }

        }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }


    }

    public void handle(SelectionKey key) throws IOException {
        //连接上了
        if(key.isConnectable()){
            //
            SocketChannel  client = (SocketChannel) key.channel();
            if(client.isConnectionPending()){//如果返回true 就是连接就绪，false 继续等待，还没有处理完连接的请求 要继续等待
                    System.out.println("我连接上了");
                    client.finishConnect();//建立好后就不用等待连接了
                //处理用户输入
                new Thread(new UserInputHandler(this)).start();

            }
            //连接到服务器后，就有可能收到服务器转发过来的信息
            client.register(selector,SelectionKey.OP_READ);
        }
        else if(key.isReadable()){
            System.out.println("有新消息");
            SocketChannel  client = (SocketChannel) key.channel();
           String msg= receive(client);
           if(msg.isEmpty())
            {
              //服务器异常
              close(selector);
            }else{
               System.out.println(client.getLocalAddress()+msg);

           }
        }



    }
    //通道 到 buffer
    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0);//循环读

        rBuffer.flip();

        return String.valueOf(charset.decode(rBuffer));

    }
    //buffer 到 通道
    public void send(String msg) throws IOException {
        if(msg.isEmpty())
            return;
      /*  if(!msg.isEmpty()){
            wBuffer.clear();
            wBuffer.put(charset.encode(msg));
            wBuffer.flip();
            while(wBuffer.hasRemaining())
                client.write(wBuffer);//todo fuck?
            if(readyToQuit(msg));{
                close(selector);
            }
        }*/


        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }

        // 检查用户是否准备退出
        if (readyToQuit(msg)) {
            close(selector);
        }

    }
    public static void main(String args[]){
        chatClient chatClient=new chatClient();
        chatClient.start();
    }
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
