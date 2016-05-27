package io.deltawave.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by will on 5/26/16.
 */
public class Client {

    Socket socket;
    PrintWriter out;
    BufferedReader in;
    BufferedReader stdIn;

    public Client() throws IOException {
        socket = new Socket("localhost", 2000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

    private void receiveMessage(String message) {
        System.out.println("Received from server: " + message);
        String[] messageParts = message.split(" ");
        if(messageParts.length > 0) {
            String messageType = messageParts[0];
            String messageBody = message.substring(messageType.length(), message.length()).trim();
            processMessage(messageType, messageBody);
        }
    }

    public void processMessage(String messageType, String messageBody) {
        switch(messageType) {
            case "000":
                sendHeartbeatReply();
                break;
        }
    }

    public void receiveCommand(String command) {
        //User typed something
    }

    public void sendHeartbeatReply() {
        send("001", "Heartbeat reply");
    }

    public void send(String messageType, String messageBody) {
        System.out.println("Sending to server: " + messageType + " " + messageBody);
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
