package nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author: yxy
 * Date: 2021/3/28
 * Time: 14:57
 * 描述:
 */
public class chatServer {
    private  static final int DEFAULT_PORT=8888;
    private static final String DEFAULT_ADDRESS="127.0.0.1";
    private static final String QUIT = "quit";
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer rbuffer=ByteBuffer.allocate(1024);
    private ByteBuffer wbuffer=ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("UTF-8");
    public void start(){
        try {
            //获得serverSocketChannel
          //  serverSocketChannel=new ServerSocket(DEFAULT_PORT).getChannel();


            // serverSocketChannel.socket()是获得该channel上的ServerSocket 为该socket绑定端口
            //我感觉和第一行同一种写法
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));

            serverSocketChannel.configureBlocking(false);


            //初始化获得selector
            selector=Selector.open();

            //注册serversocket到selector里
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，端口为"+DEFAULT_PORT+"...");

            //启动后 通过select轮询，通过轮询来查看是否有哪个小动作
            while(true){
                selector.select();//是一个阻塞函数，直到至少有一个channel被选中（被监视到 有活动发生）才会返回，不然会一直停留在这个函数里
                //从select函数返回后，必定有channel是活动的 现在我们来获取它
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key:selectionKeys){
                    handle(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close(Closeable closeable) throws IOException {
            closeable.close();
    }

    public void handle(SelectionKey key) throws IOException {
        if( key.isAcceptable()){
            //获得这个key对应的channel
            //todo 为啥这个是server
            //todo 因为accept是发生在serverSocke的通道上的
                ServerSocketChannel server = (ServerSocketChannel)key.channel();

                SocketChannel client = server.accept();

                client.configureBlocking(false);

                client.register(selector,SelectionKey.OP_READ);
                System.out.println(getClientName(client) + "已连接");

        }
        //todo 为啥这个是client
        // 因为可读 ->就是客户端
       else if(key.isReadable()){
            SocketChannel client = (SocketChannel)key.channel();
            String msg=recive(client);


            if(!msg.isEmpty()){
                System.out.println(getClientName(client)+"发送"+msg);
                forward(msg,client);
            }
            else{
                key.cancel();
                //wakeup
                selector.wakeup();

            }
            if(readyToQuit(msg)){
                key.cancel();
                selector.wakeup();
                System.out.println(getClientName(client) + "已断开");
            }
        }




    }
    //从客户端里接收读信息
    //从管道里读到缓冲区
    public String recive( SocketChannel client) throws IOException {
        rbuffer.clear();
        //将数据读出来
        while(client.read(rbuffer)>0);

        rbuffer.flip();

        return String.valueOf(charset.decode(rbuffer));

    }

    //转发给其他客户端，除了它自己
    public void forward(String msg,SocketChannel client) throws IOException {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            SelectableChannel channel = key.channel();
            //todo 为啥
            //todo 因为跳过服务器channel
            if (channel instanceof ServerSocketChannel) {

                continue;

            } else {
                if(!client.equals(channel)){
                    //清空上一次的缓存
                    wbuffer.clear();
                    //将信息写入buffer缓冲区
                    wbuffer.put(charset.encode(getClientName(client)+":"+msg));
                    //转换成读模式 limit在当前的position位置，position回归前面
                    wbuffer.flip();

                    //开始被读=被写入channel
                    while(wbuffer.hasRemaining()){
                        ((SocketChannel)channel).write(wbuffer);
                    }
                }

            }
        }
    }
    private String getClientName(SocketChannel client) {
        return "客户端[" + client.socket().getPort() + "]";
    }

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }
    public static void main(String args[]){
        chatServer server=new chatServer();
        server.start();
    }
}
