package nio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: yxy
 * Date: 2021/3/28
 * Time: 19:28
 * 描述:
 */
public class UserInputHandler  implements  Runnable{
    private chatClient chatClient;

    public UserInputHandler(chatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        BufferedReader consoleReader =
                new BufferedReader(new InputStreamReader(System.in));

        while(true){
            try {
                String s = consoleReader.readLine();
                chatClient.send(s);
                if(chatClient.readyToQuit(s)){
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
