package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class client {

    // GENERAL UPDATES:
        // Fixed System.out.println("Error type: " + getServerCode(channel).substring(1); where "channel" was spelled ..
            // "chanel" ...oops
        // Followed transaction.txt stuff for 'upload' in case below (untested currently)

    // TO DO:
        // Follow same transaction scheme for Download (if its not already fine)
        // Complete 4 (ideally five for that sweet sweet extra cred) other client-side cases

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

            switch (command) {
                // CASE 'U' UPLOAD
                case 'U':
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverIP, serverPort));
                    channel.write(command);                                 // Client sends "U"

                    if (getServerCode(channel) != 'C') {                    // alt. if getServerCode(channel) = 'E'
                        System.out.println(
                                "Server failed to serve the request."       // Server replies with "E" and the error ..
                        );                                                  // message directly after if error; ..
                        System.out.println(                                 // EX: ECommand Not Found
                                "Error type: " + getServerCode(channel).substring(1)
                        );
                    } else {                                                // Server replies with "C" when ready
                        System.out.println(
                                "The request was accepted."
                        );

                        try (Socket socket = new Socket("localhost", 8080)) { // local host?
                            System.out.println(
                                    "Enter path of the file to upload: "
                            );
                            String filePath = keyboard.nextLine();          // Client sends file with null character ..
                            ByteBuffer buffer = ByteBuffer.wrap((filePath + "0x00").getBytes()); // fileName
                            channel.write(buffer);                          // seperating file name and file data
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (getServerCode(channel) != 'C') {                // Server replies with "C" if successful
                            System.out.println(
                                    "Server was unable to process upload."
                            );
                            System.out.println(                             // Server replies with "E" and the error ..
                                    "Error type: " + getServerCode(channel).substring(1)
                            );                                              //  message directly after if error; ..
                        } else {                                            // EX: EWrite not permitted, insufficient ..
                            System.out.println(                             // permissions
                                    "Upload request was accepted."
                            );
                        }
                        channel.close();
                        break;
                    }

                // CASE 'D' DOWNLOAD
                case 'D':
                    System.out.println(
                            "Enter name of the file to download: "
                    );
                    String fileName = keyboard.nextLine();
                    ByteBuffer buffer = ByteBuffer.wrap(("G" + fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverIP, serverPort));
                    channel.write(buffer);

                    // shut down output on client side after client is done sending to server
                    channel.shutdownOutput();

                    // receive server reply code
                    if (getServerCode(channel) != 'C') { // alt. if getServerCode(channel) = 'E'
                        System.out.println(
                                "Server failed to serve the request."
                        );
                        System.out.println(
                                "Error type: " + getServerCode(channel).substring(1)
                        );
                    } else {
                        System.out.println(
                                "Download  request was accepted."
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

//    private static void sendFile(String path) throws Exception {
//        int bytes = 0;
//        File file = new File(path);
//        FileInputStream fileInputStream = new FileInputStream(file);
//
//        dataOutputStream.writeLong(file.length());
//        byte[] buffer = new byte[4*1024];
//        while ((bytes=fileInputStream.read(buffer)) != -1) {
//            dataOutputStream.write(buffer, 0, bytes);
//            dataOutputStream.flush();
//        }
//        fileInputStream.close();
//    }
}
