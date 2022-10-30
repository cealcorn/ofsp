package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class client2 {
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String directorySeperator;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java client <server_ip> <server_port>");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);
        String serverIP = args[0];

        getDirectorySeperator();
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

            clientSocket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String response1;
            String response2;

            switch (command) {
                case 'U':
                    out.println("U");
                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("The request was accepted.");
                        System.out.println("Enter path of the file to upload:");
                        String filePath = keyboard.nextLine();

                        File file = new File("data\\" + filePath); // data\\ is base directory
                        out.println("U" + "data\\" + file + "\0");

                        response2 = (in.readLine());

                        if (response2.charAt(0) != 'C') {
                            System.out.println("Server was unable to process upload.");
                            System.out.println("ERROR: " + response2.substring(1));
                        } else {
                            System.out.println("Upload request was successful.");
                        }

                        break;
                    }

                case 'D':
                    out.println("D");

                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("Download request was accepted.");
                        System.out.println("Enter the name of the file to download:");
                        String fileName = keyboard.nextLine();

                        fileName = "D" + fileName;
                        out.println(fileName);

                        Files.createDirectories(Paths.get("./downloaded"));
                        response2 = (in.readLine());
                        createFile(correctDirectorySeperator(fileName.substring(1)),
                                response2.substring(1));
                        System.out.println("File created.");
                    }

                    break;

                case 'L':
                    out.println("L");

                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("The request was accepted.");
                        System.out.println(in.readLine().substring(1));
                    }

                    break;

                case 'R':
                    out.println("R");

                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("The request was accepted.");

                        System.out.println("Enter name of file to rename:");
                        String currentFileName = keyboard.nextLine();
                        System.out.println("Enter new name for " + currentFileName + ":");
                        String newFileName = keyboard.nextLine();

                        String fileSend = "R" + currentFileName + "\0" + newFileName;
                        out.println(fileSend);
                    }

                    break;

                case 'M':
                    out.println("M");

                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("Enter name of file to move:");
                        String fileToMove = keyboard.nextLine();
                        System.out.println("Enter new file location:");
                        String newFileLocation = keyboard.nextLine();

                        String moveSend = "M" + fileToMove + "\0" + newFileLocation;
                        out.println(moveSend);
                    }

                    break;

                case 'T':
                    out.println("T");

                    response1 = (in.readLine());

                    if (response1.charAt(0) != 'C') {
                        System.out.println("Server failed to serve the request.");
                        System.out.println("ERROR: " + response1.substring(1));
                    } else {
                        System.out.println("Enter the name of file to delete:");
                        String fileToDel = keyboard.nextLine();

                        fileToDel = "T" + fileToDel;
                        out.println(fileToDel);
                    }

                    break;

                case 'H':
                    out.println("H");

                    System.out.println("Contact a system administrator, or cope.");
                    break;
            }

        } while (command != 'Q');

        in.close();
        out.close();
    }

    private static void getDirectorySeperator(){
        String operatingSystem = System.getProperty("os.name");
        if(operatingSystem.contains("Windows")){
            directorySeperator = "\\";
        } else {
            directorySeperator = "/";
        }
    }

    private static String correctDirectorySeperator(String path){
        if(path.contains(directorySeperator)){
            return path;
        } else {
            if(directorySeperator == "/"){
                return path.replace("\\", "/");
            } else{
                return path.replace("/", "\\");
            }
        }
    }

    private static void createFile(String fileName, String content){
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter("data" + directorySeperator + fileName);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}