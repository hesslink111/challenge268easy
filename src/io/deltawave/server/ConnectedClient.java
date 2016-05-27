package io.deltawave.server;

import sun.plugin2.message.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by will on 5/24/16.
 */
public class ConnectedClient extends Thread {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private ArrayList<MessageListener> messageListeners;

    private String username;

    public ConnectedClient(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        messageListeners = new ArrayList<>();

        this.username = null;
    }

    public String getIdentity() {
        return socket.toString();
    }

    @Override
    public void run() {
        boolean connected = true;
        while(connected) {
            try {
                String line = in.readLine();
                if(line != null) {
                    receivedMessage(line);
                }
            } catch (NullPointerException e) {
                ClientsList.getInstance().removeClient(this, "Could not read from client");
                connected = false;
            } catch (IOException e) {
                ClientsList.getInstance().removeClient(this, "Could not read from client");
                connected = false;
            }
        }
    }

    public void send(String message) {
        try {
            out.println(message);
        } catch (NullPointerException ex) {
            System.out.println("Could not send message to client: " + message);
        }
    }

    private void receivedMessage(String message) {
        System.out.println("Received: " + message);
        String[] messageParts = message.split(" ");
        if(messageParts.length > 0) {
            String messageType = messageParts[0];
            String messageBody = message.substring(messageType.length(), message.length()).trim();
            //give to listeners
            (new ArrayList<>(messageListeners)).stream()
                    .forEach(ml -> ml.onMessageReceived(this, messageType.toUpperCase(), messageBody));
        }
    }

    public void addMessageListener(MessageListener ml) {
        messageListeners.add(ml);
    }

    public void removeMessageListener(MessageListener ml) {
        messageListeners.remove(ml);
    }

    public void disconnect(String message) {
        try {
            socket.close();
            System.out.println("Disconnecting client: " + message);
        } catch(IOException ex) {
            System.out.println("Had some error");
            //Fail silently
            //Probably already closed
        }
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }
}
