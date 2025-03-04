import java.io.IOException;
import java.net.*;
import java.util.Scanner;

class ChatThread extends Thread {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;

    public ChatThread(MulticastSocket socket, InetAddress group, int port) {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        while (!GroupChat.finished) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("\n" + message);
            } catch (IOException e) {
                System.out.println("Error receiving message: " + e.getMessage());
            }
        }
    }
}

public class GroupChat {
    public static volatile boolean finished = false;
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String name = sc.nextLine();

            MulticastSocket socket = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            SocketAddress groupAddress = new InetSocketAddress(group, PORT);
            socket.joinGroup(groupAddress, networkInterface);

            Thread readThread = new ChatThread(socket, group, PORT);
            readThread.start();

            System.out.println("Connected to group chat. Type 'exit' to leave.");
            while (true) {
                String message = sc.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    finished = true;
                    break;
                }
                String formattedMessage = name + ": " + message;
                byte[] buffer = formattedMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
            }

            socket.leaveGroup(groupAddress, networkInterface);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}