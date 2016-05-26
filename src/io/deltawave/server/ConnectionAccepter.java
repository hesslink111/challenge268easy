package io.deltawave.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by will on 5/24/16.
 */
public class ConnectionAccepter extends Thread {

    private ServerSocket server;
    private ClientsList cList;

    public ConnectionAccepter() {
        this.cList = ClientsList.getInstance();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(2000);
            while(true) {
                Socket socket = server.accept();
                createClient(socket);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void createClient(Socket socket) {
        try {
            ConnectedClient client = new ConnectedClient(socket);
            client.start();
            cList.addClient(client);
            System.out.println("New client connected: " + socket.getInetAddress().toString());
        } catch (IOException ex) {
            //Could not create client
            System.out.println("Error, could not create client");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
