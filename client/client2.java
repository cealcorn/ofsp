package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class client2 {
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java client <server_ip> <server_port>");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);
        String serverIP = args[0];

        char command;

        do {                                                                                           // BEGIN DO-WHILE
            Scanner keyboard = new Scanner(System.in);
            System.out.println(
                    """
                            Enter a command:
                            U - Upload
                            D - Download
                            L - List
                            R - Rename
                            M - Move
                            T - Delete
                            H - Help
                            Q - Quit
                    """
            );
            command = keyboard.nextLine().toUpperCase().charAt(0);

            switch (command) {
                case 'U':                                                                                      // UPLOAD
                    clientSocket = new Socket("localhost", 8080);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));

                    if (in.readLine().charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + in.readLine());
                    } else {
                        System.out.println("The request was accepted.");
                        System.out.println("Enter path of the file to upload:");
                        String filePath = keyboard.nextLine();
                        ByteBuffer buffer = ByteBuffer.wrap((filePath + "0x00").getBytes());
                        out.write(String.valueOf(buffer));

                        if (in.readLine().charAt(0) != 'C') {
                            System.out.println("Server was unable to process upload.");
                            System.out.println("ERROR: " + in.readLine());
                        } else {
                            System.out.println("Upload request was successful.");
                        }
                        in.close();
                        out.close();
                        break;
                    }

                case 'D':                                                                                    // DOWNLOAD
                    clientSocket = new Socket("localhost", 8080);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));

                    System.out.println("Enter the name of the file to download:");
                    String fileName = keyboard.nextLine();
                    ByteBuffer buffer = ByteBuffer.wrap(("D" + fileName).getBytes());
                    out.write(String.valueOf(buffer));

                    //clientSocket.shutdownOutput();

                    if (in.readLine().charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + in.readLine());
                    } else {
                        System.out.println("Download request was accepted.");
                        Files.createDirectories(Paths.get("./downloaded"));

                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                "./downloaded/" + fileName, true));
                        ByteBuffer data = ByteBuffer.allocate(1024);
                        int bytesRead;

                        while ((bytesRead = in.read()) != -1) {
                            data.flip();
                            byte[] a = new byte[bytesRead];
                            data.get(a);
                            String serverMessage = new String(a);
                            bw.write(serverMessage);
                            data.clear();
                        }
                        bw.close();

                    }
                    in.close();
                    out.close();
                    break;

                case 'L':                                                                                        // LIST
                    clientSocket = new Socket("localhost", 8080);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));

                    if (in.readLine().charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + in.readLine());
                    } else {
                        System.out.println("The request was accepted.");
                        System.out.println(in.readLine());
                    }

                    in.close();
                    out.close();
                    break;

                case 'R':                                                                                      // RENAME
                    clientSocket = new Socket("localhost", 8080);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));

                    if (in.readLine().charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + in.readLine());
                    } else {
                        System.out.println("The request was accepted.");

                        System.out.println("Enter name of file to rename:");
                        String currentFileName = keyboard.nextLine();
                        System.out.println("Enter new name for " + currentFileName + ":");
                        String newFileName = keyboard.nextLine();

                        String fileSend = currentFileName + "," + newFileName; // split string at ',' when received?
                        out.write(fileSend);
                    }

                    in.close();
                    out.close();
                    break;

                case 'M':                                                                                        // MOVE
                    clientSocket = new Socket("localhost", 8080);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));

                    if (in.readLine().charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + in.readLine());
                    } else {
                        System.out.println("Enter name of file to move:");
                        String fileToMove = keyboard.nextLine();
                        System.out.println("Enter new file location:");
                        String newFileLocation = keyboard.nextLine();

                        String moveSend = fileToMove + "," + newFileLocation; // split string at ',' when received?
                        out.write(moveSend);
                    }

                    in.close();
                    out.close();
                    break;

//                case 'T':                                                                                    // DELETE
//                    clientSocket = new Socket("localhost", 8080);
//                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                    out = new PrintWriter(clientSocket.getOutputStream(), true);
//                    clientSocket.connect(new InetSocketAddress(serverIP, serverPort));
//
//                    break;

                case 'H':                                                                                        // HELP
                    System.out.println("Contact a system administrator.");
                    break;
            }

        } while (command != 'Q');                                                                        // END DO-WHILE
    }
}