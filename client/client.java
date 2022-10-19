package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class client {

    // TO DO: implement the client part that will work with the server part and DOWNLOAD a file from the server
    //          implement the delete command
    //          follow server.java to write the code for this .java

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
                            "Upload - U\n" +
                            "Download - D\n" +
                            "List - L\n" +
                            "Rename - R\n" +
                            "Move - M\n" +
                            "Help - H\n" +
                            "Quit - Q\n"
            );
            command = keyboard.nextLine().toUpperCase().charAt(0);

            switch (command) {
                // CASE 'U' upload

                // CASE 'D' download
                case 'D':
                    System.out.println(
                            "Enter name of the file to donwload: "
                    );
                    String fileName = keyboard.nextLine();
                    ByteBuffer buffer = ByteBuffer.wrap(("G" + fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverIP, serverPort));
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
}
