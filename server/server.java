package server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class server {
    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;

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

            ByteBuffer buffer = ByteBuffer.allocate(MAX_CLIENT_MESSAGE_LENGTH); // makes a buffer of the maximum cliemt
                                                                                // length

            try { // read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();

            char command = (char) buffer.get();

            String humanReadableCommand = "";

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

            buffer = ByteBuffer.allocate(MAX_CLIENT_MESSAGE_LENGTH);

            try {
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();

            try {
                ServeSocket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            // creates new socket and listens for data
            try {
                ServeSocket = listening_port.accept();
                // Processing = true; not needed because of single threading (was a blocking
                // call)
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            ByteBuffer payload = buffer;

            try { // read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            switch (command) {
                case 'D':
                    try {
                        checkFileAvailability(payload, ServeSocket);
                    } catch (BadPermissionsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IncorrectFileNameException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IsDirectoryException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case 'U':
                    break;
                case 'L':
                    break;
                case 'R':
                    break;
                case 'M':
                    break;
                case 'H':
                    break;
                default:
                    break;
            }

            try {
                ServeSocket.write(buffer);
                ServeSocket.close();

            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }
            if (humanReadableCommand == "Unknown") {
                throwError(ServeSocket, "Command not valid");
            }
        }
    }

    private static void throwError(SocketChannel sendSocket, String error) {
        ByteBuffer messageBuffer = ByteBuffer.allocate(error.length());
        byte[] errorAsBytes = new byte[error.length() + 1];
        errorAsBytes[0] = (byte) 'E';
        for (int i = 1; i <= error.length(); i++) {
            errorAsBytes[i] = (byte) error.charAt(i);
        }
        messageBuffer.put(errorAsBytes, 0, error.length());
        try {
            sendSocket.write(messageBuffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static File checkFileAvailability(ByteBuffer name, SocketChannel sendSocket)
            throws BadPermissionsException, IncorrectFileNameException, IsDirectoryException {
        File file = new File("data/" + name);
        if (!file.exists()) {
            throwError(sendSocket, "file does not exist");
            throw new server.IncorrectFileNameException("This file does not exist in the server data directory");
        }
        if (file.isDirectory()) {
            throwError(sendSocket, "file is a directory");
            throw new server.IsDirectoryException("The file specified is a directory");
        }
        if (file.canRead()) {
            return file;
        } else {
            throwError(sendSocket, "file is not readable");
            throw new server.BadPermissionsException("this file is not readable");
        }
    }

    public static class BadPermissionsException extends Exception {
        public BadPermissionsException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class IncorrectFileNameException extends Exception {
        public IncorrectFileNameException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class IsDirectoryException extends Exception {
        public IsDirectoryException(String errorMessage) {
            super(errorMessage);
        }
    }

}