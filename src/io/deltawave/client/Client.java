package io.deltawave.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 * Created by will on 5/26/16.
 */
public class Client {

    private String address;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;

    public Client() throws IOException {

        findServer();

        connectToServer();

        Thread socketThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        receiveMessage(in.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        socketThread.start();

        stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                receiveCommand(stdIn.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void findServer() throws IOException {

        //Wait for broadcast
        System.out.println("Waiting for server broadcast...");
        DatagramSocket dsocket = new DatagramSocket(2000, InetAddress.getByName("0.0.0.0"));
        byte[] bytes = new byte[1024];
        DatagramPacket dpacket = new DatagramPacket(bytes, 1024);
        dsocket.receive(dpacket);

        String[] messageParts = new String(bytes, 0, dpacket.getLength()).split(" ");
        if(messageParts.length == 3) {
            address = messageParts[0];
            String game = messageParts[1];
            String players = messageParts[2];

            System.out.println("Address: " + address);
            System.out.println("Game: " + game);
            System.out.println("Players: " + players);
        }
    }

    public void connectToServer() throws IOException {
        socket = new Socket(InetAddress.getByName(address), 2000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void receiveMessage(String message) {
        String[] messageParts = message.split(" ");
        if(messageParts.length > 0) {
            String messageType = messageParts[0];
            String messageBody = message.substring(messageType.length(), message.length()).trim();
            processMessage(messageType, messageBody);
        }
    }

    public void processMessage(String messageType, String messageBody) {
        if(messageType.equals("000")) {
            sendHeartbeatReply();
        } else {
            System.out.println(messageType + " " + messageBody);
        }
    }

    public void receiveCommand(String command) {
        //User typed something
        out.println(command);
    }

    public void sendHeartbeatReply() {
        send("001", "Heartbeat reply");
    }

    public void send(String messageType, String messageBody) {
        out.println(messageType + " " + messageBody);
    }

    public static void main(String[] args) {
        try {

            Client client = new Client();

        } catch (IOException e) {
            System.out.println("There was an IO error.");
            e.printStackTrace();
        }
    }
}
