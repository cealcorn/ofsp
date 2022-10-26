package server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class server {
    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;
    private static final int MAX_CLIENT_FILE_PAYLOAD_SIZE = 1048576; //allow a 1MB file (includes the file metadata/path/name)

    server thisServer;

    // private static boolean Processing = false; not needed because of single
    // threading

    server() {
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
        }

        ServerSocketChannel listening_port;

        try {
            listening_port = ServerSocketChannel.open();
        } catch (IOException ex) {
            System.out.println("IOError: " + ex);
            return;
        }

        try {
            listening_port.socket().bind(new InetSocketAddress(port));
        } catch (IOException ex) {
            System.out.println("IOError: " + ex);
            return;
        }

        while (true) {
            SocketChannel ServeSocket;
            try { // creates the client specific logical connection
                ServeSocket = listening_port.accept();
                // Processing = true; not needed because of single threading (was a blocking
                // call)
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            /*
             * not needed because of single threading (was a blocking call)
             * while(!Processing){
             * //wait and block the processing to prevent corruption
             * }
             */

            ByteBuffer buffer = ByteBuffer.allocate(1); // makes a buffer of the command length

            try { // read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();

            char command = (char) buffer.get();

            String humanReadableCommand = "";

            // figures out what command the user wants and verifies if server can do it
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
            buffer.rewind();
            if (humanReadableCommand != "Unknown") {
                try {
                    ServeSocket.write(ByteBuffer.allocate(1).put(0, (byte) 'C'));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                throwError(ServeSocket, "Unknown command: " + command);
            }

            buffer = ByteBuffer.allocate(MAX_CLIENT_FILE_PAYLOAD_SIZE);

            try {
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();

            ByteBuffer payload = buffer;

            try { // read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            // switch payload to respective command handlers
            switch (command) {
                case 'D':
                    File downloadFile;
                    try {
                        downloadFile = checkFileAvailability(payload, ServeSocket);
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
                    sendFile(downloadFile, ServeSocket);
                    break;
                case 'U':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                case 'L':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                case 'R':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                case 'M':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                case 'H':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                case 'T':
                    throwError(ServeSocket, "Command not implemented");
                    break;
                default:
                    throwError(ServeSocket, "Command not valid. Closing connection");
                    break;
            }


            try {
                ServeSocket.close();
            } catch (IOException e) {
                System.out.println("Unable to close socket. Error: " + e.getMessage());
            }
        }
    }

    // sends the error message over the TCP socket
    private static void throwError(SocketChannel sendSocket, String error) {
        ByteBuffer messageBuffer = ByteBuffer.allocate(error.length());
        byte[] errorAsBytes = new byte[error.length() + 1];
        errorAsBytes[0] = (byte) 'E';
        for (int i = 1; i <= error.length(); i++) {
            errorAsBytes[i] = (byte) error.charAt(i-1);
        }
        messageBuffer.put(errorAsBytes, 0, error.length());
        try {
            sendSocket.write(messageBuffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // verifies that the file is a file and the server (aka user) has suffiecient permissions to read it
    private static File checkFileAvailability(ByteBuffer name, SocketChannel sendSocket)
            throws BadPermissionsException, IncorrectFileNameException, IsDirectoryException {
        File file = new File("data/" + name.toString());
        if (!file.exists()) {
            throwError(sendSocket, "file does not exist");
            throw new server.IncorrectFileNameException("This file [" + name.toString() +  "] does not exist in the server data directory");
        }
        if (file.isDirectory()) {
            throwError(sendSocket, "file is a directory");
            throw new server.IsDirectoryException("The file specified is a directory");
        }
        if (file.canRead()) {
            return file;
        } else {
            throwError(sendSocket, "Server error occured, contact server admin if this keeps occuring; CODE: READ DENIED");
            throw new server.BadPermissionsException("this file is not readable");
        }
    }

    private static void sendFile(File file, SocketChannel sendSocket){
        //added 'C' to ensure that first character is known and we done accidentally send a file as an error
        String sendingFile = "C" + file.toString();
        ByteBuffer sendingBuffer = ByteBuffer.wrap(sendingFile.getBytes());
        try {
            sendSocket.write(sendingBuffer);
        } catch (IOException e) {
            throwError(sendSocket, "Server error occured, contact server admin if this keeps occuring; CODE: FILE UPLOAD IO ERROR");
            System.out.println("Error sending file: IO ERROR: " + e.getMessage());
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

}