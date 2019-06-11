package event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Player2 {
    private ServerSocket serverSocket ;
    private Socket acceptSocket ;
    private PrintStream output ;
    private BufferedReader input ;

    public static void main(String[] args) {

        new Player2().run();
    }

    public void run(){

        try {
            serverSocket = new ServerSocket(9999);
            acceptSocket = serverSocket.accept();
            output = new PrintStream(acceptSocket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(acceptSocket.getInputStream()));

            int idx =0;

            while (acceptSocket.isConnected() && idx<10 ) {
                idx++;
                String message = input.readLine()+" "+idx;
                System.out.println("Server: "+message);
                output.println(message);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
