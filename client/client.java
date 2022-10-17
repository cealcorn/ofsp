package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

    public class client {

        public static void main(String[] args) throws IOException {

            if (args.length != 2) {
                System.out.println(
                        "Usage: ClientTCP <server_ip> <server_port>"
                );
                return;
            }

            int serverPort = Integer.parseInt(args[1]);
            String serverIP = args[0];

            System.out.println(
                    "Press a key, then press enter."
            );
            Scanner keyboard = new Scanner(System.in);
            char c = keyboard.nextLine().charAt(0);

            byte[] b = new byte[1];
            b[0] = (byte) c;
            ByteBuffer buffer = ByteBuffer.wrap(b);

            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(serverIP, serverPort));
            sc.write(buffer);

            ByteBuffer rb = ByteBuffer.allocate(1);
            sc.read(rb);
            rb.rewind();
            System.out.println(
                    "Message from server: " + (char) rb.get()
            );

            rb.flip();
            sc.write(buffer);
            sc.close();
        }

    }
}
