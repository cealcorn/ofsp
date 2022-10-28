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
    in = new BufferedReader(new
    InputStreamReader(clientSocket.getInputStream()));
    System.out.println("Created input stream");
    out.println("D");
    System.out.println("Sent command to server");
    String resp1 = in.readLine();
    System.out.println(resp1);
    if(resp1.charAt(0) == 'C'){
    out.println("Dtest.txt");
    System.out.println("Responce 2: " + in.readLine());
    }
    } catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
    }
    }

    // private static Socket clientSocket;
    // private static PrintWriter out;
    // private static BufferedReader in;

    // public static void startConnection(String ip, int port) {
    //     try {
    //         clientSocket = new Socket(ip, port);
    //         out = new PrintWriter(clientSocket.getOutputStream(), true);
    //         in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }

    // public static String sendMessage(String msg) {
    //     out.println(msg);
    //     String resp;
    //     try {
    //         resp = in.readLine();
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //         return "hi";
    //     }
    //     return resp;
    // }

    // public static void stopConnection() {
    //     try {
    //         in.close();
    //         out.close();
    //         clientSocket.close();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public static void main(String[] args) {
    //     startConnection("127.0.0.1", 6666);
    //     String response = sendMessage("hello server");
    //     System.out.println(response);
    // }
}
