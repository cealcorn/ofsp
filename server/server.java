package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class server{
    server(){}

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

            ByteBuffer buffer = ByteBuffer.allocate(1); //makes a buffer of 1 byte (only one character accepted)

            try{ //read the data from TCP stream
                ServeSocket.read(buffer);
            } catch (IOException ex){
                System.out.println("IOError: " + ex);
                return;
            }

            buffer.flip();
            System.out.println("Message from Client: " + (char) buffer.get());

            buffer.rewind();
            try{
                ServeSocket.write(buffer);
                ServeSocket.close();
            }catch (IOException ex){
                System.out.println("IOError: " + ex);
                return;
            }
        }
    }
}