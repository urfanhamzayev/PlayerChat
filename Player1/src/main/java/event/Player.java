package event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Player {
    private Socket clientSocket ;
    private BufferedReader input;
    private PrintStream output ;

    public static void main(String[] args) {
        new Player().run();
    }

    public  void run(){

        try {
            clientSocket = new Socket("localhost", 9999);
            output = new PrintStream(clientSocket.getOutputStream());
            output.println("Hello");
            input = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));

            int idx =0;

            while (clientSocket.isConnected() && idx<10 ) {
                idx++;
                String message = input.readLine()+" - "+idx;
                System.out.println("Client: "+message);
                output.println(message);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
