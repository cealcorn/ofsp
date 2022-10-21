package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class server{
    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;


    server(){
    }

    private static void print_usage(){
        System.out.println("USAGE: java ServerTCP.java listening_port");
    }

    public static void main (String[] args){
        if(args.length != 1){
            print_usage();
            return;
        }

        int port;
        try{
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex){
            System.out.println("The port number could not be parsed. please input a valid integer between 1 and 65535");
            print_usage();
            return;
        }

        if(port > 65535){
            System.out.println("The port is above the range, please input a port between 1 and 65535");
            return;
        } else if(port < 1){
            System.out.println("The port is below the range, please input a port between 1 and 65535");
        }

        ServerSocketChannel listening_port;

        try{
            listening_port = ServerSocketChannel.open();
        } catch (IOException ex) {
            System.out.println("IOError: " + ex);
            return;
        }

        try{
            listening_port.socket().bind(new InetSocketAddress(port));
        } catch (IOException ex) {
            System.out.println("IOError: " + ex);
            return;
        }

        while(true){
            SocketChannel ServeSocket;
            try{ //creates the client specific logical connection
                ServeSocket = listening_port.accept();
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }


            ByteBuffer buffer = ByteBuffer.allocate(MAX_CLIENT_MESSAGE_LENGTH); //makes a buffer of the maximum cliemt length

            try{ //read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex){
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();

            char command = (char)buffer.get();

            String humanReadableCommand = "";

            switch(command){
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
            char confirmChar = humanReadableCommand != "Unknown" ? 'C' : 'E';
            try {
                ServeSocket.write(ByteBuffer.allocate(1).put(0, (byte) confirmChar));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            buffer = ByteBuffer.allocate(MAX_CLIENT_MESSAGE_LENGTH);

            try{
                ServeSocket.read(buffer);
            } catch (IOException ex){
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

            //creates new socket and listens for data
            try{
                ServeSocket = listening_port.accept();
            } catch (IOException ex) {
                System.out.println("IOError: " + ex);
                return;
            }

            ByteBuffer payload = buffer;

            try{ //read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex){
                System.out.println("IOError: " + ex);
                return;
            }

            switch(command){
                case 'D':
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


            try{
                ServeSocket.write(buffer);
                ServeSocket.close();

            }catch (IOException ex){
                System.out.println("IOError: " + ex);
                return;
            }
            if(humanReadableCommand == "Unknown"){
                throwError(ServeSocket, "Command not valid");
            }
        }
    }

    private static void throwError(SocketChannel sendSocket, String error){
        ByteBuffer messageBuffer = ByteBuffer.allocate(error.length());
        byte[] errorAsBytes = new byte[error.length()];
        for (int i=0; i<error.length();i++){
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


}