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
        testType('D', "dir2\\test2.txt", "Download");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        testType('U', sanitizeInput("testingUpload.txt\0Hello world this is a test from my testing script. have fun.\noh wait... here is a new line"), "Upload");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        testType('L', "dir2", "List");
    }

    public static void testType(char command, String payload, String humanReadableCommand){
        try {
            clientSocket = new Socket("127.0.0.1", 8088);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("\n\nSending " + humanReadableCommand + " Command. Payload: " + payload);
        out.println(command);
        String resp1;
        String resp2;
        try {
            resp1 = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if(resp1.charAt(0) == 'C'){
            System.out.println(humanReadableCommand + " Ready Signal Recieved");
            out.println(command + payload);
            try {
                resp2 = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if(resp2.charAt(0) == 'C'){
                System.out.println(humanReadableCommand + " Confirmation recieved. Responce Text: " + resp2.substring(1));
            } else {
                System.out.println("Error recieved. Error: " + resp2.substring(1));
            }
        } else {
            System.out.println("Error recieved after command sent. Error: " + resp1.substring(1));
        }
        try {
            clientSocket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String sanitizeInput(String unsanitizedString){
        return unsanitizedString.replaceAll("\n", "\0\\n");
    }
    
}
