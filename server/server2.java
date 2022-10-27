package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server2 {
    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;
    private static final int MAX_CLIENT_FILE_PAYLOAD_SIZE = 1048576; // allow a 1MB file (includes the file
                                                                     // metadata/path/name)
    private static ServerSocket serveSocket;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static PrintWriter out;

    server2 thisServer;

    server2() {
        thisServer = this;
    }

    private static void print_usage() {
        System.out.println("USAGE: java ServerTCP.java listening_port");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            print_usage();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.out.println("The port number could not be parsed. please input a valid integer between 1 and 65535");
            print_usage();
            return;
        }

        if (port > 65535) {
            System.out.println("The port is above the range, please input a port between 1 and 65535");
            return;
        } else if (port < 1) {
            System.out.println("The port is below the range, please input a port between 1 and 65535");
            return;
        }

        try {
            serveSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Unable to bind to port " + String.valueOf(port) + ". Error: " + e.getMessage());
            return;
        }

        try {
            clientSocket = serveSocket.accept();
        } catch (IOException e) {
            System.out.println("Unable to create client connection");
            return;
        }

        // adds ability to switch to different parts of the code
        boolean recievedCommand = false;
        boolean recievedPayload = false;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("could not create the buffer");
        }

        String currentInput;
        String humanReadableCommand;
        char command = 'z';
        String payload;
        Boolean isError = false;
        try {
            while ((currentInput = in.readLine()) != null) {
                if(isError){
                    break;
                }
                if (!recievedCommand) {
                    command = currentInput.charAt(0);
                    switch (command) {
                        case 'D':
                            humanReadableCommand = "Download";
                            break;
                        case 'U':
                            humanReadableCommand = "Upload";
                            break;
                        case 'L':
                            humanReadableCommand = "List";
                            break;
                        case 'R':
                            humanReadableCommand = "Rename";
                            break;
                        case 'M':
                            humanReadableCommand = "Move";
                            break;
                        case 'H':
                            humanReadableCommand = "Help";
                            break;
                        case 'T':
                            humanReadableCommand = "Delete";
                            break;
                        default:
                            humanReadableCommand = "Unknown";
                            break;
                    }
                    System.out.println("Command recieved from client: " + humanReadableCommand);
                    recievedCommand = true;
                    if (humanReadableCommand == "Unknown") {
                        recievedCommand = false;
                        throwError("Unknown Command. Please retry");
                    }
                    continue; // go wait for the next line to come in
                } else if (!recievedPayload) {
                    switch (command) {
                        case 'D':
                            payload = currentInput.substring(1);
                            File downloadFile;
                            try {
                                downloadFile = checkFileAvailability(payload);
                            } catch (BadPermissionsException e) {
                                System.out.println("Error from client download command: " + e.getMessage());
                                break;
                            } catch (IncorrectFileNameException e) {
                                System.out.println("Error from client download command: " + e.getMessage());
                                break;
                            } catch (IsDirectoryException e) {
                                System.out.println("Error from client download command: " + e.getMessage());
                                break;
                            }
                            try{
                                sendFile(downloadFile);
                            } catch(EmptyFileException e){
                                throwError("File is empty, nothing to send");
                                System.out.println("File is Empty for [" + downloadFile.getName() + "].");
                            }
                            break;
                        case 'U':
                            humanReadableCommand = "Upload";
                            break;
                        case 'L':
                            humanReadableCommand = "List";
                            break;
                        case 'R':
                            humanReadableCommand = "Rename";
                            break;
                        case 'M':
                            humanReadableCommand = "Move";
                            break;
                        case 'H':
                            humanReadableCommand = "Help";
                            break;
                        case 'T':
                            humanReadableCommand = "Delete";
                            break;
                        default:
                            throwError(
                                    "You were not supposed to get to this point... contact the admin to fix this error along with how you got here");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read from input. Error: " + e);
        }

            
        try {
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Unable to close client socket. Error: " + e.getMessage());
        }

        try {
            out.close();
            serveSocket.close();
        } catch (IOException e) {
            System.out.println("Unable to close server socket. Error: " + e.getMessage());
        }
        main(args);
    }

    // sends the error message over the TCP socket
    private static void throwError(String error) {
        out.println("E" + error);
    }

    private static void sendFile(File file) throws EmptyFileException{
        String outputBuffer = file.toString();
        if(!outputBuffer.isEmpty()){
            out.println("C" + outputBuffer);
        } else {
            throw new server2.EmptyFileException("File is empty");
        }
    }

    // verifies that the file is a file and the server (aka user on the computer) has suffiecient
    // permissions to read it
    private static File checkFileAvailability(String name)
            throws BadPermissionsException, IncorrectFileNameException, IsDirectoryException {
        File file = new File("data/" + name);
        if (!file.exists()) {
            throwError("file does not exist");
            throw new server2.IncorrectFileNameException(
                    "This file [" + name.toString() + "] does not exist in the server data directory");
        }
        if (file.isDirectory()) {
            throwError("file is a directory");
            throw new server2.IsDirectoryException("The file specified is a directory");
        }
        if (file.canRead()) {
            return file;
        } else {
            throwError("Server error occured, contact server admin if this keeps occuring; CODE: READ DENIED");
            throw new server2.BadPermissionsException("this file is not readable");
        }
    }

    private static class BadPermissionsException extends Exception {
        public BadPermissionsException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class IncorrectFileNameException extends Exception {
        public IncorrectFileNameException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class IsDirectoryException extends Exception {
        public IsDirectoryException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class EmptyFileException extends Exception{
        public EmptyFileException(String errorMessage) {
            super(errorMessage);
        }
    }

}
