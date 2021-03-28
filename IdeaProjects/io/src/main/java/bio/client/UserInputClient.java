package bio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserInputClient implements  Runnable {
    AcceptClient acceptClient;
    private final String QUIT = "quit";

    public UserInputClient(AcceptClient acceptClient) {
        this.acceptClient = acceptClient;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String s = reader.readLine();

                //向服务器发送消息
                acceptClient.send(s);
                if (acceptClient.readyToQuit(s))
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
