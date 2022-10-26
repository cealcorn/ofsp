package client;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class client {
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    // TO DO: implement the client part that will work with the server part and DOWNLOAD a file from the server
    //          implement the delete command
    //          follow server.java to write the code for this .java
    // 8080

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println(
                    "Usage: java client <server_ip> <server_port>"
            );
            return;
        }

        int serverPort = Integer.parseInt(args[1]);
        String serverIP = args[0];

        char command;

        do {
            Scanner keyboard = new Scanner(System.in);
            System.out.println(
                    "Enter a command:\n" +
                            "U - Upload\n" +
                            "D - Download\n" +
                            "L - List\n" +
                            "R - Rename\n" +
                            "M - Move\n" +
                            "H - Help\n" +
                            "Q - Quit\n"
            );
            command = keyboard.nextLine().toUpperCase().charAt(0);

            ByteBuffer buffer;
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(serverIP, serverPort));
            switch (command) {
                // CASE 'U' upload
                case 'U':
                    System.out.println(
                            "Enter path of the file to upload: "
                    );
                    String filePath = keyboard.nextLine();
                    buffer = ByteBuffer.wrap(("U" + filePath).getBytes());
                    channel.write(buffer);

                    if (getServerCode(channel) != 'C') { // replaced F from canvas code
                        System.out.println(
                                "Server failed to serve the request."
                        );
                        //TODO: fix this code to extract string from server for error message
                        System.out.println(
                                "Error type: " + getServerCode(channel)
                        );
                    } else {
                        System.out.println(
                                "The request was accepted."
                        );
                        try (Socket socket = new Socket("localhost", 8080)) { // local host?
                            dataInputStream = new DataInputStream(socket.getInputStream());
                            dataOutputStream = new DataOutputStream(socket.getOutputStream());

                            sendFile(filePath);

                            dataInputStream.close();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                // CASE 'D' download
                case 'D':
                    System.out.println(
                            "Enter name of the file to download: "
                    );
                    String fileName = keyboard.nextLine();
                    buffer = ByteBuffer.wrap(("D" + fileName).getBytes());
                    channel.write(buffer);

                    // shut down output on client side after client is done sending to server
                    channel.shutdownOutput();

                    // receive server reply code
                    if (getServerCode(channel) != 'C') { // replaced F from canvas code
                        System.out.println(
                                "Server failed to serve the request."
                        );
                    } else {
                        System.out.println(
                                "The request was accepted."
                        );
                        Files.createDirectories(Paths.get("./downloaded"));

                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                "./downloaded/" + fileName, true));
                        ByteBuffer data = ByteBuffer.allocate(1024);
                        int bytesRead;

                        while ((bytesRead = channel.read(data)) != -1) {
                            data.flip();
                            byte[] a = new byte[bytesRead];
                            data.get(a);
                            String serverMessage = new String(a);
                            bw.write(serverMessage);
                            data.clear();
                        }
                        bw.close();
                    }
                    channel.close();
                    break;

                // CASE 'L' list

                // CASE 'R' rename

                // CASE 'M' move

                // CASE 'H' help

            }
        } while (command != 'Q');
    }

    private static char getServerCode(SocketChannel channel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int bytesToRead = 1;

        //make sure we read the entire server reply
        while((bytesToRead -= channel.read(buffer)) > 0);

        buffer.flip();
        byte[] a = new byte[1];
        buffer.get(a);
        char serverReplyCode = new String(a).charAt(0);

        //System.out.println(serverReplyCode);

        return serverReplyCode;
    }

    private static void sendFile(String path) throws Exception {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        dataOutputStream.writeLong(file.length());
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
}
