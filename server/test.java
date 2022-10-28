package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class test {
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    test() {
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("os.name"));
        try {
            clientSocket = new Socket("127.0.0.1", 8088);
            System.out.println("created socket");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("Created output stream");
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Created input stream");
            out.println("D");
            System.out.println("Sent command to server");
            String resp1 = in.readLine();
            System.out.println(resp1);
            if (resp1.charAt(0) == 'C') {
                out.println("Dtest.txt");
                System.out.println("Responce 2: " + in.readLine());
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
